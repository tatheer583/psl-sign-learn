#!/usr/bin/env node
/**
 * Harvest the official "Pakistan Sign Language" dictionary catalog from
 * https://psl.org.pk/dictionary (FESF / Deaf Reach) into a small bundled
 * search index for Irssa's web build.
 *
 * What goes into the index — METADATA ONLY, no media is re-hosted:
 *   - word terms + Urdu labels + page paths + category names
 *   - concept-image URLs (thumbnails served straight from the official CDN)
 *   - the official sign-video URL ONLY for words Irssa's lessons reference,
 *     so a lesson can stream the official video with full attribution.
 *
 * Every record points back to psl.org.pk. The crawler identifies itself and
 * keeps request pressure low (small concurrency + politeness delay).
 *
 * Usage:  node web/tools/build-psl-dictionary-index.mjs
 * Output: web/public/marks/psl-dictionary-index.json
 */
import { writeFile, readFile } from 'node:fs/promises';
import { statSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const WEB_DIR = path.resolve(HERE, '..');           // web/
const OUT = path.join(WEB_DIR, 'public', 'marks', 'psl-dictionary-index.json');
const CONTENT = path.join(WEB_DIR, 'src', 'content', 'content.json');

const BASE = 'https://psl.org.pk';
const CONCURRENCY = 5;
const DELAY_MS = 120;
const UA =
  'IrssaEducationalCrawler/1.0 (+https://github.com/tatheer583/psl-sign-learn; ' +
  'indexes public dictionary metadata for a free kids app with attribution)';

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

async function fetchText(url, tries = 3) {
  for (let attempt = 1; attempt <= tries; attempt++) {
    try {
      const res = await fetch(url, { headers: { 'user-agent': UA, 'accept-language': 'en' } });
      if (res.ok) return await res.text();
      if (res.status === 404 || res.status === 410) return null;
      console.warn(`  HTTP ${res.status} on ${url} (attempt ${attempt})`);
    } catch (err) {
      console.warn(`  fetch error on ${url}: ${err.message} (attempt ${attempt})`);
    }
    await sleep(500 * attempt);
  }
  return null;
}

/* ---------- helpers ---------- */

const URDU_RE = /[\u0600-\u06FF]{2,}/g;

function humanizeWord(slugPart) {
  return slugPart
    .split('-')
    .filter(Boolean)
    .map((w) => (w.length <= 2 ? w.toUpperCase() : w[0].toUpperCase() + w.slice(1)))
    .join(' ');
}
function humanizeCategory(slug) {
  const name = slug.split('-').slice(1).join('-');   // "24-classroom" → "classroom"
  return name
    .split('-')
    .filter(Boolean)
    .map((w) => w[0].toUpperCase() + w.slice(1))
    .join(' ');
}
function norm(s) {
  return s.toLowerCase().replace(/[^a-z0-9]/g, '');
}

/** Decode a Next.js /_next/image?url=ENCODED&… capture back to the real CDN URL. */
function decodeNextImage(capture) {
  const raw = capture.replace(/&amp;/g, '&');
  return new URLSearchParams(raw).get('url') ?? raw;
}

/** Parse one category page's HTML into word cards. */
function parseCategory(html) {
  // href=/dictionary/24-classroom/1273-book → [full path, cat#, cat-slug, word#, word-slug]
  const linkRe = /href="(\/dictionary\/(\d+)-([a-z0-9-]+)\/(\d+)-([a-z0-9-]+))"/g;
  const links = [...html.matchAll(linkRe)];
  const cards = [];
  for (let i = 0; i < links.length; i++) {
    const start = links[i].index;
    const end = i + 1 < links.length ? links[i + 1].index : Math.min(start + 4000, html.length);
    const region = html.slice(start, end);
    let image = null;
    for (const m of region.matchAll(/_next\/image\?url=("?)([^"\\]+)/g)) {
      const real = decodeNextImage(m[2]);
      if (real.includes('/concept-image/')) { image = real; break; }
    }
    cards.push({
      path: links[i][1],
      categorySlug: `${links[i][2]}-${links[i][3]}`,
      // word slug has no leading id in group 5 — "book" → "Book"
      term: humanizeWord(links[i][5]),
      urdu: region.match(URDU_RE)?.[0] ?? null,
      image,
    });
  }
  return cards;
}

/* ---------- catalog ---------- */

const content = JSON.parse(await readFile(CONTENT, 'utf8'));

console.log('Fetching the dictionary category list…');
const dictHome = await fetchText(`${BASE}/dictionary`);
if (!dictHome) {
  console.error('Could not load https://psl.org.pk/dictionary — aborting.');
  process.exit(1);
}

