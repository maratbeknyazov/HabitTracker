package com.habittracker.app

/**
 * Персональное недельное расписание (универ Turin Polytechnic, группа SE2-24):
 * пары во 2-й половине дня, утро свободно, зал в 12:00 Пн/Ср/Пт.
 */

enum class SlotKind { Morning, Study, Gym, Class, Meal, Walk, English, Evening, Rest, Sleep }

data class TimeSlot(
    val start: String,
    val end: String?,          // null = момент времени («23:00»)
    val title: String,
    val kind: SlotKind,
    val note: String? = null
)

data class DaySchedule(
    val short: String,         // «Пн»
    val name: String,          // «ПОНЕДЕЛЬНИК»
    val emoji: String,
    val subtitle: String,
    val slots: List<TimeSlot>
)

data class BudgetItem(val category: String, val amount: String, val perDay: String?, val strategy: String)

/* ------------ цвета типов активностей ------------ */

fun slotColor(kind: SlotKind): Int = when (kind) {
    SlotKind.Morning -> 0xFF6ED0A0.toInt()
    SlotKind.Study -> 0xFF8C9EFF.toInt()
    SlotKind.Gym -> 0xFF4FD1C5.toInt()
    SlotKind.Class -> 0xFFF2A65A.toInt()
    SlotKind.Meal -> 0xFF8B93A7.toInt()
    SlotKind.Walk -> 0xFF6ED0A0.toInt()
    SlotKind.English -> 0xFFB784F2.toInt()
    SlotKind.Evening -> 0xFF8B93A7.toInt()
    SlotKind.Rest -> 0xFF5A6272.toInt()
    SlotKind.Sleep -> 0xFF5A6272.toInt()
}

fun slotLabel(kind: SlotKind): String = when (kind) {
    SlotKind.Morning -> "Утро"
    SlotKind.Study -> "Учёба"
    SlotKind.Gym -> "Зал"
    SlotKind.Class -> "Пары"
    SlotKind.Meal -> "Еда"
    SlotKind.Walk -> "Дорога"
    SlotKind.English -> "Английский"
    SlotKind.Evening -> "Вечер"
    SlotKind.Rest -> "Отдых"
    SlotKind.Sleep -> "Сон"
}

/* ------------ расписание недели ------------ */

