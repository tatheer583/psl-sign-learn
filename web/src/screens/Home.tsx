import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useStore, todayCount, streak } from '../store/store';
import { PageHeader, StarChip } from '../components/Pieces';

interface HomeCard {
  to: string; title: string; sub: string; emoji: string; color: number;
}

/** Port of HomeScreen — the playful launcher grid. */
export function HomeScreen() {
  const nav = useNavigate();
  const { learner } = useStore();
  const sc = todayCount(learner);
  const pct = Math.min(100, Math.round((sc / learner.dailyGoal) * 100));
  const days = streak(learner);

  const P = 0xFF5C52E5, T = 0xFF00B894, O = 0xFFFF7849, Y = 0xFFFFC312, K = 0xFFFF5E7E, C = 0xFF00B4D8;
  const cards: HomeCard[] = [
    { to: '/learn?cat=urdu', title: 'Urdu Letters', sub: 'اردو — 37 PSL letters', emoji: '🇵🇰', color: P },
    { to: '/learn?cat=english', title: 'English Letters', sub: 'ASL — 26 letters', emoji: '🔤', color: O },
    { to: '/learn?cat=numbers', title: 'Numbers 0–50', sub: 'Count with signs', emoji: '🔢', color: T },
    { to: '/learn?cat=words', title: 'Everyday Words', sub: 'Hello, water, school…', emoji: '💬', color: K },
    { to: '/practice', title: 'Camera Studio', sub: 'Real-time sign practice', emoji: '📸', color: C },
    { to: '/camera-quiz', title: 'Camera Quiz', sub: 'Match the shown sign', emoji: '🎯', color: P },
    { to: '/quiz', title: 'Sign Quiz', sub: 'Pick the matching sign!', emoji: '🧩', color: Y },
    { to: '/speed', title: 'Speed Match', sub: '45 seconds challenge', emoji: '⚡', color: O },
    { to: '/alphabet-words', title: 'Alphabet & Words', sub: 'Letters with their words', emoji: '📖', color: T },
    { to: '/converter', title: 'Gesture Converter', sub: 'Type or sign — see fingerspelling', emoji: '✋', color: K },
    { to: '/speech', title: 'Speech to Sign', sub: 'Speak a word, watch it sign', emoji: '🗣️', color: C },
    { to: '/dictionary', title: 'Official Dictionary', sub: '6,000+ real PSL signs', emoji: '🌐', color: T },
    { to: '/stickers', title: 'Stickers & Badges', sub: `Collect with ${learner.stars} stars`, emoji: '🏆', color: Y },
  ];

  return (
    <div className="page">
      <PageHeader
        title={`Hi ${learner.name}! 👋`}
        subtitle={days > 0 ? `${days}-day streak! Keep going!` : 'Ready for today\'s practice?'}
        right={<StarChip stars={learner.stars} />}
      />

      <div
        className="home-hero"
        role="button"
        tabIndex={0}
        aria-label="Continue your learning journey"
        onClick={() => nav('/learn')}
        onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); nav('/learn'); } }}
      >
        <img src="./img/hero.jpg" alt="Irssa" className="home-hero-img" />
        <div className="home-hero-text">
          <div className="urdu hero-urdu">پاکستان سائن لینگویج سیکھیں</div>
          <div>Tap to continue your journey 🚀</div>
        </div>
        <div className="home-hero-badge" onClick={(e) => e.stopPropagation()}>
          <div className="goal-ring">
            <svg viewBox="0 0 60 60" width={54} height={54}>
              <circle cx="30" cy="30" r="25" className="ring-track" />
              <circle
                cx="30" cy="30" r="25" className="ring-value"
                strokeDasharray={157}
                strokeDashoffset={157 * (1 - pct / 100)}
              />
            </svg>
            <div className="goal-label">{sc}/{learner.dailyGoal}</div>
          </div>
          <div>Today's goal</div>
        </div>
      </div>

      <div className="home-grid">
        {cards.map((c) => (
          <button
            key={c.to}
            className="home-card"
            style={{ ['--c']: `#${(c.color & 0xFFFFFF).toString(16).padStart(6, '0')}` } as React.CSSProperties}
            onClick={() => nav(c.to)}
          >
            <span className="hc-emoji">{c.emoji}</span>
            <span className="hc-title">{c.title}</span>
            <span className="hc-sub">{c.sub}</span>
          </button>
        ))}
      </div>

      <button className="family-link" onClick={() => nav('/family')}>
        👨‍👩‍👧 For grown-ups: progress, settings & honest notes
      </button>

      {/* The 100-second demo of this very app — hosted with the app, works offline. */}
      <a className="family-link demo-link" href="video/irssa-demo.mp4" target="_blank" rel="noreferrer">
        ▶ Watch the 100-second demo (video)
      </a>

      <p className="home-footer">
        Irssa · made with love for Pakistan's Deaf community ·{' '}
        <a href="#/family" onClick={(e) => { e.preventDefault(); nav('/family'); }}>attribution</a>
      </p>
    </div>
  );
}
