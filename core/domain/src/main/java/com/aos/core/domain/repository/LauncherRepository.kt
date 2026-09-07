package com.aos.core.domain.repository

import com.aos.core.common.result.Result
import com.aos.core.domain.model.LauncherItem
import com.aos.core.domain.model.PageInfo
import kotlinx.coroutines.flow.Flow

interface LauncherRepository {
    fun getPages(): Flow<Result<List<PageInfo>>>
    fun getItemsForPage(pageIndex: Int): Flow<Result<List<LauncherItem>>>
    fun getDockItems(): Flow<Result<List<LauncherItem>>>
    suspend fun saveItem(item: LauncherItem): Long
    suspend fun moveItem(itemId: Long, pageIndex: Int, cellX: Int, cellY: Int)
    suspend fun deleteItem(itemId: Long)
    suspend fun updateFolder(folderId: Long, newTitle: String, items: List<LauncherItem.AppItem>)
    suspend fun addPage(pageIndex: Int, isHomePage: Boolean): Long
    suspend fun deletePage(pageIndex: Int)
}
