package com.example.irssa

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.util.HapticUtil
import com.example.util.SignTtsHelper
import kotlinx.coroutines.delay

@Composable
fun PlayRoom(onPlay: (String)->Unit) {
  LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
    item { PageTitle("The play room","Learning feels like play.","Choose a little challenge. Try as many times as you like.") }
    item { GameTile("🧩","Find the sign","Match real hand shapes at your own pace.",Lavender) { onPlay("quiz") } }
    item { GameTile("⚡","Speed match","A gentle 60-second challenge. Pause anytime.",Peach) { onPlay("speed") } }
    item { GameTile("📷","Camera quest","Watch a letter. Make its shape. Collect a star.",Mint) { onPlay("cameraquiz") } }
    item { GameTile("✨","Sign to letters","Use your camera to build a little message.",Color(0xFFFFF2C9)) { onPlay("converter") } }
    item { GameTile("🔤","Word studio","Type a word, or let a grown-up say it.",Lavender) { onPlay("spell") } }
  }
}
@Composable
fun GameTile(emoji: String,title: String,description: String,color: Color,onClick: ()->Unit) {
  Surface(onClick=onClick,color=color,shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth()) {
    Row(Modifier.padding(22.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
      Text(emoji,fontSize=34.sp);Column(Modifier.weight(1f)) { Text(title,fontSize=19.sp,fontWeight=FontWeight.Bold);Text(description,color=Muted,fontSize=13.sp,lineHeight=19.sp,modifier=Modifier.padding(top=5.dp)) };Text("→",fontSize=25.sp,color=Purple)
    }
  }
}

@Composable
fun MatchGame(store: LearningStore,state: LearnerState,timed: Boolean) {
  val pool=store.lessons.filter { it.category=="Letters" && it.isStatic }
  var round by rememberSaveable { mutableIntStateOf(0) }
  var score by rememberSaveable { mutableIntStateOf(0) }
  var seconds by rememberSaveable { mutableIntStateOf(60) }
  var paused by rememberSaveable { mutableStateOf(false) }
  var chosen by rememberSaveable(round) { mutableStateOf<String?>(null) }
  var correct by rememberSaveable(round) { mutableStateOf(false) }
  val options=remember(round) { pool.shuffled(kotlin.random.Random(9127+round)).take(4) }
  val target=remember(round) { options[kotlin.random.Random(1703+round).nextInt(options.size)] }
  val context=LocalContext.current
  val lifecycle=LocalLifecycleOwner.current.lifecycle
  LaunchedEffect(timed,paused) {
    if(timed && !paused) lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
      while(seconds>0) { delay(1000);seconds-- }
    }
  }
  val ended=timed && seconds<=0
  LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp),modifier=Modifier.testTag("match_game")) {
    item { PageTitle(if(timed) "Speed match" else "Find the sign",if(ended) "Look how much you tried!" else "Can you find ${target.title}?",if(ended) "$score signs matched. Take a happy break, or play again." else "Look closely at the real hands.") }
    item { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
      Text("✓ $score matched",color=Purple,fontWeight=FontWeight.Bold)
      if(timed) { Text("◷ ${seconds}s",fontWeight=FontWeight.Bold);TextButton(onClick={paused=!paused},enabled=!ended) { Text(if(paused) "Continue" else "Pause") } }
    } }
    if(timed) item { LinearProgressIndicator(progress={seconds/60f},modifier=Modifier.fillMaxWidth(),color=Purple) }
    if(paused && !ended) item { Surface(color=Mint,shape=RoundedCornerShape(24.dp)) { Text("A little breather 🌿\nYour game will wait for you.",Modifier.fillMaxWidth().padding(30.dp),textAlign=TextAlign.Center) } }
    if(!ended && !paused) {
      items(options.chunked(2)) { pair -> Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        pair.forEach { option -> Card(modifier=Modifier.weight(1f),colors=CardDefaults.cardColors(containerColor=if(chosen==option.id) if(correct) Mint else Peach else Color.White)) {
          SignMedia(option,Modifier.fillMaxWidth().height(165.dp),compact=true)
          Button(onClick={
            chosen=option.id;correct=option.id==target.id
            if(correct) { score++;store.reward(target.id,"quiz");if(state.haptics) HapticUtil.playSuccess(context) }
          },enabled=!correct,modifier=Modifier.fillMaxWidth().padding(8.dp).testTag("answer_${option.id}")) {
            Text(if(chosen==option.id && correct) "✓ Found it!" else "This one")
          }
        } }
      } }
      if(chosen!=null) item { Surface(color=if(correct) Mint else Peach,shape=RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally) {
          Text(if(correct) "✨ You found ${target.title}!" else "A good try. Look at the fingers and try again.",fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
          if(correct) Button(onClick={round++},modifier=Modifier.padding(top=8.dp)) { Text("Next sign →") }
        }
      } }
    }
    if(ended) item { Button(onClick={seconds=60;score=0;round++;paused=false},modifier=Modifier.fillMaxWidth()) { Text("Play again") } }
  }
}

