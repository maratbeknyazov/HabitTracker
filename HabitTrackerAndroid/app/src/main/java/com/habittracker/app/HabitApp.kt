package com.habittracker.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HabitApp() {
    val context = LocalContext.current
    val vm: AppViewModel = viewModel(factory = AppViewModel.Factory(context.applicationContext as android.app.Application))

    // Recompute "today" every time the app comes to the foreground (fixes stale-date bug).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) vm.refreshDay()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var tab by rememberSaveable { mutableStateOf(Tab.Tracker.name) }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            val items = listOf(
                Triple(Tab.Tracker, Icons.Filled.Dashboard, "Трекер"),
                Triple(Tab.Calendar, Icons.Filled.CalendarMonth, "Календарь"),
                Triple(Tab.Plan, Icons.Filled.Checklist, "План"),
                Triple(Tab.Schedule, Icons.Filled.Schedule, "Расписание"),
                Triple(Tab.Ai, Icons.Filled.AutoAwesome, "ИИ")
            )
            NavigationBar(containerColor = Surface, tonalElevation = 0.dp) {
                items.forEach { (t, icon, label) ->
                    NavigationBarItem(
                        selected = tab == t.name,
                        onClick = { tab = t.name },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Accent,
                            selectedTextColor = Accent,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted,
                            indicatorColor = Surface2
                        )
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when (tab) {
                Tab.Calendar.name -> CalendarScreen(vm)
                Tab.Plan.name -> PlanScreen(vm)
                Tab.Schedule.name -> ScheduleScreen(vm)
                Tab.Ai.name -> AiScreen(vm)
                else -> TrackerScreen(vm)
            }
        }
    }
}

/**
 * Press-scale + haptic feedback wrapper. Use for any custom clickable "card-like" element
 * to get a pleasant physical feel: the element shrinks slightly while pressed,
 * springs back on release, and vibrates softly on click.
 */
@Composable
fun Modifier.pressable(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "pressScale"
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = null, onClick = onClick)
}

