import { useCallback, useMemo, useState } from "react";
import { Habit } from "../types/habit";
import { keyOf } from "../utils/date";

const PALETTE = ["#F2A65A", "#4FD1C5", "#8C9EFF", "#F27E7E", "#B784F2", "#6ED0A0"];

export function generateId(): string {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }
  return `id-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

export function createHabit({
  name = "",
  icon = "Flame",
  color = PALETTE[0],
  goal = 20,
  days = {},
}: {
  name?: string;
  icon?: string;
  color?: string;
  goal?: number;
  days?: Record<string, boolean>;
} = {}): Habit {
  return {
    id: generateId(),
    name: name.trim(),
    icon,
    color,
    goal,
    days,
  };
}

export function createEmptyHabit(days: Record<string, boolean> = {}): Habit {
  return {
    id: generateId(),
    name: "",
    icon: "Flame",
    color: PALETTE[0],
    goal: 20,
    days,
  };
}

export interface MonthStatsItem {
  id: string;
  name: string;
  count: number;
  pct: number;
}

export interface MonthStats {
  percent: number;
  fact: number;
  plan: number;
  perHabit: MonthStatsItem[];
}

export function dayPercent(habits: Habit[], dateKey: string): number {
  if (habits.length === 0) return 0;
  const checked = habits.filter((h) => h.days[dateKey]).length;
  return Math.round((checked / habits.length) * 100);
}

export function monthStats(habits: Habit[], today: Date): MonthStats {
  const year = today.getFullYear();
  const month = today.getMonth();
  const elapsed = Math.max(1, today.getDate());

  const perHabit = habits.map((h) => {
    let count = 0;
    for (const k of Object.keys(h.days)) {
      if (k.startsWith(`${year}-${String(month + 1).padStart(2, "0")}`) && h.days[k]) {
        count++;
      }
    }
    return {
      id: h.id,
      name: h.name,
      count,
      pct: Math.round((count / Math.max(1, Math.min(h.goal, elapsed))) * 100),
    };
  });

  const fact = perHabit.reduce((sum, h) => sum + h.count, 0);
  const plan = perHabit.reduce((sum, h) => sum + Math.min(h.goal, elapsed), 0);

  return { percent: Math.round((fact / Math.max(1, plan)) * 100), fact, plan, perHabit };
}

export interface DailyLog {
  sleep: number;
  mood: string | null;
  words: number;
}

export function createDefaultLog(): DailyLog {
  return { sleep: 7, mood: null, words: 0 };
}

export function loadDailyLog(): DailyLog {
  try {
    const raw = localStorage.getItem("hbt:dailyLog");
    if (!raw) return createDefaultLog();
    const parsed = JSON.parse(raw) as DailyLog;
    return { ...createDefaultLog(), ...parsed };
  } catch {
    return createDefaultLog();
  }
}
