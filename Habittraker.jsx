import React, { useMemo, useState } from "react";
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, ResponsiveContainer } from "recharts";
import {
  Sunrise, BookOpen, Moon, Dumbbell, Check, Flame, Trophy, Plus, X,
  ChevronLeft, ChevronRight, BedDouble, PenLine, Smile, Trash2,
  LayoutGrid, CalendarDays, ListChecks,
} from "lucide-react";

/**
 * Personal system: Habit Tracker + Calendar + Daily Plan (tasks).
 * Dark mode, mobile-first, single file, no external CSS config needed.
 * Three tabs, mirroring a real habit-tracking spreadsheet:
 *  - Трекер:   today's ring (day or month %), top habits, habit checklist
 *              with weekly dots, extra daily metrics (sleep / mood / words).
 *  - Календарь: month heatmap + tap a day to check off habits for that date.
 *  - План на день: task list with category / priority / status / due date,
 *              plus category and status charts.
 *
 * Requires: recharts, lucide-react
 * State is in-memory only (no localStorage) — wire up persistence yourself.
 */

/* ---------------- constants ---------------- */

const WEEKDAYS = ["Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"];
const MONTHS = ["Январь","Февраль","Март","Апрель","Май","Июнь","Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"];
const MOODS = ["😔", "😐", "🙂", "😀", "🤩"];
const CATEGORIES = ["Личное", "Работа", "Дом", "Друзья", "Семья", "Поездки"];
const PRIORITIES = [
  { id: "Высокий", color: "#F27E7E" },
  { id: "Средний", color: "#F2A65A" },
  { id: "Низкий", color: "#4FD1C5" },
];
const STATUS_ORDER = ["Не начато", "В работе", "Выполнено", "Просрочено"];
const STATUS_COLORS = {
  "Не начато": "#5A6272",
  "В работе": "#F2A65A",
  "Выполнено": "#4FD1C5",
  "Просрочено": "#F27E7E",
};
const PALETTE = ["#F2A65A", "#4FD1C5", "#8C9EFF", "#F27E7E", "#B784F2", "#6ED0A0"];

/* ---------------- date helpers ---------------- */

const pad = (n) => String(n).padStart(2, "0");
const keyOf = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const startOfWeekMonday = (d) => {
  const c = new Date(d);
  const diff = (c.getDay() + 6) % 7;
  c.setDate(c.getDate() - diff);
  c.setHours(0, 0, 0, 0);
  return c;
};
const addDays = (d, n) => {
  const c = new Date(d);
  c.setDate(c.getDate() + n);
  return c;
};
const buildMonthGrid = (year, month) => {
  const first = new Date(year, month, 1);
  const last = new Date(year, month + 1, 0);
  const start = startOfWeekMonday(first);
  const end = startOfWeekMonday(last);
  const endOfWeek = addDays(end, 6);
  const days = [];
  let cur = new Date(start);
  while (cur <= endOfWeek) {
    days.push(new Date(cur));
    cur = addDays(cur, 1);
  }
  const weeks = [];
  for (let i = 0; i < days.length; i += 7) weeks.push(days.slice(i, i + 7));
  return weeks;
};

const today = new Date();
const todayKey = keyOf(today);
const weekDates = Array.from({ length: 7 }, (_, i) => addDays(startOfWeekMonday(today), i));

/* ---------------- seed data ---------------- */

const seedDays = (truthyOffsets) => {
  const days = {};
  truthyOffsets.forEach((off) => { days[keyOf(addDays(today, off))] = true; });
  return days;
};

const initialHabits = [
  { id: "ws-early", name: "Workspace 1.5–3", icon: Sunrise, color: "#F2A65A", goal: 20, days: seedDays([-1, -3]) },
  { id: "study", name: "Study 3–6", icon: BookOpen, color: "#4FD1C5", goal: 20, days: seedDays([-1, -2, -3, 0]) },
  { id: "ws-late", name: "Workspace 6–9", icon: Moon, color: "#8C9EFF", goal: 20, days: seedDays([-2]) },
  { id: "exercise", name: "Exercise", icon: Dumbbell, color: "#F27E7E", goal: 12, days: seedDays([0, -3]) },
];

