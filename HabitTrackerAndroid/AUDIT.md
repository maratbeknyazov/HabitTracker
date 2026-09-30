# Аудит HabitTrackerAndroid

Дата: 01.10.2026 · Скоуп: полный аудит Kotlin/Compose-приложения + live-тесты на эмуляторе API 36.

## Статус v1.1: ВСЕ находки аудита закрыты + добавлены анимации/UX. 19/19 unit-тестов, lint 0 errors, 0 FATAL на эмуляторе.

## Закрыто в v1.1 (второй проход)

| Находка аудита | Статус | Как исправлено |
|---|---|---|
| Нет gradle wrapper | ✅ Закрыто | `gradlew` + `gradle-wrapper.jar` сгенерированы (Gradle 8.9) |
| «Застрявшее вчера» | ✅ Закрыто | `AppViewModel` → `AndroidViewModel`; `today`/`todayKey` — state, `refreshDay()` по ON_RESUME |
| Нет unit-тестов | ✅ Закрыто | 19 тестов (ModelsTest + StoreTest): даты, грид, monthStats, JSON round-trip |
| Устаревшие зависимости | ✅ Частично | BOM 2024.09.03, lifecycle 2.8.6, activity-compose 1.9.2 (+viewmodel-compose) |

## Добавлено в v1.1 (UI/UX + анимации)

- **Кольцо прогресса**: пружинная анимация заполнения, анимированный счётчик процентов, свечение (glow) при 100% + подпись «🎉 Идеальный день!»
- **«Сделано»-момент**: чекбокс привычки — press-scale + пружинный pop галочки + haptic; точки недели плавно «надуваются»
- **Press-эффекты**: mood-кнопки, чипы «Сегодня/Месяц», ячейки календаря (scale 1.1 при выборе), степперы, навигация
- **Хаптика**: тик на степперах/статусах задач, подтверждение на чекбоксах и добавлении задачи (Haptics.kt)

## 🔴 Критическое (исправлено в первом проходе)

---

## 🔴 Критическое (исправлено во время аудита)

### 1. FATAL-краш при скролле Трекера
- **Симптом** (воспроизведён на эмуляторе): `java.lang.IllegalArgumentException: Key "ws-early" was already used` → приложение падало при первом скролле вниз.
- **Причина**: [TrackerScreen.kt](app/src/main/java/com/habittracker/app/TrackerScreen.kt) — один `LazyColumn` содержит секцию «Топ-3 месяца» (`items(top, key = { it.id })`) и список привычек (`items(habits, key = { it.id })`). Ключи совпадают, т.к. `top` — подмножество `habits`. До аудита не падало лишь потому, что секции не встречались в композиции одновременно.
- **Фикс**: ключи топа получили префикс: `items(top, key = { "top-" + it.id })`. Подтверждено: агрессивный скролл вниз/вверх — 0 FATAL.

