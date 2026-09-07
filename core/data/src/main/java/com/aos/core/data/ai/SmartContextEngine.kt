package com.aos.core.data.ai

import com.aos.core.data.preferences.UserPreferencesDataStore
import com.aos.core.domain.model.ProfileType
import com.aos.core.domain.model.SmartActionType
import com.aos.core.domain.model.SmartContextCard
import com.aos.core.domain.repository.AiSuggestionRepository
import com.aos.core.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartContextEngine @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val aiSuggestionRepository: AiSuggestionRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {

    fun getSmartContextCardFlow(): Flow<SmartContextCard> {
        return combine(
            profileRepository.getActiveProfile(),
            aiSuggestionRepository.getSuggestedApps(limit = 1),
            userPreferencesDataStore.isAiSuggestionsEnabled
        ) { activeProfile, suggestions, isAiEnabled ->
            if (!isAiEnabled) {
                return@combine SmartContextCard(
                    title = "AOS Akıllı Asistan",
                    subtitle = "Yapay zeka ve öneriler kapalı. Etkinleştirmek için dokunun.",
                    badge = "ASİSTAN",
                    actionType = SmartActionType.OpenSettings,
                    actionPayload = "ai_settings",
                    actionLabel = "Ayarlar"
                )
            }

            val topApp = suggestions.firstOrNull()
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)

            // 1. Check if active profile has a specific dominant context
            when (activeProfile?.type) {
                ProfileType.Focus -> {
                    return@combine SmartContextCard(
                        title = "Odak Modu Devrede",
                        subtitle = "Dikkat dağıtıcılar engellendi. Verimli çalışmalar dileriz!",
                        badge = "VERİMLİLİK",
                        actionType = SmartActionType.SwitchProfile,
                        actionPayload = "1", // Switch back to Standard
                        actionLabel = "Tamamla"
                    )
                }
                ProfileType.Kids -> {
                    return@combine SmartContextCard(
                        title = "Çocuk Alanı Aktif",
                        subtitle = "Yalnızca izin verilen güvenli uygulamalar görünür.",
                        badge = "GÜVENLİK",
                        actionType = SmartActionType.SwitchProfile,
                        actionPayload = "1",
                        actionLabel = "Çıkış"
                    )
                }
                ProfileType.Car -> {
                    return@combine SmartContextCard(
                        title = "Araç Modu Aktif",
                        subtitle = "Güvenli sürüşler! Gözünüz yolda olsun.",
                        badge = "SÜRÜŞ",
                        actionType = if (topApp != null) SmartActionType.LaunchApp else SmartActionType.None,
                        actionPayload = topApp?.packageName ?: "",
                        actionLabel = if (topApp != null) "Aç: ${topApp.label}" else "Aktif"
                    )
                }
                ProfileType.Night -> {
                    return@combine SmartContextCard(
                        title = "Gece Modu Etkin",
                        subtitle = "Göz yormayan karanlık tema ve sessiz bildirimler.",
                        badge = "GECE",
                        actionType = SmartActionType.LockScreen,
                        actionPayload = "",
                        actionLabel = "Ekranı Kapat"
                    )
                }
                ProfileType.Work -> {
                    if (topApp != null) {
                        return@combine SmartContextCard(
                            title = "İş Modu Aktif",
                            subtitle = "Sıradaki önerilen iş aracı: ${topApp.label}",
                            badge = "ÇALIŞMA",
                            actionType = SmartActionType.LaunchApp,
                            actionPayload = topApp.packageName,
                            actionLabel = "Aç"
                        )
                    }
                }
                else -> {}
            }

            // 2. Time-of-day contextual prediction
            when (hour) {
                in 6..11 -> {
                    if (topApp != null) {
                        SmartContextCard(
                            title = "Günaydın!",
                            subtitle = "Güne ${topApp.label} ile başlamak ister misiniz?",
                            badge = "SABAH RUTİNİ",
                            actionType = SmartActionType.LaunchApp,
                            actionPayload = topApp.packageName,
                            actionLabel = "Başlat"
                        )
                    } else {
                        SmartContextCard(
                            title = "Günaydın!",
                            subtitle = "Gününüz güzel ve verimli geçsin.",
                            badge = "SABAH",
                            actionType = SmartActionType.SwitchProfile,
                            actionPayload = "2", // Work profile
                            actionLabel = "İş Modu"
                        )
                    }
                }
                in 12..17 -> {
                    if (topApp != null) {
                        SmartContextCard(
                            title = "İş & Verimlilik",
                            subtitle = "Sık kullandığınız araç: ${topApp.label}",
                            badge = "ÖNERİ",
                            actionType = SmartActionType.LaunchApp,
                            actionPayload = topApp.packageName,
                            actionLabel = "Aç"
                        )
                    } else {
                        SmartContextCard(
                            title = "Verimlilik Zamanı",
                            subtitle = "Çalışma saatleri için İş Moduna geçebilirsiniz.",
                            badge = "VERİMLİLİK",
                            actionType = SmartActionType.SwitchProfile,
                            actionPayload = "2",
                            actionLabel = "İş Modu"
                        )
                    }
                }
                in 18..22 -> {
                    if (topApp != null) {
                        SmartContextCard(
                            title = "İyi Akşamlar!",
                            subtitle = "Günün yorgunluğunu ${topApp.label} ile atın.",
                            badge = "DİNLENME",
                            actionType = SmartActionType.LaunchApp,
                            actionPayload = topApp.packageName,
                            actionLabel = "Aç"
                        )
                    } else {
                        SmartContextCard(
                            title = "İyi Akşamlar!",
                            subtitle = "Huzurlu ve dinlendirici bir akşam dileriz.",
                            badge = "DİNLENME",
                            actionType = SmartActionType.SwitchProfile,
                            actionPayload = "4", // Night profile
                            actionLabel = "Gece Modu"
                        )
                    }
                }
                else -> {
                    // 23..5 Night
                    SmartContextCard(
                        title = "İyi Geceler",
                        subtitle = "Dinlenme vakti geldi. Ekranı kilitleyip dinlenebilirsiniz.",
                        badge = "GECE",
                        actionType = SmartActionType.LockScreen,
                        actionPayload = "",
                        actionLabel = "Ekranı Kilitle"
                    )
                }
            }
        }
    }
}
