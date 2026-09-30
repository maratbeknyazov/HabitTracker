import { useCallback, useEffect, useMemo, useState } from "react";
import { STATUS_ORDER } from "../types/tasks";
import { Task } from "../types/task";

export function useTasks() {
  const [tasks, setTasks] = useState<Task[]>(() => {
    try {
      const raw = localStorage.getItem("hbt:tasks");
      return raw ? (JSON.parse(raw) as Task[]) : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem("hbt:tasks", JSON.stringify(tasks));
    } catch {
      // storage full / unavailable — ignore
    }
  }, [tasks]);

  const add = useCallback(
    (title: string, category: string, priority: string, due: string) => {
      const t: Task = {
        id: crypto.randomUUID(),
        title: title.trim(),
        category,
        priority,
        status: "NotStarted",
        due,
      };
      setTasks((prev) => [...prev, t]);
      return t;
    },
    []
  );

  const remove = useCallback(
    (id: Task["id"]) => {
      setTasks((prev) => prev.filter((t) => t.id !== id));
    },
    []
  );

  const cycleStatus = useCallback(
    (id: Task["id"]) => {
      setTasks((prev) =>
        prev.map((t) => {
          if (t.id !== id) return t;
          const idx = STATUS_ORDER.indexOf(t.status);
          const next = idx === -1 ? "NotStarted" : STATUS_ORDER[(idx + 1) % STATUS_ORDER.length];
          return { ...t, status: next };
        })
      );
    },
    []
  );

  const doneCount = useMemo(() => tasks.filter((t) => t.status === "Done").length, [tasks]);
  const totalCount = useMemo(() => tasks.length, [tasks]);

  return { tasks, add, remove, cycleStatus, doneCount, totalCount };
}
