"""One-shot splice: replace ASL alphabet/number/vocab content with PSL content."""
import re, io

P = 'app/src/main/java/com/example/data/LearningDataSource.kt'
src = io.open(P, encoding='utf-8').read()

# (id, urdu, translit, signName, [steps], emoji, funFact, pose)
L = [
 ('alif','ا','Alif','Closed fist with thumb upright alongside',["Make a gentle closed fist.","Let your thumb stand tall alongside your fingers.","Palm faces forward like a little mountain!"],'🍎','ا is for انار (anaar) — a juicy pomegranate!','FIST_THUMB_SIDE'),
 ('be','ب','Be','Four fingers up together, thumb folded into palm',["Hold four fingers straight up, touching.","Tuck your thumb into your palm.","Show your palm to the screen like an open window!"],'🐐','ب is for بکری (bakri) — a silly goat!','FLAT_FOUR_FOLD_THUMB'),
 ('pe','پ','Pe','Open hand of ب, turned as shown in the photo',["Start like ب — four fingers up.","Look at the real photo and match the gentle tilt.","پ has three dots above the letter — count them!"],'🪁','پ is for پتنگ (patang) — a dancing kite!','FLAT_FOUR_FOLD_THUMB'),
 ('te','ت','Te','Index finger up, thumb resting across the hand',["Lift your index finger straight up.","Let your thumb rest across your middle finger.","ت has two dots above the letter!"],'🦋','ت is for تتلی (titli) — a fluttery butterfly!','POINT_INDEX_O'),
 ('tte','ٹ','Tte','Soft closed fist, thumb placed as in the photo',["Make a soft closed fist.","Look at the photo and copy where your thumb sits.","ٹ is ت wearing a little cap on top!"],'🎩','ٹ is for ٹوپی (topi) — a cosy cap!','FIST_THUMB_SIDE'),
 ('se','ث','Se','Index curved into a hook, thumb touches it',["Curve your index finger into a little hook.","Touch your thumb gently to it.","ث wears three dots on top!"],'🍇','ث is for ثمر (samar) — sweet fruit!','POINT_INDEX_O'),
 ('jim','ج','Jim','Fist with index finger up, tip curled like a hook',["Make a fist with your index finger up.","Curl the top of your index finger like a tiny hook.","ج has one dot tucked inside the hook!"],'✈️','ج is for جہاز (jahaaz) — a flying aeroplane!','HOOKED_INDEX_X'),
 ('che','چ','Che','Two fingers together, pointing to the side',["Point your index and middle fingers to the side.","Hold them together like a little arrow.","چ has three dots floating below!"],'🌙','چ is for چاند (chaand) — the glowing moon!','TWO_FINGERS_SIDEWAYS'),
 ('he','ح','He','Two fingers together pointing aside, gently curled',["Hold two fingers together, pointing to the side.","Curl them gently like a flower stem.","ح has no dot at all!"],'🍮','ح is for حلوہ (halwa) — a warm sweet treat!','TWO_FINGERS_SIDEWAYS'),
 ('khe','خ','Khe','Fingers opened wide as shown in the photo',["Open your fingers wide like the photo.","Check where your thumb sits carefully.","خ has one dot sitting on top!"],'🍈','خ is for خربوزہ (kharbooza) — a big melon!','V_PEACE_SPREAD'),
 ('dal','د','Dal','Index finger bent into a hook, pointing down',["Bend your index finger into a hook.","Point it gently downwards.","د is long and smooth with no dots!"],'🚪','د is for دروازہ (darwaza) — a big door!','HOOKED_INDEX_X'),
 ('dhal','ڈ','Dhal','Index finger up, thumb and middle finger form a circle',["Point your index finger up to the sky.","Touch your thumb to your middle finger — a little circle!","ڈ is د wearing a hat on top!"],'🥁','ڈ is for ڈھول (dhol) — a loud drum!','POINT_INDEX_O'),
 ('zal','ذ','Zal','د hook shape, hand turned as in the photo',["Make د's hook shape with your finger.","Turn your hand gently downwards like the photo.","ذ has one dot above it!"],'🌍','ذ is for ذخیرہ (zakheera) — a treasure store!','HOOKED_INDEX_X'),
 ('re','ر','Re','Index crossed over middle finger',["Cross your index finger over your middle finger.","Point them gently up.","ر flows like a little wave!"],'🍞','ر is for روٹی (roti) — warm bread!','ONE_INDEX'),
 ('rre','ڑ','Rre','Index and middle in a wide V',["Hold up your index and middle finger in a wide V.","Match the spread in the real photo.","ڑ never starts a word — it hides inside, like بارش!"],'🌧️','ڑ hides inside words like بارش (barish) — rain!','V_PEACE_SPREAD'),
 ('ze','ز','Ze','Index finger pointing to the side',["Point your index finger to the side.","Keep the rest folded in your fist.","ز has one dot above it!"],'🦓','ز is for زیبرا — striped zebra!','POINT_INDEX_THUMB_SIDE'),
 ('zhe','ژ','Zhe','Two fingers up together, side by side',["Hold two fingers up together, side by side.","Match your hand to the photo.","ژ is ز with three extra dots!"],'🌨️','ژ lives in ژالہ (zaala) — falling hail!','TWO_FINGERS_SIDEWAYS'),
 ('seen','س','Seen','Soft closed fist as in the photo',["Close your fingers into a soft fist.","Look at the photo for the gentle curve.","س flows like three little hills!"],'🍏','س is for سیب (seb) — a crunchy apple!','CURL_ALL_FINGERS'),
 ('sheen','ش','Sheen','Index curled over a closed fist',["Curl your index finger over your closed fist.","Match the shape in the photo.","ش has three dots dancing above!"],'🦁','ش is for شیر (sher) — a roaring lion!','HOOKED_INDEX_X'),
 ('swad','ص','Swad','Fist with thumb up, palm turned towards you',["Make a closed fist with your thumb up — like ا!","Turn your palm gently towards you.","ص starts with a little oval shape!"],'🧼','ص is for صابن (sabun) — bubbly soap!','FIST_THUMB_SIDE'),
 ('zwad','ض','Zwad','Index and pinky up, middle fingers held by thumb',["Lift your index finger and smallest finger.","Fold your middle fingers and hold them with your thumb.","ض is ص wearing one dot!"],'✖️','ض is for ضرب (zarb) — multiplying numbers!','THUMB_PINKY_Y'),
 ('toay','ط','Toay','Index up, thumb and middle finger make a circle',["Point your index finger up.","Make a circle with your thumb and middle finger under it.","ط is tall and elegant with no dots!"],'🦜','ط is for طوطا (tota) — a talking parrot!','POINT_INDEX_O'),
 ('zoay','ظ','Zoay','Thumb touches index tip, other fingers open wide',["Touch your thumb to your index fingertip.","Let your other fingers open wide like a flower.","ظ has one dot above!"],'🏺','ظ is for ظرف (zarf) — a shining pot!','CIRCLE_O'),
 ('ain','ع','Ain','Hand down, thumb and index curved towards each other',["Point your hand downwards.","Curve your thumb and index towards each other like a beak.","ع has a tiny س shape on top!"],'👓','ع is for عینک (ainak) — bright glasses!','CURVED_C'),
 ('ghain','غ','Ghain','Like ع, with one dot above',["Make ع's shape with your hand.","Ghain looks just like Ain — with a dot!","غ has one dot above!"],'🎈','غ is for غبارہ (ghubara) — a flying balloon!','HOOKED_INDEX_X'),
 ('fay','ف','Fay','Fingers together pointing up, thumb out to the side',["Hold your fingers together pointing up.","Let your thumb rest out to the side.","ف has one dot above!"],'⚽','ف is for فٹبال — goal! football!','FLAT_FOUR_FOLD_THUMB'),
 ('qaf','ق','Qaf','Two fingers pointing downwards',["Point two fingers downwards like the photo.","ق is ک with two dots above!","Practise this one with your teacher — the camera cannot see it yet."],'🖊️','ق is for قلم (qalam) — a clever pen!','TWO_FINGERS_SIDEWAYS'),
 ('kaf','ک','Kaf','Index and middle in a wide V, thumb out',["Open index and middle finger in a wide V.","Let your thumb peek out to the side.","ک opens like friendly scissors!"],'📖','ک is for کتاب (kitaab) — a book of stories!','V_PEACE_SPREAD'),
 ('gaf','گ','Gaf','Index finger pointing to the side',["Point your index finger to the side.","Keep your fist snug and strong.","گ is ک with an extra line on top!"],'🚗','گ is for گاڑی (gaari) — a zooming car!','POINT_INDEX_THUMB_SIDE'),
 ('laam','ل','Laam','Index finger up, thumb out to the side',["Point your index finger up to the sky.","Stretch your thumb out to the side.","ل is tall like a flagpole!"],'🍬','ل is for لڈو (laddu) — a round sweet!','L_SHAPE'),
 ('meem','م','Meem','Fist folded, pointing downwards',["Fold your fingers over into a soft fist.","Point your hand gently downwards.","م has a small circle below!","Practise with a grown-up — the camera cannot see م yet."],'🐟','م is for مچھلی (machhli) — a swimming fish!','CURL_ALL_FINGERS'),
 ('noon','ن','Noon','Hand down, fingertips curled gently in',["Point your hand downwards.","Curl your fingertips gently in like the photo.","ن has one dot above!"],'🥥','ن is for ناریل (nariyal) — a hard coconut!','TWO_FINGERS_OVER_THUMB'),
 ('vao','و','Vao','Gentle closed fist as in the photo',["Make a gentle closed fist.","Match the photo for your thumb's position.","و joins words together — it means 'and'!"],'🚿','و is for وضو (wuzu) — fresh and clean!','CIRCLE_O'),
 ('hay','ہ','Hay','Index and middle in a wide V',["Open index and middle finger in a wide V.","Look at the photo for the gentle angle.","ہ is round like a little drum!"],'✋','ہ is for ہاتھ (haath) — your helping hand!','V_PEACE_SPREAD'),
 ('chhoti_yeh','ی','Chhoti Yeh','Closed fist with smallest finger raised',["Make a closed fist.","Raise your smallest finger up proudly!","ی has two dots below!"],'💎','ی is for یاقوت (yaqoot) — a red ruby!','FIST_PINKY_UP'),
 ('bari_yeh','ے','Bari Yeh','Index finger straight up, thumb out to the side',["Point your index finger straight up.","Let your thumb rest out to the side.","ے never starts a word — it finishes them, like کھیل!"],'🎲','ے finishes words like کھیل (khel) — play!','ONE_INDEX'),
 ('hamza','ء','Hamza','Thumb and index fingertips pinch into a tiny circle',["Pinch your thumb and index fingertips together.","Make a tiny circle — small but powerful!","ء hops around inside words!"],'✨','ء is hamza — it hops around inside words!','CIRCLE_O'),
]
VOWELS={'alif','vao','chhoti_yeh','bari_yeh'}
COLORS=[0xFFFF5E7E,0xFF5C52E5,0xFF00B894,0xFFFF7849,0xFFFFC312]

