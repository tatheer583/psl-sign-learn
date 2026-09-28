package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.SpaceBar
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.db.ProgressEntity
import com.example.data.model.ConversionMode
import com.example.data.model.GestureConversionResult
import com.example.data.model.GesturePresetsDataSource
import com.example.data.model.HandBodyGesturePreset
import com.example.ui.components.ConfettiCelebration
import com.example.ui.components.HandPoseCanvas
import com.example.util.BodyGestureConverterAnalyzer
import com.example.util.HapticUtil
import com.example.util.SignTtsHelper
import java.util.concurrent.Executors

@Composable
fun GestureConverterScreen(
  progress: ProgressEntity,
  onAwardStars: (Int) -> Unit = {},
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  BackHandler { onBack() }

  // TTS helper for reading converted letters/numbers and sentences
  val ttsHelper = remember { SignTtsHelper(context) }
  DisposableEffect(Unit) {
    onDispose {
      ttsHelper.shutdown()
    }
  }

  // Camera permissions
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

  // Camera settings
  var cameraFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
  var isTorchOn by remember { mutableStateOf(false) }
  var activeCamera by remember { mutableStateOf<Camera?>(null) }

  // Conversion Mode
  var conversionMode by remember { mutableStateOf(ConversionMode.ALPHABET) }

  // Accumulated converted text ribbon
  var accumulatedText by remember { mutableStateOf("IRSSA") }

  // Latest conversion result from analyzer
  var currentResult by remember {
    val defaultPreset = GesturePresetsDataSource.presets.first()
    mutableStateOf(
      GestureConversionResult(
        isHandDetected = true,
        symbol = defaultPreset.alphabetSymbol,
        label = defaultPreset.name,
        associatedWord = defaultPreset.alphabetWord,
        emoji = defaultPreset.emoji,
        pose = defaultPreset.pose,
        confidenceScore = 0.95f,
        stabilityProgress = 1.0f,
        isConfirmed = false,
        bodyPostureDescription = defaultPreset.bodyDescription,
        conversionMode = ConversionMode.ALPHABET
      )
    )
  }

  var showCelebration by remember { mutableStateOf(false) }
  var lastAppendedSymbol by remember { mutableStateOf("") }
  var lastAppendTime by remember { mutableStateOf(0L) }

  // Animated glow for body & hand guide
  val infiniteTransition = rememberInfiniteTransition(label = "converter_glow")
  val guidePulse by infiniteTransition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "guide_scale"
  )

  // Analyzer instance holder
  var analyzerInstance by remember { mutableStateOf<BodyGestureConverterAnalyzer?>(null) }

  // Function to append a recognized or tapped symbol
  fun appendSymbol(symbol: String, word: String, playSound: Boolean = true) {
    if (symbol.isBlank()) return
    accumulatedText += symbol
    HapticUtil.playSuccess(context)
    onAwardStars(5)
    showCelebration = true
    if (playSound) {
      val speech = if (conversionMode == ConversionMode.NUMBER) "Number $symbol" else "$symbol, $word"
      ttsHelper.speak(speech)
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F172A))
      .testTag("gesture_converter_screen")
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      // 1. Top App Bar
      Surface(
        color = Color(0xFF1E293B),
        shadowElevation = 4.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
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
                  .size(42.dp)
                  .background(Color.White.copy(alpha = 0.10f), CircleShape)
                  .testTag("converter_back_btn")
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                  contentDescription = "Back",
                  tint = Color.White
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "Gesture Converter",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF5C52E5)
                  ) {
                    Text(
                      text = "LIVE",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Black,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                  text = "Sign Hand & Body ➔ Alphabet / Number",
                  fontSize = 12.sp,
                  color = Color(0xFF94A3B8)
                )
              }
            }

            // Camera Controls: Flip & Torch
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              IconButton(
                onClick = {
                  HapticUtil.playTap(context)
                  cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                  } else {
                    CameraSelector.LENS_FACING_FRONT
                  }
                },
                modifier = Modifier
                  .size(38.dp)
                  .background(Color.White.copy(alpha = 0.12f), CircleShape)
                  .testTag("converter_switch_camera")
              ) {
                Icon(
                  imageVector = Icons.Rounded.Cameraswitch,
                  contentDescription = "Switch Camera",
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }

              IconButton(
                onClick = {
                  HapticUtil.playTap(context)
                  isTorchOn = !isTorchOn
                },
                modifier = Modifier
                  .size(38.dp)
                  .background(
                    if (isTorchOn) Color(0xFFFFC312) else Color.White.copy(alpha = 0.12f),
                    CircleShape
                  )
                  .testTag("converter_torch_btn")
              ) {
                Icon(
                  imageVector = Icons.Rounded.FlashOn,
                  contentDescription = "Torch",
                  tint = if (isTorchOn) Color.Black else Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Mode Selector Tabs: Alphabet (A-Z) | Numbers (1-50) | Auto
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ConversionMode.values().forEach { mode ->
              val isSelected = conversionMode == mode
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Color(0xFF5C52E5) else Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(16.dp))
                  .clickable {
                    HapticUtil.playTap(context)
                    conversionMode = mode
                    val samplePreset = GesturePresetsDataSource.presets.first()
                    analyzerInstance?.setManualPreset(samplePreset, mode)
                  }
                  .testTag("converter_mode_${mode.name.lowercase()}")
              ) {
                Row(
                  modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = mode.iconEmoji, fontSize = 14.sp)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = mode.label.substringBefore(" ("),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                  )
                }
              }
            }
          }
        }
      }

      // 2. Scrollable Body containing Camera Feed, Result Card, Text Ribbon, & Interactive Carousel
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // --- LIVE CAMERA & BODY/HAND GUIDE CARD ---
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .height(290.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
          ) {
            Box(modifier = Modifier.fillMaxSize()) {
              if (hasCameraPermission) {
                // Live CameraX Feed
                AndroidView(
                  factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                      scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()

                    val analyzer = BodyGestureConverterAnalyzer(
                      getMode = { conversionMode },
                      onResult = { result ->
                        currentResult = result

                        // Auto-confirm logic when steady
                        val now = System.currentTimeMillis()
                        if (result.isConfirmed && result.symbol.isNotBlank()) {
                          if (result.symbol != lastAppendedSymbol || (now - lastAppendTime > 3000)) {
                            lastAppendedSymbol = result.symbol
                            lastAppendTime = now
                            appendSymbol(result.symbol, result.associatedWord, playSound = true)
                          }
                        }
                      }
                    )
                    analyzerInstance = analyzer

                    cameraProviderFuture.addListener({
                      val cameraProvider = cameraProviderFuture.get()
                      val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                      }

                      val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                          it.setAnalyzer(cameraExecutor, analyzer)
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
                        // Fail gracefully
                      }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                  },
                  update = {
                    try {
                      activeCamera?.cameraControl?.enableTorch(isTorchOn)
                    } catch (_: Exception) {}
                  },
                  modifier = Modifier.fillMaxSize()
                )

                // Hand and Body Silhouette Overlay
                val guideColor = when {
                  currentResult.isConfirmed -> Color(0xFF00B894)
                  currentResult.isHandDetected -> Color(0xFFFFC312)
                  else -> Color(0xFF00B4D8)
                }

                Canvas(
                  modifier = Modifier
                    .fillMaxSize()
                    .scale(guidePulse)
                ) {
                  val w = size.width
                  val h = size.height
                  val dashEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)

                  // 1. Upper Body Silhouette (Head & Shoulders)
                  val bodyPath = Path().apply {
                    // Head arc
                    val headRadius = w * 0.16f
                    val headCenter = Offset(w * 0.5f, h * 0.28f)
                    addOval(
                      androidx.compose.ui.geometry.Rect(
                        center = headCenter,
                        radius = headRadius
                      )
                    )
                  }
                  drawPath(
                    path = bodyPath,
                    color = guideColor.copy(alpha = 0.40f),
                    style = Stroke(width = 3.dp.toPx(), pathEffect = dashEffect)
                  )

                  // Shoulders & Chest line
                  val shouldersPath = Path().apply {
                    moveTo(w * 0.15f, h * 0.75f)
                    quadraticTo(w * 0.28f, h * 0.48f, w * 0.40f, h * 0.46f)
                    lineTo(w * 0.60f, h * 0.46f)
                    quadraticTo(w * 0.72f, h * 0.48f, w * 0.85f, h * 0.75f)
                  }
                  drawPath(
                    path = shouldersPath,
                    color = guideColor.copy(alpha = 0.35f),
                    style = Stroke(width = 3.dp.toPx(), pathEffect = dashEffect)
                  )

                  // 2. Hand Target Guide Ring (center-right where hand signs)
                  val handBoxLeft = w * 0.25f
                  val handBoxTop = h * 0.28f
                  val handBoxSize = w * 0.50f

                  drawRoundRect(
                    color = guideColor.copy(alpha = 0.85f),
                    topLeft = Offset(handBoxLeft, handBoxTop),
                    size = Size(handBoxSize, handBoxSize),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                  )
                }

                // Top Badge: Detected Posture
                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = Color.Black.copy(alpha = 0.70f),
                  modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(text = currentResult.emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = if (currentResult.isHandDetected) currentResult.label else "Sign in front of camera",
                      color = Color.White,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                // Bottom Gauge: Hold to Convert
                Column(
                  modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                      Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                      )
                    )
                    .padding(14.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = currentResult.bodyPostureDescription,
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  LinearProgressIndicator(
                    progress = { currentResult.stabilityProgress },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = if (currentResult.isConfirmed) Color(0xFF00B894) else Color(0xFF5C52E5),
                    trackColor = Color.White.copy(alpha = 0.20f)
                  )
                }
              } else {
                // Permission Request Card
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Videocam,
                    contentDescription = "Camera Permission",
                    tint = Color(0xFF5C52E5),
                    modifier = Modifier.size(52.dp)
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = "Camera Access Needed",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = "Irssa's hand & body converter needs the camera to turn sign gestures into letters and numbers!",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                  )
                  Spacer(modifier = Modifier.height(16.dp))
                  Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C52E5)),
                    shape = RoundedCornerShape(14.dp)
                  ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }

        // --- CONVERTED SYMBOL & GESTURE RECOGNITION HERO CARD ---
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(
              2.dp,
              if (currentResult.isConfirmed) Color(0xFF00B894) else Color(0xFF5C52E5).copy(alpha = 0.4f)
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Big Converted Symbol Display Circle
              Surface(
                shape = RoundedCornerShape(22.dp),
                color = when (conversionMode) {
                  ConversionMode.NUMBER -> Color(0xFF00B894).copy(alpha = 0.15f)
                  ConversionMode.ALPHABET -> Color(0xFF5C52E5).copy(alpha = 0.15f)
                  ConversionMode.AUTO -> Color(0xFFFF7849).copy(alpha = 0.15f)
                },
                border = androidx.compose.foundation.BorderStroke(
                  2.dp,
                  when (conversionMode) {
                    ConversionMode.NUMBER -> Color(0xFF00B894)
                    ConversionMode.ALPHABET -> Color(0xFF5C52E5)
                    ConversionMode.AUTO -> Color(0xFFFF7849)
                  }
                ),
                modifier = Modifier.size(90.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = currentResult.symbol.ifEmpty { "—" },
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                  )
                }
              }

              Spacer(modifier = Modifier.width(16.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "Converted Symbol",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  if (currentResult.isConfirmed) {
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = Color(0xFF00B894).copy(alpha = 0.20f)
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Icon(
                          imageVector = Icons.Rounded.CheckCircle,
                          contentDescription = "Matched",
                          tint = Color(0xFF00B894),
                          modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                          text = "MATCHED",
                          color = Color(0xFF00B894),
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Black
                        )
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                  text = currentResult.associatedWord.ifEmpty { "Sign gesture to convert" },
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Black,
                  color = Color.White
                )

                Text(
                  text = "Gesture: ${currentResult.label}",
                  fontSize = 12.sp,
                  color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions: Speak & Append
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Button(
                    onClick = {
                      HapticUtil.playTap(context)
                      appendSymbol(currentResult.symbol, currentResult.associatedWord)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C52E5)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("converter_append_btn")
                  ) {
                    Text("+ Append '${currentResult.symbol}'", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }

                  IconButton(
                    onClick = {
                      HapticUtil.playTap(context)
                      val speech = if (conversionMode == ConversionMode.NUMBER) {
                        "Number ${currentResult.symbol}"
                      } else {
                        "Letter ${currentResult.symbol}, ${currentResult.associatedWord}"
                      }
                      ttsHelper.speak(speech)
                    },
                    modifier = Modifier
                      .size(38.dp)
                      .background(Color.White.copy(alpha = 0.12f), CircleShape)
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                      contentDescription = "Speak Letter",
                      tint = Color.White,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // --- ACCUMULATED CONVERTED TEXT / SENTENCE RIBBON ---
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Converted Text Ribbon",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = Color(0xFF94A3B8)
                )

                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = Color(0xFFFFC312).copy(alpha = 0.15f)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Star,
                      contentDescription = "Stars",
                      tint = Color(0xFFFFC312),
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "${progress.stars} Stars",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFFFC312)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Display Area
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(64.dp)
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                  contentAlignment = Alignment.CenterStart
                ) {
                  Text(
                    text = accumulatedText.ifEmpty { "Start signing to spell here..." },
                    fontSize = if (accumulatedText.isEmpty()) 14.sp else 22.sp,
                    fontWeight = FontWeight.Black,
                    color = if (accumulatedText.isEmpty()) Color(0xFF64748B) else Color(0xFF38BDF8),
                    letterSpacing = 2.sp
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Actions: Speak Full Word, Space, Backspace, Clear, Copy
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Speak button
                Button(
                  onClick = {
                    HapticUtil.playTap(context)
                    if (accumulatedText.isNotBlank()) {
                      ttsHelper.speak(accumulatedText)
                    } else {
                      ttsHelper.speak("No text to read yet")
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894)),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = "Speak Sentence",
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Speak", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  // Space
                  IconButton(
                    onClick = {
                      HapticUtil.playTap(context)
                      accumulatedText += " "
                    },
                    modifier = Modifier
                      .size(40.dp)
                      .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.SpaceBar,
                      contentDescription = "Add Space",
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }

                  // Backspace
                  IconButton(
                    onClick = {
                      HapticUtil.playTap(context)
                      if (accumulatedText.isNotEmpty()) {
                        accumulatedText = accumulatedText.dropLast(1)
                      }
                    },
                    modifier = Modifier
                      .size(40.dp)
                      .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Rounded.Backspace,
                      contentDescription = "Backspace",
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }

                  // Copy
                  IconButton(
                    onClick = {
                      HapticUtil.playTap(context)
                      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                      val clip = ClipData.newPlainText("Converted Signs", accumulatedText)
                      clipboard.setPrimaryClip(clip)
                      Toast.makeText(context, "Copied '$accumulatedText' to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                      .size(40.dp)
                      .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.ContentCopy,
                      contentDescription = "Copy",
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }

                  // Clear
                  IconButton(
                    onClick = {
                      HapticUtil.playTap(context)
                      accumulatedText = ""
                    },
                    modifier = Modifier
                      .size(40.dp)
                      .background(Color(0xFFFF5E7E).copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Delete,
                      contentDescription = "Clear All",
                      tint = Color(0xFFFF5E7E),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // --- INTERACTIVE POSTURE & GESTURE CAROUSEL (TAP OR SIGN SANDBOX) ---
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp, bottom = 24.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Interactive Gesture Sandbox 🖐️",
                  fontWeight = FontWeight.Black,
                  fontSize = 16.sp,
                  color = Color.White
                )
                Text(
                  text = "Tap or sign any gesture to convert to letter/number!",
                  fontSize = 12.sp,
                  color = Color(0xFF94A3B8)
                )
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF5C52E5).copy(alpha = 0.15f)
              ) {
                Text(
                  text = "${GesturePresetsDataSource.presets.size} Gestures",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF818CF8)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
            ) {
              items(GesturePresetsDataSource.presets) { preset ->
                val symbol = when (conversionMode) {
                  ConversionMode.ALPHABET -> preset.alphabetSymbol
                  ConversionMode.NUMBER -> preset.numberSymbol
                  ConversionMode.AUTO -> "${preset.alphabetSymbol}/${preset.numberSymbol}"
                }

                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = Color(0xFF1E293B),
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (currentResult.label == preset.name) Color(0xFF00B894) else Color(0xFF334155)
                  ),
                  modifier = Modifier
                    .width(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                      HapticUtil.playTap(context)
                      analyzerInstance?.setManualPreset(preset, conversionMode)
                      val chosenSym = when (conversionMode) {
                        ConversionMode.ALPHABET -> preset.alphabetSymbol
                        ConversionMode.NUMBER -> preset.numberSymbol
                        ConversionMode.AUTO -> preset.alphabetSymbol
                      }
                      val chosenWord = when (conversionMode) {
                        ConversionMode.ALPHABET -> preset.alphabetWord
                        ConversionMode.NUMBER -> preset.numberWord
                        ConversionMode.AUTO -> preset.alphabetWord
                      }
                      appendSymbol(chosenSym, chosenWord)
                    }
                    .testTag("preset_card_${preset.id}")
                ) {
                  Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Box(
                      modifier = Modifier
                        .size(54.dp)
                        .background(Color.White.copy(alpha = 0.06f), CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(text = preset.emoji, fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                      text = preset.name,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      textAlign = TextAlign.Center,
                      maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = Color(0xFF5C52E5).copy(alpha = 0.20f)
                    ) {
                      Text(
                        text = "➔ $symbol",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Celebration Confetti Explosion
    ConfettiCelebration(
      isActive = showCelebration,
      onFinished = { showCelebration = false }
    )
  }
}
