import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { allItems } from '../content/types';
import type { LearningItem } from '../content/types';
import { PageHeader, StarChip } from '../components/Pieces';
import { ItemCard } from '../components/ItemCard';
import { loadLessons } from '../lib/lessons';
import { useStore } from '../store/store';

export type CatKey = 'urdu' | 'english' | 'numbers' | 'words' | 'favorites';

const CAT_LABELS: Record<CatKey, string> = {
  urdu: '🇵🇰 اردو', english: '🔤 English', numbers: '🔢 Numbers', words: '💬 Words', favorites: '💖 Favorites',
};

function itemsForCat(cat: CatKey, favorites: Set<string>): LearningItem[] {
  switch (cat) {
    case 'urdu': return allItems.filter((i) => i.id.startsWith('letter_') && !i.id.startsWith('letter_en_'));
    case 'english': return allItems.filter((i) => i.id.startsWith('letter_en_'));
    case 'numbers': return allItems.filter((i) => i.id.startsWith('num_'));
    case 'words': return allItems.filter((i) => i.id.startsWith('vocab_'));
    case 'favorites': return allItems.filter((i) => favorites.has(i.id));
  }
}

/** Port of LearnScreen — colorful grids per category, real photos on cards. */
export function LearnScreen() {
  const nav = useNavigate();
  const [params] = useSearchParams();
  const { learner } = useStore();
  const cat = (params.get('cat') as CatKey) || 'urdu';
  const items = itemsForCat(cat, learner.favorites);
  const [mediaById, setMediaById] = useState<Record<string, string>>({});

  useEffect(() => {
    let dead = false;
    // Hydrate card photos from the bundled media manifest.
    loadLessons().then((lessons) => {
      if (dead) return;
      setMediaById(Object.fromEntries(
        lessons.filter((l) => l.media && !l.isVideo).map((l) => [l.id, `./marks/${l.media}`]),
      ));
    }).catch(() => undefined);
    return () => { dead = true; };
  }, []);

  return (
    <div className="page">
      <PageHeader
        title="Let's Learn ✨"
        subtitle="Tap a card to open its lesson"
        onBack={() => nav('/')}
        right={<StarChip stars={learner.stars} />}
      />
      <div className="cat-tabs">
        {(Object.keys(CAT_LABELS) as CatKey[]).map((k) => (
          <button
            key={k}
            className={`cat-tab ${cat === k ? 'active' : ''}`}
            onClick={() => nav(`/learn?cat=${k}`)}
          >
            {CAT_LABELS[k]}
          </button>
        ))}
      </div>
      {items.length === 0 ? (
        <p className="empty-note">Nothing here yet — open a lesson and tap 💖 to save favorites!</p>
      ) : (
        <div className="item-grid">
          {items.map((it) => (
            <ItemCard
              key={it.id}
              item={it}
              mediaSrc={mediaById[it.id]}
              onClick={() => nav(`/learn/${it.id}`)}
            />
          ))}
        </div>
      )}
    </div>
  );
}
