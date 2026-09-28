<div align="center">

# Irssa — پاکستان سائن لینگویج سیکھیں

**A Pakistan Sign Language (PSL) learning game for deaf and speech-impaired children**

Learn all 37 letters of the Urdu alphabet with real hand photographs, practise signs
in front of the camera, play games, and collect stickers — completely offline.

</div>

## Download & install (no setup needed)

1. Go to the [`apk/`](apk/) folder of this repository.
2. Download **`Irssa-v1.0-debug.apk`** onto any Android phone (Android 7.0 / API 24 or newer).
3. Open the file and allow *"Install from unknown sources"* if your phone asks.
4. That's it — the app works fully offline. Nothing is sent anywhere.

> The app never stores or uploads camera pictures. Camera frames are processed on the
> device and discarded immediately.

## What's inside

- **37 PSL letter lessons** — one real hand photograph per letter, step-by-step visual
  cues, Urdu words (ا for انار, ب for بکری…) and fun facts.
- **Camera practice** — an offline hand-recognition coach checks 34 static PSL letter
  handshapes in real time and rewards a correct sign held for 1.2 seconds.
- **Numbers 0–50 and everyday words** — guided practice, designed to be learned together
  with a teacher or family member.
- **Games** — Speed Match, quiz games, camera quest, and a sticker reward system.
- **Gesture converter & speech-to-sign** — type or speak a word and watch it finger-spelled.
- **Grown-ups area** — progress backup, daily goal, media attribution, and privacy details.

### The 37 PSL letter handshapes

![PSL handshapes reference](docs/handshapes-reference.png)

## Camera recognition: how it works, and its limits

- Letters are recognized with [Google MediaPipe Hand Landmarker](https://developers.google.com/edge/mediapipe/solutions/vision/hand_landmarker)
  plus a nearest-neighbour classifier trained on real hand landmarks (see below).
- **ق, م and غ are guided practice** — the hand detector cannot see those handshapes
  reliably, so the app never pretends to grade them.
- Numbers and word signs need movement or both hands, so they are guided practice too.
- A camera match means "the handshape looks like this letter" — it is **not** a measure
  of the child's ability.

## Building the app yourself

1. Open the project in [Android Studio](https://developer.android.com/studio).
2. Let it sync (a `debug.keystore` is included so the debug build signs automatically).
3. Run on an emulator or device, or build with:

   ```
   ./gradlew assembleDebug
   ```

   The APK appears in `app/build/outputs/apk/debug/`.

## Data sources & credits

- **Hand photographs & recognition training data:** Ali Imran Ali (2021),
  *"Data set about hand configuration of Pakistan Sign Language"*, Mendeley Data, V1,
  [doi:10.17632/y9svrbh27n.1](https://data.mendeley.com/datasets/y9svrbh27n/1) —
  **CC BY 4.0**. Accompanying paper: Imran et al. (2021), *Data in Brief* 36, 107021,
  [doi:10.1016/j.dib.2021.107021](https://doi.org/10.1016/j.dib.2021.107021).
  Photographs are used unmodified apart from resizing; the classifier was trained on
  hand landmarks extracted from the same dataset.
- **PSL learning resources:** [Deaf Reach / PSL Dictionary](https://psl.org.pk/) —
  their videos are *not* bundled; the app links to them for guided practice.
- Reproducible training pipeline: `tools/train_psl_landmarks.py`;
  evaluation results in `docs/recognition-evaluation.json`;
  design and licensing notes in `docs/RESEARCH.md`.

## Sign language choice

This edition teaches **Pakistan Sign Language**. Signs can differ between regions and
between schools — always confirm the signs a child learns here with her family and a
fluent Deaf educator. A photograph can support a teacher; it cannot replace one.
