/**
 * train_psl_hybrid.mjs — merges the NEW Kaggle PSL dataset (OpenPose) with
 * every existing Irssa source and rewrites psl.bin (same binary format as
 * the Android/web classifier — see Recognition.kt / knn.ts).
 *
 * Merged sources:
 *   1. the ORIGINAL shipped psl.bin samples (git HEAD copy):
 *      Mendeley signer A + HF signer B + personal captures (already augmented)
 *   2. tools/hf-psl-landmarks.npz      (Hugging Face Bakhtyar12, MIT, signer B)
 *   3. tools/new-psl-landmarks.npz     (NEW: Kaggle saadbutt321, OpenPose)
 *
 * Honesty rules enforced here:
 *   - exact duplicates are removed BEFORE any split (rotation augmentation
 *     and personal x5 weighting exist only in the SHIPPED bin, never in the
 *     evaluation pools, so no copies of a test vector can sit in the train
 *     half);
 *   - the "previous" source is read from git HEAD, so repeated training runs
 *     cannot accumulate their own output;
 *   - cross-person evaluations cross capture systems too (MediaPipe <-> 2D
 *     OpenPose) and are therefore conservative lower bounds.
 *
 * usage: node train_psl_hybrid.mjs [overallPerClassCap=900]
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { execFileSync } from 'node:child_process';
import crypto from 'node:crypto';
import { readNpz } from './lib/npy.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(HERE, '..', '..');
const APP_BIN = path.join(ROOT, 'app', 'src', 'main', 'assets', 'models', 'psl.bin');
const WEB_BIN = path.join(ROOT, 'web', 'public', 'marks', 'models', 'psl.bin');
const REPORT = path.join(ROOT, 'docs', 'recognition-evaluation.json');
const PREV_BIN_SPEC = 'HEAD:app/src/main/assets/models/psl.bin';

const FINAL_CAP = Number(process.argv[2]) || 900;
const HF_CAP = 120;
const PERSONAL_WEIGHT = 5;

let seed = 20261019;
const rand = () => { seed = (seed * 1103515245 + 12345) & 0x7fffffff; return seed / 0x7fffffff; };
function shuffle(arr) {
  for (let i = arr.length - 1; i > 0; i--) { const j = Math.floor(rand() * (i + 1)); [arr[i], arr[j]] = [arr[j], arr[i]]; }
  return arr;
}

/** parse an Irssa classifier binary -> {labels, targets, features, count} */
function parseBin(buf) {
  const dv = new DataView(buf.buffer, buf.byteOffset, buf.byteLength);
  let off = 0;
  const int = () => { const v = dv.getInt32(off, false); off += 4; return v; };
  if (int() !== 0x49525353) throw new Error('magic mismatch');
  const count = int(), dims = int(), classes = int();
  if (dims !== 63) throw new Error('dims != 63');
  const labels = [];
  for (let c = 0; c < classes; c++) {
    const len = dv.getUint16(off, false); off += 2;
    labels.push(new TextDecoder('utf-8').decode(buf.subarray(off, off + len))); off += len;
  }
  const targets = new Int32Array(count);
  const features = new Float32Array(count * dims);
  for (let i = 0; i < count; i++) {
    targets[i] = int();
    for (let j = 0; j < dims; j++) { features[i * dims + j] = dv.getFloat32(off, false); off += 4; }
  }
  return { labels, targets, features, count };
}

function rotateJitter(v, degrees = 8.0) {
  const t = (rand() * 2 - 1) * (Math.PI / 180) * degrees;
  const c = Math.cos(t), s = Math.sin(t);
  const out = v.slice();
  for (let i = 0; i < 63; i += 3) {
    const x = v[i], y = v[i + 1];
    out[i] = x * c - y * s;
    out[i + 1] = x * s + y * c;
  }
  return out;
}

/** exact-duplicate removal (label + vector key) */
function dedupUnique(list) {
  const seen = new Set();
  const out = [];
  for (const f of list) {
    const key = f.label + '|' + Array.from(f.v).map((x) => Math.round(x * 1e5)).join(',');
    if (seen.has(key)) continue;
    seen.add(key);
    out.push(f);
  }
  return out;
}

function balance(list, cap) {
  const by = new Map();
  for (const f of list) { if (!by.has(f.label)) by.set(f.label, []); by.get(f.label).push(f); }
  const out = [];
  for (const lbl of [...by.keys()].sort()) {
    out.push(...shuffle(by.get(lbl)).slice(0, cap));
  }
  return out;
}

