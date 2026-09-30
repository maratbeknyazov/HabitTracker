import { render, screen, act } from "@testing-library/react";
import { useDailyLog } from "../useDailyLog";
import { createMemoryHashRouter } from "react-router-dom";

const useDailyLogMock = useDailyLog as unknown as () => ReturnType<typeof useDailyLog>;

describe("useDailyLog", () => {
  it("start with default log", () => {
    const { log } = useDailyLogMock();
    expect(log.sleep).toBe(7);
    expect(log.words).toBe(0);
    expect(log.mood).toBeNull();
  });

  it("increments sleep", () => {
    const { log, incSleep, decSleep } = useDailyLogMock();
    act(() => incSleep());
    expect(log.sleep).toBe(8);
    act(() => decSleep());
    expect(log.sleep).toBe(7);
  });

  it("increments words by 50", () => {
    const { log, incWords, decWords } = useDailyLogMock();
    act(() => incWords());
    expect(log.words).toBe(50);
    act(() => decWords());
    expect(log.words).toBe(0);
  });
});
