package com.habittracker.app

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun ScheduleScreen(vm: AppViewModel) {
    // По умолчанию открыт текущий день недели
    var selected by rememberSaveable {
        mutableStateOf(WEEKDAYS[LocalDate.now().dayOfWeek.value - 1])
    }
    val day = weeklySchedule.first { it.short == selected }
    val maxBudget = budgetItems.mapNotNull { it.amount.filter { ch -> ch.isDigit() }.toFloatOrNull() }.maxOrNull() ?: 1f

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Идеальная неделя", color = TextC, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                "Пары после 14:20 · утро свободно · зал в 12:00 Пн/Ср/Пт",
                color = Muted, fontSize = 13.sp
            )
        }

        // Переключатель дней недели
        item {
            Row(
                Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(14.dp)).padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weeklySchedule.forEach { d ->
                    val isToday = d.short == WEEKDAYS[LocalDate.now().dayOfWeek.value - 1]
                    val sel = d.short == selected
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .background(
                                if (sel) Accent else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selected = d.short }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            d.short,
                            color = when {
                                sel -> DarkText
                                isToday -> Accent
                                else -> Muted
                            },
                            fontSize = 13.sp,
                            fontWeight = if (sel || isToday) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Заголовок дня
        item {
            Column(
                Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(16.dp)).padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(day.emoji, fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp))
                    Text(day.name, color = TextC, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Text(day.subtitle, color = Accent, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        // Тайм-слоты дня
        items(day.slots, key = { it.start + it.title }) { slot ->
            SlotCard(slot)
        }

        // Бюджет
        item {
            Spacer(Modifier.height(6.dp))
            SectionTitle("БЮДЖЕТ $150/МЕС (ТАШКЕНТ)")
            Column(
                Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(16.dp)).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                budgetItems.forEach { item ->
                    Column(Modifier.animateContentSize()) {
                        Row(Modifier.fillMaxWidth()) {
                            Text(item.category, color = TextC, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.weight(1f))
                            Text(
                                item.amount + (item.perDay?.let { " · $it" } ?: ""),
                                color = Accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                            )
                        }
                        val frac = item.amount.filter { ch -> ch.isDigit() }.toFloatOrNull()?.div(maxBudget) ?: 0f
                        Box(Modifier.fillMaxWidth().padding(top = 5.dp).height(5.dp)) {
                            Box(
                                Modifier.fillMaxWidth(frac.coerceIn(0.02f, 1f))
                                    .height(5.dp)
                                    .background(Color(slotColor(SlotKind.Study)), RoundedCornerShape(3.dp))
                            )
                            Box(Modifier.fillMaxWidth().height(5.dp).background(Surface2, RoundedCornerShape(3.dp)))
                        }
                        Text(item.strategy, color = Muted, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 5.dp))
                    }
                }
            }
        }

        // Ключевые правила
        item {
            Spacer(Modifier.height(6.dp))
            SectionTitle("КЛЮЧЕВЫЕ ПРАВИЛА УСПЕХА")
            Column(
                Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(16.dp)).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                successRules.forEachIndexed { i, rule ->
                    Row {
                        Text(
                            "${i + 1}", color = DarkText, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Accent, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(rule, color = TextC, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SlotCard(slot: TimeSlot) {
    val col = Color(slotColor(slot.kind))
    val moment = slot.end == null
    Row(
        Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Время
        Column(
            Modifier.width(92.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(slot.start, color = TextC, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            slot.end?.let { end ->
                Text(end, color = Muted, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.width(6.dp))
        // Цветная полоска типа активности
        Box(
            Modifier
                .padding(top = 2.dp)
                .width(3.dp)
                .height(if (moment) 26.dp else 38.dp)
                .background(col, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(10.dp))
        // Текст
        Column(Modifier.weight(1f)) {
            Text(slot.title, color = TextC, fontSize = 13.sp, fontWeight = if (moment) FontWeight.Bold else FontWeight.Medium)
            slot.note?.let {
                Text(it, color = Muted, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
        // Пилюля типа
        Box(
            Modifier
                .background(col.copy(alpha = 0.14f), RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(slotLabel(slot.kind), color = col, fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center)
        }
    }
}
