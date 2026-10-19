/**
 * Port of LearnerState / ProgressRules / LearningStore (LearningStore.kt),
 * persisted to localStorage in the SAME v2 JSON format the Android app uses
 * (learning-v2.json). A progress backup exported by the APK can be pasted
 * into the web app and vice versa.
 */
import React, { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { today } from '../lib/util';

export interface LearnerState {
  name: string;
  welcomed: boolean;
  stars: number;
  seen: Set<string>;
  mastered: Set<string>;
  favorites: Set<string>;
  rewards: Set<string>;
  practiceDays: Set<string>;
  haptics: boolean;
  reducedMotion: boolean;
  showUrdu: boolean;
  dailyGoal: number;
}

export type RewardKind = 'learn' | 'quiz' | 'camera';

export interface StoreState {
  learner: LearnerState;
  reward: (id: string, kind: RewardKind) => void;
  favorite: (id: string) => void;
  update: (change: (s: LearnerState) => LearnerState) => void;
  reset: () => void;
  importState: (json: string) => boolean;
  exportState: () => string;
}

const KEY = 'irssa-learning-v2';

export function freshState(): LearnerState {
  return {
    name: 'Irssa',
    welcomed: false,
    stars: 0,
    seen: new Set(),
    mastered: new Set(),
    favorites: new Set(),
    rewards: new Set(),
    practiceDays: new Set(),
    haptics: true,
    reducedMotion: false,
    showUrdu: true,
    dailyGoal: 5,
  };
}

function encodeState(s: LearnerState): string {
  const arr = (v: Set<string>) => [...v];
  return JSON.stringify({
    version: 2,
    name: s.name,
    welcomed: s.welcomed,
    stars: s.stars,
    seen: arr(s.seen),
    mastered: arr(s.mastered),
    favorites: arr(s.favorites),
    rewards: arr(s.rewards),
    practiceDays: arr(s.practiceDays),
    haptics: s.haptics,
    reducedMotion: s.reducedMotion,
    showUrdu: s.showUrdu,
    dailyGoal: s.dailyGoal,
  });
}

function decodeState(text: string): LearnerState {
  const o = JSON.parse(text);
  if (o?.version !== 2) throw new Error('Incompatible progress version');
  const set = (k: string): Set<string> => {
    const a = o[k];
    return Array.isArray(a) ? new Set(a.filter((x) => typeof x === 'string')) : new Set();
  };
  return {
    name: typeof o.name === 'string' && o.name ? o.name : 'Irssa',
    welcomed: Boolean(o.welcomed),
    stars: Number.isFinite(o.stars) ? Math.max(0, Math.trunc(o.stars)) : 0,
    seen: set('seen'), mastered: set('mastered'), favorites: set('favorites'),
    rewards: set('rewards'), practiceDays: set('practiceDays'),
    haptics: o.haptics !== false,
    reducedMotion: o.reducedMotion === true,
    showUrdu: o.showUrdu !== false,
    dailyGoal: Math.min(10, Math.max(3, Number.isFinite(o.dailyGoal) ? Math.trunc(o.dailyGoal) : 5)),
  };
}

function loadState(): LearnerState {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? decodeState(raw) : freshState();
  } catch {
    return freshState();
  }
}

export function progressRulesReward(s: LearnerState, id: string, kind: RewardKind, day: string): LearnerState {
  const key = `${day}|${kind}|${id}`;
  const points = kind === 'camera' ? 10 : kind === 'quiz' ? 5 : 2;
  return {
    ...s,
    stars: s.stars + (s.rewards.has(key) ? 0 : points),
    seen: new Set(s.seen).add(id),
    mastered: kind === 'camera' ? new Set(s.mastered).add(id) : s.mastered,
    rewards: new Set(s.rewards).add(key),
    practiceDays: new Set(s.practiceDays).add(day),
  };
}

export function todayCount(s: LearnerState, day = today()): number {
  let n = 0;
  s.rewards.forEach((r) => { if (r.startsWith(`${day}|learn|`)) n++; });
  return n;
}

export function streak(s: LearnerState): number {
  const d = new Date();
  const iso = (dt: Date) => dt.toISOString().slice(0, 10);
  let day = new Date(d);
  if (!s.practiceDays.has(iso(day))) day.setDate(day.getDate() - 1);
  let count = 0;
  while (s.practiceDays.has(iso(day))) {
    count++;
    day.setDate(day.getDate() - 1);
  }
  return count;
}

const StoreContext = createContext<StoreState | null>(null);

export function StoreProvider({ children }: { children: React.ReactNode }) {
  const [learner, setLearner] = useState<LearnerState>(() => loadState());

  const persist = useCallback((next: LearnerState) => {
    try {
      localStorage.setItem(KEY, encodeState(next));
    } catch {
      /* storage full or private mode — progress simply stays in memory */
    }
  }, []);

  const setAll = useCallback((next: LearnerState | ((prev: LearnerState) => LearnerState)) => {
    const resolved = typeof next === 'function' ? (next as (prev: LearnerState) => LearnerState)(learner) : next;
    setLearner(resolved);
    persist(resolved);
  }, [persist, learner]);

  const store = useMemo<StoreState>(() => ({
    learner,
    reward: (id, kind) => setAll(progressRulesReward(learner, id, kind, today())),
    favorite: (id) => setAll(() => {
      const favorites = new Set(learner.favorites);
      if (favorites.has(id)) favorites.delete(id);
      else favorites.add(id);
      return { ...learner, favorites };
    }),
    update: (change) => setAll(change(learner)),
    reset: () => setAll(freshState()),
    importState: (json: string) => {
      try {
        setAll(decodeState(json.trim()));
        return true;
      } catch {
        return false;
      }
    },
    exportState: () => encodeState(learner),
  }), [learner, setAll]);

  return <StoreContext.Provider value={store}>{children}</StoreContext.Provider>;
}

export function useStore(): StoreState {
  const ctx = useContext(StoreContext);
  if (!ctx) throw new Error('useStore outside StoreProvider');
  return ctx;
}
