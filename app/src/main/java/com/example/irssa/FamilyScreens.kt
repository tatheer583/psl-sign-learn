package com.example.irssa

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.HapticUtil
import com.example.util.SignTtsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Need(val emoji: String,val english: String,val urdu: String,val spoken: String)
val needs=listOf(
  Need("💧","Water","پانی","I would like some water, please."),Need("🍽","Food","کھانا","I am hungry. I would like some food."),
  Need("🚻","Toilet","بیت الخلا","I need to use the toilet."),Need("🤝","Help","مدد","I need your help, please."),
  Need("🛑","Stop","رکیں","Please stop."),Need("💛","A hug","گلے لگائیں","I would like a hug."),
  Need("😣","It hurts","درد ہو رہا ہے","Something hurts. Please help me."),Need("🌿","A break","آرام","I need a break."),
  Need("😊","Happy","خوش","I feel happy."),Need("😔","Sad","اداس","I feel sad."),
  Need("😨","Scared","ڈر لگ رہا ہے","I feel scared. Please stay with me."),Need("🎲","Let's play","کھیلیں","I would like to play."),
  Need("✅","Yes","ہاں","Yes."),Need("❎","No","نہیں","No."),Need("💤","Sleepy","نیند آ رہی ہے","I feel sleepy."),
)

@Composable
fun TalkBoard(state: LearnerState) {
  val context=LocalContext.current
  val speech=remember { SignTtsHelper(context) }
  DisposableEffect(speech) { onDispose { speech.shutdown() } }
  var selected by rememberSaveable { mutableStateOf<String?>(null) }
  val current=needs.firstOrNull { it.english==selected }
  LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp),modifier=Modifier.testTag("talk_board")) {
    item { PageTitle("My voice board","I have something to say.","Tap a picture. Show it to someone you trust.") }
    items(needs.chunked(3)) { row -> Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
      row.forEach { need -> Surface(onClick={selected=need.english;if(state.haptics) HapticUtil.playTap(context)},modifier=Modifier.weight(1f).testTag("need_${need.english}"),shape=RoundedCornerShape(22.dp),color=if(selected==need.english) Lavender else Color.White) {
        Column(Modifier.padding(10.dp).heightIn(min=112.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
          Text(need.emoji,fontSize=34.sp);Text(need.english,fontWeight=FontWeight.Bold,fontSize=14.sp,textAlign=TextAlign.Center)
          if(state.showUrdu) Text(need.urdu,fontSize=15.sp,color=Muted,textAlign=TextAlign.Center)
        }
      } }
    } }
  }
  if(current!=null) AlertDialog(onDismissRequest={selected=null},icon={Text(current.emoji,fontSize=64.sp)},
    title={Text(current.spoken,fontSize=25.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)},
    text={if(state.showUrdu) Text(current.urdu,fontSize=28.sp,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth())},
    confirmButton={Button(onClick={speech.speak(current.spoken)}) { Text("Read aloud") }},
    dismissButton={TextButton(onClick={selected=null}) { Text("Done") }})
}

data class Achievement(val emoji: String,val name: String,val description: String,val earned: Boolean)
fun achievements(s: LearnerState)=listOf(
  Achievement("🌱","First little step","Explore your first sign",s.seen.isNotEmpty()),
  Achievement("🍎","Letter explorer","Explore 5 different letters",s.seen.count { it.startsWith("letter_") }>=5),
  Achievement("🔢","Number finder","Explore 5 different numbers",s.seen.count { it.startsWith("num_") }>=5),
  Achievement("🤝","Everyday friend","Explore 4 everyday words",s.seen.count { it.startsWith("vocab_") }>=4),
  Achievement("📷","Camera adventurer","Match 5 different signs in the camera",s.mastered.size>=5),
  Achievement("🌻","Growing habit","Practise on 3 different days",s.practiceDays.size>=3),
  Achievement("🔤","Alphabet garden","Explore all 26 letters",s.seen.count { it.startsWith("letter_") }>=26),
  Achievement("🌟","Bright little star","Collect 100 stars",s.stars>=100),
)
@Composable
fun StarGarden(state: LearnerState) {
  LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
    item { PageTitle("Your star garden","Look how you're growing.","Every attempt is a little step forward.") }
    item { Surface(color=Mint,shape=RoundedCornerShape(26.dp)) {
      Row(Modifier.fillMaxWidth().padding(22.dp),horizontalArrangement=Arrangement.SpaceEvenly) {
        Stat("${state.seen.size}","signs explored");Stat("${state.streak()}","day streak");Stat("${state.stars}","stars")
      }
    } }
    items(achievements(state).chunked(2)) { row -> Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
      row.forEach { badge -> Surface(color=if(badge.earned) Color(0xFFFFF2C9) else Color.White,shape=RoundedCornerShape(24.dp),modifier=Modifier.weight(1f)) {
        Column(Modifier.padding(18.dp).heightIn(min=152.dp),horizontalAlignment=Alignment.CenterHorizontally) {
          Text(if(badge.earned) badge.emoji else "🌿",fontSize=38.sp)
          Text(badge.name,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center,modifier=Modifier.padding(vertical=8.dp))
          Text(if(badge.earned) "Yours to keep!" else badge.description,fontSize=12.sp,color=Muted,textAlign=TextAlign.Center)
        }
      } }
    } }
    item { Text("Stars celebrate practice. You can always learn any sign, with or without stars.",fontSize=12.sp,color=Muted,textAlign=TextAlign.Center) }
  }
}
@Composable private fun Stat(value: String,label: String) { Column(horizontalAlignment=Alignment.CenterHorizontally) { Text(value,fontSize=28.sp,fontWeight=FontWeight.ExtraBold);Text(label,fontSize=10.sp,color=Muted) } }

