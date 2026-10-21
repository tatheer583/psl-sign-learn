/**
 * MediaPipe Hand Landmarker wrapper for the web — loads the very same
 * `hand_landmarker.task` model the Android app ships (models/), from the
 * locally bundled WASM runtime (public/marks/wasm), so recognition runs
 * fully on-device, fully offline, exactly like the APK.
 */
import {
  FilesetResolver,
  HandLandmarker,
  type HandLandmarkerResult,
} from '@mediapipe/tasks-vision';
import { assetUrl } from './util';

export type { HandLandmarkerResult };
export interface MediaPlayer {
  landmarker: HandLandmarker;
  video: HTMLVideoElement;
}

let visionPromise: Promise<any> | null = null;

export async function getVision(): Promise<any> {
  if (!visionPromise) {
    // Never cache a rejected load: a single hiccup would brick the camera
    // until a full page reload. Retry on the next attempt instead.
    visionPromise = FilesetResolver.forVisionTasks(assetUrl('marks/wasm/')).catch((err) => {
      visionPromise = null;
      throw err;
    });
  }
  return visionPromise;
}

export async function createHandLandmarker(numHands = 2): Promise<HandLandmarker> {
  const vision = await getVision();
  const options = (delegate: 'GPU' | 'CPU') => ({
    baseOptions: { modelAssetPath: assetUrl('marks/models/hand_landmarker.task'), delegate },
    runningMode: 'VIDEO' as const,
    numHands,
    minHandDetectionConfidence: 0.6,
    minHandPresenceConfidence: 0.6,
  });
  try {
    return await HandLandmarker.createFromOptions(vision, options('GPU'));
  } catch (gpuError) {
    // Some phones/drivers refuse the WebGL delegate — recognition must still work.
    console.warn('GPU delegate failed, falling back to CPU:', gpuError);
    return HandLandmarker.createFromOptions(vision, options('CPU'));
  }
}

/** Start a user-facing camera; returns the stream (mirrored in CSS). */
export async function startCamera(
  video: HTMLVideoElement,
  facing: 'user' | 'environment' = 'user',
): Promise<MediaStream> {
  const stream = await navigator.mediaDevices.getUserMedia({
    audio: false,
    video: {
      facingMode: facing,
      // 480×360 is plenty for 21 landmarks and keeps cheap phone CPUs cool.
      width: { ideal: 480 },
      height: { ideal: 360 },
    },
  });
  video.srcObject = stream;
  await video.play();
  // Wait until frames are flowing so the first detectForVideo has videoTime > 0
  await new Promise<void>((resolve) => {
    if (video.readyState >= 2 && video.videoWidth > 0) return resolve();
    video.onloadeddata = () => resolve();
  });
  return stream;
}

export function stopCamera(video: HTMLVideoElement | null): void {
  const stream = video?.srcObject as MediaStream | null;
  stream?.getTracks().forEach((t) => t.stop());
  if (video) video.srcObject = null;
}

export const HAND_CONNECTIONS: ReadonlyArray<readonly [number, number]> = [
  [0, 1], [1, 2], [2, 3], [3, 4],
  [0, 5], [5, 6], [6, 7], [7, 8],
  [5, 9], [9, 10], [10, 11], [11, 12],
  [9, 13], [13, 14], [14, 15], [15, 16],
  [13, 17], [17, 18], [18, 19], [19, 20],
  [0, 17],
];

/** Mirror-aware landmark skeleton painter, matching the guide overlay feel. */
export function drawSkeleton(
  canvas: HTMLCanvasElement,
  result: HandLandmarkerResult | null,
  video: HTMLVideoElement,
  color = '#FFC312',
): void {
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;
  canvas.width = video.videoWidth || 640;
  canvas.height = video.videoHeight || 480;
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  if (!result) return;
  const W = canvas.width, H = canvas.height;
  const mirrored = (x: number) => (1 - x) * W; // CSS scaleX(-1) preview
  ctx.lineWidth = Math.max(2, W / 240);
  ctx.strokeStyle = color;
  for (const hand of result.landmarks) {
    ctx.beginPath();
    for (const [a, b] of HAND_CONNECTIONS) {
      const pa = hand[a], pb = hand[b];
      if (!pa || !pb) continue;
      ctx.moveTo(mirrored(pa.x), pa.y * H);
      ctx.lineTo(mirrored(pb.x), pb.y * H);
    }
    ctx.stroke();
    ctx.fillStyle = '#FFFFFF';
    const r = Math.max(3, W / 200);
    for (const p of hand) {
      // skip wrist dot for a cleaner overlay
      if (p === hand[0]) continue;
      ctx.beginPath();
      ctx.arc(mirrored(p.x), p.y * H, r, 0, Math.PI * 2);
      ctx.fill();
    }
  }
}
