"""Extract single-hand landmarks from the Hugging Face PSL gesture dataset.

Source: Bakhtyar12/Pakistani-Sign-Language (MIT). Sequences are 30 frames of
MediaPipe holistic output (21 left-hand + 21 right-hand + 33 pose landmarks).
For every letter sequence we take the dominant hand's 21 landmarks per frame and
apply the same wrist-centred, scale-normalized transform as the app classifier.
"""
from pathlib import Path
import io, collections
import numpy as np
import zipfile

ROOT = Path(__file__).resolve().parents[1]
ZIP = ROOT / 'tools' / 'psl-hf-landmarks.zip'
OUT = ROOT / 'tools' / 'hf-psl-landmarks.npz'

FOLDER_TO_ID = {
    'Alif': 'alif', 'Bay': 'be', 'Pay': 'pe', 'Tay': 'te', 'Ttay': 'tte', 'Say': 'se',
    'Jeem': 'jim', 'Chay': 'che', 'He': 'he', 'Khay': 'khe', 'Dal': 'dal', 'Ddal': 'dhal',
    'Zaal': 'zal', 'Ray': 're', 'rray': 'rre', 'zay': 'ze', 'seen': 'seen', 'Sheen': 'sheen',
    'suaad': 'swad', 'Zuaad': 'zwad', 'Toain': 'toay', 'Zoain': 'zoay', 'Ain': 'ain',
    'Ghain': 'ghain', 'Fay': 'fay', 'Qaaf': 'qaf', 'Kaaf': 'kaf', 'Gaaf': 'gaf',
    'Laam': 'laam', 'Meem': 'meem', 'Noon': 'noon', 'Wao': 'vao', 'Hay': 'hay',
    'Ye': 'chhoti_yeh', 'Choti-ye': 'chhoti_yeh', 'bari-ye': 'bari_yeh', 'Hamza': 'hamza',
}

def normalize(p):
    p = np.asarray(p, dtype=np.float32).reshape(21, 3)
    p = p - p[0]
    if p[5, 0] < p[17, 0]:
        p[:, 0] *= -1
    scale = np.max(np.linalg.norm(p, axis=1))
    if scale < 1e-6:
        return None
    return (p / scale).flatten()

def hand_block(frame):
    """Return the dominant hand's 21x3 landmarks for one 225-vector frame, or None."""
    left = frame[0:63].reshape(21, 3)
    right = frame[63:126].reshape(21, 3)
    ls = np.linalg.norm(left - left[0], axis=1).max() if np.isfinite(left).all() else 0.0
    rs = np.linalg.norm(right - right[0], axis=1).max() if np.isfinite(right).all() else 0.0
    if max(ls, rs) < 1e-4:
        return None, None
    return (left, 'L') if ls >= rs else (right, 'R')

X, y, groups = [], [], []
with zipfile.ZipFile(ZIP) as z:
    names = [n for n in z.namelist() if n.endswith('.npy')]
    by_class = collections.defaultdict(list)
    for n in names:
        parts = n.split('/')
        if len(parts) >= 3 and parts[1] in FOLDER_TO_ID:
            by_class[FOLDER_TO_ID[parts[1]]].append(n)
    print('classes found:', len(by_class), dict(sorted(collections.Counter(by_class).items())[:5]), '...')
    for cid in sorted(by_class):
        for seq_name in sorted(by_class[cid]):
            seq = np.load(io.BytesIO(z.read(seq_name)))
            if seq.shape != (30, 225):
                continue
            # choose the hand that dominates the sequence, then keep mid-sequence frames
            hand_counts = collections.Counter()
            frames = []
            for f in seq:
                h, side = hand_block(f)
                if h is not None:
                    hand_counts[side] += 1
                    frames.append(h)
            if not frames or max(hand_counts.values()) < 15:
                continue
            side = hand_counts.most_common(1)[0][0]
            kept = []
            for f in seq:
                h, side_f = hand_block(f)
                if h is not None and side_f == side:
                    kept.append(h)
            take = kept[:: max(1, len(kept) // 10)][:10]
            for h in take:
                feats = normalize(h)
                if feats is not None:
                    X.append(feats); y.append(cid); groups.append('HF')
    print('extracted:', len(X), dict(sorted(collections.Counter(y).items())))

np.savez_compressed(OUT, X=np.asarray(X, dtype=np.float32), y=np.asarray(y), groups=np.asarray(groups))
print('saved', OUT)
