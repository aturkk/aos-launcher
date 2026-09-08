package com.aos.core.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.util.LruCache
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.aos.core.ui.theme.SquircleShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.net.Uri
import androidx.core.content.res.ResourcesCompat

// Bounded LruCache icon cache to prevent OOM while keeping fast scrolling
object AppIconCache {
    private const val MAX_CACHE_ENTRIES = 400
    private val lruCache = LruCache<String, Bitmap>(MAX_CACHE_ENTRIES)

    @Synchronized
    fun get(key: String): Bitmap? = lruCache.get(key)

    @Synchronized
    fun put(key: String, bitmap: Bitmap) {
        lruCache.put(key, bitmap)
    }

    @Synchronized
    fun clear() {
        lruCache.evictAll()
    }

    fun preload(context: Context, packageName: String) {
        if (get(packageName) != null) return
        loadAppIconBitmap(context, packageName)
    }
}

@Composable
fun AppIconImage(
    packageName: String,
    modifier: Modifier = Modifier,
    iconPackPackage: String? = null,
    iconUri: String? = null,
    contentDescription: String? = null,
    size: Dp = 44.dp
) {
    val context = LocalContext.current
    val cacheKey = remember(packageName, iconPackPackage, iconUri) {
        when {
            !iconUri.isNullOrBlank() -> "uri:$iconUri"
            !iconPackPackage.isNullOrBlank() -> "pack:$iconPackPackage:$packageName"
            else -> packageName
        }
    }
    val cachedBitmap = remember(cacheKey) { AppIconCache.get(cacheKey) }
    val bitmapState = produceState(initialValue = cachedBitmap, key1 = cacheKey) {
        if (value == null && packageName.isNotBlank()) {
            val loaded = withContext(Dispatchers.IO) {
                loadAppIconBitmap(context, packageName, iconPackPackage, iconUri)
            }
            value = loaded
        }
    }

    val bitmap = bitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier.size(size)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(SquircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

internal fun loadAppIconBitmap(
    context: Context,
    packageName: String,
    iconPackPackage: String? = null,
    iconUri: String? = null
): Bitmap? {
    if (packageName.isBlank()) return null
    val cacheKey = when {
        !iconUri.isNullOrBlank() -> "uri:$iconUri"
        !iconPackPackage.isNullOrBlank() -> "pack:$iconPackPackage:$packageName"
        else -> packageName
    }
    AppIconCache.get(cacheKey)?.let { return it }

    return try {
        val drawable = when {
            !iconUri.isNullOrBlank() -> {
                val uri = Uri.parse(iconUri)
                val stream = context.contentResolver.openInputStream(uri)
                val bmp = android.graphics.BitmapFactory.decodeStream(stream)
                stream?.close()
                if (bmp != null) BitmapDrawable(context.resources, bmp) else null
            }
            !iconPackPackage.isNullOrBlank() -> {
                try {
                    val iconPackRes = context.packageManager.getResourcesForApplication(iconPackPackage)
                    val formatted = packageName.replace(".", "_").lowercase()
                    var resId = iconPackRes.getIdentifier(formatted, "drawable", iconPackPackage)
                    if (resId == 0) {
                        resId = iconPackRes.getIdentifier("icon_$formatted", "drawable", iconPackPackage)
                    }
                    if (resId != 0) {
                        ResourcesCompat.getDrawable(iconPackRes, resId, null)
                    } else null
                } catch (_: Exception) {
                    null
                }
            }
            else -> null
        } ?: context.packageManager.getApplicationIcon(packageName)

        val bitmap = drawableToBitmap(drawable)
        if (bitmap != null) {
            AppIconCache.put(cacheKey, bitmap)
        }
        bitmap
    } catch (_: Exception) {
        null
    }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap? {
    val targetMax = 144
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        val bmp = drawable.bitmap
        return if (bmp.width > targetMax || bmp.height > targetMax) {
            Bitmap.createScaledBitmap(bmp, targetMax, targetMax, true)
        } else {
            bmp
        }
    }

    val rawWidth = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else targetMax
    val rawHeight = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else targetMax

    val (width, height) = if (rawWidth > targetMax || rawHeight > targetMax) {
        val aspect = rawWidth.toFloat() / rawHeight.toFloat()
        if (aspect >= 1f) targetMax to (targetMax / aspect).toInt().coerceAtLeast(1)
        else (targetMax * aspect).toInt().coerceAtLeast(1) to targetMax
    } else {
        rawWidth to rawHeight
    }

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
