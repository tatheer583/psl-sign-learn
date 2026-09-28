package com.example.irssa

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.util.HapticUtil

val Ink=Color(0xFF23352F)
val Muted=Color(0xFF65716B)
val Purple=Color(0xFF6653C6)
val Cream=Color(0xFFFAF9F5)
val Mint=Color(0xFFE7F2E9)
val Peach=Color(0xFFFFEBDD)
val Lavender=Color(0xFFEFEBFC)

@Composable
fun IrssaExperience(store: LearningStore=viewModel()) {
  val state by store.state.collectAsStateWithLifecycle()
  val ready by store.ready.collectAsStateWithLifecycle()
  val error by store.error.collectAsStateWithLifecycle()
  var route by rememberSaveable { mutableStateOf("home") }
  var selectedId by rememberSaveable { mutableStateOf("letter_alif") }
  var category by rememberSaveable { mutableStateOf("Letters") }
  var gate by remember { mutableStateOf(false) }
  val context=LocalContext.current
  val colors=lightColorScheme(primary=Purple,onPrimary=Color.White,secondary=Color(0xFF347557),
    background=Cream,surface=Color.White,onSurface=Ink,onBackground=Ink,surfaceVariant=Lavender)
  MaterialTheme(colorScheme=colors,shapes=Shapes(small=RoundedCornerShape(14.dp),medium=RoundedCornerShape(22.dp),large=RoundedCornerShape(28.dp))) {
    if(!ready) {
      Surface(Modifier.fillMaxSize(),color=Cream) {
        Column(Modifier.fillMaxSize().padding(28.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) {
          Text("irssa",fontSize=44.sp,fontWeight=FontWeight.ExtraBold,color=Purple)
          Spacer(Modifier.height(16.dp))
          if(error==null) CircularProgressIndicator() else { Text(error!!);Button(onClick={store.load()}) { Text("Try again") } }
        }
      };return@MaterialTheme
    }
    if(!state.welcomed) { Welcome(state.name) { name -> store.update { it.copy(name=name,welcomed=true) } };return@MaterialTheme }
    val lesson=store.lessons.firstOrNull { it.id==selectedId } ?: store.lessons.first()
    val rootTabs=listOf("home","library","play","talk","stars")
    BackHandler(route!="home") { route=when(route) { "lesson" -> "library";"practice" -> "lesson";"quiz","speed","cameraquiz","converter","spell" -> "play";else -> "home" } }
    fun openLesson(item: Lesson) { selectedId=item.id;route="lesson" }
    fun tap() { if(state.haptics) HapticUtil.playTap(context) }
    Scaffold(containerColor=Cream,topBar={
      Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=20.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
        if(route !in rootTabs) IconButton(onClick={route=if(route=="practice") "lesson" else "home"},modifier=Modifier.testTag("back")) {
          Icon(Icons.AutoMirrored.Rounded.ArrowBack,"Back")
        }
        Column(Modifier.weight(1f)) {
          Text("irssa",fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=Ink,letterSpacing=(-1).sp)
          Text("A LITTLE SIGN. A BIG WORLD.",fontSize=8.sp,fontWeight=FontWeight.Bold,letterSpacing=1.2.sp,color=Muted)
        }
        Surface(color=Color(0xFFFFF0BC),shape=RoundedCornerShape(20.dp)) {
          Text("★  ${state.stars}",Modifier.padding(horizontal=13.dp,vertical=8.dp),fontWeight=FontWeight.Bold,color=Color(0xFF725018))
        }
        IconButton(onClick={gate=true},modifier=Modifier.testTag("grownups")) { Icon(Icons.Rounded.Settings,"Grown-up settings",tint=Muted) }
      }
    },bottomBar={ if(route in rootTabs) NavigationBar(containerColor=Color.White,tonalElevation=0.dp) {
      val tabs=listOf(Triple("home","Home",Icons.Rounded.Home),Triple("library","Learn",Icons.AutoMirrored.Rounded.MenuBook),
        Triple("play","Play",Icons.Rounded.SportsEsports),Triple("talk","Talk",Icons.AutoMirrored.Rounded.Chat),Triple("stars","Stars",Icons.Rounded.Star))
      tabs.forEach { (id,label,icon) -> NavigationBarItem(selected=route==id,onClick={tap();route=id},
        icon={Icon(icon,label)},label={Text(label,fontSize=11.sp)},modifier=Modifier.testTag("tab_$id"),
        colors=NavigationBarItemDefaults.colors(indicatorColor=Lavender,selectedIconColor=Purple,selectedTextColor=Purple)) }
    } }) { padding ->
      Column(Modifier.fillMaxSize().padding(padding)) {
        if(error!=null) Surface(color=Peach) { Text(error!!,Modifier.padding(12.dp)) }
        Box(Modifier.weight(1f)) {
          when(route) {
            "home" -> HomeJourney(state,store.lessons,onLesson=::openLesson,onLibrary={category=it;route="library"},onPlay={route="play"},onTalk={route="talk"})
            "library" -> LessonLibrary(store,state,category,{category=it},::openLesson)
            "lesson" -> LessonDetail(lesson,state,onFavorite={store.favorite(lesson.id)},onPractice={route="practice"},
              onDone={store.reward(lesson.id,"learn")},onNext={ val index=store.lessons.indexOf(lesson);openLesson(store.lessons[(index+1)%store.lessons.size]) })
            "practice" -> CameraGame(store,state,lesson,challenge=false,converter=false)
            "cameraquiz" -> CameraGame(store,state,lesson,challenge=true,converter=false)
            "converter" -> CameraGame(store,state,lesson,challenge=false,converter=true)
            "play" -> PlayRoom { route=it }
            "quiz" -> MatchGame(store,state,false)
            "speed" -> MatchGame(store,state,true)
            "spell" -> WordStudio(store.lessons,::openLesson)
            "talk" -> TalkBoard(state)
            "stars" -> StarGarden(state)
            "settings" -> FamilySettings(store,state)
          }
        }
      }
    }
    if(gate) ParentGate(onDismiss={gate=false}) { gate=false;route="settings" }
  }
}