items=[]
for i,(cid,urdu,name,sign,steps,emoji,fact,pose) in enumerate(L):
    sub=f"PSL Letter {name}"+(" (Urdu vowel)" if cid in VOWELS else "")
    lvl=1 if cid in VOWELS else 2
    st=", ".join(f'"{s}"' for s in steps)
    items.append(f"""    LearningItem(
      id = "letter_{cid}",
      title = "{urdu}",
      subtitle = "{sub}",
      category = SignCategory.ALPHABET,
      level = {lvl},
      signName = "{sign}",
      visualCueSteps = listOf({st}),
      objectEmoji = "{emoji}",
      funFact = "{fact}",
      handPoseType = HandPose.{pose},
      colorHex = 0x{COLORS[i%5]:08X}
    )""")
alphabet = "  // --- PAKISTAN SIGN LANGUAGE ALPHABET (37 URDU LETTERS) ---\n  // Handshape references from the CC BY 4.0 PSL dataset (see docs/RESEARCH.md).\n  val alphabetItems: List<LearningItem> = listOf(\n" + ",\n".join(items) + "\n  )\n"

pat = re.compile(r"  // --- COMPLETE A TO Z ALPHABET \(26 LETTERS\) ---\n  val alphabetItems: List<LearningItem> = listOf\(.*?\n  \)\n", re.S)
assert pat.search(src), "alphabet block not found"
src = pat.sub(alphabet, src, count=1)