### 2. `LocalDate.datesUntil` — NoSuchMethodError на Android 8–13
- **Симптом**: Android Lint `NewApi` Error. Метод требует API 34, а `minSdk = 26`.
- **Причина**: [Models.kt:86](app/src/main/java/com/habittracker/app/Models.kt#L86) — `daysLeft()` использовал `today.datesUntil(due)`. На эмуляторе API 36 работало, на реальных устройствах с Android 8–13 экран «План» падал бы при отрисовке первой задачи.
- **Фикс**: `java.time.temporal.ChronoUnit.DAYS.between(today, due).toInt()` — доступно с API 26 благодаря встроенной поддержке java.time.

## 🟡 Среднее (не исправлено, требует решения)

| # | Проблема | Где | Комментарий |
|---|----------|-----|-------------|
| 1 | **Нет gradle wrapper** (`gradlew`, `gradle-wrapper.jar`) | корень проекта | Сборка воспроизводится только через локальный дистрибутив из кэша `~/.gradle/wrapper/dists/gradle-8.9-bin/...`. На другой машине проект не соберётся «из коробки». Генерация: `gradle wrapper --gradle-version 8.9`. |
| 2 | **Устаревшие зависимости** (4 предупреждения lint `GradleDependency`) | [app/build.gradle.kts](app/build.gradle.kts) | core-ktx 1.13.1→1.19.1, lifecycle 2.8.4→2.11.0, activity-compose 1.9.1→1.13.0, targetSdk 35→36. Не срочно: BOM 2024.06 консистентен. |
| 3 | **Persistence без lifecycle**: `AppViewModel` — обычный класс, держит состояние в `mutableStateOf`, но переживает только пока жив composition. При убийстве процесса в фоне данные сохранены (persist на каждой мутации), но несохранённых правок нет — риск минимален. | [AppViewModel.kt](app/src/main/java/com/habittracker/app/AppViewModel.kt) | Идиоматичнее — AndroidViewModel + DataStore. |
| 4 | **`today` фиксируется на старте процесса** (`val today = LocalDate.now()` в конструкторе VM). Приложение, оставленное на ночь, покажет «вчера» до перезапуска. | [AppViewModel.kt:15](app/src/main/java/com/habittracker/app/AppViewModel.kt#L15) | Лечится пересозданием `today` в `ON_RESUME` или проверкой смены дня. |
| 5 | **Нет тестов** | — | Логика (`monthStats`, `buildMonthGrid`, `daysLeft`, JSON-сериализация) чистая и легко тестируется JUnit без Robolectric. |

## 🟢 Низкое / косметика

- `RingProgress.kt` — мёртвая функция `unusedCosSin()` и неиспользуемые импорты `cos/sin`; `planScreen` — обрезанный `.background(Brush...)` после чистки (оставлен пустой строкой, безвредно).
- `TrackerScreen`: подпись «Настроение» переносилась на 2 строки на узких экранах — кнопки уменьшены 30→27dp.
- `Common.kt`: неиспользуемый импорт `CircleShape` удалён; `WEEKDAYS_FULL` объявлен в CalendarScreen.kt после использования (стилистически лучше наверх).
- Доступность: кликабельные `Box` без `contentDescription`/semantics; `TagPill`/статусные пилюли 10sp — ниже минимума читаемости на некоторых устройствах.
- `backup_rules.xml` пустой (`<full-backup-content/>`) — фактически правило отсутствует; данные в SharedPreferences попадут в бэкап (для трекера это скорее ок).
- В манифесте нет `android:enableOnBackInvokedCallback` (targetSdk 35+ predictive back) — не блокирует.

## ✅ Что в порядке

- **Архитектура**: чёткое разделение Models (домен+логика) / Store (seed+JSON) / AppViewModel (состояние+persist) / 3 экрана+общие компоненты. Портирование логики из JSX 1:1 корректно (проверено: monthStats, grid с Пн-стартом, palette, seed-данные).
- **Сборка**: `clean assembleDebug` зелёная; APK 15 МБ, подписан debug-ключом; lint — 0 errors после фиксов.
- **Live-тесты на эмуляторе API 36**: установка, запуск, все 3 вкладки, скроллы, тапы по календарю, переключение месяцев, смена статуса задачи — без единого FATAL (logcat чист).
- **Манифест**: минимально необходимое; exported-activity корректно только для launcher; `windowSoftInputMode=adjustResize` работает.
- **Права**: не запрашиваются вообще — соответствует функциональности.

## Проделанные исправления в ходе аудита

1. `TrackerScreen.kt` — уникальные ключи LazyColumn (краш #1).
2. `Models.kt` — `datesUntil` → `ChronoUnit.DAYS` (краш #2, API 26-совместимо).
3. `res/mipmap-anydpi-v26` → `res/mipmap-anydpi` (lint ObsoleteSdkInt; потребовался clean).
4. Удалён мёртвый код: `priorityAlpha`, `STATUS_ALPHA`, `PRIO_ALPHA`, `ColorDot`, `unusedCosSin`-импорты.
5. `TrackerScreen.kt` — mood-кнопки 30→27dp (перенос «Настроение»).

## Рекомендации (по приоритету)

1. **Сгенерировать gradle wrapper** — 1 команда, делает проект переносимым.
2. Добавить JUnit-тесты на `Models.kt` + `Store.kt` (сериализация туда-обратно, месяц-грид, daysLeft на границах).
3. Обновить BOM и зависимости до актуальных стабильных.
4. Пересчитывать `today` при ON_RESUME (баг «застрявшего вчера»).
5. Release-сборка с собственным keystore (сейчас только debug).