@Composable
fun Welcome(initial: String,onStart: (String)->Unit) {
  var name by rememberSaveable { mutableStateOf(initial) }
  Column(Modifier.fillMaxSize().background(Cream).safeDrawingPadding().padding(28.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) {
    Surface(color=Lavender,shape=CircleShape,modifier=Modifier.size(116.dp)) { Box(contentAlignment=Alignment.Center) { Text("🌱",fontSize=62.sp) } }
    Spacer(Modifier.height(28.dp))
    Text("Your world,\none sign at a time.",fontSize=34.sp,lineHeight=40.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center,color=Ink)
    Text("Watch real hands. Play little games.\nLearn at your own pace.",Modifier.padding(vertical=20.dp),textAlign=TextAlign.Center,color=Muted)
    OutlinedTextField(value=name,onValueChange={name=it.take(24)},label={Text("Your name")},singleLine=true,shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().testTag("learner_name"))
    Spacer(Modifier.height(16.dp))
    Button(onClick={onStart(name.trim().ifEmpty { "Irssa" })},modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("start_learning")) { Text("Let's begin  →",fontWeight=FontWeight.Bold) }
    Text("American Sign Language • Works offline",Modifier.padding(top=16.dp),fontSize=12.sp,color=Muted)
    Text("Grown-ups: choose the sign language used by her school and community. Pakistan Sign Language resources are in Settings.",Modifier.padding(top=14.dp),fontSize=12.sp,textAlign=TextAlign.Center,color=Muted)
  }
}

@Composable
fun PageTitle(eyebrow: String,title: String,description: String="") {
  Column { Text(eyebrow.uppercase(),fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp,color=Purple)
    Text(title,fontSize=28.sp,lineHeight=34.sp,fontWeight=FontWeight.ExtraBold,color=Ink,modifier=Modifier.padding(top=5.dp))
    if(description.isNotEmpty()) Text(description,color=Muted,modifier=Modifier.padding(top=7.dp),lineHeight=21.sp) }
}

@Composable
fun HomeJourney(state: LearnerState,lessons: List<Lesson>,onLesson: (Lesson)->Unit,onLibrary: (String)->Unit,onPlay: ()->Unit,onTalk: ()->Unit) {
  val next=lessons.firstOrNull { it.id !in state.seen } ?: lessons.first()
  LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(22.dp),modifier=Modifier.testTag("home_screen")) {
    item { Row(verticalAlignment=Alignment.CenterVertically) {
      Column(Modifier.weight(1f)) { Text("Hello, ${state.name} ☀",fontSize=27.sp,fontWeight=FontWeight.ExtraBold,color=Ink);Text("What will we discover today?",color=Muted) }
      Surface(color=Mint,shape=CircleShape,modifier=Modifier.size(46.dp)) { Box(contentAlignment=Alignment.Center) { Text("🌱",fontSize=26.sp) } }
    } }
    item { Card(colors=CardDefaults.cardColors(containerColor=Purple),shape=RoundedCornerShape(28.dp)) {
      Row(Modifier.fillMaxWidth().padding(22.dp),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
          Text("YOUR NEXT LITTLE ADVENTURE",color=Color(0xFFDED7FF),fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp)
          Text("Let's learn\n${if(next.category=="Letters") "the letter " else ""}${next.title}",color=Color.White,fontSize=30.sp,lineHeight=35.sp,fontWeight=FontWeight.ExtraBold,modifier=Modifier.padding(vertical=12.dp))
          Button(onClick={onLesson(next)},colors=ButtonDefaults.buttonColors(containerColor=Color.White,contentColor=Purple)) { Text("Watch & try  →",fontWeight=FontWeight.Bold) }
        }
        Box(Modifier.size(100.dp).clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha=.12f)),contentAlignment=Alignment.Center) {
          Text(next.emoji,fontSize=56.sp)
        }
      }
    } }
    item { Surface(color=Color.White,shape=RoundedCornerShape(22.dp)) {
      Column(Modifier.padding(18.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
          Text("A little every day",fontWeight=FontWeight.Bold);Text("${state.todayCount().coerceAtMost(state.dailyGoal)} / ${state.dailyGoal} signs",color=Purple,fontWeight=FontWeight.Bold)
        }
        LinearProgressIndicator(progress={ (state.todayCount().toFloat()/state.dailyGoal).coerceIn(0f,1f) },modifier=Modifier.fillMaxWidth().padding(vertical=12.dp).height(8.dp).clip(CircleShape),color=Color(0xFF71A980),trackColor=Mint)
        Text(if(state.todayCount()>=state.dailyGoal) "You reached your goal. Time for a happy break!" else "No rush. Every little practice counts.",fontSize=12.sp,color=Muted)
      }
    } }
    item { Text("Pick a world",fontSize=20.sp,fontWeight=FontWeight.Bold,color=Ink) }
    item { Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
      WorldCard("Aa","Letters","26 discoveries",Lavender,Modifier.weight(1f)) { onLibrary("Letters") }
      WorldCard("123","Numbers","Count to 50",Peach,Modifier.weight(1f)) { onLibrary("Numbers") }
    } }
    item { Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
      WorldCard("👋","Everyday signs","Hello, friend!",Mint,Modifier.weight(1f)) { onLibrary("Words") }
      WorldCard("🎲","Time to play","Match & explore",Color(0xFFFFF2C9),Modifier.weight(1f),onPlay)
    } }
    item { Surface(onClick=onTalk,color=Color.White,shape=RoundedCornerShape(22.dp)) {
      Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
        Text("💬",fontSize=28.sp);Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)) {
          Text("Something to say?",fontWeight=FontWeight.Bold);Text("Your picture board is always here.",color=Muted,fontSize=12.sp)
        };Text("→",color=Purple,fontSize=24.sp)
      }
    } }
    item { Text("Made for curious hands and growing minds.\nPSL lessons · ${state.seen.size} signs explored",fontSize=11.sp,color=Muted,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth()) }
  }
}

