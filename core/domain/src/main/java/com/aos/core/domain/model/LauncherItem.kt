package com.aos.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class LauncherItem {
    abstract val id: Long
    abstract val pageIndex: Int
    abstract val cellX: Int
    abstract val cellY: Int
    abstract val spanX: Int
    abstract val spanY: Int

    @Serializable
    data class AppItem(
        override val id: Long = 0L,
        override val pageIndex: Int = 0,
        override val cellX: Int = 0,
        override val cellY: Int = 0,
        override val spanX: Int = 1,
        override val spanY: Int = 1,
        val packageName: String,
        val activityName: String,
        val label: String,
        val customLabel: String? = null,
        val customIconUri: String? = null
    ) : LauncherItem()

    @Serializable
    data class FolderItem(
        override val id: Long = 0L,
        override val pageIndex: Int = 0,
        override val cellX: Int = 0,
        override val cellY: Int = 0,
        override val spanX: Int = 1,
        override val spanY: Int = 1,
        val title: String = "",
        val items: List<AppItem> = emptyList(),
        val colorHex: String? = null
    ) : LauncherItem()

    @Serializable
    data class WidgetItem(
        override val id: Long = 0L,
        override val pageIndex: Int = 0,
        override val cellX: Int = 0,
        override val cellY: Int = 0,
        override val spanX: Int = 2,
        override val spanY: Int = 2,
        val appWidgetId: Int = -1,
        val providerPackage: String = "",
        val providerClass: String = ""
    ) : LauncherItem()

    @Serializable
    data class ShortcutItem(
        override val id: Long = 0L,
        override val pageIndex: Int = 0,
        override val cellX: Int = 0,
        override val cellY: Int = 0,
        override val spanX: Int = 1,
        override val spanY: Int = 1,
        val label: String,
        val intentUri: String
    ) : LauncherItem()
}
