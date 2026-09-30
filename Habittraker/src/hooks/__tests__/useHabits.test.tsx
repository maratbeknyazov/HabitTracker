import { render, screen } from "@testing-library/react";
import { useHabits } from "../useHabits";

export function HabitScreen() {
  const { habits, add, remove } = useHabits();
  return (
    <div>
      <div data-testid="count">{habits.length}</div>
      <button onClick={() => add("Test")}>Add</button>
      <button onClick={() => remove("1")}>Remove</button>
    </div>
  );
}

describe("useHabits", () => {
  it("renders with empty habits", () => {
    render(<HabitScreen />);
    expect(screen.getByTestId("count")).toHaveTextContent("0");
  });

  it("adds habit", () => {
    render(<HabitScreen />);
    screen.getByText("Add").click();
    expect(screen.getByTestId("count")).toHaveTextContent("1");
  });
});
