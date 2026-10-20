import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { CameraPractice, type CameraReading } from '../components/CameraPractice';
import { Confetti } from '../components/Confetti';
import { PageHeader, StarChip, ProgressRing } from '../components/Pieces';
import { SignMediaView } from '../components/SignMediaView';
import { itemById } from '../content/types';
import { loadLessons, type Lesson } from '../lib/lessons';
import { useStore } from '../store/store';
import { buzz, cssColor } from '../lib/util';

/**
 * Port of CameraPracticeScreen — targeted practice mode: "show me ا"
 * driven by the same landmark classifier and hold-gate as the APK.
 */
export function CameraStudioScreen() {
  const nav = useNavigate();
  const [params] = useSearchParams();
  const { learner, reward } = useStore();
  const [burst, setBurst] = useState(0);
  const [reading, setReading] = useState<CameraReading | null>(null);
  const [guess, setGuess] = useState('');
  const [lesson, setLesson] = useState<Lesson | null>(null);

  const mode = (params.get('mode') === 'english' ? 'english' : 'psl') as 'psl' | 'english';
  const target = params.get('target');
  const display = params.get('display') ?? (target ? decodeURIComponent(target) : null);
  const item = target
    ? (itemById.get(`letter_${target}`) ??
       itemById.get(`letter_en_${target}`) ??
       itemById.get(`num_${target}`))
    : null;

  const onReading = (r: CameraReading) => {
    setReading(r);
    if (r.confirmed && target != null) {
      buzz([0, 150, 60, 150]);
      reward(normalizeTargetId(target), 'camera');
      setBurst((b) => b + 1);
    }
  };

  /** The example sign shown beside the camera: the lesson's real hand photo. */
  const targetId = target ? normalizeTargetId(target) : null;
  useEffect(() => {
    let dead = false;
    setLesson(null);
    if (targetId) {
      loadLessons()
        .then((ls) => { if (!dead && ls) setLesson(ls.find((l) => l.id === targetId) ?? null); })
        .catch(() => undefined);
    }
    return () => { dead = true; };
  }, [targetId]);

  const onGuessKey = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key !== 'Enter') return;
    const g = (e.target as HTMLInputElement).value.trim();
    if (!g) return;
    if (isEnglishGuess(g)) nav(`/practice?target=${g.toLowerCase()}&mode=english&display=${encodeURIComponent(g.toUpperCase())}`);
    else if (isUrduGuess(g)) nav(`/practice?target=${urduTarget(g)}&mode=psl&display=${encodeURIComponent(g)}`);
    else if (/^\d{1,2}$/.test(g)) nav(`/practice?target=num_${g}&mode=psl&display=${g}`);
    setGuess('');
  };

  return (
    <div className="page practice-page">
      <Confetti burst={burst} reducedMotion={learner.reducedMotion} />
      <PageHeader
        title="Camera Studio 📸"
        subtitle={target ? `Show the sign for ${display ?? target}` : 'Free practice — show any sign'}
        onBack={() => nav('/')}
        right={<StarChip stars={learner.stars} />}
      />

      {item && (
        <div className="practice-target" style={{ background: cssColor(item.colorHex, 0.12) }}>
          <div className="pt-letter urdu" style={{ color: cssColor(item.colorHex) }}>{item.title}</div>
          <div className="pt-word">{item.objectEmoji} {item.funFact.split('—')[0]}</div>
        </div>
      )}

      <div className={target ? 'practice-split' : ''}>
        <div className={target ? 'split-cam' : ''}>
          <CameraPractice mode={mode} target={target} targetDisplay={display} onReading={onReading} />
        </div>
        {target && (
          <div className="split-ref">
            {lesson ? (
              <SignMediaView lesson={lesson} />
            ) : (
              <div className="sign-media placeholder">
                <div
                  className="sign-media-letter"
                  style={{ color: cssColor(item?.colorHex ?? 0x2563eb) }}
                >
                  {display ?? target}
                </div>
                <div className="media-fallback">Open this letter's lesson to see the photo.</div>
              </div>
            )}
            <div className="split-ref-label">👆 Copy this sign in the mirror</div>
          </div>
        )}
      </div>

      <div className="practice-status">
        <ProgressRing progress={reading?.progress ?? 0} />
        <p className={`status-msg ${reading?.confirmed ? 'confirmed' : ''}`}>{reading?.message ?? 'Set the camera up…'}</p>
      </div>

      <div className="guess-row">
        <input
          className="text-input"
          placeholder="Type ب / A / 7 to switch target…"
          value={guess}
          onChange={(e) => setGuess(e.target.value)}
          onKeyDown={onGuessKey}
        />
      </div>
      <p className="honest-note">
        A camera match means the handshape looks right — it is not a score of ability! 💐
      </p>
    </div>
  );
}

function normalizeTargetId(target: string): string {
  if (/^num_\d+$/.test(target)) return target;
  if (/^[a-z]$/.test(target)) return `letter_en_${target}`;
  return `letter_${target}`;
}

/** Guess helpers are intentionally small: children type an Urdu letter, A-Z, or a number. */
function isEnglishGuess(g: string) { return !!g && g.length === 1 && g.toLowerCase() !== g; }
function isUrduGuess(g: string) { return /[\u0600-\u06FF]/.test(g); }
const URDU_TARGETS: Record<string, string> = {
  'ا': 'alif', 'ب': 'be', 'پ': 'pe', 'ت': 'te', 'ٹ': 'tte', 'ث': 'se', 'ج': 'jim',
  'چ': 'che', 'ح': 'he', 'خ': 'khe', 'د': 'dal', 'ڈ': 'dhal', 'ذ': 'zal', 'ر': 're',
  'ڑ': 'rre', 'ز': 'ze', 'ژ': 'zhe', 'س': 'seen', 'ش': 'sheen', 'ص': 'swad',
  'ض': 'zwad', 'ط': 'toay', 'ظ': 'zoay', 'ع': 'ain', 'غ': 'ghain', 'ف': 'fay',
  'ق': 'qaf', 'ک': 'kaf', 'گ': 'gaf', 'ل': 'laam', 'م': 'meem', 'ن': 'noon',
  'و': 'vao', 'ہ': 'hay', 'ھ': 'hay', 'ی': 'chhoti_yeh', 'ے': 'bari_yeh', 'ء': 'hamza', 'أ': 'hamza',
};
function urduTarget(g: string): string { return URDU_TARGETS[g] ?? 'alif'; }
