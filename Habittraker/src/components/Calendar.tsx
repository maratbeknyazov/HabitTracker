import { useCallback, useMemo, useState } from "react";
import { Habit } from "../types/habit";
import { Task } from "../types/task";
import { keyOf } from "../utils/date";
import { buildMonthGrid } from "../utils/calendar";
import "./calendar.css";

interface CalProps {
  habits: Habit[];
  tasks: Task[];
  onToggleHabitDay: (id: Habit["id"], dateKey: string) => void;
  onTabChange: (tab: "tracker" | "calendar" | "plan") => void;
}

export function Calendar({ habits, tasks, onToggleHabitDay, onTabChange }: CalProps) {
  const [year, setYear] = useState(() => new Date().getFullYear());
  const [month, setMonth] = useState(() => new Date().getMonth());
  const [selected, setSelected] = useState<Date>(() => new Date());
  const [open, setOpen] = useState(true);

  const grid = useMemo(() => buildMonthGrid(year, month), [year, month]);

  const shift = useCallback(
    (delta: number) => {
      let m = month + delta;
      let y = year;
      if (m < 0) {
        m = 11;
        y -= 1;
      } else if (m > 11) {
        m = 0;
        y += 1;
      }
      setYear(y);
      setMonth(m);
      setSelected(new Date(y, m, 1));
    },
    [month, year]
  );

  const ratioFor = useCallback(
    (d: Date) => {
      if (habits.length === 0) return 0;
      return habits.filter((h) => h.days[keyOf(d)]).length / habits.length;
    },
    [habits]
  );

  const selectedKey = useMemo(() => keyOf(selected), [selected]);

  const doneTasks = useMemo(() => tasks.filter((t) => t.status === "Done").length, [tasks]);
  const totalTasks = useMemo(() => tasks.length, [tasks]);

  return (
    <section className="cal" aria-label="Календарь привычек">
      <header className="cal-header">
        <div className="cal-title-block">
          <span className="cal-arrow" onClick={() => shift(-1)} aria-label="Месяц назад">
            «
          </span>
          <h2 className="cal-month">{new Date(year, month).toLocaleDateString("ru-RU", { month: "long", year: "numeric" })}</h2>
          <span className="cal-arrow" onClick={() => shift(1)} aria-label="Месяц вперёд">
            »
          </span>
        </div>
        <div className="cal-view">
          <button
            type="button"
            className={`cal-btn ${open ? "active" : ""}`}
            onClick={() => setOpen(true)}
            aria-pressed={open}
            aria-label="Смотреть в режиме месяц"
          >
            Месяц
          </button>
          <button
            type="button"
            className={`cal-btn ${!open ? "active" : ""}`}
            onClick={() => setOpen(false)}
            aria-pressed={!open}
            aria-label="Смотреть в режиме день"
          >
            День
          </button>
        </div>
      </header>

      <div className="cal-grid">
        {["Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"].map((d) => (
          <div key={d} className="cal-weekday">
            {d}
          </div>
        ))}
        {grid.flat().map((d) => {
          const inMonth = d.getMonth() === month;
          const ratio = ratioFor(d);
          const isToday = keyOf(d) === keyOf(new Date());
          const isSelected = keyOf(d) === selectedKey;
          return (
            <button
              key={keyOf(d)}
              type="button"
              className={`cal-cell ${inMonth ? "" : "cal-cell--out"} ${isToday ? "cal-cell--today" : ""} ${isSelected ? "cal-cell--selected" : ""}`}
              style={{ "--cell-ratio": ratio }}
              onClick={() => setSelected(d)}
              aria-label={`${d.toLocaleDateString("ru-RU", { weekday: "long", day: "numeric", month: "long" })} — выполнено ${Math.round(ratio * 100)}%`}
            >
              <span className="cal-cell__fill" style={{ backgroundColor: ratio > 0 ? `rgba(242,166,90,${0.18 + ratio * 0.6})` : "transparent" }} />
              <span className="cal-cell__date">{d.getDate()}</span>
            </button>
          );
        })}
      </div>

      <div className="cal-detail">
        <div className="cal-detail-title">{selected.toLocaleDateString("ru-RU", { weekday: "long", day: "numeric", month: "long" })}</div>
        <div className="cal-habit-list">
          {habits.map((h) => {
            const v = h.days[selectedKey];
            return (
              <div key={h.id} className="cal-habit-row">
                <span className="cal-habit-name">
                  <span className="cal-habit-icon" style={{ background: h.color }} aria-hidden="true" />
                  <span>{h.name}</span>
                </span>
                <button
                  type="button"
                  className={`cal-habit-check ${v ? "on" : ""}`}
                  style={{ "--c": h.color }}
                  aria-label={`Отметить «${h.name}» как выполненную${v ? "" : " (снять)"}`}
                  onClick={() => onToggleHabitDay(h.id, selectedKey)}
                >
                  {v ? "✓" : ""}
                </button>
              </div>
            );
          })}
        </div>
        {tasks.length > 0 && (
          <div className="cal-task-min">
            <span className="cal-task-min-label">
              <span className="cal-task-min-num">{doneTasks}</span>
              <span className="cal-task-min-text">из</span>
              <span className="cal-task-min-num">{totalTasks}</span>
              <span className="cal-task-min-text">задач выполнено</span>
            </span>
            <button
              type="button"
              className="cal-nav-btn"
              onClick={() => onTabChange("plan")}
              aria-label="Открыть планировщик"
            >
              <span aria-hidden="true">→</span>
            </button>
          </div>
        )}
      </div>
    </section>
  );
}
