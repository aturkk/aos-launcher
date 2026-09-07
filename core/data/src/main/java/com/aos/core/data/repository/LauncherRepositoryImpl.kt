package com.aos.core.data.repository

import com.aos.core.common.dispatcher.AosDispatchers
import com.aos.core.common.dispatcher.Dispatcher
import com.aos.core.common.result.Result
import com.aos.core.common.result.asResult
import com.aos.core.data.database.dao.LauncherItemDao
import com.aos.core.data.database.dao.PageDao
import com.aos.core.data.database.entity.LauncherItemEntity
import com.aos.core.data.database.entity.PageEntity
import com.aos.core.domain.model.LauncherItem
import com.aos.core.domain.model.PageInfo
import com.aos.core.domain.repository.LauncherRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LauncherRepositoryImpl @Inject constructor(
    private val launcherItemDao: LauncherItemDao,
    private val pageDao: PageDao,
    @Dispatcher(AosDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : LauncherRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getPages(): Flow<Result<List<PageInfo>>> =
        pageDao.getAllPages()
            .map { list ->
                if (list.isEmpty()) {
                    listOf(PageInfo(pageId = 1L, pageIndex = 0, isHomePage = true))
                } else {
                    list.map { PageInfo(it.pageId, it.pageIndex, it.isHomePage) }
                }
            }
            .asResult()
            .flowOn(ioDispatcher)

    override fun getItemsForPage(pageIndex: Int): Flow<Result<List<LauncherItem>>> =
        launcherItemDao.getItemsForPage(pageIndex)
            .map { list -> list.map { it.toDomain() } }
            .asResult()
            .flowOn(ioDispatcher)

    override fun getDockItems(): Flow<Result<List<LauncherItem>>> =
        launcherItemDao.getDockItems()
            .map { list -> list.map { it.toDomain() } }
            .asResult()
            .flowOn(ioDispatcher)

    override suspend fun saveItem(item: LauncherItem): Long = withContext(ioDispatcher) {
        launcherItemDao.insertItem(item.toEntity())
    }

    override suspend fun moveItem(itemId: Long, pageIndex: Int, cellX: Int, cellY: Int) = withContext(ioDispatcher) {
        launcherItemDao.updatePosition(itemId, pageIndex, cellX, cellY)
    }

    override suspend fun deleteItem(itemId: Long) = withContext(ioDispatcher) {
        launcherItemDao.deleteItem(itemId)
    }

    override suspend fun updateFolder(folderId: Long, newTitle: String, items: List<LauncherItem.AppItem>) = withContext(ioDispatcher) {
        val serialized = json.encodeToString(items)
        launcherItemDao.updateFolderContent(folderId, newTitle, serialized)
    }

    override suspend fun addPage(pageIndex: Int, isHomePage: Boolean): Long = withContext(ioDispatcher) {
        pageDao.insertPage(PageEntity(pageIndex = pageIndex, isHomePage = isHomePage))
    }

    override suspend fun deletePage(pageIndex: Int) = withContext(ioDispatcher) {
        launcherItemDao.deleteItemsOnPage(pageIndex)
        pageDao.deletePage(pageIndex)
    }

    private fun LauncherItemEntity.toDomain(): LauncherItem = when (itemType) {
        "FOLDER" -> {
            val itemsList = try {
                json.decodeFromString<List<LauncherItem.AppItem>>(folderItemsJson)
            } catch (e: Exception) {
                emptyList()
            }
            LauncherItem.FolderItem(
                id = id,
                pageIndex = pageIndex,
                cellX = cellX,
                cellY = cellY,
                spanX = spanX,
                spanY = spanY,
                title = title.ifEmpty { "Klasör" },
                items = itemsList
            )
        }
        "WIDGET" -> LauncherItem.WidgetItem(
            id = id,
            pageIndex = pageIndex,
            cellX = cellX,
            cellY = cellY,
            spanX = spanX,
            spanY = spanY,
            appWidgetId = appWidgetId
        )
        "SHORTCUT" -> LauncherItem.ShortcutItem(
            id = id,
            pageIndex = pageIndex,
            cellX = cellX,
            cellY = cellY,
            spanX = spanX,
            spanY = spanY,
            label = title,
            intentUri = ""
        )
        else -> LauncherItem.AppItem(
            id = id,
            pageIndex = pageIndex,
            cellX = cellX,
            cellY = cellY,
            spanX = spanX,
            spanY = spanY,
            packageName = packageName,
            activityName = activityName,
            label = title,
            customIconUri = customIconUri
        )
    }

    private fun LauncherItem.toEntity(): LauncherItemEntity = when (this) {
        is LauncherItem.AppItem -> LauncherItemEntity(
            id = id,
            pageIndex = pageIndex,
            cellX = cellX,
            cellY = cellY,
            spanX = spanX,
            spanY = spanY,
            itemType = "APP",
            packageName = packageName,
            activityName = activityName,
            title = label,
            customIconUri = customIconUri
        )
        is LauncherItem.FolderItem -> LauncherItemEntity(
            id = id,
            pageIndex = pageIndex,
            cellX = cellX,
            cellY = cellY,
            spanX = spanX,
            spanY = spanY,
            itemType = "FOLDER",
            title = title,
            folderItemsJson = json.encodeToString(items)
        )
        is LauncherItem.WidgetItem -> LauncherItemEntity(
            id = id,
            pageIndex = pageIndex,
            cellX = cellX,
            cellY = cellY,
            spanX = spanX,
            spanY = spanY,
            itemType = "WIDGET",
            appWidgetId = appWidgetId
        )
        is LauncherItem.ShortcutItem -> LauncherItemEntity(
            id = id,
            pageIndex = pageIndex,
            cellX = cellX,
            cellY = cellY,
            spanX = spanX,
            spanY = spanY,
            itemType = "SHORTCUT",
            title = label
        )
    }
}
