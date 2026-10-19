import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Confetti } from '../components/Confetti';
import { PageHeader, StarChip } from '../components/Pieces';
import { quizPool } from '../lib/pool';
import { shuffle, buzz } from '../lib/util';
import { useStore } from '../store/store';

type QuizType = 'IDENTIFY_SIGN' | 'MATCH_OBJECT' | 'COUNTING_FINGERS';

interface QuizQuestion {
  targetItem: import('../content/types').LearningItem;
  prompt: string;
  options: import('../content/types').LearningItem[];
  type: QuizType;
}

function generate(): QuizQuestion {
  const pool = quizPool();
  const target = pool[Math.floor(Math.random() * pool.length)];
  const others = pool.filter((i) => i.id !== target.id).slice(0, 2);
  const options = shuffle([...others, target]);
  const type: QuizType = target.visualItemsCount > 0
    ? 'COUNTING_FINGERS'
    : target.category === 'ALPHABET' ? 'IDENTIFY_SIGN' : 'MATCH_OBJECT';
  return { targetItem: target, prompt: `Find sign for '${target.title}'`, options, type };
}

/**
 * Port of GameQuizScreen + generateNewQuizQuestion():
 * 50/50 letters-vs-numbers, 4 options, +5 stars per correct answer.
 */
export function GameQuizScreen() {
  const nav = useNavigate();
  const { learner, reward } = useStore();
  const [burst, setBurst] = useState(0);
  const [q, setQ] = useState<QuizQuestion>(() => generate());
  const [chosen, setChosen] = useState<string | null>(null);
  const correct = chosen !== null && chosen === q.targetItem.id;
  const answered = chosen !== null;

  const pick = (id: string) => {
    if (answered) return;
    setChosen(id);
    if (id === q.targetItem.id) {
      buzz();
      reward(q.targetItem.id, 'quiz');
      setBurst((b) => b + 1);
    }
  };

  return (
    <div className="page">
      <Confetti burst={burst} reducedMotion={learner.reducedMotion} />
      <PageHeader
        title="Sign Quiz Challenge 🧩"
        subtitle="Pick the matching sign!"
        onBack={() => nav('/')}
        right={<StarChip stars={learner.stars} />}
      />

      <div className="quiz-question-card">
        <div className="qq-big urdu">{q.targetItem.title}</div>
        <div className="qq-emoji">{q.type === 'COUNTING_FINGERS' ? '🔢' : q.type === 'MATCH_OBJECT' ? q.targetItem.objectEmoji : '✋'}</div>
        <div className="qq-prompt">{q.prompt}</div>
      </div>

      <div className="quiz-options">
        {q.options.map((o) => {
          const isTarget = o.id === q.targetItem.id;
          const stateCls = !answered ? '' : isTarget ? 'right' : o.id === chosen ? 'wrong' : '';
          return (
            <button
              key={o.id}
              className={`quiz-option ${stateCls}`}
              onClick={() => pick(o.id)}
              style={{ background: `#${(o.colorHex & 0xFFFFFF).toString(16).padStart(6, '0')}18` }}
            >
              <span className="qo-media">{q.type === 'IDENTIFY_SIGN' ? '🙋' : o.objectEmoji}</span>
              <span className="qo-title urdu">{o.title}</span>
              <span className="qo-sub">{q.type === 'IDENTIFY_SIGN' ? o.subtitle.replace(/ \(.*\)/, '') : o.subtitle}</span>
            </button>
          );
        })}
      </div>

      {answered && (
        <div className="quiz-next">
          <div className={correct ? 'verdict good' : 'verdict'}>
            {correct ? 'Brilliant! That\'s the sign! 🌟' : `It was ${q.targetItem.title} — try the next one! 💛`}
          </div>
          <button
            className="btn primary big"
            onClick={() => { setQ(generate()); setChosen(null); }}
          >
            Next question →
          </button>
        </div>
      )}
    </div>
  );
}
