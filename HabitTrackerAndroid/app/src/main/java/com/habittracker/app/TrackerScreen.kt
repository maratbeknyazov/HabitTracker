package com.habittracker.app

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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private fun ruDate(d: LocalDate): String =
    "${d.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale("ru"))}, ${d.dayOfMonth} ${MONTHS[d.monthValue - 1].lowercase()}"

@Composable
fun TrackerScreen(vm: AppViewModel) {
    val today = vm.today
    val todayKey = vm.todayKey
    val habits = vm.habits
    val log = vm.logFor(todayKey)

    var ringMode by remember { mutableStateOf("day") }
    var addOpen by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    val week = (0..6).map { addDays(startOfWeekMonday(today), it) }
    val dPct = dayPercent(habits, todayKey)
    val mStats = monthStats(habits, today)
    val ringPct = if (ringMode == "day") dPct else mStats.percent
    val doneToday = habits.count { it.days[todayKey] == true }
    val top = mStats.perHabit.sortedByDescending { it.pct }.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(ruDate(today), color = Muted, fontSize = 13.sp)
            Text("Трекер", color = TextC, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Row(
                modifier = Modifier.padding(top = 6.dp).background(Surface, RoundedCornerShape(50)).padding(3.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ToggleChip("Сегодня", ringMode == "day") { ringMode = "day" }
                ToggleChip("Месяц", ringMode == "month") { ringMode = "month" }
            }
        }

        item {
            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                RingProgress(percent = ringPct) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${animateCount(ringPct)}%", color = TextC, fontSize = 38.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (ringMode == "day") "$doneToday/${habits.size} сегодня"
                            else "${mStats.fact}/${mStats.plan} за месяц",
                            color = Muted, fontSize = 11.sp
                        )
                        if (ringPct >= 100) {
                            Spacer(Modifier.height(4.dp))
                            Text("🎉 Идеальный день!", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        if (top.isNotEmpty()) {
            item { SectionTitle("Топ привычки месяца") }
            items(top, key = { "top-" + it.id }) { h ->
                Column(
                    Modifier.fillMaxWidth()
                        .background(Surface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.EmojiEvents, null,
                            tint = if (h == top.first()) Accent else Color(0xFF5A6272),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(h.name, color = TextC, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text("${h.pct}%", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(6.dp))
                    BarTrack(h.pct / 100f, Color(h.color))
                }
            }
        }

        item { SectionTitle("Привычки") }

        if (addOpen) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = { Text("Название привычки", color = Muted, fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = Border,
                            focusedTextColor = TextC,
                            unfocusedTextColor = TextC,
                            cursorColor = Accent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        Modifier.size(38.dp).background(Surface2, RoundedCornerShape(10.dp)).clickable {
                            vm.addHabit(newName); newName = ""; addOpen = false
                        },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.Check, null, tint = TextC, modifier = Modifier.size(16.dp)) }
                    Box(
                        Modifier.size(38.dp).background(Surface2, RoundedCornerShape(10.dp)).clickable {
                            addOpen = false; newName = ""
                        },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.Close, null, tint = TextC, modifier = Modifier.size(16.dp)) }
                }
            }
        } else {
            item {
                Box(
                    Modifier.fillMaxWidth().height(38.dp)
                        .background(Surface2, RoundedCornerShape(10.dp))
                        .clickable { addOpen = true },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Add, null, tint = TextC, modifier = Modifier.size(16.dp)) }
            }
        }

        items(habits, key = { it.id }) { h ->
            val checked = h.days[todayKey] == true
            Column(
                Modifier.fillMaxWidth()
                    .background(Surface, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CheckSquare(
                        checked = checked,
                        color = Color(h.color),
                        sizeDp = 40,
                        corner = 12,
                        uncheckedIcon = {
                            Icon(habitIcon(h.icon), null, tint = Muted, modifier = Modifier.size(18.dp))
                        }
                    ) { vm.toggleHabitDay(h.id, todayKey) }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                h.name, color = TextC, fontSize = 15.sp,
                                fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Filled.Close, "Удалить", tint = Muted,
                                modifier = Modifier.size(14.dp).clickable { vm.removeHabit(h.id) }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        WeekDots(h, week, todayKey)
                    }
                }
            }
        }

        item { SectionTitle("Показатели дня") }

        item {
            Column(
                Modifier.fillMaxWidth()
                    .background(Surface, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp)
            ) {
                LogRow(label = "Часов сна", icon = { Icon(Icons.Filled.Bedtime, null, tint = Muted, modifier = Modifier.size(16.dp)) }) {
                    StepperButton("−") { vm.patchLog { copy(sleep = (sleep - 1).coerceAtLeast(0)) } }
                    Text("${log.sleep}", color = TextC, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 4.dp))
                    StepperButton("+") { vm.patchLog { copy(sleep = sleep + 1) } }
                }
                LogRow(label = "Настроение", icon = { Icon(Icons.Filled.Mood, null, tint = Muted, modifier = Modifier.size(16.dp)) }) {
                    MOODS.forEach { m ->
                        val active = log.mood == m
                        Box(
                            Modifier.size(27.dp)
                                .background(if (active) Accent else Surface2, RoundedCornerShape(8.dp))
                                .pressable { vm.patchLog { copy(mood = m) } },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(m, fontSize = 15.sp, color = if (active) TextC else Muted)
                        }
                    }
                }
                LogRow(
                    label = "Слов написано",
                    icon = { Icon(Icons.Filled.Notes, null, tint = Muted, modifier = Modifier.size(16.dp)) },
                    last = true
                ) {
                    StepperButton("−") { vm.patchLog { copy(words = (words - 50).coerceAtLeast(0)) } }
                    Text("${log.words}", color = TextC, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 4.dp))
                    StepperButton("+") { vm.patchLog { copy(words = words + 50) } }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun ToggleChip(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            .background(if (active) Surface2 else Color.Transparent, RoundedCornerShape(50))
            .pressable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (active) TextC else Muted, fontSize = 12.sp)
    }
}

@Composable
private fun LogRow(
    label: String,
    icon: @Composable () -> Unit,
    last: Boolean = false,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon()
                Text(label, color = TextC, fontSize = 13.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
        }
        if (!last) Box(Modifier.fillMaxWidth().height(1.dp).background(Surface2))
    }
}
