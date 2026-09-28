package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LearningDataSource
import com.example.data.db.ProgressEntity
import com.example.data.model.LearningItem
import com.example.util.HapticUtil
import kotlinx.coroutines.delay

@Composable
fun SpeedMatchScreen(
  progress: ProgressEntity,
  onAddScore: (Int) -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  BackHandler { onBack() }

  var score by remember { mutableIntStateOf(0) }
  var streak by remember { mutableIntStateOf(0) }
  var timeLeftSeconds by remember { mutableIntStateOf(45) }
  var isGameOver by remember { mutableStateOf(false) }

  var currentTarget by remember { mutableStateOf(LearningDataSource.alphabetItems.random()) }
  var options by remember { mutableStateOf(listOf<LearningItem>()) }
  var feedbackCorrect by remember { mutableStateOf<Boolean?>(null) }

  fun setupNewRound() {
    feedbackCorrect = null
    // Pick from all 26 letters and first 20 numbers
    val pool = (LearningDataSource.alphabetItems + LearningDataSource.numberItems.take(20)).shuffled()
    val target = pool.first()
    currentTarget = target
    val distractors = pool.filter { it.id != target.id }.take(3)
    options = (distractors + target).shuffled()
  }

  LaunchedEffect(Unit) {
    setupNewRound()
  }

  // Countdown timer
  LaunchedEffect(isGameOver) {
    while (!isGameOver && timeLeftSeconds > 0) {
      delay(1000)
      timeLeftSeconds--
    }
    if (timeLeftSeconds <= 0 && !isGameOver) {
      isGameOver = true
      HapticUtil.playCelebration(context)
      onAddScore(score)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFD))
      .padding(horizontal = 20.dp, vertical = 20.dp)
      .testTag("speed_match_screen"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Header Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = {
            HapticUtil.playTap(context)
            onBack()
          },
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = Color(0xFF1E293B)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Speed Match ⚡",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1E293B)
          )
          Text(
            text = "Fast reflexes earn bonus stars!",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B)
          )
        }
      }

      // Time Remaining Badge
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (timeLeftSeconds <= 10) Color(0xFFFF5252) else Color(0xFF5C52E5),
        shadowElevation = 2.dp
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Rounded.Bolt,
            contentDescription = "Timer",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${timeLeftSeconds}s",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = Color.White
          )
        }
      }
    }

    if (!isGameOver) {
      // Score and Streak Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFFFFC312).copy(alpha = 0.20f)
        ) {
          Text(
            text = "Score: $score pts",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B4700),
            fontSize = 13.sp
          )
        }

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFF00B894).copy(alpha = 0.20f)
        ) {
          Text(
            text = "🔥 Streak: $streak",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.Bold,
            color = Color(0xFF005845),
            fontSize = 13.sp
          )
        }
      }

      // Prompt Card: Show Sign target
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Find this Sign!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF64748B)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .size(90.dp)
              .clip(CircleShape)
              .background(Color(currentTarget.colorHex).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = currentTarget.title,
              fontWeight = FontWeight.Black,
              fontSize = 44.sp,
              color = Color(currentTarget.colorHex)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = currentTarget.signName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Center
          )
        }
      }

      // 4 Rapid Option Buttons (2x2 Grid)
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        options.chunked(2).forEach { rowOptions ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            rowOptions.forEach { opt ->
              Card(
                modifier = Modifier
                  .weight(1f)
                  .height(88.dp)
                  .clip(RoundedCornerShape(20.dp))
                  .clickable {
                    val isMatch = opt.id == currentTarget.id
                    feedbackCorrect = isMatch
                    if (isMatch) {
                      HapticUtil.playSuccess(context)
                      score += 10
                      streak += 1
                    } else {
                      HapticUtil.playPromptAlert(context)
                      streak = 0
                    }
                    setupNewRound()
                  },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(opt.colorHex).copy(alpha = 0.35f))
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Text(text = opt.objectEmoji, fontSize = 24.sp)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = opt.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color(opt.colorHex)
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
    } else {
      // Game Over Summary Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(text = "🏆", fontSize = 54.sp)
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Game Over!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1E293B)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "You scored $score points in Speed Match!",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(16.dp))
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFC312)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Rounded.Star, contentDescription = "Stars", tint = Color(0xFF422C00))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "+${score / 2} Stars Earned!",
                fontWeight = FontWeight.Black,
                color = Color(0xFF422C00),
                fontSize = 16.sp
              )
            }
          }
          Spacer(modifier = Modifier.height(20.dp))
          Button(
            onClick = {
              HapticUtil.playTap(context)
              timeLeftSeconds = 45
              score = 0
              streak = 0
              isGameOver = false
              setupNewRound()
            },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C52E5)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Play Again", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
        }
      }
    }
  }
}
