package com.example.model

import android.content.Intent

enum class IntentAction {
    OPEN_APP,
    WHATSAPP_MESSAGE,
    PHONE_CALL,
    FLASHLIGHT,
    CHECK_TIME_DATE,
    SEARCH_WEB,
    OPEN_WEBSITE,
    CREATE_REMINDER,
    OPEN_SETTINGS,
    ASK_CLARIFICATION,
    GENERAL_CONVERSATION
}

data class AiIntentResult(
    val action: IntentAction,
    val spokenResponse: String,
    val detectedLanguage: String = "en",
    val appName: String? = null,
    val contactName: String? = null,
    val phoneNumber: String? = null,
    val messageText: String? = null,
    val searchQuery: String? = null,
    val websiteUrl: String? = null,
    val reminderTitle: String? = null,
    val reminderMinutes: Int? = null,
    val flashlightEnable: Boolean? = null,
    val settingType: String? = null,
    val requiresConfirmation: Boolean = false,
    val confirmationPrompt: String? = null
)

data class PendingAction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val action: IntentAction,
    val title: String,
    val recipient: String? = null,
    val content: String? = null,
    val confirmButtonText: String = "CONFIRM & EXECUTE",
    val intentToLaunch: Intent? = null
)