const categories = new Map(); // href → {number}
for (const m of dictHome.matchAll(/href="(\/dictionary\/(\d+)-([a-z0-9-]+))"/g)) {
  categories.set(m[1], Number(m[2]));
}
console.log(`Found ${categories.size} categories.`);

const wordsByPath = new Map();
function processCategoryHtml(html) {
  for (const card of parseCategory(html)) {
    const prev = wordsByPath.get(card.path);
    if (!prev) {
      wordsByPath.set(card.path, card);
      card.lessonRefs = [];
    } else {
      if (!prev.urdu && card.urdu) prev.urdu = card.urdu;
      if (!prev.image && card.image) prev.image = card.image;
    }
  }
}

const hrefs = [...categories.keys()].sort(
  (a, b) => categories.get(a) - categories.get(b),
);
let done = 0;
async function categoryWorker() {
  while (hrefs.length) {
    const href = hrefs.shift();
    const html = await fetchText(BASE + href);
    if (href === hrefs[0] || done === 0) {
      const debugLinks = [...(html ?? '').matchAll(/href="\/dictionary\/\d+-[a-z0-9-]+\/\d+-[a-z0-9-]+"/g)].length;
      console.log(`  [debug] ${href}: html ${(html ?? '').length} B, raw word links ${debugLinks}`);
    }
    processCategoryHtml(html ?? '');
    // Discover pagination (?page=2, …) if present.
    const slugBase = href.replace(/^\//, '');
    for (const m of new Set((html ?? '').matchAll(new RegExp(`href="${slugBase}\\?page=(\\d+)"`, 'g')))) {
      const pageHtml = await fetchText(`${BASE}${href}?page=${m[1]}`);
      processCategoryHtml(pageHtml ?? '');
      await sleep(DELAY_MS);
    }
    done += 1;
    if (done % 10 === 0) {
      console.log(`  …${done} categories processed, ${wordsByPath.size} unique words so far`);
    }
    await sleep(DELAY_MS);
  }
}
await Promise.all(Array.from({ length: CONCURRENCY }, categoryWorker));
console.log(`Catalog: ${wordsByPath.size} unique words across ${categories.size} categories.`);

/* ---------- match Irssa's lessons to dictionary entries ---------- */

const byNormTerm = new Map(); // norm(term) → [paths, …]
for (const [p, card] of wordsByPath) {
  const key = norm(card.term);
  (byNormTerm.get(key) ?? byNormTerm.set(key, []).get(key)).push(p);
}

// Targets: every Urdu (PSL) letter lesson (glyph + English memory word) and
// every everyday-vocabulary lesson (display title). English ASL letters are
// intentionally excluded — FESF videos are Pakistan (PSL) references, so
// linking them onto ASL fingerspelling lessons would confuse learners.
const targets = [];
for (const [id, label] of Object.entries(content.wordLabels)) {
  if (!id.startsWith('letter_') || id.startsWith('letter_en_')) continue;
  const paren = label.match(/\(([^)]+)\)/)?.[1];
  targets.push({ lessonId: id, glyph: null, candidates: paren ? [paren] : [label] });
}
for (const it of content.alphabetItems) {
  const t = targets.find((x) => x.lessonId === it.id);
  if (t) t.glyph = it.title;                       // the Urdu glyph itself
}
for (const it of content.vocabularyItems) {
  targets.push({ lessonId: it.id, glyph: null, candidates: [it.title] });
}

// Different phrasing the dictionary may use for the same everyday word.
const EXTRA = {
  'thankyou': ['thanks', 'thank'],
  'hello': ['greetings', 'hi'],
  'water': ['drinking-water', 'drink-water'],
  'happy': ['happiness', 'glad'],
  'love': ['loving', 'to-love'],
  'friend': ['friendship', 'friends'],
  'school': ['institution-school'],
  'sweet': ['sweets', 'dessert'],
  'book': ['books'],
  'moon': ['the-moon', 'moon-noun'],
  'football': ['soccer', 'soccer-ball'],
};
const SPLIT =                       // memory word → dictionary synonym
  { Rain: 'Rainfall', Hail: 'Hailstone', Sweet: 'Dessert', Fruit: 'Fruits', Kite: 'Kites' };

function candidatePaths(target) {
  // 1) exact Urdu glyph (the official alphabet entry — best possible match)
  if (target.glyph) {
    for (const [p, card] of wordsByPath) {
      if (card.urdu && card.urdu.normalize('NFC') === target.glyph.normalize('NFC')) {
        return [p];
      }
    }
  }
  // 2) by term candidates
  for (const cand of target.candidates) {
    const keys = [norm(cand), norm(SPLIT[cand] ?? '')].filter(Boolean);
    for (const k of keys) {
      for (const key of [k, k + 's', k.endsWith('s') ? k.slice(0, -1) : k, ...(EXTRA[k] ?? []).map(norm)]) {
        const hit = byNormTerm.get(key);
        if (hit?.length) return hit;
      }
    }
  }
  return null;
}

