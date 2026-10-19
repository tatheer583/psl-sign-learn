/**
 * extract_new_dataset.mjs — feature extraction for the
 * saadbutt321/Pakistan-Sign-Language dataset (OpenPose, 21 hand points).
 *
 * Sources processed:
 *   C:/Users/1/Downloads/sign-language-database/main_dataset.db (SQLite)
 *   C:/Users/1/Downloads/sign-language-database/PSL_dataset/datasets/{alphabets,words}_dataset
 *
 * Output (in tools/):
 *   new-psl-landmarks.npz   X: float32 [n,63], y: letter ids, groups — python-loadable
 *   new-word-fixtures.json  sampled word-sign frames for guided practice
 *   new-dataset-report.json provenance + counts per class
 *
 * Feature conversion is EXACTLY the app classifier transform (Recognition.kt
 * / features.ts): wrist-centred, chirality sign by index-vs-pinky MCP x
 * order, scale by max norm. OpenPose has no depth channel, so z stays 0 —
 * a constant per query at inference, which preserves k-NN ranking and only
 * mildly tightens the rms acceptance gate (as documented in the code).
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import initSqlJs from 'sql.js';
import { writeNpz } from './lib/npy.mjs';
import { mojibakeToUtf8 } from './lib/cp437.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const TOOLS = path.resolve(HERE, '..');
const DATA = 'C:/Users/1/Downloads/sign-language-database';

// Urdu letter -> app letter id (matches LearningDataSource alphabet ids)
const LETTER_ID = {
  'ا': 'alif', 'ب': 'be', 'پ': 'pe', 'ت': 'te', 'ٹ': 'tte', 'ث': 'se', 'ج': 'jim',
  'چ': 'che', 'ح': 'he', 'خ': 'khe', 'د': 'dal', 'ڈ': 'dhal', 'ذ': 'zal', 'ر': 're',
  'ڑ': 'rre', 'ز': 'ze', 'ژ': 'zhe', 'س': 'seen', 'ش': 'sheen', 'ص': 'swad',
  'ض': 'zwad', 'ط': 'toay', 'ظ': 'zoay', 'ع': 'ain', 'غ': 'ghain', 'ف': 'fay',
  'ق': 'qaf', 'ک': 'kaf', 'گ': 'gaf', 'ل': 'laam', 'م': 'meem', 'ن': 'noon',
  'و': 'vao', 'ہ': 'hay', 'ی': 'chhoti_yeh', 'ے': 'bari_yeh', 'ء': 'hamza',
};

// wordDataset English labels -> Urdu word text used by the app content
const WORDS_EN = {
  thanks: 'شکریہ', my: 'میرا', little: 'کم', good: 'اچھا', me: 'میں', what: 'کیا',
  eat: 'کھانا', you: 'آپ', name: 'نام', know: 'جانتا', your: 'آپ کا', phone: 'فون',
};

const strip = (s) => s.replace(/[\u200b-\u200f\u202a-\u202e\u2066-\u2069\u061c\u00a0]/g, ' ').trim();

/** fold Arabic/Persian ye + Arabic-Indic presentation variants for matching */
const fold = (s) => strip(s).replace(/[\u064a\u0649]/g, '\u06cc');
/** EXACT port of LandmarkFeatures.normalize — input: 21 × (x, y, z) */
export function normalizePts(pts) {
  if (pts.length !== 21) return null;
  if (pts.some((p) => !Number.isFinite(p.x) || !Number.isFinite(p.y))) return null;
  const wrist = pts[0];
  const sign = pts[5].x < pts[17].x ? -1 : 1;
  let scale = 0;
  const v = new Float64Array(63);
  pts.forEach((p, i) => {
    const x = (p.x - wrist.x) * sign;
    const y = p.y - wrist.y;
    const z = (p.z - wrist.z);
    v[i * 3] = x; v[i * 3 + 1] = y; v[i * 3 + 2] = z;
    const norm = Math.sqrt(x * x + y * y + z * z);
    if (norm > scale) scale = norm;
  });
  if (scale < 30) return null; // pixel-space minimum hand span (OpenPose coords)
  for (let i = 0; i < 63; i++) v[i] /= scale;
  return Float32Array.from(v);
}

