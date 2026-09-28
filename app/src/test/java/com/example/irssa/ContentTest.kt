package com.example.irssa

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class ContentTest {
  private val app: Application get()=ApplicationProvider.getApplicationContext()
  @Test fun allLessonMediaCanBeOpenedOffline() {
    val lessons=LessonCatalog.load(app)
    assertEquals(122,lessons.size)
    assertEquals(122,lessons.map { it.id }.distinct().size)
    val withMedia=lessons.filter { it.media.isNotBlank() }
    assertEquals(63,withMedia.size)
    withMedia.forEach { item ->
      app.assets.open(item.media).use { assertTrue("Missing ${item.id}",it.read()!=-1) }
      assertTrue(item.source.startsWith("https://"));assertTrue(item.author.isNotBlank())
      val lic=item.license
      assertTrue("Open license required: $lic",lic.contains("CC BY 4.0")||lic.contains("CC0"))
    }
  }
  @Test fun modelRejectsInvalidHandData() {
    for(mode in listOf("psl","english")) {
      val model=LandmarkClassifier(app.assets.open("models/$mode.bin"))
      assertNull(model.predict(emptyList()))
    }
  }
  @Test fun onlyDetectedHandshapesAreGradedByTheStaticClassifier() {
    val lessons=LessonCatalog.load(app)
    val static=lessons.filter { it.category=="Letters" && it.isStatic }
    assertEquals(37,static.count { !it.isEnglish })
    assertEquals(24,static.count { it.isEnglish })
    assertFalse(lessons.first { it.id=="letter_en_j" }.isStatic)
    assertFalse(lessons.first { it.id=="letter_en_z" }.isStatic)
    assertTrue(lessons.filter { it.category=="Words" }.none { it.isStatic })
    assertTrue(lessons.filter { it.category=="Numbers" }.none { it.isStatic })
  }
  @Test fun savedProgressRoundTrips() {
    val s=LearnerState(name="Irssa",welcomed=true,stars=52,seen=setOf("letter_alif","num_1"),favorites=setOf("vocab_hello"),haptics=false)
    assertEquals(s,LearningStore.decode(LearningStore.encode(s)))
  }
  @Test fun malformedSaveIsNotSilentlyReset() {
    try { LearningStore.decode("broken");fail("Corrupt data must fail") } catch(_: Exception) { }
  }
  @Test fun wordsUseVocabularyWhileNamesUseFingerspelling() {
    val lessons=LessonCatalog.load(app)
    assertEquals(listOf("vocab_hello"),Spelling.sequence("hello",lessons).map { it.id })
    assertEquals(listOf("vocab_thanks"),Spelling.sequence("Thank You",lessons).map { it.id })
    assertEquals(listOf("num_1","num_0"),Spelling.sequence("10",lessons).map { it.id })
    assertEquals(listOf("letter_en_i","letter_en_r","letter_en_s","letter_en_s","letter_en_a"),Spelling.sequence("IRSSA",lessons).map { it.id })
    val both=LessonCatalog.load(app)
    assertTrue(both.count { it.isEnglish }>=26)
  }
}