# Number cues -> PSL-safe guided practice
cuepat = re.compile(r"    val cue = when \{.*?\n    \}\n", re.S)
assert cuepat.search(src), "cue block not found"
newcue = """    val cue = when {
      num <= 5 -> listOf(
        "Count $num little stars or blocks with your finger.",
        "Show the number sign your teacher uses for $num.",
        "Point at the number and count out loud together!"
      )
      num <= 10 -> listOf(
        "Collect $num small things like leaves or buttons.",
        "Practise the PSL number sign for $num with a grown-up.",
        "Line up your treasures and count them again!"
      )
      num % 10 == 0 -> listOf(
        "Count in tens up to $tens, then finish with zero.",
        "Ask your teacher to show how $num is signed.",
        "Clap $tens times, then once more for luck!"
      )
      else -> listOf(
        "Break $num into $tens tens and $units ones.",
        "Practise both number signs slowly, then together.",
        "Count blocks, spoons or steps to reach $num!"
      )
    }
"""
src = cuepat.sub(newcue, src, count=1)
src = src.replace('aslSignName = if (num <= 10) "Sign for $num" else "Compound ASL Sign $tens-$units"',
                  'signName = if (num <= 10) "PSL number sign for $num — learn it with your teacher" else "Compound sign for $num — learn it with your teacher"')

