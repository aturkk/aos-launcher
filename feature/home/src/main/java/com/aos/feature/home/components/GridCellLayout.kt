package com.aos.feature.home.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aos.core.domain.model.LauncherItem
import com.aos.core.ui.components.AosAppIcon
import com.aos.core.ui.theme.SquircleShape
import kotlin.math.roundToInt

@Composable
fun GridCellLayout(
    items: List<LauncherItem>,
    columns: Int = 4,
    rows: Int = 5,
    iconShape: Shape = SquircleShape,
    showLabels: Boolean = true,
    notificationCounts: Map<String, Int> = emptyMap(),
    onAppClick: (packageName: String, activityName: String) -> Unit,
    onFolderClick: (LauncherItem.FolderItem) -> Unit,
    onMoveItem: (itemId: Long, cellX: Int, cellY: Int) -> Unit,
    onMergeIntoFolder: (draggedApp: LauncherItem.AppItem, targetApp: LauncherItem.AppItem) -> Unit,
    onRemoveItem: (itemId: Long) -> Unit,
    onAppInfo: (packageName: String) -> Unit,
    onUninstall: (packageName: String) -> Unit,
    onEmptyAreaLongClick: () -> Unit = {},
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
                    .zIndex(10f)
                    .graphicsLayer {
                        scaleX = 1.15f
                        scaleY = 1.15f
                        shadowElevation = 12f
                    }
            } else {
                Modifier.offset(x = baseOffsetX, y = baseOffsetY)
            }

            when (item) {
                is LauncherItem.AppItem -> {
                    var menuExpanded by remember { mutableStateOf(false) }
                    var totalDragDistance by remember { mutableFloatStateOf(0f) }

                    Box(
                        modifier = itemModifier
                            .size(width = cellWidth * item.spanX, height = cellHeight * item.spanY)
                            .pointerInput(item.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedItemId = item.id
                                        dragOffset = Offset.Zero
                                        totalDragDistance = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount
                                        totalDragDistance += dragAmount.getDistance()
                                    },
                                    onDragEnd = {
                                        if (totalDragDistance < 25f) {
                                            // Long press without drag: show context menu
                                            menuExpanded = true
                                        } else {
                                            // Drag gesture: calculate target cell
                                            val totalOffsetX = with(density) { baseOffsetX.toPx() } + dragOffset.x
                                            val totalOffsetY = with(density) { baseOffsetY.toPx() } + dragOffset.y

                                            val targetCellX = (totalOffsetX / cellWidthPx).toInt().coerceIn(0, columns - 1)
                                            val targetCellY = (totalOffsetY / cellHeightPx).toInt().coerceIn(0, rows - 1)

                                            val targetItem = items.find { it.cellX == targetCellX && it.cellY == targetCellY && it.id != item.id }

                                            if (targetItem is LauncherItem.AppItem) {
                                                onMergeIntoFolder(item, targetItem)
                                            } else if (targetItem == null) {
                                                onMoveItem(item.id, targetCellX, targetCellY)
                                            }
                                        }

                                        draggedItemId = null
                                        dragOffset = Offset.Zero
                                        totalDragDistance = 0f
                                    },
                                    onDragCancel = {
                                        draggedItemId = null
                                        dragOffset = Offset.Zero
                                        totalDragDistance = 0f
                                    }
                                )
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
                            onClick = { onAppClick(item.packageName, item.activityName) },
                            onLongClick = null
                        )

                        HomeItemContextMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            onRemoveFromHome = { onRemoveItem(item.id) },
                            onAppInfo = { onAppInfo(item.packageName) },
                            onUninstall = { onUninstall(item.packageName) }
                        )
                    }
                }

                is LauncherItem.FolderItem -> {
                    Box(
                        modifier = itemModifier
                            .size(width = cellWidth * item.spanX, height = cellHeight * item.spanY)
                            .pointerInput(item.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedItemId = item.id
                                        dragOffset = Offset.Zero
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount
                                    },
                                    onDragEnd = {
                                        val totalOffsetX = with(density) { baseOffsetX.toPx() } + dragOffset.x
                                        val totalOffsetY = with(density) { baseOffsetY.toPx() } + dragOffset.y

                                        val targetCellX = (totalOffsetX / cellWidthPx).toInt().coerceIn(0, columns - 1)
                                        val targetCellY = (totalOffsetY / cellHeightPx).toInt().coerceIn(0, rows - 1)

                                        val isOccupied = items.any { it.cellX == targetCellX && it.cellY == targetCellY && it.id != item.id }
                                        if (!isOccupied) {
                                            onMoveItem(item.id, targetCellX, targetCellY)
                                        }

                                        draggedItemId = null
                                        dragOffset = Offset.Zero
                                    },
                                    onDragCancel = {
                                        draggedItemId = null
                                        dragOffset = Offset.Zero
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        FolderIconView(
                            folder = item,
                            showLabel = showLabels,
                            onClick = { onFolderClick(item) }
                        )
                    }
                }

                else -> {
                    // System widgets
                }
            }
        }
    }
}
