package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HandPose
import com.example.data.model.LearningItem
import com.example.util.HapticUtil

@Composable
fun VisualGestureCard(
  item: LearningItem,
  modifier: Modifier = Modifier,
  onPracticeClick: () -> Unit = {}
) {
  val context = LocalContext.current
  var isSlowMotion by remember { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition(label = "sign_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (isSlowMotion) 1800 else 1000),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )
  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.25f,
    targetValue = 0.75f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (isSlowMotion) 1800 else 1000),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_alpha"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("visual_gesture_card_${item.id}"),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header: Symbol, Subtitle, Object emoji
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(Color(item.colorHex)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = item.title,
              color = Color.White,
              fontWeight = FontWeight.Black,
              fontSize = if (item.title.length > 2) 18.sp else 26.sp
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = item.subtitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = item.category.name,
              style = MaterialTheme.typography.labelMedium,
              color = Color(item.colorHex),
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        // Fun object icon (apple, bunny, star)
        Box(
          modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(item.colorHex).copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Text(text = item.objectEmoji, fontSize = 28.sp)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Animated Sign Language Interactive Canvas Display
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(230.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(
            Brush.verticalGradient(
              listOf(
                Color(item.colorHex).copy(alpha = 0.10f),
                Color(item.colorHex).copy(alpha = 0.22f)
              )
            )
          )
          .border(
            width = 3.dp,
            color = Color(item.colorHex).copy(alpha = glowAlpha),
            shape = RoundedCornerShape(24.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        // Sign Language Hand Pose Canvas
        Canvas(
          modifier = Modifier
            .size(190.dp)
            .scale(pulseScale)
        ) {
          drawHandPose(
            pose = item.handPoseType,
            baseColor = Color(item.colorHex),
            accentColor = Color(0xFF2C3E50),
            itemTitle = item.title
          )
        }

        // Animated Sign indicator badge (Looping motion badge)
        Surface(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(12.dp),
          shape = RoundedCornerShape(12.dp),
          color = Color.White.copy(alpha = 0.90f),
          shadowElevation = 2.dp
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Sign Motion",
              tint = Color(item.colorHex),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isSlowMotion) "Slow 0.5x" else "Sign Flow",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(item.colorHex)
            )
          }
        }

        // Speed Toggle Control (Friendly for 8yo to slow down visual flow)
        Surface(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
              isSlowMotion = !isSlowMotion
              HapticUtil.playTap(context)
            },
          shape = RoundedCornerShape(12.dp),
          color = if (isSlowMotion) Color(item.colorHex) else Color.White.copy(alpha = 0.90f),
          shadowElevation = 2.dp
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = "Toggle Speed",
              tint = if (isSlowMotion) Color.White else Color(item.colorHex),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isSlowMotion) "0.5x" else "1.0x",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSlowMotion) Color.White else Color(item.colorHex)
            )
          }
        }

        // Tactile Haptic Sign Beat Button
        Surface(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(12.dp)
            .clip(CircleShape)
            .clickable {
              HapticUtil.playSuccess(context)
            },
          shape = CircleShape,
          color = Color.White.copy(alpha = 0.95f),
          shadowElevation = 3.dp
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Vibration,
              contentDescription = "Feel Sign Pulse",
              tint = Color(item.colorHex),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Feel Pulse",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(item.colorHex)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Sign Name
      Text(
        text = item.signName,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF1E293B)
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Step-by-Step Visual Guidance Cards
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item.visualCueSteps.forEachIndexed { index, step ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFFF8FAFC))
              .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(item.colorHex)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${index + 1}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = step,
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFF334155),
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      // Counting Items visualizer for numbers
      if (item.visualItemsCount > 0) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(item.colorHex).copy(alpha = 0.08f))
            .padding(10.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(item.visualItemsCount) {
            Text(
              text = item.objectEmoji,
              fontSize = 24.sp,
              modifier = Modifier.padding(horizontal = 3.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Fun fact with friendly tip
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F5F9),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "💡", fontSize = 18.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = item.funFact,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF475569),
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Practice Button: opens Camera Practice
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .clip(RoundedCornerShape(20.dp))
          .clickable {
            HapticUtil.playTap(context)
            onPracticeClick()
          }
          .testTag("practice_button_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        color = Color(item.colorHex),
        shadowElevation = 4.dp
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Rounded.TouchApp,
            contentDescription = "Practice Sign",
            tint = Color.White
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Practice in Camera Studio",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }
      }
    }
  }
}

