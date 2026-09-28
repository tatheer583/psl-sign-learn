package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ProgressEntity
import com.example.data.model.StickerBadge
import com.example.util.HapticUtil

@Composable
fun StickersScreen(
  badges: List<StickerBadge>,
  progress: ProgressEntity,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  BackHandler { onBack() }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFD))
      .padding(horizontal = 20.dp, vertical = 20.dp)
      .testTag("stickers_screen")
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
            .testTag("stickers_back_btn")
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
            text = "${progress.childName}'s Trophy Book",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1E293B)
          )
          Text(
            text = "Shiny badges & stickers",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B)
          )
        }
      }

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

    Spacer(modifier = Modifier.height(16.dp))

    // Motivational Banner
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF5C52E5).copy(alpha = 0.10f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "👑", fontSize = 28.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Collect Stars to Unlock More Badges!",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5C52E5),
            fontSize = 13.sp
          )
          Text(
            text = "Every vowel, number, and camera sign earns you shiny rewards.",
            color = Color(0xFF64748B),
            fontSize = 11.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Grid of Badges
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(bottom = 24.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(badges) { badge ->
        StickerItemCard(badge = badge, currentStars = progress.stars)
      }
    }
  }
}

@Composable
fun StickerItemCard(
  badge: StickerBadge,
  currentStars: Int
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .height(175.dp),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (badge.isUnlocked) Color.White else Color(0xFFF1F5F9)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (badge.isUnlocked) 4.dp else 0.dp),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.5.dp,
      color = if (badge.isUnlocked) Color(0xFFFFC312) else Color(0xFFE2E8F0)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Badge Emoji / Lock Icon
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(
            if (badge.isUnlocked) Color(0xFFFFF9E6) else Color(0xFFE2E8F0)
          ),
        contentAlignment = Alignment.Center
      ) {
        if (badge.isUnlocked) {
          Text(text = badge.iconEmoji, fontSize = 28.sp)
        } else {
          Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = "Locked",
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = badge.title,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = if (badge.isUnlocked) Color(0xFF1E293B) else Color(0xFF64748B),
          textAlign = TextAlign.Center
        )
        Text(
          text = badge.description,
          fontSize = 10.sp,
          color = Color(0xFF94A3B8),
          textAlign = TextAlign.Center,
          maxLines = 2,
          lineHeight = 13.sp
        )
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (badge.isUnlocked) Color(0xFF00C853).copy(alpha = 0.15f) else Color(0xFFE2E8F0)
      ) {
        Text(
          text = if (badge.isUnlocked) "UNLOCKED ✨" else "Needs ${badge.requiredStars} ⭐",
          color = if (badge.isUnlocked) Color(0xFF008537) else Color(0xFF64748B),
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
      }
    }
  }
}
