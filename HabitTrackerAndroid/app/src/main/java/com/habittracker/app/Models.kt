package com.habittracker.app

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/* ---------------- constants ---------------- */

val WEEKDAYS = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
val MONTHS = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
)
val MOODS = listOf("😔", "😐", "🙂", "😀", "🤩")
val CATEGORIES = listOf("Личное", "Работа", "Дом", "Друзья", "Семья", "Поездки")

val PRIORITIES = listOf(
    Triple("Высокий", 0xFFF27E7E.toInt(), 0.95f),
    Triple("Средний", 0xFFF2A65A.toInt(), 0.13f),
    Triple("Низкий", 0xFF4FD1C5.toInt(), 0.78f)
)
val STATUS_ORDER = listOf("Не начато", "В работе", "Выполнено", "Просрочено")
val STATUS_COLORS = mapOf(
    "Не начато" to 0xFF5A6272.toInt(),
    "В работе" to 0xFFF2A65A.toInt(),
    "Выполнено" to 0xFF4FD1C5.toInt(),
    "Просрочено" to 0xFFF27E7E.toInt()
)
val PALETTE = listOf(0xFFF2A65A.toInt(), 0xFF4FD1C5.toInt(), 0xFF8C9EFF.toInt(), 0xFFF27E7E.toInt(), 0xFFB784F2.toInt(), 0xFF6ED0A0.toInt())

fun priorityColor(id: String): Int = PRIORITIES.first { it.first == id }.second

/* ---------------- domain ---------------- */

data class Habit(
    val id: String,
    val name: String,
    val icon: IconKind,
    val color: Int,
    val goal: Int,
    val days: Map<String, Boolean> = emptyMap()
)

enum class IconKind { Sunrise, BookOpen, Moon, Dumbbell, Flame }

data class Task(
    val id: Long,
    val title: String,
    val category: String,
    val priority: String,
    val status: String,
    val due: String
)

data class DailyLog(val sleep: Int = 0, val mood: String? = null, val words: Int = 0)

/* ---------------- date helpers ---------------- */

private val KEY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun keyOf(d: LocalDate): String = d.format(KEY_FMT)

fun startOfWeekMonday(d: LocalDate): LocalDate = d.minusDays(((d.dayOfWeek.value + 6) % 7).toLong())

fun addDays(d: LocalDate, n: Int): LocalDate = d.plusDays(n.toLong())

fun buildMonthGrid(year: Int, month: Int): List<List<LocalDate>> {
    val first = LocalDate.of(year, month + 1, 1)
    val last = YearMonth.of(year, month + 1).atEndOfMonth()
    var cur = startOfWeekMonday(first)
    val endOfWeek = startOfWeekMonday(last).plusDays(6)
    val days = mutableListOf<LocalDate>()
    while (!cur.isAfter(endOfWeek)) {
        days.add(cur)
        cur = cur.plusDays(1)
    }
    return days.chunked(7)
}

fun pad2(n: Int): String = if (n < 10) "0$n" else "$n"

fun daysLeft(dueKey: String, todayKey: String): Int {
    val due = LocalDate.parse(dueKey, KEY_FMT)
    val today = LocalDate.parse(todayKey, KEY_FMT)
    return java.time.temporal.ChronoUnit.DAYS.between(today, due).toInt()
}

/* ---------------- domain logic ---------------- */

fun clamp(n: Double): Int = Math.max(0, Math.min(100, Math.round(n)).toInt())

data class MonthStatsItem(val id: String, val name: String, val color: Int, val count: Int, val pct: Int)
data class MonthStats(val percent: Int, val fact: Int, val plan: Int, val perHabit: List<MonthStatsItem>)

fun dayPercent(habits: List<Habit>, todayKey: String): Int =
    if (habits.isEmpty()) 0
    else clamp(habits.count { it.days[todayKey] == true } * 100.0 / habits.size)

fun monthStats(habits: List<Habit>, today: LocalDate): MonthStats {
    val elapsed = Math.max(1, today.dayOfMonth)
    var fact = 0
    var plan = 0
    val prefix = "${today.year}-${pad2(today.monthValue)}"
    val perHabit = habits.map { h ->
        val count = h.days.count { it.key.startsWith(prefix) && it.value }
        val habitPlan = Math.min(h.goal, elapsed)
        fact += count
        plan += habitPlan
        MonthStatsItem(h.id, h.name, h.color, count, clamp(count * 100.0 / Math.max(1, habitPlan)))
    }
    return MonthStats(clamp(fact * 100.0 / Math.max(1, plan)), fact, plan, perHabit)
}

fun ratioForDate(habits: List<Habit>, d: LocalDate): Double =
    if (habits.isEmpty()) 0.0
    else habits.count { it.days[keyOf(d)] == true }.toDouble() / habits.size
