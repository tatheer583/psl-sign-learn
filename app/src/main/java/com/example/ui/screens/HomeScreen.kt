package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LearningDataSource
import com.example.data.db.ProgressEntity
import com.example.data.model.ActiveScreen
import com.example.data.model.GameLevel
import com.example.data.model.LearningItem
import com.example.data.model.SignCategory
import com.example.util.HapticUtil

@Composable
fun HomeScreen(
  progress: ProgressEntity,
  onNavigate: (ActiveScreen) -> Unit,
  onSelectItem: (LearningItem) -> Unit,
  onSelectLevel: (GameLevel) -> Unit
) {
  val context = LocalContext.current
  var activeCategoryTab by remember { mutableStateOf("ALL") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFD))
      .testTag("home_screen_list"),
    contentPadding = PaddingValues(bottom = 32.dp)
  ) {
    // Top Hero App Bar
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
          .background(
            Brush.verticalGradient(
              listOf(
                Color(0xFF5C52E5),
                Color(0xFF4338CA)
              )
            )
          )
          .padding(horizontal = 20.dp, vertical = 24.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .clip(CircleShape)
                  .border(2.dp, Color.White, CircleShape)
              ) {
                Image(
                  painter = painterResource(id = R.drawable.img_mascot_avatar),
                  contentDescription = "Irssa Profile Mascot",
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Hi, ${progress.childName}! 👋",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Black,
                  color = Color.White
                )
                Text(
                  text = "Learn A-Z Letters & 1-50 Numbers!",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.85f)
                )
              }
            }

            // Star Counter Badge
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color(0xFFFFC312),
              shadowElevation = 4.dp
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Rounded.Star,
                  contentDescription = "Stars",
                  tint = Color(0xFF6B4700),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${progress.stars}",
                  fontWeight = FontWeight.Black,
                  fontSize = 18.sp,
                  color = Color(0xFF422C00)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Mascot encouragement banner
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "🐰", fontSize = 28.sp)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "All 26 Letters & 1 to 50 Numbers Ready!",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
                Text(
                  text = "Practice with camera mirror, word translator, and speed match!",
                  color = Color.White.copy(alpha = 0.90f),
                  fontSize = 11.sp,
                  lineHeight = 15.sp
                )
              }
            }
          }
        }
      }
    }

    // 6 Core Learning & Game Icons for Irssa
    item {
      Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Learning & Game Modes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1E293B)
          )
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF5C52E5).copy(alpha = 0.10f)
          ) {
            Text(
              text = "6 Activities",
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF5C52E5)
            )
          }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Featured: Live Hand & Body Gesture to Alphabet & Number Converter
        Surface(
          shape = RoundedCornerShape(22.dp),
          color = Color(0xFF5C52E5),
          shadowElevation = 4.dp,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable {
              HapticUtil.playTap(context)
              onNavigate(ActiveScreen.GESTURE_CONVERTER)
            }
            .testTag("home_gesture_converter_hero")
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.horizontalGradient(
                  listOf(Color(0xFF4338CA), Color(0xFF6366F1), Color(0xFF8B5CF6))
                )
              )
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .background(Color.White.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(text = "🤟", fontSize = 28.sp)
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "Gesture ➔ Text & Numbers",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = Color.White
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFC312)
                  ) {
                    Text(
                      text = "NEW ✨",
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Black,
                      color = Color(0xFF422C00),
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Sign hand & body gestures to convert live into A-Z & 1-50 with speech!",
                  fontSize = 11.sp,
                  color = Color.White.copy(alpha = 0.90f),
                  lineHeight = 15.sp
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = CircleShape,
                color = Color.White
              ) {
                Box(
                  modifier = Modifier.size(34.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = "▶", fontSize = 14.sp, color = Color(0xFF4338CA))
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Row 1: 1. Learn A to Z  &  2. Learn 1 to 50
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickActionCard(
            title = "Learn A to Z",
            subtitle = "37 Urdu + 26 English Signs",
            emoji = "🔤",
            color = Color(0xFF5C52E5),
            modifier = Modifier
              .weight(1f)
              .testTag("home_learn_alphabet_action"),
            onClick = {
              HapticUtil.playTap(context)
              onSelectItem(LearningDataSource.alphabetItems.first())
              onNavigate(ActiveScreen.LEARN_ALPHABET)
            }
          )

          QuickActionCard(
            title = "Learn 1 to 50",
            subtitle = "Numbers & Finger Dots",
            emoji = "🔢",
            color = Color(0xFF00B894),
            modifier = Modifier
              .weight(1f)
              .testTag("home_learn_numbers_action"),
            onClick = {
              HapticUtil.playTap(context)
              onSelectItem(LearningDataSource.numberItems.first())
              onNavigate(ActiveScreen.LEARN_NUMBERS)
            }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: 3. Alphabet + Sign + Word (A sign Apple)  &  4. Visual Sign Quiz (Big O)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickActionCard(
            title = "A 🤟 Apple",
            subtitle = "Letter + Sign + Word",
            emoji = "🍎",
            color = Color(0xFFFF5E7E),
            modifier = Modifier
              .weight(1f)
              .testTag("home_alphabet_words_action"),
            onClick = {
              HapticUtil.playTap(context)
              onNavigate(ActiveScreen.ALPHABET_WORDS)
            }
          )

          QuickActionCard(
            title = "Visual Quiz",
            subtitle = "Big 'O' & Sign Match",
            emoji = "🎯",
            color = Color(0xFFFF7849),
            modifier = Modifier
              .weight(1f)
              .testTag("home_quiz_action"),
            onClick = {
              HapticUtil.playTap(context)
              onNavigate(ActiveScreen.QUIZ)
            }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 3: 5. Camera Hand Quiz (Split Screen Match)  &  6. Trophy Badges & Stickers
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickActionCard(
            title = "Camera Quiz",
            subtitle = "Split-Screen & Points",
            emoji = "📸",
            color = Color(0xFF0284C7),
            modifier = Modifier
              .weight(1f)
              .testTag("home_camera_quiz_action"),
            onClick = {
              HapticUtil.playTap(context)
              onNavigate(ActiveScreen.CAMERA_QUIZ)
            }
          )

          QuickActionCard(
            title = "Trophy Stickers",
            subtitle = "Stars & Rewards",
            emoji = "🏆",
            color = Color(0xFFFFC312),
            modifier = Modifier
              .weight(1f)
              .testTag("home_stickers_action"),
            onClick = {
              HapticUtil.playTap(context)
              onNavigate(ActiveScreen.STICKERS)
            }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Tools: Word Speller & Speed Match
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .clickable {
                HapticUtil.playTap(context)
                onNavigate(ActiveScreen.SPEECH_TO_SIGN)
              }
              .testTag("home_translator_action")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "💬", fontSize = 20.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Word Speller", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                Text("Spell in signs", fontSize = 10.sp, color = Color(0xFF64748B))
              }
            }
          }

          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .clickable {
                HapticUtil.playTap(context)
                onNavigate(ActiveScreen.SPEED_MATCH)
              }
              .testTag("home_speed_action")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "⚡", fontSize = 20.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Speed Match", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                Text("45s game", fontSize = 10.sp, color = Color(0xFF64748B))
              }
            }
          }
        }
      }
    }

    // Category Tabs (All, A-Z Alphabet, 1-50 Numbers, Vocabulary)
    item {
      Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Browse Signs",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
          )
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
          val tabs = listOf(
            "ALL" to "All Signs",
            "ALPHABET" to "Urdu ا–ے (37)",
            "ENGLISH" to "English A–Z (26)",
            "NUMBERS_1_10" to "1 - 10 Numbers",
            "NUMBERS_11_50" to "11 - 50 Numbers",
            "VOCABULARY" to "Everyday Words"
          )
          items(tabs) { (key, label) ->
            val isSelected = activeCategoryTab == key
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable {
                  HapticUtil.playTap(context)
                  activeCategoryTab = key
                },
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) Color(0xFF5C52E5) else Color.White,
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF5C52E5) else Color(0xFFCBD5E1))
            ) {
              Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF334155)
              )
            }
          }
        }
      }
    }

    // Horizontal Carousel of Selected Category Signs
    item {
      Spacer(modifier = Modifier.height(14.dp))
      val filteredItems = when (activeCategoryTab) {
        "ALPHABET" -> LearningDataSource.alphabetItems
        "ENGLISH" -> LearningDataSource.englishAlphabetItems
        "NUMBERS_1_10" -> LearningDataSource.numberItems.take(10)
        "NUMBERS_11_50" -> LearningDataSource.numberItems.drop(10)
        "VOCABULARY" -> LearningDataSource.vocabularyItems
        else -> (LearningDataSource.alphabetItems.take(10) + LearningDataSource.numberItems.take(10) + LearningDataSource.vocabularyItems.take(4))
      }

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
      ) {
        items(filteredItems) { item ->
          ItemBubbleCard(
            item = item,
            isCompleted = progress.completedItemsCsv.split(",").contains(item.id),
            onClick = {
              HapticUtil.playTap(context)
              onSelectItem(item)
              onNavigate(ActiveScreen.LEARN)
            }
          )
        }
      }
    }

    // Progressive Levels Journey
    item {
      Spacer(modifier = Modifier.height(24.dp))
      Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
          text = "Progressive Learning Journey",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF1E293B)
        )
        Text(
          text = "Complete levels to unlock all 37 PSL letters and 50 numbers!",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(14.dp))
      }
    }

    // List of Game Levels
    items(LearningDataSource.levels) { level ->
      val isUnlocked = progress.stars >= level.requiredStars || progress.unlockedLevel >= level.id
      LevelJourneyCard(
        level = level,
        isUnlocked = isUnlocked,
        currentStars = progress.stars,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
        onClick = {
          if (isUnlocked) {
            HapticUtil.playTap(context)
            onSelectLevel(level)
            onNavigate(ActiveScreen.LEARN)
          } else {
            HapticUtil.playPromptAlert(context)
          }
        }
      )
    }
  }
}

