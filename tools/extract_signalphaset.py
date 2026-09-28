"""Extract landmarks from SignAlphaSet (CC BY 4.0, Mendeley 8fmvr9m98w) - 26k ASL images."""
from pathlib import Path
import io, collections, zipfile
import numpy as np
import mediapipe as mp
from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app' / 'src' / 'main' / 'assets'
OUT = ROOT / 'tools' / 'signalphaset-landmarks.npz'
PER_CLASS = 200
SKIP = {'J', 'Z'}  # motion letters never graded by a static model

def normalize(points):
    p = np.array(points, dtype=np.float32).reshape(21, 3)
    p = p - p[0]
    if p[5, 0] < p[17, 0]:
        p[:, 0] *= -1
    scale = np.max(np.linalg.norm(p, axis=1))
    if scale < 1e-6:
        return None
    return (p / scale).flatten()

outer = zipfile.ZipFile(ROOT / 'tools' / 'signalphaset.zip')
inner = zipfile.ZipFile(io.BytesIO(outer.read('SignAlphaSet/SignAlphaSet.zip')))
names = [n for n in inner.namelist() if n.lower().endswith('.jpg') and len(n.split('/')) >= 3]
by_class = collections.defaultdict(list)
for n in names:
    cls = n.split('/')[1].upper()
    if cls not in SKIP:
        by_class[cls].append(n)

options = mp.tasks.vision.HandLandmarkerOptions(
    base_options=mp.tasks.BaseOptions(model_asset_path=str(ASSETS / 'models/hand_landmarker.task')),
    num_hands=1, min_hand_detection_confidence=0.4, min_hand_presence_confidence=0.4)
X, y, groups, failures = [], [], [], collections.Counter()
with mp.tasks.vision.HandLandmarker.create_from_options(options) as det:
    for cls in sorted(by_class):
        picked = by_class[cls][::5][:PER_CLASS]  # spread across the capture order
        for i, n in enumerate(picked):
            img = ImageOps.expand(Image.open(io.BytesIO(inner.read(n))).convert('RGB'), border=40, fill='black')
            r = det.detect(mp.Image(image_format=mp.ImageFormat.SRGB, data=np.asarray(img)))
            if r.hand_landmarks:
                pts = [[p.x, p.y, p.z] for p in r.hand_landmarks[0]]
                f = normalize(pts)
                if f is not None:
                    X.append(f); y.append(cls); groups.append(n.split('/')[2].split('_')[0])
            else:
                failures[cls] += 1
        print(cls, 'accepted', sum(1 for v in y if v == cls), flush=True)

np.savez_compressed(OUT, X=np.asarray(X, dtype=np.float32), y=np.asarray(y), groups=np.asarray(groups))
(ROOT / 'tools' / 'signalphaset-failures.json').write_text(json.dumps(dict(failures), indent=1))
print('total', len(X), dict(sorted(collections.Counter(y).items())))