@Composable
fun WorldCard(symbol: String,title: String,subtitle: String,color: Color,modifier: Modifier=Modifier,onClick: ()->Unit) {
  Surface(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(24.dp),color=color) {
    Column(Modifier.padding(18.dp)) { Text(symbol,fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=Ink)
      Text(title,fontWeight=FontWeight.Bold,fontSize=16.sp,modifier=Modifier.padding(top=14.dp));Text(subtitle,color=Muted,fontSize=11.sp,modifier=Modifier.padding(top=3.dp)) }
  }
}

@Composable
fun LessonLibrary(store: LearningStore,state: LearnerState,category: String,onCategory: (String)->Unit,onLesson: (Lesson)->Unit) {
  var search by rememberSaveable { mutableStateOf("") }
  val filtered=store.lessons.filter { (category=="Favorites" && it.id in state.favorites || it.category==category) &&
    (search.isBlank() || it.title.contains(search,true) || it.word.contains(search,true)) }
  LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp),modifier=Modifier.testTag("library_screen")) {
    item { PageTitle("The discovery shelf","Small signs. Big ideas.","Watch, try, and come back to your favorites.") }
    item { OutlinedTextField(value=search,onValueChange={search=it},placeholder={Text("Find a letter, number or word")},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),leadingIcon={Icon(Icons.Rounded.Search,null)}) }
    item { LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) { items(listOf("Letters","Numbers","Words","Favorites")) { c -> FilterChip(selected=category==c,onClick={onCategory(c)},label={Text(c)},modifier=Modifier.heightIn(min=48.dp)) } } }
    if(filtered.isEmpty()) item { Text(if(category=="Favorites") "Tap a heart on a lesson to keep it here." else "Try a different word.",Modifier.padding(20.dp),color=Muted) }
    items(filtered.chunked(3)) { row -> Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
      row.forEach { lesson -> Surface(onClick={onLesson(lesson)},modifier=Modifier.weight(1f).testTag("lesson_${lesson.id}"),shape=RoundedCornerShape(20.dp),color=if(lesson.id in state.seen) Mint else Color.White) {
        Column(Modifier.padding(12.dp).heightIn(min=100.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
          Text(if(lesson.category=="Words") lesson.emoji else lesson.title,fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=Ink)
          Text(if(lesson.category=="Letters") lesson.word else if(lesson.category=="Words") lesson.title else "Number",fontSize=11.sp,textAlign=TextAlign.Center,color=Muted)
          if(lesson.id in state.seen) Text("✓ Explored",fontSize=9.sp,color=Color(0xFF347557))
        }
      } }
      repeat(3-row.size) { Spacer(Modifier.weight(1f)) }
    } }
  }
}

