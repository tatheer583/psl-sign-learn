/**
 * Exact TypeScript port of LandmarkFeatures in
 * app/src/main/java/com/example/irssa/Recognition.kt
 *
 * Mirrors: style mirroring via index/pinky MCP ordering, wrist origin,
 * scale by max distance — so the same psl.bin / english.bin model files
 * behave identically in the browser.
 */
export interface Landmark {
  x: number;
  y: number;
  z: number;
}

export function normalizeLandmarks(points: Landmark[]): Float32Array | null {
  if (points.length !== 21 || points.some((p) => !Number.isFinite(p.x) || !Number.isFinite(p.y) || !Number.isFinite(p.z))) {
    return null;
  }
  const wrist = points[0];
  const sign = points[5].x < points[17].x ? -1 : 1;
  let scale = 0;
  const result = new Float32Array(63);
  for (let i = 0; i < 21; i++) {
    const p = points[i];
    const x = (p.x - wrist.x) * sign;
    const y = p.y - wrist.y;
    const z = p.z - wrist.z;
    result[i * 3] = x;
    result[i * 3 + 1] = y;
    result[i * 3 + 2] = z;
    const norm = Math.sqrt(x * x + y * y + z * z);
    if (norm > scale) scale = norm;
  }
  if (scale < 0.0001) return null;
  for (let i = 0; i < 63; i++) result[i] /= scale;
  return result;
}
