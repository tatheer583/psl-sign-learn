<div align="center">

<img src="docs/banner.png" alt="Irssa — Learn Pakistan Sign Language" width="100%"/>

# Irssa — ارسا 🌈

**پاکستان سائن لینگویج سیکھیں · Learn Pakistan Sign Language**

*A learning game for deaf and speech-impaired children — built with real hands,
real data, and a lot of love.* ❤️

![Platform](https://img.shields.io/badge/platform-Android%207%2B-3DDC84?logo=android&logoColor=white)
![Offline](https://img.shields.io/badge/works-100%25%20offline-5C52E5)
![Letters](https://img.shields.io/badge/letters-37%20Urdu%20%2B%2026%20English-FF5E7E)
![Built with](https://img.shields.io/badge/built%20with-Kotlin%20·%20Compose%20·%20MediaPipe-7F52FF?logo=kotlin&logoColor=white)
![Data](https://img.shields.io/badge/data-CC%20BY%204.0%20·%20MIT%20·%20CC0-00B894)

Every deaf child deserves to see her language on a screen and think *"that's my hands."*
Irssa is built for that moment — the moment a letter finally clicks, the camera smiles,
and confetti falls. 🎉

</div>

---

## 🎬 Watch it work — 100 seconds, the real app

[![Irssa demo video](docs/video/thumbnail.png)](docs/video/irssa-demo.mp4)

**▶ [Play the demo video (MP4 · 1080 × 1920 · ~101 s)](docs/video/irssa-demo.mp4)**

A screen recording of the **production web build running in a real browser** —
nothing in it is mocked up:

- the one-tap onboarding (no account, no ads, nothing leaves the phone),
- the 37 Urdu letters, each with its **real hand photograph** (CC BY 4.0 dataset),
- the **official FESF dictionary** streaming live from psl.org.pk,
- the gesture converter finger-spelling a word letter by letter,
- a quiz round with confetti, stickers and stars,
- and the **camera coach** tracking a hand live on the device — 21 landmarks,
  no fake praise, no uploads.

<sub>Captions are burned in, so it plays silently anywhere. Music is original and
license-free. Vertical 9:16 — it plays in the LinkedIn and YouTube feeds as-is.</sub>

📣 **Sharing it?** A ready-to-post LinkedIn caption (English + Urdu) lives in
[`docs/linkedin-post.md`](docs/linkedin-post.md).

---

## 📲 Download & install (no setup needed)

1. Open the [`apk/`](apk/) folder of this repository.
2. Download **`Irssa-v1.0-debug.apk`** onto any Android phone (Android 7.0 or newer).
3. Open the file and allow *"Install from unknown sources"* if your phone asks.
4. That's it — the app works **fully offline**. Nothing is ever sent anywhere.

> 🔒 **Her privacy comes first.** No accounts, no ads, no analytics. Camera frames are
> processed on the device and discarded instantly — her practice belongs to her alone.

---

## 💛 What's inside

### Two alphabets, two languages — clearly separated

- **اردو (PSL): 37 letters** — every letter taught with a real hand photograph,
  gentle step-by-step cues, and Urdu words she knows: **ا for انار** (pomegranate),
  **ب for بکری** (goat)…
- **English (ASL): 26 letters** — real photographs and English words, taught as a
  *different* language, because it is one. Never mixed, never confused.

### A camera that cheers for her 📸✨

An offline recognition coach watches her hand and celebrates when a sign is truly
formed — **37 PSL handshapes and 24 static English letters**. It is trained on real
hands from **independent signers** across multiple datasets, and tuned to prefer
saying *"take your time"* over a false *"you did it!"*. When the sign holds for
1.2 seconds: stars, haptics, confetti — success she can feel, not just hear.

**Camera Studio (web)** practices in a two-panel view: the live mirror on one
side, the sign she is working on on the other — and the side card follows her
hand, naming any sign she makes ("Looks like ا 👋") while honest notes say when
it differs from the target. A confirmed hold rings Well done! and the studio
**automatically brings up the next sign in the set** — ا→ب→پ… / A→B→C… / 1→2→3… —
so one visit can cover the whole alphabet. A camera match still means *"this
handshape looks right"* — never a grade of her ability.

### Numbers, words, and games 🔢🎮

- **Numbers 0–50 and everyday words** — guided practice, made to be learned together
  with a teacher or family.
- **Speed Match, quizzes, camera quests, stickers** — because learning should feel
  like playing. Quiz questions show **4 options with same-category distractors**,
  matching the Android original.
- **Gesture converter & speech-to-sign** — type or speak a word and watch it
  finger-spelled, letter by letter; every spelled word has a **Listen again** replay
  and a **Next word** button, and each row in Alphabet & Words can be heard aloud.
- **Official Dictionary (web)** — 6,447 real PSL sign videos streamed from
  [psl.org.pk](https://psl.org.pk/) with attribution and link-back: search Urdu or
  English, browse kid-friendly category chips, and a **Next sign** button keeps
  playing one official video after another. A few entries simply don't exist on
  psl.org.pk (numbers 21–49 in steps of one, the word *Friend*) — Irssa shows no
  card at all for those rather than guessing.
- **A grown-ups area** — progress backup, daily goals, full media attribution, and
  honest notes about what the camera can and cannot do.

### Make it *hers*: teach the app her own hand ✋💖

Every hand is different. The single biggest accuracy upgrade is teaching the
recognizer *her* hand — 15 quiet minutes at a computer with a webcam:

```
tools/venv/Scripts/python.exe tools/capture_my_hand.py psl      # then: english
tools/venv/Scripts/python.exe tools/train_hybrid.py
```

Her hand enters the model weighted heavily, and the app starts understanding *her*.

### The 37 PSL handshapes

![PSL handshapes reference](docs/handshapes-reference.png)

---

## 🔍 How the camera recognition works — honestly

- [Google MediaPipe Hand Landmarker](https://developers.google.com/edge/mediapipe/solutions/vision/hand_landmarker)
  finds 21 hand landmarks; a nearest-neighbour classifier trained on real hand
  landmarks names the letter.
- **English J and Z are guided practice** — they need motion, so a static model never
  pretends to grade them. Numbers and word signs need movement or both hands.
- The acceptance gate is deliberately strict, tuned on held-out data: the app would
  rather say *"not yet, try again"* than lie with a wrong *"you did it!"*.
- A camera match means *"that handshape looks like this letter"* — it is **never** a
  measure of the child's ability. She is always more than a score.

---

## 🛠️ Building the app yourself

1. Open the project in [Android Studio](https://developer.android.com/studio).
2. Let it sync (a `debug.keystore` is included, so the debug build signs itself).
3. Run on a device or emulator, or:

   ```
   ./gradlew assembleDebug
   ```

Reproducible training lives in `tools/`: landmark extraction, hybrid multi-signer
training (`tools/train_hybrid.py`), and evaluation results in
`docs/recognition-evaluation.json`. Design and licensing notes: `docs/RESEARCH.md`.

---

## 🙏 Real hands, credited with gratitude

This app stands on real hands and generous datasets:

- **Urdu/PSL hand photographs (signer 1):** Ali Imran Ali (2021), *"Data set about hand
  configuration of Pakistan Sign Language"*, Mendeley Data V1,
  [doi:10.17632/y9svrbh27n.1](https://data.mendeley.com/datasets/y9svrbh27n/1) — **CC BY 4.0**.
  Paper: Imran et al. (2021), *Data in Brief* 36, 107021.
- **PSL gesture landmarks (signer 2):** [Bakhtyar12/Pakistani-Sign-Language](https://huggingface.co/datasets/Bakhtyar12/Pakistani-Sign-Language),
  Hugging Face, **MIT** — landmark sequences for every Urdu letter, which made recognition
  honest across different hands.
- **PSL landmark dataset (signer 3+):** [saadbutt321/Pakistan-Sign-Language](https://www.kaggle.com/datasets/saadbutt321/pakistan-sign-language-dataset),
  Kaggle — OpenPose 21-point hand landmarks for all 37 Urdu letters plus 12 common
  words, extracted with `tools/new_dataset/extract_new_dataset.mjs`. **No license
  file ships with the dataset**; verify its Kaggle listing before any public
  redistribution of extracted features. It measurably improved cross-person
  generalization (see `docs/recognition-evaluation.json`).
- **English letters:** Ayush Thakur, [ASL Dataset](https://www.kaggle.com/datasets/ayuraj/asl-dataset) (**CC0**)
  and [SignAlphaSet](https://data.mendeley.com/datasets/8fmvr9m98w/1) (Mendeley, **CC BY 4.0**).
- **PSL learning resources:** [Deaf Reach / PSL Dictionary](https://psl.org.pk/) — the
  heart of PSL learning in Pakistan. Irssa’s **Official Dictionary** screen (web) and
  lesson pages **stream their official sign videos straight from FESF’s own CDN**,
  with visible attribution on every item and a link back to `psl.org.pk`. Nothing is
  re-hosted: the bundled search index holds metadata only
  (`web/public/marks/psl-dictionary-index.json`). All dictionary © FESF — learn
  directly from them when you can.

*You can also train with private family recordings locally (e.g. a personal dataset like
the FESF dictionary materials) — the tools support it, and such data stays on your
computer, out of this repository, out of respect for its rights holders.*

## 📜 License

The **code** of Irssa (Android app, web app, training tools) is **MIT** — see
[LICENSE](LICENSE). The datasets that power the recognition models keep their own
licenses as listed above; model binaries and learning content are built from them
and are offered here under the same attribution spirit. The Kaggle
[saadbutt321/Pakistan-Sign-Language](https://www.kaggle.com/datasets/saadbutt321/pakistan-sign-language-dataset)
dataset ships no license file — check its listing before redistributing derived
artifacts.

---

## 🌍 Why Pakistan Sign Language

Language is identity. This edition teaches **Pakistan Sign Language** because that is
the language of Pakistan's Deaf community — with English fingerspelling taught
separately, as its own skill. Signs vary between regions and schools, so always confirm
what she learns here with her family and a fluent Deaf educator.

**A photograph can support a teacher. It can never replace one.** 💐

---

<div align="center">

*Made for curious hands and growing minds.* ✋🌱

**Irssa 2.0 · Free, offline, and made with love**

</div>
