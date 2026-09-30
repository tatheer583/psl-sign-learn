# Irssa implementation research — 28 September 2026 (PSL edition)

## Decisions

The family confirmed the curriculum should use **Pakistan Sign Language (PSL)**. The whole edition is rebuilt around PSL: the 37-letter Urdu alphabet curriculum, handshape photos, and the on-device recognition model. UI text is English; the communication board optionally adds Urdu. Nothing in the app presents ASL content as PSL.

### Teaching media

- **Real hand photos and recognition data:** Ali Imran Ali (2021), ["Data set about hand configuration of Pakistan Sign Language"](https://data.mendeley.com/datasets/y9svrbh27n/1), Mendeley Data, V1, doi:10.17632/y9svrbh27n.1, **CC BY 4.0**. 37 classes named by Urdu letter, webcam captures at MNS University of Agriculture Multan (dataset accompanying Imran et al., *Data in Brief* 36, 107021, doi:10.1016/j.dib.2021.107021). 1,509 images extracted; several classes carry 50 images.
- Photographs are bundled unmodified apart from resizing (one representative, brightness-selected image per letter, `app/src/main/assets/signs/letter_<class>.jpg`). Attribution appears in the grown-up area and in `media_manifest.json`.
- **Numbers and everyday words:** no redistributable PSL number/word media was found. The previous ASL Signbank videos (CC BY-NC-SA 4.0) were removed from assets and archived to `tools/asl-media-archive/` — they teach a different language and must not ship in a PSL edition. Number and vocabulary lessons now use guided practice; `media_manifest.json` records why no media is bundled and links to [Deaf Reach / PSL Dictionary](https://psl.org.pk/), whose videos are not copied without redistribution permission.
- Fingerspelling English input in the converter uses an approximate Latin→Urdu transliteration (documented in `Spelling`); Urdu spellings should be preferred whenever known.

### Recognition

- [Google MediaPipe Hand Landmarker](https://developers.google.com/edge/mediapipe/solutions/vision/hand_landmarker) detects 21 hand landmarks. That model detects hands; a separate classifier is required to recognize signs.
- The official float16 hand landmark task is packaged in the APK. Camera frames remain on device and are not stored.
- The app's nearest-neighbour classifier uses landmarks extracted from the CC BY 4.0 PSL dataset. Inputs are wrist-centred, scale-normalized and reflected into a common hand orientation. Kotlin and Python implement the same transformation. Reproducible extraction and evaluation: `tools/train_psl_landmarks.py`, dataset in `tools/psl-dataset/`.
- **Multi-signer recognition is the anti-hallucination measure.** The single Mendeley signer produced a model whose honest cross-person accuracy was only ~0.36: trained on signer A it failed on signer B — the "hallucination" reported in use. The Hugging Face landmark dataset (MIT) supplies a second independent signer for every letter, including ق, م and غ, which the dark Mendeley photos had blocked. All **37 classes** are now trained on both signers.
- **No signer grouping survives** in this dataset (capture codes repeat only inside single classes), so the person-independent holdout used for the ASL baseline is impossible. Evaluation uses the source paper's stratified split (every 5th file per class) **plus a near-duplicate diagnostic**: accuracy excluding test points with an extremely close same-class training neighbour.
- Cross-person evaluation (in `docs/recognition-evaluation.json`): Mendeley→HF 0.357, HF→Mendeley 0.369 — reported to show why single-signer recognition cannot be trusted for a new user. The shipped model merges both signers plus rotation augmentation; the family can additionally capture the child's own hand with `tools/capture_my_hand.py` (weighted ×5 at training).
- English model: CC0 ASL dataset (5 hand groups) + SignAlphaSet (CC BY 4.0, Mendeley doi:10.17632/8fmvr9m98w.1). Person-independent holdout (hand5): 0.862 CC0-only, 0.847 with SignAlphaSet merged. English J/Z are guided practice.
- **Local-only additional sources (never redistributed, models not committed):** the family may blend in private/rights-restricted data with `tools/train_hybrid.py`, which picks up `tools/fesf-psl-english-landmarks.npz` (psl.org.pk/FESF dictionary crops, © FESF all rights reserved) and `tools/kaggle-asl-landmarks.npz` (grassknoted/asl-alphabet from Kaggle, **GPL 2** — 5,530 landmarks from four signers with varied backgrounds) when present. With both blended the English model trains on ~23,000 samples and keeps the 0.847 person-independent holdout. `datamunge/sign-language-mnist` (CC0) was evaluated and rejected: its 28×28 grayscale images are below what MediaPipe hand detection can read (0 detections on test samples).
- Runtime requires actual hand detection, neighbour agreement, bounded feature distance and an uninterrupted 1.2-second hold. It requires release before repeating a confirmed sign. A classifier score is not represented as calibrated confidence.
- Numbers, compound numbers, word signs and sentence translation are outside the static model's scope. They use guided practice, with separate exploration and camera-match records.

### Family resources and excluded sources

- [Pakistan Sign Language / Deaf Reach](https://psl.org.pk/) provides real signer learning resources. A link is available inside the grown-up area. The platform's videos are not bundled without redistribution permission.
- The ASL sources used by the previous edition (Kaggle ASL dataset, ASL Signbank, CC BY-NC-SA 4.0 media) are no longer bundled. Their media is preserved out-of-app in `tools/asl-media-archive/` for provenance.
- [Lifeprint / ASL University](https://www.lifeprint.com/asl101/pages-layout/permission.htm) permits linking but explicitly restricts using its media to make apps. No Lifeprint photos or videos are copied into Irssa.
- [ASL-LEX](https://asl-lex.org/download.html) distinguishes its research data license from restricted reference video rights. Those videos are not imported.

## Product choices

Real hand photographs demonstrate letter shapes. Visual cues and optional haptics accompany success; sound is never required. Timed games have pause controls. Lessons and the communication board remain accessible without unlocking stars. Local data saves are atomic, rewards are deduplicated by day/sign/activity, and legacy progress is migrated without treating old brightness-based matches as verified mastery.

The parent area provides media attribution, language context, privacy details, optional system speech input information, and progress backup. It uses a light arithmetic gate to reduce accidental access, not authentication.

## Validation still requiring people

Comprehension, comfortable session length and the target device remain to be confirmed with the family. A fluent Deaf educator should review the teaching cues — the handshape descriptions were written from dataset photographs and hedged accordingly — and confirm the sign variants taught. Camera recognition needs physical-device and varied-signer testing. App tests and an emulator cannot establish those outcomes.
