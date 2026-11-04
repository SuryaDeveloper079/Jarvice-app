package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ListeningStatus
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.CommandControls
import com.example.ui.components.CommandHistoryFeed
import com.example.ui.components.ConfirmationCard
import com.example.ui.components.JarvisHeader
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StatusIndicator
import com.example.ui.theme.JarvisBackground
import com.example.viewmodel.JarvisViewModel

@Composable
fun JarvisHomeScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val status by viewModel.status.collectAsStateWithLifecycle()
    val rmsDb by viewModel.rmsDb.collectAsStateWithLifecycle()
    val transcription by viewModel.transcription.collectAsStateWithLifecycle()
    val history by viewModel.commandHistory.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val pendingAction by viewModel.pendingAction.collectAsStateWithLifecycle()
    val isTorchOn by viewModel.flashlightManager.isTorchOn.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Multi-permission launcher
    val permissionsToRequest = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.READ_CONTACTS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val audioGranted = results[Manifest.permission.RECORD_AUDIO] == true
        if (audioGranted) {
            viewModel.startJarvis()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice interaction.", Toast.LENGTH_LONG).show()
        }
    }

    // MODE A: Automatically begin listening when the app is open if permission is already granted
    LaunchedEffect(Unit) {
        val audioGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (audioGranted && settings.autoListenOnOpen) {
            viewModel.startJarvis()
        }
    }

    // Navigation / Intent dispatch listener
    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { intent ->
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open target application: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = JarvisBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF030814),
                            Color(0xFF050E22),
                            Color(0xFF020712)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Header (JARVIS logo, system status, flashlight toggle, settings)
                JarvisHeader(
                    isTorchOn = isTorchOn,
                    onToggleTorch = { viewModel.toggleFlashlight() },
                    onOpenSettings = { showSettingsDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Large Animated Circular Microphone / Arc Reactor Core
                ArcReactorVisualizer(
                    status = status,
                    rmsDb = rmsDb,
                    onClick = {
                        if (status == ListeningStatus.LISTENING || status == ListeningStatus.PROCESSING) {
                            viewModel.stopJarvis()
                        } else {
                            permissionLauncher.launch(permissionsToRequest)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Listening Status Indicator
                StatusIndicator(
                    status = status,
                    transcription = transcription,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                // 4. Action Confirmation Card (Section 4 & 10: User confirmation for WhatsApp / Calls)
                ConfirmationCard(
                    pendingAction = pendingAction,
                    onConfirm = { viewModel.confirmPendingAction() },
                    onCancel = { viewModel.cancelPendingAction() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5. START & STOP Controls + Natural Language Test Directives
                CommandControls(
                    status = status,
                    onStart = { permissionLauncher.launch(permissionsToRequest) },
                    onStop = { viewModel.stopJarvis() },
                    onNaturalLanguageSample = { prompt -> viewModel.processNaturalLanguageInput(prompt) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 6. Neural Conversation History (Persisted in Room Database)
                CommandHistoryFeed(
                    history = history,
                    onReplay = { responseText, langCode -> viewModel.replayResponse(responseText, langCode) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // 7. Settings Dialog (15 languages, Mode A/B, VoiceInteractionService)
    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            onUpdateSettings = { updated -> viewModel.updateSettings(updated) },
            onClearHistory = { viewModel.clearHistory() },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
