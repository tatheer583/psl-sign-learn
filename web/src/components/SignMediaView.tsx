import React, { useEffect, useState } from 'react';
import type { Lesson } from '../lib/lessons';
import { mediaUrl } from '../lib/lessons';
import { cssColor } from '../lib/util';
import { useStore } from '../store/store';

/**
 * Port of SignMedia.kt — shows the lesson's real hand photograph when it
 * exists, otherwise a friendly placeholder card. No camera media is ever
 * fetched from the internet; everything is bundled.
 */
export function SignMediaView({ lesson }: { lesson: Lesson }) {
  const { learner } = useStore();
  const [hidden, setHidden] = useState(false);
  const url = mediaUrl(lesson);

  useEffect(() => setHidden(false), [lesson.id]);

  if (!url || hidden) {
    return (
      <div className="sign-media placeholder" style={{ background: cssColor(0xFFFFF0EB) }}>
        <div className="sign-media-letter" style={{ color: cssColor(lesson.item.colorHex) }}>
          {lesson.title}
        </div>
        <div className="media-fallback">Let's learn this one with your teacher.</div>
        <div className="media-fallback small">A checked video is not available yet.</div>
      </div>
    );
  }

  return (
    <div className="sign-media photo">
      {!learner.reducedMotion && (
        <div className="photo-halo" style={{ background: cssColor(lesson.item.colorHex, 0.14) }} />
      )}
      <img
        src={url}
        alt={`A real hand showing ${lesson.title} (${lesson.language})`}
        loading="lazy"
        className="sign-photo"
        onError={() => setHidden(true)}
      />
      <div className="media-caption">● Real hand photo — {lesson.author.split(',')[0]}</div>
    </div>
  );
}
