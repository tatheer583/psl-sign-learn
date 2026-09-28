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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.model.LearningItem
import com.example.ui.components.ConfettiCelebration
import com.example.util.HandGestureAnalyzer
import com.example.util.HapticUtil
import java.util.concurrent.Executors

@Composable
fun CameraPracticeScreen(
  targetItem: LearningItem,
  cameraFacing: Int,
  isTorchOn: Boolean,
  matchProgress: Float,
  statusMessage: String,
  isMatched: Boolean,
  onToggleCamera: () -> Unit,
  onToggleTorch: () -> Unit,
  onAnalysisUpdate: (Float, String, Boolean) -> Unit,
  onResetMatch: () -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  BackHandler { onBack() }

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

  // Animated glow for the hand guide overlay
  val infiniteTransition = rememberInfiniteTransition(label = "guide_glow")
  val guidePulse by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "guide_scale"
  )

  var activeCamera by remember { mutableStateOf<Camera?>(null) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag("camera_practice_screen")
  ) {
    if (hasCameraPermission) {
      // Camera Preview
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
                    val matched = result.confidenceScore >= 0.90f && result.stabilityProgress >= 0.85f
                    onAnalysisUpdate(
                      result.stabilityProgress,
                      result.statusMessage,
                      matched
                    )
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
              // Fail gracefully if selected lens is unavailable
            }
          }, ContextCompat.getMainExecutor(ctx))

          previewView
        },
        update = {
          // React to torch changes
          try {
            activeCamera?.cameraControl?.enableTorch(isTorchOn)
          } catch (_: Exception) {
          }
        },
        modifier = Modifier.fillMaxSize()
      )
    } else {
      // Permission Rationale Card
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xFF1E1B4B))
          .padding(28.dp),
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = RoundedCornerShape(28.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF5C52E5).copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Rounded.Videocam,
                contentDescription = "Camera",
                tint = Color(0xFF5C52E5),
                modifier = Modifier.size(38.dp)
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Camera Sign Studio",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black,
              color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Irssa can see herself in real-time, mirror the hand gestures, and earn shiny stars!",
              style = MaterialTheme.typography.bodyMedium,
              textAlign = TextAlign.Center,
              color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
              onClick = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
              },
              shape = RoundedCornerShape(18.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C52E5)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Enable Camera",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
            }
          }
        }
      }
    }

    // Top Header & Controls Overlay
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 40.dp, start = 20.dp, end = 20.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Back Button
      IconButton(
        onClick = {
          HapticUtil.playTap(context)
          onBack()
        },
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.55f))
          .testTag("camera_back_btn")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
          contentDescription = "Back",
          tint = Color.White
        )
      }

      // Title & Sign name
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.60f)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Target: ${targetItem.title} ${targetItem.objectEmoji}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
      }

      // Flip Camera & Torch Buttons
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(
          onClick = {
            HapticUtil.playTap(context)
            onToggleCamera()
          },
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
            .testTag("camera_flip_btn")
        ) {
          Icon(
            imageVector = Icons.Rounded.Cameraswitch,
            contentDescription = "Switch Camera",
            tint = Color.White
          )
        }

        if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
          IconButton(
            onClick = {
              HapticUtil.playTap(context)
              onToggleTorch()
            },
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(if (isTorchOn) Color(0xFFFFC312) else Color.Black.copy(alpha = 0.55f))
          ) {
            Icon(
              imageVector = Icons.Rounded.FlashOn,
              contentDescription = "Torch",
              tint = if (isTorchOn) Color.Black else Color.White
            )
          }
        }
      }
    }

    // Mirror Practice Reference Card (Top overlay)
    Card(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 95.dp, start = 20.dp, end = 20.dp)
        .fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(targetItem.colorHex)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = targetItem.title,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = targetItem.signName,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF1E293B)
          )
          Text(
            text = targetItem.visualCueSteps.firstOrNull() ?: "Face your palm to the camera",
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            maxLines = 2
          )
        }
      }
    }

    // Center Hand Silhouette Bounding Guide Overlay
    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .size(240.dp)
        .scale(guidePulse)
        .border(
          width = if (isMatched) 5.dp else 3.5.dp,
          color = if (isMatched) Color(0xFF00E676) else Color(0xFFFFC312),
          shape = RoundedCornerShape(42.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = if (isMatched) "🌟" else "🖐️",
          fontSize = 54.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = if (isMatched) "PERFECT!" else "Place Hand Here",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = 14.sp
        )
      }
    }

    // Bottom Status & Verification Bar
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(
              Color.Transparent,
              Color.Black.copy(alpha = 0.85f),
              Color.Black
            )
          )
        )
        .padding(horizontal = 24.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Status Message
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isMatched) Color(0xFF00E676) else Color.White.copy(alpha = 0.20f)
      ) {
        Text(
          text = statusMessage,
          color = if (isMatched) Color(0xFF003814) else Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Match Progress Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        LinearProgressIndicator(
          progress = { matchProgress },
          modifier = Modifier
            .weight(1f)
            .height(10.dp)
            .clip(CircleShape),
          color = if (isMatched) Color(0xFF00E676) else Color(0xFFFFC312),
          trackColor = Color.White.copy(alpha = 0.25f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "${(matchProgress * 100).toInt()}%",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Action Buttons: Manual "I Signed It!" & "Try Again"
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Button(
          onClick = {
            HapticUtil.playSuccess(context)
            onAnalysisUpdate(1f, "Sign Verified! Superstar! 🌟", true)
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894)),
          shape = RoundedCornerShape(18.dp),
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("verify_sign_btn")
        ) {
          Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = "Verify"
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Verify Sign (+10 ⭐)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }

        if (isMatched) {
          IconButton(
            onClick = {
              HapticUtil.playTap(context)
              onResetMatch()
            },
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(Color.White.copy(alpha = 0.25f))
          ) {
            Icon(
              imageVector = Icons.Rounded.Refresh,
              contentDescription = "Reset",
              tint = Color.White
            )
          }
        }
      }
    }

    // Success Celebration Modal
    AnimatedVisibility(
      visible = isMatched,
      enter = fadeIn() + scaleIn(),
      modifier = Modifier.align(Alignment.Center)
    ) {
      Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = Modifier.padding(32.dp)
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(text = "🎉", fontSize = 54.sp)
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Awesome Job, Irssa!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1E293B)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "You accurately signed '${targetItem.title}'!",
            color = Color(0xFF64748B),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(14.dp))
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFC312)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = "Star",
                tint = Color(0xFF422C00)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "+10 Stars Earned!",
                fontWeight = FontWeight.Black,
                color = Color(0xFF422C00),
                fontSize = 15.sp
              )
            }
          }
          Spacer(modifier = Modifier.height(18.dp))
          Button(
            onClick = {
              HapticUtil.playTap(context)
              onResetMatch()
            },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C52E5)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Practice Next Sign", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
