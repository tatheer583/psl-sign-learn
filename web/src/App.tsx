import React, { useEffect } from 'react';
import { HashRouter, Route, Routes, useLocation } from 'react-router-dom';
import { StoreProvider, useStore } from './store/store';
import { OnboardingScreen } from './screens/Onboarding';
import { HomeScreen } from './screens/Home';
import { LearnScreen } from './screens/Learn';
import { LessonDetailScreen } from './screens/LessonDetail';
import { CameraStudioScreen } from './screens/CameraStudio';
import { CameraQuizScreen } from './screens/CameraQuiz';
import { GameQuizScreen } from './screens/GameQuiz';
import { SpeedMatchScreen } from './screens/SpeedMatch';
import { AlphabetWordsScreen } from './screens/AlphabetWords';
import { GestureConverterScreen } from './screens/GestureConverter';
import { SpeechToSignScreen } from './screens/SpeechToSign';
import { DictionaryScreen } from './screens/Dictionary';
import { StickersScreen } from './screens/Stickers';
import { FamilyScreen } from './screens/Family';

function Shell() {
  const { learner } = useStore();
  const location = useLocation();
  const onboarding = !learner.welcomed && location.pathname !== '/onboarding';

  useEffect(() => { window.scrollTo(0, 0); }, [location.pathname]);
  if (onboarding) return <OnboardingScreen />;

  return (
    <>
      <main className="app-main">
        <Routes>
          <Route path="/" element={<HomeScreen />} />
          <Route path="/onboarding" element={<OnboardingScreen />} />
          <Route path="/learn" element={<LearnScreen />} />
          <Route path="/learn/:id" element={<LessonDetailScreen />} />
          <Route path="/practice" element={<CameraStudioScreen />} />
          <Route path="/camera-quiz" element={<CameraQuizScreen />} />
          <Route path="/quiz" element={<GameQuizScreen />} />
          <Route path="/speed" element={<SpeedMatchScreen />} />
          <Route path="/alphabet-words" element={<AlphabetWordsScreen />} />
          <Route path="/dictionary" element={<DictionaryScreen />} />
          <Route path="/converter" element={<GestureConverterScreen />} />
          <Route path="/speech" element={<SpeechToSignScreen />} />
          <Route path="/stickers" element={<StickersScreen />} />
          <Route path="/family" element={<FamilyScreen />} />
          <Route path="*" element={<HomeScreen />} />
        </Routes>
      </main>
      <BottomNav />
    </>
  );
}

const NAV = [
  { to: '/', label: 'Home', icon: '🏠' },
  { to: '/learn', label: 'Learn', icon: '📚' },
  { to: '/practice', label: 'Practice', icon: '📸' },
  { to: '/quiz', label: 'Play', icon: '🧩' },
  { to: '/family', label: 'Family', icon: '👨‍👩‍👧' },
];

function BottomNav() {
  const location = useLocation();
  return (
    <nav className="bottom-nav" aria-label="Main">
      {NAV.map((n) => {
        const active = n.to === '/' ? location.pathname === '/' : location.pathname.startsWith(n.to);
        return (
          <a
            key={n.to}
            href={`#${n.to}`}
            className={`bn-item ${active ? 'active' : ''}`}
            aria-current={active ? 'page' : undefined}
          >
            <span className="bn-icon">{n.icon}</span>
            <span className="bn-label">{n.label}</span>
          </a>
        );
      })}
    </nav>
  );
}

export default function App() {
  return (
    <StoreProvider>
      <HashRouter>
        <Shell />
      </HashRouter>
    </StoreProvider>
  );
}
