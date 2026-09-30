package com.habittracker.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ModelsTest {

    private val d = LocalDate.of(2026, 10, 1) // четверг

    /* ---- keyOf / pad2 ---- */

    @Test
    fun keyOf_padsMonthAndDay() {
        assertEquals("2026-10-01", keyOf(d))
        assertEquals("2026-01-09", keyOf(LocalDate.of(2026, 1, 9)))
    }

    /* ---- startOfWeekMonday ---- */

    @Test
    fun startOfWeekMonday_handlesWeekBoundaries() {
        // Четверг 01.10.2026 → понедельник 28.09.2026
        assertEquals(LocalDate.of(2026, 9, 28), startOfWeekMonday(d))
        // Понедельник остаётся собой
        assertEquals(LocalDate.of(2026, 9, 28), startOfWeekMonday(LocalDate.of(2026, 9, 28)))
        // Воскресенье 04.10.2026 → понедельник 28.09.2026
        assertEquals(LocalDate.of(2026, 9, 28), startOfWeekMonday(LocalDate.of(2026, 10, 4)))
    }

    /* ---- buildMonthGrid ---- */

    @Test
    fun monthGrid_startsMonday_andCoversWholeMonth() {
        val grid = buildMonthGrid(2026, 9) // октябрь 2026 (month 0-based)
        // все строки по 7 дней
        grid.forEach { assertEquals(7, it.size) }
        // первый день — понедельник
        assertEquals(java.time.DayOfWeek.MONDAY, grid.first().first().dayOfWeek)
        // месяц полностью покрыт
        val days = grid.flatten()
        assertEquals(31, days.count { it.monthValue == 10 && it.year == 2026 })
        assertTrue(days.contains(LocalDate.of(2026, 10, 1)))
        assertTrue(days.contains(LocalDate.of(2026, 10, 31)))
    }

    @Test
    fun monthGrid_febLeapYear() {
        val grid = buildMonthGrid(2024, 1) // февраль 2024 (високосный)
        assertEquals(29, grid.flatten().count { it.monthValue == 2 && it.year == 2024 })
    }

    /* ---- daysLeft ---- */

    @Test
    fun daysLeft_futurePastToday() {
        val todayKey = "2026-10-01"
        assertEquals(2, daysLeft("2026-10-03", todayKey))
        assertEquals(0, daysLeft("2026-10-01", todayKey))
        assertEquals(-1, daysLeft("2026-09-30", todayKey))
        assertEquals(-30, daysLeft("2026-09-01", todayKey))
    }

    @Test
    fun daysLeft_monthAndYearBoundaries() {
        val todayKey = "2026-10-01"
        assertEquals(30, daysLeft("2026-10-31", todayKey))
        assertEquals(-273, daysLeft("2026-01-01", todayKey))
    }

    /* ---- dayPercent / monthStats ---- */

    @Test
    fun dayPercent_basic() {
        val habits = listOf(
            Habit("a", "A", IconKind.Flame, 1, 20, mapOf("2026-10-01" to true)),
            Habit("b", "B", IconKind.Flame, 2, 20, emptyMap())
        )
        assertEquals(50, dayPercent(habits, "2026-10-01"))
        assertEquals(0, dayPercent(emptyList(), "2026-10-01"))
    }

    @Test
    fun monthStats_countsOnlyCurrentMonth_andClamps() {
        val habits = listOf(
            Habit("a", "A", IconKind.Flame, 1, 5, mapOf(
                "2026-10-01" to true, "2026-10-02" to true,
                "2026-09-15" to true // прошлый месяц — не считается
            ))
        )
        val stats = monthStats(habits, d)
        assertEquals(2, stats.fact)
        assertEquals(1, stats.plan) // elapsed=1 → план = min(5, 1) = 1
        assertEquals(2, stats.perHabit.first().count)
    }

    @Test
    fun monthStats_planCappedByElapsedDays() {
        val habits = listOf(Habit("a", "A", IconKind.Flame, 1, 3, emptyMap()))
        // 1 октября: elapsed=1 → план = min(3, 1) = 1
        val stats = monthStats(habits, LocalDate.of(2026, 10, 1))
        assertEquals(1, stats.plan)
        // 20 октября: elapsed=20 → план = min(3, 20) = 3
        val stats20 = monthStats(habits, LocalDate.of(2026, 10, 20))
        assertEquals(3, stats20.plan)
    }

    /* ---- ratioForDate ---- */

    @Test
    fun ratioForDate_emptyHabitsSafe() {
        assertEquals(0.0, ratioForDate(emptyList(), d), 0.0001)
    }

    /* ---- clamp ---- */

    @Test
    fun clamp_bounds() {
        assertEquals(0, clamp(-50.0))
        assertEquals(100, clamp(150.0))
        assertEquals(50, clamp(50.4))
    }
}
