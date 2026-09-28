"""Train the hybrid Irssa recognition models from every real data source.

PSL model (psl.bin):
  - Mendeley webcam dataset (doi:10.17632/y9svrbh27n.1, CC BY 4.0) - signer A
  - Hugging Face PSL gesture dataset (Bakhtyar12, MIT)             - signer B
  - Your own hand  (tools/my-hand-landmarks.npz, capture_my_hand.py) - weighted x5
  Cross-source evaluation (train on one signer, test on the other) reports the
  honest cross-person accuracy that the single-source baseline could not.

English model (english.bin):
  - CC0 ASL Kaggle dataset landmarks (tools/landmarks.npz, 5 hand groups)
  - SignAlphaSet (Mendeley 8fmvr9m98w, CC BY 4.0) - extra signers
  - Your own hand, weighted x5
  Person-independent evaluation holds out the whole hand5 group.

Optionally SignAlphaSet (CC BY 4.0) landmarks are merged when present.
"""
from pathlib import Path
import collections, hashlib, json, struct
import numpy as np
from sklearn.neighbors import KNeighborsClassifier
from sklearn.metrics import accuracy_score

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app' / 'src' / 'main' / 'assets'
REPORT = ROOT / 'docs' / 'recognition-evaluation.json'

PERSONAL_WEIGHT = 5          # each personal sample enters training this many times
PER_CLASS_CAP = 120          # keep runtime kNN small and class-balanced
RNG = np.random.default_rng(2026)

def rotate_jitter(x, degrees=8.0):
    """Small in-plane rotation augmentation around the wrist (already centred)."""
    t = np.deg2rad(RNG.uniform(-degrees, degrees))
    c, s = np.cos(t), np.sin(t)
    out = x.copy()
    out[0::3] = x[0::3] * c - x[1::3] * s
    out[1::3] = x[0::3] * s + x[1::3] * c
    return out

def load(path):
    d = np.load(path)
    return d['X'], d['y'], (d['groups'] if 'groups' in d else np.array(['?'] * len(d['y'])))

def balance(X, y, cap):
    keep = []
    for lbl in sorted(set(y)):
        idx = np.where(y == lbl)[0]
        if len(idx) > cap:
            idx = RNG.choice(idx, cap, replace=False)
        keep.extend(idx)
    keep.sort()
    return X[keep], y[keep]

def evaluate(name, sources, eval_spec):
    """sources: list of (X, y, group, weight). eval_spec: (train_mask_fn, test_mask_fn) per source."""
    X = np.vstack([s[0] for s in sources]); y = np.concatenate([s[1] for s in sources])
    g = np.concatenate([s[2] for s in sources]); w = np.concatenate([np.full(len(s[0]), s[3]) for s in sources])
    train = w == 0  # placeholder replaced below
    train = np.ones(len(y), bool)
    for spec, s in zip(eval_spec, sources):
        m_train, m_test = spec(s[2])
        train[m_train] = True
        train[m_train] = m_train
        train &= ~m_test if m_test is not None else True
    test = ~train
    model = KNeighborsClassifier(n_neighbors=5, weights='distance').fit(X[train], y[train])
    pred = model.predict(X[test])
    return float(accuracy_score(y[test], pred)), int(train.sum()), int(test.sum())

