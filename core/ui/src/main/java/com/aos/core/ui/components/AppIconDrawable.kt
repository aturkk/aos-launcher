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

// Bounded LruCache icon cache to prevent OOM while keeping fast scrolling (L016)
object AppIconCache {
    private const val MAX_CACHE_ENTRIES = 150
    private val lruCache = LruCache<String, Bitmap>(MAX_CACHE_ENTRIES)

    @Synchronized
    fun get(packageName: String): Bitmap? = lruCache.get(packageName)

    @Synchronized
    fun put(packageName: String, bitmap: Bitmap) {
        lruCache.put(packageName, bitmap)
    }

    @Synchronized
    fun clear() {
        lruCache.evictAll()
    }
}

@Composable
fun AppIconImage(
    packageName: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val context = LocalContext.current
    val bitmap = remember(packageName) {
        loadAppIconBitmap(context, packageName)
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.size(size)
        )
    } else {
        Icon(
            imageVector = Icons.Default.Android,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = modifier.size(size)
        )
    }
}

private fun loadAppIconBitmap(context: Context, packageName: String): Bitmap? {
    if (packageName.isBlank()) return null
    AppIconCache.get(packageName)?.let { return it }

    return try {
        val pm = context.packageManager
        val drawable = pm.getApplicationIcon(packageName)
        val bitmap = drawableToBitmap(drawable)
        if (bitmap != null) {
            AppIconCache.put(packageName, bitmap)
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap? {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }

    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
