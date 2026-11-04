package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ListeningStatus
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisNeonBlue
import com.example.ui.theme.JarvisRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
    status: ListeningStatus,
    rmsDb: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorAnimations")

    // Slow clockwise rotation for outer tech ring
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OuterRingRotation"
    )

    // Counter-clockwise rotation for inner segmented ring
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "InnerRingRotation"
    )

    // Gentle idle breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingPulse"
    )

    // Audio-reactive scale driven by rmsDb when listening
    val audioScale by animateFloatAsState(
        targetValue = when (status) {
            ListeningStatus.LISTENING -> 1.0f + (rmsDb / 18f).coerceIn(0f, 0.45f)
            ListeningStatus.SPEAKING -> 1.0f + (breathingPulse - 0.94f) * 1.5f
            ListeningStatus.PROCESSING -> 1.08f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "AudioReactiveScale"
    )

    // Active primary color based on system status
    val statusColor = when (status) {
        ListeningStatus.LISTENING -> JarvisCyan
        ListeningStatus.PROCESSING, ListeningStatus.AWAITING_CONFIRMATION -> JarvisGold
        ListeningStatus.SPEAKING -> JarvisGreen
        ListeningStatus.ERROR -> JarvisRed
        ListeningStatus.STANDBY -> JarvisNeonBlue.copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier
            .size(260.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Holographic Canvas Arc Reactor
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2

            // 1. Outermost subtle glowing ring
            drawCircle(
                color = statusColor.copy(alpha = 0.15f),
                radius = maxRadius * 0.95f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 2. Outer Rotating Ticked Ring
            rotate(outerRotation, pivot = center) {
                val tickRadius = maxRadius * 0.88f
                val numTicks = 36
                for (i in 0 until numTicks) {
                    val angle = Math.toRadians((i * (360.0 / numTicks)))
                    val isMajor = i % 3 == 0
                    val tickLen = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
                    val startX = (center.x + (tickRadius - tickLen) * cos(angle)).toFloat()
                    val startY = (center.y + (tickRadius - tickLen) * sin(angle)).toFloat()
                    val endX = (center.x + tickRadius * cos(angle)).toFloat()
                    val endY = (center.y + tickRadius * sin(angle)).toFloat()

                    drawLine(
                        color = if (isMajor) statusColor.copy(alpha = 0.85f) else statusColor.copy(alpha = 0.35f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 3 segmented arcs
                val arcRadius = maxRadius * 0.84f
                val arcStroke = Stroke(
                    width = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(45f, 35f), 0f)
                )
                drawCircle(
                    color = statusColor.copy(alpha = 0.6f),
                    radius = arcRadius,
                    center = center,
                    style = arcStroke
                )
            }

            // 3. Middle Counter-Rotating Ring with 6 Arc Segments
            rotate(innerRotation, pivot = center) {
                val midRadius = maxRadius * 0.72f
                val numSegments = 6
                for (i in 0 until numSegments) {
                    val startAngle = i * (360f / numSegments) + 10f
                    drawArc(
                        color = statusColor.copy(alpha = 0.75f),
                        startAngle = startAngle,
                        sweepAngle = 40f,
                        useCenter = false,
                        topLeft = Offset(center.x - midRadius, center.y - midRadius),
                        size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 4. Audio Reactive Waveform Ring
            val waveRadius = (maxRadius * 0.58f * audioScale).coerceAtMost(maxRadius * 0.75f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        statusColor.copy(alpha = 0.35f),
                        statusColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = waveRadius * 1.25f
                ),
                radius = waveRadius,
                center = center
            )
            drawCircle(
                color = statusColor.copy(alpha = 0.9f),
                radius = waveRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Center Pulsing Core & Interactive Microphone Button
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(108.dp)
                .scale(audioScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            statusColor.copy(alpha = 0.45f),
                            Color(0xFF030D1C),
                            Color(0xFF01060E)
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    color = statusColor,
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, radius = 54.dp, color = statusColor),
                    onClick = onClick
                )
                .testTag("arc_reactor_microphone"),
            contentAlignment = Alignment.Center
        ) {
            // Internal core glows
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(1.dp, statusColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (status) {
                        ListeningStatus.LISTENING -> Icons.Default.Mic
                        ListeningStatus.SPEAKING -> Icons.Default.VolumeUp
                        ListeningStatus.PROCESSING -> Icons.Default.Mic
                        else -> Icons.Default.MicNone
                    },
                    contentDescription = "JARVIS Voice Microphone",
                    tint = statusColor,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}
