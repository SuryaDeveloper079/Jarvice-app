package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ListeningStatus
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisNeonBlue
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun StatusIndicator(
    status: ListeningStatus,
    transcription: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "StatusBlink")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DotAlpha"
    )

    val (statusColor, statusText) = when (status) {
        ListeningStatus.LISTENING -> Pair(JarvisCyan, "ONLINE • LISTENING FOR COMMANDS")
        ListeningStatus.PROCESSING -> Pair(JarvisGold, "PROCESSING COGNITIVE INTENT...")
        ListeningStatus.AWAITING_CONFIRMATION -> Pair(JarvisGold, "CONFIRMATION REQUIRED • VERIFY DIRECTIVE")
        ListeningStatus.SPEAKING -> Pair(JarvisGreen, "JARVIS TRANSMITTING AUDIO")
        ListeningStatus.ERROR -> Pair(JarvisRed, "SENSORS INTERRUPTED")
        ListeningStatus.STANDBY -> Pair(JarvisNeonBlue.copy(alpha = 0.8f), "SYSTEM STANDBY • READY")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisSurface.copy(alpha = 0.85f))
            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("status_indicator")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header row with pulsing status dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(if (status == ListeningStatus.STANDBY) 0.6f else dotAlpha)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelLarge,
                    color = statusColor,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Real-time transcription or status message
            Text(
                text = transcription.ifBlank { "Tap START or the Arc Reactor to begin voice recognition." },
                style = MaterialTheme.typography.bodyMedium,
                color = if (status == ListeningStatus.LISTENING) JarvisTextPrimary else JarvisTextSecondary,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                maxLines = 2
            )
        }
    }
}
