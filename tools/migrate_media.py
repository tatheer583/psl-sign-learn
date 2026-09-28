"""Export one real PSL photo per letter, archive ASL media, regenerate media_manifest.json."""
import json, hashlib, io, os, shutil
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, 'tools', 'psl-dataset')
SIGNS = os.path.join(ROOT, 'app', 'src', 'main', 'assets', 'signs')
ARCHIVE = os.path.join(ROOT, 'tools', 'asl-media-archive', 'signs')
LETTERS = ['alif','be','pe','te','tte','se','jim','che','he','khe','dal','dhal','zal','re','rre',
           'ze','zhe','seen','sheen','swad','zwad','toay','zoay','ain','ghain','fay','qaf','kaf',
           'gaf','laam','meem','noon','vao','hay','chhoti_yeh','bari_yeh','hamza']
URDU = {'alif':'ا','be':'ب','pe':'پ','te':'ت','tte':'ٹ','se':'ث','jim':'ج','che':'چ','he':'ح',
        'khe':'خ','dal':'د','dhal':'ڈ','zal':'ذ','re':'ر','rre':'ڑ','ze':'ز','zhe':'ژ','seen':'س',
        'sheen':'ش','swad':'ص','zwad':'ض','toay':'ط','zoay':'ظ','ain':'ع','ghain':'غ','fay':'ف',
        'qaf':'ق','kaf':'ک','gaf':'گ','laam':'ل','meem':'م','noon':'ن','vao':'و','hay':'ہ',
        'chhoti_yeh':'ی','bari_yeh':'ے','hamza':'ء'}

os.makedirs(ARCHIVE, exist_ok=True)
moved = 0
for f in os.listdir(SIGNS):
    if f.startswith('letter_') or f.startswith('num_'):
        shutil.move(os.path.join(SIGNS, f), os.path.join(ARCHIVE, f)); moved += 1
print('archived ASL media files:', moved)

photos = json.load(open(os.path.join(ROOT, 'tools', 'psl-photo-picks.json'), encoding='utf-8'))
entries = []
for cls in LETTERS:
    src = photos[cls]
    im = Image.open(src).convert('RGB')
    w, h = im.size
    scale = 480.0 / max(w, h)
    if scale < 1: im = im.resize((int(w * scale), int(h * scale)))
    out = os.path.join(SIGNS, f'letter_{cls}.jpg')
    im.save(out, 'JPEG', quality=85)
    digest = hashlib.sha256(open(out, 'rb').read()).hexdigest()
    entries.append({
        'id': f'letter_{cls}', 'file': f'signs/letter_{cls}.jpg', 'type': 'image',
        'gloss': f'PSL letter {URDU[cls]} ({cls})',
        'source': 'https://data.mendeley.com/datasets/y9svrbh27n/1',
        'author': 'Ali Imran Ali, MNS University of Agriculture Multan (2021)',
        'license': 'CC BY 4.0', 'sha256': digest,
        'note': 'Real hand photograph from the PSL dataset, unmodified except resize.'})
print('exported letter photos:', len(entries))

GUIDE = {'source': 'https://psl.org.pk/', 'author': 'Deaf Reach / PSL Dictionary (reference link only)',
         'license': 'No media bundled — guided practice'}
for n in range(0, 51):
    entries.append({'id': f'num_{n}', 'file': '', 'type': 'none', 'gloss': f'PSL number {n}',
                    **GUIDE, 'note': 'No redistributable PSL number video was available. Practise with a teacher; nothing is substituted.'})
for v in ['hello','thanks','water','friend','book','happy','school','love']:
    entries.append({'id': f'vocab_{v}', 'file': '', 'type': 'none', 'gloss': f'PSL word: {v}',
                    **GUIDE, 'note': 'No redistributable PSL word video was available. Practise with a teacher; nothing is substituted.'})

manifest = os.path.join(ROOT, 'app', 'src', 'main', 'assets', 'media_manifest.json')
json.dump(entries, open(manifest, 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
print('manifest entries:', len(entries))
