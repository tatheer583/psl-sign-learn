# Irssa — LinkedIn launch post

**Recommended:** upload the MP4 as a **native video post** (LinkedIn → Start a post → Video).
Native video gets autoplay in the feed, captions, and far more reach than a link.

- Video file: `docs/video/irssa-demo.mp4` (1080 × 1920, ~100 s, silent-safe with music)
- Direct link: `https://raw.githubusercontent.com/tatheer583/psl-sign-learn/main/docs/video/irssa-demo.mp4`
- Live web app: `https://tatheer583.github.io/psl-sign-learn/`
- Repo: `https://github.com/tatheer583/psl-sign-learn`
- Thumbnail / link preview: `docs/video/cover.png` (1200 × 627)

---

## Post (English)

**Every deaf child in Pakistan deserves to see her own language on a screen.**

Last month I kept seeing the same problem: Pakistani deaf children have almost no
free material for learning Pakistan Sign Language (PSL) — 37 Urdu letters, thousands
of words — and the tools that do exist are expensive, online-only, or built for
adults.

So I built **Irssa (ارسا)** — a free, fully offline sign-language learning game for
deaf and speech-impaired children.

🎥 **Watch the demo (real screen recording of the app, not a mockup):**
[▶ Watch it on LinkedIn / GitHub](https://github.com/tatheer583/psl-sign-learn)
[Try the live web app](https://tatheer583.github.io/psl-sign-learn/)

**What is actually inside (all of it works today):**

- **37 Urdu letters + 26 English letters + numbers 0–50 + everyday words**, each
  taught with a **real hand photograph** (CC BY 4.0 Mendeley dataset) and a word the
  child already knows — ا for انار (pomegranate), ب for بکری (goat).
- **A camera coach that runs entirely on the device** — MediaPipe Hand Landmarker
  tracks 21 hand landmarks and a kNN classifier names the sign. It holds the shape for
  1.2 seconds and *then* celebrates. If the shape is not right, it says "not yet" —
  the acceptance gate is deliberately strict so it never gives false praise.
- **The official PSL dictionary**: 6,447 real sign videos streamed straight from
  [psl.org.pk](https://psl.org.pk/) (FESF / Deaf Reach) with attribution and a link
  back on every single card. Nothing is re-hosted.
- **Gesture converter + speech-to-sign** — type or say a word and watch it
  finger-spelled, letter by letter.
- **Games**: sign quizzes with same-category distractors, a 45-second Speed Match
  challenge, camera quests, and a sticker/badge system that keeps motivation high.
- **A grown-ups area**: daily goals, progress backup, and honest notes about what
  the camera can and cannot do.
- **Privacy as a feature**: no accounts, no ads, no analytics. Camera frames are
  processed on the phone and discarded instantly.

**Why it is built this way:**
A photograph can support a teacher — it can never replace one. So the app teaches
with real hands and real words, and it is honest about its limits instead of
faking progress.

**Tech:** Android (Kotlin + Jetpack Compose + MediaPipe) and a React + Vite web
build, with the recognition models trained in `tools/` on multi-signer datasets
(Mendeley CC BY 4.0, HuggingFace MIT, Kaggle) — see `docs/RESEARCH.md`.

**Free, offline, and open source (MIT).** If you know a deaf child, a special
school, or a Deaf educator in Pakistan — please try it and share it with them. 🙏

A camera that cheers for her. 🌈

#PakistanSignLanguage #SignLanguage #DeafEducation #Accessibility #InclusiveEducation
#TechForGood #OpenSource #Android #MediaPipe #MachineLearning #Pakistan #SpecialEducation
#DeafAwareness #AssistiveTechnology #React #MachineLearningForGood

---

## Shorter version (if you want a punchier post)

I built a free, offline Pakistan Sign Language learning game for deaf children in
Pakistan — and it is fully open source. 🎥

37 Urdu letters with real hand photos, an on-device camera coach that tracks 21 hand
landmarks and only celebrates when the shape is truly right, the official 6,447-video
PSL dictionary from psl.org.pk, quizzes, a finger-spelling converter, and zero
accounts, ads or tracking.

Demo (real screen recording): https://github.com/tatheer583/psl-sign-learn
Try it: https://tatheer583.github.io/psl-sign-learn/

A photograph can support a teacher — it can never replace one. But it can be the
moment a letter finally clicks. 🌈

#PakistanSignLanguage #DeafEducation #Accessibility #OpenSource #TechForGood

---

## Urdu version (for a Pakistan-first audience)

**ہر deaf بچے کو اپنی زبان اسکرین پر دیکھنے کا حق ہے۔**

میں نے **Irssa (ارسا)** بنایا ہے — پاکستان سائن لینگویج سیکھنے کا مکمل آف لائن اور مفت
ایپ، جو deaf اور بول نہ سکنے والے بچوں کے لیے ہے۔

- 37 اردو حروف، ہر ایکے ساتھ اصل ہاتھ کی تصویر اور وہ لفظ جو بچے کو پہلے سے معلوم ہو
- کیمرے کا اسپلٹ ہاتھ کے 21 لینڈمارکس ٹریک کرتا ہے اور صرف صحیح شکل پر ہی کنفیٹی مناتا ہے
- psl.org.pk کی سرکاری 6,447 ویڈیو ڈکشنری، ہر ویڈیو پر تصدیق کے ساتھ
- کوئی اکاؤنٹ نہیں، کوئی اشتہار نہیں، کوئی ٹریکنگ نہیں — سب کچھ آف لائن

ویڈیو: https://github.com/tatheer583/psl-sign-learn
ایپ چلائیں: https://tatheer583.github.io/psl-sign-learn/

اگر آپ کو کوئی deaf بچہ، اسپیشل اسکول یا Deaf educator پاکستان میں ملتا ہے — please
اسے ان کے ساتھ ضرور share کریں۔ 🙏

#پاکستان_سائن_لینگویج #Irssa #Accessibility

---

## Tips for posting

1. **Upload the video natively** (not a link) — LinkedIn autoplays native video in
   the feed and it gets far more views. Add subtitles: LinkedIn auto-generates them,
   but the video already has burned-in captions.
2. **First comment** = the direct link to the repo and the live web app (LinkedIn
   suppresses reach on posts with outbound links in the body).
3. **Post between 8–10 am PKT** (LinkedIn's peak for a Pakistan audience is usually
   morning/evening).
4. **Tag**: `FESF` / `Deaf Reach Pakistan`, `National Institute for the Deaf` — and
   any special schools in your network. A tag from a school or NGO is worth more than
   any hashtag.
5. **Reply to every comment in the first hour** — LinkedIn boosts posts with fast
   conversation.
6. Cross-post the same video to YouTube (Shorts) and GitHub Releases; put the mp4 in
   a GitHub Release too, so the link never rots.