@Composable
fun CameraGame(store: LearningStore,state: LearnerState,initial: Lesson,challenge: Boolean,converter: Boolean) {
  val pool=store.lessons.filter { it.category=="Letters" && it.isStatic }
  var index by rememberSaveable { mutableIntStateOf(0) }
  var mode by rememberSaveable { mutableStateOf("psl") }
  var message by rememberSaveable { mutableStateOf("") }
  var reading by remember { mutableStateOf(CameraReading()) }
  var success by remember { mutableStateOf(false) }
  val target=if(challenge) pool[index%pool.size] else initial
  val context=LocalContext.current
  val speech=remember { SignTtsHelper(context) }
  DisposableEffect(speech) { onDispose { speech.shutdown() } }
  LaunchedEffect(target.id,mode) { success=false;reading=CameraReading() }
  LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp),modifier=Modifier.testTag("camera_game")) {
    item { PageTitle(if(converter) "Sign to letters" else if(challenge) "Camera quest" else "Your practice mirror",
      if(converter) "Hands with something to say." else "Let's try ${target.title}",
      if(converter) "One clear handshape at a time. Lower your hand between letters." else "Watch the sign, then have a go. There is no hurry.") }
    if(converter) item { Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
      FilterChip(selected=mode=="psl",onClick={mode="psl"},label={Text("Urdu ا–ے")})
      FilterChip(selected=mode=="english",onClick={mode="english"},label={Text("English A–Z")})
    } }
    if(!converter) item { SignMedia(target,Modifier.fillMaxWidth().height(220.dp)) }
    if(converter || target.isStatic) {
      item { PracticeCamera(if(converter) mode else target.recognitionMode,if(converter) null else target.classifierLabel,
        Modifier.fillMaxWidth().height(320.dp),targetDisplay=if(converter) null else target.title) { result ->
        reading=result
        if(result.confirmed) {
          if(converter) { val letter=store.lessons.firstOrNull { it.classifierLabel==result.label }?.title ?: result.label.orEmpty()
            if(message.length<120) message+=letter }
          else if(!success) { success=true;store.reward(target.id,"camera");if(state.haptics) HapticUtil.playSuccess(context) }
        }
      } }
      item { Surface(color=if(success) Mint else Lavender,shape=RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
          Text(if(success) "✨ Lovely work!" else reading.message,fontWeight=FontWeight.Bold)
          if(!success) LinearProgressIndicator(progress={reading.progress},modifier=Modifier.fillMaxWidth().padding(top=12.dp),color=Purple)
        }
      } }
      if(!converter && success && challenge) item { Button(onClick={index++;success=false},modifier=Modifier.fillMaxWidth()) { Text("Next letter →") } }
      if(converter) item { Surface(color=Color.White,shape=RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp)) {
          Text(message.ifEmpty { "Your letters will appear here…" },fontSize=26.sp,fontWeight=FontWeight.Bold,modifier=Modifier.fillMaxWidth())
          Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            TextButton(onClick={if(message.length<120) message+=" "}) { Text("Space") }
            TextButton(onClick={message=message.dropLast(1)},enabled=message.isNotEmpty()) { Text("⌫ Undo") }
            TextButton(onClick={message=""},enabled=message.isNotEmpty()) { Text("Clear") }
          }
          OutlinedButton(onClick={speech.speak(message)},enabled=message.isNotBlank(),modifier=Modifier.fillMaxWidth()) { Text("Read to a hearing person") }
        }
      } }
      item { Text("Camera hints can make mistakes. English J and Z need movement and are practised with a grown-up. This feature reads handshapes, not sentences.",fontSize=12.sp,color=Muted,lineHeight=18.sp) }
    } else {
      item { Surface(color=Mint,shape=RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(20.dp)) {
          Text("Practise together",fontSize=22.sp,fontWeight=FontWeight.Bold)
          Text("This sign needs movement${if(target.category=="Words") ", and sometimes both hands or your face" else ""}. Watch the whole demonstration and copy it with a grown-up.",Modifier.padding(vertical=12.dp),lineHeight=23.sp)
          Button(onClick={store.reward(target.id,"learn");success=true}) { Text(if(success) "✓ Practice saved" else "We practised together") }
        }
      } }
    }
  }
}