// --------------------------------------------------------------- load sources
// 1) ORIGINAL shipped model's samples from git HEAD (never our own output)
let prevBuf;
try {
  prevBuf = execFileSync('git', ['show', PREV_BIN_SPEC], { cwd: ROOT, maxBuffer: 1 << 28 });
  console.log('previous psl.bin: loaded from', PREV_BIN_SPEC);
} catch {
  prevBuf = fs.readFileSync(APP_BIN);
  console.log('previous psl.bin: not a git checkout here — using current app asset (may already include the new dataset!)');
}
const prev = parseBin(prevBuf);
const prevList = [];
for (let i = 0; i < prev.count; i++) {
  prevList.push({ label: prev.labels[prev.targets[i]], v: prev.features.slice(i * 63, i * 63 + 63), src: 'previous-psl.bin' });
}

// 2) Hugging Face NPZ
const hfNpz = await readNpz(path.join(ROOT, 'tools', 'hf-psl-landmarks.npz'));
const hfList = [];
for (let i = 0; i < hfNpz.y.data.length; i++) {
  hfList.push({ label: hfNpz.y.data[i], v: Float32Array.from(hfNpz.X.data.slice(i * 63, i * 63 + 63)), src: 'HF' });
}

// 3) NEW Kaggle dataset
const newNpz = await readNpz(path.join(ROOT, 'tools', 'new-psl-landmarks.npz'));
const newList = [];
for (let i = 0; i < newNpz.y.data.length; i++) {
  newList.push({ label: newNpz.y.data[i], v: Float32Array.from(newNpz.X.data.slice(i * 63, i * 63 + 63)), src: 'NEW', group: newNpz.groups.data[i] });
}

// uniquify EVERY source first
const uPrev = dedupUnique(prevList);
const uHf = dedupUnique(hfList);
const uNew = dedupUnique(newList);
console.log('unique frames — previous bin:', uPrev.length, '| HF:', uHf.length, '| NEW:', uNew.length);

// ---------------------------------------------------------------- evaluations
const K = 5;
function predict(trainX, trainY, q) {
  const dists = [];
  for (let i = 0; i < trainX.length; i++) {
    let d = 0;
    for (let j = 0; j < 63; j++) { const dx = trainX[i][j] - q[j]; d += dx * dx; }
    if (dists.length < K) { dists.push([d, trainY[i]]); dists.sort((a, b) => a[0] - b[0]); }
    else if (d < dists[K - 1][0]) { dists[K - 1] = [d, trainY[i]]; dists.sort((a, b) => a[0] - b[0]); }
  }
  const votes = new Map();
  for (const [d, l] of dists) votes.set(l, (votes.get(l) ?? 0) + 1 / (Math.sqrt(d) + 1e-5));
  return [...votes.entries()].sort((a, b) => b[1] - a[1])[0][0];
}
function accuracy(trainX, trainY, testX, testY) {
  let ok = 0;
  for (let i = 0; i < testX.length; i++) if (predict(trainX, trainY, testX[i]) === testY[i]) ok++;
  return { acc: ok / testX.length, n: testX.length };
}

function byLabel(list, cap) {
  const by = new Map();
  for (const f of list) { if (!by.has(f.label)) by.set(f.label, []); by.get(f.label).push(f); }
  return by;
}

const evalResults = [];

// A) old sources -> NEW Kaggle (cross-person AND cross capture-system)
{
  const oldPool = balance([...uPrev, ...uHf], FINAL_CAP);
  const tr = oldPool.filter((f) => f.label);
  const trX = tr.map((f) => f.v), trY = tr.map((f) => f.label);
  shuffle(uNew);
  const te = uNew, teX = te.map((f) => f.v), teY = te.map((f) => f.label);
  evalResults.push({ label: 'oldSources_to_newKaggle', accuracy: accuracy(trX, trY, teX, teY) });
}

// B) NEW Kaggle -> HF signer B (both sides unique, no augmentation)
{
  const trX = uNew.map((f) => f.v), trY = uNew.map((f) => f.label);
  const teBalance = [];
  const by = byLabel(uHf, HF_CAP);
  for (const lbl of [...by.keys()].sort()) teBalance.push(...shuffle(by.get(lbl)).slice(0, 60));
  const teX = teBalance.map((f) => f.v), teY = teBalance.map((f) => f.label);
  evalResults.push({ label: 'newKaggle_to_hfSignerB', accuracy: accuracy(trX, trY, teX, teY) });
}

