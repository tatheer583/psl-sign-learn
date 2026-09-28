package com.example.data

import com.example.data.model.GameLevel
import com.example.data.model.HandPose
import com.example.data.model.LearningItem
import com.example.data.model.SignCategory
import com.example.data.model.StickerBadge

object LearningDataSource {

  // --- PAKISTAN SIGN LANGUAGE ALPHABET (37 URDU LETTERS) ---
  // Handshape references from the CC BY 4.0 PSL dataset (see docs/RESEARCH.md).
  val alphabetItems: List<LearningItem> = listOf(
    LearningItem(
      id = "letter_alif",
      title = "ا",
      subtitle = "PSL Letter Alif (Urdu vowel)",
      category = SignCategory.ALPHABET,
      level = 1,
      signName = "Closed fist with thumb upright alongside",
      visualCueSteps = listOf("Make a gentle closed fist.", "Let your thumb stand tall alongside your fingers.", "Palm faces forward like a little mountain!"),
      objectEmoji = "🍎",
      funFact = "ا is for انار (anaar) — a juicy pomegranate!",
      handPoseType = HandPose.FIST_THUMB_SIDE,
      colorHex = 0xFFFF5E7E
    ),
    LearningItem(
      id = "letter_be",
      title = "ب",
      subtitle = "PSL Letter Be",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Four fingers up together, thumb folded into palm",
      visualCueSteps = listOf("Hold four fingers straight up, touching.", "Tuck your thumb into your palm.", "Show your palm to the screen like an open window!"),
      objectEmoji = "🐐",
      funFact = "ب is for بکری (bakri) — a silly goat!",
      handPoseType = HandPose.FLAT_FOUR_FOLD_THUMB,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "letter_pe",
      title = "پ",
      subtitle = "PSL Letter Pe",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Open hand of ب, turned as shown in the photo",
      visualCueSteps = listOf("Start like ب — four fingers up.", "Look at the real photo and match the gentle tilt.", "پ has three dots above the letter — count them!"),
      objectEmoji = "🪁",
      funFact = "پ is for پتنگ (patang) — a dancing kite!",
      handPoseType = HandPose.FLAT_FOUR_FOLD_THUMB,
      colorHex = 0xFF00B894
    ),
    LearningItem(
      id = "letter_te",
      title = "ت",
      subtitle = "PSL Letter Te",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index finger up, thumb resting across the hand",
      visualCueSteps = listOf("Lift your index finger straight up.", "Let your thumb rest across your middle finger.", "ت has two dots above the letter!"),
      objectEmoji = "🦋",
      funFact = "ت is for تتلی (titli) — a fluttery butterfly!",
      handPoseType = HandPose.POINT_INDEX_O,
      colorHex = 0xFFFF7849
    ),
    LearningItem(
      id = "letter_tte",
      title = "ٹ",
      subtitle = "PSL Letter Tte",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Soft closed fist, thumb placed as in the photo",
      visualCueSteps = listOf("Make a soft closed fist.", "Look at the photo and copy where your thumb sits.", "ٹ is ت wearing a little cap on top!"),
      objectEmoji = "🎩",
      funFact = "ٹ is for ٹوپی (topi) — a cosy cap!",
      handPoseType = HandPose.FIST_THUMB_SIDE,
      colorHex = 0xFFFFC312
    ),
    LearningItem(
      id = "letter_se",
      title = "ث",
      subtitle = "PSL Letter Se",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index curved into a hook, thumb touches it",
      visualCueSteps = listOf("Curve your index finger into a little hook.", "Touch your thumb gently to it.", "ث wears three dots on top!"),
      objectEmoji = "🍇",
      funFact = "ث is for ثمر (samar) — sweet fruit!",
      handPoseType = HandPose.POINT_INDEX_O,
      colorHex = 0xFFFF5E7E
    ),
    LearningItem(
      id = "letter_jim",
      title = "ج",
      subtitle = "PSL Letter Jim",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Fist with index finger up, tip curled like a hook",
      visualCueSteps = listOf("Make a fist with your index finger up.", "Curl the top of your index finger like a tiny hook.", "ج has one dot tucked inside the hook!"),
      objectEmoji = "✈️",
      funFact = "ج is for جہاز (jahaaz) — a flying aeroplane!",
      handPoseType = HandPose.HOOKED_INDEX_X,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "letter_che",
      title = "چ",
      subtitle = "PSL Letter Che",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Two fingers together, pointing to the side",
      visualCueSteps = listOf("Point your index and middle fingers to the side.", "Hold them together like a little arrow.", "چ has three dots floating below!"),
      objectEmoji = "🌙",
      funFact = "چ is for چاند (chaand) — the glowing moon!",
      handPoseType = HandPose.TWO_FINGERS_SIDEWAYS,
      colorHex = 0xFF00B894
    ),
    LearningItem(
      id = "letter_he",
      title = "ح",
      subtitle = "PSL Letter He",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Two fingers together pointing aside, gently curled",
      visualCueSteps = listOf("Hold two fingers together, pointing to the side.", "Curl them gently like a flower stem.", "ح has no dot at all!"),
      objectEmoji = "🍮",
      funFact = "ح is for حلوہ (halwa) — a warm sweet treat!",
      handPoseType = HandPose.TWO_FINGERS_SIDEWAYS,
      colorHex = 0xFFFF7849
    ),
    LearningItem(
      id = "letter_khe",
      title = "خ",
      subtitle = "PSL Letter Khe",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Fingers opened wide as shown in the photo",
      visualCueSteps = listOf("Open your fingers wide like the photo.", "Check where your thumb sits carefully.", "خ has one dot sitting on top!"),
      objectEmoji = "🍈",
      funFact = "خ is for خربوزہ (kharbooza) — a big melon!",
      handPoseType = HandPose.V_PEACE_SPREAD,
      colorHex = 0xFFFFC312
    ),
    LearningItem(
      id = "letter_dal",
      title = "د",
      subtitle = "PSL Letter Dal",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index finger bent into a hook, pointing down",
      visualCueSteps = listOf("Bend your index finger into a hook.", "Point it gently downwards.", "د is long and smooth with no dots!"),
      objectEmoji = "🚪",
      funFact = "د is for دروازہ (darwaza) — a big door!",
      handPoseType = HandPose.HOOKED_INDEX_X,
      colorHex = 0xFFFF5E7E
    ),
    LearningItem(
      id = "letter_dhal",
      title = "ڈ",
      subtitle = "PSL Letter Dhal",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index finger up, thumb and middle finger form a circle",
      visualCueSteps = listOf("Point your index finger up to the sky.", "Touch your thumb to your middle finger — a little circle!", "ڈ is د wearing a hat on top!"),
      objectEmoji = "🥁",
      funFact = "ڈ is for ڈھول (dhol) — a loud drum!",
      handPoseType = HandPose.POINT_INDEX_O,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "letter_zal",
      title = "ذ",
      subtitle = "PSL Letter Zal",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "د hook shape, hand turned as in the photo",
      visualCueSteps = listOf("Make د's hook shape with your finger.", "Turn your hand gently downwards like the photo.", "ذ has one dot above it!"),
      objectEmoji = "🌍",
      funFact = "ذ is for ذخیرہ (zakheera) — a treasure store!",
      handPoseType = HandPose.HOOKED_INDEX_X,
      colorHex = 0xFF00B894
    ),
    LearningItem(
      id = "letter_re",
      title = "ر",
      subtitle = "PSL Letter Re",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index crossed over middle finger",
      visualCueSteps = listOf("Cross your index finger over your middle finger.", "Point them gently up.", "ر flows like a little wave!"),
      objectEmoji = "🍞",
      funFact = "ر is for روٹی (roti) — warm bread!",
      handPoseType = HandPose.CROSSED_INDEX_MIDDLE_R,
      colorHex = 0xFFFF7849
    ),
    LearningItem(
      id = "letter_rre",
      title = "ڑ",
      subtitle = "PSL Letter Rre",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index and middle in a wide V",
      visualCueSteps = listOf("Hold up your index and middle finger in a wide V.", "Match the spread in the real photo.", "ڑ never starts a word — it hides inside, like بارش!"),
      objectEmoji = "🌧️",
      funFact = "ڑ hides inside words like بارش (barish) — rain!",
      handPoseType = HandPose.V_PEACE_SPREAD,
      colorHex = 0xFFFFC312
    ),
    LearningItem(
      id = "letter_ze",
      title = "ز",
      subtitle = "PSL Letter Ze",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index finger pointing to the side",
      visualCueSteps = listOf("Point your index finger to the side.", "Keep the rest folded in your fist.", "ز has one dot above it!"),
      objectEmoji = "🦓",
      funFact = "ز is for زیبرا — striped zebra!",
      handPoseType = HandPose.POINT_INDEX_THUMB_SIDE,
      colorHex = 0xFFFF5E7E
    ),
    LearningItem(
      id = "letter_zhe",
      title = "ژ",
      subtitle = "PSL Letter Zhe",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Two fingers up together, side by side",
      visualCueSteps = listOf("Hold two fingers up together, side by side.", "Match your hand to the photo.", "ژ is ز with three extra dots!"),
      objectEmoji = "🌨️",
      funFact = "ژ lives in ژالہ (zaala) — falling hail!",
      handPoseType = HandPose.TWO_FINGERS_SIDEWAYS,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "letter_seen",
      title = "س",
      subtitle = "PSL Letter Seen",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Soft closed fist as in the photo",
      visualCueSteps = listOf("Close your fingers into a soft fist.", "Look at the photo for the gentle curve.", "س flows like three little hills!"),
      objectEmoji = "🍏",
      funFact = "س is for سیب (seb) — a crunchy apple!",
      handPoseType = HandPose.CURL_ALL_FINGERS,
      colorHex = 0xFF00B894
    ),
    LearningItem(
      id = "letter_sheen",
      title = "ش",
      subtitle = "PSL Letter Sheen",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index curled over a closed fist",
      visualCueSteps = listOf("Curl your index finger over your closed fist.", "Match the shape in the photo.", "ش has three dots dancing above!"),
      objectEmoji = "🦁",
      funFact = "ش is for شیر (sher) — a roaring lion!",
      handPoseType = HandPose.HOOKED_INDEX_X,
      colorHex = 0xFFFF7849
    ),
    LearningItem(
      id = "letter_swad",
      title = "ص",
      subtitle = "PSL Letter Swad",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Fist with thumb up, palm turned towards you",
      visualCueSteps = listOf("Make a closed fist with your thumb up — like ا!", "Turn your palm gently towards you.", "ص starts with a little oval shape!"),
      objectEmoji = "🧼",
      funFact = "ص is for صابن (sabun) — bubbly soap!",
      handPoseType = HandPose.FIST_THUMB_SIDE,
      colorHex = 0xFFFFC312
    ),
    LearningItem(
      id = "letter_zwad",
      title = "ض",
      subtitle = "PSL Letter Zwad",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index and pinky up, middle fingers held by thumb",
      visualCueSteps = listOf("Lift your index finger and smallest finger.", "Fold your middle fingers and hold them with your thumb.", "ض is ص wearing one dot!"),
      objectEmoji = "✖️",
      funFact = "ض is for ضرب (zarb) — multiplying numbers!",
      handPoseType = HandPose.THUMB_PINKY_Y,
      colorHex = 0xFFFF5E7E
    ),
    LearningItem(
      id = "letter_toay",
      title = "ط",
      subtitle = "PSL Letter Toay",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index up, thumb and middle finger make a circle",
      visualCueSteps = listOf("Point your index finger up.", "Make a circle with your thumb and middle finger under it.", "ط is tall and elegant with no dots!"),
      objectEmoji = "🦜",
      funFact = "ط is for طوطا (tota) — a talking parrot!",
      handPoseType = HandPose.POINT_INDEX_O,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "letter_zoay",
      title = "ظ",
      subtitle = "PSL Letter Zoay",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Thumb touches index tip, other fingers open wide",
      visualCueSteps = listOf("Touch your thumb to your index fingertip.", "Let your other fingers open wide like a flower.", "ظ has one dot above!"),
      objectEmoji = "🏺",
      funFact = "ظ is for ظرف (zarf) — a shining pot!",
      handPoseType = HandPose.CIRCLE_O,
      colorHex = 0xFF00B894
    ),
    LearningItem(
      id = "letter_ain",
      title = "ع",
      subtitle = "PSL Letter Ain",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Hand down, thumb and index curved towards each other",
      visualCueSteps = listOf("Point your hand downwards.", "Curve your thumb and index towards each other like a beak.", "ع has a tiny س shape on top!"),
      objectEmoji = "👓",
      funFact = "ع is for عینک (ainak) — bright glasses!",
      handPoseType = HandPose.CURVED_C,
      colorHex = 0xFFFF7849
    ),
    LearningItem(
      id = "letter_ghain",
      title = "غ",
      subtitle = "PSL Letter Ghain",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Like ع, with one dot above",
      visualCueSteps = listOf("Make ع's shape with your hand.", "Ghain looks just like Ain — with a dot!", "غ has one dot above!"),
      objectEmoji = "🎈",
      funFact = "غ is for غبارہ (ghubara) — a flying balloon!",
      handPoseType = HandPose.HOOKED_INDEX_X,
      colorHex = 0xFFFFC312
    ),
    LearningItem(
      id = "letter_fay",
      title = "ف",
      subtitle = "PSL Letter Fay",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Fingers together pointing up, thumb out to the side",
      visualCueSteps = listOf("Hold your fingers together pointing up.", "Let your thumb rest out to the side.", "ف has one dot above!"),
      objectEmoji = "⚽",
      funFact = "ف is for فٹبال — goal! football!",
      handPoseType = HandPose.FLAT_FOUR_FOLD_THUMB,
      colorHex = 0xFFFF5E7E
    ),
    LearningItem(
      id = "letter_qaf",
      title = "ق",
      subtitle = "PSL Letter Qaf",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Two fingers pointing downwards",
      visualCueSteps = listOf("Point two fingers downwards like the photo.", "ق is ک with two dots above!", "Practise this one with your teacher — the camera cannot see it yet."),
      objectEmoji = "🖊️",
      funFact = "ق is for قلم (qalam) — a clever pen!",
      handPoseType = HandPose.TWO_FINGERS_SIDEWAYS,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "letter_kaf",
      title = "ک",
      subtitle = "PSL Letter Kaf",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index and middle in a wide V, thumb out",
      visualCueSteps = listOf("Open index and middle finger in a wide V.", "Let your thumb peek out to the side.", "ک opens like friendly scissors!"),
      objectEmoji = "📖",
      funFact = "ک is for کتاب (kitaab) — a book of stories!",
      handPoseType = HandPose.V_PEACE_SPREAD,
      colorHex = 0xFF00B894
    ),
    LearningItem(
      id = "letter_gaf",
      title = "گ",
      subtitle = "PSL Letter Gaf",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index finger pointing to the side",
      visualCueSteps = listOf("Point your index finger to the side.", "Keep your fist snug and strong.", "گ is ک with an extra line on top!"),
      objectEmoji = "🚗",
      funFact = "گ is for گاڑی (gaari) — a zooming car!",
      handPoseType = HandPose.POINT_INDEX_THUMB_SIDE,
      colorHex = 0xFFFF7849
    ),
    LearningItem(
      id = "letter_laam",
      title = "ل",
      subtitle = "PSL Letter Laam",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index finger up, thumb out to the side",
      visualCueSteps = listOf("Point your index finger up to the sky.", "Stretch your thumb out to the side.", "ل is tall like a flagpole!"),
      objectEmoji = "🍬",
      funFact = "ل is for لڈو (laddu) — a round sweet!",
      handPoseType = HandPose.L_SHAPE,
      colorHex = 0xFFFFC312
    ),
    LearningItem(
      id = "letter_meem",
      title = "م",
      subtitle = "PSL Letter Meem",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Fist folded, pointing downwards",
      visualCueSteps = listOf("Fold your fingers over into a soft fist.", "Point your hand gently downwards.", "م has a small circle below!", "Practise with a grown-up — the camera cannot see م yet."),
      objectEmoji = "🐟",
      funFact = "م is for مچھلی (machhli) — a swimming fish!",
      handPoseType = HandPose.CURL_ALL_FINGERS,
      colorHex = 0xFFFF5E7E
    ),
    LearningItem(
      id = "letter_noon",
      title = "ن",
      subtitle = "PSL Letter Noon",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Hand down, fingertips curled gently in",
      visualCueSteps = listOf("Point your hand downwards.", "Curl your fingertips gently in like the photo.", "ن has one dot above!"),
      objectEmoji = "🥥",
      funFact = "ن is for ناریل (nariyal) — a hard coconut!",
      handPoseType = HandPose.TWO_FINGERS_OVER_THUMB,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "letter_vao",
      title = "و",
      subtitle = "PSL Letter Vao (Urdu vowel)",
      category = SignCategory.ALPHABET,
      level = 1,
      signName = "Gentle closed fist as in the photo",
      visualCueSteps = listOf("Make a gentle closed fist.", "Match the photo for your thumb's position.", "و joins words together — it means 'and'!"),
      objectEmoji = "🚿",
      funFact = "و is for وضو (wuzu) — fresh and clean!",
      handPoseType = HandPose.CIRCLE_O,
      colorHex = 0xFF00B894
    ),
    LearningItem(
      id = "letter_hay",
      title = "ہ",
      subtitle = "PSL Letter Hay",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Index and middle in a wide V",
      visualCueSteps = listOf("Open index and middle finger in a wide V.", "Look at the photo for the gentle angle.", "ہ is round like a little drum!"),
      objectEmoji = "✋",
      funFact = "ہ is for ہاتھ (haath) — your helping hand!",
      handPoseType = HandPose.V_PEACE_SPREAD,
      colorHex = 0xFFFF7849
    ),
    LearningItem(
      id = "letter_chhoti_yeh",
      title = "ی",
      subtitle = "PSL Letter Chhoti Yeh (Urdu vowel)",
      category = SignCategory.ALPHABET,
      level = 1,
      signName = "Closed fist with smallest finger raised",
      visualCueSteps = listOf("Make a closed fist.", "Raise your smallest finger up proudly!", "ی has two dots below!"),
      objectEmoji = "💎",
      funFact = "ی is for یاقوت (yaqoot) — a red ruby!",
      handPoseType = HandPose.FIST_PINKY_UP,
      colorHex = 0xFFFFC312
    ),
    LearningItem(
      id = "letter_bari_yeh",
      title = "ے",
      subtitle = "PSL Letter Bari Yeh (Urdu vowel)",
      category = SignCategory.ALPHABET,
      level = 1,
      signName = "Index finger straight up, thumb out to the side",
      visualCueSteps = listOf("Point your index finger straight up.", "Let your thumb rest out to the side.", "ے never starts a word — it finishes them, like کھیل!"),
      objectEmoji = "🎲",
      funFact = "ے finishes words like کھیل (khel) — play!",
      handPoseType = HandPose.ONE_INDEX,
      colorHex = 0xFFFF5E7E
    ),
    LearningItem(
      id = "letter_hamza",
      title = "ء",
      subtitle = "PSL Letter Hamza",
      category = SignCategory.ALPHABET,
      level = 2,
      signName = "Thumb and index fingertips pinch into a tiny circle",
      visualCueSteps = listOf("Pinch your thumb and index fingertips together.", "Make a tiny circle — small but powerful!", "ء hops around inside words!"),
      objectEmoji = "✨",
      funFact = "ء is hamza — it hops around inside words!",
      handPoseType = HandPose.CIRCLE_O,
      colorHex = 0xFF5C52E5
    )
  )

