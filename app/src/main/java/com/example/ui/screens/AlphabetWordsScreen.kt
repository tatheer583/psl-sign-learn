package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.LearningDataSource
import com.example.data.db.ProgressEntity
import com.example.data.model.LearningItem
import com.example.ui.components.HandPoseCanvas
import com.example.ui.components.VisualGestureCard
import com.example.util.HapticUtil

@Composable
fun AlphabetWordsScreen(
  progress: ProgressEntity,
  onPracticeInCamera: (LearningItem) -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  BackHandler { onBack() }

  var activeFilter by remember { mutableStateOf("ALL") }
  var inspectingItem by remember { mutableStateOf<LearningItem?>(null) }

  val letters = remember(activeFilter) {
    when (activeFilter) {
      "VOWELS" -> LearningDataSource.vowelItems
      "A_M" -> LearningDataSource.alphabetItems.filter { it.title[0] in 'A'..'M' }
      "N_Z" -> LearningDataSource.alphabetItems.filter { it.title[0] in 'N'..'Z' }
      else -> LearningDataSource.alphabetItems
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFD))
      .testTag("alphabet_words_screen")
  ) {
    // Header Bar
    Surface(
      color = Color.White,
      shadowElevation = 3.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = {
              HapticUtil.playTap(context)
              onBack()
            },
            modifier = Modifier.testTag("alphabet_words_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
              contentDescription = "Back",
              tint = Color(0xFF1E293B)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = "Letter 🤟 Sign 🍎 Word",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = Color(0xFF1E293B)
            )
            Text(
              text = "A is for Apple, B is for Bear...",
              fontSize = 12.sp,
              color = Color(0xFF64748B)
            )
          }
        }

        // Stars counter badge
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFFFFC312).copy(alpha = 0.20f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Rounded.Star,
              contentDescription = "Stars",
              tint = Color(0xFFD97706),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${progress.stars}",
              fontWeight = FontWeight.Black,
              fontSize = 14.sp,
              color = Color(0xFF92400E)
            )
          }
        }
      }
    }

    // Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val filterOptions = listOf(
        "ALL" to "All A–Z (26)",
        "VOWELS" to "Vowels (A,E,I,O,U)",
        "A_M" to "A – M",
        "N_Z" to "N – Z"
      )
      filterOptions.forEach { (key, label) ->
        val isSelected = activeFilter == key
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (isSelected) Color(0xFF5C52E5) else Color.White,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF5C52E5) else Color(0xFFCBD5E1)
          ),
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable {
              HapticUtil.playTap(context)
              activeFilter = key
            }
        ) {
          Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color(0xFF334155)
          )
        }
      }
    }

    // Grid of Letter + Sign + Word cards
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      contentPadding = PaddingValues(16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(letters, key = { it.id }) { item ->
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable {
              HapticUtil.playTap(context)
              inspectingItem = item
            }
            .border(
              width = 1.5.dp,
              color = Color(item.colorHex).copy(alpha = 0.25f),
              shape = RoundedCornerShape(22.dp)
            )
            .testTag("alphabet_word_card_${item.title}")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Header Row: Letter badge and Object Emoji
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(Color(item.colorHex)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = item.title,
                  fontWeight = FontWeight.Black,
                  fontSize = 20.sp,
                  color = Color.White
                )
              }

              Text(
                text = item.objectEmoji,
                fontSize = 28.sp
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PSL Hand Sign Silhouette
            Box(
              modifier = Modifier
                .size(86.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(item.colorHex).copy(alpha = 0.10f)),
              contentAlignment = Alignment.Center
            ) {
              HandPoseCanvas(
                pose = item.handPoseType,
                color = Color(item.colorHex),
                modifier = Modifier.size(72.dp),
                itemTitle = item.title
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Word display: e.g. "Apple"
            Text(
              text = item.wordLabel,
              fontWeight = FontWeight.Black,
              fontSize = 17.sp,
              color = Color(0xFF1E293B),
              textAlign = TextAlign.Center
            )

            Text(
              text = item.signName,
              fontSize = 11.sp,
              color = Color(0xFF64748B),
              textAlign = TextAlign.Center,
              maxLines = 1
            )
          }
        }
      }
    }
  }

  // Interactive Detail Dialog when child taps a card
  inspectingItem?.let { item ->
    Dialog(onDismissRequest = { inspectingItem = null }) {
      Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(Color(item.colorHex)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = item.title,
                  fontWeight = FontWeight.Black,
                  fontSize = 24.sp,
                  color = Color.White
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "${item.title} is for ${item.wordLabel}",
                  fontWeight = FontWeight.Black,
                  fontSize = 18.sp,
                  color = Color(0xFF1E293B)
                )
                Text(
                  text = "American Sign Language",
                  fontSize = 11.sp,
                  color = Color(0xFF64748B)
                )
              }
            }

            IconButton(onClick = { inspectingItem = null }) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close",
                tint = Color(0xFF64748B)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Big Gesture Card Canvas
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(20.dp))
              .background(
                Brush.verticalGradient(
                  listOf(
                    Color(item.colorHex).copy(alpha = 0.12f),
                    Color(item.colorHex).copy(alpha = 0.24f)
                  )
                )
              )
              .border(2.dp, Color(item.colorHex).copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
          ) {
            HandPoseCanvas(
              pose = item.handPoseType,
              color = Color(item.colorHex),
              modifier = Modifier.size(150.dp),
              itemTitle = item.title
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Visual cue tip
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(item.colorHex).copy(alpha = 0.10f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "✨ How to sign '${item.title}':",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(item.colorHex)
              )
              Spacer(modifier = Modifier.height(4.dp))
              item.visualCueSteps.take(2).forEach { step ->
                Text(
                  text = "• $step",
                  fontSize = 12.sp,
                  color = Color(0xFF334155),
                  lineHeight = 16.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Practice in Camera Button
          Button(
            onClick = {
              HapticUtil.playTap(context)
              val target = inspectingItem
              inspectingItem = null
              if (target != null) {
                onPracticeInCamera(target)
              }
            },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(item.colorHex)),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("practice_in_camera_button")
          ) {
            Icon(imageVector = Icons.Rounded.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Practice '${item.title}' in Camera",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
