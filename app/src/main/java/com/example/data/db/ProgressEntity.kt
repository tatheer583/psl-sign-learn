package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class ProgressEntity(
  @PrimaryKey val id: Int = 1,
  val childName: String = "Irssa",
  val stars: Int = 0,
  val unlockedLevel: Int = 1,
  val completedItemsCsv: String = "",
  val unlockedBadgesCsv: String = "",
  val hasCompletedOnboarding: Boolean = false,
  val streakDays: Int = 1,
  val lastPlayedEpoch: Long = System.currentTimeMillis()
)