@Composable
fun QuickActionCard(
  title: String,
  subtitle: String? = null,
  icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
  emoji: String? = null,
  color: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    modifier = modifier
      .height(115.dp)
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
    border = androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.35f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(color),
        contentAlignment = Alignment.Center
      ) {
        if (emoji != null) {
          Text(text = emoji, fontSize = 22.sp)
        } else if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }
      }
      Column {
        Text(
          text = title,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = Color(0xFF1E293B),
          lineHeight = 16.sp
        )
        if (subtitle != null) {
          Text(
            text = subtitle,
            fontSize = 10.sp,
            color = Color(0xFF64748B),
            lineHeight = 13.sp
          )
        }
      }
    }
  }
}

@Composable
fun ItemBubbleCard(
  item: LearningItem,
  isCompleted: Boolean,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .width(115.dp)
      .height(145.dp)
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .testTag("item_card_${item.id}"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    border = androidx.compose.foundation.BorderStroke(
      width = if (isCompleted) 2.dp else 1.dp,
      color = if (isCompleted) Color(item.colorHex) else Color(0xFFE2E8F0)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top row: object emoji and star
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = item.objectEmoji, fontSize = 20.sp)
        if (isCompleted) {
          Text(text = "⭐", fontSize = 14.sp)
        }
      }

      // Big Letter / Number
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(CircleShape)
          .background(Color(item.colorHex).copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = item.title,
          fontWeight = FontWeight.Black,
          fontSize = if (item.title.length > 2) 16.sp else 24.sp,
          color = Color(item.colorHex)
        )
      }

      Text(
        text = item.subtitle,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF475569),
        maxLines = 1
      )
    }
  }
}

