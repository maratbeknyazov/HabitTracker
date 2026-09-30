import { useState } from "react";
import { useHabits } from "./hooks/useHabits";
import { useTasks } from "./hooks/useTasks";
import { useDailyLog } from "./hooks/useDailyLog";
import { TabId, TABS } from "./types/tab";
import { Calendar } from "./components/Calendar";
import { Tracker } from "./components/Tracker";
import { Plan } from "./components/Plan";
import { dayPercent, monthStats } from "./logic/habits";
import { keyOf } from "./utils/date";

export default function App() {
  const [tab, setTab] = useState<TabId>("tracker");

  const { habits, add: addHabit, remove: removeHabit, toggleDay } = useHabits();
  const { tasks, add: addTask, remove: removeTask, cycleStatus } = useTasks();
  const { log, set, incSleep, decSleep, incWords, decWords, setMood } = useDailyLog();

  const dayPct = dayPercent(habits, keyOf(new Date()));
  const month = monthStats(habits, new Date());

  const doneTasks = tasks.filter((t) => t.status === "Done").length;
  const totalTasks = tasks.length;

  return (
    <div className="app">
      <header className="app-header">
        <div className="app-title">
          <h1>Хабитракер</h1>
          <p>Привычки · Календарь · План</p>
        </div>
        <nav className="app-tabs" role="tablist" aria-label="Основные разделы">
          {TABS.map(({ id, label, icon }) => (
            <button
              key={id}
              type="button"
              role="tab"
              aria-selected={tab === id}
              aria-controls={`panel-${id}`}
              id={`tab-${id}`}
              className={`app-tab ${tab === id ? "active" : ""}`}
              onClick={() => setTab(id)}
            >
              <span className="app-tab-icon" aria-hidden="true">
                {icon}
              </span>
              {label}
            </button>
          ))}
        </nav>
      </header>

      <main className="app-main">
        {tab === "tracker" && (
          <Tracker
            habits={habits}
            dayPercent={dayPct}
            monthStats={month}
            onAddHabit={addHabit}
            onRemoveHabit={removeHabit}
            onToggleHabitDay={toggleDay}
          />
        )}
        {tab === "calendar" && (
          <Calendar
            habits={habits}
            tasks={tasks}
            onToggleHabitDay={toggleDay}
            onTabChange={setTab}
          />
        )}
        {tab === "plan" && (
          <Plan doneTasks={doneTasks} totalTasks={totalTasks} statusData={[]} />
        )}
      </main>

      <footer className="app-footer">
        <div className="app-footer-item">
          <span className="app-footer-label">Сон</span>
          <div className="app-footer-row">
            <button type="button" onClick={() => decSleep()}>−</button>
            <span className="app-footer-value">{log.sleep}h</span>
            <button type="button" onClick={() => incSleep()}>+</button>
          </div>
        </div>
        <div className="app-footer-item">
          <span className="app-footer-label">Слов</span>
          <div className="app-footer-row">
            <button type="button" onClick={() => decWords()}>−</button>
            <span className="app-footer-value">{log.words}</span>
            <button type="button" onClick={() => incWords()}>+</button>
          </div>
        </div>
        <div className="app-footer-item">
          <span className="app-footer-label">Настроение</span>
          <div className="app-footer-row">
            {[
              { label: "десяток", value: "😔", next: 0 },
              { label: "нейтрал", value: "😐", next: 1 },
              { label: "хорош", value: "🙂", next: 2 },
              { label: "отлично", value: "😀", next: 3 },
              { label: "взрыв", value: "🤩", next: 4 },
            ].map((m) => (
              <button
                key={m.value}
                type="button"
                className="app-footer-mood"
                aria-label={m.label}
                onClick={() => setMood(m.value)}
              >
                {m.value}
              </button>
            ))}
          </div>
        </div>
      </footer>

      <div className="app-progress" role="progressbar" aria-label="Индикатор прогресса">
        <div className="app-progress-bar"></div>
      </div>
    </div>
  );
}
