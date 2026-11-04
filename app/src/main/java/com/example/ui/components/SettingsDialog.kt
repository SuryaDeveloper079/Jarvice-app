package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.JarvisLanguage
import com.example.model.JarvisSettings
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    settings: JarvisSettings,
    onUpdateSettings: (JarvisSettings) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var speechPitch by remember { mutableFloatStateOf(settings.speechPitch) }
    var speechRate by remember { mutableFloatStateOf(settings.speechRate) }
    var selectedLangCode by remember { mutableStateOf(settings.selectedLanguageCode) }
    var wakeWordEnabled by remember { mutableStateOf(settings.wakeWordEnabled) }
    var autoListenOnOpen by remember { mutableStateOf(settings.autoListenOnOpen) }
    var backgroundServiceEnabled by remember { mutableStateOf(settings.backgroundServiceEnabled) }
    var batterySaver by remember { mutableStateOf(settings.batterySaverMode) }
    var userTitle by remember { mutableStateOf(settings.userTitle) }
    var languageMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, JarvisBorder, RoundedCornerShape(16.dp))
                .testTag("settings_dialog"),
            color = JarvisSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM CONFIGURATION",
                            style = MaterialTheme.typography.titleMedium,
                            color = JarvisCyan
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close settings",
                            tint = JarvisTextSecondary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = JarvisBorder.copy(alpha = 0.4f)
                )

                // 1. Multilingual Language Selector (15 languages)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cognitive Language (15 Languages)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = languageMenuExpanded,
                    onExpandedChange = { languageMenuExpanded = !languageMenuExpanded }
                ) {
                    val currentLang = JarvisLanguage.findByCode(selectedLangCode)
                    OutlinedTextField(
                        value = "${currentLang.name} (${currentLang.nativeName})",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false },
                        modifier = Modifier.background(JarvisSurfaceElevated)
                    ) {
                        JarvisLanguage.SUPPORTED_LANGUAGES.forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = lang.name, color = JarvisTextPrimary, fontWeight = FontWeight.Bold)
                                        Text(text = lang.nativeName, color = JarvisTextSecondary, fontSize = 12.sp)
                                    }
                                },
                                onClick = {
                                    selectedLangCode = lang.code
                                    languageMenuExpanded = false
                                    onUpdateSettings(
                                        settings.copy(
                                            selectedLanguageCode = lang.code,
                                            speechPitch = speechPitch,
                                            speechRate = speechRate,
                                            userTitle = userTitle,
                                            wakeWordEnabled = wakeWordEnabled,
                                            autoListenOnOpen = autoListenOnOpen,
                                            backgroundServiceEnabled = backgroundServiceEnabled,
                                            batterySaverMode = batterySaver
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. User Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "User Salutation: $userTitle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Sir", "Tony", "Boss", "Doctor").forEach { title ->
                        OutlinedButton(
                            onClick = {
                                userTitle = title
                                onUpdateSettings(
                                    settings.copy(
                                        userTitle = title,
                                        speechPitch = speechPitch,
                                        speechRate = speechRate,
                                        selectedLanguageCode = selectedLangCode,
                                        wakeWordEnabled = wakeWordEnabled,
                                        autoListenOnOpen = autoListenOnOpen,
                                        backgroundServiceEnabled = backgroundServiceEnabled,
                                        batterySaverMode = batterySaver
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (userTitle == title) JarvisCyan else JarvisBorder.copy(alpha = 0.4f)
                                )
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (userTitle == title) JarvisCyan.copy(alpha = 0.15f) else Color.Transparent
                            )
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (userTitle == title) JarvisCyan else JarvisTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Voice Pitch & Rate Sliders
                Text(
                    text = "TTS Pitch (${String.format(java.util.Locale.US, "%.2f", speechPitch)}x)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JarvisTextPrimary
                )
                Slider(
                    value = speechPitch,
                    onValueChange = { speechPitch = it },
                    onValueChangeFinished = {
                        onUpdateSettings(
                            settings.copy(
                                speechPitch = speechPitch,
                                speechRate = speechRate,
                                userTitle = userTitle,
                                selectedLanguageCode = selectedLangCode,
                                wakeWordEnabled = wakeWordEnabled,
                                autoListenOnOpen = autoListenOnOpen,
                                backgroundServiceEnabled = backgroundServiceEnabled,
                                batterySaverMode = batterySaver
                            )
                        )
                    },
                    valueRange = 0.7f..1.4f,
                    colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                )

                Text(
                    text = "TTS Speed (${String.format(java.util.Locale.US, "%.2f", speechRate)}x)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JarvisTextPrimary
                )
                Slider(
                    value = speechRate,
                    onValueChange = { speechRate = it },
                    onValueChangeFinished = {
                        onUpdateSettings(
                            settings.copy(
                                speechPitch = speechPitch,
                                speechRate = speechRate,
                                userTitle = userTitle,
                                selectedLanguageCode = selectedLangCode,
                                wakeWordEnabled = wakeWordEnabled,
                                autoListenOnOpen = autoListenOnOpen,
                                backgroundServiceEnabled = backgroundServiceEnabled,
                                batterySaverMode = batterySaver
                            )
                        )
                    },
                    valueRange = 0.7f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Mode A: Auto-listen on open
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceElevated)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Mode A: Auto-Listen on Launch", style = MaterialTheme.typography.titleMedium, fontSize = 13.sp, color = JarvisTextPrimary)
                        Text(text = "Begins listening immediately when app opens", style = MaterialTheme.typography.labelSmall, color = JarvisTextSecondary)
                    }
                    Switch(
                        checked = autoListenOnOpen,
                        onCheckedChange = { checked ->
                            autoListenOnOpen = checked
                            onUpdateSettings(settings.copy(autoListenOnOpen = checked))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisCyan.copy(alpha = 0.3f))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Mode B: System Default Assistant (VoiceInteractionService)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceElevated)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Mode B: System Assistant Role", style = MaterialTheme.typography.titleMedium, fontSize = 13.sp, color = JarvisTextPrimary)
                        Text(text = "Configure JARVIS in Android Digital Assistant settings", style = MaterialTheme.typography.labelSmall, color = JarvisTextSecondary)
                    }
                    IconButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // fallback to general settings
                                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open Assistant Settings", tint = JarvisCyan)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 6. Background Service Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceElevated)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Background Foreground Service", style = MaterialTheme.typography.titleMedium, fontSize = 13.sp, color = JarvisTextPrimary)
                        Text(text = "Shows persistent notification while active", style = MaterialTheme.typography.labelSmall, color = JarvisTextSecondary)
                    }
                    Switch(
                        checked = backgroundServiceEnabled,
                        onCheckedChange = { checked ->
                            backgroundServiceEnabled = checked
                            onUpdateSettings(settings.copy(backgroundServiceEnabled = checked))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisCyan.copy(alpha = 0.3f))
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Purge Conversation History
                OutlinedButton(
                    onClick = onClearHistory,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(JarvisRed)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "PURGE LOCAL CONVERSATION DATABASE", style = MaterialTheme.typography.labelLarge, color = JarvisRed)
                }
            }
        }
    }
}