@Composable
fun HandPoseCanvas(
  pose: HandPose,
  color: Color,
  modifier: Modifier = Modifier,
  itemTitle: String = ""
) {
  Canvas(modifier = modifier) {
    drawHandPose(
      pose = pose,
      baseColor = color,
      accentColor = Color(0xFF2C3E50),
      itemTitle = itemTitle
    )
  }
}

// Custom Draw function for drawing hand silhouettes
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHandPose(
  pose: HandPose,
  baseColor: Color,
  accentColor: Color,
  itemTitle: String
) {
  val cx = size.width / 2f
  val cy = size.height / 2f

  val skinColor = Color(0xFFFFD1A4)
  val skinOutline = Color(0xFFD49B6A)

  // Palm base
  drawRoundRect(
    color = skinColor,
    topLeft = Offset(cx - 45f, cy - 10f),
    size = Size(90f, 90f),
    cornerRadius = CornerRadius(22f, 22f)
  )
  drawRoundRect(
    color = skinOutline,
    topLeft = Offset(cx - 45f, cy - 10f),
    size = Size(90f, 90f),
    cornerRadius = CornerRadius(22f, 22f),
    style = Stroke(width = 4f)
  )

  // Wrist
  drawRoundRect(
    color = skinColor,
    topLeft = Offset(cx - 28f, cy + 65f),
    size = Size(56f, 40f),
    cornerRadius = CornerRadius(10f, 10f)
  )
  drawRoundRect(
    color = skinOutline,
    topLeft = Offset(cx - 28f, cy + 65f),
    size = Size(56f, 40f),
    cornerRadius = CornerRadius(10f, 10f),
    style = Stroke(width = 4f)
  )

  when (pose) {
    HandPose.FIST_THUMB_SIDE -> {
      // Letter A: Closed fist, thumb straight up on side
      for (i in 0..3) {
        val fingerX = cx - 36f + (i * 20f)
        drawRoundRect(
          color = skinColor,
          topLeft = Offset(fingerX, cy - 25f),
          size = Size(18f, 35f),
          cornerRadius = CornerRadius(9f, 9f)
        )
        drawRoundRect(
          color = skinOutline,
          topLeft = Offset(fingerX, cy - 25f),
          size = Size(18f, 35f),
          cornerRadius = CornerRadius(9f, 9f),
          style = Stroke(width = 3.5f)
        )
      }
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 58f, cy - 40f),
        size = Size(20f, 65f),
        cornerRadius = CornerRadius(10f, 10f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 58f, cy - 40f),
        size = Size(20f, 65f),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 4f)
      )
    }

    HandPose.FLAT_FOUR_FOLD_THUMB -> {
      // Letter B: 4 flat fingers straight up, thumb across palm
      for (i in 0..3) {
        val fingerX = cx - 36f + (i * 19f)
        drawRoundRect(
          color = Color(0xFFFFC085),
          topLeft = Offset(fingerX, cy - 80f),
          size = Size(18f, 85f),
          cornerRadius = CornerRadius(9f, 9f)
        )
        drawRoundRect(
          color = baseColor,
          topLeft = Offset(fingerX, cy - 80f),
          size = Size(18f, 85f),
          cornerRadius = CornerRadius(9f, 9f),
          style = Stroke(width = 3.5f)
        )
      }
      // Thumb folded across palm
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 30f, cy + 15f),
        size = Size(60f, 18f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 30f, cy + 15f),
        size = Size(60f, 18f),
        cornerRadius = CornerRadius(9f, 9f),
        style = Stroke(width = 3.5f)
      )
    }

    HandPose.CURVED_C -> {
      // Letter C: Curve hand into C
      val path = Path().apply {
        moveTo(cx + 35f, cy - 60f)
        cubicTo(cx - 40f, cy - 60f, cx - 40f, cy + 40f, cx + 35f, cy + 40f)
      }
      drawPath(
        path = path,
        color = baseColor,
        style = Stroke(width = 24f, cap = StrokeCap.Round)
      )
    }

    HandPose.POINT_INDEX_O -> {
      // Letter D: Index straight up, others form circle with thumb
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 20f, cy - 80f),
        size = Size(18f, 85f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 20f, cy - 80f),
        size = Size(18f, 85f),
        cornerRadius = CornerRadius(9f, 9f),
        style = Stroke(width = 4f)
      )
      drawCircle(
        color = skinColor,
        radius = 26f,
        center = Offset(cx + 10f, cy + 10f)
      )
      drawCircle(
        color = baseColor,
        radius = 26f,
        center = Offset(cx + 10f, cy + 10f),
        style = Stroke(width = 4f)
      )
    }

    HandPose.CURL_ALL_FINGERS -> {
      // Letter E: Curled fingers resting on thumb
      for (i in 0..3) {
        val fingerX = cx - 36f + (i * 20f)
        drawRoundRect(
          color = skinColor,
          topLeft = Offset(fingerX, cy - 35f),
          size = Size(18f, 30f),
          cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
          color = skinOutline,
          topLeft = Offset(fingerX, cy - 35f),
          size = Size(18f, 30f),
          cornerRadius = CornerRadius(8f, 8f),
          style = Stroke(width = 3.5f)
        )
      }
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 40f, cy + 5f),
        size = Size(80f, 18f),
        cornerRadius = CornerRadius(9f, 9f)
      )
    }

    HandPose.THREE_FINGERS_UP_OK -> {
      // Letter F: Index and thumb in circle, other 3 fingers up
      for (i in 0..2) {
        val fingerX = cx - 5f + (i * 20f)
        drawRoundRect(
          color = Color(0xFFFFC085),
          topLeft = Offset(fingerX, cy - 75f),
          size = Size(16f, 80f),
          cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
          color = baseColor,
          topLeft = Offset(fingerX, cy - 75f),
          size = Size(16f, 80f),
          cornerRadius = CornerRadius(8f, 8f),
          style = Stroke(width = 3.5f)
        )
      }
      drawCircle(
        color = baseColor,
        radius = 22f,
        center = Offset(cx - 30f, cy - 10f),
        style = Stroke(width = 5f)
      )
    }

    HandPose.POINT_INDEX_THUMB_SIDE -> {
      // Letter G: Index and thumb pointing horizontally (pincher)
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 20f, cy - 35f),
        size = Size(75f, 18f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 20f, cy - 35f),
        size = Size(75f, 18f),
        cornerRadius = CornerRadius(9f, 9f),
        style = Stroke(width = 3.5f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 20f, cy - 10f),
        size = Size(65f, 18f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 20f, cy - 10f),
        size = Size(65f, 18f),
        cornerRadius = CornerRadius(9f, 9f),
        style = Stroke(width = 3.5f)
      )
    }

    HandPose.TWO_FINGERS_SIDEWAYS -> {
      // Letter H: Index & Middle pointing sideways
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 25f, cy - 40f),
        size = Size(80f, 18f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 25f, cy - 18f),
        size = Size(80f, 18f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 25f, cy - 40f),
        size = Size(80f, 40f),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 4f)
      )
    }

    HandPose.FIST_PINKY_UP -> {
      // Letter I: Pinky raised tall
      for (i in 0..2) {
        val fingerX = cx - 36f + (i * 20f)
        drawRoundRect(
          color = skinColor,
          topLeft = Offset(fingerX, cy - 15f),
          size = Size(18f, 30f),
          cornerRadius = CornerRadius(9f, 9f)
        )
      }
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 28f, cy - 70f),
        size = Size(16f, 75f),
        cornerRadius = CornerRadius(8f, 8f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx + 28f, cy - 70f),
        size = Size(16f, 75f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 4f)
      )
    }

    HandPose.PINKY_SWOOP_J -> {
      // Letter J: Pinky swoops in a J curve
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 20f, cy - 65f),
        size = Size(16f, 65f),
        cornerRadius = CornerRadius(8f, 8f)
      )
      val jPath = Path().apply {
        moveTo(cx + 28f, cy)
        cubicTo(cx + 28f, cy + 45f, cx - 20f, cy + 45f, cx - 20f, cy + 20f)
      }
      drawPath(jPath, color = baseColor, style = Stroke(width = 6f, cap = StrokeCap.Round))
    }

    HandPose.PEACE_THUMB_MIDDLE_K -> {
      // Letter K: V with thumb pointing up between fingers
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 25f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 10f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 8f, cy - 45f),
        size = Size(16f, 50f),
        cornerRadius = CornerRadius(8f, 8f)
      )
    }

    HandPose.L_SHAPE -> {
      // Letter L: Index up, thumb horizontal
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 10f, cy - 80f),
        size = Size(20f, 85f),
        cornerRadius = CornerRadius(10f, 10f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 10f, cy - 80f),
        size = Size(20f, 85f),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 4f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 55f, cy + 5f),
        size = Size(55f, 20f),
        cornerRadius = CornerRadius(10f, 10f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 55f, cy + 5f),
        size = Size(55f, 20f),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 4f)
      )
    }

    HandPose.THREE_FINGERS_OVER_THUMB -> {
      // Letter M: 3 fingers folded over thumb
      for (i in 0..2) {
        val fingerX = cx - 30f + (i * 20f)
        drawRoundRect(
          color = Color(0xFFFFC085),
          topLeft = Offset(fingerX, cy - 30f),
          size = Size(18f, 45f),
          cornerRadius = CornerRadius(9f, 9f)
        )
      }
      drawCircle(color = baseColor, radius = 10f, center = Offset(cx + 25f, cy + 20f))
    }

    HandPose.TWO_FINGERS_OVER_THUMB -> {
      // Letter N: 2 fingers folded over thumb
      for (i in 0..1) {
        val fingerX = cx - 20f + (i * 20f)
        drawRoundRect(
          color = Color(0xFFFFC085),
          topLeft = Offset(fingerX, cy - 30f),
          size = Size(18f, 45f),
          cornerRadius = CornerRadius(9f, 9f)
        )
      }
      drawCircle(color = baseColor, radius = 10f, center = Offset(cx + 15f, cy + 20f))
    }

    HandPose.CIRCLE_O -> {
      // Letter O: Full circular curve
      drawCircle(color = skinColor, radius = 48f, center = Offset(cx, cy - 10f))
      drawCircle(color = baseColor, radius = 48f, center = Offset(cx, cy - 10f), style = Stroke(width = 6f))
      drawCircle(color = Color.White, radius = 24f, center = Offset(cx, cy - 10f))
      drawCircle(color = skinOutline, radius = 24f, center = Offset(cx, cy - 10f), style = Stroke(width = 3f))
    }

    HandPose.P_DOWNWARD_K -> {
      // Letter P: Downward pointing K
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 20f, cy + 5f),
        size = Size(18f, 75f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 20f, cy + 5f),
        size = Size(18f, 75f),
        cornerRadius = CornerRadius(9f, 9f),
        style = Stroke(width = 3.5f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 5f, cy + 10f),
        size = Size(50f, 18f),
        cornerRadius = CornerRadius(9f, 9f)
      )
    }

    HandPose.Q_DOWNWARD_G -> {
      // Letter Q: Downward pincher
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 15f, cy + 5f),
        size = Size(18f, 75f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 8f, cy + 5f),
        size = Size(18f, 55f),
        cornerRadius = CornerRadius(9f, 9f)
      )
    }

    HandPose.CROSSED_INDEX_MIDDLE_R -> {
      // Letter R: Crossed index and middle
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 18f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 5f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      // Visual cross accent
      drawLine(
        color = baseColor,
        start = Offset(cx - 22f, cy - 35f),
        end = Offset(cx + 15f, cy - 55f),
        strokeWidth = 6f,
        cap = StrokeCap.Round
      )
    }

    HandPose.FIST_THUMB_OVER_S -> {
      // Letter S: Closed fist with thumb wrapped across front
      for (i in 0..3) {
        val fingerX = cx - 36f + (i * 20f)
        drawRoundRect(
          color = skinColor,
          topLeft = Offset(fingerX, cy - 25f),
          size = Size(18f, 35f),
          cornerRadius = CornerRadius(9f, 9f)
        )
      }
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 35f, cy - 10f),
        size = Size(70f, 20f),
        cornerRadius = CornerRadius(10f, 10f)
      )
    }

    HandPose.THUMB_BETWEEN_INDEX_T -> {
      // Letter T: Thumb peek between index and middle
      for (i in 0..3) {
        val fingerX = cx - 36f + (i * 20f)
        drawRoundRect(
          color = skinColor,
          topLeft = Offset(fingerX, cy - 25f),
          size = Size(18f, 35f),
          cornerRadius = CornerRadius(9f, 9f)
        )
      }
      drawCircle(color = baseColor, radius = 14f, center = Offset(cx - 15f, cy - 20f))
    }

    HandPose.FIST_THUMB_INDEX -> {
      // Letter U: Index and middle fingers together
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 20f, cy - 75f),
        size = Size(40f, 80f),
        cornerRadius = CornerRadius(10f, 10f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 20f, cy - 75f),
        size = Size(40f, 80f),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 4f)
      )
    }

    HandPose.V_PEACE_SPREAD -> {
      // Letter V: Peace sign spread apart
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 30f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 12f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 30f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f),
        style = Stroke(width = 4f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx + 12f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f),
        style = Stroke(width = 4f)
      )
    }

    HandPose.W_THREE_FINGERS -> {
      // Letter W: 3 middle fingers spread
      for (i in 0..2) {
        val fingerX = cx - 30f + (i * 26f)
        drawRoundRect(
          color = Color(0xFFFFC085),
          topLeft = Offset(fingerX, cy - 75f),
          size = Size(16f, 80f),
          cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
          color = baseColor,
          topLeft = Offset(fingerX, cy - 75f),
          size = Size(16f, 80f),
          cornerRadius = CornerRadius(8f, 8f),
          style = Stroke(width = 3.5f)
        )
      }
    }

    HandPose.HOOKED_INDEX_X -> {
      // Letter X: Hooked index finger
      val hook = Path().apply {
        moveTo(cx - 10f, cy + 10f)
        lineTo(cx - 10f, cy - 45f)
        cubicTo(cx - 10f, cy - 70f, cx + 20f, cy - 70f, cx + 20f, cy - 40f)
      }
      drawPath(hook, color = baseColor, style = Stroke(width = 16f, cap = StrokeCap.Round))
    }

    HandPose.THUMB_PINKY_Y -> {
      // Letter Y: Thumb and pinky extended out
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 65f, cy - 35f),
        size = Size(24f, 60f),
        cornerRadius = CornerRadius(12f, 12f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 65f, cy - 35f),
        size = Size(24f, 60f),
        cornerRadius = CornerRadius(12f, 12f),
        style = Stroke(width = 3.5f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 40f, cy - 50f),
        size = Size(20f, 70f),
        cornerRadius = CornerRadius(10f, 10f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx + 40f, cy - 50f),
        size = Size(20f, 70f),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 3.5f)
      )
    }

    HandPose.INDEX_Z_DRAW -> {
      // Letter Z: Index finger tracing Z
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 10f, cy - 70f),
        size = Size(20f, 75f),
        cornerRadius = CornerRadius(10f, 10f)
      )
      // Z trace arrow
      val zPath = Path().apply {
        moveTo(cx - 45f, cy - 65f)
        lineTo(cx + 35f, cy - 65f)
        lineTo(cx - 35f, cy - 15f)
        lineTo(cx + 45f, cy - 15f)
      }
      drawPath(zPath, color = baseColor, style = Stroke(width = 6f, cap = StrokeCap.Round))
    }

    HandPose.ONE_INDEX -> {
      // Number 1: Index finger up
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 10f, cy - 80f),
        size = Size(20f, 85f),
        cornerRadius = CornerRadius(10f, 10f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 10f, cy - 80f),
        size = Size(20f, 85f),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 4f)
      )
    }

    HandPose.TWO_PEACE -> {
      // Number 2: Index & middle in V
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 28f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 10f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 28f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f),
        style = Stroke(width = 4f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx + 10f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f),
        style = Stroke(width = 4f)
      )
    }

    HandPose.THREE_THUMB_TWO -> {
      // Number 3: Thumb, Index, Middle
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 55f, cy - 30f),
        size = Size(22f, 60f),
        cornerRadius = CornerRadius(11f, 11f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 15f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 10f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 55f, cy - 30f),
        size = Size(22f, 60f),
        cornerRadius = CornerRadius(11f, 11f),
        style = Stroke(width = 3.5f)
      )
    }

    HandPose.FOUR_FINGERS -> {
      // Number 4: 4 fingers up
      for (i in 0..3) {
        val fingerX = cx - 36f + (i * 20f)
        drawRoundRect(
          color = Color(0xFFFFC085),
          topLeft = Offset(fingerX, cy - 75f),
          size = Size(16f, 80f),
          cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
          color = baseColor,
          topLeft = Offset(fingerX, cy - 75f),
          size = Size(16f, 80f),
          cornerRadius = CornerRadius(8f, 8f),
          style = Stroke(width = 3.5f)
        )
      }
    }

    HandPose.FIVE_OPEN_PALM -> {
      // Number 5 / Open Palm
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 56f, cy - 30f),
        size = Size(20f, 60f),
        cornerRadius = CornerRadius(10f, 10f)
      )
      for (i in 0..3) {
        val fingerX = cx - 34f + (i * 20f)
        drawRoundRect(
          color = Color(0xFFFFC085),
          topLeft = Offset(fingerX, cy - 80f),
          size = Size(16f, 85f),
          cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
          color = baseColor,
          topLeft = Offset(fingerX, cy - 80f),
          size = Size(16f, 85f),
          cornerRadius = CornerRadius(8f, 8f),
          style = Stroke(width = 3.5f)
        )
      }
    }

    HandPose.THUMB_TOUCH_PINKY -> {
      // Number 6: Thumb touches pinky
      for (i in 0..2) {
        val fingerX = cx - 30f + (i * 22f)
        drawRoundRect(
          color = Color(0xFFFFC085),
          topLeft = Offset(fingerX, cy - 75f),
          size = Size(18f, 80f),
          cornerRadius = CornerRadius(9f, 9f)
        )
      }
      drawCircle(color = baseColor, radius = 18f, center = Offset(cx + 25f, cy + 10f))
    }

    HandPose.THUMB_TOUCH_RING -> {
      // Number 7: Thumb touches ring finger
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 30f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 5f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 30f, cy - 65f),
        size = Size(16f, 70f),
        cornerRadius = CornerRadius(8f, 8f)
      )
      drawCircle(color = baseColor, radius = 16f, center = Offset(cx + 12f, cy - 10f))
    }

    HandPose.THUMB_TOUCH_MIDDLE -> {
      // Number 8: Thumb touches middle finger
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 30f, cy - 75f),
        size = Size(18f, 80f),
        cornerRadius = CornerRadius(9f, 9f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 15f, cy - 70f),
        size = Size(16f, 75f),
        cornerRadius = CornerRadius(8f, 8f)
      )
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 35f, cy - 65f),
        size = Size(16f, 70f),
        cornerRadius = CornerRadius(8f, 8f)
      )
      drawCircle(color = baseColor, radius = 16f, center = Offset(cx - 5f, cy - 10f))
    }

    HandPose.THUMB_TOUCH_INDEX -> {
      // Number 9: Thumb touches index finger (OK)
      for (i in 0..2) {
        val fingerX = cx - 5f + (i * 20f)
        drawRoundRect(
          color = Color(0xFFFFC085),
          topLeft = Offset(fingerX, cy - 75f),
          size = Size(16f, 80f),
          cornerRadius = CornerRadius(8f, 8f)
        )
      }
      drawCircle(color = baseColor, radius = 20f, center = Offset(cx - 30f, cy - 15f), style = Stroke(width = 5f))
    }

    HandPose.COMPOUND_TENS -> {
      // Numbers 11 to 50: Compound two-digit visual representation
      val tensChar = itemTitle.firstOrNull() ?: '2'
      val unitsChar = itemTitle.getOrNull(1) ?: '0'

      // Left hand miniature (Tens)
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx - 70f, cy - 40f),
        size = Size(45f, 65f),
        cornerRadius = CornerRadius(14f, 14f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - 70f, cy - 40f),
        size = Size(45f, 65f),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(width = 3.5f)
      )

      // Right hand miniature (Units)
      drawRoundRect(
        color = Color(0xFFFFC085),
        topLeft = Offset(cx + 25f, cy - 40f),
        size = Size(45f, 65f),
        cornerRadius = CornerRadius(14f, 14f)
      )
      drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx + 25f, cy - 40f),
        size = Size(45f, 65f),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(width = 3.5f)
      )

      // Transition Arrow between hands
      val path = Path().apply {
        moveTo(cx - 15f, cy - 10f)
        lineTo(cx + 15f, cy - 10f)
      }
      drawPath(path, color = baseColor, style = Stroke(width = 5f, cap = StrokeCap.Round))
    }

    else -> {
      // Vocabulary & Greetings
      for (i in 0..3) {
        val fingerX = cx - 34f + (i * 20f)
        drawRoundRect(
          color = Color(0xFFFFC085),
          topLeft = Offset(fingerX, cy - 65f),
          size = Size(16f, 70f),
          cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
          color = baseColor,
          topLeft = Offset(fingerX, cy - 65f),
          size = Size(16f, 70f),
          cornerRadius = CornerRadius(8f, 8f),
          style = Stroke(width = 3.5f)
        )
      }
      val path = Path().apply {
        moveTo(cx - 60f, cy - 40f)
        quadraticTo(cx, cy - 90f, cx + 60f, cy - 40f)
      }
      drawPath(
        path = path,
        color = baseColor,
        style = Stroke(width = 4f, cap = StrokeCap.Round)
      )
    }
  }
}
