package com.habittracker.app

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Donut progress ring with a spring-animated fill. At 100% the ring gains
 * a soft accent glow — a small celebration for a perfect day.
 */
@Composable
fun RingProgress(
    percent: Int,
    size: Dp = 200.dp,
    strokeWidth: Dp = 16.dp,
    centerContent: @Composable () -> Unit = {}
) {
    val track = Surface2
    val fill = Accent
    val target = percent.coerceIn(0, 100) / 100f

    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessLow
        ),
        label = "ringFill"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (percent >= 100) 1f else 0f,
        animationSpec = tween(600),
        label = "ringGlow"
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)

            if (glowAlpha > 0.01f) {
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        style = android.graphics.Paint.Style.STROKE
                        this.strokeWidth = stroke
                        this.strokeCap = android.graphics.Paint.Cap.ROUND
                        color = fill.copy(alpha = 0.35f * glowAlpha).toArgb()
                        setShadowLayer(stroke * 1.2f, 0f, 0f, fill.copy(alpha = 0.8f * glowAlpha).toArgb())
                    }
                    canvas.nativeCanvas.drawArc(
                        inset, inset,
                        this.size.width - inset, this.size.height - inset,
                        -90f, 360f * animated, false, paint
                    )
                }
            }

            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            if (animated > 0.001f) {
                drawArc(
                    color = fill,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        centerContent()
    }
}

/** Animates a number from 0 to [target] for the percent counter in the ring center. */
@Composable
fun animateCount(target: Int): Int {
    val anim by animateFloatAsState(
        targetValue = target.toFloat(),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "countUp"
    )
    return anim.toInt()
}

