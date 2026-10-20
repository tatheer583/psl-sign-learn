import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { CameraPractice, type CameraReading } from '../components/CameraPractice';
import { Confetti } from '../components/Confetti';
import { PageHeader, StarChip, ProgressRing } from '../components/Pieces';
import { SignMediaView } from '../components/SignMediaView';
import { allItems, itemById, type LearningItem } from '../content/types';
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
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [detected, setDetected] = useState<string | null>(null);
  const [celebrate, setCelebrate] = useState<'next' | 'last' | null>(null);
  const advanceTimer = useRef<number | null>(null);
  const didAdvance = useRef<string | null>(null);

  /** All bundled lessons — powers the "what your hand says" side panel. */
  useEffect(() => {
    let dead = false;
    loadLessons()
      .then((ls) => { if (!dead && ls) setLessons(ls); })
      .catch(() => undefined);
    return () => { dead = true; };
  }, []);
  const lessonById = useMemo(() => new Map(lessons.map((l) => [l.id, l])), [lessons]);

  const mode = (params.get('mode') === 'english' ? 'english' : 'psl') as 'psl' | 'english';
  const target = params.get('target');
  const display = params.get('display') ?? (target ? decodeURIComponent(target) : null);
  const item = target
    ? (itemById.get(`letter_${target}`) ??
       itemById.get(`letter_en_${target}`) ??
       itemById.get(`num_${target}`))
    : null;

  const targetId = target ? normalizeTargetId(target) : null;

  /** The sequence: next lesson in the same family (ا→ب→پ… / a→b→c… / 1→2→3…), or null at the end. */
  const nextInFamily = (id: string): LearningItem | null => {
    const root = id.startsWith('num_') ? 'num_' : id.startsWith('letter_en_') ? 'letter_en_' : 'letter_';
    const fam = allItems.filter((i) =>
      root === 'letter_' ? (i.id.startsWith('letter_') && !i.id.startsWith('letter_en_')) : i.id.startsWith(root),
    );
    const k = fam.findIndex((i) => i.id === id);
    return k >= 0 ? (fam[k + 1] ?? null) : null;
  };

  const onReading = (r: CameraReading) => {
    setReading(r);
    if (r.label) setDetected((prev) => (prev === r.label ? prev : r.label)); // remember last known shape
    if (r.confirmed && targetId != null && didAdvance.current !== targetId) {
      didAdvance.current = targetId; // confirmations repeat every frame — advance once
      buzz([0, 150, 60, 150]);
      reward(targetId, 'camera');
      setBurst((b) => b + 1);
      const next = nextInFamily(targetId);
      if (next) {
        setCelebrate('next');
        advanceTimer.current = window.setTimeout(() => {
          advanceTimer.current = null;
          setCelebrate(null);
          setReading(null);
          setDetected(null);
          const tier = next.id.startsWith('letter_en_') ? 'letter_en_' : next.id.startsWith('num_') ? 'num_' : 'letter_';
          nav(`/practice?target=${encodeURIComponent(next.id.replace(tier, ''))}&mode=${mode}&display=${encodeURIComponent(next.title)}`);
        }, 2400);
      } else {
        setCelebrate('last');
        advanceTimer.current = window.setTimeout(() => { advanceTimer.current = null; setCelebrate(null); }, 3600);
      }
    }
  };

  // A new target (auto-advance or manual switch) re-arms the chain.
  useEffect(() => {
    didAdvance.current = null;
    if (advanceTimer.current) { window.clearTimeout(advanceTimer.current); advanceTimer.current = null; }
    setCelebrate(null);
  }, [targetId]);
  useEffect(() => () => { if (advanceTimer.current) window.clearTimeout(advanceTimer.current); }, []);

  /** Side panel: the sign matching the user's CURRENT gesture — else the target example. */
  const shown =
    (detected ? lessonById.get(normalizeTargetId(detected)) : null) ??
    (targetId ? lessonById.get(targetId) : null) ??
    null;

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

      <div className={`practice-split ${reading?.confirmed ? 'hit' : ''}`}>
        <div className="split-cam">
          <CameraPractice mode={mode} target={target} targetDisplay={display} onReading={onReading} />
        </div>
        <div className="split-ref">
          {shown ? (
            <SignMediaView lesson={shown} />
          ) : (
            <div className="sign-media placeholder">
              <div className="sign-media-letter" style={{ color: cssColor(0x0ea5e9) }}>👋</div>
              <div className="media-fallback">Show any sign in the mirror.</div>
            </div>
          )}
          <div className="split-ref-label">
            {shown && !targetId && <>Looks like <b className="urdu">{shown.item.title}</b> 👋</>}
            {shown && targetId && shown.id !== targetId && (
              <>You're showing <b className="urdu">{shown.item.title}</b> — target is <b className="urdu">{display}</b></>
            )}
            {shown && targetId && shown.id === targetId && <>🎯 <b className="urdu">{shown.item.title}</b> — that's it!</>}
            {!shown && 'Show any sign — I say what it is'}
          </div>
        </div>
      </div>

      <div className="practice-status">
        <ProgressRing progress={reading?.progress ?? 0} />
        <p className={`status-msg ${reading?.confirmed ? 'confirmed' : ''}`}>{reading?.message ?? 'Set the camera up…'}</p>
      </div>

      {celebrate && (() => {
        const nextNow = targetId ? nextInFamily(targetId) : null;
        return (
          <div className="celebrate-banner" role="status">
            <div className="celebrate-big">Well done! 🎉</div>
            {celebrate === 'next' && nextNow && (
              <div className="celebrate-sub">Up next — show <b className="urdu">{nextNow.title}</b> {nextNow.objectEmoji}</div>
            )}
            {celebrate === 'last' && (
              <div className="celebrate-sub">That was the last one in this set — keep practising, or type another sign below!</div>
            )}
          </div>
        );
      })()}

      <div className="guess-row">
        <input
          className="text-input"
          placeholder="Type ب / A / 7 to switch target…"
          value={guess}
          onChange={(e) => setGuess(e.target.value)}
          onKeyDown={onGuessKey}
        />
        {targetId && (() => {
          const nx = nextInFamily(targetId);
          if (!nx) return null;
          const tier = nx.id.startsWith('letter_en_') ? 'letter_en_' : nx.id.startsWith('num_') ? 'num_' : 'letter_';
          return (
            <button
              className="btn secondary skip-btn"
              onClick={() => {
                setReading(null);
                setDetected(null);
                nav(`/practice?target=${encodeURIComponent(nx.id.replace(tier, ''))}&mode=${mode}&display=${encodeURIComponent(nx.title)}`);
              }}
            >
              ⏭ Skip
            </button>
          );
        })()}
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
