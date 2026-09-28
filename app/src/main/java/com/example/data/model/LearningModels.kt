package com.example.data.model

enum class SignCategory {
  ALPHABET,    // All letters A to Z
  VOWEL,       // A, E, I, O, U subset
  NUMBER,      // All numbers 1 to 50
  VOCABULARY   // School, feelings, greetings, everyday words
}

enum class HandPose {
  FIST_THUMB_SIDE,        // A, 10
  FLAT_FOUR_FOLD_THUMB,   // B
  CURVED_C,               // C
  POINT_INDEX_O,          // D
  CURL_ALL_FINGERS,       // E
  THREE_FINGERS_UP_OK,    // F
  POINT_INDEX_THUMB_SIDE, // G
  TWO_FINGERS_SIDEWAYS,   // H
  FIST_PINKY_UP,          // I
  PINKY_SWOOP_J,          // J
  PEACE_THUMB_MIDDLE_K,   // K
  L_SHAPE,                // L
  THREE_FINGERS_OVER_THUMB,// M
  TWO_FINGERS_OVER_THUMB, // N
  CIRCLE_O,               // O
  P_DOWNWARD_K,           // P
  Q_DOWNWARD_G,           // Q
  CROSSED_INDEX_MIDDLE_R, // R
  FIST_THUMB_OVER_S,      // S
  THUMB_BETWEEN_INDEX_T,  // T
  FIST_THUMB_INDEX,       // U
  V_PEACE_SPREAD,         // V
  W_THREE_FINGERS,        // W
  HOOKED_INDEX_X,         // X
  THUMB_PINKY_Y,          // Y
  INDEX_Z_DRAW,           // Z
  ONE_INDEX,              // 1
  TWO_PEACE,              // 2
  THREE_THUMB_TWO,        // 3
  FOUR_FINGERS,           // 4
  FIVE_OPEN_PALM,         // 5
  THUMB_TOUCH_PINKY,      // 6
  THUMB_TOUCH_RING,       // 7
  THUMB_TOUCH_MIDDLE,     // 8
  THUMB_TOUCH_INDEX,      // 9
  COMPOUND_TENS,          // 20, 30, 40, 50, etc.
  WAVE_HELLO,             // Vocab Hello
  CHIN_FORWARD,           // Vocab Thank You
  W_TAP_CHIN,             // Vocab Water
  INDEX_HOOK,             // Vocab Friend
  PRAY_OPEN,              // Vocab Book
  HAND_SWEEP_CHEST,       // Vocab Happy
  CLAP_SCHOOL,            // Vocab School
  CROSS_HEART             // Vocab Love
}

data class LearningItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val category: SignCategory,
  val level: Int,
  val signName: String,
  val visualCueSteps: List<String>,
  val visualItemsCount: Int = 0,
  val objectEmoji: String,
  val funFact: String,
  val handPoseType: HandPose,
  val colorHex: Long,
  val vibrationPattern: LongArray = longArrayOf(0, 150),
  val associatedWord: String = ""
) {
  val wordLabel: String
    get() = when (id) {
      "letter_en_a" -> "Apple"
      "letter_en_b" -> "Bear"
      "letter_en_c" -> "Cat"
      "letter_en_d" -> "Dog"
      "letter_en_e" -> "Elephant"
      "letter_en_f" -> "Fox"
      "letter_en_g" -> "Giraffe"
      "letter_en_h" -> "Horse"
      "letter_en_i" -> "Ice Cream"
      "letter_en_j" -> "Jelly"
      "letter_en_k" -> "Kangaroo"
      "letter_en_l" -> "Lion"
      "letter_en_m" -> "Monkey"
      "letter_en_n" -> "Nest"
      "letter_en_o" -> "Orange"
      "letter_en_p" -> "Panda"
      "letter_en_q" -> "Queen"
      "letter_en_r" -> "Rainbow"
      "letter_en_s" -> "Star"
      "letter_en_t" -> "Turtle"
      "letter_en_u" -> "Umbrella"
      "letter_en_v" -> "Violin"
      "letter_en_w" -> "Whale"
      "letter_en_x" -> "Xylophone"
      "letter_en_y" -> "Yo-Yo"
      "letter_en_z" -> "Zebra"
      "letter_alif" -> "Anaar (Pomegranate)"
      "letter_be" -> "Bakri (Goat)"
      "letter_pe" -> "Patang (Kite)"
      "letter_te" -> "Titli (Butterfly)"
      "letter_tte" -> "Topi (Cap)"
      "letter_se" -> "Samar (Fruit)"
      "letter_jim" -> "Jahaaz (Airplane)"
      "letter_che" -> "Chaand (Moon)"
      "letter_he" -> "Halwa (Sweet)"
      "letter_khe" -> "Kharbooza (Melon)"
      "letter_dal" -> "Darwaza (Door)"
      "letter_dhal" -> "Dhol (Drum)"
      "letter_zal" -> "Zameen (Earth)"
      "letter_re" -> "Roti (Bread)"
      "letter_rre" -> "Barish (Rain)"
      "letter_ze" -> "Zebra"
      "letter_zhe" -> "Zaala (Hail)"
      "letter_seen" -> "Seb (Apple)"
      "letter_sheen" -> "Sher (Lion)"
      "letter_swad" -> "Sabun (Soap)"
      "letter_zwad" -> "Zarb (Multiply)"
      "letter_toay" -> "Tota (Parrot)"
      "letter_zoay" -> "Zarf (Pot)"
      "letter_ain" -> "Ainak (Glasses)"
      "letter_ghain" -> "Ghubara (Balloon)"
      "letter_fay" -> "Football"
      "letter_qaf" -> "Qalam (Pen)"
      "letter_kaf" -> "Kitaab (Book)"
      "letter_gaf" -> "Gaari (Car)"
      "letter_laam" -> "Laddu (Sweet)"
      "letter_meem" -> "Machhli (Fish)"
      "letter_noon" -> "Nariyal (Coconut)"
      "letter_vao" -> "Wuzu (Wash)"
      "letter_hay" -> "Haath (Hand)"
      "letter_chhoti_yeh" -> "Yaqoot (Ruby)"
      "letter_bari_yeh" -> "Khel (Play)"
      "letter_hamza" -> "Juz (Part)"
      else -> when {
        associatedWord.isNotEmpty() -> associatedWord
        category == SignCategory.NUMBER -> "Number $title"
        else -> title
      }
    }
}

data class GameLevel(
  val id: Int,
  val title: String,
  val subtitle: String,
  val description: String,
  val requiredStars: Int,
  val badgeReward: String,
  val badgeEmoji: String,
  val itemIds: List<String>,
  val colorHex: Long
)

data class StickerBadge(
  val id: String,
  val title: String,
  val description: String,
  val iconEmoji: String,
  val requiredStars: Int,
  val isUnlocked: Boolean = false,
  val category: String = "Explorer"
)

enum class ActiveScreen {
  ONBOARDING,
  HOME,
  LEARN,
  QUIZ,
  CAMERA_STUDIO,
  STICKERS,
  SPEECH_TO_SIGN,
  SPEED_MATCH,
  LEARN_ALPHABET,
  LEARN_NUMBERS,
  ALPHABET_WORDS,
  CAMERA_QUIZ,
  GESTURE_CONVERTER
}
