/** Asset URLs resolved against document base, so the build works from any folder. */
export function assetUrl(path: string): string {
  return new URL(path, document.baseURI).href;
}

/** Kotlin colorHex (0xAARRGGBB as unsigned) → CSS color string. */
export function cssColor(argb: number, alpha = 1): string {
  const a = ((argb >>> 24) & 0xFF) / 255;
  const r = (argb >>> 16) & 0xFF;
  const g = (argb >>> 8) & 0xFF;
  const b = argb & 0xFF;
  return `rgba(${r},${g},${b},${a * alpha})`;
}

export function today(): string {
  return new Date().toISOString().slice(0, 10);
}

export function shuffle<T>(arr: T[]): T[] {
  const out = [...arr];
  for (let i = out.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [out[i], out[j]] = [out[j], out[i]];
  }
  return out;
}

/** Friendly haptics — Android uses vibration patterns too. */
export function buzz(pattern: number[] = [0, 150]): void {
  try {
    if ('vibrate' in navigator) navigator.vibrate(pattern);
  } catch {
    /* ignore */
  }
}
