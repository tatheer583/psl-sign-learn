package com.example.irssa

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

@Composable
fun SignMedia(lesson: Lesson,modifier: Modifier=Modifier,compact: Boolean=false) {
  val context=LocalContext.current
  val owner=LocalLifecycleOwner.current
  if(lesson.media.isEmpty()) {
    Surface(modifier,color=Peach,shape=RoundedCornerShape(24.dp)) {
      Column(Modifier.padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        Text(lesson.title,style=MaterialTheme.typography.displayLarge,fontWeight=FontWeight.Bold)
        Text("Let's learn this one with your teacher.",textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        Text("A checked video is not available yet.",style=MaterialTheme.typography.bodySmall,color=Muted,modifier=Modifier.padding(top=12.dp))
      }
    }
    return
  }
  if(!lesson.isVideo) {
    Box(modifier.clip(RoundedCornerShape(24.dp)).background(Color(0xFF151E26)),contentAlignment=Alignment.Center) {
      AsyncImage(model="file:///android_asset/${lesson.media}",contentDescription="A real hand showing PSL ${lesson.title}",
        modifier=Modifier.fillMaxSize().padding(12.dp),contentScale=ContentScale.Fit)
    }
    return
  }
  key(lesson.id) {
    var playing by remember { mutableStateOf(false) }
    var slow by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val player=remember(lesson.id) { ExoPlayer.Builder(context).build().apply {
      volume=0f;repeatMode=Player.REPEAT_MODE_ONE
      setMediaItem(MediaItem.fromUri(Uri.parse("asset:///${lesson.media}")));prepare()
    } }
    val wantedPlay by rememberUpdatedState(playing)
    DisposableEffect(player,owner) {
      val listener=object: Player.Listener { override fun onPlayerError(e: PlaybackException) { error=true;playing=false } }
      val observer=LifecycleEventObserver { _,event ->
        if(event==Lifecycle.Event.ON_PAUSE) player.pause()
        if(event==Lifecycle.Event.ON_RESUME && wantedPlay) player.play()
      }
      player.addListener(listener);owner.lifecycle.addObserver(observer)
      onDispose { owner.lifecycle.removeObserver(observer);player.removeListener(listener);player.release() }
    }
    Column(modifier.clip(RoundedCornerShape(24.dp)).background(Color(0xFF102E35))) {
      Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center) {
        AndroidView(factory={ PlayerView(it).apply {
          this.player=player;useController=false;resizeMode=AspectRatioFrameLayout.RESIZE_MODE_FIT
          setShutterBackgroundColor(android.graphics.Color.rgb(16,46,53))
        } },update={ it.player=player },modifier=Modifier.fillMaxSize())
        if(error) Surface(color=Color.White,shape=RoundedCornerShape(16.dp),modifier=Modifier.padding(16.dp)) {
          Column(Modifier.padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            Text("Let's open this sign again.")
            TextButton(onClick={error=false;player.prepare()}) { Text("Try again") }
          }
        }
      }
      Row(Modifier.fillMaxWidth().padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceEvenly) {
        TextButton(onClick={ playing=!playing;if(playing) player.play() else player.pause() }) {
          Text(if(playing) "Ⅱ Pause" else "▶ Watch",color=Color.White,fontWeight=FontWeight.Bold)
        }
        TextButton(onClick={player.seekTo(0);playing=true;player.play()}) { Text("↺ Again",color=Color.White) }
        if(!compact) TextButton(onClick={slow=!slow;player.setPlaybackSpeed(if(slow) 0.5f else 1f)}) {
          Text(if(slow) "½ Slow" else "1× Speed",color=Color.White)
        }
      }
    }
  }
}