val weeklySchedule: List<DaySchedule> = listOf(
    DaySchedule(
        short = "Пн", name = "ПОНЕДЕЛЬНИК", emoji = "🏋️",
        subtitle = "Зал в 12:00 + пары 14:20–18:40",
        slots = listOf(
            TimeSlot("07:00", "07:30", "Подъём, гигиена, плотный завтрак", SlotKind.Morning),
            TimeSlot("07:30", "08:00", "Английский — разговорная практика", SlotKind.English, "Говорение вслух, IT-лексика"),
            TimeSlot("08:00", "10:00", "Python + Statistics & Probability", SlotKind.Study, "Практика кода и задач"),
            TimeSlot("10:00", "11:15", "n8n + Менеджмент и продажи", SlotKind.Study, "Уроки n8n, продажа услуг автоматизации"),
            TimeSlot("11:15", "11:35", "Сборы, дорога в универ пешком (15–20 мин)", SlotKind.Walk),
            TimeSlot("12:00", "13:30", "Тренажёрный зал в универе + душ", SlotKind.Gym),
            TimeSlot("13:30", "14:15", "Обед и отдых", SlotKind.Meal),
            TimeSlot("14:20", "17:10", "Пары: SMD (lec), DSS (lec)", SlotKind.Class),
            TimeSlot("17:15", "17:40", "Дорога домой пешком", SlotKind.Walk, "Проветрить голову"),
            TimeSlot("18:00", "19:30", "Физика / химия / ДЗ", SlotKind.Study),
            TimeSlot("19:30", "20:00", "Ужин", SlotKind.Meal),
            TimeSlot("20:00", "20:40", "Английский — Listening", SlotKind.English, "Аудирование, подкасты"),
            TimeSlot("20:40", "21:30", "OSINT / папки Надики", SlotKind.Evening),
            TimeSlot("21:30", "23:00", "Отдых, подготовка к завтра", SlotKind.Rest),
            TimeSlot("23:00", null, "Сон — 8 часов восстановления", SlotKind.Sleep)
        )
    ),
    DaySchedule(
        short = "Вт", name = "ВТОРНИК", emoji = "💻",
        subtitle = "Пары 14:20 / 15:50 – 18:40",
        slots = listOf(
            TimeSlot("07:30", "08:00", "Подъём, завтрак, английские слова", SlotKind.Morning),
            TimeSlot("08:00", "10:30", "Java / Kotlin", SlotKind.Study, "ООП, синтаксис, консольные/Android-приложения"),
            TimeSlot("10:30", "12:00", "IoT & Embedded basics", SlotKind.Study, "Теория и практика IoT"),
            TimeSlot("12:00", "13:00", "Обед, перерыв", SlotKind.Meal),
            TimeSlot("13:00", "13:40", "Менеджмент и лидерство", SlotKind.Study, "Путь к Lead Developer: задачи, делегирование, Code Review"),
            TimeSlot("14:20", "18:40", "Пары: DSS (lab), IoT (prac), SMD (lab)", SlotKind.Class, "Начало меняется по расписанию недели (14:20 или 15:50)"),
            TimeSlot("19:00", "20:30", "Лабораторные и ДЗ", SlotKind.Study),
            TimeSlot("20:30", "21:00", "Английский — чтение IT-документации", SlotKind.English),
            TimeSlot("21:00", "23:00", "Отдых", SlotKind.Rest),
            TimeSlot("23:00", null, "Сон", SlotKind.Sleep)
        )
    ),
    DaySchedule(
        short = "Ср", name = "СРЕДА", emoji = "🚀",
        subtitle = "Без пар! Проекты + продажи + зал в 12:00",
        slots = listOf(
            TimeSlot("07:30", "08:00", "Подъём, завтрак", SlotKind.Morning),
            TimeSlot("08:00", "10:30", "Machine Learning & AI", SlotKind.Study, "Python, NumPy, Pandas, алгоритмы ML"),
            TimeSlot("10:30", "11:30", "n8n практика", SlotKind.Study, "Готовый workflow для портфолио"),
            TimeSlot("11:35", "12:00", "Дорога в университетский зал", SlotKind.Walk),
            TimeSlot("12:00", "13:30", "Тренажёрный зал + душ", SlotKind.Gym),
            TimeSlot("13:30", "14:30", "Обед", SlotKind.Meal),
            TimeSlot("14:30", "16:30", "Продажи услуг и фриланс", SlotKind.Study, "КП, поиск клиентов, портфолио автоматизаций"),
            TimeSlot("16:30", "18:00", "Физика и химия", SlotKind.Study, "Задачи и теория"),
            TimeSlot("18:00", "19:00", "Прогулка на свежем воздухе", SlotKind.Rest),
            TimeSlot("19:00", "20:00", "Ужин", SlotKind.Meal),
            TimeSlot("20:00", "20:40", "Английский — Speaking", SlotKind.English, "Речь со своим отражением / диктофоном"),
            TimeSlot("20:40", "21:30", "Разбор папок Надики / OSINT", SlotKind.Evening),
            TimeSlot("23:00", null, "Сон", SlotKind.Sleep)
        )
    ),
    DaySchedule(
        short = "Чт", name = "ЧЕТВЕРГ", emoji = "🔬",
        subtitle = "Пары 15:50 – 18:40",
        slots = listOf(
            TimeSlot("07:30", "08:00", "Подъём, завтрак, английский", SlotKind.Morning),
            TimeSlot("08:00", "10:30", "Java / Spring / Backend", SlotKind.Study, "Архитектура, глубокое погружение"),
            TimeSlot("10:30", "12:00", "Statistics & Probability + Физика", SlotKind.Study),
            TimeSlot("12:00", "13:00", "Обед", SlotKind.Meal),
            TimeSlot("13:00", "15:15", "Пет-проект / n8n-автоматизации", SlotKind.Study),
            TimeSlot("15:50", "18:40", "Пары: SMD (lab), SMD (lec)", SlotKind.Class),
            TimeSlot("19:00", "20:30", "Университетские ДЗ", SlotKind.Study),
            TimeSlot("20:30", "21:00", "Английский", SlotKind.English),
            TimeSlot("21:00", "23:00", "Отдых", SlotKind.Rest),
            TimeSlot("23:00", null, "Сон", SlotKind.Sleep)
        )
    ),
    DaySchedule(
        short = "Пт", name = "ПЯТНИЦА", emoji = "⚡",
        subtitle = "Зал в 12:00 + пары 14:20–18:40",
        slots = listOf(
            TimeSlot("07:30", "08:00", "Подъём, завтрак", SlotKind.Morning),
            TimeSlot("08:00", "10:30", "Python + IoT / ML", SlotKind.Study, "Подготовка к предметам дня"),
            TimeSlot("10:30", "11:30", "Менеджмент и продажи", SlotKind.Study),
            TimeSlot("12:00", "13:30", "Зал — или пара StartupK0", SlotKind.Gym, "На неделе 8 в 12:00 пара StartupK0: зал переноси на 10:30 или вечер"),
            TimeSlot("14:20", "17:10", "Пары: IoT (lec), ML&AI (prac)", SlotKind.Class),
            TimeSlot("17:30", "19:00", "Ужин, отдых", SlotKind.Meal),
            TimeSlot("19:00", "20:30", "Закрытие долгов и заданий недели", SlotKind.Study),
            TimeSlot("20:30", "21:00", "Английский", SlotKind.English),
            TimeSlot("21:00", "23:00", "Отдых в конце недели", SlotKind.Rest),
            TimeSlot("23:00", null, "Сон", SlotKind.Sleep)
        )
    ),
    DaySchedule(
        short = "Сб", name = "СУББОТА", emoji = "🛠",
        subtitle = "Пары 14:20 – 17:10",
        slots = listOf(
            TimeSlot("08:00", "08:30", "Подъём, завтрак", SlotKind.Morning),
            TimeSlot("08:30", "11:00", "Пет-проект / практика программирования", SlotKind.Study, "Например: Android на Kotlin + Python backend"),
            TimeSlot("11:00", "12:30", "GitHub, LinkedIn, кейсы n8n", SlotKind.Study, "Упаковка портфолио"),
            TimeSlot("12:30", "13:30", "Обед", SlotKind.Meal),
            TimeSlot("14:20", "17:10", "Пары: ML&AI (lec), IoT (lec)", SlotKind.Class),
            TimeSlot("17:30", "19:00", "Прогулка, свободное время", SlotKind.Rest),
            TimeSlot("19:00", "20:30", "OSINT и порядок в файлах", SlotKind.Evening),
            TimeSlot("23:00", null, "Сон", SlotKind.Sleep)
        )
    ),
    DaySchedule(
        short = "Вс", name = "ВОСКРЕСЕНЬЕ", emoji = "☀️",
        subtitle = "Восстановление и планирование",
        slots = listOf(
            TimeSlot("09:00", "09:30", "Подъём, завтрак", SlotKind.Morning),
            TimeSlot("09:30", "10:30", "Английский — отдыхом", SlotKind.English, "Фильм / видео на английском без напряжения"),
            TimeSlot("10:30", "12:00", "Недельный ревью", SlotKind.Study, "Дедлайны универа, план расходов на неделю"),
            TimeSlot("12:00", "23:00", "Полный отдых, прогулки, личные дела", SlotKind.Rest),
            TimeSlot("23:00", null, "Сон", SlotKind.Sleep)
        )
    )
)