@Composable
fun LessonDetail(lesson: Lesson,state: LearnerState,onFavorite: ()->Unit,onPractice: ()->Unit,onDone: ()->Unit,onNext: ()->Unit) {
  val context=LocalContext.current
  var celebrated by remember(lesson.id) { mutableStateOf(false) }
  LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp),modifier=Modifier.testTag("lesson_detail")) {
    item { Row(verticalAlignment=Alignment.CenterVertically) {
      Column(Modifier.weight(1f)) { PageTitle("${lesson.category} · American Sign Language",if(lesson.category=="Letters") "${lesson.title} is for ${lesson.word}" else lesson.title) }
      IconButton(onClick=onFavorite) { Icon(if(lesson.id in state.favorites) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,"Favorite this sign",tint=Purple) }
    } }
    item { SignMedia(lesson,Modifier.fillMaxWidth().height(if(lesson.isVideo) 320.dp else 290.dp)) }
    item { Text(if(lesson.media.isBlank()) "Guided practice · learn with a PSL teacher (psl.org.pk)" else "Ali Imran Ali · PSL Dataset, CC BY 4.0 · Real hand photograph",fontSize=10.sp,color=Muted) }
    if(lesson.title in listOf("J","Z") && lesson.category=="Letters") item { Surface(color=Peach,shape=RoundedCornerShape(18.dp)) {
      Text("${lesson.title} needs movement. This photo is a handshape reference. Follow the motion with a grown-up; the camera does not grade this letter.",Modifier.padding(16.dp),color=Ink)
    } }
    item { Text("Watch. Notice. Try.",fontSize=20.sp,fontWeight=FontWeight.Bold) }
    items(lesson.steps.withIndex().toList()) { (index,step) -> Row(verticalAlignment=Alignment.Top,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
      Surface(color=Lavender,shape=CircleShape,modifier=Modifier.size(28.dp)) { Box(contentAlignment=Alignment.Center) { Text("${index+1}",color=Purple,fontWeight=FontWeight.Bold) } }
      Text(step,Modifier.weight(1f),color=Ink,lineHeight=23.sp)
    } }
    item { Button(onClick=onPractice,modifier=Modifier.fillMaxWidth().heightIn(min=54.dp).testTag("practice_sign")) { Text(if(lesson.isStatic) "📷  Try with the camera" else "Practice together",fontWeight=FontWeight.Bold) } }
    item { OutlinedButton(onClick={onDone();celebrated=true;if(state.haptics) HapticUtil.playSuccess(context)},modifier=Modifier.fillMaxWidth().heightIn(min=52.dp).testTag("lesson_done")) {
      Text(if(celebrated) "✓  Practice saved!" else "I've watched and practised")
    } }
    if(celebrated) item { Surface(color=Mint,shape=RoundedCornerShape(18.dp)) {
      Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically) { Text("🌱",fontSize=32.sp);Text("A little more learning.\nYou are growing!",Modifier.weight(1f).padding(start=12.dp));TextButton(onClick=onNext) { Text("Next →") } }
    } }
  }
}