@Composable
fun ParentGate(onDismiss: ()->Unit,onOpen: ()->Unit) {
  var answer by remember { mutableStateOf("") }
  val a=remember { (11..19).random() };val b=remember { (7..12).random() }
  AlertDialog(onDismissRequest=onDismiss,title={Text("For a grown-up")},text={Column {
    Text("Learning settings and source links are here.\nWhat is $a + $b?")
    OutlinedTextField(value=answer,onValueChange={answer=it.filter(Char::isDigit).take(3)},label={Text("Answer")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.testTag("parent_answer"))
  } },confirmButton={Button(onClick=onOpen,enabled=answer.toIntOrNull()==a+b) { Text("Open settings") }},dismissButton={TextButton(onClick=onDismiss) { Text("Back to play") }})
}

@Composable
fun FamilySettings(store: LearningStore,state: LearnerState) {
  val context=LocalContext.current
  val scope=rememberCoroutineScope()
  var name by rememberSaveable { mutableStateOf(state.name) }
  var notice by remember { mutableStateOf<String?>(null) }
  var reset by remember { mutableStateOf(false) }
  val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
    if(uri!=null) scope.launch {
      notice=withContext(Dispatchers.IO) { runCatching {
        requireNotNull(context.contentResolver.openOutputStream(uri)).use { it.write(LearningStore.encode(state).toByteArray()) }
        "Progress backup saved."
      }.getOrElse { "The backup could not be saved. Please try again." } }
    }
  }
  fun open(url: String) { try { context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url))) } catch(e: Exception) { notice="No browser is available on this device." } }
  LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
    item { PageTitle("For her grown-ups","A little support goes a long way.","Practise together for a few happy minutes. Follow her pace.") }
    item { OutlinedTextField(value=name,onValueChange={name=it.take(24)},label={Text("Her name")},singleLine=true,modifier=Modifier.fillMaxWidth()) }
    item { Button(onClick={store.update { it.copy(name=name.trim().ifEmpty { "Irssa" }) };notice="Name saved."}) { Text("Save name") } }
    item { SettingToggle("Gentle vibrations","A touch cue after taps and success",state.haptics) { value -> store.update { it.copy(haptics=value) } } }
    item { SettingToggle("Urdu on the picture board","English and Urdu labels for everyday needs",state.showUrdu) { value -> store.update { it.copy(showUrdu=value) } } }
    item { Surface(color=Color.White,shape=RoundedCornerShape(20.dp)) { Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
      Column(Modifier.weight(1f)) { Text("Daily practice goal",fontWeight=FontWeight.Bold);Text("${state.dailyGoal} signs • no time pressure",color=Muted,fontSize=12.sp) }
      TextButton(onClick={store.update { it.copy(dailyGoal=(it.dailyGoal-1).coerceAtLeast(3)) }},enabled=state.dailyGoal>3) { Text("−",fontSize=24.sp) }
      TextButton(onClick={store.update { it.copy(dailyGoal=(it.dailyGoal+1).coerceAtMost(10)) }},enabled=state.dailyGoal<10) { Text("+",fontSize=24.sp) }
    } } }
    item { Text("The right sign language",fontSize=20.sp,fontWeight=FontWeight.Bold)
      Text("The lessons in this edition use Pakistan Sign Language (PSL), with handshape references from a published PSL research dataset. Always confirm signs with her family and teacher — a photo cannot replace a fluent Deaf educator.",color=Muted,modifier=Modifier.padding(top=8.dp),lineHeight=23.sp)
      OutlinedButton(onClick={open("https://psl.org.pk/")},modifier=Modifier.fillMaxWidth().padding(top=10.dp)) { Text("Visit Deaf Reach's PSL resources ↗") }
    }
    item { Text("Camera coaching",fontSize=20.sp,fontWeight=FontWeight.Bold)
      Text("An offline experimental coach checks the 37 PSL handshapes and 24 static English letters, using real data from two independent signers. It can still make mistakes, especially with unfamiliar hands, angles or lighting. English J and Z, the numbers and everyday words use guided practice. In the tools folder, capture_my_hand.py can add her own hand to the model. No camera score is a measure of her ability.",color=Muted,modifier=Modifier.padding(top=8.dp),lineHeight=23.sp)
    }
    item { Text("Her privacy",fontSize=20.sp,fontWeight=FontWeight.Bold)
      Text("No account, adverts or analytics. Camera frames are processed on the device and discarded. Progress stays in the app. Optional spoken input uses the device's speech service, which may need a connection. Read-aloud is only used when someone taps it.",color=Muted,modifier=Modifier.padding(top=8.dp),lineHeight=23.sp)
    }
    item { Text("Real hands, credited sources",fontSize=20.sp,fontWeight=FontWeight.Bold)
      Text("Hand photographs: \"Data set about hand configuration of Pakistan Sign Language\" by Ali Imran Ali (MNS University of Agriculture Multan, 2021), Mendeley Data V1, doi:10.17632/y9svrbh27n.1, CC BY 4.0. Photos are used unmodified apart from resizing, and the on-device classifier was trained on hand landmarks from the same dataset. English letters use the CC0 ASL dataset and SignAlphaSet (Mendeley doi:10.17632/8fmvr9m98w.1, CC BY 4.0); PSL recognition training also uses the Hugging Face PSL gesture dataset (Bakhtyar12, MIT). No ASL Signbank videos are bundled.",color=Muted,modifier=Modifier.padding(top=8.dp),lineHeight=23.sp)
      TextButton(onClick={open("https://data.mendeley.com/datasets/y9svrbh27n/1")}) { Text("PSL photograph dataset ↗") }
      TextButton(onClick={open("https://creativecommons.org/licenses/by/4.0/")}) { Text("Dataset license ↗") }
      TextButton(onClick={open("https://doi.org/10.1016/j.dib.2021.107021")}) { Text("Dataset paper ↗") }
    }
    item { Text("Guided-practice references",fontSize=18.sp,fontWeight=FontWeight.Bold) }
    items(store.lessons.filter { it.media.isBlank() }.chunked(6)) { row -> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
      row.forEach { lesson -> TextButton(onClick={open("https://psl.org.pk/")}) { Text(lesson.title,fontSize=12.sp) } }
    } }
    item { OutlinedButton(onClick={export.launch("irssa-progress.json")},modifier=Modifier.fillMaxWidth()) { Text("Save a progress backup") } }
    if(notice!=null) item { Text(notice!!,color=Purple) }
    item { TextButton(onClick={reset=true},modifier=Modifier.fillMaxWidth()) { Text("Start a fresh learning journey") } }
    item { Text("Irssa 2.0 · Made for your family",color=Muted,fontSize=12.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center) }
  }
  if(reset) AlertDialog(onDismissRequest={reset=false},title={Text("Start again?")},text={Text("This clears stars, favorites and practice history on this device. Save a backup first if you want to keep them.")},
    confirmButton={TextButton(onClick={store.update { LearnerState(name=it.name,welcomed=true,haptics=it.haptics,showUrdu=it.showUrdu) };reset=false}) { Text("Clear learning progress") }},dismissButton={TextButton(onClick={reset=false}) { Text("Keep my progress") }})
}

@Composable private fun SettingToggle(title: String,description: String,value: Boolean,onChange: (Boolean)->Unit) {
  Surface(color=Color.White,shape=RoundedCornerShape(20.dp)) { Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) { Text(title,fontWeight=FontWeight.Bold);Text(description,color=Muted,fontSize=12.sp) };Switch(checked=value,onCheckedChange=onChange)
  } }
}