/* ------------ бюджет $150/мес (Ташкент) ------------ */

val budgetItems: List<BudgetItem> = listOf(
    BudgetItem(
        "Продукты и еда", "$95", "~$5 в день",
        "Яйца, творог, куриное филе, гречка, рис, овсянка, чечевица, овощи. Готовка на 3 дня вперёд, обед с собой в контейнере."
    ),
    BudgetItem(
        "Транспорт", "$10–12", "~$0.4 в день",
        "15–25 минут пешком до универа — экономия + ежедневная активность. Автобус/метро только в плохую погоду."
    ),
    BudgetItem(
        "Быт и гигиена", "$10", null,
        "Шампунь, мыло, зубная паста, бытовые мелочи."
    ),
    BudgetItem(
        "Связь и интернет", "$5", null,
        "Студенческий тариф с мобильным интернетом."
    ),
    BudgetItem(
        "Резерв", "$18–20", null,
        "Неприкосновенный запас: аптека, распечатки, форс-мажоры. Не израсходовал — переносишь на следующий месяц."
    )
)

private fun budgetAmount(n: String): Float = n.filter { it.isDigit() }.toFloatOrNull() ?: 0f

fun budgetBarFraction(item: BudgetItem, maxAmount: Float): Float {
    val v = budgetAmount(item.amount)
    return if (maxAmount <= 0f) 0f else v / maxAmount
}

/* ------------ ключевые правила успеха ------------ */

val successRules: List<String> = listOf(
    "Утро 08:00–12:00 — под сложные предметы (ML, Python, Java, n8n): пары только после 14:20, мозг утром свежий.",
    "Связывай предметы: изучаешь ML — пиши Python; SMD — применяй в Java/Kotlin; n8n — сразу в КП для клиентов.",
    "Английский каждый день: 25–30 минут ежедневной практики дадут результат уже через 3 месяца.",
    "Бюджет: не покупай лишнего в первые две недели месяца, всегда держи резерв $20."
)
