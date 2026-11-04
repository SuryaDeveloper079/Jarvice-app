package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.JarvisCognitiveEngine
import com.example.data.local.ConversationEntity
import com.example.data.local.JarvisDatabase
import com.example.data.repository.JarvisRepository
import com.example.manager.AppDiscoveryManager
import com.example.manager.FlashlightManager
import com.example.manager.PhoneAutomationManager
import com.example.manager.TtsManager
import com.example.model.AiIntentResult
import com.example.model.CommandHistoryItem
import com.example.model.IntentAction
import com.example.model.JarvisLanguage
import com.example.model.JarvisSettings
import com.example.model.ListeningStatus
import com.example.model.PendingAction
import com.example.service.JarvisForegroundService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "JarvisViewModel"
    private val context: Context get() = getApplication<Application>().applicationContext

    // Local Persistence Database & Repository
    private val database = JarvisDatabase.getDatabase(context)
    val repository = JarvisRepository(database.conversationDao(), database.userPreferenceDao())

    // Managers & Cognitive Engine
    val ttsManager = TtsManager(context)
    val flashlightManager = FlashlightManager(context)
    val appDiscoveryManager = AppDiscoveryManager(context)
    val phoneAutomationManager = PhoneAutomationManager(context, flashlightManager, appDiscoveryManager)
    val cognitiveEngine = JarvisCognitiveEngine()

    // State flows
    private val _status = MutableStateFlow(ListeningStatus.STANDBY)
    val status: StateFlow<ListeningStatus> = _status.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    private val _lastResponse = MutableStateFlow("Cognitive matrix online. Listening for your instruction, Sir.")
    val lastResponse: StateFlow<String> = _lastResponse.asStateFlow()

    private val _commandHistory = MutableStateFlow<List<CommandHistoryItem>>(emptyList())
    val commandHistory: StateFlow<List<CommandHistoryItem>> = _commandHistory.asStateFlow()

    private val _settings = MutableStateFlow(JarvisSettings())
    val settings: StateFlow<JarvisSettings> = _settings.asStateFlow()

    private val _pendingAction = MutableStateFlow<PendingAction?>(null)
    val pendingAction: StateFlow<PendingAction?> = _pendingAction.asStateFlow()

    // Event flow for launching Intents
    private val _navigationEvents = MutableSharedFlow<Intent>(extraBufferCapacity = 5)
    val navigationEvents: SharedFlow<Intent> = _navigationEvents.asSharedFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isManualStop = false

    init {
        initSpeechRecognizer()
        loadPersistedData()
    }

    private fun loadPersistedData() {
        viewModelScope.launch {
            // Load saved conversations from Room DB
            repository.conversations.collect { entities ->
                _commandHistory.value = entities.map { entity ->
                    val action = try {
                        IntentAction.valueOf(entity.intentType)
                    } catch (e: Exception) {
                        IntentAction.GENERAL_CONVERSATION
                    }
                    CommandHistoryItem(
                        id = entity.id.toString(),
                        timestamp = entity.timestamp,
                        userQuery = entity.userQuery,
                        jarvisResponse = entity.aiResponse,
                        actionType = action,
                        languageCode = entity.languageCode,
                        isSuccess = entity.executionStatus != "FAILED",
                        executionDetails = entity.toolParameters
                    )
                }
            }
        }

        viewModelScope.launch {
            // Load saved user preferences from Room DB
            val savedTitle = repository.getPreferenceDirect("user_title") ?: "Sir"
            val savedPitch = repository.getPreferenceDirect("speech_pitch")?.toFloatOrNull() ?: 0.95f
            val savedRate = repository.getPreferenceDirect("speech_rate")?.toFloatOrNull() ?: 1.0f
            val savedLang = repository.getPreferenceDirect("selected_language") ?: "auto"
            val savedWakeWord = repository.getPreferenceDirect("wake_word") == "true"
            val savedBg = repository.getPreferenceDirect("bg_listening") == "true"

            val updatedSettings = JarvisSettings(
                userTitle = savedTitle,
                speechPitch = savedPitch,
                speechRate = savedRate,
                selectedLanguageCode = savedLang,
                wakeWordEnabled = savedWakeWord,
                backgroundServiceEnabled = savedBg
            )
            _settings.value = updatedSettings
            ttsManager.updateSettings(savedPitch, savedRate)
        }
    }

    private fun initSpeechRecognizer() {
        mainHandler.post {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createRecognitionListener())
                }
            } else {
                Log.e(TAG, "Speech recognition not available")
                _status.value = ListeningStatus.ERROR
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _status.value = ListeningStatus.LISTENING
                _transcription.value = "Audio sensors active... Speak naturally."
            }

            override fun onBeginningOfSpeech() {
                _transcription.value = "Voice frequency detected..."
            }

            override fun onRmsChanged(rmsdB: Float) {
                _rmsDb.value = rmsdB.coerceAtLeast(0f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _status.value = ListeningStatus.PROCESSING
                _rmsDb.value = 0f
            }

            override fun onError(error: Int) {
                _rmsDb.value = 0f
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timeout."
                    SpeechRecognizer.ERROR_AUDIO -> "Audio hardware issue."
                    SpeechRecognizer.ERROR_NETWORK -> "Network connectivity issue."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                    else -> "Speech recognition interrupted."
                }

                if (!isManualStop && _settings.value.wakeWordEnabled) {
                    mainHandler.postDelayed({
                        if (!isManualStop && _status.value != ListeningStatus.SPEAKING && _status.value != ListeningStatus.AWAITING_CONFIRMATION) {
                            startListeningInternal()
                        }
                    }, 1200)
                } else if (!isManualStop && _status.value == ListeningStatus.LISTENING) {
                    _status.value = ListeningStatus.STANDBY
                    _transcription.value = errorMessage
                }
            }

            override fun onResults(results: Bundle?) {
                _rmsDb.value = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull()?.trim()

                if (!recognizedText.isNullOrBlank()) {
                    _transcription.value = recognizedText
                    processNaturalLanguageInput(recognizedText)
                } else {
                    if (!isManualStop && _settings.value.wakeWordEnabled) {
                        mainHandler.postDelayed({ startListeningInternal() }, 800)
                    } else {
                        _status.value = ListeningStatus.STANDBY
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let {
                    _transcription.value = it
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startJarvis() {
        isManualStop = false
        if (_settings.value.backgroundServiceEnabled) {
            JarvisForegroundService.start(context)
        }
        startListeningInternal()
    }

    private fun startListeningInternal() {
        mainHandler.post {
            try {
                val selectedLang = _settings.value.selectedLanguageCode
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    if (selectedLang != "auto") {
                        val locale = JarvisLanguage.findByCode(selectedLang).locale
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString())
                    } else {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                    }
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }
                speechRecognizer?.startListening(intent)
                _status.value = ListeningStatus.LISTENING
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start SpeechRecognizer", e)
                _status.value = ListeningStatus.ERROR
            }
        }
    }

    fun stopJarvis() {
        isManualStop = true
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling speech recognizer", e)
            }
        }
        ttsManager.stop()
        if (_settings.value.backgroundServiceEnabled) {
            JarvisForegroundService.stop(context)
        }
        _status.value = ListeningStatus.STANDBY
        _rmsDb.value = 0f
        _pendingAction.value = null
        _transcription.value = "Sensors standing by."
    }

    fun processNaturalLanguageInput(input: String) {
        viewModelScope.launch {
            _status.value = ListeningStatus.PROCESSING
            val currentSettings = _settings.value

            // 1. Context from previous conversations
            val recentContext = _commandHistory.value.take(4).map {
                "User: ${it.userQuery} -> JARVIS: ${it.jarvisResponse}"
            }

            // 2. Cognitive AI Intent Reasoning (Gemini 3.8 Flash structured engine)
            val intentResult = cognitiveEngine.analyzeIntent(
                userPrompt = input,
                conversationContext = recentContext,
                preferredLanguage = currentSettings.selectedLanguageCode,
                userTitle = currentSettings.userTitle
            )

            // 3. Dynamic Tool Execution & Safety Confirmation
            executeIntent(input, intentResult)
        }
    }

    private suspend fun executeIntent(originalQuery: String, result: AiIntentResult) {
        val userTitle = _settings.value.userTitle
        var finalSpokenResponse = result.spokenResponse
        var executionStatus = "SUCCESS"
        var toolDetails = ""

        when (result.action) {
            IntentAction.OPEN_APP -> {
                val targetAppName = result.appName ?: originalQuery
                val appInfo = appDiscoveryManager.findApp(targetAppName)
                if (appInfo != null) {
                    val launchIntent = appDiscoveryManager.getLaunchIntent(appInfo)
                    if (launchIntent != null) {
                        _navigationEvents.tryEmit(launchIntent)
                        finalSpokenResponse = "Opening ${appInfo.appName} now, $userTitle."
                        toolDetails = "Launched ${appInfo.packageName}"
                    } else {
                        finalSpokenResponse = "I located ${appInfo.appName}, but could not acquire a launch intent, $userTitle."
                        executionStatus = "FAILED"
                    }
                } else {
                    finalSpokenResponse = "I searched installed applications for '$targetAppName', but it is not installed on this device, $userTitle."
                    executionStatus = "FAILED"
                    toolDetails = "App not found: $targetAppName"
                }
            }

            IntentAction.WHATSAPP_MESSAGE -> {
                // Section 4 & 10: Show recipient and message before sending. Require user confirmation.
                val contactName = result.contactName ?: "recipient"
                val messageText = result.messageText ?: originalQuery
                val pending = phoneAutomationManager.prepareWhatsAppMessage(
                    recipientName = contactName,
                    messageBody = messageText,
                    explicitPhone = result.phoneNumber
                )
                _pendingAction.value = pending
                _status.value = ListeningStatus.AWAITING_CONFIRMATION
                finalSpokenResponse = "I have prepared the WhatsApp message for ${pending.recipient}: \"$messageText\". Please confirm to transmit, $userTitle."
                executionStatus = "CONFIRMATION_REQUIRED"
                toolDetails = "Pending confirmation for WhatsApp: ${pending.recipient}"
            }

            IntentAction.PHONE_CALL -> {
                // Section 5 & 10: Require user confirmation before placing calls.
                val target = result.contactName ?: result.phoneNumber ?: "contact"
                val pending = phoneAutomationManager.preparePhoneCall(
                    targetName = target,
                    explicitPhone = result.phoneNumber
                )
                _pendingAction.value = pending
                _status.value = ListeningStatus.AWAITING_CONFIRMATION
                finalSpokenResponse = "Communication link to ${pending.recipient} is ready. Tap confirm to open the dialer, $userTitle."
                executionStatus = "CONFIRMATION_REQUIRED"
                toolDetails = "Pending dialer confirmation for: ${pending.recipient}"
            }

            IntentAction.FLASHLIGHT -> {
                val enable = result.flashlightEnable ?: !flashlightManager.isTorchOn.value
                val success = flashlightManager.setTorch(enable)
                finalSpokenResponse = if (success) {
                    if (enable) "Tactical illumination activated, $userTitle." else "Illumination deactivated, $userTitle."
                } else {
                    "Unable to access the camera flash unit, $userTitle."
                }
                toolDetails = "Flashlight set to $enable (success: $success)"
            }

            IntentAction.CHECK_TIME_DATE -> {
                val (timeStr, dateStr) = phoneAutomationManager.getFormattedTimeAndDate(result.detectedLanguage)
                finalSpokenResponse = "The current time is $timeStr, and today is $dateStr, $userTitle."
                toolDetails = "Time: $timeStr, Date: $dateStr"
            }

            IntentAction.SEARCH_WEB -> {
                val query = result.searchQuery ?: originalQuery
                val searchIntent = phoneAutomationManager.searchWeb(query)
                _navigationEvents.tryEmit(searchIntent)
                finalSpokenResponse = "Searching global databases for \"$query\", $userTitle."
                toolDetails = "Web search for: $query"
            }

            IntentAction.OPEN_WEBSITE -> {
                val url = result.websiteUrl ?: "https://google.com"
                val webIntent = phoneAutomationManager.openWebsite(url)
                _navigationEvents.tryEmit(webIntent)
                finalSpokenResponse = "Navigating to $url, $userTitle."
                toolDetails = "Opened URL: $url"
            }

            IntentAction.CREATE_REMINDER -> {
                val title = result.reminderTitle ?: "JARVIS Reminder"
                val minutes = result.reminderMinutes ?: 10
                val timerIntent = phoneAutomationManager.createReminder(title, minutes)
                _navigationEvents.tryEmit(timerIntent)
                finalSpokenResponse = "Setting a $minutes-minute timer for \"$title\", $userTitle."
                toolDetails = "Timer created: $title ($minutes min)"
            }

            IntentAction.OPEN_SETTINGS -> {
                val setting = result.settingType ?: "general"
                val settingsIntent = phoneAutomationManager.openDeviceSetting(setting)
                _navigationEvents.tryEmit(settingsIntent)
                finalSpokenResponse = "Opening device $setting settings, $userTitle."
                toolDetails = "Opened settings: $setting"
            }

            IntentAction.ASK_CLARIFICATION,
            IntentAction.GENERAL_CONVERSATION -> {
                // Already synthesized directly by Gemini 3.8 Flash in spokenResponse
                toolDetails = "Cognitive conversational reply"
            }
        }

        _lastResponse.value = finalSpokenResponse

        // Save turn to Room Database for durable local persistence
        val entity = ConversationEntity(
            userQuery = originalQuery,
            aiResponse = finalSpokenResponse,
            intentType = result.action.name,
            languageCode = result.detectedLanguage,
            executionStatus = executionStatus,
            toolParameters = toolDetails
        )
        repository.saveConversation(entity)

        if (_status.value != ListeningStatus.AWAITING_CONFIRMATION) {
            _status.value = ListeningStatus.SPEAKING
        }

        // Multilingual Spoken Output via Text-to-Speech
        ttsManager.speak(finalSpokenResponse, result.detectedLanguage) {
            if (_status.value != ListeningStatus.AWAITING_CONFIRMATION) {
                if (!isManualStop && _settings.value.wakeWordEnabled) {
                    _status.value = ListeningStatus.LISTENING
                    startListeningInternal()
                } else {
                    _status.value = ListeningStatus.STANDBY
                }
            }
        }
    }

    fun confirmPendingAction() {
        val pending = _pendingAction.value ?: return
        pending.intentToLaunch?.let { intent ->
            _navigationEvents.tryEmit(intent)
        }
        val userTitle = _settings.value.userTitle
        val confirmationReply = "Directive confirmed and transmitted, $userTitle."
        _lastResponse.value = confirmationReply
        _pendingAction.value = null
        _status.value = ListeningStatus.STANDBY
        ttsManager.speak(confirmationReply)
    }

    fun cancelPendingAction() {
        val userTitle = _settings.value.userTitle
        val cancellationReply = "Directive cancelled as requested, $userTitle."
        _lastResponse.value = cancellationReply
        _pendingAction.value = null
        _status.value = ListeningStatus.STANDBY
        ttsManager.speak(cancellationReply)
    }

    fun replayResponse(text: String, languageCode: String? = null) {
        _status.value = ListeningStatus.SPEAKING
        ttsManager.speak(text, languageCode) {
            _status.value = ListeningStatus.STANDBY
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearConversations()
            _commandHistory.value = emptyList()
        }
    }

    fun toggleFlashlight() {
        val newState = flashlightManager.toggleTorch()
        val title = _settings.value.userTitle
        val reply = if (newState) "Illumination activated, $title." else "Illumination deactivated, $title."
        _lastResponse.value = reply
        ttsManager.speak(reply)
    }

    fun updateSettings(newSettings: JarvisSettings) {
        _settings.value = newSettings
        ttsManager.updateSettings(newSettings.speechPitch, newSettings.speechRate)

        viewModelScope.launch {
            repository.setPreference("user_title", newSettings.userTitle)
            repository.setPreference("speech_pitch", newSettings.speechPitch.toString())
            repository.setPreference("speech_rate", newSettings.speechRate.toString())
            repository.setPreference("selected_language", newSettings.selectedLanguageCode)
            repository.setPreference("wake_word", newSettings.wakeWordEnabled.toString())
            repository.setPreference("bg_listening", newSettings.backgroundServiceEnabled.toString())
        }

        if (newSettings.backgroundServiceEnabled && _status.value == ListeningStatus.LISTENING) {
            JarvisForegroundService.start(context)
        } else if (!newSettings.backgroundServiceEnabled) {
            JarvisForegroundService.stop(context)
        }
    }

    override fun onCleared() {
        mainHandler.removeCallbacksAndMessages(null)
        speechRecognizer?.destroy()
        speechRecognizer = null
        ttsManager.shutdown()
        super.onCleared()
    }
}
