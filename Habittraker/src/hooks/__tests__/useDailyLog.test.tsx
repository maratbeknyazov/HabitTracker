import { render, screen, act } from "@testing-library/react";
import { useDailyLog } from "../useDailyLog";

export function DailyLogScreen() {
  const { log, incSleep, decSleep, incWords, decWords, setMood } = useDailyLog();
  return (
    <div>
      <div data-testid="sleep">{log.sleep}</div>
      <button onClick={incSleep}>Sleep+</button>
      <button onClick={decSleep}>Sleep−</button>
      <button onClick={incWords}>Words+</button>
      <button onClick={decWords}>Words−</button>
      <button onClick={() => setMood("😀")}>Mood</button>
    </div>
  );
}

describe("useDailyLog", () => {
  it("renders with default log", () => {
    render(<DailyLogScreen />);
    expect(screen.getByTestId("sleep")).toHaveTextContent("7");
  });

  it("increments sleep", () => {
    render(<DailyLogScreen />);
    act(() => {
      screen.getByText("Sleep+").click();
    });
    expect(screen.getByTestId("sleep")).toHaveTextContent("8");
  });

  it("increments words by 50", () => {
    render(<DailyLogScreen />);
    act(() => {
      screen.getByText("Words+").click();
    });
    expect(screen.getByTestId("sleep")).toHaveTextContent("50");
  });
});
