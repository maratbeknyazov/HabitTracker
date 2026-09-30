/**
 * Скриншот данных для оценки состояния проекта.
 */
export interface PlanSnapshot {
  habits: string[];
  tasks: string[];
  dailyLog: {
    sleep: number;
    mood: string | null;
    words: number;
  };
}
