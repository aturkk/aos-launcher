package com.aos.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.ripple
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aos.core.domain.model.AppInfo
import com.aos.core.domain.model.LauncherItem
import com.aos.core.ui.components.AosAppIcon
import com.aos.core.ui.theme.SquircleShape
import com.aos.feature.home.widget.LauncherWidgetHost
import com.aos.feature.home.widget.SystemWidgetView
import kotlin.math.roundToInt

@Composable
fun GridCellLayout(
    items: List<LauncherItem>,
    columns: Int = 4,
    rows: Int = 5,
    iconShape: Shape = SquircleShape,
    showLabels: Boolean = true,
    notificationCounts: Map<String, Int> = emptyMap(),
    isEditMode: Boolean = false,
    pendingPlacedApp: AppInfo? = null,
    onPlacePendingApp: (AppInfo, cellX: Int, cellY: Int) -> Unit = { _, _, _ -> },
    onAppClick: (packageName: String, activityName: String) -> Unit,
    onFolderClick: (LauncherItem.FolderItem) -> Unit,
    onMoveItem: (itemId: Long, cellX: Int, cellY: Int) -> Unit,
    onMergeIntoFolder: (draggedApp: LauncherItem.AppItem, targetApp: LauncherItem.AppItem) -> Unit,
    onAddToExistingFolder: (draggedApp: LauncherItem.AppItem, targetFolder: LauncherItem.FolderItem) -> Unit = { _, _ -> },
    onRemoveItem: (itemId: Long) -> Unit,
    onAppInfo: (packageName: String) -> Unit,
    onUninstall: (packageName: String) -> Unit,
    onEmptyAreaLongClick: () -> Unit = {},
    widgetHost: LauncherWidgetHost? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onEmptyAreaLongClick()
                    }
                )
            }
    ) {
        val cellWidth = maxWidth / columns
        val cellHeight = maxHeight / rows

        val cellWidthPx = with(density) { cellWidth.toPx() }
        val cellHeightPx = with(density) { cellHeight.toPx() }

        var draggedItemId by remember { mutableStateOf<Long?>(null) }
        var dragOffset by remember { mutableStateOf(Offset.Zero) }

        // Drop zone at the top when dragging
        AnimatedVisibility(
            visible = draggedItemId != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .zIndex(30f)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.92f),
                shadowElevation = 8.dp,
                modifier = Modifier.padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kaldırmak için buraya bırakın",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        items.forEach { item ->
            val isBeingDragged = draggedItemId == item.id

            val baseOffsetX = cellWidth * item.cellX
            val baseOffsetY = cellHeight * item.cellY

            val itemModifier = if (isBeingDragged) {
                Modifier
                    .offset {
                        IntOffset(
                            x = (with(density) { baseOffsetX.toPx() } + dragOffset.x).roundToInt(),
                            y = (with(density) { baseOffsetY.toPx() } + dragOffset.y).roundToInt()
                        )
                    }
                    .zIndex(20f)
                    .graphicsLayer {
                        scaleX = 1.15f
                        scaleY = 1.15f
                        shadowElevation = 16f
                    }
            } else {
                Modifier.offset(x = baseOffsetX, y = baseOffsetY)
            }

            when (item) {
                is LauncherItem.AppItem -> {
                    val appInteraction = remember(item.id) { MutableInteractionSource() }
                    Box(
                        modifier = itemModifier
                            .size(width = cellWidth * item.spanX, height = cellHeight * item.spanY)
                            .semantics {
                                role = Role.Button
                                contentDescription = item.customLabel ?: item.label
                                onClick {
                                    onAppClick(item.packageName, item.activityName)
                                    true
                                }
                            }
                            .indication(appInteraction, ripple(bounded = false, radius = 34.dp))
                            .pointerInput(item.id) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val press = PressInteraction.Press(down.position)
                                    appInteraction.tryEmit(press)

                                    val longPress = awaitLongPressOrCancellation(down.id)
                                    if (longPress != null) {
                                        appInteraction.tryEmit(PressInteraction.Cancel(press))
                                        // Long press -> Start dragging
                                        draggedItemId = item.id
                                        dragOffset = Offset.Zero
                                        var isDragCompleted = false
                                        try {
                                            drag(longPress.id) { change ->
                                                change.consume()
                                                dragOffset += change.positionChange()
                                            }
                                            isDragCompleted = true
                                        } finally {
                                            if (isDragCompleted) {
                                                val totalOffsetX = with(density) { baseOffsetX.toPx() } + dragOffset.x
                                                val totalOffsetY = with(density) { baseOffsetY.toPx() } + dragOffset.y

                                                if (totalOffsetY < 90f) {
                                                    onRemoveItem(item.id)
                                                } else {
                                                    val targetCellX = (totalOffsetX / cellWidthPx).toInt().coerceIn(0, columns - 1)
                                                    val targetCellY = (totalOffsetY / cellHeightPx).toInt().coerceIn(0, rows - 1)

                                                    val targetItem = items.find { it.cellX == targetCellX && it.cellY == targetCellY && it.id != item.id }

                                                    if (targetItem is LauncherItem.AppItem) {
                                                        onMergeIntoFolder(item, targetItem)
                                                    } else if (targetItem is LauncherItem.FolderItem) {
                                                        onAddToExistingFolder(item, targetItem)
                                                    } else if (targetItem == null) {
                                                        onMoveItem(item.id, targetCellX, targetCellY)
                                                    }
                                                }
                                            }
                                            draggedItemId = null
                                            dragOffset = Offset.Zero
                                        }
                                    } else {
                                        // Tap -> Open app
                                        appInteraction.tryEmit(PressInteraction.Release(press))
                                        onAppClick(item.packageName, item.activityName)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AosAppIcon(
                            label = item.customLabel ?: item.label,
                            packageName = item.packageName,
                            activityName = item.activityName,
                            iconUri = item.customIconUri,
                            shape = iconShape,
                            showLabel = showLabels,
                            badgeCount = notificationCounts[item.packageName] ?: 0,
                            onClick = null,
                            onLongClick = null
                        )
                        if (isEditMode) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 6.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.65f))
                                    .border(1.dp, Color.Black.copy(alpha = 0.35f), CircleShape)
                            )
                        }
                    }
                }

                is LauncherItem.FolderItem -> {
                    val folderInteraction = remember(item.id) { MutableInteractionSource() }
                    Box(
                        modifier = itemModifier
                            .size(width = cellWidth * item.spanX, height = cellHeight * item.spanY)
                            .semantics {
                                role = Role.Button
                                contentDescription = item.title
                                onClick {
                                    onFolderClick(item)
                                    true
                                }
                            }
                            .indication(folderInteraction, ripple(bounded = false, radius = 34.dp))
                            .pointerInput(item.id) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val press = PressInteraction.Press(down.position)
                                    folderInteraction.tryEmit(press)

                                    val longPress = awaitLongPressOrCancellation(down.id)
                                    if (longPress != null) {
                                        folderInteraction.tryEmit(PressInteraction.Cancel(press))
                                        // Long press -> Start dragging folder
                                        draggedItemId = item.id
                                        dragOffset = Offset.Zero
                                        var isDragCompleted = false
                                        try {
                                            drag(longPress.id) { change ->
                                                change.consume()
                                                dragOffset += change.positionChange()
                                            }
                                            isDragCompleted = true
                                        } finally {
                                            if (isDragCompleted) {
                                                val totalOffsetX = with(density) { baseOffsetX.toPx() } + dragOffset.x
                                                val totalOffsetY = with(density) { baseOffsetY.toPx() } + dragOffset.y

                                                if (totalOffsetY < 90f) {
                                                    onRemoveItem(item.id)
                                                } else {
                                                    val targetCellX = (totalOffsetX / cellWidthPx).toInt().coerceIn(0, columns - 1)
                                                    val targetCellY = (totalOffsetY / cellHeightPx).toInt().coerceIn(0, rows - 1)

                                                    val isOccupied = items.any { it.cellX == targetCellX && it.cellY == targetCellY && it.id != item.id }
                                                    if (!isOccupied) {
                                                        onMoveItem(item.id, targetCellX, targetCellY)
                                                    }
                                                }
                                            }
                                            draggedItemId = null
                                            dragOffset = Offset.Zero
                                        }
                                    } else {
                                        // Tap -> Open folder
                                        folderInteraction.tryEmit(PressInteraction.Release(press))
                                        onFolderClick(item)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        FolderIconView(
                            folder = item,
                            showLabel = showLabels,
                            onClick = null
                        )

                        if (isEditMode) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 6.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.65f))
                                    .border(1.dp, Color.Black.copy(alpha = 0.35f), CircleShape)
                            )
                        }
                    }
                }

                is LauncherItem.WidgetItem -> {
                    Box(
                        modifier = itemModifier
                            .size(width = cellWidth * item.spanX, height = cellHeight * item.spanY)
                            .clip(RoundedCornerShape(16.dp))
                            .pointerInput(item.id) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val longPress = awaitLongPressOrCancellation(down.id)
                                    if (longPress != null) {
                                        // Long press -> Drag widget
                                        draggedItemId = item.id
                                        dragOffset = Offset.Zero
                                        var isDragCompleted = false
                                        try {
                                            drag(longPress.id) { change ->
                                                change.consume()
                                                dragOffset += change.positionChange()
                                            }
                                            isDragCompleted = true
                                        } finally {
                                            if (isDragCompleted) {
                                                val totalOffsetX = with(density) { baseOffsetX.toPx() } + dragOffset.x
                                                val totalOffsetY = with(density) { baseOffsetY.toPx() } + dragOffset.y

                                                if (totalOffsetY < 90f) {
                                                    onRemoveItem(item.id)
                                                } else {
                                                    val targetCellX = (totalOffsetX / cellWidthPx).toInt().coerceIn(0, columns - item.spanX)
                                                    val targetCellY = (totalOffsetY / cellHeightPx).toInt().coerceIn(0, rows - item.spanY)

                                                    val isOccupied = items.any {
                                                        it.id != item.id &&
                                                        it.cellX < (targetCellX + item.spanX) && (it.cellX + it.spanX) > targetCellX &&
                                                        it.cellY < (targetCellY + item.spanY) && (it.cellY + it.spanY) > targetCellY
                                                    }
                                                    if (!isOccupied) {
                                                        onMoveItem(item.id, targetCellX, targetCellY)
                                                    }
                                                }
                                            }
                                            draggedItemId = null
                                            dragOffset = Offset.Zero
                                        }
                                    }
                                }
                            }
                    ) {
                        if (widgetHost != null && item.appWidgetId != -1) {
                            SystemWidgetView(
                                appWidgetId = item.appWidgetId,
                                widgetHost = widgetHost,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Widget", color = Color.White)
                            }
                        }

                        if (isEditMode) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 6.dp, end = 6.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.65f))
                                    .border(1.dp, Color.Black.copy(alpha = 0.35f), CircleShape)
                            )
                        }
                    }
                }

                else -> {}
            }
        }

        // Empty cells drop targets when placing pending app from App Drawer
        if (pendingPlacedApp != null) {
            for (r in 0 until rows) {
                for (c in 0 until columns) {
                    val isOccupied = items.any { it.cellX == c && it.cellY == r }
                    if (!isOccupied) {
                        Box(
                            modifier = Modifier
                                .offset(x = cellWidth * c, y = cellHeight * r)
                                .size(cellWidth, cellHeight)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                .clickable {
                                    onPlacePendingApp(pendingPlacedApp, c, r)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ Yerleştir",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
