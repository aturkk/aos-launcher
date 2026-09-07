package com.aos.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aos.core.data.database.entity.PageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PageDao {
    @Query("SELECT * FROM launcher_pages ORDER BY pageIndex ASC")
    fun getAllPages(): Flow<List<PageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: PageEntity): Long

    @Query("DELETE FROM launcher_pages WHERE pageIndex = :pageIndex")
    suspend fun deletePage(pageIndex: Int)

    @Query("UPDATE launcher_pages SET pageIndex = pageIndex - 1 WHERE pageIndex > :deletedIndex")
    suspend fun decrementPageIndicesAbove(deletedIndex: Int)

    @Query("SELECT COUNT(*) FROM launcher_pages")
    suspend fun getPageCount(): Int

    @Query("DELETE FROM launcher_pages")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pages: List<PageEntity>)
}
