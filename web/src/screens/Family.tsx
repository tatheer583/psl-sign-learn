import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '../components/Pieces';
import { loadManifest } from '../lib/lessons';
import { todayCount, streak, useStore } from '../store/store';
import type { ManifestEntry } from '../lib/lessons';

/** Port of FamilyScreens.kt — progress, settings, backup, attribution, honesty. */
export function FamilyScreen() {
  const nav = useNavigate();
  const { learner, update, reset, importState, exportState } = useStore();
  const [importMsg, setImportMsg] = useState<string | null>(null);
  const [manifest, setManifest] = useState<ManifestEntry[]>([]);
  const fileRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    loadManifest()
      .then((m) => setManifest([...m.values()]))
      .catch(() => setManifest([]));
  }, []);

  const learnedCount = learner.seen.size;
  const masteredCount = learner.mastered.size;
  const sc = todayCount(learner);
  const days = streak(learner);

  const exportJson = () => {
    const blob = new Blob([exportState()], { type: 'application/json' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `irssa-progress-${learner.name.toLowerCase().replace(/\W+/g, '-') || 'learner'}.json`;
    a.click();
    URL.revokeObjectURL(a.href);
  };

  const importJson = (file: File) => {
    const reader = new FileReader();
    reader.onload = () => {
      const ok = importState(String(reader.result ?? ''));
      setImportMsg(ok ? 'Progress restored! 💛' : 'That file could not be read as Irssa progress.');
    };
    reader.readAsText(file);
  };

  return (
    <div className="page family-page">
      <PageHeader title="For grown-ups 👨‍👩‍👧" subtitle="Progress, settings and honest notes" onBack={() => nav('/')} />

      <section className="family-card">
        <h3>Progress</h3>
        <div className="stats-row">
          <div className="stat"><b>{learner.stars}</b><span>⭐ stars</span></div>
          <div className="stat"><b>{learnedCount}</b><span>opened</span></div>
          <div className="stat"><b>{masteredCount}</b><span>mastered</span></div>
          <div className="stat"><b>{days}</b><span>day streak</span></div>
        </div>
        <div className="stat-detail">
          Today: {sc} of {learner.dailyGoal} lessons · Total practice days: {learner.practiceDays.size}
        </div>
      </section>

      <section className="family-card">
        <h3>Settings</h3>
        <label className="switch-row">
          <input
            type="checkbox"
            checked={learner.haptics}
            onChange={(e) => update((s) => ({ ...s, haptics: e.target.checked }))}
          />
          Vibration feedback (on phones)
        </label>
        <label className="switch-row">
          <input
            type="checkbox"
            checked={learner.reducedMotion}
            onChange={(e) => update((s) => ({ ...s, reducedMotion: e.target.checked }))}
          />
          Reduced motion (no confetti)
        </label>
        <label className="switch-row">
          <input
            type="checkbox"
            checked={learner.showUrdu}
            onChange={(e) => update((s) => ({ ...s, showUrdu: e.target.checked }))}
          />
          Show Urdu script together with English
        </label>
        <div className="goal-row">
          <span>Daily goal: {learner.dailyGoal} lessons</span>
          <input
            type="range" min={3} max={10} value={learner.dailyGoal}
            onChange={(e) => update((s) => ({ ...s, dailyGoal: Number(e.target.value) }))}
          />
        </div>
      </section>

      <section className="family-card">
        <h3>Backup & restore</h3>
        <p className="muted">
          Progress lives in this browser. Export a small JSON file and import it on any
          device — nothing is ever uploaded by the app.
        </p>
        <div className="btn-row">
          <button className="btn secondary" onClick={exportJson}>⬇ Export progress</button>
          <button className="btn secondary" onClick={() => fileRef.current?.click()}>⬆ Import progress</button>
          <input
            ref={fileRef}
            type="file"
            accept="application/json"
            hidden
            onChange={(e) => e.target.files?.[0] && importJson(e.target.files[0])}
          />
        </div>
        {importMsg && <p className="muted small">{importMsg}</p>}
        <div className="btn-row">
          <button
            className="btn danger"
            onClick={() => {
              if (window.confirm('Delete all stars and history from this browser? This cannot be undone.')) reset();
            }}
          >
            Reset all progress
          </button>
        </div>
      </section>

      <section className="family-card">
        <h3>Media attribution — with gratitude 🙏</h3>
        <div className="attr-table">
          {manifest.map((m) => (
            <div key={m.id} className="attr-row">
              <span className="attr-id">{m.id}</span>
              <span className="attr-author">{m.author}</span>
              <span className="attr-license">{m.license}</span>
            </div>
          ))}
        </div>
      </section>

      <section className="family-card">
        <h3>Honest notes 💛</h3>
        <ul className="honest-list">
          <li>The camera runs entirely on this device — frames are analysed and discarded instantly. No accounts, no ads, no analytics.</li>
          <li>A camera match means "the handshape looks like this letter" — never a measure of her ability. She is always more than a score.</li>
          <li>Number signs and everyday signs need movement or both hands, so the camera only grades static handshapes with real training data behind them.</li>
          <li>Signs vary between regions and schools. Always confirm learning with a fluent Deaf educator. <a href="https://psl.org.pk/" target="_blank" rel="noreferrer">psl.org.pk (Deaf Reach)</a> is the heart of PSL learning in Pakistan.</li>
        </ul>
      </section>

      <p className="home-footer">Irssa 2.0 · Free, offline, and made with love</p>
    </div>
  );
}
