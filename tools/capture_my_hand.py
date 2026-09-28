"""Capture YOUR own hand to personalize Irssa's sign recognition.

Webcam tool: it shows a letter, you sign it, press SPACE to save a frame.
Landmarks are extracted on the spot and stored in tools/my-hand-landmarks.npz,
which tools/train_hybrid.py merges into the shipped models.

Usage:
  tools/venv/Scripts/python.exe tools/capture_my_hand.py psl     # Urdu alphabet
  tools/venv/Scripts/python.exe tools/capture_my_hand.py english # English alphabet

Keys: SPACE = save sample | N = next letter | Q = quit
Aim for 15+ samples per letter, vary angle and distance a little.
"""
import sys
from pathlib import Path

import cv2
import numpy as np
import mediapipe as mp

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app' / 'src' / 'main' / 'assets'
OUT = ROOT / 'tools' / 'my-hand-landmarks.npz'

PSL = ['ا','ب','پ','ت','ٹ','ث','ج','چ','ح','خ','د','ڈ','ذ','ر','ڑ','ز','ژ','س','ش','ص','ض',
       'ط','ظ','ع','غ','ف','ق','ک','گ','ل','م','ن','و','ہ','ی','ے','ء']
PSL_IDS = ['alif','be','pe','te','tte','se','jim','che','he','khe','dal','dhal','zal','re','rre',
           'ze','zhe','seen','sheen','swad','zwad','toay','zoay','ain','ghain','fay','qaf','kaf',
           'gaf','laam','meem','noon','vao','hay','chhoti_yeh','bari_yeh','hamza']
ENGLISH = list('ABCDFGHIKLMNOPQRSTUVWXY')  # J/Z need motion; E is tracked too -> full list below
ENGLISH = list('ABCDEFGHIKLMNOPQRSTUVWXY')

def normalize(points):
    p = np.array(points, dtype=np.float32).reshape(21, 3)
    p = p - p[0]
    if p[5, 0] < p[17, 0]:
        p[:, 0] *= -1
    scale = np.max(np.linalg.norm(p, axis=1))
    return (p / max(float(scale), 1e-6)).flatten()

def load_existing():
    if OUT.exists():
        d = np.load(OUT, allow_pickle=True)
        return list(d['X']), list(d['y']), list(d['hand'])
    return [], [], []

def main():
    language = sys.argv[1] if len(sys.argv) > 1 else 'psl'
    if language == 'psl':
        chars, ids, title = PSL, PSL_IDS, 'PSL (Urdu)'
    elif language == 'english':
        chars, ids, title = ENGLISH, ENGLISH, 'English (ASL)'
    else:
        raise SystemExit('choose "psl" or "english"')

    X, y, hand = load_existing()
    have = {c: sum(1 for v in y if v == c) for c in ids}
    print(f'Already captured: ' + ', '.join(f'{c}:{n}' for c, n in have.items() if n))

    options = mp.tasks.vision.HandLandmarkerOptions(
        base_options=mp.tasks.BaseOptions(model_asset_path=str(ASSETS / 'models/hand_landmarker.task')),
        num_hands=1, min_hand_detection_confidence=0.5, min_hand_presence_confidence=0.5)
    detector = mp.tasks.vision.HandLandmarker.create_from_options(options)
    cam = cv2.VideoCapture(0)
    if not cam.isOpened():
        raise SystemExit('No webcam found.')

    idx = 0
    print(f'Training {title}: {len(ids)} letters. SPACE=save, N=next letter, Q=quit.')
    while True:
        char, cid = chars[idx], ids[idx]
        ok, frame = cam.read()
        if not ok:
            continue
        frame = cv2.flip(frame, 1)
        rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        result = detector.detect(mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb))
        count = sum(1 for v in y if v == cid)
        overlay = f'{char}  ({cid})  saved: {count}'
        hint = 'Show your hand like the app photo. SPACE=save  N=next letter  Q=quit'
        if result.hand_landmarks:
            pts = [[p.x, p.y, p.z] for p in result.hand_landmarks[0]]
            feats = normalize(pts)
            mp_draw = np.array([[p.x * frame.shape[1], p.y * frame.shape[0]] for p in result.hand_landmarks[0]], dtype=np.int32)
            cv2.polylines(frame, [mp_draw], False, (0, 255, 120), 2)
            for pt in mp_draw:
                cv2.circle(frame, tuple(pt), 3, (255, 200, 0), -1)
            hint = 'Hand locked. SPACE=save sample'
        else:
            hint = 'No hand seen - more light, hand fully in frame'
        cv2.putText(frame, overlay, (20, 50), cv2.FONT_HERSHEY_SIMPLEX, 1.4, (0, 0, 0), 6)
        cv2.putText(frame, overlay, (20, 50), cv2.FONT_HERSHEY_SIMPLEX, 1.4, (80, 255, 80), 2)
        cv2.putText(frame, hint, (20, frame.shape[0] - 20), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 2)
        cv2.imshow(f'Irssa hand capture - {title}', frame)
        key = cv2.waitKey(1) & 0xFF
        if key == ord('q') or key == 27:
            break
        elif key == ord('n'):
            idx = (idx + 1) % len(chars)
        elif key == ord(' ') and result.hand_landmarks:
            X.append(feats); y.append(cid); hand.append('MYHAND')
    detector.close(); cam.release(); cv2.destroyAllWindows()
    np.savez_compressed(OUT, X=np.asarray(X, dtype=np.float32),
                        y=np.asarray(y), hand=np.asarray(hand))
    counts = {c: sum(1 for v in y if v == c) for c in ids}
    print('Saved', OUT)
    print('Per letter:', counts)
    print('Now run: tools/venv/Scripts/python.exe tools/train_hybrid.py')

if __name__ == '__main__':
    main()
