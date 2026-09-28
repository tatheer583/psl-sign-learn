package com.example

import com.example.data.LearningDataSource
import com.example.data.model.SignCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testLearningDataSourceIntegrity() {
    val items = LearningDataSource.allItems
    assertTrue("Should have learning items", items.isNotEmpty())

    // All 37 Urdu letters of the PSL alphabet present
    val alphabet = LearningDataSource.alphabetItems
    assertEquals(37, alphabet.size)
    val alphabetLetters = alphabet.map { it.id }.toSet()
    for (id in listOf(
      "letter_alif","letter_be","letter_pe","letter_te","letter_tte","letter_se","letter_jim",
      "letter_che","letter_he","letter_khe","letter_dal","letter_dhal","letter_zal","letter_re",
      "letter_rre","letter_ze","letter_zhe","letter_seen","letter_sheen","letter_swad","letter_zwad",
      "letter_toay","letter_zoay","letter_ain","letter_ghain","letter_fay","letter_qaf","letter_kaf",
      "letter_gaf","letter_laam","letter_meem","letter_noon","letter_vao","letter_hay",
      "letter_chhoti_yeh","letter_bari_yeh","letter_hamza")) {
      assertTrue("Missing PSL letter $id", alphabetLetters.contains(id))
    }

    // 26 English (ASL) letters present, distinct from the Urdu PSL set
    val english = LearningDataSource.englishAlphabetItems
    assertEquals(26, english.size)
    assertTrue(english.none { it.id in alphabetLetters })

    // 0 to 50 numbers present
    val numbers = LearningDataSource.numberItems
    assertEquals(51, numbers.size)
    val numberTitles = numbers.map { it.title }.toSet()
    for (num in 0..50) {
      assertTrue("Missing number $num", numberTitles.contains(num.toString()))
    }

    // Levels check
    val levels = LearningDataSource.levels
    assertEquals(5, levels.size)
    assertEquals(1, levels.first().id)
    assertEquals(0, levels.first().requiredStars)
  }

  @Test
  fun testLevel1ItemsPresent() {
    val level1 = LearningDataSource.levels.first { it.id == 1 }
    assertNotNull(level1)
    val itemIds = level1.itemIds
    assertTrue(itemIds.contains("letter_alif"))
    assertTrue(itemIds.contains("num_1"))
  }

  @Test
  fun testLetterWordMappings() {
    val letterAlif = LearningDataSource.alphabetItems.first { it.id == "letter_alif" }
    assertEquals("ا", letterAlif.title)
    assertEquals("Anaar (Pomegranate)", letterAlif.wordLabel)
    assertEquals("🍎", letterAlif.objectEmoji)

    val letterBe = LearningDataSource.alphabetItems.first { it.id == "letter_be" }
    assertEquals("Bakri (Goat)", letterBe.wordLabel)
    assertEquals("🐐", letterBe.objectEmoji)

    val letterQaf = LearningDataSource.alphabetItems.first { it.id == "letter_qaf" }
    assertEquals("Qalam (Pen)", letterQaf.wordLabel)
    assertEquals("🖊️", letterQaf.objectEmoji)

    val letterLaam = LearningDataSource.alphabetItems.first { it.id == "letter_laam" }
    assertEquals("Laddu (Sweet)", letterLaam.wordLabel)
    assertEquals("🍬", letterLaam.objectEmoji)
  }

  @Test
  fun testGesturePresetsDataSource() {
    val presets = com.example.data.model.GesturePresetsDataSource.presets
    assertTrue("Should have gesture presets", presets.isNotEmpty())

    val fist = presets.first { it.id == "fist_thumb_side" }
    assertEquals("ا", fist.alphabetSymbol)
    assertEquals("", fist.numberSymbol)

    val peace = presets.first { it.id == "v_peace_spread" }
    assertEquals("ک", peace.alphabetSymbol)
    assertEquals("", peace.numberSymbol)

    val openPalm = presets.first { it.id == "five_open_palm" }
    assertEquals("ف", openPalm.alphabetSymbol)

    // Test ActiveScreen has GESTURE_CONVERTER
    val screens = com.example.data.model.ActiveScreen.values()
    assertTrue(screens.contains(com.example.data.model.ActiveScreen.GESTURE_CONVERTER))
  }
}
