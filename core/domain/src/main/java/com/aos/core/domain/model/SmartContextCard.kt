package com.aos.core.domain.model

enum class SmartActionType {
    LaunchApp,
    SwitchProfile,
    SearchWeb,
    OpenSettings,
    LockScreen,
    None
}

data class SmartContextCard(
    val title: String,
    val subtitle: String,
    val badge: String,
    val iconName: String = "auto",
    val actionType: SmartActionType = SmartActionType.None,
    val actionPayload: String = "",
    val actionLabel: String = "Aç"
)
