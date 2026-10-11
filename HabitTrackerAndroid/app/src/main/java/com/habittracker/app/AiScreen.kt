package com.habittracker.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

/**
 * «ИИ-расписание»: пользователь описывает свой распорядок, промпт уходит
 * в n8n webhook (BuildConfig.AI_WEBHOOK_URL) или в бесплатный LLM
 * (Gemini free tier, BuildConfig.AI_API_KEY). Ответ превращается в задачи
 * и добавляется в «План» на сегодня.
 *
 * Значения задаются в local.properties (не коммитится):
 *   AI_WEBHOOK_URL=https://твой-n8n.example.com/webhook/schedule
 *   AI_API_KEY=...
 */
@Composable
fun AiScreen(vm: AppViewModel) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var prompt by remember { mutableStateOf(SAMPLE_PROMPT) }
    var result by remember { mutableStateOf("") }
    var imported by remember { mutableStateOf(0) }
    var state by remember { mutableStateOf("idle") } // idle | loading | done | error
    var errorMsg by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("ИИ-расписание", color = TextC, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            "Опиши свой распорядок — ИИ превратит его в расписание и добавит задачи в «План».",
            color = Muted, fontSize = 13.sp, lineHeight = 18.sp
        )

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            placeholder = { Text("Опиши распорядок дня…", color = Muted, fontSize = 13.sp) },
            minLines = 6,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                unfocusedBorderColor = Border,
                focusedTextColor = TextC,
                unfocusedTextColor = TextC,
                cursorColor = Accent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        val busy = state == "loading"
        val canSend = !busy && prompt.isNotBlank()
        Box(
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(if (canSend) Accent else Surface2, RoundedCornerShape(10.dp))
                .clickable(enabled = canSend) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    state = "loading"
                    val text = prompt.trim()
                    scope.launch {
                        try {
                            val answer = withContext(Dispatchers.IO) { AiClient.generateSchedule(text) }
                            result = answer
                            val tasks = AiParser.importTasks(answer, vm.todayKey)
                            imported = tasks.size
                            if (tasks.isNotEmpty()) vm.addTasks(tasks)
                            state = "done"
                        } catch (e: Exception) {
                            errorMsg = when {
                                BuildConfig.AI_WEBHOOK_URL.isBlank() && BuildConfig.AI_API_KEY.isBlank() ->
                                    "ИИ не настроен: пропиши AI_API_KEY или AI_WEBHOOK_URL в local.properties и пересобери APK."
                                else -> e.message ?: e.toString()
                            }
                            state = "error"
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (busy) "Генерирую…" else "Сгенерировать расписание",
                color = if (canSend) DarkText else Muted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (state == "done") {
            Row(
                Modifier.fillMaxWidth().background(Success.copy(alpha = 0.14f), RoundedCornerShape(14.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✅", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Готово! Добавлено задач: $imported (см. вкладку «План»)",
                    color = Success, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                )
            }
            ResultCard(result)
        } else if (state == "error") {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Danger.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text("Ошибка: $errorMsg", color = Danger, fontSize = 13.sp, lineHeight = 19.sp)
            }
            ResultCard(result)
        }
        if (state != "loading" && state != "done") {
            HintCard()
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ResultCard(text: String) {
    if (text.isBlank()) return
    Box(
        Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(text, color = TextC, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun HintCard() {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("КАК ПОДКЛЮЧИТЬ ИИ", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(
                "· n8n: создай workflow с Webhook (POST), адрес пропиши в local.properties как AI_WEBHOOK_URL=… и пересобери APK.\n" +
                "· Бесплатный LLM: получи ключ на ai.google.dev (Google AI Studio, есть бесплатная квота), пропиши AI_API_KEY=….\n" +
                "· Ответ ИИ превращается в задачи по строкам вида «08:00–10:30 Java/Kotlin — ООП».",
                color = Muted, fontSize = 12.sp, lineHeight = 18.sp
            )
        }
    }
}

private val SAMPLE_PROMPT = """
    Дай мне расписание распорядок дня.

    Бюджет расходов: 150 долларов в месяц (продукты, транспорт, бытовое).
    Дорога до универа 15–25 минут пешком.
    Предметы: физика, химия, statistics probability, java, kotlin,
    python, iot, ML, английский.
    Изучить менеджмент: как продавать свои услуги и стать лид-программистом.
    n8n уроки (русский YouTube).
    Если останется время: OSINT и сортировка папок «Надики» для учёбы.
    Тренировка в зале универа: понедельник, среда, пятница в 12:00.
    Пары во 2-й половине дня (14:20–18:40), среда полностью без пар.
""".trim()

/* ---------- HTTP-клиент ---------- */

object AiClient {

    /** POST промпта в n8n webhook либо LLM-провайдер. Блокирующий вызов — вызывать из coroutine. */
    fun generateSchedule(prompt: String): String {
        val webhook = BuildConfig.AI_WEBHOOK_URL
        val apiKey = BuildConfig.AI_API_KEY
        return when {
            webhook.isNotBlank() -> {
                val body = org.json.JSONObject().put("prompt", prompt).toString()
                val resp = post(webhook, body)
                // n8n обычно возвращает JSON {"output": "..."} или массив [{"output": "..."}]
                try {
                    val j = org.json.JSONObject(resp)
                    listOf("output", "text", "result", "schedule", "message", "reply")
                        .firstNotNullOfOrNull { if (j.optString(it, "").isNotBlank()) j.optString(it) else null }
                        ?: resp
                } catch (e: Exception) {
                    // возможно массив — возьмём первый элемент
                    try {
                        val arr = org.json.JSONArray(resp)
                        if (arr.length() > 0) arr.getJSONObject(0).optString("output", resp) else resp
                    } catch (e2: Exception) {
                        resp
                    }
                }
            }
            apiKey.isNotBlank() -> callGemini(apiKey, prompt)
            else -> throw IllegalStateException(
                "Не задан ни AI_WEBHOOK_URL, ни AI_API_KEY. Пропиши один из них в local.properties и пересобери приложение."
            )
        }
    }

    private fun post(url: String, body: String): String {
        var conn: HttpURLConnection? = null
        try {
            conn = (URI.create(url).toURL().openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 25_000
                readTimeout = 60_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
            return readResponse(conn)
        } finally {
            conn?.disconnect()
        }
    }

    private fun callGemini(apiKey: String, prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val body = org.json.JSONObject()
            .put("contents", org.json.JSONArray().put(
                org.json.JSONObject().put("parts", org.json.JSONArray().put(
                    org.json.JSONObject().put("text", prompt))
                ))
            ).toString()
        var conn: HttpURLConnection? = null
        try {
            conn = (URI.create(url).toURL().openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 25_000
                readTimeout = 60_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
            val resp = readResponse(conn)
            val j = org.json.JSONObject(resp)
            val candidates = j.optJSONArray("candidates")
                ?: throw IllegalStateException("Пустой ответ модели: ${resp.take(200)}")
            return candidates.getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts").getJSONObject(0)
                .optString("text", "")
        } finally {
            conn?.disconnect()
        }
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
        if (code !in 200..299) throw IllegalStateException("HTTP $code: ${text.take(300)}")
        return text
    }
}

/** Парсинг свободного ответа ИИ в задачи приложения. */
object AiParser {

    /**
     * Строки вида «08:00–10:30 Java/Kotlin — ООП и приложения» (или просто
     * значимые строки) становятся задачами на todayKey. Пропускает заголовки,
     * пустые строки и декоративные разделители.
     */
    fun importTasks(answer: String, todayKey: String): List<Task> {
        val tasks = mutableListOf<Task>()
        answer.lines().forEach { raw ->
            val line = raw.trim()
                .removePrefix("-").removePrefix("•").removePrefix("*").trim()
                .removePrefix("—").removePrefix("–").trim()
            if (line.length < 5) return@forEach
            // пропускаем сырой JSON / технический мусор и эмодзи-строки без содержания
            if (line.startsWith("{") || line.startsWith("[")) return@forEach
            if (line.startsWith("http://") || line.startsWith("https://")) return@forEach
            if (line.matches(Regex("^[\\p{So}\\s:—–-]+$"))) return@forEach
            // убираем тайм-слот из начала строки
            val cleaned = Regex("^\\d{1,2}[:.]\\d{2}\\s*[-–—]\\s*\\d{1,2}[:.]?\\d{0,2}\\s*")
                .replace(line, "").trim()
                .removePrefix(":").trim()
            if (cleaned.length < 3) return@forEach
            tasks.add(
                Task(
                    id = System.currentTimeMillis() + tasks.size,
                    title = cleaned,
                    category = "Расписание",
                    priority = "Средний",
                    status = "Не начато",
                    due = todayKey
                )
            )
        }
        return tasks.distinctBy { it.title }
    }
}