object Spelling {
  // Approximate Latin→Urdu transliteration so English input can still be fingerspelled
  // with PSL handshapes; Urdu spellings should be preferred when they are known.
  private val latinToPsl=mapOf('A' to "alif",'B' to "be",'C' to "seen",'D' to "dal",'E' to "alif",
    'F' to "fay",'G' to "gaf",'H' to "hay",'I' to "alif",'J' to "jim",'K' to "kaf",'L' to "laam",
    'M' to "meem",'N' to "noon",'O' to "alif",'P' to "pe",'Q' to "qaf",'R' to "re",'S' to "seen",
    'T' to "te",'U' to "alif",'V' to "vao",'W' to "vao",'X' to "khe",'Y' to "chhoti_yeh",'Z' to "ze")
  fun sequence(text: String,lessons: List<Lesson>): List<Lesson> {
    val whole=lessons.firstOrNull { it.category=="Words" && it.title.equals(text.trim(),true) }
    if(whole!=null) return listOf(whole)
    val words=text.trim().split(Regex("\\s+"))
    return words.flatMap { word ->
      val match=lessons.firstOrNull { it.category=="Words" && it.title.equals(word,true) }
      if(match!=null) listOf(match) else word.uppercase().mapNotNull { char ->
        if(char.isDigit()) lessons.firstOrNull { it.title==char.toString() && it.category=="Numbers" }
        // English words fingerspell with the English alphabet; Urdu letters are the fallback.
        else lessons.firstOrNull { it.id=="letter_en_${char.lowercase()}" }
          ?: lessons.firstOrNull { it.id=="letter_${latinToPsl[char]}" }
      }
    }
  }
}

@Composable
fun WordStudio(lessons: List<Lesson>,onLesson: (Lesson)->Unit) {
  var input by rememberSaveable { mutableStateOf("IRSSA") }
  var active by rememberSaveable { mutableIntStateOf(0) }
  var playing by rememberSaveable { mutableStateOf(false) }
  var notice by remember { mutableStateOf<String?>(null) }
  val sequence=remember(input) { Spelling.sequence(input,lessons) }
  val current=sequence.getOrNull(active)
  val owner=LocalLifecycleOwner.current
  val speech=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if(result.resultCode==Activity.RESULT_OK) { input=result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.take(60) ?: input;active=0;playing=false }
    else notice="No words received. You can type them below."
  }
  LaunchedEffect(playing,input) {
    if(playing && sequence.isNotEmpty()) owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
      while(playing) { delay(2200);if(active<sequence.lastIndex) active++ else { active=0;playing=false } }
    }
  }
  LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
    item { PageTitle("Word studio","Give a word some hands.","Known words show their sign. Other words are fingerspelled.") }
    item { OutlinedTextField(value=input,onValueChange={input=it.take(60);active=0;playing=false},modifier=Modifier.fillMaxWidth(),label={Text("A word or name")},singleLine=true,shape=RoundedCornerShape(20.dp)) }
    item { LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) { items(listOf("IRSSA","HELLO","WATER","BOOK","LOVE","CAT")) { word -> SuggestionChip(onClick={input=word;active=0;playing=false},label={Text(word)}) } } }
    if(notice!=null) item { Text(notice!!,color=Muted,fontSize=12.sp) }
    item { OutlinedButton(onClick={
      try { speech.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US").putExtra(RecognizerIntent.EXTRA_PROMPT,"A grown-up can say a word").putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true)) }
      catch(e: Exception) { notice="Speech input is not available on this device. Typing works offline." }
    },modifier=Modifier.fillMaxWidth()) { Text("A grown-up can say a word") } }
    if(current!=null) {
      item { LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) { items(sequence.indices.toList()) { i -> FilterChip(selected=i==active,onClick={active=i;playing=false},label={Text(sequence[i].title)}) } } }
      item { SignMedia(current,Modifier.fillMaxWidth().height(320.dp)) }
      item { Text("${active+1} of ${sequence.size}  ·  ${current.title}",color=Purple,fontWeight=FontWeight.Bold) }
      item { Button(onClick={playing=!playing},enabled=sequence.size>1 && sequence.none { it.isVideo },modifier=Modifier.fillMaxWidth()) { Text(if(playing) "Pause spelling" else "Play the letter sequence") } }
      item { TextButton(onClick={onLesson(current)},modifier=Modifier.fillMaxWidth()) { Text("Learn this sign →") } }
    } else item { Text("Type letters A–Z or digits 0–9 to begin.",Modifier.padding(24.dp),color=Muted) }
    item { Text("Fingerspelling is a spelling tool. It does not translate English grammar into PSL. System speech input may use your device's speech provider.",fontSize=12.sp,color=Muted,lineHeight=18.sp) }
  }
}
