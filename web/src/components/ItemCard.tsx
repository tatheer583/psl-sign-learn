import React from 'react';
import type { LearningItem } from '../content/types';
import { cssColor } from '../lib/util';
import { useStore } from '../store/store';

/** Learning-grid card, same colorful identity as the Android grid. */
export function ItemCard({
  item,
  onClick,
  mediaSrc,
  showUrduTitle = false,
}: {
  item: LearningItem;
  onClick: () => void;
  mediaSrc?: string;
  showUrduTitle?: boolean;
}) {
  const { learner } = useStore();
  const mastered = learner.mastered.has(item.id);
  const seen = learner.seen.has(item.id);
  const favorite = learner.favorites.has(item.id);
  const urdu = item.id.startsWith('letter_') && !item.id.startsWith('letter_en_');
  return (
    <button
      className="item-card"
      onClick={onClick}
      style={{ background: cssColor(0xFFFFFFFF) }}
      aria-label={`Learn ${item.subtitle}`}
    >
      {favorite && <span className="card-fav">💖</span>}
      {mastered && <span className="card-mastered" title="Mastered on camera">✓</span>}
      <div
        className="card-media"
        style={{
          background: `linear-gradient(160deg, ${cssColor(item.colorHex, 0.16)}, ${cssColor(item.colorHex, 0.05)})`,
        }}
      >
        {mediaSrc ? (
          <img src={mediaSrc} alt="" loading="lazy" onError={({ currentTarget }) => {
            currentTarget.style.display = 'none';
            const sib = currentTarget.nextElementSibling as HTMLElement | null;
            if (sib) sib.style.display = 'flex';
          }} />
        ) : null}
        <span
          className="card-fallback"
          style={{ display: mediaSrc ? 'none' : 'flex', fontSize: urdu ? undefined : '1.7em' }}
        >
          {item.objectEmoji}
        </span>
      </div>
      <div className="card-title urdu" style={{ color: cssColor(item.colorHex) }}>
        {showUrduTitle || urdu ? item.title : item.title}
      </div>
      <div className="card-subtitle">{seen ? 'Seen' : 'New'}</div>
    </button>
  );
}

/** Small target-scrolling grid card used by Alphabet & Words browser. */
export function WordRow({
  item,
  word,
  onClick,
  mediaSrc,
}: {
  item: LearningItem;
  word: string;
  onClick: () => void;
  mediaSrc?: string;
}) {
  return (
    <button className="word-row" onClick={onClick}>
      <span className="word-rank urdu">{item.title}</span>
      {mediaSrc
        ? <img className="word-media" src={mediaSrc} alt="" loading="lazy" />
        : <span className="word-media fallback">{item.objectEmoji}</span>}
      <span className="word-name">{word}</span>
      <span className="word-arrow">→</span>
    </button>
  );
}
