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

- **Two alphabets, clearly separated:**
  - **Urdu (PSL): 37 letters** — real hand photographs, step-by-step cues, Urdu words
    (ا for انار, ب for بکری…).
  - **English (ASL): 26 letters** — real hand photographs and English words, taught as a
    *different* language from PSL, never mixed.
- **Camera practice** — an offline hand-recognition coach checks all 37 static PSL
  handshapes and 24 static English letters in real time. Recognition is trained on real
  data from **independent signers** and only confirms a sign held for 1.2 seconds.
- **Numbers 0–50 and everyday words** — guided practice, designed to be learned together
  with a teacher or family member.
- **Personalize it with your own hand** (optional, on a computer with a webcam):

  ```
  tools/venv/Scripts/python.exe tools/capture_my_hand.py psl      # then: english
  tools/venv/Scripts/python.exe tools/train_hybrid.py
  ```

  Sign each letter a few times in front of your webcam; the model is retrained with
  your hand weighted heavily, which is the single biggest accuracy upgrade possible.
- **Games** — Speed Match, quiz games, camera quest, and a sticker reward system.
- **Gesture converter & speech-to-sign** — type or speak a word and watch it finger-spelled.
- **Grown-ups area** — progress backup, daily goal, media attribution, and privacy details.

### The 37 PSL letter handshapes

![PSL handshapes reference](docs/handshapes-reference.png)

## Camera recognition: how it works, and its limits

- Letters are recognized with [Google MediaPipe Hand Landmarker](https://developers.google.com/edge/mediapipe/solutions/vision/hand_landmarker)
  plus a nearest-neighbour classifier trained on real hand landmarks (see below).
- **English J and Z are guided practice** — they need motion, so a static model never
  grades them. Numbers and word signs need movement or both hands.
- The acceptance gate is deliberately strict (tuned on held-out data): it prefers saying
  "not yet" over a wrong "You did it!".
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

- **Urdu/PSL hand photographs (signer 1):** Ali Imran Ali (2021),
  *"Data set about hand configuration of Pakistan Sign Language"*, Mendeley Data, V1,
  [doi:10.17632/y9svrbh27n.1](https://data.mendeley.com/datasets/y9svrbh27n/1) —
  **CC BY 4.0**. Accompanying paper: Imran et al. (2021), *Data in Brief* 36, 107021,
  [doi:10.1016/j.dib.2021.107021](https://doi.org/10.1016/j.dib.2021.107021).
  Photographs are used unmodified apart from resizing; the classifier was trained on
  hand landmarks extracted from the same dataset.
- **PSL gesture landmarks (signer 2):** [Bakhtyar12/Pakistani-Sign-Language](https://huggingface.co/datasets/Bakhtyar12/Pakistani-Sign-Language),
  Hugging Face, **MIT** — MediaPipe landmark sequences for all Urdu letters, used to make
  recognition robust across different hands.
- **English letters:** Ayush Thakur, [ASL Dataset](https://www.kaggle.com/datasets/ayuraj/asl-dataset) (**CC0**)
  and [SignAlphaSet](https://data.mendeley.com/datasets/8fmvr9m98w/1) (Mendeley, **CC BY 4.0**).
- **PSL learning resources:** [Deaf Reach / PSL Dictionary](https://psl.org.pk/) —
  their videos are *not* bundled; the app links to them for guided practice.
- Reproducible training pipeline: `tools/train_psl_landmarks.py`;
  evaluation results in `docs/recognition-evaluation.json`;
  design and licensing notes in `docs/RESEARCH.md`.

## Sign language choice

This edition teaches **Pakistan Sign Language**. Signs can differ between regions and
between schools — always confirm the signs a child learns here with her family and a
fluent Deaf educator. A photograph can support a teacher; it cannot replace one.
