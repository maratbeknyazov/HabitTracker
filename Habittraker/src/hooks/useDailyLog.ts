import { useCallback, useEffect, useState } from "react";
import { DailyLog, createDefaultLog } from "../logic/habits";

const STORAGE_KEY = "hbt:dailyLog";

export function useDailyLog() {
  const [log, setLog] = useState<DailyLog>(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return createDefaultLog();
      return { ...createDefaultLog(), ...JSON.parse(raw) };
    } catch {
      return createDefaultLog();
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(log));
    } catch {
      // ignore
    }
  }, [log]);

  const set = useCallback((patch: Partial<DailyLog>) => {
    setLog((prev) => ({ ...prev, ...patch }));
  }, []);

  const incSleep = useCallback(() => set({ sleep: Math.max(0, log.sleep + 1) }), [log.sleep, set]);
  const decSleep = useCallback(() => set({ sleep: Math.max(0, log.sleep - 1) }), [log.sleep, set]);
  const incWords = useCallback(() => set({ words: Math.max(0, log.words + 50) }), [log.words, set]);
  const decWords = useCallback(() => set({ words: Math.max(0, log.words - 50) }), [log.words, set]);
  const setMood = useCallback(
    (mood: string) => set({ mood }),
    [set]
  );

  return { log, set, incSleep, decSleep, incWords, decWords, setMood };
}
