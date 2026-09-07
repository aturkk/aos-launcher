package com.aos.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aos.core.data.database.entity.LauncherItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherItemDao {
    @Query("SELECT * FROM launcher_items WHERE pageIndex = :pageIndex AND isDockItem = 0")
    fun getItemsForPage(pageIndex: Int): Flow<List<LauncherItemEntity>>

    @Query("SELECT * FROM launcher_items WHERE isDockItem = 1 ORDER BY cellX ASC")
    fun getDockItems(): Flow<List<LauncherItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LauncherItemEntity): Long

    @Update
    suspend fun updateItem(item: LauncherItemEntity)

    @Query("UPDATE launcher_items SET pageIndex = :pageIndex, cellX = :cellX, cellY = :cellY WHERE id = :itemId")
    suspend fun updatePosition(itemId: Long, pageIndex: Int, cellX: Int, cellY: Int)

    @Query("UPDATE launcher_items SET title = :newTitle, folderItemsJson = :folderItemsJson WHERE id = :folderId")
    suspend fun updateFolderContent(folderId: Long, newTitle: String, folderItemsJson: String)

    @Query("DELETE FROM launcher_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Long)

    @Query("DELETE FROM launcher_items WHERE pageIndex = :pageIndex")
    suspend fun deleteItemsOnPage(pageIndex: Int)

    @Query("DELETE FROM launcher_items")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<LauncherItemEntity>)
}
