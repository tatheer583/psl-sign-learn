/**
 * Restore a Windows-CP437-mojibake name (created by a zip extraction without
 * UTF-8 flags — common on Kaggle datasets) back to its true UTF-8 form.
 * E.g. bytes DA AF E2 80 AC ('گ' + U+202C) arrive as the glyphs "┌»ΓÇ¼".
 * iconv-lite ships a complete, correct CP437 table.
 */
import iconv from 'iconv-lite';
import fs from 'node:fs';

const CACHE_PATH = null; // no persistence needed

export function mojibakeToUtf8(name) {
  if (![...name].some((c) => c.codePointAt(0) >= 0x80)) return name; // pure ASCII — untouched
  try {
    // each mojibake char may carry multiple original bytes; decode via CP437
    const bytes = iconv.encode(name, 'cp437');
    const decoded = iconv.decode(bytes, 'utf8').replace(/\ufffd/g, '');
    if (!decoded) return name;
    return decoded;
  } catch {
    return name;
  }
}

// quick self-test when run directly
if (import.meta.url === `file://${process.argv[1]}`.replace(/\\/g, '/')) {
  const dir = 'C:/Users/1/Downloads/sign-language-database/PSL_dataset/datasets/alphabets_dataset';
  for (const n of fs.readdirSync(dir)) {
    const fixed = mojibakeToUtf8(n);
    console.log(JSON.stringify(n), '->', JSON.stringify(fixed), [...fixed].map((c) => c.codePointAt(0).toString(16)).join(' '));
  }
}