const initialTasks = [
  { id: 1, title: "Подъём в 8 утра", category: "Личное", priority: "Высокий", status: "В работе", due: todayKey },
  { id: 2, title: "Пробежка", category: "Дом", priority: "Средний", status: "Не начато", due: keyOf(addDays(today, 2)) },
  { id: 3, title: "Купить продукты", category: "Дом", priority: "Низкий", status: "Просрочено", due: keyOf(addDays(today, -1)) },
  { id: 4, title: "Позвонить родителям", category: "Семья", priority: "Низкий", status: "Выполнено", due: todayKey },
];

/* ---------------- small utils ---------------- */

const clamp = (n) => Math.max(0, Math.min(100, Math.round(n)));
const daysLeft = (dueKey) => Math.round((new Date(dueKey) - new Date(todayKey)) / 86400000);

export default function HabitTracker() {
  const [tab, setTab] = useState("tracker");
  const [habits, setHabits] = useState(initialHabits);
  const [tasks, setTasks] = useState(initialTasks);
  const [dailyLog, setDailyLog] = useState({ [todayKey]: { sleep: 7, mood: null, words: 0 } });
  const [ringMode, setRingMode] = useState("day"); // day | month
  const [newHabitOpen, setNewHabitOpen] = useState(false);
  const [newHabitName, setNewHabitName] = useState("");
  const [calYear, setCalYear] = useState(today.getFullYear());
  const [calMonth, setCalMonth] = useState(today.getMonth());
  const [selectedDate, setSelectedDate] = useState(today);
  const [newTask, setNewTask] = useState({ title: "", category: CATEGORIES[0], priority: "Средний", due: todayKey });

  const log = dailyLog[todayKey] || { sleep: 0, mood: null, words: 0 };
  const setLog = (patch) =>
    setDailyLog((prev) => ({ ...prev, [todayKey]: { ...(prev[todayKey] || { sleep: 0, mood: null, words: 0 }), ...patch } }));

  const toggleHabitDay = (habitId, dateKey) => {
    setHabits((prev) =>
      prev.map((h) => (h.id === habitId ? { ...h, days: { ...h.days, [dateKey]: !h.days[dateKey] } } : h))
    );
  };

  const addHabit = () => {
    if (!newHabitName.trim()) return;
    const color = PALETTE[habits.length % PALETTE.length];
    setHabits((prev) => [...prev, { id: `h-${Date.now()}`, name: newHabitName.trim(), icon: Flame, color, goal: 20, days: {} }]);
    setNewHabitName("");
    setNewHabitOpen(false);
  };
  const removeHabit = (id) => setHabits((prev) => prev.filter((h) => h.id !== id));

  const addTask = () => {
    if (!newTask.title.trim()) return;
    setTasks((prev) => [...prev, { id: Date.now(), title: newTask.title.trim(), category: newTask.category, priority: newTask.priority, status: "Не начато", due: newTask.due }]);
    setNewTask({ title: "", category: CATEGORIES[0], priority: "Средний", due: todayKey });
  };
  const removeTask = (id) => setTasks((prev) => prev.filter((t) => t.id !== id));
  const cycleStatus = (id) =>
    setTasks((prev) =>
      prev.map((t) => (t.id === id ? { ...t, status: STATUS_ORDER[(STATUS_ORDER.indexOf(t.status) + 1) % STATUS_ORDER.length] } : t))
    );

  /* ---- computed: rings & ranking ---- */

  const dayPercent = useMemo(() => {
    if (!habits.length) return 0;
    return clamp((habits.filter((h) => h.days[todayKey]).length / habits.length) * 100);
  }, [habits]);

  const monthStats = useMemo(() => {
    const elapsed = Math.max(1, today.getDate());
    let fact = 0, plan = 0;
    const perHabit = habits.map((h) => {
      const count = Object.keys(h.days).filter((k) => k.startsWith(`${today.getFullYear()}-${pad(today.getMonth() + 1)}`) && h.days[k]).length;
      const habitPlan = Math.min(h.goal, elapsed);
      fact += count;
      plan += habitPlan;
      return { ...h, count, pct: clamp((count / Math.max(1, habitPlan)) * 100) };
    });
    return { percent: clamp((fact / Math.max(1, plan)) * 100), fact, plan, perHabit };
  }, [habits]);

  const ringPercent = ringMode === "day" ? dayPercent : monthStats.percent;
  const ringChartData = [{ value: ringPercent }, { value: 100 - ringPercent }];
  const topHabits = [...monthStats.perHabit].sort((a, b) => b.pct - a.pct).slice(0, 3);

  /* ---- computed: calendar ---- */

  const monthGrid = useMemo(() => buildMonthGrid(calYear, calMonth), [calYear, calMonth]);
  const shiftMonth = (delta) => {
    let m = calMonth + delta, y = calYear;
    if (m < 0) { m = 11; y -= 1; } else if (m > 11) { m = 0; y += 1; }
    setCalMonth(m); setCalYear(y);
  };
  const ratioForDate = (d) => {
    if (!habits.length) return 0;
    const k = keyOf(d);
    return habits.filter((h) => h.days[k]).length / habits.length;
  };
  const selectedKey = keyOf(selectedDate);

  /* ---- computed: tasks ---- */

  const totalTasks = tasks.length;
  const doneTasks = tasks.filter((t) => t.status === "Выполнено").length;
  const categoryData = CATEGORIES.map((c) => ({ name: c.slice(0, 4), value: tasks.filter((t) => t.category === c).length }));
  const statusData = STATUS_ORDER.map((s) => ({ name: s, value: tasks.filter((t) => t.status === s).length })).filter((s) => s.value > 0);

  return (
    <div className="ht-root">
      <style>{CSS}</style>

      {tab === "tracker" && (
        <>
          <div className="ht-header">
            <span className="ht-date">{today.toLocaleDateString("ru-RU", { weekday: "long", day: "numeric", month: "long" })}</span>
            <h1>Трекер</h1>
          </div>

          <div className="ht-ring-toggle">
            <button className={ringMode === "day" ? "active" : ""} onClick={() => setRingMode("day")}>Сегодня</button>
            <button className={ringMode === "month" ? "active" : ""} onClick={() => setRingMode("month")}>Месяц</button>
          </div>

          <div className="ht-ring-wrap">
            <PieChart width={220} height={220}>
              <Pie data={ringChartData} dataKey="value" innerRadius={78} outerRadius={100} startAngle={90} endAngle={-270} stroke="none">
                <Cell fill="#F2A65A" />
                <Cell fill="#1E232D" />
              </Pie>
            </PieChart>
            <div className="ht-ring-center">
              <span className="ht-percent">{ringPercent}%</span>
              <span className="ht-percent-label">
                {ringMode === "day" ? `${habits.filter((h) => h.days[todayKey]).length}/${habits.length} сегодня` : `${monthStats.fact}/${monthStats.plan} за месяц`}
              </span>
            </div>
          </div>

          {topHabits.length > 0 && (
            <>
              <div className="ht-section-title">Топ привычки месяца</div>
              <div className="ht-top-list">
                {topHabits.map((h, i) => (
                  <div className="ht-top-row" key={h.id}>
                    <span className="ht-top-rank"><Trophy size={13} color={i === 0 ? "#F2A65A" : "#5A6272"} /></span>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: "flex", justifyContent: "space-between" }}>
                        <span className="ht-top-name">{h.name}</span>
                        <span className="ht-top-pct">{h.pct}%</span>
                      </div>
                      <div className="ht-top-bar-bg"><div className="ht-top-bar-fill" style={{ width: `${h.pct}%`, background: h.color }} /></div>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}

          <div className="ht-section-title">Привычки</div>
          <div className="ht-inline-add">
            {newHabitOpen ? (
              <>
                <input autoFocus placeholder="Название привычки" value={newHabitName} onChange={(e) => setNewHabitName(e.target.value)} onKeyDown={(e) => e.key === "Enter" && addHabit()} />
                <button className="ht-icon-btn" onClick={addHabit}><Check size={16} /></button>
                <button className="ht-icon-btn" onClick={() => setNewHabitOpen(false)}><X size={16} /></button>
              </>
            ) : (
              <button className="ht-icon-btn" style={{ width: "100%" }} onClick={() => setNewHabitOpen(true)}><Plus size={16} /></button>
            )}
          </div>

          <div className="ht-list">
            {habits.map((h) => {
              const Icon = h.icon;
              const checkedToday = h.days[todayKey];
              return (
                <div className="ht-card" key={h.id}>
                  <button className={`ht-check ${checkedToday ? "on" : ""}`} style={{ "--c": h.color }} onClick={() => toggleHabitDay(h.id, todayKey)}>
                    {checkedToday ? <Check size={18} strokeWidth={3} /> : <Icon size={18} />}
                  </button>
                  <div className="ht-card-main">
                    <div className="ht-card-top">
                      <span className="ht-card-name">{h.name}</span>
                      <button className="ht-task-del" onClick={() => removeHabit(h.id)}><X size={14} /></button>
                    </div>
                    <div className="ht-dots">
                      {weekDates.map((d) => {
                        const k = keyOf(d);
                        const v = h.days[k];
                        return <span key={k} className={`ht-dot ${v ? "filled" : ""} ${k === todayKey ? "today" : ""}`} style={v ? { background: h.color } : undefined} title={WEEKDAYS[(d.getDay() + 6) % 7]} />;
                      })}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          <div className="ht-section-title">Показатели дня</div>
          <div className="ht-extra">
            <div className="ht-extra-row">
              <span className="ht-extra-label"><BedDouble size={16} /> Часов сна</span>
              <div className="ht-stepper">
                <button onClick={() => setLog({ sleep: Math.max(0, (log.sleep || 0) - 1) })}>−</button>
                <span>{log.sleep || 0}</span>
                <button onClick={() => setLog({ sleep: (log.sleep || 0) + 1 })}>+</button>
              </div>
            </div>
            <div className="ht-extra-row">
              <span className="ht-extra-label"><Smile size={16} /> Настроение</span>
              <div className="ht-mood-row">
                {MOODS.map((m) => (
                  <button key={m} className={`ht-mood-btn ${log.mood === m ? "active" : ""}`} onClick={() => setLog({ mood: m })}>{m}</button>
                ))}
              </div>
            </div>
            <div className="ht-extra-row">
              <span className="ht-extra-label"><PenLine size={16} /> Слов написано</span>
              <div className="ht-stepper">
                <button onClick={() => setLog({ words: Math.max(0, (log.words || 0) - 50) })}>−</button>
                <span>{log.words || 0}</span>
                <button onClick={() => setLog({ words: (log.words || 0) + 50 })}>+</button>
              </div>
            </div>
          </div>
        </>
      )}

      {tab === "calendar" && (
        <>
          <div className="ht-header">
            <span className="ht-date">Календарь</span>
            <h1>{MONTHS[calMonth]} {calYear}</h1>
          </div>

          <div className="ht-cal-header">
            <button className="ht-cal-nav-btn" onClick={() => shiftMonth(-1)}><ChevronLeft size={16} /></button>
            <span style={{ fontSize: 13, color: "var(--muted)" }}>Нажми на день, чтобы отметить привычки</span>
            <button className="ht-cal-nav-btn" onClick={() => shiftMonth(1)}><ChevronRight size={16} /></button>
          </div>

          <div className="ht-cal-grid">
            {WEEKDAYS.map((w) => <div className="ht-cal-weekday" key={w}>{w}</div>)}
            {monthGrid.flat().map((d) => {
              const inMonth = d.getMonth() === calMonth;
              const ratio = ratioForDate(d);
              const isToday = keyOf(d) === todayKey;
              const isSelected = keyOf(d) === selectedKey;
              return (
                <div
                  key={keyOf(d)}
                  className={`ht-cal-cell ${inMonth ? "" : "out"} ${isToday ? "today" : ""} ${isSelected ? "selected" : ""}`}
                  onClick={() => setSelectedDate(d)}
                >
                  <div className="fill" style={{ background: ratio > 0 ? `rgba(242,166,90,${0.18 + ratio * 0.6})` : "transparent" }} />
                  <span style={{ position: "relative" }}>{d.getDate()}</span>
                </div>
              );
            })}
          </div>

          <div className="ht-cal-detail">
            <div className="ht-cal-detail-title">
              {selectedDate.toLocaleDateString("ru-RU", { weekday: "long", day: "numeric", month: "long" })}
            </div>
            {habits.map((h) => {
              const Icon = h.icon;
              const v = h.days[selectedKey];
              return (
                <div className="ht-cal-habit-row" key={h.id}>
                  <span style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 13 }}><Icon size={14} color={h.color} /> {h.name}</span>
                  <button className={`ht-check small ${v ? "on" : ""}`} style={{ "--c": h.color }} onClick={() => toggleHabitDay(h.id, selectedKey)}>
                    {v ? <Check size={14} strokeWidth={3} /> : null}
                  </button>
                </div>
              );
            })}
          </div>
        </>
      )}

      {tab === "plan" && (
        <>
          <div className="ht-header">
            <span className="ht-date">{today.toLocaleDateString("ru-RU", { day: "numeric", month: "long" })}</span>
            <h1>План на день</h1>
          </div>

          <div className="ht-stat-row">
            <div className="ht-stat-box"><div className="ht-stat-num">{totalTasks}</div><div className="ht-stat-label">Всего задач</div></div>
            <div className="ht-stat-box"><div className="ht-stat-num">{doneTasks}</div><div className="ht-stat-label">Выполнено</div></div>
          </div>

          <div className="ht-quote">
            <div className="ht-quote-title">ДИСЦИПЛИНА</div>
            <div className="ht-quote-text">Делать то, что не хочется, ради того, чего по-настоящему хочешь достичь.</div>
          </div>

          <div className="ht-chart-row">
            <div className="ht-chart-card">
              <div className="ht-chart-title">По категориям</div>
              <ResponsiveContainer width="100%" height={110}>
                <BarChart data={categoryData}>
                  <XAxis dataKey="name" tick={{ fill: "#8B93A7", fontSize: 9 }} axisLine={false} tickLine={false} />
                  <YAxis hide />
                  <Bar dataKey="value" radius={[4, 4, 0, 0]} fill="#F2A65A" />
                </BarChart>
              </ResponsiveContainer>
            </div>
            <div className="ht-chart-card" style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
              <div className="ht-chart-title" style={{ alignSelf: "flex-start" }}>По статусу</div>
              <PieChart width={100} height={100}>
                <Pie data={statusData} dataKey="value" innerRadius={26} outerRadius={44} stroke="none">
                  {statusData.map((s) => <Cell key={s.name} fill={STATUS_COLORS[s.name]} />)}
                </Pie>
              </PieChart>
            </div>
          </div>

          <div className="ht-task-form">
            <input placeholder="Новая задача" value={newTask.title} onChange={(e) => setNewTask((p) => ({ ...p, title: e.target.value }))} onKeyDown={(e) => e.key === "Enter" && addTask()} />
            <div className="ht-task-form-row">
              <select value={newTask.category} onChange={(e) => setNewTask((p) => ({ ...p, category: e.target.value }))}>
                {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
              <select value={newTask.priority} onChange={(e) => setNewTask((p) => ({ ...p, priority: e.target.value }))}>
                {PRIORITIES.map((p) => <option key={p.id} value={p.id}>{p.id}</option>)}
              </select>
              <input type="date" value={newTask.due} onChange={(e) => setNewTask((p) => ({ ...p, due: e.target.value }))} />
            </div>
            <button className="ht-add-btn" onClick={addTask}><Plus size={15} /> Добавить задачу</button>
          </div>

          <div className="ht-list">
            {tasks.map((t) => {
              const pr = PRIORITIES.find((p) => p.id === t.priority);
              const dl = daysLeft(t.due);
              return (
                <div className="ht-task-card" key={t.id}>
                  <div className="ht-task-top">
                    <span className="ht-task-title">{t.title}</span>
                    <button className="ht-task-del" onClick={() => removeTask(t.id)}><Trash2 size={14} /></button>
                  </div>
                  <div className="ht-task-tags">
                    <span className="ht-tag" style={{ background: "var(--surface-2)", color: "var(--muted)" }}>{t.category}</span>
                    <span className="ht-tag" style={{ background: `${pr.color}22`, color: pr.color }}>{t.priority}</span>
                  </div>
                  <div className="ht-task-bottom">
                    <span>{dl === 0 ? "сегодня" : dl > 0 ? `осталось ${dl} дн.` : `просрочено на ${Math.abs(dl)} дн.`}</span>
                    <button className="ht-status-pill" style={{ background: `${STATUS_COLORS[t.status]}22`, color: STATUS_COLORS[t.status] }} onClick={() => cycleStatus(t.id)}>{t.status}</button>
                  </div>
                </div>
              );
            })}
          </div>
        </>
      )}

      <div className="ht-tabbar">
        <button className={`ht-tab ${tab === "tracker" ? "active" : ""}`} onClick={() => setTab("tracker")}><LayoutGrid size={18} /> Трекер</button>
        <button className={`ht-tab ${tab === "calendar" ? "active" : ""}`} onClick={() => setTab("calendar")}><CalendarDays size={18} /> Календарь</button>
        <button className={`ht-tab ${tab === "plan" ? "active" : ""}`} onClick={() => setTab("plan")}><ListChecks size={18} /> План</button>
      </div>
    </div>
  );
}

const CSS = `
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;700&family=Space+Grotesk:wght@500;700&display=swap');

.ht-root {
  --bg: #0B0E14; --surface: #151A24; --surface-2: #1E232D;
  --text: #E7E9EE; --muted: #8B93A7; --accent: #F2A65A;
  font-family: 'DM Sans', -apple-system, sans-serif;
  background: var(--bg); color: var(--text);
  min-height: 100vh; width: 100%; max-width: 430px; margin: 0 auto;
  padding: 28px 20px 12px; box-sizing: border-box;
}
* { box-sizing: border-box; }

.ht-header { margin-bottom: 8px; }
.ht-date { font-size: 13px; color: var(--muted); text-transform: capitalize; }
.ht-header h1 { font-family: 'Space Grotesk', sans-serif; font-size: 28px; margin: 2px 0 18px; font-weight: 700; }

.ht-ring-toggle { display: flex; background: var(--surface); border-radius: 999px; padding: 3px; margin: 0 auto 10px; width: fit-content; }
.ht-ring-toggle button { border: none; background: none; color: var(--muted); font-size: 12px; padding: 6px 16px; border-radius: 999px; cursor: pointer; }
.ht-ring-toggle button.active { background: var(--surface-2); color: var(--text); }

.ht-ring-wrap { position: relative; display: flex; justify-content: center; align-items: center; margin: 4px auto 22px; width: 220px; height: 220px; }
.ht-ring-center { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; pointer-events: none; }
.ht-percent { font-family: 'Space Grotesk', sans-serif; font-size: 38px; font-weight: 700; line-height: 1; }
.ht-percent-label { font-size: 11px; color: var(--muted); margin-top: 6px; text-align: center; }

.ht-section-title { font-size: 13px; color: var(--muted); margin: 4px 0 10px; font-weight: 500; }

.ht-top-list { display: flex; flex-direction: column; gap: 8px; margin-bottom: 22px; }
.ht-top-row { display: flex; align-items: center; gap: 10px; background: var(--surface); border-radius: 12px; padding: 10px 12px; }
.ht-top-rank { display: flex; }
.ht-top-name { font-size: 13px; }
.ht-top-pct { font-size: 12px; color: var(--muted); font-weight: 600; }
.ht-top-bar-bg { height: 4px; background: var(--surface-2); border-radius: 2px; margin-top: 6px; overflow: hidden; }
.ht-top-bar-fill { height: 100%; border-radius: 2px; }

.ht-inline-add { display: flex; gap: 8px; margin-bottom: 12px; }
.ht-inline-add input { flex: 1; background: var(--surface); border: 1px solid var(--surface-2); color: var(--text); border-radius: 10px; padding: 10px 12px; font-size: 13px; }
.ht-icon-btn { background: var(--surface-2); border: none; color: var(--text); height: 38px; border-radius: 10px; display: flex; align-items: center; justify-content: center; cursor: pointer; flex: 0 0 38px; }

.ht-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 22px; }
.ht-card { display: flex; align-items: center; gap: 14px; background: var(--surface); border-radius: 16px; padding: 14px; }
.ht-check { flex: 0 0 auto; width: 40px; height: 40px; border-radius: 12px; border: 1.5px solid var(--surface-2); background: var(--surface-2); color: var(--muted); display: flex; align-items: center; justify-content: center; cursor: pointer; transition: background .15s, border-color .15s, color .15s; }
.ht-check.on { background: var(--c); border-color: var(--c); color: #0B0E14; }
.ht-check.small { width: 26px; height: 26px; border-radius: 8px; }
.ht-card-main { flex: 1; min-width: 0; }
.ht-card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.ht-card-name { font-size: 15px; font-weight: 500; }
.ht-dots { display: flex; gap: 6px; }
.ht-dot { width: 20px; height: 6px; border-radius: 3px; background: var(--surface-2); }
.ht-dot.today { box-shadow: 0 0 0 1.5px var(--muted); }

.ht-extra { background: var(--surface); border-radius: 16px; padding: 6px 14px; margin-bottom: 26px; }
.ht-extra-row { display: flex; align-items: center; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--surface-2); }
.ht-extra-row:last-child { border-bottom: none; }
.ht-extra-label { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.ht-stepper { display: flex; align-items: center; gap: 10px; font-size: 14px; font-weight: 600; }
.ht-stepper button { width: 26px; height: 26px; border-radius: 8px; border: none; background: var(--surface-2); color: var(--text); cursor: pointer; font-size: 15px; }
.ht-mood-row { display: flex; gap: 6px; }
.ht-mood-btn { background: var(--surface-2); border: none; border-radius: 8px; width: 30px; height: 30px; font-size: 15px; cursor: pointer; opacity: .45; }
.ht-mood-btn.active { opacity: 1; background: var(--accent); }

.ht-cal-header { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 14px; }
.ht-cal-header span { flex: 1; text-align: center; }
.ht-cal-nav-btn { background: var(--surface); border: none; color: var(--text); width: 32px; height: 32px; border-radius: 10px; display: flex; align-items: center; justify-content: center; cursor: pointer; flex: 0 0 32px; }
.ht-cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 6px; margin-bottom: 18px; }
.ht-cal-weekday { font-size: 11px; color: var(--muted); text-align: center; padding-bottom: 4px; }
.ht-cal-cell { aspect-ratio: 1; border-radius: 10px; background: var(--surface); display: flex; align-items: center; justify-content: center; position: relative; font-size: 12px; cursor: pointer; color: var(--text); overflow: hidden; }
.ht-cal-cell.out { opacity: .3; }
.ht-cal-cell.today { box-shadow: 0 0 0 1.5px var(--accent); }
.ht-cal-cell.selected { box-shadow: 0 0 0 1.5px #fff; }
.ht-cal-cell .fill { position: absolute; inset: 0; border-radius: 10px; }
.ht-cal-detail { background: var(--surface); border-radius: 16px; padding: 14px; margin-bottom: 22px; }
.ht-cal-detail-title { font-size: 13px; color: var(--muted); margin-bottom: 8px; text-transform: capitalize; }
.ht-cal-habit-row { display: flex; align-items: center; justify-content: space-between; padding: 9px 0; border-bottom: 1px solid var(--surface-2); }
.ht-cal-habit-row:last-child { border-bottom: none; }

.ht-stat-row { display: flex; gap: 10px; margin-bottom: 16px; }
.ht-stat-box { flex: 1; background: var(--surface); border-radius: 14px; padding: 12px; text-align: center; }
.ht-stat-num { font-family: 'Space Grotesk', sans-serif; font-size: 22px; font-weight: 700; }
.ht-stat-label { font-size: 11px; color: var(--muted); margin-top: 2px; }

.ht-quote { background: linear-gradient(135deg, var(--surface), var(--surface-2)); border-radius: 16px; padding: 16px; margin-bottom: 16px; }
.ht-quote-title { font-family: 'Space Grotesk', sans-serif; font-weight: 700; color: var(--accent); font-size: 13px; letter-spacing: .5px; margin-bottom: 6px; }
.ht-quote-text { font-size: 13px; color: var(--muted); line-height: 1.5; }

.ht-chart-row { display: flex; gap: 10px; margin-bottom: 16px; }
.ht-chart-card { flex: 1; background: var(--surface); border-radius: 14px; padding: 10px; }
.ht-chart-title { font-size: 11px; color: var(--muted); margin-bottom: 2px; padding-left: 4px; }

.ht-task-form { display: flex; flex-direction: column; gap: 8px; background: var(--surface); border-radius: 14px; padding: 12px; margin-bottom: 16px; }
.ht-task-form input, .ht-task-form select { background: var(--surface-2); border: 1px solid #262C38; color: var(--text); border-radius: 10px; padding: 9px 10px; font-size: 13px; font-family: inherit; }
.ht-task-form-row { display: flex; gap: 8px; }
.ht-task-form-row > * { flex: 1; min-width: 0; }
.ht-add-btn { background: var(--accent); color: #0B0E14; border: none; border-radius: 10px; padding: 10px; font-weight: 700; font-size: 13px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px; }

.ht-task-card { background: var(--surface); border-radius: 14px; padding: 12px; }
.ht-task-top { display: flex; justify-content: space-between; align-items: flex-start; gap: 8px; }
.ht-task-title { font-size: 14px; font-weight: 500; flex: 1; }
.ht-task-del { background: none; border: none; color: var(--muted); cursor: pointer; padding: 2px; flex: 0 0 auto; }
.ht-task-tags { display: flex; gap: 6px; margin-top: 8px; flex-wrap: wrap; }
.ht-tag { font-size: 10px; padding: 3px 8px; border-radius: 999px; font-weight: 500; }
.ht-task-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; font-size: 11px; color: var(--muted); }
.ht-status-pill { border: none; cursor: pointer; font-size: 10px; padding: 4px 10px; border-radius: 999px; font-weight: 600; }

.ht-tabbar { display: flex; position: sticky; bottom: 0; background: var(--surface); border-radius: 18px 18px 0 0; margin: 10px -20px 0; padding: 0 4px; }
.ht-tab { flex: 1; padding: 12px 0 14px; text-align: center; font-size: 11px; color: var(--muted); background: none; border: none; display: flex; flex-direction: column; align-items: center; gap: 4px; cursor: pointer; }
.ht-tab.active { color: var(--accent); }
`;