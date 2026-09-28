package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.util.HapticUtil

data class OnboardingStep(
  val title: String,
  val subtitle: String,
  val highlightBadge: String,
  val emoji: String,
  val color: Color
)

@Composable
fun OnboardingScreen(
  onComplete: () -> Unit
) {
  val context = LocalContext.current
  var currentStepIndex by remember { mutableIntStateOf(0) }

  val steps = listOf(
    OnboardingStep(
      title = "Welcome, Irssa!",
      subtitle = "Your friendly magical world to learn sign language, vowels, numbers, and school words!",
      highlightBadge = "Meet Sparky & Irssa",
      emoji = "👋",
      color = Color(0xFF5C52E5)
    ),
    OnboardingStep(
      title = "Visual Sign Magic",
      subtitle = "Watch colorful animated hands, follow easy finger steps, and feel gentle tactile pulses!",
      highlightBadge = "Vowels & Numbers",
      emoji = "✨",
      color = Color(0xFF00B894)
    ),
    OnboardingStep(
      title = "Camera Sign Studio",
      subtitle = "Show your hand to the camera! Practice real-time signs with both front and back cameras.",
      highlightBadge = "Earn Shiny Stars 🌟",
      emoji = "📸",
      color = Color(0xFFFF7849)
    )
  )

  val currentStep = steps[currentStepIndex]

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            Color(0xFFF0F4FF),
            Color(0xFFFFFFFF),
            Color(0xFFFFF6EE)
          )
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top App Branding
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "🌟", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "IRSSA - LEARN & PLAY",
              fontWeight = FontWeight.Black,
              letterSpacing = 1.2.sp,
              color = MaterialTheme.colorScheme.primary,
              fontSize = 13.sp
            )
          }
        }
      }

      // Hero Illustration and Mascot Box
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          Image(
            painter = painterResource(id = R.drawable.img_hero_banner),
            contentDescription = "Irssa Welcome Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )

          // Mascot floating bubble
          Surface(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 6.dp
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
              ) {
                Image(
                  painter = painterResource(id = R.drawable.img_mascot_avatar),
                  contentDescription = "Mascot Sparky",
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Hi Irssa!",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
              )
            }
          }
        }
      }

      // Step text information with lively animation
      AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically()
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = currentStep.color.copy(alpha = 0.15f)
          ) {
            Text(
              text = "${currentStep.emoji}  ${currentStep.highlightBadge}",
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              color = currentStep.color,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = currentStep.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = Color(0xFF1E293B)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = currentStep.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color(0xFF64748B),
            lineHeight = 22.sp
          )
        }
      }

      // Bottom Row: Step Dots & Action Button
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Dot Indicators
        Row(
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.padding(bottom = 20.dp)
        ) {
          steps.forEachIndexed { idx, _ ->
            val isSelected = idx == currentStepIndex
            Box(
              modifier = Modifier
                .padding(horizontal = 4.dp)
                .height(10.dp)
                .width(if (isSelected) 28.dp else 10.dp)
                .clip(CircleShape)
                .background(
                  if (isSelected) currentStep.color else Color(0xFFCBD5E1)
                )
            )
          }
        }

        // Action Button
        Button(
          onClick = {
            HapticUtil.playTap(context)
            if (currentStepIndex < steps.size - 1) {
              currentStepIndex++
            } else {
              HapticUtil.playCelebration(context)
              onComplete()
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("onboarding_next_button"),
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = currentStep.color
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = if (currentStepIndex == steps.size - 1) "Let's Play Irssa!" else "Continue",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = if (currentStepIndex == steps.size - 1) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.ArrowForward,
              contentDescription = "Next",
              tint = Color.White
            )
          }
        }
      }
    }
  }
}
