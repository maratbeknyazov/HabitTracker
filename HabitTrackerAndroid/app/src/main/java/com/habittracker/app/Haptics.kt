package com.habittracker.app

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/** Soft tick for small successes: checking a habit, picking a mood. */
fun HapticFeedback.tick() = performHapticFeedback(HapticFeedbackType.TextHandleMove)

/** Confirmed success: task done, habit checked. */
fun HapticFeedback.thud() = performHapticFeedback(HapticFeedbackType.LongPress)
