import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Confetti } from '../components/Confetti';
import { PageHeader } from '../components/Pieces';
import { speedPool } from '../lib/pool';
import type { LearningItem } from '../content/types';
import { useStore } from '../store/store';
import { buzz, shuffle } from '../lib/util';

/**
 * Port of SpeedMatchScreen: 45 seconds, pick the matching word/card,
 * bonus stars = max(5, score/2), Speed Reflex Champ badge at 40.
 */
export function SpeedMatchScreen() {
  const nav = useNavigate();
  const { learner, reward, update } = useStore();
  const [score, setScore] = useState(0);
  const [streak, setStreak] = useState(0);
  const [timeLeft, setTimeLeft] = useState(45);
  const [over, setOver] = useState(false);
  const [target, setTarget] = useState<LearningItem | null>(null);
  const [options, setOptions] = useState<LearningItem[]>([]);
  const [correct, setCorrect] = useState<boolean | null>(null);
  const scoredRef = useRef(false);

  const setupRound = () => {
    setCorrect(null);
    const pool = shuffle(speedPool());
    const t = pool[0];
    setTarget(t);
    setOptions(shuffle([...pool.slice(1, 4), t]));
  };

  useEffect(() => { setupRound(); }, []);

  useEffect(() => {
    if (over) return;
    if (timeLeft <= 0) {
      setOver(true);
      // Android: bonusStars = (score / 2).coerceAtLeast(5)
      const bonus = Math.max(5, Math.floor(score / 2));
      update((s) => ({
        ...s,
        stars: s.stars + bonus,
        practiceDays: new Set(s.practiceDays).add(new Date().toISOString().slice(0, 10)),
      }));
      if (score >= 40) {
        // Speed Reflex Champ badge milestone
        reward('badge_speed_match', 'camera');
      }
      return;
    }
    const t = setTimeout(() => setTimeLeft((s) => s - 1), 1000);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [timeLeft, over]);

  const pick = (id: string) => {
    if (over || target === null || correct !== null) return;
    const ok = id === target.id;
    setCorrect(ok);
    if (ok) { buzz(); setScore((s) => s + 1); setStreak((s) => s + 1); }
    else setStreak(0);
    setTimeout(setupRound, ok ? 450 : 800);
  };

  return (
    <div className="page">
      <Confetti burst={over && score > 0 ? 1 : 0} reducedMotion={learner.reducedMotion} />
      <PageHeader
        title="Speed Match ⚡"
        subtitle={over ? `Time! Final score ${score}` : `${timeLeft}s left · Score ${score} · Streak ${streak}`}
        onBack={() => nav('/')}
      />

      {!over && target !== null && (
        <>
          <div className="quiz-question-card target">
            <div className="qq-big urdu">{target.title}</div>
            <div className="qq-prompt">Tap the matching word & picture!</div>
          </div>
          <div className="quiz-options">
            {options.map((o) => (
              <button
                key={o.id}
                className={`quiz-option ${correct === null ? '' : o.id === target.id ? 'right' : ''}`}
                onClick={() => pick(o.id)}
                style={{ background: `#${(o.colorHex & 0xFFFFFF).toString(16).padStart(6, '0')}18` }}
              >
                <span className="qo-media">{o.objectEmoji}</span>
                <span className="qo-title">{o.category === 'ALPHABET' || o.id.startsWith('letter_en_') ? o.title : o.objectEmoji}</span>
                <span className="qo-sub">{o.subtitle.replace(/ \(.*\)/, '')}</span>
              </button>
            ))}
          </div>
        </>
      )}

      {over && (
        <div className="gameover-card">
          <div className="go-emoji">{score >= 40 ? '⚡👑' : score >= 20 ? '🌟' : '💪'}</div>
          <h2>Score {score}</h2>
          <p>
            {score >= 40
              ? 'Speed Reflex Champ! The badge is yours!'
              : 'Every round makes your hands faster and more confident!'}
          </p>
          <button className="btn primary big" onClick={() => {
            setScore(0); setStreak(0); setTimeLeft(45); setOver(false); scoredRef.current = false; setupRound();
          }}>Play again ⚡</button>
          <button className="btn secondary" onClick={() => nav('/')}>Back home</button>
        </div>
      )}
      <span hidden>{scoredRef.current}</span>
    </div>
  );
}
