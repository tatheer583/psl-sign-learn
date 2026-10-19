/**
 * Exact TypeScript port of LandmarkClassifier in Recognition.kt —
 * parses the SAME psl.bin / english.bin binary (big-endian, Java
 * DataInputStream format) and runs distance-weighted 5-NN with the same
 * deliberately strict acceptance gate: agreement >= 0.95 && rms <= 0.07.
 *
 * Binary layout (written by tools/train_hybrid.py):
 *   int   magic 0x49525353 ("IRSS")
 *   int   sample count
 *   int   dims (63)
 *   int   classes
 *   classes × u16 length + UTF-8 bytes   (Java modified UTF-8; labels are ASCII)
 *   per sample: int classIndex + `dims` big-endian floats
 */
import { Landmark, normalizeLandmarks } from './features';

export interface Prediction {
  label: string;
  agreement: number;
  distance: number;
  accepted: boolean;
}

export class LandmarkClassifier {
  readonly labels: string[];
  readonly dims: number;
  private features: Float32Array[] = [];
  private targets: Int32Array;

  constructor(bytes: ArrayBuffer) {
    const view = new DataView(bytes);
    let off = 0;
    const int = () => { const v = view.getInt32(off, false); off += 4; return v; };
    // unsigned u16 length prefix (Java readUTF); labels are ASCII so byte-length == chars
    const utf = () => {
      const len = view.getUint16(off, false); off += 2;
      const b = new Uint8Array(bytes, off, len); off += len;
      return new TextDecoder('utf-8').decode(b);
    };
    if (int() !== 0x49525353) throw new Error('Not an Irssa classifier binary');
    const count = int();
    if (count < 1 || count > 50000) throw new Error('Bad classifier sample count');
    const dims = int();
    if (dims !== 63) throw new Error('Unexpected dims: ' + dims);
    const classes = int();
    if (classes < 1 || classes > 64) throw new Error('Bad classifier classes');
    this.dims = dims;
    this.labels = Array.from({ length: classes }, () => utf());
    this.targets = new Int32Array(count);
    for (let i = 0; i < count; i++) {
      const t = int();
      if (t < 0 || t >= classes) throw new Error('Bad class index in classifier');
      this.targets[i] = t;
      this.features.push(new Float32Array(bytes.slice(off, off + dims * 4)));
      off += dims * 4;
    }
  }

  get sampleCount(): number {
    return this.features.length;
  }

  /** distance-weighted 5-NN with explicit distance and ambiguity rejection */
  predict(points: Landmark[]): Prediction | null {
    const query = normalizeLandmarks(points);
    if (!query) return null;
    const FIVE = 5;
    const distances: number[] = [Infinity, Infinity, Infinity, Infinity, Infinity];
    const neighbors: number[] = [0, 0, 0, 0, 0];
    for (let i = 0; i < this.features.length; i++) {
      const f = this.features[i];
      let d = 0;
      for (let j = 0; j < this.dims; j++) {
        const delta = query[j] - f[j];
        d += delta * delta;
      }
      if (d < distances[FIVE - 1]) {
        let k = FIVE - 1;
        while (k > 0 && d < distances[k - 1]) {
          distances[k] = distances[k - 1];
          neighbors[k] = neighbors[k - 1];
          k--;
        }
        distances[k] = d;
        neighbors[k] = this.targets[i];
      }
    }
    const votes = new Float64Array(this.labels.length);
    for (let k = 0; k < FIVE; k++) {
      if (Number.isFinite(distances[k])) {
        votes[neighbors[k]] += 1 / (Math.sqrt(distances[k]) + 0.00001);
      }
    }
    let winner = 0;
    let total = 0;
    for (let c = 0; c < votes.length; c++) {
      total += votes[c];
      if (votes[c] > votes[winner]) winner = c;
    }
    if (total === 0) return null;
    const agreement = votes[winner] / total;
    const rms = Math.sqrt(distances[0] / this.dims);
    // The gate is deliberately strict — same notes as the Kotlin original:
    // agreement is not a calibrated probability and is never displayed as accuracy.
    const accepted = agreement >= 0.95 && rms <= 0.07;
    return { label: this.labels[winner], agreement, distance: rms, accepted };
  }
}