# ---------------------------------------------------------------- PSL model
def build_psl(report):
    mX, my, mg = load(ROOT / 'tools' / 'psl-landmarks.npz')
    hX, hy, hg = load(ROOT / 'tools' / 'hf-psl-landmarks.npz')
    hX, hy = balance(hX, hy, PER_CLASS_CAP)
    # rotation augmentation doubles both sources
    mA_X = np.vstack([mX, np.stack([rotate_jitter(v) for v in mX])])
    mA_y = np.concatenate([my, my])
    hA_X = np.vstack([hX, np.stack([rotate_jitter(v) for v in hX])])
    hA_y = np.concatenate([hy, hy])
    sources = [(mA_X, mA_y, np.array(['MSA'] * len(mA_y)), 1),
               (hA_X, hA_y, np.array(['HF'] * len(hA_y)), 1)]
    personal = ROOT / 'tools' / 'my-hand-landmarks.npz'
    if personal.exists():
        pX, py, ph = load(personal)
        keep = np.isin(py, sorted(set(my) & set(hy)))
        pX, py, ph = pX[keep], py[keep], ph[keep]
        if len(pX):
            sources.append((np.vstack([pX] + [pX] * (PERSONAL_WEIGHT - 1)),
                            np.concatenate([py] * PERSONAL_WEIGHT), ph, 1))
            print('personal PSL samples:', len(pX), 'x', PERSONAL_WEIGHT)
    # honest cross-person evaluation: train signer A -> test signer B, and vice versa
    mXe, mye = balance(mX, my, 999)
    hXe, hye = balance(hX, hy, 60)
    model_a = KNeighborsClassifier(n_neighbors=5, weights='distance').fit(mXe, mye)
    acc_a_to_b = float(accuracy_score(hye, model_a.predict(hXe)))
    model_b = KNeighborsClassifier(n_neighbors=5, weights='distance').fit(hXe, hye)
    acc_b_to_a = float(accuracy_score(mye, model_b.predict(mXe)))
    print('cross-person PSL: Mendeley->HF', round(acc_a_to_b, 3), '| HF->Mendeley', round(acc_b_to_a, 3))
    # final model: everything
    X = np.vstack([s[0] for s in sources]); y = np.concatenate([s[1] for s in sources])
    labels = sorted(set(y)); idx = {v: i for i, v in enumerate(labels)}
    path = ASSETS / 'models' / 'psl.bin'
    with path.open('wb') as f:
        f.write(struct.pack('>iii', 0x49525353, len(X), 63))
        f.write(struct.pack('>i', len(labels)))
        for lbl in labels:
            b = lbl.encode(); f.write(struct.pack('>H', len(b))); f.write(b)
        for vec, lbl in zip(X, y):
            f.write(struct.pack('>i', idx[lbl]))
            f.write(struct.pack('>63f', *vec))
    report['evaluation']['psl'] = {
        'sources': ['Mendeley doi:10.17632/y9svrbh27n.1 (CC BY 4.0), signer A',
                    'Hugging Face Bakhtyar12/Pakistani-Sign-Language (MIT), signer B',
                    'personal capture (capture_my_hand.py) x%d' % PERSONAL_WEIGHT],
        'crossPersonAccuracy': {'mendeley_to_hf': acc_a_to_b, 'hf_to_mendeley': acc_b_to_a},
        'trainSamples': len(X), 'classes': labels,
        'note': 'Cross-person numbers are the honest generalization estimate; the shipped model trains on both signers plus personal data.',
        'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}
    print('psl.bin:', len(X), 'samples,', len(labels), 'classes')

# ------------------------------------------------------------- English model
def build_english(report):
    eX, ey, eg = load(ROOT / 'tools' / 'landmarks.npz')
    letters = sorted(v for v in set(ey) if v.isalpha())
    keep = np.isin(ey, letters)
    eX, ey, eg = eX[keep], ey[keep], eg[keep]
    test = eg == 'hand5'
    train = ~test
    if test.sum() == 0:
        raise ValueError('no hand5 holdout')
    model = KNeighborsClassifier(n_neighbors=5, weights='distance').fit(eX[train], ey[train])
    person_indep = float(accuracy_score(ey[test], model.predict(eX[test])))
    print('English person-independent CC0-only (hand5 holdout):', round(person_indep, 3))
    sources = [(eX, ey, eg, 1)]
    sas = ROOT / 'tools' / 'signalphaset-landmarks.npz'
    if sas.exists():
        aX, ay, ag = load(sas)
        aX, ay = balance(aX, ay, 150)
        sources.append((aX, ay, np.array(['SAS'] * len(ay)), 1))
        print('SignAlphaSet samples:', len(ay))
    personal = ROOT / 'tools' / 'my-hand-landmarks.npz'
    if personal.exists():
        pX, py, ph = load(personal)
        keep = np.isin(py, letters)
        pX, py, ph = pX[keep], py[keep], ph[keep]
        if len(pX):
            sources.append((np.vstack([pX] + [pX] * (PERSONAL_WEIGHT - 1)),
                            np.concatenate([py] * PERSONAL_WEIGHT), ph, 1))
            print('personal English samples:', len(pX), 'x', PERSONAL_WEIGHT)
    X = np.vstack([s[0] for s in sources]); y = np.concatenate([s[1] for s in sources])
    aug_X = np.stack([rotate_jitter(v) for v in X])
    X = np.vstack([X, aug_X]); y = np.concatenate([y, y])
    labels = sorted(set(y)); idx = {v: i for i, v in enumerate(labels)}
    # second honest check: person-independent holdout re-scored with the full model family
    model_full_eval = KNeighborsClassifier(n_neighbors=5, weights='distance').fit(
        np.vstack([eX[train], aX]) if sas.exists() else eX[train],
        np.concatenate([ey[train], ay]) if sas.exists() else ey[train])
    person_indep_full = float(accuracy_score(ey[test], model_full_eval.predict(eX[test])))
    print('English person-independent with SignAlphaSet (hand5 holdout):', round(person_indep_full, 3))
    path = ASSETS / 'models' / 'english.bin'
    with path.open('wb') as f:
        f.write(struct.pack('>iii', 0x49525353, len(X), 63))
        f.write(struct.pack('>i', len(labels)))
        for lbl in labels:
            b = lbl.encode(); f.write(struct.pack('>H', len(b))); f.write(b)
        for vec, lbl in zip(X, y):
            f.write(struct.pack('>i', idx[lbl]))
            f.write(struct.pack('>63f', *vec))
    report['evaluation']['english'] = {
        'sources': ['CC0 ASL Kaggle dataset (hand groups hand1-hand5)',
                    'personal capture x%d' % PERSONAL_WEIGHT],
        'personIndependentAccuracy': person_indep,
        'personIndependentAccuracyWithSignAlphaSet': person_indep_full,
        'heldOutGroup': 'hand5', 'trainSamples': len(X), 'classes': labels,
        'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}
    print('english.bin:', len(X), 'samples,', len(labels), 'classes')

report = json.loads(REPORT.read_text()) if REPORT.exists() else {}
report['hybrid'] = {'trained': '2026-09-29', 'gate': 'agreement>=0.95, rms<=0.07'}
build_psl(report)
build_english(report)
REPORT.write_text(json.dumps(report, indent=2))
print('done')
