import { describe, expect, it } from "@jest/globals";
import { keyOf, addDays, keyOfMonth } from "../date";

describe("keyOf", () => {
  it("создаёт YYYY-MM-DD", () => {
    expect(keyOf(new Date(2026, 8, 5))).toBe("2026-09-05");
  });
});

describe("addDays", () => {
  it("добавляет дни", () => {
    const a = new Date(2026, 8, 30);
    const b = addDays(a, 3);
    expect(b.getDate()).toBe(3);
    expect(b.getMonth()).toBe(9);
  });

  it("не мутирует оригинал", () => {
    const a = new Date(2026, 8, 30);
    const b = addDays(a, 3);
    expect(a.getDate()).toBe(30);
  });
});

describe("keyOfMonth", () => {
  it("создаёт YYYY-MM", () => {
    expect(keyOfMonth(new Date(2026, 0, 15))).toBe("2026-01");
  });
});
