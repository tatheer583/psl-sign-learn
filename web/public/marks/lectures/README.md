# Teacher lecture videos

This folder holds **teacher-made lecture videos** that ship inside the web app
and play offline from `marks/lectures/`. It exists so a teacher can add their
own recorded lesson videos (for example a PSL classroom lecture) and have the
app arrange them under a *Teacher lectures* section on the Official Dictionary
screen.

## How to add

1. Put the video file here (mp4, H.264 recommended, keep it small).
2. Add an entry to `manifest.json`:

```json
{
  "lectures": [
    {
      "title": "PSL classroom lecture 1",
      "description": "What the lecture teaches",
      "file": "lecture-1.mp4",
      "author": "the teacher's name",
      "license": "e.g. CC BY 4.0, all rights reserved, or permission note",
      "topics": ["vocab_school", "word keywords the lecture teaches"]
    }
  ]
}
```

3. Commit both files. The web app reads the manifest at runtime — an empty
   `lectures` array simply shows no lectures (never a fake player).

Honesty note: whatever you add must be a video you have the right to
redistribute. Videos taken from psl.org.pk stay OUT of this folder — they are
streamed from the official CDN with attribution by the dictionary screen.
