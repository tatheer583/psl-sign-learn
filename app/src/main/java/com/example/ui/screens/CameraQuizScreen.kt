package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Videocam
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.LearningDataSource
import com.example.data.db.ProgressEntity
import com.example.data.model.LearningItem
import com.example.ui.components.CelebrationOverlay
import com.example.ui.components.ConfettiCelebration
import com.example.ui.components.HandPoseCanvas
import com.example.util.HandGestureAnalyzer
import com.example.util.HapticUtil
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@Composable
fun CameraQuizScreen(
  progress: ProgressEntity,
  onAwardPoints: (Int, String) -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  BackHandler { onBack() }

  // Mode: "ALPHABET" (A to Z) or "NUMBERS" (1 to 50)
  var quizMode by remember { mutableStateOf("ALPHABET") }
  var currentIndex by remember { mutableIntStateOf(0) }
  var quizScore by remember { mutableIntStateOf(0) }
  var streakCount by remember { mutableIntStateOf(0) }

  // Camera Settings
  var cameraFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
  var isTorchOn by remember { mutableStateOf(false) }
  var activeCamera by remember { mutableStateOf<Camera?>(null) }

  // Matching State
  var matchProgress by remember { mutableFloatStateOf(0f) }
  var isMatched by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf("Raise your hand to match the sign! ✋") }
  var showCelebration by remember { mutableStateOf(false) }

  val currentItems = remember(quizMode) {
    if (quizMode == "ALPHABET") LearningDataSource.alphabetItems
    else LearningDataSource.numberItems
  }

  val currentTarget: LearningItem = currentItems.getOrElse(currentIndex % currentItems.size) {
    currentItems.first()
  }

  // Camera Permission
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
    if (isGranted) {
      HapticUtil.playSuccess(context)
    }
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  // Advance to next after match celebration
  LaunchedEffect(isMatched) {
    if (isMatched) {
      showCelebration = true
      HapticUtil.playCelebration(context)
      quizScore += 10
      streakCount += 1
      onAwardPoints(10, currentTarget.id)

      delay(1500)
      // Advance to next letter or number
      currentIndex = (currentIndex + 1) % currentItems.size
      isMatched = false
      matchProgress = 0f
      showCelebration = false
      statusMessage = "Raise hand for '${currentItems[currentIndex].title}'! ✋"
    }
  }

  // Reset match progress when target changes
  LaunchedEffect(currentTarget.id) {
    matchProgress = 0f
    isMatched = false
  }

  val infiniteTransition = rememberInfiniteTransition(label = "camera_quiz_glow")
  val pulseGlow by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1100),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F172A))
      .testTag("camera_quiz_screen")
  ) {
    // Top Bar with Back, Score, and Mode Tabs
    Surface(
      color = Color(0xFF1E293B),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
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
              modifier = Modifier.testTag("camera_quiz_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
              Text(
                text = "Camera Hand Quiz 📸",
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = Color.White
              )
              Text(
                text = "Match sign with hand in camera!",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }

          // Score & Streak Pill
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFFFC312)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = Color(0xFF6B4700),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "+$quizScore",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color(0xFF422C00)
              )
              if (streakCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "🔥$streakCount",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Mode switch tabs: A to Z vs 1 to 50
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (quizMode == "ALPHABET") Color(0xFF5C52E5) else Color(0xFF334155),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                HapticUtil.playTap(context)
                quizMode = "ALPHABET"
                currentIndex = 0
              }
          ) {
            Text(
              text = "🔤 Letters (A to Z)",
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(vertical = 8.dp),
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = Color.White
            )
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (quizMode == "NUMBERS") Color(0xFF00B894) else Color(0xFF334155),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                HapticUtil.playTap(context)
                quizMode = "NUMBERS"
                currentIndex = 0
              }
          ) {
            Text(
              text = "🔢 Numbers (1 to 50)",
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(vertical = 8.dp),
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = Color.White
            )
          }
        }
      }
    }

    // Split Screen Content: Side 1 (Target Sign Card) & Side 2 (Live Camera)
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Top Half: Target Letter/Number Card with Hand Sign diagram
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Left: Big Target Letter & Word
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(currentTarget.colorHex).copy(alpha = 0.15f)
            ) {
              Text(
                text = "TARGET SIGN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(currentTarget.colorHex),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Big Letter/Number
            Box(
              modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(currentTarget.colorHex)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = currentTarget.title,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Word with Emoji (e.g. "Apple 🍎" or "Orange 🍊")
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = currentTarget.wordLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = currentTarget.objectEmoji,
                fontSize = 22.sp
              )
            }
          }

          // Right: Hand Pose Reference Silhouette
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(108.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(currentTarget.colorHex).copy(alpha = 0.10f))
                .border(2.dp, Color(currentTarget.colorHex).copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
              contentAlignment = Alignment.Center
            ) {
              HandPoseCanvas(
                pose = currentTarget.handPoseType,
                color = Color(currentTarget.colorHex),
                modifier = Modifier.size(92.dp),
                itemTitle = currentTarget.title
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = currentTarget.signName,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF64748B),
              textAlign = TextAlign.Center,
              maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Skip arrow button
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFFF1F5F9),
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  HapticUtil.playTap(context)
                  currentIndex = (currentIndex + 1) % currentItems.size
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Next Sign", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                  contentDescription = "Next",
                  modifier = Modifier.size(14.dp),
                  tint = Color(0xFF475569)
                )
              }
            }
          }
        }
      }

      // Bottom Half: Live Camera Interface
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1.3f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          if (hasCameraPermission) {
            AndroidView(
              factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                  scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                val cameraExecutor = Executors.newSingleThreadExecutor()

                cameraProviderFuture.addListener({
                  val cameraProvider = cameraProviderFuture.get()
                  val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                  }

                  val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                      it.setAnalyzer(
                        cameraExecutor,
                        HandGestureAnalyzer { result ->
                          val matched = result.confidenceScore >= 0.88f && result.stabilityProgress >= 0.85f
                          matchProgress = result.stabilityProgress
                          statusMessage = result.statusMessage
                          if (matched && !isMatched) {
                            isMatched = true
                          }
                        }
                      )
                    }

                  val selector = CameraSelector.Builder()
                    .requireLensFacing(cameraFacing)
                    .build()

                  try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                      lifecycleOwner,
                      selector,
                      preview,
                      imageAnalyzer
                    )
                    activeCamera = camera
                  } catch (_: Exception) {
                  }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
              },
              update = {
                try {
                  activeCamera?.cameraControl?.enableTorch(isTorchOn)
                } catch (_: Exception) {
                }
              },
              modifier = Modifier.fillMaxSize()
            )

            // Overlaid Hand Target Box with Pulse Glow
            Box(
              modifier = Modifier
                .align(Alignment.Center)
                .size(160.dp)
                .scale(if (matchProgress > 0.4f) pulseGlow else 1f)
                .border(
                  width = if (isMatched) 4.dp else 2.5.dp,
                  color = if (isMatched) Color(0xFF00C853) else if (matchProgress > 0.5f) Color(0xFFFFC312) else Color.White.copy(alpha = 0.8f),
                  shape = RoundedCornerShape(24.dp)
                ),
              contentAlignment = Alignment.Center
            ) {
              if (!isMatched && matchProgress < 0.2f) {
                Text(
                  text = "Raise Hand Up ✋",
                  color = Color.White.copy(alpha = 0.90f),
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  textAlign = TextAlign.Center
                )
              }
            }

            // Top Floating Camera Controls (Flip Camera, Torch)
            Row(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Torch Button
              Surface(
                shape = CircleShape,
                color = if (isTorchOn) Color(0xFFFFC312) else Color.Black.copy(alpha = 0.5f),
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .clickable {
                    isTorchOn = !isTorchOn
                    HapticUtil.playTap(context)
                  }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Rounded.FlashOn,
                    contentDescription = "Torch",
                    tint = if (isTorchOn) Color.Black else Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }

              // Flip Camera Button (Front / Back)
              Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .clickable {
                    cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_FRONT) {
                      CameraSelector.LENS_FACING_BACK
                    } else {
                      CameraSelector.LENS_FACING_FRONT
                    }
                    HapticUtil.playTap(context)
                  }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Rounded.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }

            // Bottom Progress & Status Bar on Camera
            Column(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                  Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                  )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isMatched) "✨ MATCHED! +10 ⭐" else statusMessage,
                  color = if (isMatched) Color(0xFF00E676) else Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
                Text(
                  text = "${(matchProgress * 100).toInt()}%",
                  color = if (isMatched) Color(0xFF00E676) else Color(0xFFFFC312),
                  fontWeight = FontWeight.Black,
                  fontSize = 12.sp
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              LinearProgressIndicator(
                progress = { matchProgress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = if (isMatched) Color(0xFF00E676) else Color(0xFFFFC312),
                trackColor = Color.White.copy(alpha = 0.25f)
              )
            }

            // Correct Match Success Overlay
            if (isMatched) {
              Box(
                modifier = Modifier.align(Alignment.Center)
              ) {
                Surface(
                  shape = RoundedCornerShape(24.dp),
                  color = Color(0xFF00C853),
                  shadowElevation = 8.dp
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.CheckCircle,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = "AWESOME! +10 ⭐",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                      )
                      Text(
                        text = "'${currentTarget.title}' Sign Matched!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                      )
                    }
                  }
                }
              }
            }
          } else {
            // Permission needed
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Rounded.Videocam,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Camera Permission Required",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = {
                  permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C52E5))
              ) {
                Text("Grant Camera Access")
              }
            }
          }
        }
      }
    }
  }

  // Animated Celebration Overlay & Confetti
  CelebrationOverlay(
    isVisible = showCelebration,
    title = "PERFECT SIGN MATCH! ⭐",
    subtitle = "You matched '${currentTarget.title}' (${currentTarget.wordLabel})!",
    points = 10,
    emoji = currentTarget.objectEmoji,
    onDismiss = { showCelebration = false }
  )
}
