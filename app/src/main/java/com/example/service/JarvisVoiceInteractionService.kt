package com.example.service

import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JarvisVoiceInteractionService : VoiceInteractionService() {
    private val TAG = "JarvisVoiceService"

    companion object {
        private val _isAssistantActive = MutableStateFlow(false)
        val isAssistantActive: StateFlow<Boolean> = _isAssistantActive.asStateFlow()

        fun isDefaultAssistant(context: Context): Boolean {
            return isActiveService(context, ComponentName(context, JarvisVoiceInteractionService::class.java))
        }
    }

    override fun onReady() {
        super.onReady()
        _isAssistantActive.value = true
        Log.d(TAG, "JARVIS VoiceInteractionService is ready and bound as default assistant")
    }

    override fun onShutdown() {
        _isAssistantActive.value = false
        super.onShutdown()
    }
}

class JarvisVoiceInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return JarvisVoiceInteractionSession(this)
    }
}

class JarvisVoiceInteractionSession(context: Context) : VoiceInteractionSession(context) {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        // Launch main activity into foreground with assist context
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("EXTRA_ASSIST_LAUNCH", true)
        }
        context.startActivity(intent)
        finish()
    }
}
