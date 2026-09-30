import { describe, expect, it } from "@jest/globals";
import { dayPercent, monthStats } from "../habits";
import { Habit } from "../../types/habit";
import { addDays, keyOf } from "../../utils/date";

function makeHabit(over: Partial<Habit> = {}): Habit {
  return {
    id: "1",
    name: "Test",
    icon: "Flame",
    color: "#F2A65A",
    goal: 20,
    days: {},
    ...over,
  };
}

describe("dayPercent", () => {
  it("возвращает 0 для пустого списка", () => {
    expect(dayPercent([], keyOf(addDays(new Date(), 0)))).toBe(0);
  });

  it("считает процент выполненных привычек", () => {
    const today = keyOf(addDays(new Date(), 0));
    const habits = [
      makeHabit({ id: "1", days: { [today]: true } }),
      makeHabit({ id: "2", days: {} }),
      makeHabit({ id: "3", days: { [today]: true } }),
    ];
    expect(dayPercent(habits, today)).toBe(67);
  });

  it("округляет до целого", () => {
    const today = keyOf(addDays(new Date(), 0));
    const habits = [
      makeHabit({ id: "1", days: { [today]: true } }),
      makeHabit({ id: "2", days: {} }),
      makeHabit({ id: "3", days: {} }),
    ];
    expect(dayPercent(habits, today)).toBe(33);
  });
});

describe("monthStats", () => {
  it("считает выполнения в пределах месяца", () => {
    const today = new Date(2026, 8, 15); // сентябрь
    const start = keyOf(addDays(today, -3));
    const habits = [
      makeHabit({
        id: "1",
        goal: 20,
        days: { [start]: true, [keyOf(addDays(today, 1))]: true },
      }),
      makeHabit({ id: "2", goal: 20, days: {} }),
    ];

    const stats = monthStats(habits, today);
    expect(stats.fact).toBe(2);
    expect(stats.plan).toBe(20);
    expect(stats.percent).toBe(10);
  });
});
