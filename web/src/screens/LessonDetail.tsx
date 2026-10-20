import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { itemById } from '../content/types';
import { loadLessons, type Lesson } from '../lib/lessons';
import { SignMediaView } from '../components/SignMediaView';
import { PageHeader, StarChip } from '../components/Pieces';
import { useStore } from '../store/store';
import { buzz, cssColor } from '../lib/util';
import { speak } from '../lib/tts';
import { entryForLesson, dictPageUrl, type PslDictEntry } from '../lib/pslDictionary';

/** Port of the lesson detail (CameraPracticeScreen lesson pane). */
export function LessonDetailScreen() {
  const { id = '' } = useParams();
  const nav = useNavigate();
  const { learner, reward, favorite } = useStore();
  const [lessons, setLessons] = useState<Lesson[] | null>(null);
  const [official, setOfficial] = useState<PslDictEntry | null>(null);
  const item = itemById.get(id);

  useEffect(() => {
    let dead = false;
    loadLessons().then((ls) => { if (!dead) setLessons(ls); }).catch(() => setLessons([]));
    // The official dictionary entry (FESF / psl.org.pk), if this lesson was
    // matched at harvest time — streamed, never re-hosted.
    entryForLesson(id).then((e) => { if (!dead) setOfficial(e); });
    return () => { dead = true; };
  }, [id]);

  if (!item) {
    return (
      <div className="page">
        <PageHeader title="Not found 🤔" onBack={() => nav(-1)} />
        <p className="empty-note">This lesson does not exist.</p>
      </div>
    );
  }

  const l = lessons?.find((x) => x.id === id);
  const view: Lesson = l ?? synthetic(item);
  const isEnglish = id.startsWith('letter_en_');
  const mode = isEnglish ? 'english' : 'psl';
  const needsTeacher = !view.isStatic && view.category === 'Letters';
  const isUrduLetter = item.id.startsWith('letter_') && !item.id.startsWith('letter_en_');
  const mastered = learner.mastered.has(id);
  const fav = learner.favorites.has(id);

  return (
    <div className="page lesson-page">
      <PageHeader
        title={`${item.subtitle}`}
        subtitle={view.language}
        onBack={() => nav('/learn')}
        right={<StarChip stars={learner.stars} />}
      />
      <div className="lesson-grid">
        <div className="lesson-card">
          <SignMediaView lesson={view} />
          <div className="lesson-title-row">
            <div className="lesson-title urdu" style={{ color: cssColor(item.colorHex) }}>{item.title}</div>
            <button
              className="speaker-btn"
              onClick={() => speak(view.word || item.associatedWord || item.title || item.subtitle)}
              aria-label="Say it aloud"
            >🔊</button>
            <button
              className={`fav-btn ${fav ? 'on' : ''}`}
              onClick={() => { buzz([0, 80]); favorite(id); }}
              aria-label="Favorite"
            >{fav ? '💖' : '🤍'}</button>
          </div>
          <div className="lesson-word"><b>{view.word}</b> {item.objectEmoji}</div>
          {item.visualItemsCount > 0 && (
            <div className="lesson-count">{'⭐'.repeat(Math.min(item.visualItemsCount, 10))}{item.visualItemsCount > 10 ? ` ×${item.visualItemsCount}` : ''}</div>
          )}
        </div>

        <div className="lesson-card cues">
          <h3>How to sign it — step by step</h3>
          <ol className="cue-list">
            {item.visualCueSteps.map((s, i) => <li key={i}>{s}</li>)}
          </ol>
          <div className="funfact" style={{ background: cssColor(item.colorHex, 0.12) }}>
            💡 {item.funFact}
          </div>
          <div className="lesson-signname">✋ {item.signName}</div>

          <div className="lesson-actions">
            <button
              className="btn secondary"
              onClick={() => { reward(id, 'learn'); buzz(); }}
              disabled={hasLearnedToday(learner.rewards, id)}
            >
              {hasLearnedToday(learner.rewards, id) ? 'Learned today ✓' : 'I learned this! (+2 ⭐)'}
            </button>
            {view.isStatic ? (
              <button className="btn primary" onClick={() => nav(`/practice?target=${view.classifierLabel}&mode=${mode}&display=${encodeURIComponent(item.title)}`)}>
                📸 Practice on camera
              </button>
            ) : (
              <div className="teacher-note">
                {needsTeacher
                  ? '✋ This letter needs guided practice together with a teacher — a photo alone cannot grade it.'
                  : '🤝 practise together with a teacher or family'}
              </div>
            )}
          </div>
          {mastered && <div className="mastered-chip">✓ Mastered on camera — brilliant!</div>}
        </div>
      </div>
      {official && (
        <div className="lesson-card official-card">
          <h3>Official PSL sign from the FESF dictionary 🌐</h3>
          {official.video ? (
            <video
              src={official.video}
              poster={official.poster ?? official.image}
              controls
              preload="none"
              playsInline
            />
          ) : (
            <p className="empty-note">The full video plays on the official page.</p>
          )}
          <p className="official-note">
            {isUrduLetter && (
              <>FESF’s official sign for “{official.term}” — the memory word of
              letter <span className="urdu">{item.title}</span> — streamed from{' '} </>
            )}
            {!isUrduLetter && (
              <>FESF’s official sign for “{official.term}” — streamed from{' '}</>
            )}
            <a href={dictPageUrl(official.path)} target="_blank" rel="noreferrer noopener">psl.org.pk ↗</a>
            . It never decides your grade: {needsTeacher
              ? 'this sign still needs guided practice with your teacher.'
              : 'your camera keeps its own judgement.'}
          </p>
        </div>
      )}
      <span hidden lang="ur">{'کار'}</span>
    </div>
  );
}

function hasLearnedToday(rewards: Set<string>, id: string): boolean {
  const day = new Date().toISOString().slice(0, 10);
  return rewards.has(`${day}|learn|${id}`);
}

function synthetic(item: NonNullable<ReturnType<typeof itemById.get>>): Lesson {
  return {
    id: item.id, title: item.title, word: '', emoji: item.objectEmoji,
    category: item.id.startsWith('letter_') ? 'Letters' : item.id.startsWith('num_') ? 'Numbers' : 'Words',
    media: '', isVideo: false, source: '', author: '', license: '',
    steps: item.visualCueSteps, item,
    isEnglish: item.id.startsWith('letter_en_'),
    language: item.id.startsWith('letter_en_') ? 'English (ASL)' : 'Urdu (PSL)',
    isStatic: false, recognitionMode: 'psl',
    classifierLabel: item.id.replace('letter_', ''),
  };
}
