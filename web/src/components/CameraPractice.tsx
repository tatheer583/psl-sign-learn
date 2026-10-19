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
 * k-NN classifier + the same StrictSignGate (1.2 s hold, 550 ms release).
 *
 * Everything runs on this device — frames are analysed and discarded,
 * identical to the APK's privacy promise.
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

  const [permissionDenied, setPermissionDenied] = useState(false);
  const [failure, setFailure] = useState<string | null>(null);
  const [retry, setRetry] = useState(0);
  const [facing, setFacing] = useState<'user' | 'environment'>('user');

  useEffect(() => {
    let cancelled = false;
    let raf = 0;
    let stream: MediaStream | null = null;
    let landmarker: HandLandmarker | null = null;

    (async () => {
      setFailure(null);
      setPermissionDenied(false);
      const video = videoRef.current;
      if (!video) return;
      try {
        if (!navigator.mediaDevices?.getUserMedia) throw new DOMException('no mediaDevices', 'NotSupportedError');
        stream = await navigator.mediaDevices.getUserMedia({
          audio: false,
          video: { facingMode: facing, width: { ideal: 640 }, height: { ideal: 480 } },
        });
        if (cancelled) { stream.getTracks().forEach((t) => t.stop()); return; }
        video.srcObject = stream;
        await video.play();
        await new Promise<void>((resolve) => {
          if (video.readyState >= 2 && video.videoWidth > 0) return resolve();
          video.onloadeddata = () => resolve();
        });

        landmarker = await createHandLandmarker(2);
        const bin = await fetch(assetUrl(`marks/models/${mode}.bin`));
        const classifier = new LandmarkClassifier(await bin.arrayBuffer());

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
              const matches = candidate !== null && (target === null || target === candidate);
              const hold = gate.update(matches ? candidate : null, now, true);
              deliver({
                label: candidate,
                progress: hold.progress,
                confirmed: hold.confirmed,
                message:
                  hold.confirmed ? 'You did it! ✨ Lower your hand before the next sign.'
                  : candidate === null ? 'I see your hand. Turn it gently towards the camera.'
                  : target !== null && candidate !== target
                    ? `Let's look at ${targetDisplay ?? target} again. Take your time.`
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
        const name = (e as DOMException)?.name ?? '';
        if (name === 'NotAllowedError' || name === 'SecurityError') {
          setPermissionDenied(true);
        } else if (!cancelled) {
          setFailure('This camera is unavailable. Try reopening practice.');
        }
      }
    })();

    return () => {
      cancelled = true;
      cancelAnimationFrame(raf);
      try { landmarker?.close(); } catch { /* already closed */ }
      stopCamera(videoRef.current);
    };
  }, [mode, target, targetDisplay, retry, facing, playing]);

  if (permissionDenied) {
    return (
      <div className="camera-panel">
        <div className="camera-emoji">📷</div>
        <div className="camera-title">A mirror for your signs</div>
        <p>Camera practice happens on this device. Pictures are never saved or sent.</p>
        <button className="btn primary" onClick={() => setRetry((r) => r + 1)}>Allow camera</button>
        <p className="camera-hint">
          Allow camera access for this site in your browser settings, then tap the button again.
        </p>
      </div>
    );
  }

  return (
    <div className={`camera-frame ${failure ? 'has-failure' : ''}`}>
      <video ref={videoRef} playsInline muted className="camera-video" />
      <canvas ref={canvasRef} className="camera-skeleton" />
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
      {failure && (
        <div className="camera-failure">
          <p>{failure}</p>
          <button className="btn primary" onClick={() => setRetry((r) => r + 1)}>Retry</button>
        </div>
      )}
    </div>
  );
}
