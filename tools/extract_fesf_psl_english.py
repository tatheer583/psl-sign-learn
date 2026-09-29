"""Extract landmarks from the FESF/psl.org.pk PSL English-alphabet dataset.

Local-use training only: the source is (c) FESF, all rights reserved - the derived
model must NOT be redistributed (no public GitHub push) without FESF permission.
"""
from pathlib import Path
import collections, json
import numpy as np
import mediapipe as mp
from PIL import Image, ImageOps

SRC = Path('C:/Users/1/Downloads/PSL_Alphabet_A-Z/hands')
OUT = Path(__file__).resolve().parent / 'fesf-psl-english-landmarks.npz'
SKIP = {'J', 'Z'}

def normalize(points):
    p = np.array(points, dtype=np.float32).reshape(21, 3)
    p = p - p[0]
    if p[5, 0] < p[17, 0]:
        p[:, 0] *= -1
    scale = np.max(np.linalg.norm(p, axis=1))
    if scale < 1e-6:
        return None
    return (p / scale).flatten()

options = mp.tasks.vision.HandLandmarkerOptions(
    base_options=mp.tasks.BaseOptions(model_asset_path=str(Path(__file__).resolve().parents[1] / 'app/src/main/assets/models/hand_landmarker.task')),
    num_hands=1, min_hand_detection_confidence=0.3, min_hand_presence_confidence=0.3)
X, y, groups, failures = [], [], [], collections.Counter()
with mp.tasks.vision.HandLandmarker.create_from_options(options) as det:
    paths = sorted(SRC.glob('*/*.jpg'))
    for i, p in enumerate(paths):
        cls = p.parent.name.upper()
        if cls in SKIP:
            continue
        img = ImageOps.expand(Image.open(p).convert('RGB'), border=30, fill='black')
        r = det.detect(mp.Image(image_format=mp.ImageFormat.SRGB, data=np.asarray(img)))
        if r.hand_landmarks:
            f = normalize([[q.x, q.y, q.z] for q in r.hand_landmarks[0]])
            if f is not None:
                X.append(f); y.append(cls)
                groups.append(p.stem.split('_')[1])  # sign1 / sign2
        else:
            failures[cls] += 1
        if i % 200 == 0:
            print(i, len(paths), 'accepted', len(X), flush=True)

np.savez_compressed(OUT, X=np.asarray(X, dtype=np.float32), y=np.asarray(y), groups=np.asarray(groups))
json.dump(dict(failures), open(OUT.with_name('fesf-failures.json'), 'w'), indent=1)
print('total', len(X), dict(sorted(collections.Counter(y).items())))
print('detection failures', dict(failures))
