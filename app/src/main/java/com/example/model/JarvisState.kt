package com.example.model

enum class ListeningStatus(val label: String) {
    STANDBY("SYSTEM STANDBY"),
    LISTENING("ONLINE - LISTENING..."),
    PROCESSING("ANALYZING INTENT..."),
    SPEAKING("JARVIS TRANSMITTING..."),
    AWAITING_CONFIRMATION("AWAITING CONFIRMATION"),
    ERROR("SENSORS OFFLINE")
}

data class CommandHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val userQuery: String,
    val jarvisResponse: String,
    val actionType: IntentAction,
    val languageCode: String = "en",
    val isSuccess: Boolean = true,
    val executionDetails: String = ""
)

data class JarvisSettings(
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 0.95f,
    val selectedLanguageCode: String = "auto",
    val wakeWordEnabled: Boolean = false,
    val autoListenOnOpen: Boolean = true,
    val backgroundServiceEnabled: Boolean = false,
    val batterySaverMode: Boolean = false,
    val userTitle: String = "Sir"
)
