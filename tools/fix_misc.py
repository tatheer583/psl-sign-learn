"""Rewrite gesture presets to PSL letters, fix vowel filter, fix re pose."""
import io, re

p='app/src/main/java/com/example/data/model/GestureConversionModels.kt'
s=io.open(p,encoding='utf-8').read()
pat=re.compile(r"object GesturePresetsDataSource \{.*\n  \)\n\}", re.S)
assert pat.search(s)
new = '''object GesturePresetsDataSource {
  // PSL letter presets. Number symbols stay empty: PSL number signs need a real
  // teacher, so the converter never pretends a handshape proves a number.
  val presets: List<HandBodyGesturePreset> = listOf(
    HandBodyGesturePreset(
      id = "fist_thumb_side",
      name = "Gentle Fist",
      emoji = "✊",
      bodyDescription = "Hand at chest height, thumb upright against folded fingers",
      alphabetSymbol = "ا",
      numberSymbol = "",
      alphabetWord = "Anaar (Pomegranate) 🍎",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.FIST_THUMB_SIDE,
      funHint = "ا is for Anaar! Thumb stands tall like a mountain"
    ),
    HandBodyGesturePreset(
      id = "flat_four_thumb",
      name = "Flat Four Palm",
      emoji = "🖐️",
      bodyDescription = "Four fingers straight up, thumb tucked across palm",
      alphabetSymbol = "ب",
      numberSymbol = "",
      alphabetWord = "Bakri (Goat) 🐐",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.FLAT_FOUR_FOLD_THUMB,
      funHint = "ب is for Bakri! Tall and steady like a goat"
    ),
    HandBodyGesturePreset(
      id = "curved_c",
      name = "Curved Beak",
      emoji = "🤏",
      bodyDescription = "Thumb and index curved towards each other, hand pointing down",
      alphabetSymbol = "ع",
      numberSymbol = "",
      alphabetWord = "Ainak (Glasses) 👓",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.CURVED_C,
      funHint = "ع is for Ainak! A little beak for glasses"
    ),
    HandBodyGesturePreset(
      id = "point_index_d",
      name = "Index Circle",
      emoji = "☝️",
      bodyDescription = "Index finger tall to the sky, thumb and middle finger make a circle",
      alphabetSymbol = "ڈ",
      numberSymbol = "",
      alphabetWord = "Dhol (Drum) 🥁",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.POINT_INDEX_O,
      funHint = "ڈ is for Dhol! Boom boom drum finger"
    ),
    HandBodyGesturePreset(
      id = "curled_claws_e",
      name = "Soft Fist",
      emoji = "🐾",
      bodyDescription = "Fingers closed into a soft, gentle fist",
      alphabetSymbol = "س",
      numberSymbol = "",
      alphabetWord = "Seb (Apple) 🍏",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.CURL_ALL_FINGERS,
      funHint = "س is for Seb! Three hills in one letter"
    ),
    HandBodyGesturePreset(
      id = "three_fingers_ok_f",
      name = "Circle Under Index",
      emoji = "👌",
      bodyDescription = "Index finger up, thumb and middle finger circle beneath it",
      alphabetSymbol = "ط",
      numberSymbol = "",
      alphabetWord = "Tota (Parrot) 🦜",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.THREE_FINGERS_UP_OK,
      funHint = "ط is for Tota! A proud parrot finger"
    ),
    HandBodyGesturePreset(
      id = "horizontal_two_h",
      name = "Two Fingers Side",
      emoji = "👉",
      bodyDescription = "Index and middle fingers together, pointing to the side",
      alphabetSymbol = "چ",
      numberSymbol = "",
      alphabetWord = "Chaand (Moon) 🌙",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.TWO_FINGERS_SIDEWAYS,
      funHint = "چ is for Chaand! Three dots under the moon"
    ),
    HandBodyGesturePreset(
      id = "pinky_up_i",
      name = "Pinky Tall",
      emoji = "🤙",
      bodyDescription = "Fist closed, little pinky finger pointing straight up",
      alphabetSymbol = "ی",
      numberSymbol = "",
      alphabetWord = "Yaqoot (Ruby) 💎",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.FIST_PINKY_UP,
      funHint = "ی is for Yaqoot! The small finger shines"
    ),
    HandBodyGesturePreset(
      id = "l_shape",
      name = "L-Shape Hand",
      emoji = "📐",
      bodyDescription = "Index finger up, thumb extended to form a right angle",
      alphabetSymbol = "ل",
      numberSymbol = "",
      alphabetWord = "Laddu (Sweet) 🍬",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.L_SHAPE,
      funHint = "ل is for Laddu! Tall and proud like a flagpole"
    ),
    HandBodyGesturePreset(
      id = "circle_o",
      name = "Tiny Ring",
      emoji = "⭕",
      bodyDescription = "Thumb and fingertips meet in a small round ring",
      alphabetSymbol = "و",
      numberSymbol = "",
      alphabetWord = "Wuzu (Wash) 🚿",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.CIRCLE_O,
      funHint = "و joins words together — it means 'and'!"
    ),
    HandBodyGesturePreset(
      id = "crossed_fingers_r",
      name = "Crossed Fingers",
      emoji = "🤞",
      bodyDescription = "Index finger crossed over middle finger",
      alphabetSymbol = "ر",
      numberSymbol = "",
      alphabetWord = "Roti (Bread) 🍞",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.CROSSED_INDEX_MIDDLE_R,
      funHint = "ر is for Roti! A warm wave shape"
    ),
    HandBodyGesturePreset(
      id = "v_peace_spread",
      name = "Wide V",
      emoji = "✌️",
      bodyDescription = "Index and middle spread apart in a tall V-shape",
      alphabetSymbol = "ک",
      numberSymbol = "",
      alphabetWord = "Kitaab (Book) 📖",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.V_PEACE_SPREAD,
      funHint = "ک is for Kitaab! Friendly open scissors"
    ),
    HandBodyGesturePreset(
      id = "w_three_fingers",
      name = "Wide Open Fingers",
      emoji = "🖖",
      bodyDescription = "Fingers opened wide apart, as in the photo",
      alphabetSymbol = "خ",
      numberSymbol = "",
      alphabetWord = "Kharbooza (Melon) 🍈",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.W_THREE_FINGERS,
      funHint = "خ is for Kharbooza! One dot on top"
    ),
    HandBodyGesturePreset(
      id = "thumb_pinky_y",
      name = "Two Tall Fingers",
      emoji = "🤟",
      bodyDescription = "Index and smallest finger up, middle fingers held by thumb",
      alphabetSymbol = "ض",
      numberSymbol = "",
      alphabetWord = "Zarb (Multiply) ✖️",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.THUMB_PINKY_Y,
      funHint = "ض is for Zarb! ص wearing a dot"
    ),
    HandBodyGesturePreset(
      id = "five_open_palm",
      name = "Open Palm",
      emoji = "🖐️",
      bodyDescription = "Fingers together pointing up, thumb out to the side",
      alphabetSymbol = "ف",
      numberSymbol = "",
      alphabetWord = "Football ⚽",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.FIVE_OPEN_PALM,
      funHint = "ف is for Football! Goal!"
    ),
    HandBodyGesturePreset(
      id = "wave_hello",
      name = "Waving Hand",
      emoji = "👋",
      bodyDescription = "Hand waving gently side to side at eye level",
      alphabetSymbol = "👋",
      numberSymbol = "",
      alphabetWord = "Hello 👋",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.WAVE_HELLO,
      funHint = "A friendly greeting for everyone!"
    ),
    HandBodyGesturePreset(
      id = "cross_heart",
      name = "Hands Over Heart",
      emoji = "❤️",
      bodyDescription = "Both hands crossed flat over heart / chest",
      alphabetSymbol = "❤️",
      numberSymbol = "",
      alphabetWord = "Love ❤️",
      numberWord = "Learn with a teacher 🌱",
      pose = HandPose.CROSS_HEART,
      funHint = "A sign of love and warmth from Irssa's heart"
    )
  )
}'''
s=pat.sub(new,s)
io.open(p,'w',encoding='utf-8',newline='\n').write(s)
print('presets rewritten')

