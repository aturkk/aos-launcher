package com.aos.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launcher_items")
data class LauncherItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val pageIndex: Int = 0,
    val cellX: Int = 0,
    val cellY: Int = 0,
    val spanX: Int = 1,
    val spanY: Int = 1,
    val itemType: String, // "APP", "FOLDER", "WIDGET", "SHORTCUT"
    val packageName: String = "",
    val activityName: String = "",
    val title: String = "",
    val customIconUri: String? = null,
    val appWidgetId: Int = -1,
    val isDockItem: Boolean = false,
    val folderItemsJson: String = "[]"
)
