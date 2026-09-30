package com.habittracker.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun CalendarScreen(vm: AppViewModel) {
    val habits = vm.habits
    val today = vm.today

    var calYear by remember { mutableStateOf(today.year) }
    var calMonth0 by remember { mutableStateOf(today.monthValue - 1) }
    var selected by remember { mutableStateOf(today) }

    val grid = buildMonthGrid(calYear, calMonth0)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Календарь", color = Muted, fontSize = 13.sp)
            Text("${MONTHS[calMonth0]} $calYear", color = TextC, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBtn(Icons.Filled.ChevronLeft) {
                    var m = calMonth0 - 1
                    var y = calYear
                    if (m < 0) { m = 11; y -= 1 }
                    calMonth0 = m; calYear = y
                }
                Text(
                    "Нажми на день, чтобы отметить привычки",
                    color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                NavBtn(Icons.Filled.ChevronRight) {
                    var m = calMonth0 + 1
                    var y = calYear
                    if (m > 11) { m = 0; y += 1 }
                    calMonth0 = m; calYear = y
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth()) {
                WEEKDAYS.forEach { w ->
                    Text(
                        w, color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        items(grid.size) { rowIdx ->
            val week = grid[rowIdx]
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { d ->
                    val inMonth = d.monthValue - 1 == calMonth0
                    val ratio = ratioForDate(habits, d)
                    val isToday = d == today
                    val isSelected = d == selected
                    val k = keyOf(d)
                    val cellScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                        label = "cellScale"
                    )
                    Box(
                        Modifier.weight(1f)
                            .aspectRatio(1f)
                            .graphicsLayer { scaleX = cellScale; scaleY = cellScale }
                            .background(
                                if (ratio > 0) Accent.copy(alpha = (0.18 + ratio * 0.6).toFloat()) else Surface,
                                RoundedCornerShape(10.dp)
                            )
                            .then(
                                when {
                                    isSelected -> Modifier.border(1.5.dp, Color.White, RoundedCornerShape(10.dp))
                                    isToday -> Modifier.border(1.5.dp, Accent, RoundedCornerShape(10.dp))
                                    else -> Modifier
                                }
                            )
                            .pressable { selected = d },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${d.dayOfMonth}",
                            color = if (inMonth) TextC else Muted,
                            fontSize = 12.sp,
                            modifier = Modifier.alpha(if (inMonth) 1f else 0.35f)
                        )
                    }
                }
            }
        }

        item {
            Column(
                Modifier.fillMaxWidth()
                    .padding(top = 6.dp)
                    .background(Surface, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(selectedDateTitle(selected), color = Muted, fontSize = 13.sp, modifier = Modifier.padding(bottom = 4.dp))
                if (habits.isEmpty()) {
                    Text("Нет привычек", color = Muted, fontSize = 13.sp)
                } else {
                    habits.forEachIndexed { idx, h ->
                        val v = h.days[keyOf(selected)] == true
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(habitIcon(h.icon), null, tint = Color(h.color), modifier = Modifier.size(14.dp))
                                Text(h.name, color = TextC, fontSize = 13.sp)
                            }
                            CheckSquare(
                                checked = v,
                                color = Color(h.color),
                                sizeDp = 26,
                                corner = 8
                            ) { vm.toggleHabitDay(h.id, k0(selected)) }
                        }
                        if (idx != habits.lastIndex) {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Surface2))
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

private fun k0(d: LocalDate): String = keyOf(d)

private fun selectedDateTitle(d: LocalDate): String =
    "${weekdayRu(d)}, ${d.dayOfMonth} ${MONTHS[d.monthValue - 1].lowercase()} ${d.year}"

private fun weekdayRu(d: LocalDate): String = WEEKDAYS_FULL[(d.dayOfWeek.value + 6) % 7]

val WEEKDAYS_FULL = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")

@Composable
private fun NavBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        Modifier.size(32.dp)
            .background(Surface, RoundedCornerShape(10.dp))
            .pressable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = TextC, modifier = Modifier.size(16.dp))
    }
}
