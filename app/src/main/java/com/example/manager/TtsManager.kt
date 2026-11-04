package com.example.manager

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.JarvisLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "JarvisTts"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var pendingSpeech: Pair<String, String?>? = null
    private var currentOnDone: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setPitch(0.95f)
            tts?.setSpeechRate(1.0f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    val callback = currentOnDone
                    currentOnDone = null
                    callback?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    currentOnDone = null
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    currentOnDone = null
                    Log.e(TAG, "TTS utterance error: $errorCode")
                }
            })

            isInitialized = true
            pendingSpeech?.let { (text, lang) ->
                speak(text, lang)
                pendingSpeech = null
            }
        } else {
            Log.e(TAG, "TTS initialization failed: $status")
        }
    }

    fun updateSettings(pitch: Float, rate: Float) {
        if (isInitialized) {
            tts?.setPitch(pitch)
            tts?.setSpeechRate(rate)
        }
    }

    fun speak(text: String, languageCode: String? = null, onDone: (() -> Unit)? = null) {
        currentOnDone = onDone
        if (!isInitialized) {
            pendingSpeech = Pair(text, languageCode)
            return
        }

        // Apply detected or selected language locale if specified
        if (!languageCode.isNullOrBlank() && languageCode != "auto") {
            try {
                val targetLang = JarvisLanguage.findByCode(languageCode)
                val result = tts?.setLanguage(targetLang.locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Language $languageCode missing in TTS, falling back to default")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to apply language $languageCode to TTS", e)
            }
        }

        val params = Bundle()
        val utteranceId = "jarvis_speech_${System.currentTimeMillis()}"
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
            _isSpeaking.value = false
            currentOnDone = null
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
