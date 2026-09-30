import { useState } from "react";
import { Habit } from "../types/habit";
import { MonthStats } from "../logic/habits";
import { keyOf } from "../utils/date";
import "./tracker.css";

interface TrackerProps {
  habits: Habit[];
  onAddHabit: (name: string) => void;
  onRemoveHabit: (id: Habit["id"]) => void;
  onToggleHabitDay: (id: Habit["id"], dateKey: string) => void;
  dayPercent: number;
  monthStats: MonthStats;
}

export function Tracker({
  habits,
  onAddHabit,
  onRemoveHabit,
  onToggleHabitDay,
  dayPercent,
  monthStats,
}: TrackerProps) {
  const [name, setName] = useState("");

  const addHabit = () => {
    if (!name.trim()) return;
    onAddHabit(name.trim());
    setName("");
  };

  const down = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") addHabit();
    if (e.key === "Escape") setName("");
  };

  const weekDates = useState(() => {
    const start = new Date();
    start.setDate(start.getDate() - ((start.getDay() + 6) % 7));
    return Array.from({ length: 7 }, (_, i) => new Date(start.getFullYear(), start.getMonth(), start.getDate() + i));
  })[0];

  return (
    <section className="tracker" aria-label="Трекер привычек">
      <header className="tracker-header">
        <h1 className="tracker-title">Трекер</h1>
        <p className="tracker-date">
          {new Date().toLocaleDateString("ru-RU", { weekday: "long", day: "numeric", month: "long" })}
        </p>
      </header>

      <div className="tracker-progress">
        <div
          className="tracker-progress-ring"
          role="progressbar"
          aria-valuenow={dayPercent}
          aria-valuemin={0}
          aria-valuemax={100}
        >
          <span className="tracker-progress-label">{dayPercent}%</span>
        </div>
        <div className="tracker-progress-summary">
          <span className="tracker-progress-text">
            {habits.filter((h) => h.days[keyOf(new Date())]).length}/{habits.length} привычек сегодня
          </span>
        </div>
      </div>

      {monthStats.perHabit.length > 0 && (
        <div className="tracker-table">
          <div className="tracker-table-head">
            <span>Привычка</span>
            <span>%</span>
            <span>Сделано</span>
          </div>
          {monthStats.perHabit.slice(0, 3).map((h) => (
            <div key={h.id} className="tracker-table-row">
              <span className="tracker-table-name">{h.name}</span>
              <span className="tracker-table-pct">{h.pct}%</span>
              <span className="tracker-table-done">{h.count}/{h.goal}</span>
            </div>
          ))}
        </div>
      )}

      <div className="tracker-habits">
        {habits.map((h) => {
          const checked = h.days[keyOf(new Date())];
          return (
            <div key={h.id} className="tracker-habit">
              <button
                type="button"
                className={`tracker-habit-check ${checked ? "on" : ""}`}
                style={{ "--c": h.color }}
                aria-label={`Пометить «${h.name}» как ${checked ? "выполненную" : "не выполненную"}`}
                onClick={() => onToggleHabitDay(h.id, keyOf(new Date()))}
              >
                {checked ? "✓" : <span className="tracker-habit-icon" />}
              </button>
              <div className="tracker-habit-body">
                <div className="tracker-habit-top">
                  <span className="tracker-habit-name">{h.name}</span>
                  <button
                    type="button"
                    className="tracker-habit-remove"
                    aria-label={`Удалить привычку «${h.name}»`}
                    onClick={() => onRemoveHabit(h.id)}
                  >
                    <span aria-hidden="true">×</span>
                  </button>
                </div>
                <div className="tracker-habit-week">
                  {weekDates.map((d) => {
                    const k = keyOf(d);
                    const v = h.days[k];
                    return (
                      <span
                        key={k}
                        className={`tracker-habit-dot ${v ? "on" : ""}`}
                        style={v ? { background: h.color } : undefined}
                      >
                        {d.toLocaleDateString("ru-RU", { weekday: "short" })}
                      </span>
                    );
                  })}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div className="tracker-extra">
        <div className="tracker-extra-row">
          <span className="tracker-extra-label">Сон</span>
          <div className="tracker-stepper">
            <button type="button" aria-label="Уменьшить часы сна">−</button>
            <span className="tracker-stepper-value">0</span>
            <button type="button" aria-label="Увеличить часы сна">+</button>
          </div>
        </div>
        <div className="tracker-extra-row">
          <span className="tracker-extra-label">Мood</span>
          <div className="tracker-mood">
            {["😔", "😐", "🙂", "😀", "🤩"].map((m) => (
              <button key={m} type="button" className="tracker-mood-btn">
                {m}
              </button>
            ))}
          </div>
        </div>
        <div className="tracker-extra-row">
          <span className="tracker-extra-label">Слов</span>
          <div className="tracker-stepper">
            <button type="button" aria-label="Уменьшить слово">−</button>
            <span className="tracker-stepper-value">0</span>
            <button type="button" aria-label="Увеличить слово">+</button>
          </div>
        </div>
      </div>

      <div className="tracker-task-form">
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          onKeyDown={down}
          placeholder="Новая привычка"
          className="tracker-task-input"
          aria-label="Название привычки"
        />
        <button type="button" className="tracker-task-add" onClick={addHabit} disabled={!name.trim()}>
          <span aria-hidden="true">+</span>
          Добавить
        </button>
      </div>

      <div className="tracker-task-list">
        <div className="tracker-task-item">
          <span className="tracker-task-title">Пример задачи</span>
          <span className="tracker-task-meta">Личное · Средний · В работе</span>
          <button
            type="button"
            className="tracker-task-delete"
            aria-label="Удалить задачу"
          >
            <span aria-hidden="true">×</span>
          </button>
        </div>
      </div>

      <button type="button" className="tracker-add-task" onClick={() => {}}>
        <span aria-hidden="true">+</span>
        Добавить задачу
      </button>
    </section>
  );
}
