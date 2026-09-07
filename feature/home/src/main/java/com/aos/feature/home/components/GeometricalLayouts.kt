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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
 * Smart Launcher 6 Petek (Honeycomb / Hexagonal Staggered) İkon Düzeni.
 * Sık kullanılan uygulamaları başparmak bölgesinde (Thumb Zone) 3-4-3 matrisinde
 * birbirine kenetlenen petek yapısında konumlandırır.
 */
@Composable
fun HoneycombLayout(
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

    // Honeycomb slot offsets (3 - 4 - 3 rows: 10 slots total)
    // Row 0 (3 items, top): y = -78dp, x = -80dp, 0dp, +80dp
    // Row 1 (4 items, mid): y = 0dp,   x = -120dp, -40dp, +40dp, +120dp
    // Row 2 (3 items, bot): y = +78dp, x = -80dp, 0dp, +80dp
    val slotPositions = listOf(
        // Row 0
        Pair(-80f, -78f), Pair(0f, -78f), Pair(80f, -78f),
        // Row 1 (Staggered offset)
        Pair(-120f, 0f), Pair(-40f, 0f), Pair(40f, 0f), Pair(120f, 0f),
        // Row 2
        Pair(-80f, 78f), Pair(0f, 78f), Pair(80f, 78f)
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow for honeycomb cluster
        Box(
            modifier = Modifier
                .size(310.dp, 240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                            Color.Transparent
                        )
                    )
                )
        )

        slotPositions.forEachIndexed { index, (xDp, yDp) ->
            val offsetX = with(density) { xDp.dp.toPx() }.roundToInt()
            val offsetY = with(density) { yDp.dp.toPx() }.roundToInt()
            val item = items.getOrNull(index)

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX, offsetY) }
                    .size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item != null) {
                    GeometricItemSlot(
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
                    EmptyGeometricSlot(onClick = onEmptySlotClick)
                }
            }
        }
    }
}

/**
 * Smart Launcher 6 Kavisli Kemer (Arch / Thumb Sweep) İkon Düzeni.
 * Sık kullanılan uygulamaları başparmağın doğal dönme ekseni boyunca
 * ekranın alt üçte birinde ergonomik bir kemer şeklinde konumlandırır.
 */
@Composable
fun ArchLayout(
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

    // 7 slots arched over the bottom area
    // Angles from -150 deg (left) to -30 deg (right)
    val slotAngles = listOf(-150.0, -130.0, -110.0, -90.0, -70.0, -50.0, -30.0)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val radiusPx = with(density) { 160.dp.toPx() }
        val centerShiftY = with(density) { 50.dp.toPx() }

        // Subtle ambient curved glow
        Box(
            modifier = Modifier
                .offset { IntOffset(0, centerShiftY.roundToInt()) }
                .size(320.dp, 200.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        slotAngles.forEachIndexed { index, angleDeg ->
            val angleRad = Math.toRadians(angleDeg)
            val offsetX = (radiusPx * cos(angleRad)).roundToInt()
            val offsetY = (radiusPx * sin(angleRad) + centerShiftY).roundToInt()
            val item = items.getOrNull(index)

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX, offsetY) }
                    .size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item != null) {
                    GeometricItemSlot(
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
                    EmptyGeometricSlot(onClick = onEmptySlotClick)
                }
            }
        }
    }
}

@Composable
private fun GeometricItemSlot(
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
private fun EmptyGeometricSlot(
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
