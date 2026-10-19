import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Confetti } from '../components/Confetti';
import { PageHeader, StarChip } from '../components/Pieces';
import { content } from '../content/types';
import { useStore } from '../store/store';

/** Port of StickersScreen — badge staircase unlocked by stars. */
export function StickersScreen() {
  const nav = useNavigate();
  const { learner } = useStore();
  const [burst, setBurst] = useState(0);
  const stars = learner.stars;

  const badges = useMemo(() =>
    content.badges.map((b) => ({ ...b, unlocked: stars >= b.requiredStars })),
  [stars]);

  // celebrate transitions
  React.useEffect(() => {
    setBurst((b) => b + 1);
  }, [badges.filter((b) => b.unlocked).length]);

  const unlockedCount = badges.filter((b) => b.unlocked).length;

  return (
    <div className="page">
      <Confetti burst={burst > 1 ? 1 : 0} reducedMotion={learner.reducedMotion} />
      <PageHeader
        title="Stickers & Badges 🏆"
        subtitle={`${unlockedCount} of ${badges.length} collected`}
        onBack={() => nav('/')}
        right={<StarChip stars={stars} />}
      />
      <p className="empty-note center">
        Learn, play and practise — every star brings the next sticker closer! 🐛→🦋
      </p>
      <div className="badge-grid">
        {badges.map((b) => (
          <div key={b.id} className={`badge-card ${b.unlocked ? 'unlocked' : 'locked'}`}>
            <div className="badge-emoji">{b.unlocked ? b.iconEmoji : '🔒'}</div>
            <div className="badge-title">{b.title}</div>
            <div className="badge-desc">{b.description}</div>
            <div className="badge-progress">
              <div
                className="badge-bar"
                style={{ width: `${Math.min(100, (stars / b.requiredStars) * 100)}%` }}
              />
            </div>
            <div className="badge-count">⭐ {Math.min(stars, b.requiredStars)} / {b.requiredStars}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
