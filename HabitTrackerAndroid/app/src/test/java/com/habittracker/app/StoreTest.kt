package com.habittracker.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StoreTest {

    private val today = LocalDate.of(2026, 10, 1)

    /* ---- habits JSON round-trip ---- */

    @Test
    fun habits_roundTrip_preservesEverything() {
        val habits = listOf(
            Habit("h1", "Спорт", IconKind.Dumbbell, 0xFFF27E7E.toInt(), 12,
                mapOf("2026-10-01" to true, "2026-09-30" to false)),
            Habit("h2", "Чтение", IconKind.BookOpen, 0xFF4FD1C5.toInt(), 20, emptyMap())
        )
        val restored = habitsFromJson(habitsToJson(habits))
        assertEquals(habits, restored)
    }

    @Test
    fun habits_roundTrip_daysMapValues() {
        val days = mapOf("2026-10-01" to true, "2026-10-02" to false, "2026-10-03" to true)
        val restored = habitsFromJson(habitsToJson(listOf(Habit("x", "X", IconKind.Flame, 1, 20, days))))
        assertEquals(days, restored.single().days)
    }

    @Test
    fun habits_unknownIcon_fallsBackToFlame() {
        val json = """[{"id":"h","name":"H","icon":"NoSuchIcon","color":-1000,"goal":5,"days":{}}]"""
        assertEquals(IconKind.Flame, habitsFromJson(json).single().icon)
    }

    /* ---- tasks JSON round-trip ---- */

    @Test
    fun tasks_roundTrip_preservesEverything() {
        val tasks = listOf(
            Task(1, "Задача", "Работа", "Высокий", "В работе", "2026-10-01"),
            Task(2, "Ещё", "Дом", "Низкий", "Просрочено", "2026-09-28")
        )
        assertEquals(tasks, tasksFromJson(tasksToJson(tasks)))
    }

    /* ---- daily logs JSON round-trip ---- */

    @Test
    fun logs_roundTrip_preservesEverything() {
        val logs = mapOf(
            "2026-10-01" to DailyLog(sleep = 7, mood = "🙂", words = 950),
            "2026-09-30" to DailyLog(sleep = 0, mood = null, words = 0)
        )
        assertEquals(logs, logsFromJson(logsToJson(logs)))
    }

    @Test
    fun logs_nullMood_survivesRoundTrip() {
        val logs = mapOf("2026-10-01" to DailyLog(sleep = 5, mood = null, words = 10))
        val restored = logsFromJson(logsToJson(logs))
        assertEquals(null, restored["2026-10-01"]!!.mood)
        assertEquals(5, restored["2026-10-01"]!!.sleep)
    }

    /* ---- seed data sanity ---- */

    @Test
    fun initialTasks_usesTodayKey() {
        val tasks = initialTasks(today)
        assertEquals(4, tasks.size)
        assertTrue(tasks.any { it.due == "2026-10-01" })
        assertTrue(tasks.any { it.due == "2026-10-03" })
        assertTrue(tasks.any { it.due == "2026-09-30" })
    }

    @Test
    fun initialHabits_seedsRelativeDays() {
        val habits = initialHabits(today)
        assertEquals(4, habits.size)
        val study = habits.first { it.id == "study" }
        assertTrue(study.days["2026-10-01"] == true)
        assertTrue(study.days["2026-09-30"] == true)
        assertTrue(study.days["2026-09-28"] == true)
    }
}
