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
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Widgets
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
import com.aos.feature.home.widget.WidgetStackView
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
    onMergeIntoWidgetStack: (draggedWidget: LauncherItem.WidgetItem, targetWidget: LauncherItem.WidgetItem) -> Unit = { _, _ -> },
    onAddWidgetToExistingStack: (draggedWidget: LauncherItem.WidgetItem, targetStack: LauncherItem.WidgetStackItem) -> Unit = { _, _ -> },
    onOpenStackSettings: (LauncherItem.WidgetStackItem) -> Unit = {},
    onAddWidgetToSingleWidget: (LauncherItem.WidgetItem) -> Unit = {},
    onOpenPopupWidget: (LauncherItem.AppItem) -> Unit = {},
    onAddPopupWidget: (LauncherItem.AppItem) -> Unit = {},
    onRemovePopupWidget: (LauncherItem.AppItem) -> Unit = {},
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
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val deleteThresholdPx = with(density) { 110.dp.toPx() }

        var draggedItemId by remember { mutableStateOf<Long?>(null) }
        var dragOffset by remember { mutableStateOf(Offset.Zero) }
        val lastTapTimes = remember { mutableMapOf<Long, Long>() }

        // For pending app placement from app drawer: free-form drag
        var placementPointerOffset by remember(pendingPlacedApp) { mutableStateOf<Offset?>(null) }

        // Drop zone at the top when dragging or placing pending app
        AnimatedVisibility(
            visible = draggedItemId != null || pendingPlacedApp != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .zIndex(45f)
        ) {
            val isHoveringDelete = (draggedItemId != null && (dragOffset.y + with(density) { 60.dp.toPx() } < deleteThresholdPx)) ||
                (placementPointerOffset?.let { it.y < deleteThresholdPx } == true)

            val dropBgColor = if (isHoveringDelete) Color(0xFFFF1744) else Color(0xFFD32F2F).copy(alpha = 0.92f)
            val dropScale = if (isHoveringDelete) 1.08f else 1f

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = dropBgColor,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(4.dp)
                    .graphicsLayer {
                        scaleX = dropScale
                        scaleY = dropScale
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (pendingPlacedApp != null) Icons.Default.Close else Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (pendingPlacedApp != null) "İptal etmek için buraya bırakın" else "Kaldırmak için buraya bırakın",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
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

                                                if (totalOffsetY < deleteThresholdPx) {
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
                                        // Tap / Double Tap detection
                                        appInteraction.tryEmit(PressInteraction.Release(press))
                                        val now = System.currentTimeMillis()
                                        val lastTap = lastTapTimes[item.id] ?: 0L
                                        if (item.popupWidgetId != null && (now - lastTap) < 380L) {
                                            lastTapTimes[item.id] = 0L
                                            onOpenPopupWidget(item)
                                        } else {
                                            lastTapTimes[item.id] = now
                                            onAppClick(item.packageName, item.activityName)
                                        }
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

                        // Subtle Pop-up widget indicator dot when not in edit mode (Smart Launcher style)
                        if (!isEditMode && item.popupWidgetId != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = if (showLabels) 14.dp else 4.dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFAB47BC))
                            )
                        }

                        if (isEditMode) {
                            // Pop-up Widget action badge on Top-Left
                            val hasPopup = item.popupWidgetId != null
                            val badgeBg = if (hasPopup) Color(0xFF9C27B0) else MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(top = 2.dp, start = 2.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(badgeBg)
                                    .clickable {
                                        if (hasPopup) {
                                            onOpenPopupWidget(item)
                                        } else {
                                            onAddPopupWidget(item)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = if (hasPopup) "Açılır Widget'ı Göster" else "Açılır Widget Ekle",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            // Delete badge on Top-Right
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

                                                if (totalOffsetY < deleteThresholdPx) {
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

                is LauncherItem.WidgetItem -> {
                    Box(
                        modifier = itemModifier
                            .size(width = cellWidth * item.spanX, height = cellHeight * item.spanY)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        if (widgetHost != null && item.appWidgetId != -1) {
                            SystemWidgetView(
                                appWidgetId = item.appWidgetId,
                                widgetHost = widgetHost,
                                onLongClick = {
                                    draggedItemId = item.id
                                    dragOffset = Offset.Zero
                                },
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
                            // Transparent drag layer in edit mode to avoid native view touch stealing
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(item.id) {
                                        detectDragGestures(
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

                                                if (totalOffsetY < deleteThresholdPx) {
                                                    onRemoveItem(item.id)
                                                } else {
                                                    val targetCellX = (totalOffsetX / cellWidthPx).toInt().coerceIn(0, columns - item.spanX)
                                                    val targetCellY = (totalOffsetY / cellHeightPx).toInt().coerceIn(0, rows - item.spanY)

                                                    val targetWidget = items.firstOrNull { other ->
                                                        other.id != item.id && other is LauncherItem.WidgetItem &&
                                                        other.cellX == targetCellX && other.cellY == targetCellY
                                                    } as? LauncherItem.WidgetItem

                                                    val targetStack = items.firstOrNull { other ->
                                                        other.id != item.id && other is LauncherItem.WidgetStackItem &&
                                                        other.cellX == targetCellX && other.cellY == targetCellY
                                                    } as? LauncherItem.WidgetStackItem

                                                    if (targetWidget != null) {
                                                        onMergeIntoWidgetStack(item, targetWidget)
                                                    } else if (targetStack != null) {
                                                        onAddWidgetToExistingStack(item, targetStack)
                                                    } else {
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
                                            },
                                            onDragCancel = {
                                                draggedItemId = null
                                                dragOffset = Offset.Zero
                                            }
                                        )
                                    }
                            )

                            // Blue circular "+ / Stack" button on top-left to convert/add to stack
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(top = 4.dp, start = 4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                                    .clickable { onAddWidgetToSingleWidget(item) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Yığına Dönüştür",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            // Red circular delete button on top-right
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935))
                                    .clickable { onRemoveItem(item.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Widgetı Kaldır",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                is LauncherItem.WidgetStackItem -> {
                    Box(
                        modifier = itemModifier
                            .size(width = cellWidth * item.spanX, height = cellHeight * item.spanY)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        WidgetStackView(
                            stack = item,
                            widgetHost = widgetHost,
                            isEditMode = isEditMode,
                            onLongClick = {
                                draggedItemId = item.id
                                dragOffset = Offset.Zero
                            },
                            onOpenStackSettings = {
                                onOpenStackSettings(item)
                            },
                            onRemoveStack = {
                                onRemoveItem(item.id)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isEditMode) {
                            // Transparent drag layer in edit mode to avoid native view touch stealing
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(item.id) {
                                        detectDragGestures(
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

                                                if (totalOffsetY < deleteThresholdPx) {
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
                                                draggedItemId = null
                                                dragOffset = Offset.Zero
                                            },
                                            onDragCancel = {
                                                draggedItemId = null
                                                dragOffset = Offset.Zero
                                            }
                                        )
                                    }
                            )
                        }
                    }
                }

                else -> {}
            }
        }

        // Free-form Drag & Drop placement for pending app from App Drawer (NO static + Yerleştir buttons)
        if (pendingPlacedApp != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(35f)
                    .pointerInput(pendingPlacedApp) {
                        detectTapGestures { tapOffset ->
                            if (tapOffset.y >= deleteThresholdPx) {
                                val targetCellX = (tapOffset.x / cellWidthPx).toInt().coerceIn(0, columns - 1)
                                val targetCellY = (tapOffset.y / cellHeightPx).toInt().coerceIn(0, rows - 1)
                                onPlacePendingApp(pendingPlacedApp, targetCellX, targetCellY)
                            }
                        }
                    }
                    .pointerInput(pendingPlacedApp) {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                placementPointerOffset = startOffset
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val current = placementPointerOffset ?: change.position
                                placementPointerOffset = current + dragAmount
                            },
                            onDragEnd = {
                                val offset = placementPointerOffset
                                if (offset != null) {
                                    if (offset.y < deleteThresholdPx) {
                                        // Dropped on cancel drop zone
                                    } else {
                                        val targetCellX = (offset.x / cellWidthPx).toInt().coerceIn(0, columns - 1)
                                        val targetCellY = (offset.y / cellHeightPx).toInt().coerceIn(0, rows - 1)
                                        onPlacePendingApp(pendingPlacedApp, targetCellX, targetCellY)
                                    }
                                }
                                placementPointerOffset = null
                            },
                            onDragCancel = {
                                placementPointerOffset = null
                            }
                        )
                    }
            ) {
                // Active snap cell glow preview
                val activeOffset = placementPointerOffset
                if (activeOffset != null && activeOffset.y >= deleteThresholdPx) {
                    val hoverCellX = (activeOffset.x / cellWidthPx).toInt().coerceIn(0, columns - 1)
                    val hoverCellY = (activeOffset.y / cellHeightPx).toInt().coerceIn(0, rows - 1)
                    Box(
                        modifier = Modifier
                            .offset(x = cellWidth * hoverCellX, y = cellHeight * hoverCellY)
                            .size(cellWidth, cellHeight)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.20f))
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    )
                }

                // Floating App Icon under finger
                val floatPos = placementPointerOffset ?: Offset(maxWidthPx / 2f, maxHeightPx / 2f)
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (floatPos.x - with(density) { 36.dp.toPx() }).roundToInt(),
                                y = (floatPos.y - with(density) { 36.dp.toPx() }).roundToInt()
                            )
                        }
                        .size(72.dp)
                        .graphicsLayer {
                            scaleX = 1.18f
                            scaleY = 1.18f
                            shadowElevation = 24f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AosAppIcon(
                        label = pendingPlacedApp.label,
                        packageName = pendingPlacedApp.packageName,
                        activityName = pendingPlacedApp.activityName,
                        shape = iconShape,
                        showLabel = showLabels
                    )
                }
            }
        }
    }
}
