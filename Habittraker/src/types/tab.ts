/**
 * Навигация между вкладками.
 * Состояние — `tab: "tracker" | "calendar" | "plan"`.
 */
export type TabId = "tracker" | "calendar" | "plan";

export const TABS: { id: TabId; label: string; icon: string }[] = [
  { id: "tracker", label: "Трекер", icon: "LayoutGrid" },
  { id: "calendar", label: "Календарь", icon: "CalendarDays" },
  { id: "plan", label: "План", icon: "ListChecks" },
];

export function getTabLabel(tab: TabId): string {
  return TABS.find((t) => t.id === tab)?.label ?? "Трекер";
}
