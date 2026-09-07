package com.aos.core.data.iconpack

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.aos.core.domain.model.IconPack
import com.aos.core.domain.repository.IconPackRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconPackManager @Inject constructor(
    @ApplicationContext private val context: Context
) : IconPackRepository {
    private val iconCache = ConcurrentHashMap<String, Drawable>()

    private val supportedActions = listOf(
        "org.adw.launcher.THEMES",
        "com.novalauncher.THEME",
        "com.anddoes.launcher.THEME",
        "com.gau.go.launcherex.theme"
    )

    override suspend fun getInstalledIconPacks(): List<IconPack> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packs = mutableMapOf<String, IconPack>()

        for (action in supportedActions) {
            val intent = Intent(action)
            val resolveInfos = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (!packs.containsKey(pkg)) {
                    val label = info.loadLabel(pm).toString()
                    packs[pkg] = IconPack(packageName = pkg, label = label)
                }
            }
        }

        packs.values.sortedBy { it.label }
    }

    fun loadIcon(iconPackPackage: String, targetPackage: String): Drawable? {
        val cacheKey = "$iconPackPackage:$targetPackage"
        iconCache[cacheKey]?.let { return it }

        val pm = context.packageManager
        return try {
            val iconPackResources = pm.getResourcesForApplication(iconPackPackage)
            val formattedDrawableName = targetPackage.replace(".", "_").lowercase()

            var resId = iconPackResources.getIdentifier(formattedDrawableName, "drawable", iconPackPackage)
            if (resId == 0) {
                resId = iconPackResources.getIdentifier("icon_$formattedDrawableName", "drawable", iconPackPackage)
            }

            if (resId != 0) {
                val drawable = ResourcesCompat.getDrawable(iconPackResources, resId, null)
                if (drawable != null) {
                    iconCache[cacheKey] = drawable
                }
                drawable
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
