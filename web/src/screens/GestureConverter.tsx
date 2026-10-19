import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { content, itemById } from '../content/types';
import { SignMediaView } from '../components/SignMediaView';
import { CameraPractice, type CameraReading } from '../components/CameraPractice';
import { PageHeader } from '../components/Pieces';
import { loadLessons, type Lesson } from '../lib/lessons';
import { buzz } from '../lib/util';

/** classifier label ("alif") → visual preset, built once. */
const symbolByLabel = (() => {
  const bySymbol = new Map<string, string>(
    content.presets.map((p) => [p.alphabetSymbol, p.alphabetSymbol]),
  );
  void bySymbol;
  const out = new Map<string, { symbol: string; preset: (typeof content.presets)[number] }>();
  for (const p of content.presets) {
    // preset.alphabetSymbol is the Urdu symbol; its letter id follows our naming.
    const label = idsByPresetId(p.id);
    if (label) out.set(label, { symbol: p.alphabetSymbol, preset: p });
  }
  return out;
})();

function idsByPresetId(presetId: string): string | null {
  // Preset ids are semantic ("fist_thumb_side" → alif). Map them via symbol table.
  const symToLabel: Record<string, string> = {
    'ا': 'alif', 'ب': 'be', 'ع': 'ain', 'ڈ': 'dhal', 'س': 'seen', 'ط': 'toay',
    'چ': 'che', 'ی': 'chhoti_yeh', 'ل': 'laam', 'و': 'vao', 'ر': 're', 'ک': 'kaf',
    'خ': 'khe', 'ض': 'zwad', 'ف': 'fay',
  };
  for (const [sym, label] of Object.entries(symToLabel)) {
    const preset = content.presets.find((p) => p.alphabetSymbol === sym);
    if (preset && preset.id === presetId) return label;
  }
  return null;
}

/** Urdu symbol → letter item id (for typed fingerspelling). */
const idsBySymbol = new Map<string, string>([
  ['ا', 'letter_alif'], ['ب', 'letter_be'], ['پ', 'letter_pe'], ['ت', 'letter_te'],
  ['ٹ', 'letter_tte'], ['ث', 'letter_se'], ['ج', 'letter_jim'], ['چ', 'letter_che'],
  ['ح', 'letter_he'], ['خ', 'letter_khe'], ['د', 'letter_dal'], ['ڈ', 'letter_dhal'],
  ['ذ', 'letter_zal'], ['ر', 'letter_re'], ['ڑ', 'letter_rre'], ['ز', 'letter_ze'],
  ['ژ', 'letter_zhe'], ['س', 'letter_seen'], ['ش', 'letter_sheen'], ['ص', 'letter_swad'],
  ['ض', 'letter_zwad'], ['ط', 'letter_toay'], ['ظ', 'letter_zoay'], ['ع', 'letter_ain'],
  ['غ', 'letter_ghain'], ['ف', 'letter_fay'], ['ق', 'letter_qaf'], ['ک', 'letter_kaf'],
  ['گ', 'letter_gaf'], ['ل', 'letter_laam'], ['م', 'letter_meem'], ['ن', 'letter_noon'],
  ['و', 'letter_vao'], ['ہ', 'letter_hay'], ['ی', 'letter_chhoti_yeh'], ['ے', 'letter_bari_yeh'],
  ['ء', 'letter_hamza'],
]);

/**
 * Port of GestureConverterScreen — type text and watch it finger-spelled
 * with real hand photos, or sign live and let the camera name the shape.
 */
export function GestureConverterScreen() {
  const nav = useNavigate();
  const [tab, setTab] = useState<'type' | 'camera'>('type');
  const [text, setText] = useState('');
  const [lessons, setLessons] = useState<Lesson[] | null>(null);
  const [reading, setReading] = useState<CameraReading | null>(null);
  const [detected, setDetected] = useState<{ symbol: string; word: string; emoji: string; hint: string } | null>(null);
  const latestLabel = useRef<string | null>(null);

  useEffect(() => {
    let dead = false;
    loadLessons().then((ls) => { if (!dead) setLessons(ls); else void dead; }).catch(() => setLessons([]));
    return () => { dead = true; };
  }, []);

  const onReading = (r: CameraReading) => {
    setReading(r);
    if (r.confirmed) buzz([0, 120]);
    if (r.label) latestLabel.current = r.label;
    if (r.confirmed && r.label && symbolByLabel.has(r.label)) {
      const entry = symbolByLabel.get(r.label)!;
      setDetected({
        symbol: entry.symbol,
        word: entry.preset.alphabetWord,
        emoji: entry.preset.emoji,
        hint: entry.preset.funHint,
      });
    }
  };

  const lessonById = useMemo(() => {
    return (id: string) => lessons?.find((l) => l.id === id) ?? null;
  }, [lessons]);

  const typedLetters = useMemo(() => {
    const chars = [...text.normalize('NFC')];
    return chars.map((ch) => {
      const lower = ch.toLowerCase();
      if (/[a-z]/.test(lower) && itemById.has(`letter_en_${lower}`)) {
        return { ch: ch.toUpperCase(), id: `letter_en_${lower}` };
      }
      if (idsBySymbol.has(ch)) return { ch, id: idsBySymbol.get(ch)! };
      return { ch: ch === ' ' ? '␣' : ch, id: null };
    });
  }, [text]);

  return (
    <div className="page">
      <PageHeader
        title="Gesture Converter ✋"
        subtitle="Type or sign — watch it fingerspelled"
        onBack={() => nav('/')}
      />
      <div className="mode-tabs">
        <button className={`mode-tab ${tab === 'type' ? 'on' : ''}`} onClick={() => setTab('type')}>🔤 Type text</button>
        <button className={`mode-tab ${tab === 'camera' ? 'on' : ''}`} onClick={() => setTab('camera')}>📸 Sign live</button>
      </div>

      {tab === 'type' ? (
        <>
          <input
            className="text-input big"
            placeholder="Type English or Urdu — e.g. salaam"
            value={text}
            onChange={(e) => setText(e.target.value)}
            maxLength={40}
          />
          {typedLetters.length > 0 && (
            <div className="spell-row">
              {typedLetters.map((c, i) => {
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
          )}
          <p className="honest-note">
            Whole words and numbers in PSL need movement or both hands — the app and your teacher
            make a great team 🤝
          </p>
        </>
      ) : (
        <>
          <CameraPractice mode="psl" onReading={onReading} />
          <div className="detect-card">
            {detected ? (
              <>
                <div className="detect-big urdu">{detected.symbol}</div>
                <div className="detect-word">{detected.emoji} {detected.word}</div>
                <div className="detect-hint">💡 {detected.hint}</div>
                <button className="btn secondary" onClick={() => { buzz(); setDetected(null); }}>
                  Clear and sign again
                </button>
              </>
            ) : (
              <p className="status-msg">{reading?.message ?? 'Show your hand to the camera…'}</p>
            )}
          </div>
          <div className="preset-grid">
            {content.presets.map((p) => (
              <div key={p.id} className="preset-card" title={p.bodyDescription}>
                <div className="pre-emoji">{p.emoji}</div>
                <div className="pre-sym urdu">{p.alphabetSymbol}</div>
                <div className="pre-name">{p.name}</div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