# Vowel filter on AlphabetWordsScreen uses Urdu vowels
p2='app/src/main/java/com/example/ui/screens/AlphabetWordsScreen.kt'
s2=io.open(p2,encoding='utf-8').read()
old='"VOWELS" -> LearningDataSource.alphabetItems.filter { it.title in listOf("A", "E", "I", "O", "U") }'
assert old in s2
s2=s2.replace(old,'"VOWELS" -> LearningDataSource.vowelItems')
io.open(p2,'w',encoding='utf-8',newline='\n').write(s2)
print('vowel filter fixed')

# re pose should match its crossed-finger photo
p3='app/src/main/java/com/example/data/LearningDataSource.kt'
s3=io.open(p3,encoding='utf-8').read()
i=s3.find('letter_re')
seg=s3[i:i+800]
assert 'ONE_INDEX' in seg
s3=s3[:i]+seg.replace('HandPose.ONE_INDEX','HandPose.CROSSED_INDEX_MIDDLE_R')+s3[i+800:]
io.open(p3,'w',encoding='utf-8',newline='\n').write(s3)
print('re pose fixed')

# HomeScreen level text
p4='app/src/main/java/com/example/ui/screens/HomeScreen.kt'
s4=io.open(p4,encoding='utf-8').read()
s4=s4.replace('Complete levels to unlock all 26 letters and 50 numbers!','Complete levels to unlock all 37 PSL letters and 50 numbers!')
io.open(p4,'w',encoding='utf-8',newline='\n').write(s4)
print('home text fixed')
