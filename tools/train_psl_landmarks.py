"""PSL (Pakistan Sign Language) alphabet baseline from real CC BY 4.0 hand photographs.
Source: Ali Imran Ali (2021), "Data set about hand configuration of Pakistan Sign Language",
Mendeley Data, V1, doi:10.17632/y9svrbh27n.1 — 37 static Urdu-alphabet handshapes,
webcam captures (MNS University of Agriculture Multan), CC BY 4.0.
Groups by capture prefix for honest held-out evaluation; exports an auditable
nearest-neighbour model in the same binary format as the ASL baseline.
"""
from pathlib import Path
import collections, hashlib, json, struct
import numpy as np
import mediapipe as mp
from PIL import Image, ImageOps
from sklearn.neighbors import KNeighborsClassifier
from sklearn.metrics import classification_report, accuracy_score

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'app/src/main/assets'
DATA=ROOT/'tools/psl-dataset'

def normalize(points):
    p=np.array(points,dtype=np.float32).reshape(21,3)
    p=p-p[0]
    # Canonical reflection by MCP anatomy; supports either hand without camera mirroring.
    if p[5,0] < p[17,0]: p[:,0]*=-1
    scale=np.max(np.linalg.norm(p,axis=1))
    return (p/max(float(scale),1e-6)).flatten()

cache=ROOT/'tools/psl-landmarks.npz'
if not cache.exists():
    options=mp.tasks.vision.HandLandmarkerOptions(base_options=mp.tasks.BaseOptions(model_asset_path=str(ASSETS/'models/hand_landmarker.task')),num_hands=1,min_hand_detection_confidence=0.4,min_hand_presence_confidence=0.4)
    X=[]; y=[]; groups=[]; filenames=[]; samples=[]; failures=collections.Counter()
    with mp.tasks.vision.HandLandmarker.create_from_options(options) as detector:
        paths=sorted(p for p in DATA.glob('*/*.png'))
        for i,path in enumerate(paths):
            label=path.parent.name
            group=path.stem.split('_')[0]
            img=Image.open(path).convert('RGB')
            img=ImageOps.expand(img,border=40,fill='black')
            result=detector.detect(mp.Image(image_format=mp.ImageFormat.SRGB,data=np.asarray(img)))
            if result.hand_landmarks:
                pts=[[p.x,p.y,p.z] for p in result.hand_landmarks[0]]
                X.append(normalize(pts));y.append(label)
                groups.append(group);filenames.append(str(path.relative_to(DATA)))
                if len(samples)<5: samples.append({'landmarks':pts,'features':normalize(pts).tolist(),'label':label})
            else: failures[label]+=1
            if i%200==0: print(f'Landmarks: {i}/{len(paths)}; accepted {len(X)}',flush=True)
    np.savez_compressed(cache,X=np.asarray(X),y=np.asarray(y),groups=np.asarray(groups),filenames=np.asarray(filenames))
    (ROOT/'docs/landmark-fixtures.json').write_text(json.dumps(samples),encoding='utf-8')
    (ROOT/'docs/detection-failures.json').write_text(json.dumps(dict(failures),indent=2),encoding='utf-8')

d=np.load(cache);X=d['X'];y=d['y'];groups=d['groups']
print('Dataset',len(X),flush=True)
# Classes where MediaPipe hand detection saw too few real examples cannot support
# recognition at all; they stay guided practice (like moving letters in the ASL edition).
MIN_SAMPLES=8
counts=collections.Counter(y)
excluded={k:v for k,v in sorted(counts.items()) if v<MIN_SAMPLES}
keep=np.isin(y,list(excluded),invert=True) if excluded else np.ones(len(y),bool)
X,y=X[keep],y[keep]
labels=sorted(set(y)); label_idx={v:i for i,v in enumerate(labels)}
print('Trained classes',len(labels),labels,flush=True)
report={'source':'https://data.mendeley.com/datasets/y9svrbh27n/1','citation':'Ali, Ali Imran (2021), "Data set about hand configuration of Pakistan Sign Language", Mendeley Data, V1, doi:10.17632/y9svrbh27n.1','paper':'Imran et al. (2021), Data in Brief 36, 107021, doi:10.1016/j.dib.2021.107021','license':'CC BY 4.0','detectedSamples':int(len(X)),'detectionAccepted':dict(collections.Counter(y)),'undetectableClasses':excluded,'scope':'Static PSL Urdu-alphabet handshapes only. Classes MediaPipe cannot reliably detect, moving signs and compound numbers are guided practice. Webcam captures of few signers; not evaluated on children or target phone.','evaluation':{}}
# No signer grouping survives in this dataset (capture codes repeat inside single
# classes), so evaluation uses the paper's stratified split, every 5th file per class,
# plus a near-duplicate diagnostic instead of claiming a person-independent result.
test=np.zeros(len(y),bool)
for label in labels:
    idx=np.where(y==label)[0]
    test[idx[::5]]=True
train=~test
model=KNeighborsClassifier(n_neighbors=5,weights='distance').fit(X[train],y[train])
predictions=model.predict(X[test])
metrics=classification_report(y[test],predictions,output_dict=True,zero_division=0)
accuracy=float(accuracy_score(y[test],predictions))
# Near-duplicate diagnostic: same-session webcam frames can almost repeat a training
# frame. Accuracy excluding test points with an extremely close same-class training
# neighbour shows what survives once those lucky matches are removed.
nearest_same=np.zeros(test.sum())
t_idx=np.where(test)[0]
for i,qi in enumerate(t_idx):
    same=np.where(y[train]==y[qi])[0]
    dmin=np.min(np.linalg.norm(X[train][same]-X[qi],axis=1))
    nearest_same[i]=dmin
threshold=0.10
suspicious=nearest_same<threshold
strict=accuracy_score(y[test][~suspicious],predictions[~suspicious]) if (~suspicious).any() else 0.0
report['evaluation']['psl_alphabet']={'split':'stratified every 5th file per class','train':int(train.sum()),'test':int(test.sum()),'accuracy':accuracy,'strictAccuracyExcludingNearDuplicates':float(strict),'nearDuplicateThreshold':threshold,'nearDuplicateTestSamples':int(suspicious.sum()),'medianNearestTrainDistanceSameClass':float(np.median(nearest_same)),'caveat':'Same-session frames may repeat; person-independent accuracy is not established.','perClass':metrics}
print('psl_alphabet accuracy',accuracy,'strict',strict,flush=True)
path=ASSETS/'models/alphabet.bin'
with path.open('wb') as f:
    f.write(struct.pack('>iii',0x49525353,len(X),63))
    f.write(struct.pack('>i',len(labels)))
    for label in labels:
        b=label.encode();f.write(struct.pack('>H',len(b)));f.write(b)
    for vector,label in zip(X,y):
        f.write(struct.pack('>i',label_idx[label]))
        f.write(struct.pack('>63f',*vector))
report['evaluation']['psl_alphabet']['sha256']=hashlib.sha256(path.read_bytes()).hexdigest()
report['labels']=labels
(ROOT/'docs/recognition-evaluation.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print('Model exported.',flush=True)
