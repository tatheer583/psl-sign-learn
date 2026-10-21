import React, { useEffect, useRef, useState } from 'react';
import type { HandLandmarker } from '@mediapipe/tasks-vision';
import { LandmarkClassifier } from '../lib/knn';
import { StableSignGate } from '../lib/gate';
import type { Landmark } from '../lib/features';
import {
  createHandLandmarker,
  drawSkeleton,
  startCamera,
  stopCamera,
} from '../lib/landmarker';
import { assetUrl } from '../lib/util';

export interface CameraReading {
  label: string | null;
  progress: number;
  confirmed: boolean;
  message: string;
}

/**
 * Port of PracticeCamera.kt to the browser: getUserMedia preview +
 * MediaPipe tasks-vision Hand Landmarker + the same psl.bin / english.bin
 * k-NN classifier + the same StableSignGate (1.2 s hold, 550 ms release).
 *
 * Mobile-first rules:
 *  - the camera starts from a real tap, never from page load (iOS Safari
 *    only grants the camera inside a gesture; in-app webviews refuse it);
 *  - every load stage is announced — the one-time ~20 MB model download
 *    must never read as "mysterious nothing";
 *  - target changes flow through refs so the camera never reboots.
 */
export function CameraPractice({
  mode,
  target = null,
  targetDisplay = null,
  onReading,
  showFlip = true,
  playing = true,
}: {
  mode: 'psl' | 'english';
  target?: string | null;
  targetDisplay?: string | null;
  onReading: (reading: CameraReading) => void;
  showFlip?: boolean;
  playing?: boolean;
}) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const onReadingRef = useRef(onReading);
  onReadingRef.current = onReading;
  const targetRef = useRef(target);
  targetRef.current = target;
  const targetDisplayRef = useRef(targetDisplay);
  targetDisplayRef.current = targetDisplay;

  const [started, setStarted] = useState(false);
  const [preparing, setPreparing] = useState<string | null>(null);
  const [permissionDenied, setPermissionDenied] = useState(false);
  const [failure, setFailure] = useState<string | null>(null);
  const [retry, setRetry] = useState(0);
  const [facing, setFacing] = useState<'user' | 'environment'>('user');
  const kickRef = useRef<Promise<MediaStream> | null>(null);
  const startedRef = useRef(false);

  /**
   * Runs inside the tap handler itself: iOS Safari only grants the camera
   * from a gesture, so the boot must begin here — the loader effect below
   * simply awaits whoever got there first.
   */
  const beginCamera = () => {
    if (startedRef.current) return;
    startedRef.current = true;
    const video = videoRef.current;
    if (video && !video.srcObject) {
      const kick = startCamera(video, facing);
      kick.catch(() => null); // awaited again by the loader — no unhandled rejection
      kickRef.current = kick;
    }
    setStarted(true);
  };

  useEffect(() => {
    if (!started) return;
    let cancelled = false;
    let raf = 0;
    let landmarker: HandLandmarker | null = null;

    (async () => {
      setFailure(null);
      setPermissionDenied(false);
      const video = videoRef.current;
      if (!video) return;
      try {
        if (!navigator.mediaDevices?.getUserMedia) throw new DOMException('no mediaDevices', 'NotSupportedError');

        // Reuse the camera the tap gesture opened; start it otherwise.
        let stream: MediaStream | null = null;
        if (kickRef.current) {
          try { stream = await kickRef.current; } catch { stream = null; }
          kickRef.current = null;
        }
        if (!stream || !video.srcObject) {
          setPreparing('Asking for the camera…');
          stream = await startCamera(video, facing);
        }

        setPreparing('Loading the hand tracker… (first visit downloads once)');
        landmarker = await createHandLandmarker(2);

        setPreparing('Loading your sign model…');
        const bin = await fetch(assetUrl(`marks/models/${mode}.bin`));
        if (!bin.ok) throw new DOMException(`model fetch ${bin.status}`, 'NotSupportedError');
        const classifier = new LandmarkClassifier(await bin.arrayBuffer());
        setPreparing(null);

        const gate = new StableSignGate();
        let lastFrame = 0;
        const deliver = (r: CameraReading) => { if (!cancelled) onReadingRef.current(r); };

        const loop = () => {
          raf = requestAnimationFrame(loop);
          const now = performance.now();
          if (!playing) return;
          if (now - lastFrame < 90) return; // ~11 fps analysis, mirroring Android throttle
          lastFrame = now;
          const el = videoRef.current;
          if (!el || el.readyState < 2 || el.videoWidth === 0) return;
          try {
            const result = landmarker!.detectForVideo(el, now);
            const canvas = canvasRef.current;
            if (canvas) drawSkeleton(canvas, result, el);
            const hands = result.landmarks ?? [];
            if (hands.length !== 1) {
              gate.update(null, now, hands.length > 0);
              deliver({
                label: null, progress: 0, confirmed: false,
                message: hands.length === 0
                  ? 'Show your hand. Good light helps!'
                  : 'For this game, show just one hand.',
              });
            } else {
              const points: Landmark[] = hands[0].map((l) => ({ x: l.x, y: l.y, z: l.z }));
              const prediction = classifier.predict(points);
              const candidate = prediction && prediction.accepted ? prediction.label : null;
              const tgt = targetRef.current;
              const matches = candidate !== null && (tgt === null || tgt === candidate);
              const hold = gate.update(matches ? candidate : null, now, true);
              deliver({
                label: candidate,
                progress: hold.progress,
                confirmed: hold.confirmed,
                message:
                  hold.confirmed ? 'You did it! ✨ Lower your hand before the next sign.'
                  : candidate === null ? 'I see your hand. Turn it gently towards the camera.'
                  : tgt !== null && candidate !== tgt
                    ? `Let's look at ${targetDisplayRef.current ?? tgt} again. Take your time.`
                  : hold.progress >= 1 ? 'Lower your hand, then show another sign.'
                  : 'Keep this shape for a moment…',
              });
            }
          } catch {
            gate.reset();
            deliver({ label: null, progress: 0, confirmed: false, message: 'Camera practice needs a restart. Tap Retry.' });
          }
        };
        raf = requestAnimationFrame(loop);
      } catch (e) {
        setPreparing(null);
        const name = (e as DOMException)?.name ?? '';
        if (name === 'NotAllowedError' || name === 'SecurityError') {
          setPermissionDenied(true);
        } else {
          setFailure(failureText(e));
        }
      }
    })();

    return () => {
      cancelled = true;
      cancelAnimationFrame(raf);
      try { landmarker?.close(); } catch { /* already closed */ }
      stopCamera(videoRef.current);
    };
  }, [mode, started, retry, facing, playing]);

  if (permissionDenied) {
    return (
      <div className="camera-panel">
        <div className="camera-emoji">📷</div>
        <div className="camera-title">The camera needs your permission</div>
        <p>Camera practice happens on this device. Pictures are never saved or sent.</p>
        <button className="btn primary" onClick={() => setRetry((r) => r + 1)}>Allow camera</button>
        <p className="camera-hint">
          On a phone: allow the camera when your browser asks (or in Chrome/Safari site
          settings), then tap the button again.
        </p>
      </div>
    );
  }

  return (
    <div className={`camera-frame ${failure ? 'has-failure' : ''}`}>
      <video ref={videoRef} playsInline muted className={`camera-video ${started ? '' : 'camera-video-pre'}`} />
      <canvas ref={canvasRef} className="camera-skeleton" />

      {!started && (
        <div className="camera-gate">
          <div className="camera-emoji">📸</div>
          <div className="camera-title">A mirror for your signs</div>
          <p>Everything runs on this device — nothing is saved or sent.</p>
          <button className="btn primary big" onClick={beginCamera}>Turn on the camera</button>
          {looksLikeInAppWebview() && (
            <p className="camera-hint">
              This looks like an in-app browser (Instagram, Facebook, etc.) — it blocks
              the camera. Open Irssa in Chrome or Safari instead 🙏
            </p>
          )}
          <p className="camera-hint">First time loads the sign brain (~20 MB) — after that it is fast.</p>
        </div>
      )}

      {started && (
        <>
          {showFlip && (
            <button
              className="camera-ghost-btn"
              onClick={() => setFacing((f) => (f === 'user' ? 'environment' : 'user'))}
              title="Switch camera"
            >
              ⇄ Flip
            </button>
          )}
          <div className="camera-ondevice">● On your device</div>
          {preparing && !failure && <div className="camera-loading">⏳ {preparing}</div>}
          {failure && (
            <div className="camera-failure">
              <p>{failure}</p>
              <button className="btn primary" onClick={() => setRetry((r) => r + 1)}>Retry</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

/**
 * One phone truth instead of a generic shrug: say what actually went wrong.
 */
function failureText(e: unknown): string {
  switch ((e as DOMException)?.name) {
    case 'NotFoundError':
    case 'DevicesNotFoundError':
      return 'No camera was found on this device.';
    case 'NotReadableError':
    case 'TrackStartError':
      return 'Another app is using the camera — close it, then tap Retry.';
    case 'OverconstrainedError':
      return 'This device could not open its front camera that way. Tap Retry.';
    case 'NotSupportedError':
      return soundsLikeInApp
        ? 'In-app browsers block the camera. Open this page in Chrome or Safari.'
        : 'This browser has no camera API. Try Chrome, Safari, or Firefox.';
    default:
      return 'The camera could not start (weak internet on the first visit also causes this). Tap Retry.';
  }
}
const soundsLikeInApp = looksLikeInAppWebview();

/** Instagram/Facebook/Line et al. ship WebViews that refuse getUserMedia. */
function looksLikeInAppWebview(): boolean {
  const ua = navigator.userAgent || '';
  return /\b(FBAN|FBAV|FBBH|FBIOPH)\b/i.test(ua)
    || /Instagram/i.test(ua)
    || /Line\//i.test(ua)
    || (/iPhone|iPad/i.test(ua) && /AppleWebKit/i.test(ua) && !/Safari|CriOS|FxiOS|EdgiOS|Opt/i.test(ua));
}
