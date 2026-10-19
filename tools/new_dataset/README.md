# New Kaggle PSL dataset — extraction & training

Works on the local copy of **Kaggle: saadbutt321/pakistan-sign-language-dataset**
(`Downloads/sign-language-database`) without any Python — everything is Node.

## What the dataset is
- `main_dataset.db` (SQLite): OpenPose 21-point hand coordinates per row
  (`rightHandDataset` letters 5,112 rows; `wordDataset` words 706 rows), pixel x/y, no confidence, no depth.
- `PSL_dataset/datasets/{alphabets,words}_dataset/<label>/<session>/*_keypoints.json`:
  full OpenPose frames (`people[]` with `hand_left/right_keypoints_2d`, x,y,confidence).

## Quirks handled here
- Folder names are **CP437 mojibake** (zip extracted without UTF-8 flags) —
  `lib/cp437.mjs` restores them (`DA AF E2 80 AC` glyphs -> `گ` + U+202C).
- Direction controls (U+202C etc.) and Persian/Arabic ye variants are stripped/folded.
- Rows where OpenPose wrote the (150,150) placeholder are skipped; JSON hands are
  picked by mean keypoint confidence (>= 0.45) and side.
- No depth channel: z is stored 0 — for k-NN ranking that is a per-candidate
  constant or a per-query constant, never both varying, so ordering is preserved.
- Feature transform is *exactly* the app's `LandmarkFeatures.normalize`
  (wrist-centred, mirror by index-vs-pinky MCP x order, divide by max norm),
  verified frame-geometry-wise in a probe (OpenPose joint roles == MediaPipe roles).

## Files produced (in `tools/`)
| file | content |
|---|---|
| `new-psl-landmarks.npz` | X float32 [n,63], y letter ids, groups — python-loadable |
| `new-word-fixtures.json` | sampled frames for the 12 common words (guided practice) |
| `new-dataset-report.json` | per-class counts + provenance |

## Reproduce
```sh
npm install        # sql.js, jszip, iconv-lite
node extract_new_dataset.mjs      # -> npz + fixtures + report
node train_psl_hybrid.mjs 450     # -> new psl.bin (app + web) + docs update
node validate_bin.mjs             # runs the shipped bin through the web parser + classifier
```

## Honesty rules (leak-proof evaluation)
- Exact duplicates are removed **before** any split; rotation augmentation and
  the NEW x5 weighting exist only in the shipped bin, never in evaluation pools.
- The "previous model" source is read from `git show HEAD:app/.../psl.bin`, so
  re-running cannot feed its own output back in.
- Cross-person numbers also cross capture systems (MediaPipe <-> 2D OpenPose),
  so they are conservative lower bounds (see `docs/recognition-evaluation.json`).

Word signs are **not** mixed into the letter classifier (they are motion signs);
they stay as guided fixtures per the app's honesty policy.
