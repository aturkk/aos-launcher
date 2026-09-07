package com.aos.feature.appdrawer.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.core.domain.model.AppCategory

/**
 * Smart Launcher tarzı dikey kategori sütunu (Vertical Category Rail).
 * Çekmecenin sol kenarında yer alır ve başparmakla tek dokunuşla kategoriler arası geçiş sağlar.
 */
@Composable
fun VerticalCategoryRail(
    selectedCategory: AppCategory,
    onSelectCategory: (AppCategory) -> Unit,
    categoryCounts: Map<AppCategory, Int> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        AppCategory.All,
        AppCategory.Communication,
        AppCategory.Internet,
        AppCategory.Games,
        AppCategory.Media,
        AppCategory.Productivity,
        AppCategory.Tools
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(58.dp)
            .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF181920).copy(alpha = 0.94f),
                        Color(0xFF101116).copy(alpha = 0.96f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                val count = categoryCounts[category] ?: 0

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                    label = "rail_icon_color"
                )

                val pillBgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else Color.Transparent,
                    label = "rail_pill_bg"
                )

                val pillBorderColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else Color.Transparent,
                    label = "rail_pill_border"
                )

                val pillWidth by animateDpAsState(
                    targetValue = if (isSelected) 46.dp else 40.dp,
                    animationSpec = spring(),
                    label = "rail_pill_width"
                )

                Box(
                    modifier = Modifier
                        .size(width = pillWidth, height = 44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(pillBgColor)
                        .border(1.dp, pillBorderColor, RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSelectCategory(category)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Active indicator line on the left side
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .width(3.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(category),
                            contentDescription = category.title,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )

                        if (count > 0 && category != AppCategory.All) {
                            Text(
                                text = count.toString(),
                                color = iconColor.copy(alpha = 0.75f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(category: AppCategory): ImageVector = when (category) {
    AppCategory.All -> Icons.Default.AutoAwesome
    AppCategory.Communication -> Icons.AutoMirrored.Filled.Chat
    AppCategory.Media -> Icons.Default.PlayCircle
    AppCategory.Games -> Icons.Default.SportsEsports
    AppCategory.Productivity -> Icons.Default.Work
    AppCategory.Internet -> Icons.Default.Public
    AppCategory.Tools -> Icons.Default.Build
    AppCategory.Other -> Icons.Default.Category
}
