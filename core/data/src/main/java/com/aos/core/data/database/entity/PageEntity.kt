package com.aos.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launcher_pages")
data class PageEntity(
    @PrimaryKey(autoGenerate = true)
    val pageId: Long = 0L,
    val pageIndex: Int,
    val isHomePage: Boolean = false
)
