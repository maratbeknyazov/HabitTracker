import { render, screen, act } from "@testing-library/react";
import { useHabits } from "../useHabits";
import { createMemoryHashRouter } from "react-router-dom";

const useHabitsMock = useHabits as unknown as () => ReturnType<typeof useHabits>;

describe("useHabits", () => {
  it("start with empty habits", () => {
    const { habits } = useHabitsMock();
    expect(habits).toEqual([]);
  });

  it("adds habit", () => {
    const { add } = useHabitsMock();
    const h = add("Test");
    expect(h.name).toBe("Test");
    expect(h.days).toEqual({});
  });
});