@Composable
fun LevelJourneyCard(
  level: GameLevel,
  isUnlocked: Boolean,
  currentStars: Int,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .clickable(onClick = onClick)
      .testTag("level_card_${level.id}"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isUnlocked) Color.White else Color(0xFFF1F5F9)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 4.dp else 0.dp),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.5.dp,
      color = if (isUnlocked) Color(level.colorHex).copy(alpha = 0.40f) else Color(0xFFE2E8F0)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(
            if (isUnlocked) Color(level.colorHex) else Color(0xFFCBD5E1)
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(text = level.badgeEmoji, fontSize = 26.sp)
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = level.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = if (isUnlocked) Color(0xFF1E293B) else Color(0xFF64748B)
          )
          Icon(
            imageVector = if (isUnlocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
            contentDescription = if (isUnlocked) "Unlocked" else "Locked",
            tint = if (isUnlocked) Color(0xFF00B894) else Color(0xFF94A3B8),
            modifier = Modifier.size(16.dp)
          )
        }

        Text(
          text = level.description,
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF64748B),
          fontSize = 11.sp,
          lineHeight = 15.sp,
          maxLines = 2
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (!isUnlocked) {
          val progressRatio = (currentStars.toFloat() / level.requiredStars).coerceIn(0f, 1f)
          Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
              progress = { progressRatio },
              modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape),
              color = Color(level.colorHex),
              trackColor = Color(0xFFE2E8F0)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "$currentStars/${level.requiredStars} ⭐",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF64748B)
            )
          }
        } else {
          Text(
            text = "Reward: ${level.badgeReward} 🏆",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(level.colorHex)
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Icon(
        imageVector = Icons.Rounded.PlayArrow,
        contentDescription = "Start Level",
        tint = if (isUnlocked) Color(level.colorHex) else Color(0xFF94A3B8),
        modifier = Modifier.size(24.dp)
      )
    }
  }
}
