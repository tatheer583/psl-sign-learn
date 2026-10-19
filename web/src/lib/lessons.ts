/**
 * Port of Lesson / LessonCatalog / recognizedLetters from
 * app/src/main/java/com/example/irssa/LearningStore.kt,
 * joined with app/src/main/assets/media_manifest.json.
 */
import { assetUrl } from './util';
import { allItems, wordLabel } from '../content/types';
import type { LearningItem } from '../content/types';

export interface ManifestEntry {
  id: string;
  file: string;
  type: string;
  gloss: string;
  source: string;
  author: string;
  license: string;
  note?: string;
}

export interface Lesson {
  id: string;
  title: string;
  word: string;
  emoji: string;
  category: 'Letters' | 'Numbers' | 'Words';
  media: string;
  isVideo: boolean;
  source: string;
  author: string;
  license: string;
  steps: string[];
  item: LearningItem;
  // derived — copied from the Kotlin Lesson class:
  isEnglish: boolean;
  language: string;
  isStatic: boolean;
  recognitionMode: 'english' | 'psl';
  classifierLabel: string;
}

// All 37 PSL handshapes have multi-signer training data (Mendeley + HF).
// English J and Z stay guided practice.
const RECOGNIZED_LETTERS = new Set([
  'letter_ain', 'letter_alif', 'letter_bari_yeh', 'letter_be', 'letter_che', 'letter_chhoti_yeh',
  'letter_dal', 'letter_dhal', 'letter_fay', 'letter_gaf', 'letter_ghain', 'letter_hamza', 'letter_hay', 'letter_he',
  'letter_jim', 'letter_kaf', 'letter_khe', 'letter_laam', 'letter_meem', 'letter_noon', 'letter_pe',
  'letter_qaf', 'letter_re', 'letter_rre', 'letter_se', 'letter_seen', 'letter_sheen', 'letter_swad',
  'letter_te', 'letter_toay', 'letter_tte', 'letter_vao', 'letter_zal', 'letter_zay', 'letter_ze', 'letter_zhe',
  'letter_zoay', 'letter_zwad',
]);

function lessonOf(item: LearningItem, entries: Map<string, ManifestEntry>): Lesson | null {
  const a = entries.get(item.id);
  if (!a) {
    // Missing teaching asset — Android would fail fast; web shows a soft fallback.
    console.warn('No media for item', item.id);
    return null;
  }
  const isEnglish = item.id.startsWith('letter_en_');
  const category: Lesson['category'] =
    item.id.startsWith('letter_') ? 'Letters' :
    item.id.startsWith('num_') ? 'Numbers' : 'Words';
  const isVideo = Boolean(a.file) && a.type === 'video';
  return {
    id: item.id,
    title: item.title,
    word: wordLabel(item.id),
    emoji: item.objectEmoji,
    category,
    media: a.file,
    isVideo: isVideo && Boolean(a.file),
    source: a.source,
    author: a.author,
    license: a.license,
    steps: item.visualCueSteps,
    item,
    isEnglish,
    language: isEnglish ? 'English (ASL)' : (category === 'Letters' ? 'Urdu (PSL)' : 'Guided practice'),
    isStatic: category === 'Letters' &&
      (isEnglish ? !['J', 'Z'].includes(item.title) : RECOGNIZED_LETTERS.has(item.id)),
    recognitionMode: isEnglish ? 'english' : 'psl',
    classifierLabel: isEnglish ? item.id.replace('letter_en_', '') : item.id.replace('letter_', ''),
  };
}

let manifestPromise: Promise<Map<string, ManifestEntry>> | null = null;

export function loadManifest(): Promise<Map<string, ManifestEntry>> {
  if (!manifestPromise) {
    manifestPromise = fetch(assetUrl('marks/media_manifest.json'))
      .then((r) => r.json() as Promise<ManifestEntry[]>)
      .then((list) => new Map(list.map((e) => [/** hmm: manifest ids, one per item */ e.id, e])));
  }
  return manifestPromise;
}

/** Same shape and order as the Android LessonCatalog.load(). */
export async function loadLessons(): Promise<Lesson[]> {
  const entries = await loadManifest();
  return allItems
    .map((item) => lessonOf(item, entries))
    .filter((l): l is Lesson => l !== null);
}

export const CATEGORY_TITLES: Record<Lesson['category'], string> = {
  Letters: 'Letters',
  Numbers: 'Numbers',
  Words: 'Words',
};

export function mediaUrl(lesson: Lesson): string {
  return lesson.media ? assetUrl(`marks/${lesson.media}`) : '';
}

export { RECOGNIZED_LETTERS };