const lessonRefs = new Map();
const unmatched = [];
const catNum = (p) => {
  const slug = wordsByPath.get(p)?.categorySlug ?? '';
  const n = Number(slug.split('-')[0]);
  return Number.isFinite(n) ? n : 1e9;
};
for (const t of targets) {
  let paths = candidatePaths(t);
  if (!paths) { unmatched.push(t); continue; }
  paths = paths.slice().sort((a, b) => catNum(a) - catNum(b));
  lessonRefs.set(t.lessonId, paths[0]);
  for (const p of paths) {
    const card = wordsByPath.get(p);
    if (card && !card.lessonRefs.includes(t.lessonId)) card.lessonRefs.push(t.lessonId);
  }
}
if (unmatched.length) {
  console.log('No dictionary match for:');
  for (const t of unmatched) console.log(`  ${t.lessonId} (${t.glyph ?? t.candidates.join(', ')})`);
}

/* ---------- official video for every referenced word page ---------- */

const videoPaths = [...new Set(lessonRefs.values())];
console.log(`Fetching ${videoPaths.length} word pages for official video URLs…`);

function extractVideo(html) {
  const vids = [...html.matchAll(/https:\/\/[a-z0-9.]+\.cloudfront\.net\/psl\/public\/videos\/[^"'\s]+\.mp4/g)]
    .map((m) => m[0]);
  return vids.find((v) => v.includes('720p')) ?? vids[0] ?? null;
}
function extractPoster(html) {
  const posters = [...html.matchAll(/https:\/\/[a-z0-9.]+\.cloudfront\.net\/psl\/public\/posters\/[^"'\s]+/g)]
    .map((m) => m[0]);
  return posters[0] ?? null;
}
function extractExactTerm(html) {
  const h1s = [...html.matchAll(/<h1[^>]*>([\s\S]{0,120}?)<\/h1>/g)]
    .map((m) => m[1].replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim())
    .filter((t) => t && t.toLowerCase() !== 'psl dictionary');
  return h1s.at(-1) ?? null;
}

let fdone = 0;
async function videoWorker() {
  while (videoPaths.length) {
    const p = videoPaths.shift();
    const html = await fetchText(BASE + p);
    const card = wordsByPath.get(p);
    if (card && html) {
      card.exactTerm = extractExactTerm(html) ?? card.term;
      card.video = extractVideo(html);
      card.poster = extractPoster(html) ?? card.image;
      if (!card.video) console.log(`  no video on ${p} (image-only entry)`);
    } else if (!html) {
      console.log(`  word page failed: ${p}`);
    }
    fdone += 1;
    if (fdone % 10 === 0) console.log(`  …${fdone} word pages processed`);
    await sleep(DELAY_MS);
  }
}
await Promise.all(Array.from({ length: CONCURRENCY }, videoWorker));

/* ---------- write the bundle ---------- */

const out = {
  generated: new Date().toISOString(),
  source: `${BASE}/dictionary`,
  provider: 'Family Educational Services Foundation (FESF), Deaf Reach — psl.org.pk',
  license:
    'All dictionary content © FESF. Irssa bundles none of it: the index holds metadata only; videos and images stream from the official site and every entry links back to psl.org.pk.',
  wordCount: wordsByPath.size,
  categories: [...categories.keys()]
    .sort((a, b) => categories.get(a) - categories.get(b))
    .map((href) => ({
      path: href,
      name: humanizeCategory(href.replace(/^\//, '')),
    })),
  lessons: Object.fromEntries(lessonRefs),
  words: [...wordsByPath.values()]
    .sort(
      (a, b) =>
        (Number(a.categorySlug.split('-')[0]) || 1e9) - (Number(b.categorySlug.split('-')[0]) || 1e9) ||
        a.path.localeCompare(b.path),
    )
    .map((w) => ({
      term: w.exactTerm ?? w.term,
      urdu: w.urdu ?? undefined,
      category: humanizeCategory(w.categorySlug),
      path: w.path,
      image: w.image ?? undefined,
      ...(w.lessonRefs.length ? { lessonRefs: w.lessonRefs } : {}),
      ...(w.video ? { video: w.video, poster: w.poster ?? w.image } : {}),
    })),
};

await writeFile(OUT, JSON.stringify(out, null, 1), 'utf8');
const kb = Math.round(statSync(OUT).size / 1024);
console.log(
  `✓ wrote public/marks/psl-dictionary-index.json — ` +
    `${out.wordCount} words, ${out.categories.length} categories, ` +
    `${Object.keys(out.lessons).length} lesson links (~${kb} KB)`,
);