# Vocabulary: keep ids/emojis, replace ASL-specific claims with guided practice wording
vpat = re.compile(r"  val vocabularyItems: List<LearningItem> = listOf\(.*?\n  \)\n", re.S)
assert vpat.search(src), "vocab block not found"
V = [
 ('vocab_hello','Hello','Greeting Word',['Wave your hand warmly, just like the photo of a friendly hello.','Practise the PSL sign with your teacher or family.','Smile — faces talk too!'],'👋','A warm hello can be waved, signed, or smiled!'),
 ('vocab_thanks','Thank You','Polite Word',['Think of something kind someone did for you today.','Learn the PSL sign for Thank You with your teacher.','Say it with your whole face, not just your hands!'],'💖','Thanking a friend makes both hearts warm!'),
 ('vocab_water','Water','Drink Word',['When you feel thirsty, show your teacher the PSL sign for Water.','Practise it together before snack time.','Water keeps your brain strong for learning!'],'💧','Asking for water by sign is a real superpower!'),
 ('vocab_friend','Friend','Social Word',['Think of your best friend and their name in signs.','Learn the PSL sign for Friend with your teacher.','Friends can sign to each other across the room!'],'🤝','Friendship signs are like secret handshakes!'),
 ('vocab_book','Book','School Word',['Hold an imaginary book in your hands.','Learn the PSL sign for Book with your teacher.','Every book is an adventure waiting for you!'],'📖','Books open worlds — and signs open books!'),
 ('vocab_happy','Happy','Feeling Word',['Touch your chest — can you feel your happy heartbeat?','Learn the PSL sign for Happy with your teacher.','Show your happy face together with the sign!'],'😄','Happiness shows in hands and faces at the same time!'),
 ('vocab_school','School','Place Word',['Think about your classroom and your friends there.','Learn the PSL sign for School with your teacher.','Sign it every morning when you arrive!'],'🏫','School is where signs and friends grow!'),
 ('vocab_love','Love','Heart Word',['Hug yourself gently — that is what love feels like.','Learn the PSL sign for Love with your teacher.','Send a sign of love to someone far away!'],'❤️','Love is the first sign many families learn!'),
]
vitems=[]
for vid,title,sub,steps,emoji,fact in V:
    st=", ".join(f'"{s}"' for s in steps)
    vitems.append(f"""    LearningItem(
      id = "{vid}",
      title = "{title}",
      subtitle = "{sub}",
      category = SignCategory.VOCABULARY,
      level = 3,
      signName = "Learned together with a PSL teacher",
      visualCueSteps = listOf({st}),
      objectEmoji = "{emoji}",
      funFact = "{fact}",
      handPoseType = HandPose.WAVE_HELLO,
      colorHex = 0xFF5C52E5
    )""")
src = vpat.sub("  val vocabularyItems: List<LearningItem> = listOf(\n" + ",\n".join(vitems) + "\n  )\n", src, count=1)

src = src.replace('  // COMBINED LIST (All 26 Alphabet letters + 50 Numbers + Vocabulary)',
                  '  // COMBINED LIST (All 37 PSL letters + 50 Numbers + Vocabulary)')
src = src.replace("""  val vowelItems: List<LearningItem> = alphabetItems.filter {
    it.title in listOf("A", "E", "I", "O", "U")
  }""","""  val vowelItems: List<LearningItem> = alphabetItems.filter {
    it.title in listOf("ا", "و", "ی", "ے")
  }""")

