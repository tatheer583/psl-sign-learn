package com.example.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProgressRepository(private val progressDao: ProgressDao) {

  val progressFlow: Flow<ProgressEntity> = progressDao.getProgressFlow().map {
    it ?: ProgressEntity()
  }

  suspend fun getCurrentProgress(): ProgressEntity {
    return progressDao.getProgress() ?: ProgressEntity()
  }

  suspend fun saveProgress(entity: ProgressEntity) {
    progressDao.insertOrUpdate(entity)
  }

  suspend fun addStars(additionalStars: Int, completedItemId: String? = null) {
    val current = getCurrentProgress()
    val updatedCompleted = if (completedItemId != null && !current.completedItemsCsv.split(",").contains(completedItemId)) {
      if (current.completedItemsCsv.isEmpty()) completedItemId else "${current.completedItemsCsv},$completedItemId"
    } else {
      current.completedItemsCsv
    }

    val newStars = current.stars + additionalStars

    // Calculate level unlock based on stars
    var newUnlockedLevel = current.unlockedLevel
    if (newStars >= 90 && newUnlockedLevel < 5) newUnlockedLevel = 5
    else if (newStars >= 60 && newUnlockedLevel < 4) newUnlockedLevel = 4
    else if (newStars >= 35 && newUnlockedLevel < 3) newUnlockedLevel = 3
    else if (newStars >= 15 && newUnlockedLevel < 2) newUnlockedLevel = 2

    val updated = current.copy(
      stars = newStars,
      unlockedLevel = newUnlockedLevel,
      completedItemsCsv = updatedCompleted,
      lastPlayedEpoch = System.currentTimeMillis()
    )
    progressDao.insertOrUpdate(updated)
  }

  suspend fun setOnboardingCompleted() {
    val current = getCurrentProgress()
    progressDao.insertOrUpdate(current.copy(hasCompletedOnboarding = true))
  }

  suspend fun unlockBadge(badgeId: String) {
    val current = getCurrentProgress()
    val badges = current.unlockedBadgesCsv.split(",").filter { it.isNotBlank() }.toMutableSet()
    badges.add(badgeId)
    progressDao.insertOrUpdate(current.copy(unlockedBadgesCsv = badges.joinToString(",")))
  }

  suspend fun resetProgress() {
    progressDao.insertOrUpdate(ProgressEntity())
  }
}
