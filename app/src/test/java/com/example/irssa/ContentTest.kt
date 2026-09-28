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
    assertEquals(96,lessons.size)
    assertEquals(96,lessons.map { it.id }.distinct().size)
    val withMedia=lessons.filter { it.media.isNotBlank() }
    assertEquals(37,withMedia.size)
    withMedia.forEach { item ->
      app.assets.open(item.media).use { assertTrue("Missing ${item.id}",it.read()!=-1) }
      assertTrue(item.source.startsWith("https://"));assertTrue(item.author.isNotBlank())
      assertTrue("PSL photos must be CC BY 4.0",item.license.contains("CC BY 4.0"))
    }
  }
  @Test fun modelRejectsInvalidHandData() {
    val model=LandmarkClassifier(app.assets.open("models/alphabet.bin"))
    assertNull(model.predict(emptyList()))
  }
  @Test fun onlyDetectedHandshapesAreGradedByTheStaticClassifier() {
    val lessons=LessonCatalog.load(app)
    assertEquals(34,lessons.count { it.category=="Letters" && it.isStatic })
    for(id in listOf("letter_qaf","letter_meem","letter_ghain")) {
      assertFalse("$id has too few usable detections to be graded",lessons.first { it.id==id }.isStatic)
    }
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
    assertEquals(listOf("letter_alif","letter_re","letter_seen","letter_seen","letter_alif"),Spelling.sequence("IRSSA",lessons).map { it.id })
  }
}
