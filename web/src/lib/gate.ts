/**
 * Exact TypeScript port of StableSignGate in Recognition.kt —
 * requires continuous evidence for `holdMillis`, then hand removal
 * before accepting another sign.
 */
export interface HoldResult {
  progress: number;
  confirmed: boolean;
}

export class StableSignGate {
  private holdMillis: number;
  private releaseMillis: number;
  private label: string | null = null;
  private start = 0;
  private previous = 0;
  private latched = false;
  private missingSince: number | null = null;

  constructor(holdMillis = 1200, releaseMillis = 550) {
    this.holdMillis = holdMillis;
    this.releaseMillis = releaseMillis;
  }

  reset(): void {
    this.label = null;
    this.start = 0;
    this.previous = 0;
    this.latched = false;
    this.missingSince = null;
  }

  update(candidate: string | null, time: number, handPresent = candidate !== null): HoldResult {
    if (candidate === null) {
      if (handPresent) this.missingSince = null;
      else if (this.missingSince === null) this.missingSince = time;
      if (!handPresent && time - (this.missingSince ?? time) >= this.releaseMillis) this.latched = false;
      this.label = null;
      this.start = time;
      this.previous = time;
      return { progress: 0, confirmed: false };
    }
    this.missingSince = null;
    if (this.latched) return { progress: 1, confirmed: false };
    if (candidate !== this.label || time - this.previous > 500 || time < this.previous) {
      this.label = candidate;
      this.start = time;
    }
    this.previous = time;
    const progress = Math.min(1, Math.max(0, (time - this.start) / this.holdMillis));
    if (progress >= 1) {
      this.latched = true;
      return { progress: 1, confirmed: true };
    }
    return { progress, confirmed: false };
  }
}
