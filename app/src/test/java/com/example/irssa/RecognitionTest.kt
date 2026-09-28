package com.example.irssa

import org.junit.Assert.*
import org.junit.Test

class RecognitionTest {
  private fun hand()=List(21) { i -> Landmark((i%5)*0.08f,(i/5)*0.09f,i*0.002f) }
  @Test fun normalizationIsTranslationAndScaleInvariant() {
    val points=hand()
    val moved=points.map { Landmark(it.x*2+0.3f,it.y*2-0.5f,it.z*2+0.1f) }
    assertArrayEquals(LandmarkFeatures.normalize(points),LandmarkFeatures.normalize(moved),0.00001f)
  }
  @Test fun mirroredHandsHaveSameFeatures() {
    assertArrayEquals(LandmarkFeatures.normalize(hand()),LandmarkFeatures.normalize(hand().map { it.copy(x=1-it.x) }),0.00001f)
  }
  @Test fun missingDegenerateAndNonFiniteHandsAreRejected() {
    assertNull(LandmarkFeatures.normalize(emptyList()))
    assertNull(LandmarkFeatures.normalize(List(21) { Landmark(0f,0f,0f) }))
    assertNull(LandmarkFeatures.normalize(hand().toMutableList().apply { this[4]=Landmark(Float.NaN,0f,0f) }))
  }
  @Test fun emptyCameraNeverConfirms() {
    val gate=StableSignGate()
    for(t in 0L..10000L step 100) assertFalse(gate.update(null,t).confirmed)
  }
  @Test fun differentShapesDoNotAccumulateSuccess() {
    val gate=StableSignGate()
    for(t in 0L..10000L step 100) assertFalse(gate.update(if(t%200L==0L) "A" else "B",t).confirmed)
  }
  @Test fun stableEvidenceConfirmsOnlyOnceUntilHandIsRemoved() {
    val gate=StableSignGate();var confirmations=0
    for(t in 0L..3000L step 100) if(gate.update("A",t).confirmed) confirmations++
    assertEquals(1,confirmations)
    gate.update(null,3100);gate.update(null,3700)
    for(t in 3800L..5300L step 100) if(gate.update("A",t).confirmed) confirmations++
    assertEquals(2,confirmations)
  }
  @Test fun backgroundGapCannotCompleteAHold() {
    val gate=StableSignGate()
    gate.update("A",0);gate.update("A",400)
    assertEquals(0f,gate.update("A",30000).progress)
    assertFalse(gate.update("A",30100).confirmed)
  }
  @Test fun resetRemovesPreviousLessonEvidence() {
    val gate=StableSignGate();gate.update("A",0);gate.update("A",400);gate.reset()
    assertEquals(0f,gate.update("A",500).progress)
  }
  @Test fun uncertainVisibleHandDoesNotRearmConfirmedLetter() {
    val gate=StableSignGate()
    for(t in 0L..1500L step 100) gate.update("A",t)
    for(t in 1600L..3000L step 100) gate.update(null,t,handPresent=true)
    for(t in 3100L..5000L step 100) assertFalse(gate.update("A",t).confirmed)
  }
}
