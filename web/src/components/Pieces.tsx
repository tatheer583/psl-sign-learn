import React from 'react';
import { cssColor } from '../lib/util';

/** Yellow star chip identical to the Android header. */
export function StarChip({ stars }: { stars: number }) {
  return (
    <span className="star-chip">
      <span className="star-icon">⭐</span> {stars}
    </span>
  );
}

/** Top bar with back button + title/subtitle, light style. */
export function PageHeader({
  title,
  subtitle,
  onBack,
  right,
  dark,
}: {
  title: string;
  subtitle?: string;
  onBack?: () => void;
  right?: React.ReactNode;
  dark?: boolean;
}) {
  return (
    <header className={`page-header ${dark ? 'dark' : ''}`}>
      <div className="ph-left">
        {onBack && (
          <button className="back-btn" onClick={onBack} aria-label="Back">←</button>
        )}
        <div>
          <div className="ph-title">{title}</div>
          {subtitle && <div className="ph-subtitle">{subtitle}</div>}
        </div>
      </div>
      <div className="ph-right">{right}</div>
    </header>
  );
}

/** Round turbo progress ring for camera hold progress. */
export function ProgressRing({ progress, size = 64 }: { progress: number; size?: number }) {
  const r = size / 2 - 5;
  const c = 2 * Math.PI * r;
  return (
    <svg width={size} height={size} className="progress-ring" aria-hidden>
      <circle cx={size / 2} cy={size / 2} r={r} className="ring-track" strokeDasharray={c} />
      <circle
        cx={size / 2}
        cy={size / 2}
        r={r}
        className="ring-value"
        strokeDasharray={c}
        strokeDashoffset={c * (1 - Math.min(1, Math.max(0, progress)))}
      />
    </svg>
  );
}

export function ColorDot({ hex, size = 12 }: { hex: number; size?: number }) {
  return <span className="color-dot" style={{ background: cssColor(hex), width: size, height: size }} />;
}
