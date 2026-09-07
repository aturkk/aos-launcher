package com.aos.core.domain.model

enum class CommandType {
    OpenApp,
    SwitchProfile,
    Search,
    LockScreen,
    OpenSettings,
    Unrecognized
}

data class AssistantResult(
    val type: CommandType,
    val payload: String = "",
    val message: String
)
