package com.example.irssa

import android.app.Application
import android.util.AtomicFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LearningDataSource
import com.example.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

data class Lesson(val id: String, val title: String, val word: String, val emoji: String,
  val category: String, val media: String, val isVideo: Boolean, val source: String,
  val author: String, val license: String, val steps: List<String>) {
  val isStatic: Boolean get() = category == "Letters" && id in recognizedLetters
  val recognitionMode: String get() = "alphabet"
  val classifierLabel: String get() = id.removePrefix("letter_")
  companion object {
    // Handshape classes with enough real PSL training examples for the on-device
    // classifier (see docs/recognition-evaluation.json). غ, م and ق stay guided
    // practice because the detector cannot see them reliably.
    val recognizedLetters: Set<String> = setOf(
      "letter_ain","letter_alif","letter_bari_yeh","letter_be","letter_che","letter_chhoti_yeh",
      "letter_dal","letter_dhal","letter_fay","letter_gaf","letter_hamza","letter_hay","letter_he",
      "letter_jim","letter_kaf","letter_khe","letter_laam","letter_noon","letter_pe","letter_re",
      "letter_rre","letter_se","letter_seen","letter_sheen","letter_swad","letter_te","letter_toay",
      "letter_tte","letter_vao","letter_zal","letter_ze","letter_zhe","letter_zoay","letter_zwad")
  }
}

object LessonCatalog {
  fun load(app: Application): List<Lesson> {
    val manifest=JSONArray(app.assets.open("media_manifest.json").bufferedReader().use { it.readText() })
    val assets=(0 until manifest.length()).associate { i -> val a=manifest.getJSONObject(i);a.getString("id") to a }
    return LearningDataSource.allItems.map { item ->
      val a=requireNotNull(assets[item.id]) { "Missing teaching asset: ${item.id}" }
      val category=when { item.id.startsWith("letter_") -> "Letters";item.id.startsWith("num_") -> "Numbers";else -> "Words" }
      val file=a.optString("file")
      Lesson(item.id,item.title,item.wordLabel,item.objectEmoji,category,file,file.isNotBlank()&&a.optString("type")=="video",
        a.getString("source"),a.getString("author"),a.getString("license"),
        item.visualCueSteps)    }
  }
}

data class LearnerState(val name: String="Irssa",val welcomed: Boolean=false,val stars: Int=0,
  val seen: Set<String> = emptySet(),val mastered: Set<String> = emptySet(),val favorites: Set<String> = emptySet(),
  val rewards: Set<String> = emptySet(),val practiceDays: Set<String> = emptySet(),
  val haptics: Boolean=true,val reducedMotion: Boolean=false,val showUrdu: Boolean=true,val dailyGoal: Int=5) {
  fun todayCount(today: String=LocalDate.now().toString()): Int = rewards.count { it.startsWith("$today|learn|") }
  fun streak(today: LocalDate=LocalDate.now()): Int {
    var day=if(today.toString() in practiceDays) today else today.minusDays(1)
    var count=0
    while(day.toString() in practiceDays) { count++;day=day.minusDays(1) }
    return count
  }
}

object ProgressRules {
  fun reward(s: LearnerState,id: String,kind: String,today: String): LearnerState {
    require(kind in setOf("learn","quiz","camera"))
    val key="$today|$kind|$id"
    val points=when(kind) { "camera" -> 10;"quiz" -> 5;else -> 2 }
    return s.copy(stars=s.stars+if(key in s.rewards) 0 else points,seen=s.seen+id,
      mastered=if(kind=="camera") s.mastered+id else s.mastered,rewards=s.rewards+key,practiceDays=s.practiceDays+today)
  }
}

class LearningStore(application: Application): AndroidViewModel(application) {
  private val _state=MutableStateFlow(LearnerState());val state=_state.asStateFlow()
  private val _ready=MutableStateFlow(false);val ready=_ready.asStateFlow()
  private val _error=MutableStateFlow<String?>(null);val error=_error.asStateFlow()
  var lessons: List<Lesson> = emptyList();private set
  private val lock=Mutex()
  private val storage=AtomicFile(File(application.filesDir,"learning-v2.json"))
  init { load() }
  fun load() { viewModelScope.launch(Dispatchers.IO) { lock.withLock {
    try {
      lessons=LessonCatalog.load(getApplication())
      val initial=if(storage.baseFile.exists()) decode(String(storage.readFully(),Charsets.UTF_8)) else {
        val old=AppDatabase.getDatabase(getApplication()).progressDao().getProgress()
        LearnerState(name=old?.childName ?: "Irssa",welcomed=old?.hasCompletedOnboarding ?: false,stars=old?.stars ?: 0,
          seen=old?.completedItemsCsv?.split(',')?.filter { it.isNotBlank() }?.toSet() ?: emptySet())
      }
      _state.value=initial;_ready.value=true;_error.value=null
    } catch(e: Exception) { _error.value="Your learning could not be opened. Please try again. Your saved progress has been kept." }
  } } }
  fun update(change: (LearnerState)->LearnerState) {
    if(!_ready.value) return
    viewModelScope.launch(Dispatchers.IO) { lock.withLock {
      val updated=change(_state.value)
      var stream: java.io.FileOutputStream?=null
      try {
        stream=storage.startWrite();stream.write(encode(updated).toByteArray(Charsets.UTF_8));storage.finishWrite(stream)
        _state.value=updated;_error.value=null
      } catch(e: Exception) { storage.failWrite(stream);_error.value="Progress could not be saved. Free some device space and try again." }
    } }
  }
  fun reward(id: String,kind: String)=update { ProgressRules.reward(it,id,kind,LocalDate.now().toString()) }
  fun favorite(id: String)=update { it.copy(favorites=if(id in it.favorites) it.favorites-id else it.favorites+id) }
  companion object {
    fun encode(s: LearnerState): String=JSONObject().apply {
      put("version",2);put("name",s.name);put("welcomed",s.welcomed);put("stars",s.stars)
      put("seen",JSONArray(s.seen.toList()));put("mastered",JSONArray(s.mastered.toList()));put("favorites",JSONArray(s.favorites.toList()))
      put("rewards",JSONArray(s.rewards.toList()));put("practiceDays",JSONArray(s.practiceDays.toList()))
      put("haptics",s.haptics);put("reducedMotion",s.reducedMotion);put("showUrdu",s.showUrdu);put("dailyGoal",s.dailyGoal)
    }.toString()
    fun decode(text: String): LearnerState {
      val o=JSONObject(text);require(o.getInt("version")==2)
      fun set(k: String): Set<String> { val a=o.optJSONArray(k) ?: return emptySet();return (0 until a.length()).map { a.getString(it) }.toSet() }
      return LearnerState(o.getString("name"),o.getBoolean("welcomed"),o.getInt("stars"),set("seen"),set("mastered"),set("favorites"),
        set("rewards"),set("practiceDays"),o.optBoolean("haptics",true),o.optBoolean("reducedMotion",false),o.optBoolean("showUrdu",true),o.optInt("dailyGoal",5).coerceIn(3,10))
    }
  }
}
