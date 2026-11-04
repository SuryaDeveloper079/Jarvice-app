package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ListeningStatus
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary

@Composable
fun CommandControls(
    status: ListeningStatus,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onNaturalLanguageSample: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = status == ListeningStatus.LISTENING || status == ListeningStatus.PROCESSING

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // START and STOP Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onStart,
                enabled = !isListening,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("start_jarvis_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisCyan,
                    contentColor = Color(0xFF020C18),
                    disabledContainerColor = JarvisCyan.copy(alpha = 0.35f),
                    disabledContentColor = Color(0xFF020C18).copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START JARVIS",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.sp
                )
            }

            OutlinedButton(
                onClick = onStop,
                enabled = isListening || status == ListeningStatus.SPEAKING || status == ListeningStatus.AWAITING_CONFIRMATION,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("stop_jarvis_button"),
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isListening || status == ListeningStatus.SPEAKING) JarvisRed else JarvisRed.copy(alpha = 0.3f)
                    )
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = JarvisRed,
                    disabledContentColor = JarvisRed.copy(alpha = 0.35f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STOP JARVIS",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Natural Language Sample Directives Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val naturalSamples = listOf(
                "Open WhatsApp and send Rahul a message saying I will arrive tomorrow",
                "Open YouTube",
                "Turn on flashlight",
                "Explain Python programming",
                "Help me plan my day",
                "What time is it?",
                "Open Google Maps",
                "Call Rahul",
                "Search Google for quantum computing",
                "Open device bluetooth settings"
            )

            naturalSamples.forEach { sample ->
                SuggestionChip(
                    onClick = { onNaturalLanguageSample(sample) },
                    label = {
                        Text(
                            text = sample,
                            style = MaterialTheme.typography.bodyMedium,
                            color = JarvisTextPrimary,
                            fontSize = 13.sp
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = JarvisSurfaceVariant.copy(alpha = 0.75f)
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = JarvisCyan.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("natural_sample_${sample.take(15).replace(" ", "_").lowercase()}")
                )
            }
        }
    }
}
