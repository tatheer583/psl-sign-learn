"""Extract landmarks from grassknoted/asl-alphabet (Kaggle, GPL 2 - LOCAL USE ONLY).

87,000 colour photos of the ASL alphabet from 4 signers with varied backgrounds.
Derived model must not be pushed publicly (GPL obligations) - see RESEARCH.md.
"""
from pathlib import Path
import collections, json, sys
import numpy as np
import mediapipe as mp
from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parents[1]
BASE = Path(sys.argv[1]) if len(sys.argv) > 1 else None
OUT = ROOT / 'tools' / 'kaggle-asl-landmarks.npz'
PER_CLASS = 250
SKIP = {'J', 'Z', 'del', 'nothing', 'space'}

def normalize(points):
    p = np.array(points, dtype=np.float32).reshape(21, 3)
    p = p - p[0]
    if p[5, 0] < p[17, 0]:
        p[:, 0] *= -1
    scale = np.max(np.linalg.norm(p, axis=1))
    if scale < 1e-6:
        return None
    return (p / scale).flatten()

if BASE is None:
    # auto-find under the kagglehub cache
    cands = sorted(Path('A:/temp/kagglehub/datasets').glob('*/grassknoted/asl-alphabet/*'))
    BASE = cands[0]
train = BASE / 'asl_alphabet_train' / 'asl_alphabet_train'
classes = sorted(d.name for d in train.iterdir() if d.is_dir() and d.name not in SKIP)
print('classes:', classes)

options = mp.tasks.vision.HandLandmarkerOptions(
    base_options=mp.tasks.BaseOptions(model_asset_path=str(ROOT / 'app/src/main/assets/models/hand_landmarker.task')),
    num_hands=1, min_hand_detection_confidence=0.4, min_hand_presence_confidence=0.4)
X, y, groups, failures = [], [], [], collections.Counter()
with mp.tasks.vision.HandLandmarker.create_from_options(options) as det:
    for cls in classes:
        imgs = sorted((train / cls).glob('*.jpg'))[::4][:PER_CLASS]
        for i, p in enumerate(imgs):
            img = Image.open(p).convert('RGB')
            r = det.detect(mp.Image(image_format=mp.ImageFormat.SRGB, data=np.asarray(img)))
            if r.hand_landmarks:
                f = normalize([[q.x, q.y, q.z] for q in r.hand_landmarks[0]])
                if f is not None:
                    X.append(f); y.append(cls)
                    groups.append(p.stem.split('_')[0])  # signer batch
            else:
                failures[cls] += 1
        print(cls, sum(1 for v in y if v == cls), flush=True)

np.savez_compressed(OUT, X=np.asarray(X, dtype=np.float32), y=np.asarray(y), groups=np.asarray(groups))
json.dump(dict(failures), open(OUT.with_name('kaggle-asl-failures.json'), 'w'), indent=1)
print('total', len(X), dict(sorted(collections.Counter(y).items())))
