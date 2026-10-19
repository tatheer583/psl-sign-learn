import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { itemById } from '../content/types';
import { SignMediaView } from '../components/SignMediaView';
import { PageHeader } from '../components/Pieces';
import { loadLessons, type Lesson } from '../lib/lessons';
import {
  speak, speechRecognitionAvailable, startListening, ttsAvailable, setTtsEnabled,
} from '../lib/tts';
import { useStore } from '../store/store';

/**
 * Port of SpeechToSignScreen — speak or type a word, watch it finger-spelled.
 * (Web Speech API; Chrome/Edge support it best. Never leaves the device.)
 */
export function SpeechToSignScreen() {
  const nav = useNavigate();
  const { learner, update } = useStore();
  const [lessons, setLessons] = useState<Lesson[] | null>(null);
  const [word, setWord] = useState('');
  const [listening, setListening] = useState(false);
  const [voiceOn, setVoiceOn] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    let dead = false;
    loadLessons().then((ls) => { if (!dead) setLessons(ls); }).catch(() => setLessons([]));
    return () => { dead = true; };
  }, []);

  const lessonById = useMemo(() => (id: string) => lessons?.find((l) => l.id === id) ?? null, [lessons]);

  const applyWord = (w: string) => {
    const cleaned = w.trim().slice(0, 40);
    setWord(cleaned);
    setErr(null);
    if (cleaned && voiceOn) speak(cleaned);
  };

  const toggleListen = () => {
    if (!speechRecognitionAvailable()) {
      setErr('Speech recognition works in Chrome / Edge — or just type the word below. 💛');
      return;
    }
    if (listening) { setListening(false); return; }
    setListening(true);
    const stop = startListening(
      (said) => { applyWord(said); setListening(false); },
      (m) => { setErr(`Could not hear that (${m}). Try again, or type below. 💛`); setListening(false); },
    );
    setTimeout(() => { stop(); setListening(false); }, 7000);
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
        disabled={!speechRecognitionAvailable()}
      >
        {listening ? '🎙 Listening…' : '🎙 Tap and say a word'}
      </button>
      {err && <p className="error-note">{err}</p>}

      <input
        className="text-input big"
        placeholder="…or type it here"
        value={word}
        onChange={(e) => setWord(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && applyWord(word)}
        maxLength={40}
      />

      {letters.length > 0 && (
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
      <p className="honest-note">Speech and text never leave this device — nothing is uploaded.</p>
    </div>
  );
}
