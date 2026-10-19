/**
 * Minimal NPY reader/writer (numpy array file format v1.0) used to produce
 * .npz archives that remain loadable by the original Python pipeline
 * (np.load / tools/train_hybrid.py).
 */
import JSZip from 'jszip';
import fs from 'node:fs';

const MAGIC = [0x93, 0x4e, 0x55, 0x4d, 0x50, 0x59]; // \x93NUMPY

/** Serialize a Float32Array into a little-endian '<f4' .npy buffer. */
export function npyFloat32(name, data, shape) {
  const elems = shape.reduce((a, b) => a * b, 1);
  if (data.length !== elems) throw new Error(`size mismatch for ${name}`);
  const body = Buffer.alloc(elems * 4);
  const dv = new DataView(body.buffer);
  for (let i = 0; i < elems; i++) dv.setFloat32(i * 4, data[i], true);
  const shapeStr = shape.length === 1 ? `(${shape[0]},)` : `(${shape.join(', ')})`;
  const dict = `{'descr': '<f4', 'fortran_order': False, 'shape': ${shapeStr}, }`;
  return writeRawNpy(body, shape, dict);
}

/** Serialize a string array into '<U{maxlen}' .npy buffer (UTF-32 chars). */
export function npyStr(name, values) {
  const width = Math.max(4, ...values.map((v) => v.length));
  const body = Buffer.alloc(values.length * width * 4);
  for (let i = 0; i < values.length; i++) {
    for (let c = 0; c < values[i].length; c++) {
      body.writeUInt32LE(values[i].charCodeAt(c) & 0xffff, (i * width + c) * 4);
    }
  }
  const dict = `{'descr': '<U${width}', 'fortran_order': False, 'shape': (${values.length},), }`;
  return writeRawNpy(body, [values.length], dict);
}

function writeNpy(name, body, shape, descr) {
  const shapeStr = shape.length === 1 ? `(${shape[0]},)` : `(${shape.join(', ')})`;
  const dict = `{'descr': '${descr}', 'fortran_order': False, 'shape': ${shapeStr}, }`;
  return writeRawNpy(body, shape, dict);
}
void writeNpy;

function writeRawNpy(body, shape, dict) {
  const header = Buffer.from(dict, 'ascii');
  const target = 64; // numpy aligns the full header to a 64-byte multiple
  const pad = ((target - ((6 + 2 + header.length + 1) % target)) % target);
  const headLen = header.length + pad + 1; // padded spaces + trailing newline
  const total = Buffer.alloc(10 + headLen); // 6 magic + 2 version + header(+pad+\n)
  Buffer.from(MAGIC).copy(total, 0);
  total.writeUInt8(1, 6);   // version major
  total.writeUInt8(0, 7);   // version minor
  total.writeUInt16LE(headLen, 8);
  header.copy(total, 10);
  Buffer.alloc(pad, 0x20).copy(total, 10 + header.length);
  total[10 + header.length + pad] = 0x0a; // '\n'
  return Buffer.concat([total, body]);
}

/** Read one .npy buffer -> {descr, shape, data: typed array | string[]} */
export function readNpy(buf) {
  if (buf[0] !== 0x93 || buf[1] !== 0x4e) throw new Error('not an npy file');
  const major = buf[6];
  let headerLen;
  if (major === 1) headerLen = buf.readUInt16LE(8);
  else if (major === 2) headerLen = buf.readUInt32LE(8);
  else throw new Error('unsupported npy version ' + major);
  const header = buf.slice(10, 10 + headerLen).toString('ascii');
  const descr = /'descr'\s*:\s*'([^']+)'/.exec(header)?.[1];
  const shapeMatch = /'shape'\s*:\s*\(([^)]*)\)/.exec(header);
  const shape = shapeMatch[1].split(',').map((s) => parseInt(s.trim(), 10)).filter((n) => Number.isFinite(n));
  const fortran = /'fortran_order':\s*(True|False)/.exec(header)?.[1] === 'True';
  const offset = 10 + headerLen;
  const body = buf.slice(offset);

  if (descr === '<f4' || descr === 'float32') {
    const n = shape.reduce((a, b) => a * b, 1);
    const out = new Float32Array(n);
    for (let i = 0; i < n; i++) out[i] = body.readFloatLE(i * 4);
    return { descr, shape, fortran, data: out };
  }
  if (descr.startsWith('<U')) {
    const width = parseInt(descr.slice(2), 10);
    const n = shape.reduce((a, b) => a * b, 1);
    const out = [];
    for (let i = 0; i < n; i++) {
      let s = '';
      for (let c = 0; c < width; c++) {
        const code = body.readInt32LE((i * width + c) * 4);
        if (code === 0) continue;
        s += String.fromCharCode(code);
      }
      out.push(s.replace(/\x00+$/, '').trim());
    }
    return { descr, shape, fortran, data: out };
  }
  if (descr === '|u1' || descr === '|i1') {
    const n = shape.reduce((a, b) => a * b, 1);
    const out = new Int32Array(n);
    for (let i = 0; i < n; i++) out[i] = body[descr === '|u1' ? 'readUInt8' : 'readInt8'](i);
    return { descr, shape, fortran, data: out };
  }
  if (descr === '<i4' || descr === '<i8') {
    const bytes = descr === '<i4' ? 4 : 8;
    const n = shape.reduce((a, b) => a * b, 1);
    const out = new BigInt64Array(n);
    for (let i = 0; i < n; i++) out[i] = body.readBigInt64LE(i * bytes);
    return { descr, shape, fortran, data: out };
  }
  throw new Error('unsupported npy descr: ' + descr);
}

/** Write a dict of arrays (Float32Array / number[] / string[]) to a .npz FILE. */
export async function writeNpz(outPath, entries) {
  const zip = new JSZip();
  for (const [name, array] of Object.entries(entries)) {
    if (Array.isArray(array) && typeof array[0] === 'string') {
      zip.file(`${name}.npy`, npyStr(name, array));
    } else {
      const shape = array.npzShape ?? [array.length];
      zip.file(`${name}.npy`, npyFloat32(name, array, shape));
    }
  }
  const buf = await zip.generateAsync({ type: 'nodebuffer', compression: 'DEFLATE' });
  fs.writeFileSync(outPath, buf);
  return buf.length;
}

/** Read a .npz file -> { name: readNpy result } */
export async function readNpz(pathOrBuf) {
  const buf = Buffer.isBuffer(pathOrBuf) ? pathOrBuf : await import('node:fs').then((fs) => fs.readFileSync(pathOrBuf));
  const zip = await JSZip.loadAsync(buf);
  const out = {};
  for (const name of Object.keys(zip.files)) {
    if (!name.endsWith('.npy')) continue;
    const content = await zip.files[name].async('nodebuffer');
    out[name.replace(/\.npy$/, '')] = readNpy(content);
  }
  return out;
}
