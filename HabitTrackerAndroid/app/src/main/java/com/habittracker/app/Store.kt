package com.habittracker.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/* ---------------- seed data ---------------- */

fun seedDays(today: LocalDate, truthyOffsets: List<Int>): Map<String, Boolean> =
    truthyOffsets.associate { keyOf(addDays(today, it)) to true }

fun initialHabits(today: LocalDate): List<Habit> = listOf(
    Habit("ws-early", "Workspace 1.5–3", IconKind.Sunrise, 0xFFF2A65A.toInt(), 20, seedDays(today, listOf(-1, -3))),
    Habit("study", "Study 3–6", IconKind.BookOpen, 0xFF4FD1C5.toInt(), 20, seedDays(today, listOf(-1, -2, -3, 0))),
    Habit("ws-late", "Workspace 6–9", IconKind.Moon, 0xFF8C9EFF.toInt(), 20, seedDays(today, listOf(-2))),
    Habit("exercise", "Exercise", IconKind.Dumbbell, 0xFFF27E7E.toInt(), 12, seedDays(today, listOf(0, -3)))
)

fun initialTasks(today: LocalDate): List<Task> {
    val t = keyOf(today)
    return listOf(
        Task(1, "Подъём в 8 утра", "Личное", "Высокий", "В работе", t),
        Task(2, "Пробежка", "Дом", "Средний", "Не начато", keyOf(addDays(today, 2))),
        Task(3, "Купить продукты", "Дом", "Низкий", "Просрочено", keyOf(addDays(today, -1))),
        Task(4, "Позвонить родителям", "Семья", "Низкий", "Выполнено", t)
    )
}

/* ---------------- JSON (de)serialization ---------------- */

fun habitsToJson(habits: List<Habit>): String {
    val arr = JSONArray()
    habits.forEach { h ->
        val days = JSONObject()
        h.days.forEach { (k, v) -> days.put(k, v) }
        arr.put(
            JSONObject()
                .put("id", h.id)
                .put("name", h.name)
                .put("icon", h.icon.name)
                .put("color", h.color)
                .put("goal", h.goal)
                .put("days", days)
        )
    }
    return arr.toString()
}

fun habitsFromJson(json: String): List<Habit> {
    val arr = JSONArray(json)
    return (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        val days = mutableMapOf<String, Boolean>()
        val d = o.optJSONObject("days")
        if (d != null) d.keys().forEach { k -> days[k] = d.getBoolean(k) }
        Habit(
            id = o.getString("id"),
            name = o.getString("name"),
            icon = try { IconKind.valueOf(o.getString("icon")) } catch (e: Exception) { IconKind.Flame },
            color = o.getInt("color"),
            goal = o.optInt("goal", 20),
            days = days
        )
    }
}

fun tasksToJson(tasks: List<Task>): String {
    val arr = JSONArray()
    tasks.forEach { t ->
        arr.put(
            JSONObject()
                .put("id", t.id)
                .put("title", t.title)
                .put("category", t.category)
                .put("priority", t.priority)
                .put("status", t.status)
                .put("due", t.due)
        )
    }
    return arr.toString()
}

fun tasksFromJson(json: String): List<Task> {
    val arr = JSONArray(json)
    return (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        Task(
            id = o.getLong("id"),
            title = o.getString("title"),
            category = o.optString("category", "Личное"),
            priority = o.optString("priority", "Средний"),
            status = o.optString("status", "Не начато"),
            due = o.optString("due", "")
        )
    }
}

fun logsToJson(logs: Map<String, DailyLog>): String {
    val root = JSONObject()
    logs.forEach { (k, l) ->
        root.put(
            k,
            JSONObject()
                .put("sleep", l.sleep)
                .put("mood", l.mood ?: JSONObject.NULL)
                .put("words", l.words)
        )
    }
    return root.toString()
}

fun logsFromJson(json: String): Map<String, DailyLog> {
    val root = JSONObject(json)
    val out = mutableMapOf<String, DailyLog>()
    root.keys().forEach { k ->
        val o = root.getJSONObject(k)
        out[k] = DailyLog(
            sleep = o.optInt("sleep", 0),
            mood = if (o.isNull("mood")) null else o.optString("mood"),
            words = o.optInt("words", 0)
        )
    }
    return out
}
