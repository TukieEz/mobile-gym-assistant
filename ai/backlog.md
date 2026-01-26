# Backlog — Mobile Gym Assistant

**Max ID:** 8

---

## MVP Tasks

| ID | Задача | Status | Priority |
|----|--------|--------|----------|
| FTR-001 | Quick Set Input — быстрый ввод подхода (вес × повторы) | queued | P0 |
| FTR-002 | Exercise Picker — выбор упражнения с поиском | queued | P0 |
| FTR-003 | Workout Session — экран текущей тренировки | queued | P0 |
| FTR-004 | Workout History — список тренировок по датам | queued | P1 |
| FTR-005 | Exercise Progress — график прогресса по упражнению | queued | P1 |
| FTR-006 | Personal Records — автоматический трекинг PR | queued | P1 |
| ARCH-007 | Hilt DI Setup — настройка dependency injection | queued | P0 |
| ARCH-008 | Navigation Setup — настройка навигации | queued | P0 |

---

## Статусы

| Status | Meaning |
|--------|---------|
| `draft` | Спека в процессе |
| `queued` | Готово к работе |
| `in_progress` | В работе |
| `blocked` | Ждёт решения |
| `done` | Завершено |

---

## Приоритеты

- **P0** — Must have для MVP
- **P1** — Should have для MVP
- **P2** — Nice to have
- **P3** — Future

---

## Порядок работы

1. ARCH-007 (Hilt) → ARCH-008 (Navigation)
2. FTR-001 (Set Input) → FTR-002 (Exercise Picker) → FTR-003 (Workout Session)
3. FTR-004 (History) → FTR-005 (Progress) → FTR-006 (Records)
