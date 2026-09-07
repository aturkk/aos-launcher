package com.aos.core.data.ai

import com.aos.core.common.result.Result
import com.aos.core.domain.model.AssistantResult
import com.aos.core.domain.model.CommandType
import com.aos.core.domain.repository.AppListRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalNluCommandParser @Inject constructor(
    private val appListRepository: AppListRepository
) {

    suspend fun parse(rawQuery: String): AssistantResult {
        val query = rawQuery.trim().lowercase(Locale.ROOT)
        if (query.isBlank()) {
            return AssistantResult(
                type = CommandType.Unrecognized,
                message = "Nasıl yardımcı olabilirim?"
            )
        }

        // 1. Lock Screen / Sleep
        if (isLockCommand(query)) {
            return AssistantResult(
                type = CommandType.LockScreen,
                message = "Ekran kilitleniyor..."
            )
        }

        // 2. Settings
        if (isSettingsCommand(query)) {
            return AssistantResult(
                type = CommandType.OpenSettings,
                message = "Ayarlar açılıyor..."
            )
        }

        // 3. Profile Switches
        val profileMatch = checkProfileCommand(query)
        if (profileMatch != null) {
            return profileMatch
        }

        // 4. App Launch patterns: "[uygulama] aç", "aç [uygulama]", "başlat [uygulama]", "open [app]", "launch [app]"
        val appMatch = checkAppLaunchCommand(query)
        if (appMatch != null) {
            return appMatch
        }

        // 5. Search patterns: "ara [x]", "google [x]", "search [x]", "[x] nedir", "[x] kimdir"
        val searchMatch = checkSearchCommand(rawQuery)
        if (searchMatch != null) {
            return searchMatch
        }

        // 6. Direct app name match without "aç": e.g. user just said "whatsapp" or "spotify" or "youtube"
        val directApp = findAppByName(query)
        if (directApp != null) {
            return AssistantResult(
                type = CommandType.OpenApp,
                payload = directApp.first, // packageName
                message = "${directApp.second} açılıyor..."
            )
        }

        // 7. Fallback to Web Search
        return AssistantResult(
            type = CommandType.Search,
            payload = rawQuery.trim(),
            message = "\"${rawQuery.trim()}\" için web'de aranıyor..."
        )
    }

    private fun isLockCommand(q: String): Boolean {
        return q == "ekranı kilitle" || q == "kilitle" || q == "ekranı kapat" ||
                q == "lock screen" || q == "lock" || q == "sleep" || q == "uyut" || q == "kapat"
    }

    private fun isSettingsCommand(q: String): Boolean {
        return q == "ayarları aç" || q == "ayarlar" || q == "ayarlara git" ||
                q == "open settings" || q == "settings" || q == "özelleştir" ||
                q == "tema" || q == "profil ayarları"
    }

    private fun checkProfileCommand(q: String): AssistantResult? {
        if (q.contains("iş modu") || q.contains("işe geç") || q.contains("work mode") || q.contains("çalışma modu")) {
            return AssistantResult(
                type = CommandType.SwitchProfile,
                payload = "2",
                message = "İş Moduna geçiliyor."
            )
        }
        if (q.contains("odak modu") || q.contains("odaklan") || q.contains("focus mode")) {
            return AssistantResult(
                type = CommandType.SwitchProfile,
                payload = "3",
                message = "Odak Moduna geçiliyor."
            )
        }
        if (q.contains("gece modu") || q.contains("geceye geç") || q.contains("night mode") || q.contains("karanlık mod")) {
            return AssistantResult(
                type = CommandType.SwitchProfile,
                payload = "4",
                message = "Gece Moduna geçiliyor."
            )
        }
        if (q.contains("çocuk modu") || q.contains("çocuk alanı") || q.contains("kids mode")) {
            return AssistantResult(
                type = CommandType.SwitchProfile,
                payload = "5",
                message = "Çocuk Moduna geçiliyor."
            )
        }
        if (q.contains("araç modu") || q.contains("araba modu") || q.contains("car mode") || q.contains("sürüş modu")) {
            return AssistantResult(
                type = CommandType.SwitchProfile,
                payload = "6",
                message = "Araç Moduna geçiliyor."
            )
        }
        if (q.contains("standart mod") || q.contains("normal mod") || q.contains("varsayılan mod") || q.contains("default mode")) {
            return AssistantResult(
                type = CommandType.SwitchProfile,
                payload = "1",
                message = "Standart profile dönülüyor."
            )
        }
        return null
    }

    private suspend fun checkAppLaunchCommand(q: String): AssistantResult? {
        val patterns = listOf(
            Regex("^(.*)\\s+(aç|ac|çalıştır|calistir|başlat|baslat)$"),
            Regex("^(aç|ac|çalıştır|calistir|başlat|baslat|open|launch)\\s+(.*)$")
        )

        for (pattern in patterns) {
            val match = pattern.find(q)
            if (match != null) {
                val candidateName = if (match.groupValues.size >= 3) {
                    if (match.groupValues[1] in listOf("aç", "ac", "çalıştır", "calistir", "başlat", "baslat", "open", "launch")) {
                        match.groupValues[2]
                    } else {
                        match.groupValues[1]
                    }
                } else {
                    ""
                }

                if (candidateName.isNotBlank()) {
                    val app = findAppByName(candidateName.trim())
                    if (app != null) {
                        return AssistantResult(
                            type = CommandType.OpenApp,
                            payload = app.first,
                            message = "${app.second} açılıyor..."
                        )
                    }
                }
            }
        }
        return null
    }

    private fun checkSearchCommand(raw: String): AssistantResult? {
        val lower = raw.lowercase(Locale.ROOT).trim()
        val searchPrefixes = listOf("ara ", "search ", "google ", "internette ara ", "webde ara ")
        for (prefix in searchPrefixes) {
            if (lower.startsWith(prefix)) {
                val query = raw.substring(prefix.length).trim()
                return AssistantResult(
                    type = CommandType.Search,
                    payload = query,
                    message = "\"$query\" web üzerinde aranıyor..."
                )
            }
        }
        if (lower.endsWith(" ara") || lower.endsWith(" arat")) {
            val query = raw.substring(0, raw.lastIndexOf(' ')).trim()
            return AssistantResult(
                type = CommandType.Search,
                payload = query,
                message = "\"$query\" web üzerinde aranıyor..."
            )
        }
        if (lower.endsWith(" nedir") || lower.endsWith(" kimdir") || lower.endsWith(" nasıldır")) {
            return AssistantResult(
                type = CommandType.Search,
                payload = raw,
                message = "\"$raw\" web üzerinde aranıyor..."
            )
        }
        return null
    }

    private suspend fun findAppByName(name: String): Pair<String, String>? {
        val result = appListRepository.getInstalledApps().firstOrNull()
        if (result is Result.Success) {
            val cleanName = name.replace("'", "").replace("’", "").lowercase(Locale.ROOT)
            val matched = result.data.firstOrNull { app ->
                val label = app.label.lowercase(Locale.ROOT)
                label == cleanName ||
                        label.startsWith(cleanName) ||
                        cleanName.startsWith(label) ||
                        app.packageName.lowercase(Locale.ROOT).contains(cleanName)
            }
            if (matched != null) {
                return Pair(matched.packageName, matched.label)
            }
        }
        return null
    }
}