// ------------------------------------------------------------------ SQLite
async function fromDb() {
  const SQL = await initSqlJs();
  const db = new SQL.Database(fs.readFileSync(`${DATA}/main_dataset.db`));
  const letters = [];
  {
    const cols = [];
    for (let k = 1; k <= 21; k++) { cols.push(`x${k}`, `y${k}`); }
    cols.push('label');
    const sel = db.exec(`SELECT ${cols.join(',')} FROM rightHandDataset`);
    const names = sel[0].columns;
    for (const row of sel[0].values) {
      const g = {};
      names.forEach((c, i) => { g[c] = row[i]; });
      const pts = [];
      for (let k = 1; k <= 21; k++) pts.push({ x: g[`x${k}`], y: g[`y${k}`], z: 0 });
      const label = strip(String(g.label));
      const id = LETTER_ID[label];
      if (!id) { console.warn('unknown letter label in db:', label); continue; }
      // placeholder rows: openpose writes (150,150) for unresolved joints
      const unresolved = pts.filter((p) => Math.abs(p.x - 150) < 1e-9 && Math.abs(p.y - 150) < 1e-9).length;
      if (unresolved > 4) continue;
      letters.push({ pts, id, src: 'db:rightHand' });
    }
  }
  const words = [];
  {
    const sel = db.exec('SELECT * FROM wordDataset');
    const cols = sel[0].columns;
    for (const row of sel[0].values) {
      const g = {};
      cols.forEach((c, i) => { g[c] = row[i]; });
      const en = String(g.label);
      const pts = [];
      for (let k = 1; k <= 21; k++) pts.push({ x: g[`x${k}`], y: g[`y${k}`], z: 0 });
      const unresolved = pts.filter((p) => Math.abs(p.x - 150) < 1e-9 && Math.abs(p.y - 150) < 1e-9).length;
      if (unresolved > 4) continue;
      words.push({ pts, word: WORDS_EN[en] ?? en, en, src: 'db:word' });
    }
  }
  return { letters, words };
}

// ------------------------------------------------------------- Openpose JSON
function bestHand(people) {
  let best = null;
  for (const person of people) {
    for (const key of ['hand_right_keypoints_2d', 'hand_left_keypoints_2d']) {
      const arr = person[key];
      if (!Array.isArray(arr) || arr.length !== 63) continue;
      const pts = [];
      for (let k = 0; k < 21; k++) pts.push({ x: arr[k * 3], y: arr[k * 3 + 1], z: 0, c: arr[k * 3 + 2] });
      const live = pts.filter((p) => (Math.abs(p.x) > 1 || Math.abs(p.y) > 1) && p.c > 0.03);
      if (live.length < 15) continue;
      const meanC = live.reduce((s, p) => s + p.c, 0) / live.length;
      if (!best || meanC > best.meanC) best = { pts, meanC, side: key.includes('right') ? 'R' : 'L' };
    }
  }
  return best && best.meanC >= 0.45 ? best : null;
}

async function fromFolders() {
  const letters = [];
  const words = [];
  const alphabetDir = `${DATA}/PSL_dataset/datasets/alphabets_dataset`;
  const wordsDir = `${DATA}/PSL_dataset/datasets/words_dataset`;
  for (const letterDir of fs.readdirSync(alphabetDir)) {
    const fixed = strip(mojibakeToUtf8(letterDir));
    const id = LETTER_ID[fixed];
    if (!id) {
      console.warn(`unknown folder letter: "${fixed}" (${[...fixed].map((c) => c.codePointAt(0).toString(16)).join(' ')})`);
      continue;
    }
    const dir = path.join(alphabetDir, letterDir);
    for (const session of fs.readdirSync(dir)) {
      const sDir = path.join(dir, session);
      if (!fs.statSync(sDir).isDirectory()) continue;
      const files = fs.readdirSync(sDir).filter((f) => f.endsWith('_keypoints.json')).sort();
      for (const f of files) {
        try {
          const j = JSON.parse(fs.readFileSync(path.join(sDir, f), 'utf8'));
          const hand = bestHand(j.people ?? []);
          if (!hand) continue;
          letters.push({ pts: hand.pts, id, src: `json:${session}`, conf: hand.meanC });
        } catch { /* skip unreadable frame */ }
      }
    }
  }
  for (const wordDir of fs.readdirSync(wordsDir)) {
    const dir = path.join(wordsDir, wordDir);
    const wordText = fold(mojibakeToUtf8(wordDir));
    const en = Object.keys(WORDS_EN).find((k) => fold(WORDS_EN[k]) === wordText);
    for (const session of fs.readdirSync(dir)) {
      const sDir = path.join(dir, session);
      if (!fs.statSync(sDir).isDirectory()) continue;
      const files = fs.readdirSync(sDir).filter((f) => f.endsWith('_keypoints.json')).sort();
      for (const f of files) {
        try {
          const j = JSON.parse(fs.readFileSync(path.join(sDir, f), 'utf8'));
          const hand = bestHand(j.people ?? []);
          if (!hand) continue;
          words.push({ pts: hand.pts, word: en ? WORDS_EN[en] : wordText, en, src: `json:${session}` });
        } catch { /* skip */ }
      }
    }
  }
  return { letters, words };
}

// ------------------------------------------------------------------------- run
const [{ letters: dbLetters, words: dbWords }, { letters: jsonLetters, words: jsonWords }] = await Promise.all([fromDb(), fromFolders()]);

