package com.aos.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OxygenEditModeTopBar(
    onGroupClick: () -> Unit = {},
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Grupla" Pill Button
        Surface(
            onClick = onGroupClick,
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF2A2B30).copy(alpha = 0.88f)
        ) {
            Text(
                text = "Grupla",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // "Tamamlandı" Pill Button
        Surface(
            onClick = onDoneClick,
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF2A2B30).copy(alpha = 0.88f)
        ) {
            Text(
                text = "Tamamlandı",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun OxygenEditModeBottomBar(
    onAddWidget: () -> Unit,
    onOpenWallpaperAndStyle: () -> Unit,
    onOpenLayoutGrid: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        OxygenEditActionButton(
            icon = Icons.Default.Widgets,
            label = "Widget'lar",
            onClick = onAddWidget
        )

        OxygenEditActionButton(
            icon = Icons.Default.Wallpaper,
            label = "Duvar kağıtları\nve stil",
            onClick = onOpenWallpaperAndStyle
        )

        OxygenEditActionButton(
            icon = Icons.Default.GridView,
            label = "Yerleşim",
            onClick = onOpenLayoutGrid
        )

        OxygenEditActionButton(
            icon = Icons.Default.Settings,
            label = "Ana ekran\nayarları",
            onClick = onOpenSettings
        )
    }
}

@Composable
private fun OxygenEditActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(82.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2B30).copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center
            ),
            color = Color.White,
            maxLines = 2
        )
    }
}
