/** Port of SignTtsHelper.kt — speechSynthesis with the same voice shape. */
let enabled = false;

export function ttsAvailable(): boolean {
  return 'speechSynthesis' in window;
}

export function ttsEnabled(): boolean {
  return enabled && ttsAvailable();
}

/** Mirrors SignTtsHelper: voices exist only after some engines warm up. */
function primeVoice(): SpeechSynthesisVoice | null {
  const voices = window.speechSynthesis.getVoices();
  return voices.find((v) => v.lang === 'en-US') ?? voices.find((v) => v.lang.startsWith('en')) ?? null;
}

export function speak(text: string): void {
  if (!ttsAvailable() || !text.trim()) return;
  try {
    const synth = window.speechSynthesis;
    synth.cancel();
    const utter = new SpeechSynthesisUtterance(text);
    const voice = primeVoice();
    if (voice) utter.voice = voice;
    utter.lang = voice?.lang ?? 'en-US';
    utter.rate = 0.85;  // Friendly, clear pace for children
    utter.pitch = 1.15; // Warm, encouraging tone
    synth.speak(utter);
  } catch {
    /* graceful fallback */
  }
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

export function setTtsEnabled(value: boolean): void {
  enabled = value;
}
