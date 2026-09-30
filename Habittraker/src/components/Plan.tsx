import "./plan.css";

interface PlanProps {
  doneTasks: number;
  totalTasks: number;
  statusData: { name: string; value: number }[];
}

export function Plan({ doneTasks, totalTasks, statusData }: PlanProps) {
  return (
    <section className="plan" aria-label="Планировщик">
      <header className="plan-header">
        <h1 className="plan-title">План на день</h1>
        <p className="plan-quote">Делать то, что не хочется, ради того, что по-настоящему хочешь.</p>
      </header>

      <div className="plan-stats">
        <div className="plan-stat">
          <span className="plan-stat-value">{totalTasks}</span>
          <span className="plan-stat-label">всего</span>
        </div>
        <div className="plan-stat">
          <span className="plan-stat-value">{doneTasks}</span>
          <span className="plan-stat-label">выполнено</span>
        </div>
      </div>

      <div className="plan-charts">
        <div className="plan-chart">
          <h2 className="plan-chart-title">По категориям</h2>
          <div className="plan-chart-bar">
            <span className="plan-chart-bar-label">Личное</span>
            <span className="plan-chart-bar-value">12</span>
          </div>
          <div className="plan-chart-bar">
            <span className="plan-chart-bar-label">Работа</span>
            <span className="plan-chart-bar-value">8</span>
          </div>
          <div className="plan-chart-bar">
            <span className="plan-chart-bar-label">Дом</span>
            <span className="plan-chart-bar-value">6</span>
          </div>
        </div>

        <div className="plan-chart plan-chart--pie">
          <h2 className="plan-chart-title">По статусу</h2>
          {statusData.map((s) => (
            <div key={s.name} className="plan-chart-pie">
              <span className="plan-chart-pie-label">{s.name}</span>
              <span className="plan-chart-pie-value">{s.value}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="plan-tasks">
        <div className="plan-task">
          <span className="plan-task-check">☐</span>
          <span className="plan-task-text">Пробежка</span>
          <span className="plan-task-detail"> Делюсь с друзьями</span>
        </div>
        <div className="plan-task">
          <span className="plan-task-check">☐</span>
          <span className="plan-task-text">Читать книгу</span>
          <span className="plan-task-detail"> 20 стр.</span>
        </div>
        <div className="plan-task">
          <span className="plan-task-check">☐</span>
          <span className="plan-task-text">Позвать родителей</span>
          <span className="plan-task-detail"> Завтра</span>
        </div>
      </div>

      <div className="plan-actions">
        <button type="button" className="plan-apply">
          <span aria-hidden="true">✓</span>
          Применить
        </button>
        <button type="button" className="plan-clear">
          <span aria-hidden="true">✕</span>
          Очистить
        </button>
      </div>
    </section>
  );
}
