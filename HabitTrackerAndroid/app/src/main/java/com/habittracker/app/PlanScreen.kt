package com.habittracker.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate


@Composable
fun PlanScreen(vm: AppViewModel) {
    val tasks = vm.tasks
    val todayKey = vm.todayKey
    val haptic = LocalHapticFeedback.current

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CATEGORIES[0]) }
    var priority by remember { mutableStateOf("Средний") }
    var due by remember { mutableStateOf(todayKey) }

    val total = tasks.size
    val done = tasks.count { it.status == "Выполнено" }
    val catCounts = CATEGORIES.map { c -> tasks.count { it.category == c } }
    val maxCat = (catCounts.maxOrNull() ?: 0).coerceAtLeast(1)
    val statusData = STATUS_ORDER.mapNotNull { s ->
        val v = tasks.count { it.status == s }
        if (v > 0) Triple(s, v, Color(STATUS_COLORS[s]!!)) else null
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(ruDateKey(todayKey), color = Muted, fontSize = 13.sp)
            Text("План на день", color = TextC, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("$total", "Всего задач", Modifier.weight(1f))
                StatBox("$done", "Выполнено", Modifier.weight(1f))
            }
        }

        item {
            Column(
                Modifier.fillMaxWidth()
                    .background(Surface, RoundedCornerShape(16.dp))

                    .padding(16.dp)
            ) {
                Text("ДИСЦИПЛИНА", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Делать то, что не хочется, ради того, чего по-настоящему хочешь достичь.",
                    color = Muted, fontSize = 13.sp, lineHeight = 20.sp
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f).background(Surface, RoundedCornerShape(14.dp)).padding(10.dp)) {
                    Text("По категориям", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                    Spacer(Modifier.height(8.dp))
                    CATEGORIES.forEachIndexed { i, c ->
                        val v = catCounts[i]
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(c.take(4), color = Muted, fontSize = 9.sp, modifier = Modifier.width(30.dp))
                            Box(
                                Modifier.weight(1f).height(14.dp)
                                    .background(if (v > 0) Accent.copy(alpha = 0.9f * v / maxCat) else Surface2, RoundedCornerShape(4.dp))
                            )
                            Text("$v", color = Muted, fontSize = 9.sp, modifier = Modifier.padding(start = 6.dp).width(12.dp))
                        }
                    }
                }
                Column(Modifier.weight(1f).background(Surface, RoundedCornerShape(14.dp)).padding(10.dp)) {
                    Text("По статусу", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                        if (statusData.isEmpty()) {
                            Text("Нет задач", color = Muted, fontSize = 11.sp)
                        } else {
                            RingProgressSmall(segments = statusData.map { Triple(it.third, it.second.toFloat(), it.first) })
                        }
                    }
                    statusData.forEach { (name, v, col) ->
                        Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(col, RoundedCornerShape(50)))
                            Spacer(Modifier.width(6.dp))
                            Text(name, color = Muted, fontSize = 10.sp)
                            Spacer(Modifier.weight(1f))
                            Text("$v", color = TextC, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            Column(
                Modifier.fillMaxWidth()
                    .background(Surface, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Новая задача", color = Muted, fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Border,
                        focusedTextColor = TextC,
                        unfocusedTextColor = TextC,
                        cursorColor = Accent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Dropdownish(
                        options = CATEGORIES, selected = category,
                        onSelect = { category = it },
                        label = { it },
                        modifier = Modifier.weight(1f)
                    )
                    Dropdownish(
                        options = PRIORITIES.map { it.first }, selected = priority,
                        onSelect = { priority = it },
                        label = { it },
                        modifier = Modifier.weight(1f)
                    )
                    Dropdownish(
                        options = dateOptions(todayKey), selected = due,
                        onSelect = { due = it },
                        label = { dateShort(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Box(
                    Modifier.fillMaxWidth()
                        .height(40.dp)
                        .background(Accent, RoundedCornerShape(10.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vm.addTask(title, category, priority, due)
                            title = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Add, null, tint = DarkText, modifier = Modifier.size(15.dp))
                        Text("Добавить задачу", color = DarkText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(tasks, key = { it.id }) { t ->
            val dl = daysLeft(t.due, todayKey)
            val sCol = Color(STATUS_COLORS[t.status] ?: 0xFF8B93A7.toInt())
            Column(
                Modifier.fillMaxWidth()
                    .background(Surface, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(t.title, color = TextC, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Filled.Delete, "Удалить", tint = Muted,
                        modifier = Modifier.size(14.dp).clickable { vm.removeTask(t.id) }
                    )
                }
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TagPill(t.category, Surface2, Muted)
                    TagPill(t.priority, priorityPillBg(t.priority), Color(priorityColor(t.priority)))
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dueText(dl), color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Box(
                        Modifier
                            .background(pillBg(sCol), RoundedCornerShape(50))
                            .clickable { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); vm.cycleStatus(t.id) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(t.status, color = sCol, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

private fun ruDateKey(key: String): String {
    val d = LocalDate.parse(key)
    return "${d.dayOfMonth} ${MONTHS[d.monthValue - 1].lowercase()}"
}

private fun dateShort(key: String): String {
    val d = LocalDate.parse(key)
    return "${d.dayOfMonth}.${if (d.monthValue < 10) "0" else ""}${d.monthValue}"
}

private fun dateOptions(todayKey: String): List<String> {
    val t = LocalDate.parse(todayKey)
    return (-1..14).map { t.plusDays(it.toLong()).toString() }
}

private fun dueText(dl: Int): String = when {
    dl == 0 -> "сегодня"
    dl > 0 -> "осталось $dl дн."
    else -> "просрочено на ${-dl} дн."
}

private fun pillBg(color: Color): Color = color.copy(alpha = 0.16f)

private fun priorityPillBg(id: String): Color = Color(priorityColor(id)).copy(alpha = 0.15f)

@Composable
private fun StatBox(num: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier.background(Surface, RoundedCornerShape(14.dp)).padding(vertical = 12.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(num, color = TextC, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun Dropdownish(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    label: (String) -> String,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Box(
            Modifier.fillMaxWidth()
                .background(Surface2, RoundedCornerShape(10.dp))
                .clickable { open = true }
                .padding(horizontal = 10.dp, vertical = 9.dp)
        ) {
            Text(label(selected), color = TextC, fontSize = 12.sp, maxLines = 1)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(label(opt), color = if (opt == selected) Accent else TextC, fontSize = 13.sp) },
                    onClick = { onSelect(opt); open = false }
                )
            }
        }
    }
}

@Composable
private fun RingProgressSmall(segments: List<Triple<Color, Float, String>>) {
    val total = segments.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
    Canvas(Modifier.size(90.dp)) {
        val stroke = 16.dp.toPx()
        val inset = stroke / 2
        val arcSize = Size(size.width - stroke, size.height - stroke)
        var start = -90f
        segments.forEach { (col, v, _) ->
            val sweep = 360f * (v / total)
            drawArc(
                color = col,
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke)
            )
            start += sweep
        }
    }
}
