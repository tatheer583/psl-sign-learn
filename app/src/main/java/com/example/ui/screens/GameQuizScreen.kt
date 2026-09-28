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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ProgressEntity
import com.example.data.model.LearningItem
import com.example.ui.components.CelebrationOverlay
import com.example.ui.components.HandPoseCanvas
import com.example.util.HapticUtil
import com.example.viewmodel.QuizQuestion

@Composable
fun GameQuizScreen(
  question: QuizQuestion?,
  selectedOption: LearningItem?,
  isAnswerCorrect: Boolean?,
  progress: ProgressEntity,
  onSelectAnswer: (LearningItem) -> Unit,
  onNextQuestion: () -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  BackHandler { onBack() }

  var showCelebrationOverlay by remember { mutableStateOf(false) }

  LaunchedEffect(isAnswerCorrect) {
    if (isAnswerCorrect == true) {
      showCelebrationOverlay = true
    } else {
      showCelebrationOverlay = false
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF8FAFD))
        .padding(horizontal = 20.dp, vertical = 20.dp)
        .testTag("quiz_screen"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
    // Top Bar
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
            .testTag("quiz_back_button")
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
            text = "Sign Quiz Challenge",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1E293B)
          )
          Text(
            text = "Pick the matching sign!",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B)
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFFC312),
        shadowElevation = 2.dp
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = "Stars",
            tint = Color(0xFF6B4700),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${progress.stars}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF422C00)
          )
        }
      }
    }

    if (question != null) {
      // Question Card: Big Target Letter/Number (NO long sentence prompts)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp, horizontal = 20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(question.targetItem.colorHex).copy(alpha = 0.12f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = Color(question.targetItem.colorHex),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "FIND THE MATCHING SIGN",
                color = Color(question.targetItem.colorHex),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Big prominent Target Letter / Number Display
          Box(
            modifier = Modifier
              .size(110.dp)
              .clip(RoundedCornerShape(30.dp))
              .background(
                Brush.verticalGradient(
                  listOf(
                    Color(question.targetItem.colorHex),
                    Color(question.targetItem.colorHex).copy(alpha = 0.85f)
                  )
                )
              )
              .border(3.dp, Color.White, RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = question.targetItem.title,
              style = MaterialTheme.typography.displayLarge,
              fontWeight = FontWeight.Black,
              fontSize = 68.sp,
              color = Color.White
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Which hand sign matches '${question.targetItem.title}'?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
          )
        }
      }

      // 3 Visual Option Cards with Hand Sign + Letter + Word + Emoji
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        question.options.forEach { option ->
          val isSelected = selectedOption?.id == option.id
          val isCorrectOption = option.id == question.targetItem.id

          val cardBg = when {
            selectedOption == null -> Color.White
            isSelected && isCorrectOption -> Color(0xFFE8F9EE)
            isSelected && !isCorrectOption -> Color(0xFFFFECEE)
            isCorrectOption -> Color(0xFFE8F9EE)
            else -> Color(0xFFF8FAFC)
          }

          val borderColor = when {
            selectedOption == null -> Color(0xFFE2E8F0)
            isSelected && isCorrectOption -> Color(0xFF00C853)
            isSelected && !isCorrectOption -> Color(0xFFFF3D57)
            isCorrectOption -> Color(0xFF00C853)
            else -> Color(0xFFE2E8F0)
          }

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(22.dp))
              .clickable(enabled = selectedOption == null) {
                HapticUtil.playTap(context)
                onSelectAnswer(option)
                if (option.id == question.targetItem.id) {
                  HapticUtil.playSuccess(context)
                } else {
                  HapticUtil.playPromptAlert(context)
                }
              }
              .testTag("quiz_option_${option.id}"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = androidx.compose.foundation.BorderStroke(2.5.dp, borderColor)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                // Visual Hand Sign Pose
                Box(
                  modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(option.colorHex).copy(alpha = 0.12f))
                    .border(1.5.dp, Color(option.colorHex).copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  HandPoseCanvas(
                    pose = option.handPoseType,
                    color = Color(option.colorHex),
                    modifier = Modifier.size(52.dp),
                    itemTitle = option.title
                  )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Letter + Word
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = option.title,
                      fontWeight = FontWeight.Black,
                      fontSize = 24.sp,
                      color = Color(option.colorHex)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = option.wordLabel,
                      fontWeight = FontWeight.Black,
                      fontSize = 17.sp,
                      color = Color(0xFF1E293B)
                    )
                  }
                  Text(
                    text = option.signName,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1
                  )
                }
              }

              // Associated Emoji / Icon
              Text(
                text = option.objectEmoji,
                fontSize = 32.sp,
                modifier = Modifier.padding(start = 8.dp)
              )
            }
          }
        }
      }

      // Feedback & Next Button
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        AnimatedVisibility(
          visible = isAnswerCorrect != null,
          enter = fadeIn() + scaleIn()
        ) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isAnswerCorrect == true) Color(0xFF00C853) else Color(0xFFFF5252),
            modifier = Modifier.padding(bottom = 12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Result",
                tint = Color.White
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isAnswerCorrect == true) "Superstar! Correct! +5 ⭐" else "Nice try! Keep going!",
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 14.sp
              )
            }
          }
        }

        if (selectedOption != null) {
          Button(
            onClick = {
              HapticUtil.playTap(context)
              showCelebrationOverlay = false
              onNextQuestion()
            },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C52E5)),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("quiz_next_button")
          ) {
            Text(
              text = "Next Challenge",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
              contentDescription = "Next Question"
            )
          }
        }
      }
    }
  }

    // Animated overlay and confetti effect that triggers when user correctly matches a sign
    CelebrationOverlay(
      isVisible = showCelebrationOverlay,
      title = "PERFECT MATCH! ⭐",
      subtitle = "You found '${question?.targetItem?.title}' (${question?.targetItem?.wordLabel})!",
      points = 5,
      emoji = question?.targetItem?.objectEmoji ?: "🎉",
      onDismiss = {
        showCelebrationOverlay = false
      }
    )
  }
}
