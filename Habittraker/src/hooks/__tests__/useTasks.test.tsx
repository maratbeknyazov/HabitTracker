import { render, screen } from "@testing-library/react";
import { useTasks } from "../useTasks";

export function TaskScreen() {
  const { tasks, add, remove, cycleStatus } = useTasks();
  return (
    <div>
      <div data-testid="count">{tasks.length}</div>
      <button onClick={() => add("Test", "Личное", "Средний", "2026-10-01")}>Add</button>
      <button onClick={() => remove("1")}>Remove</button>
      <button onClick={() => cycleStatus("1")}>Cycle</button>
    </div>
  );
}

describe("useTasks", () => {
  it("renders with empty tasks", () => {
    render(<TaskScreen />);
    expect(screen.getByTestId("count")).toHaveTextContent("0");
  });

  it("adds task", () => {
    render(<TaskScreen />);
    screen.getByText("Add").click();
    expect(screen.getByTestId("count")).toHaveTextContent("1");
  });
});
