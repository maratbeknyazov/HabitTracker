import { render, screen, act } from "@testing-library/react";
import { useTasks } from "../useTasks";
import { createMemoryHashRouter } from "react-router-dom";

const useTasksMock = useTasks as unknown as () => ReturnType<typeof useTasks>;

describe("useTasks", () => {
  it("start with empty tasks", () => {
    const { tasks } = useTasksMock();
    expect(tasks).toEqual([]);
  });

  it("adds task", () => {
    const { add } = useTasksMock();
    const t = add("Test", "Личное", "Средний", "2026-10-01");
    expect(t.title).toBe("Test");
    expect(t.status).toBe("NotStarted");
  });
});
