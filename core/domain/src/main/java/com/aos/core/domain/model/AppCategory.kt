package com.aos.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppCategory(
    val title: String,
    val iconName: String
) {
    All("Tümü", "all"),
    Communication("İletişim", "chat"),
    Media("Medya", "play_circle"),
    Games("Oyunlar", "sports_esports"),
    Productivity("Üretkenlik", "work"),
    Internet("İnternet", "public"),
    Tools("Araçlar", "build"),
    Other("Diğer", "category")
}
