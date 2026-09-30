/**
 * Задача (plan).
 * Надёжный строго типизированный объект для планировщика.
 */
export type TaskStatus = "NotStarted" | "InProgress" | "Done" | "Overdue";

export interface Task {
  id: string;
  title: string;
  category: string;
  priority: string;
  status: TaskStatus;
  due: string;
}

/**
 * Данные категорий для визуализации.
 */
export interface CategoryDataItem {
  name: string;
  value: number;
}

/**
 * Данные статусов для визуализации.
 */
export interface StatusDataItem {
  name: string;
  value: number;
}
