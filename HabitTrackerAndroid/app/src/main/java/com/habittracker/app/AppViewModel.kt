package com.habittracker.app

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate

enum class Tab { Tracker, Calendar, Plan }

/**
 * Holds all app state. `today` is recomputed on every [refreshDay] (ON_RESUME),
 * so the app never shows a stale date after being left overnight.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("hbt", Context.MODE_PRIVATE)

    var today by mutableStateOf(LocalDate.now())
        private set
    var todayKey by mutableStateOf(keyOf(today))
        private set

    var habits by mutableStateOf<List<Habit>>(emptyList())
        private set
    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set
    var dailyLogs by mutableStateOf<Map<String, DailyLog>>(emptyMap())
        private set

    init {
        habits = prefs.getString("hbt:habits", null)?.let {
            runCatching { habitsFromJson(it) }.getOrNull()
        } ?: initialHabits(today)
        tasks = prefs.getString("hbt:tasks", null)?.let {
            runCatching { tasksFromJson(it) }.getOrNull()
        } ?: initialTasks(today)
        dailyLogs = prefs.getString("hbt:dailyLog", null)?.let {
            runCatching { logsFromJson(it) }.getOrNull()
        } ?: mapOf(todayKey to DailyLog(sleep = 7, mood = null, words = 0))
        persistAll()
    }

    /** Call from ON_RESUME: recompute today, seed an empty log for the new day if the date changed. */
    fun refreshDay() {
        val now = LocalDate.now()
        if (now == today) return
        today = now
        todayKey = keyOf(now)
        if (!dailyLogs.containsKey(todayKey)) {
            dailyLogs = dailyLogs + (todayKey to DailyLog())
            persistAll()
        }
    }

    fun persistAll() {
        prefs.edit()
            .putString("hbt:habits", habitsToJson(habits))
            .putString("hbt:tasks", tasksToJson(tasks))
            .putString("hbt:dailyLog", logsToJson(dailyLogs))
            .apply()
    }

    /* ---- habits ---- */

    fun toggleHabitDay(habitId: String, dateKey: String) {
        habits = habits.map { h ->
            if (h.id == habitId) h.copy(days = h.days + (dateKey to !(h.days[dateKey] ?: false)))
            else h
        }
        persistAll()
    }

    fun addHabit(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val color = PALETTE[habits.size % PALETTE.size]
        habits = habits + Habit("h-${System.currentTimeMillis()}", trimmed, IconKind.Flame, color, 20, emptyMap())
        persistAll()
    }

    fun removeHabit(id: String) {
        habits = habits.filter { it.id != id }
        persistAll()
    }

    /* ---- daily log ---- */

    fun logFor(key: String): DailyLog = dailyLogs[key] ?: DailyLog()

    fun patchLog(patch: DailyLog.() -> DailyLog) {
        val cur = logFor(todayKey)
        val next = patch(cur)
        dailyLogs = dailyLogs + (todayKey to next)
        persistAll()
    }

    /* ---- tasks ---- */

    fun addTask(title: String, category: String, priority: String, due: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        tasks = tasks + Task(System.currentTimeMillis(), trimmed, category, priority, "Не начато", due)
        persistAll()
    }

    fun removeTask(id: Long) {
        tasks = tasks.filter { it.id != id }
        persistAll()
    }

    fun cycleStatus(id: Long) {
        tasks = tasks.map { t ->
            if (t.id == id) t.copy(status = STATUS_ORDER[(STATUS_ORDER.indexOf(t.status) + 1) % STATUS_ORDER.size])
            else t
        }
        persistAll()
    }

    companion object {
        fun Factory(app: Application) = viewModelFactory {
            initializer { AppViewModel(app) }
        }
    }
}
