/**
 * Validate the freshly written psl.bin with the web app's own TS logic:
 *  - parse the binary exactly like web/src/lib/knn.ts
 *  - run true 5-NN distance-weighted predictions on a few frames from
 *   -toolbox datasets, verify sanity (finite, label ids, expected classes)
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { readNpz } from './lib/npy.mjs';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', '..');
const bin = fs.readFileSync(path.join(ROOT, 'web', 'public', 'marks', 'models', 'psl.bin'));
const appBin = fs.readFileSync(path.join(ROOT, 'app', 'src', 'main', 'assets', 'models', 'psl.bin'));
const same = Buffer.compare(bin, appBin) === 0;
console.log('web & app bins identical:', same);

// --- knn.ts parse (port) ---
const dv = new DataView(bin.buffer, bin.byteOffset, bin.byteLength);
let off = 0;
const magic = dv.getInt32(off, false); off += 4;
const count = dv.getInt32(off, false); off += 4;
const dims = dv.getInt32(off, false); off += 4;
const classes = dv.getInt32(off, false); off += 4;
const dec = new TextDecoder('utf-8');
const labels = [];
for (let c = 0; c < classes; c++) {
  const len = dv.getUint16(off, false); off += 2;
  labels.push(dec.decode(bin.subarray(off, off + len))); off += len;
}
const targets = new Int32Array(count);
const features = new Float32Array(count * dims);
for (let i = 0; i < count; i++) {
  targets[i] = dv.getInt32(off, false); off += 4;
  for (let j = 0; j < dims; j++) { features[i * dims + j] = dv.getFloat32(off, false); off += 4; }
}
console.log('magic ok:', magic === 0x49525353, '| samples:', count, '| dims:', dims, '| classes:', classes, '| consumed == size:', off === bin.length);
console.log('labels:', labels.join(','));

// sanity: every target index valid; all features finite
let bad = 0;
for (let i = 0; i < count; i++) {
  if (targets[i] < 0 || targets[i] >= classes) bad++;
  for (let j = 0; j < dims; j++) if (!Number.isFinite(features[i * dims + j])) { bad++; break; }
}
console.log('invalid targets/features:', bad);

// --- predict like the app: 5-NN, distance-weighted, strict gate ---
function classify(q) {
  const n = [];
  for (let i = 0; i < count; i++) {
    let d = 0;
    for (let j = 0; j < dims; j++) { const dx = features[i * dims + j] - q[j]; d += dx * dx; }
    if (n.length < 5) { n.push([d, targets[i]]); n.sort((a, b) => a[0] - b[0]); }
    else if (d < n[4][0]) { n[4] = [d, targets[i]]; n.sort((a, b) => a[0] - b[0]); }
  }
  const votes = new Map();
  for (const [d, t] of n) votes.set(t, (votes.get(t) ?? 0) + 1 / (Math.sqrt(d) + 1e-5));
  const winner = [...votes.entries()].sort((a, b) => b[1] - a[1])[0];
  const agreement = winner[1] / 5; // of 5 neighbors
  const rms = Math.sqrt(n[0][0] / dims);
  return { label: labels[winner[0]], agreement, rms, accepted: agreement >= 0.95 && rms <= 0.07 };
}

const npz = await readNpz(path.join(ROOT, 'tools', 'new-psl-landmarks.npz'));
let hit = 0, accepted = 0, total = 0;
const trials = 400;
const idxs = new Set();
while (idxs.size < trials) idxs.add(Math.floor(Math.random() * npz.y.data.length));
for (const i of idxs) {
  const v = npz.X.data.slice(i * 63, i * 63 + 63);
  const p = classify(v);
  total++;
  if (p.label === npz.y.data[i]) hit++;
  if (p.accepted) accepted++;
}
console.log(`\nopenpose frames fed through web classifier: ${hit}/${total} top-1 correct, ${accepted} passed the strict gate (same-frame duplicates are in-bin, so top-1 is a parser/sanity check, not an accuracy claim)`);
