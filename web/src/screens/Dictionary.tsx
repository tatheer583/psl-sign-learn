import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { assetUrl } from '../lib/util';
import {
  loadPslDictionary, searchEntries, dictPageUrl, dictSearchUrl,
  type PslDictEntry, type PslDictIndex,
} from '../lib/pslDictionary';
import { loadLectures, type Lecture } from '../lib/lectures';
import { speak } from '../lib/tts';
import { PageHeader } from '../components/Pieces';

const PAGE_SIZE = 48;

/**
 * Official PSL Dictionary (FESF / Deaf Reach) search — the index bundles
 * metadata only; videos and images stream from the official psl.org.pk CDN.
 * Teacher lecture videos, when provided, play locally from marks/lectures/.
 */
export function DictionaryScreen() {
  const nav = useNavigate();
  const [index, setIndex] = useState<PslDictIndex | null>(null);
  const [lectures, setLectures] = useState<Lecture[]>([]);
  const [q, setQ] = useState('');
  const [picked, setPicked] = useState<PslDictEntry | null>(null);
  const [limit, setLimit] = useState(PAGE_SIZE);

  useEffect(() => {
    let dead = false;
    loadPslDictionary().then((i) => { if (!dead) setIndex(i); });
    loadLectures().then((l) => { if (!dead) setLectures(l); });
    return () => { dead = true; };
  }, []);

  const defaultWords = useMemo(
    () => (index ? index.words.filter((w) => w.lessonRefs?.length) : []),
    [index],
  );
  const results = useMemo(
    () => (q.trim() && index ? searchEntries(index, q, 360) : defaultWords),
    [index, q, defaultWords],
  );
  const visible = results.slice(0, limit);

  return (
    <div className="page">
      <PageHeader
        title="Official Dictionary 🌐"
        subtitle="6,000+ real PSL signs — from psl.org.pk"
        onBack={() => nav(-1)}
      />

      <p className="dict-note">
        Made by <b>FESF</b> / the Deaf Reach program. Videos stream live from the
        official dictionary — Irssa bundles none of them and links every card back.
      </p>

      <input
        className="text-input big"
        placeholder="Search a word… e.g. water, کتاب, school"
        value={q}
        onChange={(e) => { setQ(e.target.value); setLimit(PAGE_SIZE); }}
        aria-label="Search the official PSL dictionary"
      />

      {picked && (
        <div className="dict-player">
          {picked.video ? (
            <video
              className="dict-video"
              src={picked.video}
              poster={picked.poster ?? picked.image}
              controls
              preload="none"
              playsInline
            />
          ) : (
            <div className="dict-novideo">🎬 The official page has the full entry.</div>
          )}
          <div className="dict-attr">
            <div className="dict-attr-head">
              <b>{picked.term}</b>
              {picked.urdu && <span className="urdu dict-attr-urdu"> · {picked.urdu}</span>}
              <button
                className="speaker-mini"
                onClick={() => speak(picked.term)}
                aria-label={`Hear ${picked.term}`}
              >🔊</button>
            </div>
            Official “{picked.category}” sign — © FESF Deaf Reach ·{' '}
            <a href={dictPageUrl(picked.path)} target="_blank" rel="noreferrer noopener">
              open on psl.org.pk ↗
            </a>
          </div>
        </div>
      )}

      {q.trim() && results.length > 0 && (
        <div className="dict-count">{results.length} official match{results.length === 1 ? '' : 'es'}</div>
      )}

      {lectures.length > 0 && (
        <div className="dict-lectures">
          <h3>Teacher lectures 📚</h3>
          {lectures.map((lec) => (
            <div key={lec.file} className="dict-lecture-card">
              <h4>{lec.title}</h4>
              {lec.description && <p>{lec.description}</p>}
              <video
                className="dict-video"
                src={assetUrl(`marks/lectures/${lec.file}`)}
                controls
                preload="none"
                playsInline
              />
              {lec.author && (
                <div className="dict-attr">
                  by {lec.author}{lec.license ? ` · ${lec.license}` : ''}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      <div className="dict-grid">
        {visible.map((w) => (
          <button key={w.path} className="dict-card" onClick={() => setPicked(w)}>
            {w.image ? (
              <img src={w.image} alt={w.term} loading="lazy" />
            ) : (
              <span className="dict-fallback" aria-hidden>🎬</span>
            )}
            <span className="dict-term">{w.term}</span>
            {w.urdu && <span className="dict-urdu urdu">{w.urdu}</span>}
            <span className="dict-cat">{w.category}</span>
          </button>
        ))}
      </div>

      {results.length > visible.length && (
        <button className="btn secondary dict-more" onClick={() => setLimit(limit + PAGE_SIZE)}>
          Show more signs ({results.length - visible.length} left)
        </button>
      )}

      {q.trim() && results.length === 0 && (
        <p className="empty-note">
          Not in the bundled index. Search directly in the official dictionary:{' '}
          <a href={dictSearchUrl(q)} target="_blank" rel="noreferrer noopener">
            search psl.org.pk for “{q.trim()}” ↗
          </a>
        </p>
      )}

      {!q.trim() && index && (
        <div className="dict-count">
          {defaultWords.length} signs used in Irssa’s lessons ·{' '}
          <a href={dictSearchUrl('')} target="_blank" rel="noreferrer noopener">
            browse all on psl.org.pk ↗
          </a>
        </div>
      )}

      <p className="honest-note">
        All dictionary words and videos belong to FESF (Deaf Reach). They stream
        from their official site for guided practice — nothing is copied or
        re-hosted, and grading in Irssa never depends on them.
      </p>
    </div>
  );
}
