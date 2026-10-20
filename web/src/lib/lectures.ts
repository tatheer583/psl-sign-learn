/**
 * Teacher lecture videos, served locally from marks/lectures/.
 * The manifest decides what exists — an empty manifest must never fake a
 * missing lecture, the UI honestly says none are uploaded yet.
 */
import { assetUrl } from './util';

export interface Lecture {
  title: string;
  description?: string;
  /** File name under public/marks/lectures/ (bundled with the site, local playback only). */
  file: string;
  author?: string;
  license?: string;
  /** Optional relevance: lesson ids or search keywords this lecture teaches. */
  topics?: string[];
}

let promise: Promise<Lecture[]> | null = null;

export function loadLectures(): Promise<Lecture[]> {
  promise ??= fetch(assetUrl('marks/lectures/manifest.json'))
    .then((r) => (r.ok ? r.json() : ({} as any)))
    .then((m: { lectures?: Lecture[] }) => (Array.isArray(m?.lectures) ? m.lectures : []))
    .catch(() => []);
  return promise;
}