  // --- COMPLETE 1 TO 50 NUMBERS ---
  private val numberEmojis = listOf(
    "⭐", "🐰", "🎈", "🦋", "🖐️", "🍓", "🌈", "🐙", "💎", "🌟",
    "🍎", "🚗", "🌸", "🧁", "🚀", "🐣", "🌻", "🏀", "🍭", "⚽",
    "🎨", "🎸", "🍉", "🐬", "🦊", "🍒", "🍩", "🍕", "🚲", "🎯",
    "🦄", "🥝", "🌺", "🚂", "🦁", "🥞", "🚁", "🐢", "🍇", "🏆",
    "🐳", "🧩", "🎁", "⛵", "🌴", "🍦", "🎪", "🐞", "🧸", "👑"
  )

  val numberItems: List<LearningItem> = (0..50).map { num ->
    val tens = num / 10
    val units = num % 10

    val pose = when (num) {
      1 -> HandPose.ONE_INDEX
      2 -> HandPose.TWO_PEACE
      3 -> HandPose.THREE_THUMB_TWO
      4 -> HandPose.FOUR_FINGERS
      5 -> HandPose.FIVE_OPEN_PALM
      6 -> HandPose.THUMB_TOUCH_PINKY
      7 -> HandPose.THUMB_TOUCH_RING
      8 -> HandPose.THUMB_TOUCH_MIDDLE
      9 -> HandPose.THUMB_TOUCH_INDEX
      10 -> HandPose.FIST_THUMB_SIDE
      else -> HandPose.COMPOUND_TENS
    }

    val cue = when {
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

    val emoji = numberEmojis.getOrElse(num - 1) { "⭐" }
    val colorHex = when (num % 5) {
      0 -> 0xFFFF5E7E
      1 -> 0xFF5C52E5
      2 -> 0xFF00B894
      3 -> 0xFFFF7849
      else -> 0xFFFFC312
    }

    val level = when {
      num <= 5 -> 1
      num <= 10 -> 2
      num <= 25 -> 3
      else -> 4
    }

    LearningItem(
      id = "num_$num",
      title = "$num",
      subtitle = "Number $num",
      category = SignCategory.NUMBER,
      level = level,
      signName = if (num <= 10) "PSL number sign for $num — learn it with your teacher" else "Compound sign for $num — learn it with your teacher",
      visualCueSteps = cue,
      visualItemsCount = if (num <= 10) num else 0,
      objectEmoji = emoji,
      funFact = "$num $emoji objects! You're an incredible math & sign explorer!",
      handPoseType = pose,
      colorHex = colorHex
    )
  }

  // --- EVERYDAY VOCABULARY ---
  val vocabularyItems: List<LearningItem> = listOf(
    LearningItem(
      id = "vocab_hello",
      title = "Hello",
      subtitle = "Greeting Word",
      category = SignCategory.VOCABULARY,
      level = 3,
      signName = "Learned together with a PSL teacher",
      visualCueSteps = listOf("Wave your hand warmly, just like the photo of a friendly hello.", "Practise the PSL sign with your teacher or family.", "Smile — faces talk too!"),
      objectEmoji = "👋",
      funFact = "A warm hello can be waved, signed, or smiled!",
      handPoseType = HandPose.WAVE_HELLO,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "vocab_thanks",
      title = "Thank You",
      subtitle = "Polite Word",
      category = SignCategory.VOCABULARY,
      level = 3,
      signName = "Learned together with a PSL teacher",
      visualCueSteps = listOf("Think of something kind someone did for you today.", "Learn the PSL sign for Thank You with your teacher.", "Say it with your whole face, not just your hands!"),
      objectEmoji = "💖",
      funFact = "Thanking a friend makes both hearts warm!",
      handPoseType = HandPose.WAVE_HELLO,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "vocab_water",
      title = "Water",
      subtitle = "Drink Word",
      category = SignCategory.VOCABULARY,
      level = 3,
      signName = "Learned together with a PSL teacher",
      visualCueSteps = listOf("When you feel thirsty, show your teacher the PSL sign for Water.", "Practise it together before snack time.", "Water keeps your brain strong for learning!"),
      objectEmoji = "💧",
      funFact = "Asking for water by sign is a real superpower!",
      handPoseType = HandPose.WAVE_HELLO,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "vocab_friend",
      title = "Friend",
      subtitle = "Social Word",
      category = SignCategory.VOCABULARY,
      level = 3,
      signName = "Learned together with a PSL teacher",
      visualCueSteps = listOf("Think of your best friend and their name in signs.", "Learn the PSL sign for Friend with your teacher.", "Friends can sign to each other across the room!"),
      objectEmoji = "🤝",
      funFact = "Friendship signs are like secret handshakes!",
      handPoseType = HandPose.WAVE_HELLO,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "vocab_book",
      title = "Book",
      subtitle = "School Word",
      category = SignCategory.VOCABULARY,
      level = 3,
      signName = "Learned together with a PSL teacher",
      visualCueSteps = listOf("Hold an imaginary book in your hands.", "Learn the PSL sign for Book with your teacher.", "Every book is an adventure waiting for you!"),
      objectEmoji = "📖",
      funFact = "Books open worlds — and signs open books!",
      handPoseType = HandPose.WAVE_HELLO,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "vocab_happy",
      title = "Happy",
      subtitle = "Feeling Word",
      category = SignCategory.VOCABULARY,
      level = 3,
      signName = "Learned together with a PSL teacher",
      visualCueSteps = listOf("Touch your chest — can you feel your happy heartbeat?", "Learn the PSL sign for Happy with your teacher.", "Show your happy face together with the sign!"),
      objectEmoji = "😄",
      funFact = "Happiness shows in hands and faces at the same time!",
      handPoseType = HandPose.WAVE_HELLO,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "vocab_school",
      title = "School",
      subtitle = "Place Word",
      category = SignCategory.VOCABULARY,
      level = 3,
      signName = "Learned together with a PSL teacher",
      visualCueSteps = listOf("Think about your classroom and your friends there.", "Learn the PSL sign for School with your teacher.", "Sign it every morning when you arrive!"),
      objectEmoji = "🏫",
      funFact = "School is where signs and friends grow!",
      handPoseType = HandPose.WAVE_HELLO,
      colorHex = 0xFF5C52E5
    ),
    LearningItem(
      id = "vocab_love",
      title = "Love",
      subtitle = "Heart Word",
      category = SignCategory.VOCABULARY,
      level = 3,
      signName = "Learned together with a PSL teacher",
      visualCueSteps = listOf("Hug yourself gently — that is what love feels like.", "Learn the PSL sign for Love with your teacher.", "Send a sign of love to someone far away!"),
      objectEmoji = "❤️",
      funFact = "Love is the first sign many families learn!",
      handPoseType = HandPose.WAVE_HELLO,
      colorHex = 0xFF5C52E5
    )
  )

  // COMBINED LIST (All 37 PSL letters + 50 Numbers + Vocabulary)
  val allItems: List<LearningItem> = alphabetItems + numberItems + vocabularyItems

  // Filter helper subsets
  val vowelItems: List<LearningItem> = alphabetItems.filter {
    it.title in listOf("ا", "و", "ی", "ے")
  }

  val levels: List<GameLevel> = listOf(
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

  val badges: List<StickerBadge> = listOf(
    StickerBadge("badge_first_vowel", "Vowel Explorer", "Signed your very first Urdu vowel ا!", "🍎", 3),
    StickerBadge("badge_vowels_all", "Vowel Rainbow", "Mastered the Urdu vowels ا, و, ی and ے with glowing confidence", "🌈", 15),
    StickerBadge("badge_alpha_half", "Half Alphabet Star", "Discovered the first PSL letters!", "🔤", 25),
    StickerBadge("badge_alpha_complete", "Full PSL Master", "Learned all 37 Pakistan Sign Language letters!", "🏅", 40),
    StickerBadge("badge_high_five", "High-Five Hero", "Counted all 5 fingers in sign language", "🖐️", 50),
    StickerBadge("badge_count_twenty", "Counting Star 20", "Counted all numbers from 1 to 20!", "🔢", 65),
    StickerBadge("badge_count_fifty", "Super 50 Champion", "Counted all the way from 1 to 50!", "💯", 85),
    StickerBadge("badge_polite", "Polite Butterfly", "Learned to sign Hello and Thank You", "🦋", 95),
    StickerBadge("badge_camera_champ", "Camera Magician", "Successfully matched 5 camera gestures in real-time", "📸", 110),
    StickerBadge("badge_speed_match", "Speed Reflex Champ", "Scored 5 consecutive correct matches in Speed Match", "⚡", 130),
    StickerBadge("badge_golden_crown", "Irssa The Champion", "Mastered all levels and collected 150+ stars!", "👑", 150)
  )
}
