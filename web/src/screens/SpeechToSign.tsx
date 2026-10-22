import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { itemById } from '../content/types';
import { SignMediaView } from '../components/SignMediaView';
import { PageHeader } from '../components/Pieces';
import { loadLessons, type Lesson } from '../lib/lessons';
import {
  speak, speakAlways, speechRecognitionBlockedReason, startListening,
  ttsAvailable, ttsEnabled, setTtsEnabled,
} from '../lib/tts';

/** Ready-made words so the screen always demonstrates something, mic or no mic. */
const EXAMPLE_WORDS = [
  { label: 'انار', en: 'pomegranate' },
  { label: 'پانی', en: 'water' },
  { label: 'کتاب', en: 'book' },
  { label: 'گھر', en: 'home' },
  { label: 'apple', en: 'apple' },
];

/**
 * Port of SpeechToSignScreen — speak or type a word, watch it finger-spelled.
 * (Web Speech API; Chrome/Edge support it best. Never leaves the device.)
 */
export function SpeechToSignScreen() {
  const nav = useNavigate();
  const [lessons, setLessons] = useState<Lesson[] | null>(null);
  const [word, setWord] = useState('');
  const [listening, setListening] = useState(false);
  // Mirror the persisted voice preference — a page reload must not desync
  // from the checkbox (that display-only state made voice "mysteriously" die).
  const [voiceOn, setVoiceOn] = useState(() => ttsAvailable() && ttsEnabled());
  const [err, setErr] = useState<string | null>(null);
  const [hint, setHint] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  // The live recognition session — kept so leaving the page (or tapping the mic
  // again) really stops the microphone instead of leaving it open in the background.
  const stopRef = useRef<(() => void) | null>(null);
  const timerRef = useRef<number | null>(null);
  const heardRef = useRef(false);

  useEffect(() => {
    let dead = false;
    loadLessons().then((ls) => { if (!dead) setLessons(ls); }).catch(() => setLessons([]));
    return () => { dead = true; };
  }, []);

  useEffect(() => () => {
    stopRef.current?.();
    if (timerRef.current !== null) window.clearTimeout(timerRef.current);
  }, []);

  const lessonById = useMemo(() => (id: string) => lessons?.find((l) => l.id === id) ?? null, [lessons]);

  const applyWord = (w: string) => {
    const cleaned = w.trim().slice(0, 40);
    setWord(cleaned);
    setErr(null);
    if (cleaned && voiceOn) speak(cleaned);
  };

  const stopSession = () => {
    stopRef.current?.();
    stopRef.current = null;
    if (timerRef.current !== null) { window.clearTimeout(timerRef.current); timerRef.current = null; }
    setListening(false);
  };

  const toggleListen = () => {
    if (listening) { stopSession(); return; }
    const blocked = speechRecognitionBlockedReason();
    if (blocked) {
      setErr(
        blocked === 'insecure'
          ? 'The microphone needs a secure page (https). Type the word below instead. 💛'
          : 'This browser cannot listen (Chrome and Edge can). Type the word below instead. 💛',
      );
      inputRef.current?.focus();
      return;
    }
    setErr(null);
    setHint(null);
    heardRef.current = false;
    setListening(true);
    const stop = startListening(
      (said) => { heardRef.current = true; applyWord(said); stopSession(); },
      (m) => { setErr(m); stopSession(); },
      () => {
        // The engine ended on its own (silence or a network stop) — say what happened.
        if (!heardRef.current) {
          setErr('I did not catch that — tap the mic and speak a little louder, or type the word below. 💛');
        }
        stopSession();
      },
    );
    stopRef.current = stop;
    timerRef.current = window.setTimeout(() => {
      timerRef.current = null;
      if (!heardRef.current) {
        setErr('I was still listening but heard nothing — tap the mic and try again, or type the word below. 💛');
      }
      stopSession();
    }, 9000);
  };

  const letters = useMemo(() => {
    const chars = [...word.trim().normalize('NFC')];
    return chars.map((ch) => {
      const lower = ch.toLowerCase();
      if (/[a-z]/.test(lower) && itemById.has(`letter_en_${lower}`)) {
        return { ch: ch.toUpperCase(), id: `letter_en_${lower}` };
      }
      if (/[\u0621-\u06D3]/.test(ch)) {
        // small Urdu symbol→id map for fingerspelling
        const SYM: Record<string, string> = {
          'ا': 'letter_alif', 'ب': 'letter_be', 'پ': 'letter_pe', 'ت': 'letter_te',
          'ٹ': 'letter_tte', 'ث': 'letter_se', 'ج': 'letter_jim', 'چ': 'letter_che',
          'ح': 'letter_he', 'خ': 'letter_khe', 'د': 'letter_dal', 'ڈ': 'letter_dhal',
          'ذ': 'letter_zal', 'ر': 'letter_re', 'ڑ': 'letter_rre', 'ز': 'letter_ze',
          'ژ': 'letter_zhe', 'س': 'letter_seen', 'ش': 'letter_sheen', 'ص': 'letter_swad',
          'ض': 'letter_zwad', 'ط': 'letter_toay', 'ظ': 'letter_zoay', 'ع': 'letter_ain',
          'غ': 'letter_ghain', 'ف': 'letter_fay', 'ق': 'letter_qaf', 'ک': 'letter_kaf',
          'گ': 'letter_gaf', 'ل': 'letter_laam', 'م': 'letter_meem', 'ن': 'letter_noon',
          'و': 'letter_vao', 'ہ': 'letter_hay', 'ی': 'letter_chhoti_yeh', 'ے': 'letter_bari_yeh',
          'ء': 'letter_hamza',
        };
        const id = SYM[ch];
        return id ? { ch, id } : { ch, id: null };
      }
      return { ch: ch === ' ' ? '␣' : ch, id: null };
    });
  }, [word]);

  return (
    <div className="page">
      <PageHeader
        title="Speech to Sign 🗣️"
        subtitle="A word spoken aloud, spelled in hands"
        onBack={() => nav('/')}
      />

      <button
        className={`mic-btn ${listening ? 'listening' : ''}`}
        onClick={toggleListen}
        aria-pressed={listening}
      >
        {listening ? '🎙 Listening… tap to stop' : '🎙 Tap and say a word'}
      </button>
      {err && <p className="error-note">{err}</p>}
      {hint && <p className="stts-hint">{hint}</p>}

      <input
        className="text-input big"
        placeholder="…or type it here"
        value={word}
        ref={inputRef}
        onChange={(e) => setWord(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && applyWord(word)}
        maxLength={40}
      />

      <div className="example-chips" role="group" aria-label="Example words">
        <span className="example-label">Try one:</span>
        {EXAMPLE_WORDS.map((w) => (
          <button
            key={w.label}
            className="example-chip"
            onClick={() => { setHint(`${w.label} — ${w.en}`); applyWord(w.label); }}
          >
            {w.label}
          </button>
        ))}
      </div>

      {letters.length > 0 && (
        <>
          <div className="spell-row">
            {letters.map((c, i) => {
              const lesson = c.id ? lessonById(c.id) : null;
              if (!lesson) {
                return <div key={i} className="spell-card skip"><span className="spell-ch">{c.ch}</span></div>;
              }
              return (
                <div key={i} className="spell-card">
                  <SignMediaView lesson={lesson} />
                  <span className={`spell-ch ${/[a-zA-Z]/.test(c.ch) ? '' : 'urdu'}`}>{c.ch}</span>
                </div>
              );
            })}
          </div>
          <p className="media-credit">
            Photos: PSL hand dataset (Mendeley Data V1, CC BY 4.0) — Ali Imran Ali, MNS University of Agriculture Multan.
          </p>
        </>
      )}

      {word.trim() && (
        <div className="stts-actions">
          <button className="btn secondary" onClick={() => speakAlways(word.trim())}>
            🔊 Listen again
          </button>
          <button
            className="btn secondary"
            onClick={() => { setWord(''); setErr(null); inputRef.current?.focus(); }}
          >
            ➡ Next word
          </button>
        </div>
      )}

      <div className="stts-settings">
        <label className="switch-row">
          <input
            type="checkbox"
            checked={ttsAvailable() && voiceOn}
            onChange={(e) => {
              setVoiceOn(e.target.checked);
              setTtsEnabled(e.target.checked);
            }}
          />
          Voice says the word aloud (English)
        </label>
      </div>
      <p className="honest-note">
        Honest note: Chrome and Edge recognise your voice with their own online service, so the
        microphone needs an internet connection. The app itself never stores or forwards anything —
        and typing a word keeps it 100% on the device. 🔒
      </p>
    </div>
  );
}
