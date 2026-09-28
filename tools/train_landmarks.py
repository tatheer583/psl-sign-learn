"""Reproducible small on-device baseline from CC0 real hand photographs.
Splits by hand/person prefix; no near-duplicate frame random-split claims.
Exports an auditable nearest-neighbour model, not executable pickle objects.
"""
from pathlib import Path
import collections, hashlib, json, re, struct, zipfile
import numpy as np
import mediapipe as mp
from PIL import Image, ImageOps, ImageDraw
from sklearn.neighbors import KNeighborsClassifier
from sklearn.metrics import classification_report, accuracy_score

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'app/src/main/assets'

def normalize(points):
    p=np.array(points,dtype=np.float32).reshape(21,3)
    p=p-p[0]
    # Canonical reflection by MCP anatomy; supports either hand without camera mirroring.
    if p[5,0] < p[17,0]: p[:,0]*=-1
    scale=np.max(np.linalg.norm(p,axis=1))
    return (p/max(float(scale),1e-6)).flatten()

cache=ROOT/'tools/landmarks.npz'
if not cache.exists():
    options=mp.tasks.vision.HandLandmarkerOptions(base_options=mp.tasks.BaseOptions(model_asset_path=str(ASSETS/'models/hand_landmarker.task')),num_hands=1,min_hand_detection_confidence=0.4,min_hand_presence_confidence=0.4)
    X=[]; y=[]; groups=[]; filenames=[]; samples=[]; failures=collections.Counter()
    with mp.tasks.vision.HandLandmarker.create_from_options(options) as detector, zipfile.ZipFile(ROOT/'tools/asl-dataset.zip') as archive:
        names=sorted(x for x in archive.namelist() if len(x.split('/'))==3 and x.lower().endswith(('.jpeg','.jpg','.png')))
        for i,name in enumerate(names):
            label=name.split('/')[1].upper()
            # J and Z require motion; never train a static model to claim them.
            if label in ['J','Z']: continue
            import io
            img=Image.open(io.BytesIO(archive.read(name))).convert('RGB')
            img=ImageOps.expand(img,border=40,fill='black')
            result=detector.detect(mp.Image(image_format=mp.ImageFormat.SRGB,data=np.asarray(img)))
            if result.hand_landmarks:
                pts=[[p.x,p.y,p.z] for p in result.hand_landmarks[0]]
                X.append(normalize(pts));y.append(label)
                groups.append(re.search(r'hand\d+',name).group() if re.search(r'hand\d+',name) else name.split('/')[-1].split('_')[0])
                filenames.append(name)
                if len(samples)<5: samples.append({'landmarks':pts,'features':normalize(pts).tolist(),'label':label})
            else: failures[label]+=1
            if i%200==0: print(f'Landmarks: {i}/{len(names)}; accepted {len(X)}',flush=True)
    np.savez_compressed(cache,X=np.asarray(X),y=np.asarray(y),groups=np.asarray(groups),filenames=np.asarray(filenames))
    (ROOT/'docs/landmark-fixtures.json').write_text(json.dumps(samples),encoding='utf-8')
    (ROOT/'docs/detection-failures.json').write_text(json.dumps(dict(failures),indent=2),encoding='utf-8')

d=np.load(cache);X=d['X'];y=d['y'];groups=d['groups']
print('Dataset',len(X),'groups',collections.Counter(groups),flush=True)
report={'source':'https://www.kaggle.com/datasets/ayuraj/asl-dataset','license':'CC0 1.0','detectedSamples':len(X),'groups':dict(collections.Counter(groups)),'scope':'Static ASL handshapes only. J/Z and moving signs excluded. Not evaluated on children or target phone.','evaluation':{}}
# Alphabet / number modes are deliberately separate: O/0 and V/2 overlap.
for mode in ['alphabet','number']:
    mask=np.array([v.isalpha() if mode=='alphabet' else v.isdigit() for v in y])
    xm,ym,gm=X[mask],y[mask],groups[mask]
    unique=sorted(set(gm))
    holdout=unique[-1]
    train=gm!=holdout;test=gm==holdout
    if len(unique)<2: raise ValueError('No distinct signer/hand group available for honest evaluation')
    model=KNeighborsClassifier(n_neighbors=5,weights='distance').fit(xm[train],ym[train])
    predictions=model.predict(xm[test])
    metrics=classification_report(ym[test],predictions,output_dict=True,zero_division=0)
    report['evaluation'][mode]={'heldOutGroup':holdout,'train':int(train.sum()),'test':int(test.sum()),'accuracy':float(accuracy_score(ym[test],predictions)),'perClass':metrics}
    print(mode,'held-out',holdout,'accuracy',report['evaluation'][mode]['accuracy'],flush=True)
    # Export all available real examples after documenting a separate holdout evaluation.
    labels=sorted(set(ym)); label_idx={v:i for i,v in enumerate(labels)}
    path=ASSETS/f'models/{mode}.bin'
    with path.open('wb') as f:
        f.write(struct.pack('>iii',0x49525353,len(xm),63))
        f.write(struct.pack('>i',len(labels)))
        for label in labels:
            b=label.encode();f.write(struct.pack('>H',len(b)));f.write(b)
        for vector,label in zip(xm,ym):
            f.write(struct.pack('>i',label_idx[label]))
            f.write(struct.pack('>63f',*vector))
    report['evaluation'][mode]['sha256']=hashlib.sha256(path.read_bytes()).hexdigest()
(ROOT/'docs/recognition-evaluation.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print('Models exported.',flush=True)
