/**
 * convert-content.mjs — extracts the Irssa learning content from the Android
 * Kotlin sources into JSON for the web app. Run from web/:  node tools/convert-content.mjs
 *
 * Parsed sources (the single source of truth stays in the Android app):
 *   app/src/main/java/com/example/data/LearningDataSource.kt
 *   app/src/main/java/com/example/data/model/GestureConversionModels.kt
 */
import { readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = dirname(fileURLToPath(import.meta.url));
const ROOT = join(HERE, '..', '..');
const KOTLIN = (p) => readFileSync(join(ROOT, p), 'utf8');
const DS = KOTLIN('app/src/main/java/com/example/data/LearningDataSource.kt');
const GC = KOTLIN('app/src/main/java/com/example/data/model/GestureConversionModels.kt');
const OUT = join(HERE, '..', 'src', 'content', 'content.json');

// ---------------------------------------------------------------- helpers
const S = '"((?:[^"\\\\]|\\\\.)*)"';

/** Balance-scan every `func(...)` call and return the inner text of each. */
function allCalls(src, name) {
  const out = [];
  let i = src.indexOf(name + '(');
  while (i !== -1) {
    let j = i + name.length, depth = 0, inStr = false;
    for (; j < src.length; j++) {
      const c = src[j];
      if (inStr) {
        if (c === '\\') { j++; continue; }
        if (c === '"') { inStr = false; continue; }
        continue;
      }
      if (c === '"') { inStr = true; continue; }
      if (c === '(') depth++;
      else if (c === ')') { depth--; if (depth === 0) break; }
    }
    out.push(src.slice(i + name.length + 1, j));
    i = src.indexOf(name + '(', j);
  }
  return out;
}

/** Grab `key = value` for scalar fields (string / number / enum) from a call body. */
function scalar(body, key) {
  const m = body.match(new RegExp('\\b' + key + '\\s*=\\s*(?:"((?:[^"\\\\]|\\\\.)*)"|(-?[\\dA-Za-z_.]+))'));
  if (!m) return undefined;
  if (m[1] !== undefined) return m[1];
  return m[2];
}
function intField(body, key, fallback) {
  const v = scalar(body, key);
  return v === undefined ? fallback : parseInt(v, 10);
}
function stringList(body, key) {
  const m = body.match(new RegExp('\\b' + key + '\\s*=\\s*listOf\\s*\\('));
  if (!m) return undefined;
  let i = m.index + m[0].length;
  let depth = 1, inStr = false, j = i;
  for (; j < body.length; j++) {
    const c = body[j];
    if (inStr) {
      if (c === '\\') { j++; continue; }
      if (c === '"') { inStr = false; continue; }
      continue;
    }
    if (c === '"') { inStr = true; continue; }
    if (c === '(') depth++;
    else if (c === ')') { depth--; if (depth === 0) break; }
  }
  const inner = body.slice(i, j);
  const items = [];
  const re = new RegExp(S, 'g');
  g: for (let k = 0; k < inner.length;) {
    re.lastIndex = k;
    const r = re.exec(inner);
    if (!r || r.index < k) break g;
    items.push(r[1]);
    k = r.index + r[0].length;
  }
  return items;
}
function enumName(body, key) {
  const v = scalar(body, key);
  return v === undefined ? undefined : String(v).split('.').pop();
}

/** Slice the balanced-parentheses body starting right after `listOf(`. */
function bodyFrom(src, startIdx) {
  let i = startIdx, depth = 1, j = i;
  for (; j < src.length; j++) {
    const c = src[j];
    if (c === '(') depth++;
    else if (c === ')') { depth--; if (depth === 0) break; }
  }
  return src.slice(i, j);
}
/** Slice the body of `val name: ... = listOf( ... )` (top-level section). */
function sectionList(src, name) {
  const re = new RegExp('val ' + name + '\\s*:[^=]*=\\s*listOf\\s*\\(');
  const m = src.match(re);
  if (!m) throw new Error('section not found: ' + name);
  let i = m.index + m[0].length, depth = 1, j = i;
  for (; j < src.length; j++) {
    if (src[j] === '(') depth++;
    else if (src[j] === ')') { depth--; if (depth === 0) break; }
  }
  return src.slice(i, j);
}

// ------------------------------------------------------------- learning items
function item(callBody) {
  return {
    id: scalar(callBody, 'id'),
    title: scalar(callBody, 'title'),
    subtitle: scalar(callBody, 'subtitle'),
    category: enumName(callBody, 'category'),
    level: intField(callBody, 'level', 1),
    signName: scalar(callBody, 'signName'),
    visualCueSteps: stringList(callBody, 'visualCueSteps') ?? [],
    visualItemsCount: intField(callBody, 'visualItemsCount', 0),
    objectEmoji: scalar(callBody, 'objectEmoji'),
    funFact: scalar(callBody, 'funFact'),
    handPoseType: enumName(callBody, 'handPoseType'),
    colorHex: parseInt(scalar(callBody, 'colorHex') ?? '0xFF5C52E5', 16) >>> 0,
    associatedWord: scalar(callBody, 'associatedWord') ?? '',
  };
}

function parseItems(name) {
  return allCalls(sectionList(DS, name), 'LearningItem').map(item);
}

const alphabetItems = parseItems('alphabetItems');
const englishAlphabetItems = parseItems('englishAlphabetItems');
const vocabularyItems = parseItems('vocabularyItems');

// ------------------------------------------------------------------- numbers
// Port of `(0..50).map { num -> ... }` in LearningDataSource.kt (kotlinevaluated by hand).
const emojiMatch = DS.match(/val\s+numberEmojis\s*=\s*listOf\s*\(/);
if (!emojiMatch) throw new Error('numberEmojis not found');
const emojiBlock = bodyFrom(DS, emojiMatch.index + emojiMatch[0].length);
const numberEmojis = [...emojiBlock.matchAll(new RegExp(S, 'g'))].map((m) => m[1]);

const POSE = {
  1: 'ONE_INDEX', 2: 'TWO_PEACE', 3: 'THREE_THUMB_TWO', 4: 'FOUR_FINGERS',
  5: 'FIVE_OPEN_PALM', 6: 'THUMB_TOUCH_PINKY', 7: 'THUMB_TOUCH_RING',
  8: 'THUMB_TOUCH_MIDDLE', 9: 'THUMB_TOUCH_INDEX', 10: 'FIST_THUMB_SIDE',
};
const COLORS = [0xFFFF5E7E, 0xFF5C52E5, 0xFF00B894, 0xFFFF7849, 0xFFFFC312];

const numberItems = [];
for (let num = 0; num <= 50; num++) {
  const tens = Math.trunc(num / 10), units = num % 10;
  const pose = num in POSE ? POSE[num] : 'COMPOUND_TENS';
  let cue;
  if (num <= 5) cue = [
    `Count ${num} little stars or blocks with your finger.`,
    `Show the number sign your teacher uses for ${num}.`,
    'Point at the number and count out loud together!',
  ];
  else if (num <= 10) cue = [
    `Collect ${num} small things like leaves or buttons.`,
    `Practise the PSL number sign for ${num} with a grown-up.`,
    'Line up your treasures and count them again!',
  ];
  else if (num % 10 === 0) cue = [
    `Count in tens up to ${tens}, then finish with zero.`,
    `Ask your teacher to show how ${num} is signed.`,
    `Clap ${tens} times, then once more for luck!`,
  ];
  else cue = [
    `Break ${num} into ${tens} tens and ${units} ones.`,
    'Practise both number signs slowly, then together.',
    `Count blocks, spoons or steps to reach ${num}!`,
  ];
  const emoji = numberEmojis[Math.max(0, num - 1)] ?? '⭐';
  const colorHex = COLORS[num % 5];
  const level = num <= 5 ? 1 : num <= 10 ? 2 : num <= 25 ? 3 : 4;
  numberItems.push({
    id: `num_${num}`, title: `${num}`, subtitle: `Number ${num}`,
    category: 'NUMBER', level: level,
    signName: num <= 10
      ? `PSL number sign for ${num} — learn it with your teacher`
      : `Compound sign for ${num} — learn it with your teacher`,
    visualCueSteps: cue, visualItemsCount: num <= 10 ? num : 0,
    objectEmoji: emoji,
    funFact: `${num} ${emoji} objects! You're an incredible math & sign explorer!`,
    handPoseType: pose, colorHex: colorHex, associatedWord: '',
  });
}

// -------------------------------------------------------------------- levels
const vowelIds = alphabetItems.filter((it) => ['ا', 'و', 'ی', 'ے'].includes(it.title)).map((it) => it.id);
const levelCalls = allCalls(sectionList(DS, 'levels'), 'GameLevel');
console.assert = console.assert; // eslint no-console guard
const safeMap = levelCalls.map((b) => {
  const ids = scalar(b, 'itemIds');
  // itemIds is an expression, mapped to concrete ids below by level id.
  return {
    id: intField(b, 'id', 0),
    title: scalar(b, 'title'),
    subtitle: scalar(b, 'subtitle'),
    description: scalar(b, 'description'),
    requiredStars: intField(b, 'requiredStars', 0),
    badgeReward: scalar(b, 'badgeReward'),
    badgeEmoji: scalar(b, 'badgeEmoji'),
    colorHex: parseInt(scalar(b, 'colorHex') ?? '0xFF5C52E5', 16) >>> 0,
    _idsExpr: ids ? undefined : undefined, // mark that we built ids programmatically
  };
});
const idsByLevel = (
  lvl) => {
  const alpha = alphabetItems.map((i) => i.id);
  const nums = (a, b) => { const o = []; for (let i = a; i <= b; i++) o.push(`num_${i}`); return o; };
  switch (lvl) {
    case 1: return [...vowelIds, ...nums(1, 5)];
    case 2: return [...alpha.slice(0, 12), ...nums(6, 20)];
    case 3: return [...alpha.slice(12), ...nums(21, 35), 'vocab_hello', 'vocab_thanks', 'vocab_water', 'vocab_friend'];
    case 4: return [...nums(36, 50), 'vocab_book', 'vocab_happy', 'vocab_school', 'vocab_love'];
    case 5: return [...alpha.slice(0, 5), 'num_5', 'num_10', 'num_25', 'num_50', 'vocab_hello', 'vocab_thanks', 'vocab_love'];
    default: return [];
  }
};
const levels = safeMap.map((l) => ({ ...l, itemIds: idsByLevel(l.id) }));

// ------------------------------------------------------------------- badges
const badges = allCalls(sectionList(DS, 'badges'), 'StickerBadge').map((b) => {
  const strings = [...b.matchAll(new RegExp(S, 'g'))].map((m) => m[1]);
  const nums = [...b.matchAll(/\d+/g)].map((m) => parseInt(m[0], 10));
  return {
    id: strings[0], title: strings[1], description: strings[2],
    iconEmoji: strings[3], requiredStars: nums[0],
    category: enumName(b, 'category') ?? 'Explorer',
  };
});
// badge_speed_match has no number in strings; the positional count regexed above
// resolves each badge's requiredStars from its declared integer only.

// ------------------------------------------------------------------ presets
const presets = allCalls(sectionList(GC, 'presets'), 'HandBodyGesturePreset').map((b) => ({
  id: scalar(b, 'id'),
  name: scalar(b, 'name'),
  emoji: scalar(b, 'emoji'),
  bodyDescription: scalar(b, 'bodyDescription'),
  alphabetSymbol: scalar(b, 'alphabetSymbol'),
  numberSymbol: scalar(b, 'numberSymbol') ?? '',
  alphabetWord: scalar(b, 'alphabetWord'),
  numberWord: scalar(b, 'numberWord'),
  pose: enumName(b, 'pose'),
  funHint: scalar(b, 'funHint'),
}));

// --------------------------------------------------------------- word labels
// Port of LearningItem.wordLabel from data/model/LearningModels.kt.
const wordLabels = {
  letter_en_a: 'Apple', letter_en_b: 'Bear', letter_en_c: 'Cat', letter_en_d: 'Dog',
  letter_en_e: 'Elephant', letter_en_f: 'Fox', letter_en_g: 'Giraffe', letter_en_h: 'Horse',
  letter_en_i: 'Ice Cream', letter_en_j: 'Jelly', letter_en_k: 'Kangaroo', letter_en_l: 'Lion',
  letter_en_m: 'Monkey', letter_en_n: 'Nest', letter_en_o: 'Orange', letter_en_p: 'Panda',
  letter_en_q: 'Queen', letter_en_r: 'Rainbow', letter_en_s: 'Star', letter_en_t: 'Turtle',
  letter_en_u: 'Umbrella', letter_en_v: 'Violin', letter_en_w: 'Whale', letter_en_x: 'Xylophone',
  letter_en_y: 'Yo-Yo', letter_en_z: 'Zebra',
  letter_alif: 'Anaar (Pomegranate)', letter_be: 'Bakri (Goat)', letter_pe: 'Patang (Kite)',
  letter_te: 'Titli (Butterfly)', letter_tte: 'Topi (Cap)', letter_se: 'Samar (Fruit)',
  letter_jim: 'Jahaaz (Airplane)', letter_che: 'Chaand (Moon)', letter_he: 'Halwa (Sweet)',
  letter_khe: 'Kharbooza (Melon)', letter_dal: 'Darwaza (Door)', letter_dhal: 'Dhol (Drum)',
  letter_zal: 'Zameen (Earth)', letter_re: 'Roti (Bread)', letter_rre: 'Barish (Rain)',
  letter_ze: 'Zebra', letter_zhe: 'Zaala (Hail)', letter_seen: 'Seb (Apple)',
  letter_sheen: 'Sher (Lion)', letter_swad: 'Sabun (Soap)', letter_zwad: 'Zarb (Multiply)',
  letter_toay: 'Tota (Parrot)', letter_zoay: 'Zarf (Pot)', letter_ain: 'Ainak (Glasses)',
  letter_ghain: 'Ghubara (Balloon)', letter_fay: 'Football', letter_qaf: 'Qalam (Pen)',
  letter_kaf: 'Kitaab (Book)', letter_gaf: 'Gaari (Car)', letter_laam: 'Laddu (Sweet)',
  letter_meem: 'Machhli (Fish)', letter_noon: 'Nariyal (Coconut)', letter_vao: 'Wuzu (Wash)',
  letter_hay: 'Haath (Hand)', letter_chhoti_yeh: 'Yaqoot (Ruby)', letter_bari_yeh: 'Khel (Play)',
  letter_hamza: 'Juz (Part)',
};

const content = {
  generated: new Date().toISOString().slice(0, 10),
  alphabetItems, englishAlphabetItems, numberItems, vocabularyItems,
  levels, badges, presets, wordLabels,
};

writeFileSync(OUT, JSON.stringify(content));
console.log(
  'content.json written:',
  alphabetItems.length, 'PSL letters |', englishAlphabetItems.length, 'English letters |',
  numberItems.length, 'numbers |', vocabularyItems.length, 'words |',
  levels.length, 'levels |', badges.length, 'badges |', presets.length, 'presets'
);
const missing = [...content.alphabetItems, ...content.englishAlphabetItems, ...content.numberItems, ...content.vocabularyItems]
  .filter((it) => !it.id || !it.title || !it.funFact || !it.objectEmoji || !it.signName || !it.subtitle || !it.handPoseType);
if (missing.length) {
  console.error('INCOMPLETE PARSING, ids:', missing.map((m) => m.id));
  process.exit(1);
}
console.log('parse OK — every item complete.');
