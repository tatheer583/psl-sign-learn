import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CameraPractice, type CameraReading } from '../components/CameraPractice';
import { Confetti } from '../components/Confetti';
import { PageHeader, ProgressRing } from '../components/Pieces';
import { allItems } from '../content/types';
import type { LearningItem } from '../content/types';
import { useStore } from '../store/store';
import { buzz, shuffle } from '../lib/util';

/**
 * Port of CameraQuizScreen — shows a target sign card, the child forms it,
 * and the real landmark classifier (not a brightness heuristic, like the
 * tablet build) confirms the exact target through the strict gate.
 * Numbers stay teacher-verified: the child forms the sign steadily, a
 * grown-up taps the honest check. PSL numbers need a teacher, always.
 */
export function CameraQuizScreen() {
  const nav = useNavigate();
  const { learner, reward } = useStore();
  const [burst, setBurst] = useState(0);
  const [mode, setMode] = useState<'ALPHABET' | 'NUMBERS'>('ALPHABET');
  const [index, setIndex] = useState(0);
  const [score, setScore] = useState(0);
  const [streak, setStreak] = useState(0);
  const [reading, setReading] = useState<CameraReading | null>(null);
  const [teacherCheck, setTeacherCheck] = useState(false);

  const pool: LearningItem[] = useMemo(() => {
    if (mode === 'ALPHABET') {
      // Only handshapes the studio classifier can truly recognise + trained English letters? keep PSL focus
      return allItems.filter((i) => i.id.startsWith('letter_') && !i.id.startsWith('letter_en_') && i.title !== 'ء');
    }
    return allItems.filter((i) => i.id.startsWith('num_') && Number(i.title) >= 1 && Number(i.title) <= 20);
  }, [mode]);

  useEffect(() => { setIndex((i) => i % pool.length); }, [pool.length]);
  const target = pool[index % pool.length];
  const isNumber = mode === 'NUMBERS';
  const targetId = isNumber
    ? `num_${target.title}`
    : `letter_${target.id.replace('letter_', '')}`;
  const matched = Boolean(reading?.confirmed && reading.label === null);
  const numberHold = Math.min(1, (reading?.progress ?? 0) * 2);

  // Match celebration (alphabet mode) — identical flow to Android: +10, 1.5s, next.
  const matchedRef = useRef(false);
  useEffect(() => {
    if (isNumber) return;
    const confirmed = Boolean(reading?.confirmed);
    if (confirmed && !matchedRef.current) {
      matchedRef.current = true;
      buzz([0, 150, 60, 150]);
      setScore((s) => s + 10);
      setStreak((s) => s + 1);
      reward(`letter_${target.id.replace('letter_', '')}`, 'camera');
      setBurst((b) => b + 1);
      const t = setTimeout(() => {
        matchedRef.current = false;
        setTeacherCheck(false);
        setIndex((i) => (i + 1) % pool.length);
      }, 1500);
      return () => clearTimeout(t);
    }
    if (!confirmed && !matchedRef.current) setTeacherCheck(false);
  }, [reading, isNumber, target.id, pool.length, reward]);

  const onReading = (r: CameraReading) => setReading(r);

  return (
    <div className="page practice-page dark-bg">
      <Confetti burst={burst} reducedMotion={learner.reducedMotion} />
      <PageHeader
        title="Camera Hand Quiz 📸"
        subtitle={`Score ${score} · Streak ${streak}`}
        onBack={() => nav('/')}
        dark
      />
      <div className="mode-tabs">
        <button className={`mode-tab ${mode === 'ALPHABET' ? 'on' : ''}`} onClick={() => { setMode('ALPHABET'); setIndex(0); }}>اردو Letters</button>
        <button className={`mode-tab ${mode === 'NUMBERS' ? 'on' : ''}`} onClick={() => { setMode('NUMBERS'); setIndex(0); }}>🔢 Numbers</button>
      </div>

      <div className="quiz-target-card big" style={{ background: `#${(target.colorHex & 0xFFFFFF).toString(16).padStart(6, '0')}22` }}>
        <div className="qt-title urdu">{target.title}</div>
        <div className="qt-sub">{target.objectEmoji} {target.signName}</div>
        {matched && <div className="qt-celebrate">You did it! 🎉</div>}
      </div>

      <CameraPractice
        mode="psl"
        target={isNumber ? null : target.id.replace('letter_', '')}
        targetDisplay={target.title}
        onReading={onReading}
      />

      <div className="practice-status light">
        <ProgressRing progress={isNumber ? numberHold : (reading?.progress ?? 0)} />
        <p className={`status-msg ${reading?.message?.includes('You did it') ? 'confirmed' : ''}`}>
          {isNumber
            ? reading?.message ?? 'Show your hand…'
            : reading?.message ?? 'Raise your hand…'}
        </p>
      </div>

      {isNumber && (
        <div className="teacher-check">
          <label>
            <input
              type="checkbox"
              checked={teacherCheck}
              onChange={(e) => {
                setTeacherCheck(e.target.checked);
                if (e.target.checked) {
                  buzz();
                  setScore((s) => s + 10);
                  setStreak((s) => s + 1);
                  setTeacherCheck(false);
                  setTimeout(() => setIndex((i) => (i + 1) % pool.length), 600);
                }
              }}
            />
            👩‍🏫 A grown-up confirms the number sign was formed correctly
          </label>
          <p className="honest-note">Number signs need a real teacher — the camera will not pretend to grade them.</p>
        </div>
      )}
      <span hidden>{targetId}</span>
    </div>
  );
}
