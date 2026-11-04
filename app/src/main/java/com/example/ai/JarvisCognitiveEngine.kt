package com.example.ai

import android.util.Log
import com.example.model.AiIntentResult
import com.example.model.IntentAction
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

class JarvisCognitiveEngine {
    private val TAG = "JarvisCognitiveEngine"

    private val intentSchema = Schema.obj(
        properties = mapOf(
            "action" to Schema.enumeration(
                listOf(
                    "OPEN_APP",
                    "WHATSAPP_MESSAGE",
                    "PHONE_CALL",
                    "FLASHLIGHT",
                    "CHECK_TIME_DATE",
                    "SEARCH_WEB",
                    "OPEN_WEBSITE",
                    "CREATE_REMINDER",
                    "OPEN_SETTINGS",
                    "ASK_CLARIFICATION",
                    "GENERAL_CONVERSATION"
                )
            ),
            "spokenResponse" to Schema.string(
                description = "Concise spoken response in the user's spoken language (1-2 sentences), ready for Text-to-Speech without asterisks or markdown"
            ),
            "detectedLanguage" to Schema.string(
                description = "Two-letter ISO language code: en, ta, hi, te, kn, ml, bn, mr, gu, pa, ur, ar, fr, es, ja"
            ),
            "appName" to Schema.string(description = "Name of the app requested to launch"),
            "contactName" to Schema.string(description = "Name of contact to call or message"),
            "phoneNumber" to Schema.string(description = "Explicit phone number digits if mentioned"),
            "messageText" to Schema.string(description = "Extracted message body to send"),
            "searchQuery" to Schema.string(description = "Web search terms"),
            "websiteUrl" to Schema.string(description = "Website URL to open"),
            "reminderTitle" to Schema.string(description = "Title of timer or reminder"),
            "reminderMinutes" to Schema.integer(description = "Duration in minutes"),
            "flashlightEnable" to Schema.boolean(description = "True for on, False for off"),
            "settingType" to Schema.string(description = "wifi, bluetooth, sound, display, voice_assistant"),
            "requiresConfirmation" to Schema.boolean(description = "True for messaging or placing calls"),
            "missingInformationPrompt" to Schema.string(description = "Question to ask user if essential details are missing")
        ),
        optionalProperties = listOf(
            "appName",
            "contactName",
            "phoneNumber",
            "messageText",
            "searchQuery",
            "websiteUrl",
            "reminderTitle",
            "reminderMinutes",
            "flashlightEnable",
            "settingType",
            "missingInformationPrompt"
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are J.A.R.V.I.S., the ultimate intelligent cognitive assistant for Android.
                    You do NOT rely on rigid keywords; you understand natural human intent, context, and code-switching across 15 languages:
                    English, Tamil, Hindi, Telugu, Kannada, Malayalam, Bengali, Marathi, Gujarati, Punjabi, Urdu, Arabic, French, Spanish, Japanese.
                    Support mixed regional speech seamlessly (e.g. Tanglish: "Rahul ku WhatsApp la message anupu", Hinglish: "WhatsApp pe Rahul ko message bhejo").

                    Analyze the user's request and classify into the most appropriate action:
                    1. OPEN_APP: User wants to launch or open ANY installed app (e.g. "Open YouTube", "Launch Instagram", "Start Google Maps", "Open Calculator", "Play Spotify", "Open Settings"). Extract the appName.
                    2. WHATSAPP_MESSAGE: User wants to message someone on WhatsApp (e.g. "Open WhatsApp and send Rahul a message saying I will arrive tomorrow", "Rahul ko WhatsApp pe message karo kal milte hain"). Extract contactName and messageText. Set requiresConfirmation = true. If recipient or message text is missing, choose ASK_CLARIFICATION.
                    3. PHONE_CALL: User wants to call or dial someone (e.g. "Call Rahul", "Mom ko phone lagao", "Dial 9876543210"). Extract contactName and/or phoneNumber. Set requiresConfirmation = true.
                    4. FLASHLIGHT: User wants to control the phone flashlight/torch. Extract flashlightEnable (true/false).
                    5. CHECK_TIME_DATE: User asks for current time or date.
                    6. SEARCH_WEB: User wants to look up or search something online. Extract searchQuery.
                    7. OPEN_WEBSITE: User wants to open a web address. Extract websiteUrl.
                    8. CREATE_REMINDER: User wants to set an alarm, reminder, or timer (e.g. "Remind me in 15 minutes to call Tony"). Extract reminderTitle and reminderMinutes.
                    9. OPEN_SETTINGS: User wants to open device settings (wifi, bluetooth, sound, display, voice assistant). Extract settingType.
                    10. ASK_CLARIFICATION: If the user wants an action but an essential detail is missing (e.g. "Send a WhatsApp message" without recipient, or "Call" without a name).
                    11. GENERAL_CONVERSATION: User is chatting, asking questions (e.g. "Explain Python", "Help me plan my day", "Who built you?", telling a joke).

                    CRITICAL RULES:
                    - Always formulate spokenResponse in the EXACT language/dialect spoken by the user (or English with user's title if language is English).
                    - Address the user respectfully as 'Sir' (or cultural equivalent).
                    - Keep spokenResponse concise (1 to 2 sentences) so it can be spoken cleanly via Android Text-to-Speech without asterisks, emojis, or markdown tags.
                    """.trimIndent()
                )
            },
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = intentSchema
                temperature = 0.4f
            }
        )
    }

    suspend fun analyzeIntent(
        userPrompt: String,
        conversationContext: List<String> = emptyList(),
        preferredLanguage: String = "auto",
        userTitle: String = "Sir"
    ): AiIntentResult {
        return try {
            withTimeout(35_000L) {
                val contextPrompt = if (conversationContext.isNotEmpty()) {
                    "Previous conversation turns:\n" + conversationContext.takeLast(3).joinToString("\n") + "\n\n"
                } else ""

                val fullPrompt = "${contextPrompt}User ($userTitle, preferred language: $preferredLanguage) says: $userPrompt"
                val response = model.generateContent(fullPrompt)
                val jsonString = response.text ?: throw IllegalStateException("Empty response from AI engine")
                parseIntentJson(jsonString, userTitle)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI engine inference timed out", e)
            AiIntentResult(
                action = IntentAction.GENERAL_CONVERSATION,
                spokenResponse = "Apologies, $userTitle, neural matrix processing timed out. Please repeat your instruction."
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI engine analysis failed", e)
            // Graceful fallback to smart local heuristic
            fallbackHeuristic(userPrompt, userTitle)
        }
    }

    private fun parseIntentJson(jsonString: String, userTitle: String): AiIntentResult {
        val json = JSONObject(jsonString)
        val actionName = json.optString("action", "GENERAL_CONVERSATION")
        val action = try {
            IntentAction.valueOf(actionName)
        } catch (e: Exception) {
            IntentAction.GENERAL_CONVERSATION
        }

        val spokenResponse = json.optString("spokenResponse", "Understood, $userTitle.")
        val detectedLanguage = json.optString("detectedLanguage", "en")
        val appName = json.optString("appName").takeIf { it.isNotBlank() }
        val contactName = json.optString("contactName").takeIf { it.isNotBlank() }
        val phoneNumber = json.optString("phoneNumber").takeIf { it.isNotBlank() }
        val messageText = json.optString("messageText").takeIf { it.isNotBlank() }
        val searchQuery = json.optString("searchQuery").takeIf { it.isNotBlank() }
        val websiteUrl = json.optString("websiteUrl").takeIf { it.isNotBlank() }
        val reminderTitle = json.optString("reminderTitle").takeIf { it.isNotBlank() }
        val reminderMinutes = if (json.has("reminderMinutes")) json.optInt("reminderMinutes") else null
        val flashlightEnable = if (json.has("flashlightEnable")) json.optBoolean("flashlightEnable") else null
        val settingType = json.optString("settingType").takeIf { it.isNotBlank() }
        val requiresConfirmation = json.optBoolean("requiresConfirmation", action == IntentAction.WHATSAPP_MESSAGE || action == IntentAction.PHONE_CALL)
        val missingPrompt = json.optString("missingInformationPrompt").takeIf { it.isNotBlank() }

        return AiIntentResult(
            action = action,
            spokenResponse = spokenResponse,
            detectedLanguage = detectedLanguage,
            appName = appName,
            contactName = contactName,
            phoneNumber = phoneNumber,
            messageText = messageText,
            searchQuery = searchQuery,
            websiteUrl = websiteUrl,
            reminderTitle = reminderTitle,
            reminderMinutes = reminderMinutes,
            flashlightEnable = flashlightEnable,
            settingType = settingType,
            requiresConfirmation = requiresConfirmation,
            confirmationPrompt = missingPrompt
        )
    }

    private fun fallbackHeuristic(userPrompt: String, userTitle: String): AiIntentResult {
        val lower = userPrompt.trim().lowercase()
        return when {
            lower.contains("whatsapp") && (lower.contains("message") || lower.contains("send")) -> {
                AiIntentResult(
                    action = IntentAction.WHATSAPP_MESSAGE,
                    spokenResponse = "Preparing WhatsApp communication, $userTitle.",
                    messageText = userPrompt,
                    requiresConfirmation = true
                )
            }
            lower.startsWith("call ") || lower.startsWith("dial ") -> {
                val target = lower.removePrefix("call ").removePrefix("dial ").trim()
                AiIntentResult(
                    action = IntentAction.PHONE_CALL,
                    contactName = target,
                    spokenResponse = "Opening communication link to $target, $userTitle.",
                    requiresConfirmation = true
                )
            }
            lower.contains("flashlight on") || lower.contains("turn on flashlight") -> {
                AiIntentResult(
                    action = IntentAction.FLASHLIGHT,
                    flashlightEnable = true,
                    spokenResponse = "Illumination activated, $userTitle."
                )
            }
            lower.contains("flashlight off") || lower.contains("turn off flashlight") -> {
                AiIntentResult(
                    action = IntentAction.FLASHLIGHT,
                    flashlightEnable = false,
                    spokenResponse = "Illumination deactivated, $userTitle."
                )
            }
            lower.startsWith("open ") || lower.startsWith("launch ") -> {
                val target = lower.removePrefix("open ").removePrefix("launch ").trim()
                AiIntentResult(
                    action = IntentAction.OPEN_APP,
                    appName = target,
                    spokenResponse = "Launching $target now, $userTitle."
                )
            }
            else -> {
                AiIntentResult(
                    action = IntentAction.GENERAL_CONVERSATION,
                    spokenResponse = "At your command, $userTitle. How may I assist you further?"
                )
            }
        }
    }
}
