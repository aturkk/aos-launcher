package com.aos.core.data.category

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.aos.core.domain.model.AppCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCategorizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager get() = context.packageManager
    fun categorize(packageName: String, label: String = ""): AppCategory {
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)

            // 1. Android Native ApplicationInfo.category (API 26+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (appInfo.category) {
                    ApplicationInfo.CATEGORY_GAME -> return AppCategory.Games
                    ApplicationInfo.CATEGORY_AUDIO,
                    ApplicationInfo.CATEGORY_VIDEO,
                    ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.Media
                    ApplicationInfo.CATEGORY_SOCIAL,
                    ApplicationInfo.CATEGORY_NEWS -> return AppCategory.Communication
                    ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.Productivity
                    ApplicationInfo.CATEGORY_MAPS -> return AppCategory.Internet
                }
            }
        } catch (_: Exception) {}

        // 2. Heuristic package and label matching
        val target = (packageName + " " + label).lowercase()

        return when {
            // Communication & Social
            target.containsAny(
                "whatsapp", "telegram", "signal", "messenger", "instagram", "facebook",
                "twitter", "tiktok", "snapchat", "discord", "viber", "wechat", "line",
                "dialer", "contact", "phone", "sms", "mms", "message", "sohbet", "mesaj"
            ) -> AppCategory.Communication

            // Media & Entertainment
            target.containsAny(
                "youtube", "spotify", "netflix", "music", "player", "video", "sound",
                "audio", "gallery", "photo", "camera", "cinema", "tv", "radio", "podcast",
                "primevideo", "disney", "twitch", "film", "muzik", "kamera", "galeri"
            ) -> AppCategory.Media

            // Games
            target.containsAny(
                "game", "play", "puzzle", "racing", "craft", "clash", "candy", "arcade",
                "pubg", "roblox", "steam", "epicgames", "oyun", "simulator"
            ) -> AppCategory.Games

            // Productivity
            target.containsAny(
                "office", "docs", "sheets", "slides", "excel", "word", "powerpoint", "pdf",
                "notes", "keep", "drive", "cloud", "dropbox", "mail", "gmail", "outlook",
                "calendar", "todo", "task", "trello", "notion", "slack", "teams", "zoom",
                "meet", "notlar", "ajanda", "takvim", "belge"
            ) -> AppCategory.Productivity

            // Internet, Browsers & Shopping
            target.containsAny(
                "chrome", "browser", "firefox", "opera", "edge", "safari", "tarayici",
                "amazon", "ebay", "shopping", "trendyol", "hepsiburada", "aliexpress",
                "booking", "airbnb", "trip", "uber", "map", "navigation", "harita", "hava"
            ) -> AppCategory.Internet

            // Tools & Utilities
            target.containsAny(
                "settings", "calculator", "clock", "alarm", "file", "explorer", "cleaner",
                "antivirus", "vpn", "terminal", "backup", "compass", "recorder", "flashlight",
                "ayar", "hesap", "saat", "dosya", "fener", "arac"
            ) -> AppCategory.Tools

            else -> AppCategory.Other
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        for (kw in keywords) {
            if (this.contains(kw)) return true
        }
        return false
    }
}