const all = [...dbLetters, ...jsonLetters];
console.log('letter frames: db', dbLetters.length, '+ json', jsonLetters.length, '=', all.length);
console.log('word frames: db', dbWords.length, '+ json', jsonWords.length, '=', dbWords.length + jsonWords.length);

// normalize + quality gate
const feats = [];
for (const s of all) {
  const v = normalizePts(s.pts);
  if (v) feats.push({ v, id: s.id, src: s.src });
}
console.log('normalized letter frames:', feats.length);

// retain only classes the classifier knows — all 37 are expected
const byClass = new Map();
for (const f of feats) {
  if (!byClass.has(f.id)) byClass.set(f.id, []);
  byClass.get(f.id).push(f);
}
const classes = [...byClass.keys()].sort();
console.log('classes:', classes.length, classes.map((c) => `${c}:${byClass.get(c).length}`).join('  '));

// sequence de-duplication: inside every session, drop frames within 0.03 of any kept frame
const kept = [];
const quant = (v) => Math.round(v * 100);
for (const id of classes) {
  const sessions = new Map();
  for (const f of byClass.get(id)) sessions.set(f.src, [...(sessions.get(f.src) ?? []), f]);
  for (const [, frames] of sessions) {
    const sessionKept = [];
    for (const f of frames) {
      const similar = sessionKept.some((g) => {
        let d = 0;
        for (let i = 0; i < 63; i += 3) {
          const dx = g.v[i] - f.v[i];
          const dy = g.v[i + 1] - f.v[i + 1];
          d += dx * dx + dy * dy;
        }
        return d < 0.0009; // (0.03)^2
      });
      if (!similar) { sessionKept.push(f); kept.push(f); }
    }
  }
}
console.log('after sequence dedup:', kept.length);

// shuffle, cap per class, yield final arrays
let seed = 20261019;
const rand = () => { seed = (seed * 1103515245 + 12345) & 0x7fffffff; return seed / 0x7fffffff; };
const CAP = 320;
const final = [];
for (const id of classes) {
  const list = kept.filter((f) => f.id === id);
  for (let i = list.length - 1; i > 0; i--) { const j = Math.floor(rand() * (i + 1)); [list[i], list[j]] = [list[j], list[i]]; }
  final.push(...list.slice(0, Math.min(list.length, CAP)));
}
console.log('final training frames:', final.length);

// ---- write NPZ (python-compatible) ----
const X = new Float32Array(final.length * 63);
final.forEach((f, i) => X.set(f.v, i * 63));
X.npzShape = [final.length, 63];
await writeNpz(path.join(TOOLS, 'new-psl-landmarks.npz'), {
  X,
  y: final.map((f) => f.id),
  groups: final.map((f) => f.src),
});
console.log('wrote tools/new-psl-landmarks.npz');

// ---- word fixtures for guided practice ----
const fixtures = {};
for (const word of new Set([...dbWords, ...jsonWords].map((w) => w.word))) {
  const frames = [...dbWords, ...jsonWords].filter((w) => w.word === word);
  const seen = new Set();
  const picked = [];
  for (let i = 0; i < frames.length && picked.length < 14; i++) {
    const f = frames[Math.floor(i * frames.length / Math.min(frames.length, 14))];
    const key = quant(f.pts[4].x) + '|' + quant(f.pts[4].y) + '|' + quant(f.pts[8].x);
    if (seen.has(key)) continue;
    seen.add(key);
    picked.push({
      points: f.pts.map((p) => [Math.round(p.x * 10) / 10, Math.round(p.y * 10) / 10]),
    });
  }
  fixtures[word] = { frames: picked, totalFrames: frames.length };
}
fs.writeFileSync(path.join(TOOLS, 'new-word-fixtures.json'), JSON.stringify(fixtures, null, 1));

// ---- report ----
const report = {
  source: 'Kaggle: saadbutt321/pakistan-sign-language-dataset (local copy at Downloads/sign-language-database)',
  format: 'OpenPose 21-point hand keypoints (2D pixel coordinates, no depth channel)',
  extracted: new Date().toISOString().slice(0, 10),
  rawLetterFrames: all.length,
  normalized: feats.length,
  afterSequenceDedup: kept.length,
  finalTrainingFrames: final.length,
  perClass: Object.fromEntries(classes.map((id) => [id, final.filter((f) => f.id === id).length])),
  wordFixtures: Object.fromEntries(Object.entries(fixtures).map(([w, f]) => [w, f.totalFrames])),
  notes: 'z channel is absent in OpenPose 2D hands; stored as 0. See tools/new_dataset/README.md for the k-NN constant-offset argument.',
};
fs.writeFileSync(path.join(TOOLS, 'new-dataset-report.json'), JSON.stringify(report, null, 2));
console.log('wrote tools/new-dataset-report.json');
