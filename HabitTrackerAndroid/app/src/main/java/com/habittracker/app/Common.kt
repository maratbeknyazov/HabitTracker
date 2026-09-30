package com.habittracker.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun habitIcon(kind: IconKind): ImageVector = when (kind) {
    IconKind.Sunrise -> Icons.Filled.WbTwilight
    IconKind.BookOpen -> Icons.Filled.MenuBook
    IconKind.Moon -> Icons.Filled.DarkMode
    IconKind.Dumbbell -> Icons.Filled.FitnessCenter
    IconKind.Flame -> Icons.Filled.LocalFireDepartment
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Muted,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
    )
}

@Composable
fun TagPill(text: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        color = fg,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/**
 * Square check button with a satisfying "done!" moment:
 * soft tick on press, springy pop of the checkmark, gentle haptic.
 */
@Composable
fun CheckSquare(
    checked: Boolean,
    color: androidx.compose.ui.graphics.Color,
    sizeDp: Int,
    corner: Int,
    uncheckedIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val pressed by remember { MutableInteractionSource() }.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "checkPress"
    )
    // Pop of the checkmark icon when it appears
    val pop = remember { Animatable(if (checked) 1f else 0.4f) }
    LaunchedEffect(checked) {
        if (checked) {
            pop.snapTo(0.4f)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            pop.animateTo(
                1.15f,
                spring(dampingRatio = 0.45f, stiffness = 380f)
            )
            pop.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 300f))
        } else {
            pop.snapTo(1f)
        }
    }

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .graphicsLayer { scaleX = scale * pop.value.coerceAtLeast(0.85f); scaleY = scale * pop.value.coerceAtLeast(0.85f) }
            .background(if (checked) color else Surface2, RoundedCornerShape(corner.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Отмечено",
                tint = DarkText,
                modifier = Modifier
                    .size((sizeDp * 0.55).dp)
                    .graphicsLayer {
                        scaleX = pop.value
                        scaleY = pop.value
                        alpha = pop.value.coerceIn(0f, 1f)
                    }
            )
        } else if (uncheckedIcon != null) {
            uncheckedIcon()
        }
    }
}

/* Round minus/plus stepper button with press feedback */
@Composable
fun StepperButton(text: String, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val pressed by remember { MutableInteractionSource() }.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.82f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 700f),
        label = "stepPress"
    )
    Box(
        modifier = Modifier
            .size(26.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(Surface2, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = TextC, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

/* Weekday dot row (tracker habit cards). Dots pop in when filled. */
@Composable
fun WeekDots(habit: Habit, week: List<java.time.LocalDate>, todayKey: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        week.forEach { d ->
            val k = keyOf(d)
            val filled = habit.days[k] == true
            val heightScale by animateFloatAsState(
                targetValue = if (filled) 1f else 0.55f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f),
                label = "dotH"
            )
            Box(
                Modifier
                    .size(width = 20.dp, height = 6.dp)
                    .graphicsLayer {
                        scaleY = heightScale
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                    }
                    .background(
                        if (filled) androidx.compose.ui.graphics.Color(habit.color) else Surface2,
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Composable
fun BarTrack(fraction: Float, color: androidx.compose.ui.graphics.Color) {
    val anim by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "barFill"
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(Surface2, RoundedCornerShape(2.dp))
    ) {
        Box(
            Modifier
                .fillMaxWidth(anim)
                .height(4.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
    }
}
