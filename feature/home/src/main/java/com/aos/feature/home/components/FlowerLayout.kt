package com.aos.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.aos.core.domain.model.LauncherItem
import com.aos.core.ui.components.AosAppIcon
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Smart Launcher'ın dünyaca ünlü Çiçek (Flower / Circle) İkon Düzeni.
 * Sık kullanılan uygulamaları tek elle başparmak erişiminde (Thumb Zone)
 * dairesel bir yörünge etrafında konumlandırır.
 */
@Composable
fun FlowerLayout(
    items: List<LauncherItem>,
    iconShape: Shape,
    showLabels: Boolean,
    notificationCounts: Map<String, Int>,
    onAppClick: (packageName: String, activityName: String) -> Unit,
    onFolderClick: (LauncherItem.FolderItem) -> Unit,
    onEmptySlotClick: () -> Unit = {},
    isEditMode: Boolean = false,
    onRemoveItem: (Long) -> Unit = {},
    onItemLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Up to 8 items in the flower ring, plus 1 central item (total 9 slots)
    val centerItem = items.firstOrNull()
    val orbitalItems = items.drop(1).take(8)
    val totalOrbitSlots = 6 // Classic 6-petal flower for optimal thumb spacing

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val radiusPx = with(density) { 115.dp.toPx() }

        // Subtle ambient ring glow
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.06f), CircleShape)
        )

        // Center item
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            if (centerItem != null) {
                FlowerItemSlot(
                    item = centerItem,
                    iconShape = iconShape,
                    showLabels = showLabels,
                    notificationCounts = notificationCounts,
                    isEditMode = isEditMode,
                    onAppClick = onAppClick,
                    onFolderClick = onFolderClick,
                    onRemoveItem = onRemoveItem,
                    onItemLongClick = onItemLongClick
                )
            } else {
                EmptyFlowerSlot(onClick = onEmptySlotClick)
            }
        }

        // Orbital items
        for (i in 0 until totalOrbitSlots) {
            // Angle starting from top (-90 deg or -PI/2)
            val angle = -Math.PI / 2.0 + (i * 2.0 * Math.PI / totalOrbitSlots)
            val offsetX = (radiusPx * cos(angle)).roundToInt()
            val offsetY = (radiusPx * sin(angle)).roundToInt()

            val item = orbitalItems.getOrNull(i)

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX, offsetY) }
                    .size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item != null) {
                    FlowerItemSlot(
                        item = item,
                        iconShape = iconShape,
                        showLabels = showLabels,
                        notificationCounts = notificationCounts,
                        isEditMode = isEditMode,
                        onAppClick = onAppClick,
                        onFolderClick = onFolderClick,
                        onRemoveItem = onRemoveItem,
                        onItemLongClick = onItemLongClick
                    )
                } else {
                    EmptyFlowerSlot(onClick = onEmptySlotClick)
                }
            }
        }

        val overflowCount = items.size - (1 + totalOrbitSlots)
        val hasWidgets = items.any { it is LauncherItem.WidgetItem || it is LauncherItem.WidgetStackItem }
        if (isEditMode && (overflowCount > 0 || hasWidgets)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                val warningText = buildString {
                    if (overflowCount > 0) {
                        append("+$overflowCount öğe bu düzene sığmıyor. ")
                    }
                    if (hasWidgets) {
                        append("Widget'lar geometrik düzende desteklenmez. ")
                    }
                    append("Izgara moduna geçin.")
                }
                Text(
                    text = warningText,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun FlowerItemSlot(
    item: LauncherItem,
    iconShape: Shape,
    showLabels: Boolean,
    notificationCounts: Map<String, Int>,
    isEditMode: Boolean,
    onAppClick: (packageName: String, activityName: String) -> Unit,
    onFolderClick: (LauncherItem.FolderItem) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onItemLongClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(68.dp),
        contentAlignment = Alignment.Center
    ) {
        when (item) {
            is LauncherItem.AppItem -> {
                AosAppIcon(
                    label = item.label,
                    packageName = item.packageName,
                    activityName = item.activityName,
                    shape = iconShape,
                    showLabel = showLabels,
                    badgeCount = notificationCounts[item.packageName] ?: 0,
                    onClick = { onAppClick(item.packageName, item.activityName) },
                    onLongClick = onItemLongClick
                )
            }
            is LauncherItem.FolderItem -> {
                FolderIconView(
                    folder = item,
                    showLabel = showLabels,
                    onClick = { onFolderClick(item) }
                )
            }
            else -> {}
        }

        if (isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .clickable { onRemoveItem(item.id) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kaldır",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyFlowerSlot(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Uygulama Ekle",
            tint = Color.White.copy(alpha = 0.45f),
            modifier = Modifier.size(20.dp)
        )
    }
}
