package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ConfettiShape {
  RECTANGLE,
  CIRCLE,
  STAR
}

data class ConfettiParticle(
  val initialX: Float,
  val initialY: Float,
  val velocityX: Float,
  val velocityY: Float,
  val gravity: Float,
  val size: Float,
  val color: Color,
  val shape: ConfettiShape,
  val rotationSpeed: Float,
  val flipSpeed: Float,
  val swayFrequency: Float
)

@Composable
fun ConfettiCelebration(
  isActive: Boolean,
  modifier: Modifier = Modifier,
  onFinished: () -> Unit = {}
) {
  if (!isActive) return

  val progress = remember { Animatable(0f) }
  val particles = remember {
    val colors = listOf(
      Color(0xFFFFC312), // Vivid Gold
      Color(0xFFFF5E7E), // Coral Pink
      Color(0xFF5C52E5), // Indigo Purple
      Color(0xFF00B894), // Emerald Mint
      Color(0xFFFF7849), // Warm Orange
      Color(0xFF00B4D8), // Sky Turquoise
      Color(0xFFE056FD), // Lavender Spark
      Color(0xFFF368E0)  // Rose
    )
    val shapes = ConfettiShape.values()
    List(85) {
      ConfettiParticle(
        initialX = 0.2f + (Random.nextFloat() * 0.6f),
        initialY = -0.05f - (Random.nextFloat() * 0.15f),
        velocityX = (Random.nextFloat() - 0.5f) * 0.7f,
        velocityY = 0.7f + (Random.nextFloat() * 0.85f),
        gravity = 0.4f + (Random.nextFloat() * 0.3f),
        size = 14f + (Random.nextFloat() * 18f),
        color = colors.random(),
        shape = shapes.random(),
        rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
        flipSpeed = 6f + (Random.nextFloat() * 8f),
        swayFrequency = 3f + (Random.nextFloat() * 4f)
      )
    }
  }

  LaunchedEffect(isActive) {
    progress.snapTo(0f)
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
    )
    onFinished()
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    val canvasW = size.width
    val canvasH = size.height
    val currentProgress = progress.value

    particles.forEach { p ->
      val time = currentProgress
      val sway = sin(time * p.swayFrequency * Math.PI.toFloat()) * 0.05f
      val currentX = (p.initialX + (p.velocityX * time) + sway) * canvasW
      val currentY = (p.initialY + (p.velocityY * time) + (0.5f * p.gravity * time * time)) * canvasH
      val alpha = (1f - (currentProgress * 0.85f)).coerceIn(0f, 1f)

      if (currentY in -20f..canvasH + 40f && currentX in -20f..canvasW + 20f) {
        val rotation = p.rotationSpeed * time
        val flipScaleX = cos(time * p.flipSpeed)

        rotate(degrees = rotation, pivot = Offset(currentX, currentY)) {
          scale(scaleX = flipScaleX, scaleY = 1f, pivot = Offset(currentX, currentY)) {
            when (p.shape) {
              ConfettiShape.RECTANGLE -> {
                drawRect(
                  color = p.color.copy(alpha = alpha),
                  topLeft = Offset(currentX - (p.size / 2f), currentY - (p.size * 0.35f)),
                  size = Size(p.size, p.size * 0.7f)
                )
              }
              ConfettiShape.CIRCLE -> {
                drawCircle(
                  color = p.color.copy(alpha = alpha),
                  radius = p.size * 0.4f,
                  center = Offset(currentX, currentY)
                )
              }
              ConfettiShape.STAR -> {
                val starPath = Path()
                val radius = p.size * 0.5f
                val innerRadius = radius * 0.42f
                for (i in 0 until 10) {
                  val r = if (i % 2 == 0) radius else innerRadius
                  val angle = (i * 36.0 - 90.0) * Math.PI / 180.0
                  val sx = currentX + (r * cos(angle)).toFloat()
                  val sy = currentY + (r * sin(angle)).toFloat()
                  if (i == 0) starPath.moveTo(sx, sy) else starPath.lineTo(sx, sy)
                }
                starPath.close()
                drawPath(path = starPath, color = p.color.copy(alpha = alpha))
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Animated Celebration Overlay banner with rich visuals, bouncing stars,
 * and confetti for when a sign is correctly matched in Quiz Mode.
 */
@Composable
fun CelebrationOverlay(
  isVisible: Boolean,
  title: String = "PERFECT MATCH! ⭐",
  subtitle: String = "You learned this sign!",
  points: Int = 5,
  emoji: String = "🎉",
  onDismiss: () -> Unit = {}
) {
  if (!isVisible) return

  LaunchedEffect(isVisible) {
    delay(2200)
    onDismiss()
  }

  val infiniteTransition = rememberInfiniteTransition(label = "celebration_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "badge_scale"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.40f))
      .clickable { onDismiss() }
      .testTag("celebration_overlay"),
    contentAlignment = Alignment.Center
  ) {
    // Background Confetti
    ConfettiCelebration(isActive = true)

    // Center Bouncy Victory Card
    AnimatedVisibility(
      visible = isVisible,
      enter = fadeIn(tween(200)) + scaleIn(spring(dampingRatio = 0.6f)),
      exit = fadeOut(tween(200)) + scaleOut(tween(200))
    ) {
      Card(
        modifier = Modifier
          .padding(32.dp)
          .scale(pulseScale)
          .clip(RoundedCornerShape(32.dp))
          .border(4.dp, Color(0xFFFFC312), RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
      ) {
        Column(
          modifier = Modifier
            .background(
              Brush.verticalGradient(
                listOf(
                  Color(0xFFFFFBEB),
                  Color.White
                )
              )
            )
            .padding(26.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Bouncing Emoji / Trophy Badge
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  listOf(
                    Color(0xFFFFC312),
                    Color(0xFFFF8F00)
                  )
                )
              )
              .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(text = emoji, fontSize = 42.sp)
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Headline
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = subtitle,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Stars Award Pill
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF00C853),
            shadowElevation = 4.dp
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "+$points STARS EARNED!",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color.White,
                letterSpacing = 0.5.sp
              )
            }
          }
        }
      }
    }
  }
}