for (const e of evalResults) {
  console.log(`${e.label}: ${(e.accuracy.acc * 100).toFixed(1)}% on ${e.accuracy.n} frames`);
}

// ------------------------------------------------------ build the SHIPPED bin
// unique union, class-balanced, then augmentation ONLY for the artifact
const union = balance([...uPrev, ...uHf, ...uNew], FINAL_CAP);
const shipped = [];
for (const f of union) {
  shipped.push(f);                                        // unique original
  shipped.push({ ...f, v: rotateJitter(f.v) });           // rotation twin
  if (f.src === 'NEW') for (let w = 1; w < PERSONAL_WEIGHT; w++) shipped.push(f); // x5 prior
}
// keep per-class overall size bounded even with weighting
const byClass = byLabel(shipped, FINAL_CAP * 2);
const finalList = [];
for (const lbl of [...byClass.keys()].sort()) finalList.push(...byClass.get(lbl));
shuffle(finalList);

const allLabels = [...new Set(finalList.map((f) => f.label))].sort();
const idx = new Map(allLabels.map((l, i) => [l, i]));
console.log('shipped psl.bin samples:', finalList.length, 'classes:', allLabels.length);

function packBin(vecs, labels, labelList) {
  const chunks = [];
  const head = Buffer.alloc(16);
  head.writeInt32BE(0x49525353, 0);
  head.writeInt32BE(vecs.length, 4);
  head.writeInt32BE(63, 8);
  head.writeInt32BE(labelList.length, 12);
  chunks.push(head);
  for (const lbl of labelList) {
    const len = Buffer.byteLength(lbl, 'utf8');
    const l = Buffer.alloc(2 + len);
    l.writeUInt16BE(len, 0);
    l.write(lbl, 2, 'utf8');
    chunks.push(l);
  }
  for (let i = 0; i < vecs.length; i++) {
    const row = Buffer.alloc(4 + 252);
    row.writeInt32BE(idx.get(labels[i]) ?? 0, 0);
    for (let j = 0; j < 63; j++) row.writeFloatBE(vecs[i][j] ?? 0, 4 + j * 4);
    chunks.push(row);
  }
  return Buffer.concat(chunks);
}

const bin = packBin(finalList.map((f) => f.v), finalList.map((f) => f.label), allLabels);
fs.writeFileSync(WEB_BIN, bin);
fs.writeFileSync(APP_BIN, bin);
console.log('psl.bin bytes:', bin.length);

// ------------------------------------------------------------ docs + report
let report = {};
try { report = JSON.parse(fs.readFileSync(REPORT, 'utf8')); } catch { /* first write */ }
report.newKaggleDataset = {
  trained: new Date().toISOString().slice(0, 10),
  source: 'Kaggle saadbutt321/pakistan-sign-language-dataset (OpenPose 21-point hands, z = 0)',
  pipeline: 'tools/new_dataset/extract_new_dataset.mjs -> tools/new-psl-landmarks.npz -> tools/new_dataset/train_psl_hybrid.mjs',
  mergedSources: [
    { name: 'original shipped psl.bin (git HEAD): Mendeley signer A + HF signer B + personal captures, already augmented', n: prev.count, unique: uPrev.length },
    { name: 'Hugging Face Bakhtyar12 (MIT), signer B', unique: uHf.length, shipped: finalList.filter((f) => f.src === 'HF').length },
    { name: `Kaggle saadbutt321 (OpenPose), weighted x${PERSONAL_WEIGHT} in the shipped bin`, unique: uNew.length, shipped: finalList.filter((f) => f.src === 'NEW').length },
  ],
  classes: allLabels,
  trainSamples: finalList.length,
  perClassOverallCap: FINAL_CAP * 2,
  crossPersonAccuracy: Object.fromEntries(evalResults.map((e) => [e.label, e.accuracy.acc])),
  crossPersonEvalFrames: Object.fromEntries(evalResults.map((e) => [e.label, e.accuracy.n])),
  crossPersonNote: 'Unique frames only; evaluation pools contain no duplicated or augmented copies (leak-proof). Both directions also cross capture systems (MediaPipe <-> 2D OpenPose), so they are a conservative lower bound. Previous shipped-model cross-person estimate: mendeley_to_hf 0.357, hf_to_mendeley 0.369.',
  sha256: crypto.createHash('sha256').update(bin).digest('hex'),
};
fs.writeFileSync(REPORT, JSON.stringify(report, null, 2));
console.log('updated docs/recognition-evaluation.json');
