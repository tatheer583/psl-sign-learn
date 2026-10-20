/** Port of SignTtsHelper.kt — speechSynthesis with the same voice shape. */
const VOICE_KEY = 'irssa-voice-enabled';

let enabled = readEnabled();

function readEnabled(): boolean {
  try {
    return localStorage.getItem(VOICE_KEY) !== 'off';
  } catch {
    return true; // private mode: treat voice as on (it never records anything)
  }
}

export function ttsAvailable(): boolean {
  return 'speechSynthesis' in window;
}

export function ttsEnabled(): boolean {
  return enabled && ttsAvailable();
}

export function setTtsEnabled(value: boolean): void {
  enabled = value;
  try {
    localStorage.setItem(VOICE_KEY, value ? 'on' : 'off');
  } catch {
    /* private mode — keep the session value only */
  }
  if (value) warmVoices();
}

/**
 * Voice lists populate late on several engines — warm them so the very first
 * word a child hears is spoken, not swallowed by an empty voice list.
 */
let voicesWarmed = false;
function warmVoices(): void {
  if (!ttsAvailable() || voicesWarmed) return;
  const synth = window.speechSynthesis;
  voicesWarmed = synth.getVoices().length > 0;
  if (!voicesWarmed) {
    synth.addEventListener?.(
      'voiceschanged',
      () => { voicesWarmed = true; },
      { once: true },
    );
  }
}
if (ttsAvailable()) warmVoices();

/** Mirrors SignTtsHelper: prefer an English voice, else nothing (default voice). */
function primeVoice(): SpeechSynthesisVoice | null {
  const voices = window.speechSynthesis.getVoices();
  return (
    voices.find((v) => v.lang === 'en-US') ??
    voices.find((v) => v.lang.replace('_', '-').startsWith('en')) ??
    voices.find((v) => /english/i.test(v.name)) ??
    null
  );
}

export function speak(text: string): void {
  if (ttsEnabled()) doSpeak(text);
}

/**
 * Unconditional say-aloud — for explicit "Listen again" taps, which must
 * work even when automatic voice playback is switched off.
 */
export function speakAlways(text: string): void {
  if (ttsAvailable()) doSpeak(text);
}

/*
 * One utterance at a time, with a generation token:
 * every speak() invalidates every utterance scheduled before it, and the
 * only timer that may fire is the newest. Older timers dying at cancel
 * time is what fixes the "repeats the word again and again" ghosting.
 */
let speakToken = 0;
let pendingTimer: number | null = null;

function doSpeak(text: string): void {
  if (!ttsAvailable() || !text.trim()) return;
  const synth = window.speechSynthesis;
  const token = ++speakToken;
  if (pendingTimer !== null) {
    window.clearTimeout(pendingTimer);
    pendingTimer = null;
  }
  // Chrome can silently drop an utterance when speak() lands mid-cancel;
  // a beat after an actual cancel prevents that.
  const wasBusy = synth.speaking || synth.pending;
  synth.cancel();
  const utter = buildUtter(text);
  const start = () => {
    pendingTimer = null;
    if (token !== speakToken) return; // a newer voice request won — skip
    try { synth.speak(utter); } catch { /* ignore */ }
  };
  if (wasBusy) pendingTimer = window.setTimeout(start, 80);
  else start();
}

function buildUtter(text: string): SpeechSynthesisUtterance {
  const utter = new SpeechSynthesisUtterance(text);
  const voice = primeVoice();
  if (voice) utter.voice = voice;
  utter.lang = voice?.lang ?? 'en-US';
  utter.rate = 0.85;  // Friendly, clear pace for children
  utter.pitch = 1.15; // Warm, encouraging tone
  utter.onerror = () => { /* never fuss at the kid */ };
  return utter;
}

/** Web Speech recognition helper for Speech-to-Sign (Chrome / Edge). */
export function speechRecognitionAvailable(): boolean {
  return 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window;
}

export function startListening(
  onText: (text: string) => void,
  onError: (message: string) => void,
  lang = 'en-US',
): () => void {
  const Ctor = (window as any).SpeechRecognition ?? (window as any).webkitSpeechRecognition;
  if (!Ctor) {
    onError('Speech recognition is not available in this browser.');
    return () => undefined;
  }
  const rec = new Ctor();
  rec.lang = lang;
  rec.interimResults = false;
  rec.maxAlternatives = 1;
  let closed = false;
  rec.onresult = (e: any) => {
    const said = e.results?.[0]?.[0]?.transcript ?? '';
    if (said) onText(said);
  };
  rec.onerror = (e: any) => onError(e?.error ?? 'Could not hear that.');
  rec.onend = () => { closed = true; };
  try {
    rec.start();
  } catch {
    onError('Please allow microphone access and try again.');
  }
  return () => {
    if (!closed) {
      try { rec.stop(); } catch { /* ignore */ }
    }
  };
}
