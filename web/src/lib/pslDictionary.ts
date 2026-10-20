/**
 * The official "Pakistan Sign Language" dictionary (FESF / Deaf Reach,
 * https://psl.org.pk/dictionary) — harvested once into a metadata-only index.
 *
 * Honesty rules baked into the data model:
 *  - Irssa bundles NO dictionary media; videos and posters stream from the
 *    official CDN while the entry links back to psl.org.pk.
 *  - English (ASL) letter lessons are never auto-linked to PSL dictionary
 *    entries — different language, different fingerspelling.
 */
import { assetUrl } from './util';

export interface PslDictEntry {
  term: string;        // "Book"
  urdu?: string;       // کتاب
  category: string;    // "Classroom"
  path: string;        // "/dictionary/24-classroom/1273-book"
  image?: string;      // official concept-image thumbnail (CDN)
  lessonRefs?: string[]; // Irssa lessons that reference this word
  video?: string;      // official 720p sign video (CDN), when harvested
  poster?: string;     // poster frame for the video
}

export interface PslDictIndex {
  generated: string;
  source: string;
  provider: string;
  license: string;
  wordCount: number;
  categories: Array<{ path: string; name: string }>;
  /** Irssa lesson id → dictionary word path (harvested matches). */
  lessons: Record<string, string>;
  words: PslDictEntry[];
}

let indexPromise: Promise<PslDictIndex | null> | null = null;

export function loadPslDictionary(): Promise<PslDictIndex | null> {
  indexPromise ??= fetch(assetUrl('marks/psl-dictionary-index.json'))
    .then((r) => (r.ok ? (r.json() as Promise<PslDictIndex>) : null))
    .catch(() => null);
  return indexPromise;
}

/** Irssa lesson id → its official dictionary entry (null if none was harvested). */
export async function entryForLesson(lessonId: string): Promise<PslDictEntry | null> {
  const idx = await loadPslDictionary();
  const path = idx?.lessons[lessonId];
  if (!idx || !path) return null;
  return idx.words.find((w) => w.path === path) ?? null;
}

function norm(s: string): string {
  return s.toLowerCase().replace(/[^a-z0-9\u0600-\u06FF]+/g, '');
}

export function searchEntries(index: PslDictIndex, query: string, limit = 240): PslDictEntry[] {
  const q = query.trim();
  if (!q) return [];
  const nq = norm(q);
  const scored: Array<{ e: PslDictEntry; score: number }> = [];
  for (const w of index.words) {
    const nt = norm(w.term);
    let score = -1;
    if (nt === nq) score = 0;
    else if (nt.startsWith(nq)) score = 1;
    else if (nt.includes(nq)) score = 2;
    else if (w.urdu && w.urdu.includes(q)) score = 1;
    else if (norm(w.category).includes(nq) || norm(w.category) === nq) score = 3;
    if (score >= 0) scored.push({ e: w, score, });
  }
  scored.sort(
    (a, b) => a.score - b.score
      || (Number(a.e.lessonRefs?.length ?? 0) > Number(b.e.lessonRefs?.length ?? 0) ? -1 : 0)
      || a.e.term.localeCompare(b.e.term),
  );
  return scored.slice(0, limit).map((s) => s.e);
}

export const dictPageUrl = (path: string) => `https://psl.org.pk${path}`;
export const dictSearchUrl = (q: string) =>
  `https://psl.org.pk/search?q=${encodeURIComponent(q.trim())}`;