# Levels
lpat = re.compile(r"  val levels: List<GameLevel> = listOf\(.*?\n  \)\n\n  val badges", re.S)
assert lpat.search(src), "levels block not found"
newlevels = """  val levels: List<GameLevel> = listOf(
    GameLevel(
      id = 1,
      title = "Level 1: Urdu Vowels & Counting 1-5",
      subtitle = "Starting Irssa's Journey",
      description = "Meet the Urdu vowels ا, و, ی and ے and practise counting 1 to 5.",
      requiredStars = 0,
      badgeReward = "Vowel Pioneer",
      badgeEmoji = "🌈",
      itemIds = (vowelItems.map { it.id } + (1..5).map { "num_$it" }),
      colorHex = 0xFF5C52E5
    ),
    GameLevel(
      id = 2,
      title = "Level 2: First Letters & Numbers 6-20",
      subtitle = "Expanding Vocabulary",
      description = "Learn the first twelve PSL letters and count all the way to 20.",
      requiredStars = 15,
      badgeReward = "Alphabet Explorer",
      badgeEmoji = "🔤",
      itemIds = (alphabetItems.take(12).map { it.id } + (6..20).map { "num_$it" }),
      colorHex = 0xFF00B894
    ),
    GameLevel(
      id = 3,
      title = "Level 3: More Letters & Numbers 21-35",
      subtitle = "Completing the PSL Alphabet",
      description = "Finish the remaining PSL letters, learn everyday words, and count to 35!",
      requiredStars = 35,
      badgeReward = "Alphabet Master",
      badgeEmoji = "⭐",
      itemIds = (alphabetItems.drop(12).map { it.id } + (21..35).map { "num_$it" } + listOf("vocab_hello", "vocab_thanks", "vocab_water", "vocab_friend")),
      colorHex = 0xFFFF7849
    ),
    GameLevel(
      id = 4,
      title = "Level 4: Counting 36 to 50 & School Words",
      subtitle = "Ready for Classroom",
      description = "Reach the big 50 in counting! Learn words for School, Books, and Love.",
      requiredStars = 60,
      badgeReward = "Math & School Star",
      badgeEmoji = "🏫",
      itemIds = ((36..50).map { "num_$it" } + listOf("vocab_book", "vocab_happy", "vocab_school", "vocab_love")),
      colorHex = 0xFFFF5E7E
    ),
    GameLevel(
      id = 5,
      title = "Level 5: Master Sign Hero (Speed Challenge)",
      subtitle = "The Ultimate Champion",
      description = "Test your skills across the PSL alphabet and 50 numbers in Camera Studio and Speed Match!",
      requiredStars = 90,
      badgeReward = "Master Sign Champion",
      badgeEmoji = "👑",
      itemIds = (alphabetItems.take(5).map { it.id } + listOf("num_5", "num_10", "num_25", "num_50", "vocab_hello", "vocab_thanks", "vocab_love")),
      colorHex = 0xFFFFC312
    )
  )

  val badges"""
src = lpat.sub(newlevels, src, count=1)

src = src.replace('StickerBadge("badge_first_vowel", "Vowel Explorer", "Signed your very first vowel A!", "🍎", 3)',
                  'StickerBadge("badge_first_vowel", "Vowel Explorer", "Signed your very first Urdu vowel ا!", "🍎", 3)')
src = src.replace('StickerBadge("badge_vowels_all", "Vowel Rainbow", "Mastered A, E, I, O, U with glowing confidence", "🌈", 15)',
                  'StickerBadge("badge_vowels_all", "Vowel Rainbow", "Mastered the Urdu vowels ا, و, ی and ے with glowing confidence", "🌈", 15)')
src = src.replace('StickerBadge("badge_alpha_half", "Half Alphabet Star", "Discovered letters A through M!", "🔤", 25)',
                  'StickerBadge("badge_alpha_half", "Half Alphabet Star", "Discovered the first PSL letters!", "🔤", 25)')
src = src.replace('StickerBadge("badge_alpha_complete", "Full A-Z Master", "Learned all 26 American Sign Language letters!", "🏅", 40)',
                  'StickerBadge("badge_alpha_complete", "Full PSL Master", "Learned all 37 Pakistan Sign Language letters!", "🏅", 40)')

io.open(P, 'w', encoding='utf-8', newline='\n').write(src)
print('spliced OK; aslSignName remaining:', src.count('aslSignName'))
