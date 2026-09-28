package com.example.irssa

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ProgressTest {
  @Test fun repeatPracticeCannotFarmStars() {
    var s=LearnerState()
    repeat(20) { s=ProgressRules.reward(s,"letter_a","camera","2026-09-28") }
    assertEquals(10,s.stars);assertEquals(setOf("letter_a"),s.mastered)
  }
  @Test fun watchingDoesNotClaimCameraMastery() {
    val s=ProgressRules.reward(LearnerState(),"letter_a","learn","2026-09-28")
    assertEquals(2,s.stars);assertTrue(s.mastered.isEmpty());assertEquals(1,s.todayCount("2026-09-28"))
  }
  @Test fun uniqueDailyLessonsDriveGoal() {
    var s=LearnerState()
    s=ProgressRules.reward(s,"letter_a","learn","2026-09-28")
    s=ProgressRules.reward(s,"letter_a","quiz","2026-09-28")
    s=ProgressRules.reward(s,"letter_b","learn","2026-09-28")
    assertEquals(2,s.todayCount("2026-09-28"));assertEquals(0,s.todayCount("2026-09-29"))
  }
  @Test fun streakSurvivesUntilTodayAndBreaksAfterMissedDay() {
    val s=LearnerState(practiceDays=setOf("2026-09-26","2026-09-27"))
    assertEquals(2,s.streak(LocalDate.parse("2026-09-28")))
    assertEquals(0,s.streak(LocalDate.parse("2026-09-29")))
  }
  @Test fun zeroScoreLearnerDoesNotHaveEarnedBadges() {
    assertTrue(achievements(LearnerState()).none { it.earned })
  }
  @Test fun quizSuccessDoesNotClaimCameraMastery() {
    val s=ProgressRules.reward(LearnerState(),"letter_b","quiz","2026-09-28")
    assertTrue(s.mastered.isEmpty());assertEquals(5,s.stars)
  }
}
