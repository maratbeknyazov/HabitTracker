import { useCallback, useEffect, useState } from "react";
import { Habit } from "../types/habit";
import { dayPercent, monthStats } from "../logic/habits";
import { generateId } from "../logic/habits";

export function useHabits() {
  const [habits, setHabits] = useState<Habit[]>(() => {
    try {
      const raw = localStorage.getItem("hbt:habits");
      return raw ? (JSON.parse(raw) as Habit[]) : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem("hbt:habits", JSON.stringify(habits));
    } catch {
      // storage unavailable — keep memory state
    }
  }, [habits]);

  const add = useCallback(
    (name: string, icon = "Flame", color = "#F2A65A", goal = 20) => {
      const h: Habit = {
        id: generateId(),
        name: name.trim(),
        icon,
        color,
        goal,
        days: {},
      };
      setHabits((prev) => [...prev, h]);
      return h;
    },
    []
  );

  const remove = useCallback((id: Habit["id"]) => {
    setHabits((prev) => prev.filter((h) => h.id !== id));
  }, []);

  const toggleDay = useCallback(
    (id: Habit["id"], dateKey: string) => {
      setHabits((prev) =>
        prev.map((h) => {
          if (h.id !== id) return h;
          return {
            ...h,
            days: { ...h.days, [dateKey]: !h.days[dateKey] },
          };
        })
      );
    },
    []
  );

  const percent = useCallback(() => dayPercent(habits, keyOf(new Date())), [habits]);
  const month = useCallback(() => monthStats(habits, new Date()), [habits]);

  return { habits, add, remove, toggleDay, percent, month };
}
