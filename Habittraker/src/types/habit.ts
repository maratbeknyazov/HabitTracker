/**
 * Привычка (habit).
 * `days` хранит карту даты -> выполнено для кратковременной истории.
 */
export interface Habit {
  id: string;
  name: string;
  icon: string;
  color: string;
  goal: number;
  days: Record<string, boolean>;
}
