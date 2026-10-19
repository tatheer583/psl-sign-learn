import React, { useEffect, useRef } from 'react';

interface Piece {
  x: number; y: number; vx: number; vy: number;
  size: number; color: string; rot: number; vr: number; shape: number;
}

const COLORS = ['#5C52E5', '#FF7849', '#00B894', '#FFC312', '#FF5E7E', '#00B4D8'];

/** Celebration confetti — honouring prefers-reduced-motion via the store. */
export function Confetti({ burst, reducedMotion }: { burst: number; reducedMotion?: boolean }) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    if (!burst || reducedMotion) return;
    const canvas = canvasRef.current;
    const ctx = canvas?.getContext('2d');
    if (!canvas || !ctx) return;
    const W = (canvas.width = window.innerWidth);
    const H = (canvas.height = window.innerHeight);
    const pieces: Piece[] = [];
    for (let i = 0; i < 110; i++) {
      pieces.push({
        x: W / 2 + (Math.random() - 0.5) * W * 0.35,
        y: H * 0.35 + (Math.random() - 0.5) * 80,
        vx: (Math.random() - 0.5) * 9,
        vy: -(Math.random() * 10 + 4),
        size: 6 + Math.random() * 8,
        color: COLORS[i % COLORS.length],
        rot: Math.random() * Math.PI,
        vr: (Math.random() - 0.5) * 0.3,
        shape: i % 2,
      });
    }
    let raf = 0;
    let life = 0;
    const tick = () => {
      life++;
      ctx.clearRect(0, 0, W, H);
      for (const p of pieces) {
        p.x += p.vx;
        p.y += p.vy;
        p.vy += 0.22;
        p.rot += p.vr;
        ctx.save();
        ctx.translate(p.x, p.y);
        ctx.rotate(p.rot);
        ctx.fillStyle = p.color;
        if (p.shape === 0) ctx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size * 0.6);
        else {
          ctx.beginPath();
          ctx.arc(0, 0, p.size / 2.4, 0, Math.PI * 2);
          ctx.fill();
        }
        ctx.restore();
      }
      if (life < 150) raf = requestAnimationFrame(tick);
      else ctx.clearRect(0, 0, W, H);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [burst, reducedMotion]);

  return (
    <canvas
      ref={canvasRef}
      style={{
        position: 'fixed', inset: 0, pointerEvents: 'none',
        zIndex: 999, width: '100%', height: '100%', display: reducedMotion ? 'none' : 'block',
      }}
      aria-hidden
    />
  );
}
