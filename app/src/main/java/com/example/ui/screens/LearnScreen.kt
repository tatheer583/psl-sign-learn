package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LearningDataSource
import com.example.data.db.ProgressEntity
import com.example.data.model.GameLevel
import com.example.data.model.LearningItem
import com.example.ui.components.VisualGestureCard
import com.example.util.HapticUtil

@Composable
fun LearnScreen(
  selectedItem: LearningItem,
  selectedLevel: GameLevel,
  progress: ProgressEntity,
  onSelectItem: (LearningItem) -> Unit,
  onOpenPractice: () -> Unit,
  onBack: () -> Unit,
  customItemList: List<LearningItem>? = null,
  customTitle: String? = null
) {
  val context = LocalContext.current
  BackHandler { onBack() }

  val levelItems = customItemList ?: LearningDataSource.allItems.filter { it.id in selectedLevel.itemIds }
  val currentIdx = levelItems.indexOfFirst { it.id == selectedItem.id }.coerceAtLeast(0)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFD))
      .testTag("learn_screen"),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
  ) {
    // Top Bar
    item {
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
              .testTag("learn_back_button")
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
              text = customTitle ?: selectedLevel.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = Color(0xFF1E293B)
            )
            Text(
              text = "Sign & Finger Guidance",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF64748B)
            )
          }
        }

        // Stars Pill
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = Color(0xFFFFC312),
          shadowElevation = 2.dp
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Rounded.Star,
              contentDescription = "Stars",
              tint = Color(0xFF6B4700),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${progress.stars}",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color(0xFF422C00)
            )
          }
        }
      }
    }

    // Horizontal Pill Selector for Items in this Level
    item {
      Spacer(modifier = Modifier.height(16.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
      ) {
        items(levelItems) { item ->
          val isSelected = item.id == selectedItem.id
          val isCompleted = progress.completedItemsCsv.split(",").contains(item.id)

          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .clickable {
                HapticUtil.playTap(context)
                onSelectItem(item)
              }
              .testTag("item_selector_${item.id}"),
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) Color(item.colorHex) else Color.White,
            border = androidx.compose.foundation.BorderStroke(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) Color(item.colorHex) else Color(0xFFE2E8F0)
            ),
            shadowElevation = if (isSelected) 3.dp else 0.dp
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isSelected) Color.White else Color(0xFF334155)
              )
              if (isCompleted) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "⭐", fontSize = 11.sp)
              }
            }
          }
        }
      }
    }

    // Main Interactive Visual Gesture Card
    item {
      Spacer(modifier = Modifier.height(16.dp))
      VisualGestureCard(
        item = selectedItem,
        onPracticeClick = {
          onOpenPractice()
        }
      )
    }

    // Bottom Navigation (Previous / Next / Camera)
    item {
      Spacer(modifier = Modifier.height(20.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = {
            if (currentIdx > 0) {
              HapticUtil.playTap(context)
              onSelectItem(levelItems[currentIdx - 1])
            }
          },
          enabled = currentIdx > 0,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Previous Sign"
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Prev")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
          onClick = {
            HapticUtil.playTap(context)
            onOpenPractice()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894)),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .weight(1.3f)
            .testTag("learn_camera_btn")
        ) {
          Icon(
            imageVector = Icons.Rounded.CameraAlt,
            contentDescription = "Camera"
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Camera")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
          onClick = {
            if (currentIdx < levelItems.size - 1) {
              HapticUtil.playTap(context)
              onSelectItem(levelItems[currentIdx + 1])
            }
          },
          enabled = currentIdx < levelItems.size - 1,
          colors = ButtonDefaults.buttonColors(containerColor = Color(selectedItem.colorHex)),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.weight(1f)
        ) {
          Text("Next")
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = "Next Sign"
          )
        }
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
