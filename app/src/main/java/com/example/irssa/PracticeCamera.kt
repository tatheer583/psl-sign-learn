package com.example.irssa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

data class CameraReading(val label: String?=null,val progress: Float=0f,val confirmed: Boolean=false,
  val message: String="Show one hand, with all your fingers in view.")

@Composable
fun PracticeCamera(mode: String,target: String?,modifier: Modifier=Modifier,onReading: (CameraReading)->Unit) {
  val context=LocalContext.current
  var permission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) }
  var requested by remember { mutableStateOf(false) }
  val request=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission=it;requested=true }
  if(!permission) {
    Surface(modifier,shape=RoundedCornerShape(24.dp),color=Color(0xFFEDEAFB)) {
      Column(Modifier.padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        Text("📷",style=MaterialTheme.typography.displaySmall)
        Text("A mirror for your signs",style=MaterialTheme.typography.titleLarge)
        Text("Camera practice happens on this device. Pictures are never saved or sent.",modifier=Modifier.padding(vertical=12.dp))
        Button(onClick={request.launch(Manifest.permission.CAMERA)}) { Text("Allow camera") }
        if(requested) TextButton(onClick={context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:${context.packageName}")))}) { Text("Open camera permission settings") }
      }
    }
    return
  }
  val owner=LocalLifecycleOwner.current
  val latestCallback by rememberUpdatedState(onReading)
  var front by remember { mutableStateOf(true) }
  var torch by remember { mutableStateOf(false) }
  var hasTorch by remember { mutableStateOf(false) }
  var camera by remember { mutableStateOf<Camera?>(null) }
  var failure by remember { mutableStateOf<String?>(null) }
  var retry by remember { mutableIntStateOf(0) }
  val previewView=remember(context) { PreviewView(context).apply { scaleType=PreviewView.ScaleType.FIT_CENTER } }
  DisposableEffect(owner,front,mode,target,retry) {
    val active=AtomicBoolean(true)
    val executor=Executors.newSingleThreadExecutor()
    val main=Handler(Looper.getMainLooper())
    val gate=StableSignGate()
    var landmarker: HandLandmarker?=null
    var classifier: LandmarkClassifier?=null
    var lastFrame=0L
    var provider: ProcessCameraProvider?=null
    var analysis: ImageAnalysis?=null
    var preview: Preview?=null
    fun deliver(reading: CameraReading) { main.post { if(active.get()) latestCallback(reading) } }
    failure=null;torch=false;camera=null
    val future=ProcessCameraProvider.getInstance(context)
    future.addListener({
      if(active.get()) try {
        val p=future.get();provider=p
        val preferred=if(front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        val fallback=if(front) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
        val selector=if(p.hasCamera(preferred)) preferred else fallback
        val view=Preview.Builder().build().also { it.surfaceProvider=previewView.surfaceProvider };preview=view
        @Suppress("DEPRECATION")
        val frames=ImageAnalysis.Builder().setTargetResolution(android.util.Size(640,480))
          .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();analysis=frames
        frames.setAnalyzer(executor) { frame ->
          try {
            val now=SystemClock.uptimeMillis()
            if(active.get() && now-lastFrame>=90) {
              lastFrame=now
              if(landmarker==null) {
                landmarker=HandLandmarker.createFromOptions(context,HandLandmarker.HandLandmarkerOptions.builder()
                  .setBaseOptions(BaseOptions.builder().setModelAssetPath("models/hand_landmarker.task").build())
                  .setNumHands(2).setMinHandDetectionConfidence(0.6f).setMinHandPresenceConfidence(0.6f)
                  .setRunningMode(RunningMode.IMAGE).build())
                classifier=LandmarkClassifier(context.assets.open("models/$mode.bin"))
              }
              val bitmap=frame.toBitmap()
              val rotation=Matrix().apply { postRotate(frame.imageInfo.rotationDegrees.toFloat()) }
              val upright=Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,rotation,true)
              val image=BitmapImageBuilder(upright).build()
              try {
                val result=landmarker!!.detect(image)
                val hands=result.landmarks()
                if(hands.size!=1) {
                  gate.update(null,now,handPresent=hands.isNotEmpty())
                  deliver(CameraReading(message=if(hands.isEmpty()) "Show your hand. Good light helps!" else "For this game, show just one hand."))
                } else {
                  val points=hands[0].map { Landmark(it.x(),it.y(),it.z()) }
                  val prediction=classifier!!.predict(points)
                  val candidate=prediction?.takeIf { it.accepted }?.label
                  val matches=candidate!=null && (target==null || target==candidate)
                  val hold=gate.update(if(matches) candidate else null,now,handPresent=true)
                  deliver(CameraReading(candidate,hold.progress,hold.confirmed,when {
                    hold.confirmed -> "You did it! ✨ Lower your hand before the next sign."
                    candidate==null -> "I see your hand. Turn it gently towards the camera."
                    target!=null && candidate!=target -> "Let's look at $target again. Take your time."
                    hold.progress>=1f -> "Lower your hand, then show another sign."
                    else -> "Keep this shape for a moment…"
                  }))
                }
              } finally { image.close();if(upright!==bitmap) upright.recycle();bitmap.recycle() }
            }
          } catch(e: Exception) {
            gate.reset();deliver(CameraReading(message="Camera practice needs a restart. Tap Retry."))
            main.post { if(active.get()) failure="Camera practice could not start. Your lessons are still available." }
          } finally { frame.close() }
        }
        val c=p.bindToLifecycle(owner,selector,view,frames);camera=c;hasTorch=c.cameraInfo.hasFlashUnit()
      } catch(e: Exception) { failure="This camera is unavailable. Try switching cameras or reopening practice." }
    },ContextCompat.getMainExecutor(context))
    onDispose {
      active.set(false);analysis?.clearAnalyzer()
      val owned=listOfNotNull(preview,analysis).toTypedArray();if(owned.isNotEmpty()) provider?.unbind(*owned)
      executor.execute { landmarker?.close() };executor.shutdown();camera=null
    }
  }
  LaunchedEffect(torch,camera) { camera?.cameraControl?.enableTorch(torch) }
  Box(modifier.clip(RoundedCornerShape(24.dp)).background(Color(0xFF10232C))) {
    AndroidView(factory={previewView},modifier=Modifier.fillMaxSize())
    Row(Modifier.align(Alignment.TopEnd).padding(8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
      FilledTonalButton(onClick={front=!front}) { Text("⇄ Flip") }
      if(hasTorch) FilledTonalButton(onClick={torch=!torch}) { Text(if(torch) "Light off" else "Light on") }
    }
    failure?.let { message -> Surface(Modifier.align(Alignment.Center).padding(16.dp),shape=RoundedCornerShape(16.dp)) {
      Column(Modifier.padding(16.dp)) { Text(message);Button(onClick={retry++}) { Text("Retry") } }
    } }
    Surface(Modifier.align(Alignment.BottomStart).padding(10.dp),color=Color(0xDD142A32),shape=RoundedCornerShape(12.dp)) {
      Text("●  On your device",Modifier.padding(8.dp),color=Color.White,style=MaterialTheme.typography.labelSmall)
    }
  }
}
