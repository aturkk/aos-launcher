package com.aos.core.data.repository

import android.os.Build
import com.aos.core.common.dispatcher.AosDispatchers
import com.aos.core.common.dispatcher.Dispatcher
import com.aos.core.common.result.Result
import com.aos.core.data.crypto.AosCryptoManager
import com.aos.core.data.database.AosLauncherDatabase
import com.aos.core.data.database.dao.LauncherItemDao
import com.aos.core.data.database.dao.PageDao
import com.aos.core.data.database.dao.ProfileDao
import com.aos.core.data.database.entity.LauncherItemEntity
import com.aos.core.data.database.entity.PageEntity
import com.aos.core.data.database.entity.ProfileEntity
import com.aos.core.data.preferences.UserPreferencesDataStore
import com.aos.core.domain.model.BackupPayload
import com.aos.core.domain.model.DarkModeOption
import com.aos.core.domain.model.IconShapeOption
import com.aos.core.domain.model.LauncherItem
import com.aos.core.domain.model.PageInfo
import com.aos.core.domain.model.PageTransitionEffect
import com.aos.core.domain.model.Profile
import com.aos.core.domain.model.ProfileType
import com.aos.core.domain.model.SearchEngineOption
import com.aos.core.domain.model.SyncStatus
import com.aos.core.domain.model.ThemeConfig
import com.aos.core.domain.repository.UserPreferences
import com.aos.core.domain.repository.BackupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val launcherItemDao: LauncherItemDao,
    private val pageDao: PageDao,
    private val profileDao: ProfileDao,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val aosCryptoManager: AosCryptoManager,
    private val database: AosLauncherDatabase,
    @Dispatcher(AosDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : BackupRepository {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)

    override fun getSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()

    override suspend fun createBackupPayload(): BackupPayload = withContext(ioDispatcher) {
        val pages = pageDao.getAllPages().firstOrNull()?.map { PageInfo(it.pageId, it.pageIndex, it.isHomePage) }
            ?: listOf(PageInfo(pageId = 1L, pageIndex = 0, isHomePage = true))

        val allItems = mutableListOf<LauncherItem>()
        pages.forEach { page ->
            val pageItems = launcherItemDao.getItemsForPage(page.pageIndex).firstOrNull() ?: emptyList()
            allItems.addAll(pageItems.map { it.toDomain() })
        }
        val dockItems = launcherItemDao.getDockItems().firstOrNull() ?: emptyList()
        allItems.addAll(dockItems.map { it.toDomain() })

        val profileEntities = profileDao.getAllProfiles().firstOrNull() ?: emptyList()
        val profiles = profileEntities.map { it.toDomain() }

        val prefs = userPreferencesDataStore.userPreferences.first()

        val brand = Build.MANUFACTURER?.replaceFirstChar { it.uppercase() } ?: "Android"
        val model = Build.MODEL ?: "Device"

        BackupPayload(
            version = 1,
            createdAt = System.currentTimeMillis(),
            deviceName = "$brand $model",
            pages = pages,
            items = allItems,
            profiles = profiles,
            themeConfig = prefs.themeConfig,
            userPreferences = prefs
        )
    }

    override suspend fun exportEncryptedBackup(password: String): Result<ByteArray> = withContext(ioDispatcher) {
        try {
            val payload = createBackupPayload()
            val jsonString = serializePayload(payload)
            val encryptedBytes = aosCryptoManager.encrypt(jsonString.toByteArray(Charsets.UTF_8), password)
            Result.Success(encryptedBytes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun importEncryptedBackup(encryptedBytes: ByteArray, password: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            val decryptedBytes = aosCryptoManager.decrypt(encryptedBytes, password)
            val jsonString = String(decryptedBytes, Charsets.UTF_8)
            val payload = deserializePayload(jsonString)
            restorePayload(payload)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun restorePayload(payload: BackupPayload): Result<Unit> = withContext(ioDispatcher) {
        try {
            // 1. Clear existing items and pages
            launcherItemDao.clearAll()
            pageDao.clearAll()

            // 2. Insert pages
            val pageEntities = payload.pages.map {
                PageEntity(pageId = it.pageId, pageIndex = it.pageIndex, isHomePage = it.isHomePage)
            }
            pageDao.insertAll(pageEntities)

            // 3. Insert launcher items
            val itemEntities = payload.items.map { item ->
                itemToEntity(item)
            }
            launcherItemDao.insertAll(itemEntities)

            // 4. Update / insert profiles
            val profileEntities = payload.profiles.map { prof ->
                ProfileEntity(
                    id = prof.id,
                    name = prof.name,
                    type = prof.type.name,
                    isActive = prof.isActive,
                    blockedPackagesJson = JSONArray(prof.blockedPackages).toString(),
                    allowedPackagesJson = JSONArray(prof.allowedPackages).toString(),
                    pinCode = prof.pinCode,
                    isScheduleEnabled = prof.isScheduleEnabled,
                    startHour = prof.startHour,
                    startMinute = prof.startMinute,
                    endHour = prof.endHour,
                    endMinute = prof.endMinute,
                    iconName = prof.iconName
                )
            }
            if (profileEntities.isNotEmpty()) {
                profileDao.insertAll(profileEntities)
            }

            // 5. Restore Preferences & Theme
            userPreferencesDataStore.setGridDimensions(
                payload.userPreferences.gridRows,
                payload.userPreferences.gridColumns
            )
            userPreferencesDataStore.setShowAppLabels(payload.userPreferences.showAppLabels)
            userPreferencesDataStore.setDoubleTapToSleep(payload.userPreferences.doubleTapToSleep)
            userPreferencesDataStore.updateThemeConfig(payload.themeConfig)

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun syncWithCloud(): Result<Unit> = withContext(ioDispatcher) {
        _syncStatus.value = SyncStatus.Syncing
        try {
            // Simulate cloud synchronization (zero-knowledge payload push)
            kotlinx.coroutines.delay(1200)
            val now = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.Success(now)
            Result.Success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error(e.localizedMessage ?: "Senkronizasyon hatası")
            Result.Error(e)
        }
    }

    private fun serializePayload(payload: BackupPayload): String {
        val root = JSONObject()
        root.put("version", payload.version)
        root.put("createdAt", payload.createdAt)
        root.put("deviceName", payload.deviceName)

        // Pages
        val pagesArray = JSONArray()
        payload.pages.forEach { page ->
            val pObj = JSONObject()
            pObj.put("pageId", page.pageId)
            pObj.put("pageIndex", page.pageIndex)
            pObj.put("isHomePage", page.isHomePage)
            pagesArray.put(pObj)
        }
        root.put("pages", pagesArray)

        // Items
        val itemsArray = JSONArray()
        payload.items.forEach { item ->
            val iObj = JSONObject()
            iObj.put("id", item.id)
            iObj.put("pageIndex", item.pageIndex)
            iObj.put("cellX", item.cellX)
            iObj.put("cellY", item.cellY)
            iObj.put("spanX", item.spanX)
            iObj.put("spanY", item.spanY)

            when (item) {
                is LauncherItem.AppItem -> {
                    iObj.put("type", "APP")
                    iObj.put("packageName", item.packageName)
                    iObj.put("activityName", item.activityName)
                    iObj.put("label", item.label)
                    item.customLabel?.let { iObj.put("customLabel", it) }
                    item.customIconUri?.let { iObj.put("customIconUri", it) }
                    item.popupWidgetId?.let { iObj.put("popupWidgetId", it) }
                }
                is LauncherItem.FolderItem -> {
                    iObj.put("type", "FOLDER")
                    iObj.put("title", item.title)
                    item.colorHex?.let { iObj.put("colorHex", it) }
                    val subApps = JSONArray()
                    item.items.forEach { sub ->
                        val sObj = JSONObject()
                        sObj.put("id", sub.id)
                        sObj.put("packageName", sub.packageName)
                        sObj.put("activityName", sub.activityName)
                        sObj.put("label", sub.label)
                        subApps.put(sObj)
                    }
                    iObj.put("folderItems", subApps)
                }
                is LauncherItem.WidgetStackItem -> {
                    iObj.put("type", "WIDGET_STACK")
                    val subWidgets = JSONArray()
                    item.widgets.forEach { sub ->
                        val sObj = JSONObject()
                        sObj.put("id", sub.id)
                        sObj.put("appWidgetId", sub.appWidgetId)
                        sObj.put("providerPackage", sub.providerPackage)
                        sObj.put("providerClass", sub.providerClass)
                        sObj.put("spanX", sub.spanX)
                        sObj.put("spanY", sub.spanY)
                        subWidgets.put(sObj)
                    }
                    iObj.put("widgets", subWidgets)
                }
                is LauncherItem.WidgetItem -> {
                    iObj.put("type", "WIDGET")
                    iObj.put("appWidgetId", item.appWidgetId)
                    iObj.put("providerPackage", item.providerPackage)
                    iObj.put("providerClass", item.providerClass)
                }
                is LauncherItem.ShortcutItem -> {
                    iObj.put("type", "SHORTCUT")
                    iObj.put("label", item.label)
                    iObj.put("intentUri", item.intentUri)
                }
            }
            itemsArray.put(iObj)
        }
        root.put("items", itemsArray)

        // Profiles
        val profilesArray = JSONArray()
        payload.profiles.forEach { prof ->
            val prObj = JSONObject()
            prObj.put("id", prof.id)
            prObj.put("name", prof.name)
            prObj.put("type", prof.type.name)
            prObj.put("isActive", prof.isActive)

            val blockedArr = JSONArray()
            prof.blockedPackages.forEach { blockedArr.put(it) }
            prObj.put("blockedPackages", blockedArr)

            val allowedArr = JSONArray()
            prof.allowedPackages.forEach { allowedArr.put(it) }
            prObj.put("allowedPackages", allowedArr)

            prof.pinCode?.let { prObj.put("pinCode", it) }
            prObj.put("isScheduleEnabled", prof.isScheduleEnabled)
            prObj.put("startHour", prof.startHour)
            prObj.put("startMinute", prof.startMinute)
            prObj.put("endHour", prof.endHour)
            prObj.put("endMinute", prof.endMinute)
            prObj.put("iconName", prof.iconName)
            profilesArray.put(prObj)
        }
        root.put("profiles", profilesArray)

        // Preferences & Theme
        val prefsObj = JSONObject()
        prefsObj.put("gridRows", payload.userPreferences.gridRows)
        prefsObj.put("gridColumns", payload.userPreferences.gridColumns)
        prefsObj.put("showAppLabels", payload.userPreferences.showAppLabels)
        prefsObj.put("doubleTapToSleep", payload.userPreferences.doubleTapToSleep)

        val tc = payload.themeConfig
        val tcObj = JSONObject()
        tcObj.put("darkMode", tc.darkMode.name)
        tcObj.put("useDynamicColors", tc.useDynamicColors)
        tcObj.put("primaryColorArgb", tc.primaryColorArgb)
        tcObj.put("iconShape", tc.iconShape.name)
        tcObj.put("pageTransition", tc.pageTransition.name)
        tc.selectedIconPackPackage?.let { tcObj.put("selectedIconPackPackage", it) }
        tcObj.put("animationSpeedMultiplier", tc.animationSpeedMultiplier.toDouble())
        tcObj.put("isParallaxEnabled", tc.isParallaxEnabled)
        tcObj.put("showNotificationBadges", tc.showNotificationBadges)
        tcObj.put("searchEngine", tc.searchEngine.name)
        prefsObj.put("themeConfig", tcObj)

        root.put("userPreferences", prefsObj)

        return root.toString()
    }

    private fun deserializePayload(jsonString: String): BackupPayload {
        val root = JSONObject(jsonString)
        val version = root.optInt("version", 1)
        val createdAt = root.optLong("createdAt", System.currentTimeMillis())
        val deviceName = root.optString("deviceName", "AOS Backup")

        // Pages
        val pages = mutableListOf<PageInfo>()
        val pagesArray = root.optJSONArray("pages")
        if (pagesArray != null) {
            for (i in 0 until pagesArray.length()) {
                val pObj = pagesArray.getJSONObject(i)
                pages.add(
                    PageInfo(
                        pageId = pObj.optLong("pageId", (i + 1).toLong()),
                        pageIndex = pObj.optInt("pageIndex", i),
                        isHomePage = pObj.optBoolean("isHomePage", i == 0)
                    )
                )
            }
        }
        if (pages.isEmpty()) {
            pages.add(PageInfo(pageId = 1L, pageIndex = 0, isHomePage = true))
        }

        // Items
        val items = mutableListOf<LauncherItem>()
        val itemsArray = root.optJSONArray("items")
        if (itemsArray != null) {
            for (i in 0 until itemsArray.length()) {
                val iObj = itemsArray.getJSONObject(i)
                val id = iObj.optLong("id", 0L)
                val pageIndex = iObj.optInt("pageIndex", 0)
                val cellX = iObj.optInt("cellX", 0)
                val cellY = iObj.optInt("cellY", 0)
                val spanX = iObj.optInt("spanX", 1)
                val spanY = iObj.optInt("spanY", 1)
                val type = iObj.optString("type", "APP")

                when (type) {
                    "FOLDER" -> {
                        val title = iObj.optString("title", "Klasör")
                        val colorHex = if (iObj.has("colorHex")) iObj.getString("colorHex") else null
                        val subList = mutableListOf<LauncherItem.AppItem>()
                        val subArr = iObj.optJSONArray("folderItems")
                        if (subArr != null) {
                            for (j in 0 until subArr.length()) {
                                val sObj = subArr.getJSONObject(j)
                                subList.add(
                                    LauncherItem.AppItem(
                                        id = sObj.optLong("id", 0L),
                                        packageName = sObj.optString("packageName", ""),
                                        activityName = sObj.optString("activityName", ""),
                                        label = sObj.optString("label", "")
                                    )
                                )
                            }
                        }
                        items.add(
                            LauncherItem.FolderItem(
                                id = id,
                                pageIndex = pageIndex,
                                cellX = cellX,
                                cellY = cellY,
                                spanX = spanX,
                                spanY = spanY,
                                title = title,
                                items = subList,
                                colorHex = colorHex
                            )
                        )
                    }
                    "WIDGET" -> {
                        items.add(
                            LauncherItem.WidgetItem(
                                id = id,
                                pageIndex = pageIndex,
                                cellX = cellX,
                                cellY = cellY,
                                spanX = spanX,
                                spanY = spanY,
                                appWidgetId = iObj.optInt("appWidgetId", -1),
                                providerPackage = iObj.optString("providerPackage", ""),
                                providerClass = iObj.optString("providerClass", "")
                            )
                        )
                    }
                    "SHORTCUT" -> {
                        items.add(
                            LauncherItem.ShortcutItem(
                                id = id,
                                pageIndex = pageIndex,
                                cellX = cellX,
                                cellY = cellY,
                                spanX = spanX,
                                spanY = spanY,
                                label = iObj.optString("label", ""),
                                intentUri = iObj.optString("intentUri", "")
                            )
                        )
                    }
                    else -> {
                        // APP
                        items.add(
                            LauncherItem.AppItem(
                                id = id,
                                pageIndex = pageIndex,
                                cellX = cellX,
                                cellY = cellY,
                                spanX = spanX,
                                spanY = spanY,
                                packageName = iObj.optString("packageName", ""),
                                activityName = iObj.optString("activityName", ""),
                                label = iObj.optString("label", ""),
                                customLabel = if (iObj.has("customLabel")) iObj.getString("customLabel") else null,
                                customIconUri = if (iObj.has("customIconUri")) iObj.getString("customIconUri") else null
                            )
                        )
                    }
                }
            }
        }

        // Profiles
        val profiles = mutableListOf<Profile>()
        val profilesArray = root.optJSONArray("profiles")
        if (profilesArray != null) {
            for (i in 0 until profilesArray.length()) {
                val prObj = profilesArray.getJSONObject(i)
                val id = prObj.optLong("id", (i + 1).toLong())
                val name = prObj.optString("name", "Profil")
                val typeStr = prObj.optString("type", ProfileType.Normal.name)
                val profType = try { ProfileType.valueOf(typeStr) } catch (e: Exception) { ProfileType.Normal }
                val isActive = prObj.optBoolean("isActive", i == 0)

                val blockedList = mutableListOf<String>()
                val blockedArr = prObj.optJSONArray("blockedPackages")
                if (blockedArr != null) {
                    for (k in 0 until blockedArr.length()) {
                        blockedList.add(blockedArr.getString(k))
                    }
                }

                val allowedList = mutableListOf<String>()
                val allowedArr = prObj.optJSONArray("allowedPackages")
                if (allowedArr != null) {
                    for (k in 0 until allowedArr.length()) {
                        allowedList.add(allowedArr.getString(k))
                    }
                }

                profiles.add(
                    Profile(
                        id = id,
                        name = name,
                        type = profType,
                        isActive = isActive,
                        blockedPackages = blockedList,
                        allowedPackages = allowedList,
                        pinCode = if (prObj.has("pinCode")) prObj.getString("pinCode") else null,
                        isScheduleEnabled = prObj.optBoolean("isScheduleEnabled", false),
                        startHour = prObj.optInt("startHour", 9),
                        startMinute = prObj.optInt("startMinute", 0),
                        endHour = prObj.optInt("endHour", 18),
                        endMinute = prObj.optInt("endMinute", 0),
                        iconName = prObj.optString("iconName", "default")
                    )
                )
            }
        }

        // Preferences & Theme
        val prefsObj = root.optJSONObject("userPreferences")
        val gridRows = prefsObj?.optInt("gridRows", 5) ?: 5
        val gridColumns = prefsObj?.optInt("gridColumns", 4) ?: 4
        val showAppLabels = prefsObj?.optBoolean("showAppLabels", true) ?: true
        val doubleTapToSleep = prefsObj?.optBoolean("doubleTapToSleep", true) ?: true

        val tcObj = prefsObj?.optJSONObject("themeConfig")
        val darkMode = try { DarkModeOption.valueOf(tcObj?.optString("darkMode", "System") ?: "System") } catch (e: Exception) { DarkModeOption.System }
        val useDynamicColors = tcObj?.optBoolean("useDynamicColors", true) ?: true
        val primaryColorArgb = tcObj?.optLong("primaryColorArgb", 0xFF6200EE) ?: 0xFF6200EE
        val iconShape = try { IconShapeOption.valueOf(tcObj?.optString("iconShape", "Squircle") ?: "Squircle") } catch (e: Exception) { IconShapeOption.Squircle }
        val pageTransition = try { PageTransitionEffect.valueOf(tcObj?.optString("pageTransition", "Cube") ?: "Cube") } catch (e: Exception) { PageTransitionEffect.Cube }
        val selectedIconPackPackage = if (tcObj?.has("selectedIconPackPackage") == true) tcObj.getString("selectedIconPackPackage") else null
        val animationSpeedMultiplier = (tcObj?.optDouble("animationSpeedMultiplier", 1.0) ?: 1.0).toFloat()
        val isParallaxEnabled = tcObj?.optBoolean("isParallaxEnabled", true) ?: true
        val showNotificationBadges = tcObj?.optBoolean("showNotificationBadges", true) ?: true
        val searchEngine = try { SearchEngineOption.valueOf(tcObj?.optString("searchEngine", "Google") ?: "Google") } catch (e: Exception) { SearchEngineOption.Google }

        val themeConfig = ThemeConfig(
            darkMode = darkMode,
            useDynamicColors = useDynamicColors,
            primaryColorArgb = primaryColorArgb,
            iconShape = iconShape,
            pageTransition = pageTransition,
            selectedIconPackPackage = selectedIconPackPackage,
            animationSpeedMultiplier = animationSpeedMultiplier,
            isParallaxEnabled = isParallaxEnabled,
            showNotificationBadges = showNotificationBadges,
            searchEngine = searchEngine
        )

        val userPreferences = UserPreferences(
            gridRows = gridRows,
            gridColumns = gridColumns,
            showAppLabels = showAppLabels,
            doubleTapToSleep = doubleTapToSleep,
            themeConfig = themeConfig
        )

        return BackupPayload(
            version = version,
            createdAt = createdAt,
            deviceName = deviceName,
            pages = pages,
            items = items,
            profiles = profiles,
            themeConfig = themeConfig,
            userPreferences = userPreferences
        )
    }

    private fun LauncherItemEntity.toDomain(): LauncherItem = when (itemType) {
        "FOLDER" -> {
            val list = mutableListOf<LauncherItem.AppItem>()
            try {
                val arr = JSONArray(folderItemsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        LauncherItem.AppItem(
                            id = obj.optLong("id", 0L),
                            packageName = obj.optString("packageName", ""),
                            activityName = obj.optString("activityName", ""),
                            label = obj.optString("label", "")
                        )
                    )
                }
            } catch (e: Exception) {
                // empty
            }
            LauncherItem.FolderItem(
                id = id,
                pageIndex = pageIndex,
                cellX = cellX,
                cellY = cellY,
                spanX = spanX,
                spanY = spanY,
                title = title.ifEmpty { "Klasör" },
                items = list
            )
        }
        "WIDGET_STACK" -> {
            val list = mutableListOf<LauncherItem.WidgetItem>()
            try {
                val arr = JSONArray(folderItemsJson)
                for (i in 0 until arr.length()) {
                    val sub = arr.getJSONObject(i)
                    list.add(
                        LauncherItem.WidgetItem(
                            id = sub.optLong("id", 0L),
                            pageIndex = pageIndex,
                            cellX = cellX,
                            cellY = cellY,
                            spanX = sub.optInt("spanX", spanX),
                            spanY = sub.optInt("spanY", spanY),
                            appWidgetId = sub.optInt("appWidgetId", -1),
                            providerPackage = sub.optString("providerPackage", ""),
                            providerClass = sub.optString("providerClass", "")
                        )
                    )
                }
            } catch (_: Exception) {}
            LauncherItem.WidgetStackItem(
                id = id,
                pageIndex = pageIndex,
                cellX = cellX,
                cellY = cellY,
                spanX = spanX,
                spanY = spanY,
                widgets = list
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
            customIconUri = customIconUri,
            popupWidgetId = if (appWidgetId != -1) appWidgetId else null
        )
    }

    private fun itemToEntity(item: LauncherItem): LauncherItemEntity = when (item) {
        is LauncherItem.AppItem -> LauncherItemEntity(
            id = item.id,
            pageIndex = item.pageIndex,
            cellX = item.cellX,
            cellY = item.cellY,
            spanX = item.spanX,
            spanY = item.spanY,
            itemType = "APP",
            packageName = item.packageName,
            activityName = item.activityName,
            title = item.label,
            customIconUri = item.customIconUri,
            appWidgetId = item.popupWidgetId ?: -1,
            isDockItem = item.pageIndex == -1
        )
        is LauncherItem.FolderItem -> {
            val arr = JSONArray()
            item.items.forEach { sub ->
                val obj = JSONObject()
                obj.put("id", sub.id)
                obj.put("packageName", sub.packageName)
                obj.put("activityName", sub.activityName)
                obj.put("label", sub.label)
                arr.put(obj)
            }
            LauncherItemEntity(
                id = item.id,
                pageIndex = item.pageIndex,
                cellX = item.cellX,
                cellY = item.cellY,
                spanX = item.spanX,
                spanY = item.spanY,
                itemType = "FOLDER",
                title = item.title,
                folderItemsJson = arr.toString(),
                isDockItem = item.pageIndex == -1
            )
        }
        is LauncherItem.WidgetStackItem -> {
            val arr = JSONArray()
            item.widgets.forEach { sub ->
                val obj = JSONObject()
                obj.put("id", sub.id)
                obj.put("appWidgetId", sub.appWidgetId)
                obj.put("providerPackage", sub.providerPackage)
                obj.put("providerClass", sub.providerClass)
                obj.put("spanX", sub.spanX)
                obj.put("spanY", sub.spanY)
                arr.put(obj)
            }
            LauncherItemEntity(
                id = item.id,
                pageIndex = item.pageIndex,
                cellX = item.cellX,
                cellY = item.cellY,
                spanX = item.spanX,
                spanY = item.spanY,
                itemType = "WIDGET_STACK",
                folderItemsJson = arr.toString(),
                isDockItem = false
            )
        }
        is LauncherItem.WidgetItem -> LauncherItemEntity(
            id = item.id,
            pageIndex = item.pageIndex,
            cellX = item.cellX,
            cellY = item.cellY,
            spanX = item.spanX,
            spanY = item.spanY,
            itemType = "WIDGET",
            appWidgetId = item.appWidgetId,
            isDockItem = false
        )
        is LauncherItem.ShortcutItem -> LauncherItemEntity(
            id = item.id,
            pageIndex = item.pageIndex,
            cellX = item.cellX,
            cellY = item.cellY,
            spanX = item.spanX,
            spanY = item.spanY,
            itemType = "SHORTCUT",
            title = item.label,
            isDockItem = item.pageIndex == -1
        )
    }

    private fun ProfileEntity.toDomain(): Profile {
        val profType = try { ProfileType.valueOf(type) } catch (e: Exception) { ProfileType.Normal }
        val blockedList = mutableListOf<String>()
        try {
            val arr = JSONArray(blockedPackagesJson)
            for (i in 0 until arr.length()) blockedList.add(arr.getString(i))
        } catch (e: Exception) {}

        val allowedList = mutableListOf<String>()
        try {
            val arr = JSONArray(allowedPackagesJson)
            for (i in 0 until arr.length()) allowedList.add(arr.getString(i))
        } catch (e: Exception) {}

        return Profile(
            id = id,
            name = name,
            type = profType,
            isActive = isActive,
            blockedPackages = blockedList,
            allowedPackages = allowedList,
            pinCode = pinCode,
            isScheduleEnabled = isScheduleEnabled,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            iconName = iconName
        )
    }
}
