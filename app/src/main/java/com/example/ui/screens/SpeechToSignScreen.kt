package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LearningDataSource
import com.example.data.model.LearningItem
import com.example.ui.components.VisualGestureCard
import com.example.util.HapticUtil
import kotlinx.coroutines.delay

@Composable
fun SpeechToSignScreen(
  onPracticeItem: (LearningItem) -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  BackHandler { onBack() }

  var textInput by remember { mutableStateOf("IRSSA") }
  var isPlayingSequence by remember { mutableStateOf(false) }
  var activeCharIndex by remember { mutableIntStateOf(0) }

  // Quick word chips for child/family
  val quickPresets = listOf("IRSSA", "HELLO", "WATER", "BOOK", "LOVE", "CAT", "DOG", "STAR")

  // Map input characters to LearningItems (letters and digits)
  val matchedItems: List<LearningItem> = remember(textInput) {
    val clean = textInput.uppercase().trim()
    clean.mapNotNull { ch ->
      when {
        ch in 'A'..'Z' -> LearningDataSource.alphabetItems.firstOrNull { it.title.equals(ch.toString(), ignoreCase = true) }
        ch in '0'..'9' -> LearningDataSource.numberItems.firstOrNull { it.title.equals(ch.toString(), ignoreCase = true) }
        else -> null
      }
    }
  }

  // Automatic Sequence Player (plays letter signs in rhythmic timing)
  LaunchedEffect(isPlayingSequence, activeCharIndex) {
    if (isPlayingSequence && matchedItems.isNotEmpty()) {
      HapticUtil.playTap(context)
      delay(1400)
      if (activeCharIndex < matchedItems.size - 1) {
        activeCharIndex++
      } else {
        isPlayingSequence = false
        activeCharIndex = 0
        HapticUtil.playSuccess(context)
      }
    }
  }

  val currentItem = matchedItems.getOrNull(activeCharIndex) ?: matchedItems.firstOrNull()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFD))
      .padding(horizontal = 20.dp, vertical = 20.dp)
      .testTag("speech_to_sign_screen"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Bar
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
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = Color(0xFF1E293B)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Word to Sign Translator",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1E293B)
          )
          Text(
            text = "Type any word to see signs in PSL!",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B)
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF5C52E5).copy(alpha = 0.15f)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Rounded.Translate,
            contentDescription = "Translate",
            tint = Color(0xFF5C52E5),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Visual PSL",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF5C52E5)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Text Input field
    OutlinedTextField(
      value = textInput,
      onValueChange = {
        textInput = it.take(15)
        activeCharIndex = 0
        isPlayingSequence = false
      },
      placeholder = { Text("Type word or name...") },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("translator_input_field"),
      shape = RoundedCornerShape(18.dp),
      colors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedIndicatorColor = Color(0xFF5C52E5),
        unfocusedIndicatorColor = Color(0xFFCBD5E1)
      ),
      trailingIcon = {
        if (textInput.isNotEmpty()) {
          IconButton(onClick = { textInput = "" }) {
            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
          }
        }
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Quick Preset Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(quickPresets) { word ->
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable {
              HapticUtil.playTap(context)
              textInput = word
              activeCharIndex = 0
              isPlayingSequence = false
            },
          shape = RoundedCornerShape(14.dp),
          color = if (textInput.equals(word, ignoreCase = true)) Color(0xFF5C52E5) else Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
          Text(
            text = word,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (textInput.equals(word, ignoreCase = true)) Color.White else Color(0xFF334155)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Letter Selector Tabs (Spelling the word)
    if (matchedItems.isNotEmpty()) {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(matchedItems.indices.toList()) { idx ->
          val item = matchedItems[idx]
          val isSelected = idx == activeCharIndex

          Surface(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .clickable {
                HapticUtil.playTap(context)
                activeCharIndex = idx
                isPlayingSequence = false
              },
            shape = CircleShape,
            color = if (isSelected) Color(item.colorHex) else Color.White,
            border = androidx.compose.foundation.BorderStroke(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) Color(item.colorHex) else Color(0xFFCBD5E1)
            ),
            shadowElevation = if (isSelected) 4.dp else 0.dp
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = item.title,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = if (isSelected) Color.White else Color(0xFF1E293B)
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Display Active Sign Card
    if (currentItem != null) {
      VisualGestureCard(
        item = currentItem,
        modifier = Modifier.weight(1f),
        onPracticeClick = {
          onPracticeItem(currentItem)
        }
      )
    } else {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
      ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            text = "Type any letters above to see the signs!",
            color = Color(0xFF64748B),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Sequence Play / Pause Button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = {
          HapticUtil.playTap(context)
          isPlayingSequence = !isPlayingSequence
        },
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isPlayingSequence) Color(0xFFFF5E7E) else Color(0xFF5C52E5)
        ),
        modifier = Modifier
          .weight(1f)
          .height(52.dp)
      ) {
        Icon(
          imageVector = Icons.Rounded.Speed,
          contentDescription = "Play"
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isPlayingSequence) "Pause Spelling" else "Spell Word in Sign",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      }
    }
  }
}
