/**
 * Формирует ключ даты в формате YYYY-MM-DD.
 * Используется как совместимый с ISO-строками ключ для сравнения.
 */
export function keyOf(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

/**
 * Добавляет n дней к дате (не изменяет оригинал).
 */
export function addDays(date: Date, n: number): Date {
  const copy = new Date(date);
  copy.setDate(copy.getDate() + n);
  return copy;
}

/**
 * Формирует YYYY-MM (без дня).
 */
export function keyOfMonth(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  return `${year}-${month}`;
}

/**
 * Парсит ISO-строку в Date. Для валидных входных данных.
 */
export function parseDate(value: string): Date {
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) {
    throw new Error(`Invalid date: ${value}`);
  }
  return d;
}
