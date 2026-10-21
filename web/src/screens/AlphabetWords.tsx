import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { allItems } from '../content/types';
import { wordLabel } from '../content/types';
import { loadLessons } from '../lib/lessons';
import { PageHeader, StarChip } from '../components/Pieces';
import { useStore } from '../store/store';

/** Port of AlphabetWordsScreen — letters matched with their everyday words. */
export function AlphabetWordsScreen() {
  const nav = useNavigate();
  const [params] = useSearchParams();
  const { learner } = useStore();
  const [lang, setLang] = useState<'urdu' | 'english'>(params.get('lang') === 'english' ? 'english' : 'urdu');
  const [media, setMedia] = useState<Record<string, string>>({});

  useEffect(() => {
    let dead = false;
    loadLessons().then((ls) => {
      if (dead) return;
      setMedia(Object.fromEntries(ls.filter((l) => l.media && !l.isVideo).map((l) => [l.id, `./marks/${l.media}`])));
    }).catch(() => undefined);
    return () => { dead = true; };
  }, []);

  const letters = allItems.filter((i) =>
    lang === 'urdu' ? i.id.startsWith('letter_') && !i.id.startsWith('letter_en_') : i.id.startsWith('letter_en_'),
  );

  return (
    <div className="page">
      <PageHeader
        title="Alphabet & Words 📖"
        subtitle={lang === 'urdu' ? 'Every letter meets its word — انار, بکری…' : 'Every letter meets its word — Apple, Bear…'}
        onBack={() => nav('/')}
        right={<StarChip stars={learner.stars} />}
      />
      <div className="cat-tabs">
        <button className={`cat-tab ${lang === 'urdu' ? 'active' : ''}`} onClick={() => setLang('urdu')}>🇵🇰 اردو</button>
        <button className={`cat-tab ${lang === 'english' ? 'active' : ''}`} onClick={() => setLang('english')}>🔤 English</button>
      </div>
      <div className="word-list">
        {letters.map((it) => (
          <button key={it.id} className="word-row" onClick={() => nav(`/learn/${it.id}`)}>
            <span className="word-rank urdu">{it.title}</span>
            {media[it.id]
              ? <img className="word-media" src={media[it.id]} alt="" loading="lazy" />
              : <span className="word-media fallback">{it.objectEmoji}</span>}
            <span className="word-name">{wordLabel(it.id)}</span>
            <span className="word-arrow">→</span>
          </button>
        ))}
      </div>
    </div>
  );
}
