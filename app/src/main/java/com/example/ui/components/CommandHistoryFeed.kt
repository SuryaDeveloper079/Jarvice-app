package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CommandHistoryItem
import com.example.model.IntentAction
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisNeonBlue
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommandHistoryFeed(
    history: List<CommandHistoryItem>,
    onReplay: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NEURAL CONVERSATION LOG",
                style = MaterialTheme.typography.titleMedium,
                color = JarvisCyan,
                letterSpacing = 1.0.sp
            )
            Text(
                text = "${history.size} RECORDS (ROOM DB)",
                style = MaterialTheme.typography.labelSmall,
                color = JarvisTextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisSurface.copy(alpha = 0.5f))
                    .border(1.dp, JarvisBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = JarvisTextMuted,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No stored conversation records.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextSecondary
                    )
                    Text(
                        text = "Speak naturally in any of the 15 supported languages.",
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisTextMuted
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                history.forEach { item ->
                    CommandHistoryCard(
                        item = item,
                        onReplay = { onReplay(item.jarvisResponse, item.languageCode) }
                    )
                }
            }
        }
    }
}

@Composable
fun CommandHistoryCard(
    item: CommandHistoryItem,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeString = rememberFormattedTime(item.timestamp)
    val (icon, badgeColor, actionLabel) = getActionMetadata(item.actionType)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = JarvisSurfaceElevated.copy(alpha = 0.85f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(JarvisBorder.copy(alpha = 0.35f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Action Badge + Language Pill + Timestamp + Replay Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Language code badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(JarvisSurfaceVariant)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.languageCode.uppercase(Locale.ROOT),
                            style = MaterialTheme.typography.labelSmall,
                            color = JarvisCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisTextMuted,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onReplay,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(JarvisSurfaceVariant)
                            .testTag("replay_button_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Replay speech",
                            tint = JarvisCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User Query
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "USER // ",
                    style = MaterialTheme.typography.labelSmall,
                    color = JarvisNeonBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "\"${item.userQuery}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JarvisTextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // JARVIS AI Response
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "JARVIS // ",
                    style = MaterialTheme.typography.labelSmall,
                    color = JarvisGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.jarvisResponse,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JarvisTextSecondary
                )
            }

            if (item.executionDetails.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "TOOL STATUS // ${item.executionDetails}",
                    style = MaterialTheme.typography.labelSmall,
                    color = JarvisTextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun getActionMetadata(type: IntentAction): Triple<ImageVector, Color, String> {
    return when (type) {
        IntentAction.OPEN_APP -> Triple(Icons.Default.OpenInBrowser, JarvisCyan, "APP LAUNCH")
        IntentAction.WHATSAPP_MESSAGE -> Triple(Icons.Default.Chat, JarvisGreen, "WHATSAPP")
        IntentAction.PHONE_CALL -> Triple(Icons.Default.Call, JarvisGreen, "PHONE CALL")
        IntentAction.FLASHLIGHT -> Triple(Icons.Default.FlashOn, JarvisGold, "ILLUMINATION")
        IntentAction.CHECK_TIME_DATE -> Triple(Icons.Default.Schedule, JarvisNeonBlue, "CHRONOMETER")
        IntentAction.SEARCH_WEB -> Triple(Icons.Default.Language, JarvisCyan, "WEB SEARCH")
        IntentAction.OPEN_WEBSITE -> Triple(Icons.Default.Language, JarvisCyan, "WEBSITE")
        IntentAction.CREATE_REMINDER -> Triple(Icons.Default.Alarm, JarvisGold, "REMINDER")
        IntentAction.OPEN_SETTINGS -> Triple(Icons.Default.Settings, JarvisGold, "SETTINGS")
        IntentAction.ASK_CLARIFICATION -> Triple(Icons.Default.QuestionMark, JarvisGold, "CLARIFICATION")
        IntentAction.GENERAL_CONVERSATION -> Triple(Icons.Default.Lightbulb, Color(0xFFB388FF), "AI COGNITION")
    }
}

@Composable
private fun rememberFormattedTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
