# FTR-001: Quick Set Input — Быстрый ввод подхода

**Status:** queued
**Priority:** P0
**Created:** 2026-01-26

---

## Описание

Компонент для максимально быстрого ввода подхода между сетами в зале.
Главное — скорость: вес × повторы за 2-3 тапа.

## User Story

**Как** пользователь в зале,
**я хочу** быстро записать подход (вес и повторы),
**чтобы** не отвлекаться от тренировки.

## Acceptance Criteria

- [ ] Ввод веса через numpad (только цифры + точка)
- [ ] Ввод повторов через numpad
- [ ] Кнопка "Сохранить" добавляет сет
- [ ] Показывает последний сет как референс
- [ ] Можно использовать предыдущий вес (+/- кнопки)
- [ ] Haptic feedback при сохранении

---

## Technical Design

### UI Components

```kotlin
@Composable
fun QuickSetInput(
    exerciseId: Long,
    lastWeight: Float?,
    lastReps: Int?,
    onSave: (weight: Float, reps: Int) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
private fun WeightInput(
    value: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
)

@Composable
private fun RepsInput(
    value: String,
    onValueChange: (String) -> Unit
)
```

### State

```kotlin
data class QuickSetInputState(
    val weight: String = "",
    val reps: String = "",
    val lastWeight: Float? = null,
    val lastReps: Int? = null,
    val isSaving: Boolean = false
)
```

### Новые файлы

```
app/src/main/kotlin/com/gymassistant/
└── ui/components/
    └── QuickSetInput.kt
```

---

## Allowed Files

```
app/src/main/kotlin/com/gymassistant/ui/components/QuickSetInput.kt
app/src/test/kotlin/com/gymassistant/ui/components/QuickSetInputTest.kt
```

---

## Testing

### Unit Tests

- [ ] Weight parsing (int, float, edge cases)
- [ ] Reps validation (positive integers only)
- [ ] Increment/decrement logic

### UI Tests

- [ ] Input focus behavior
- [ ] Save button enabled state
- [ ] Haptic feedback trigger

---

## Design Notes

```
┌─────────────────────────────────────┐
│  Last: 80kg × 10                    │
├─────────────────────────────────────┤
│  ┌─────────┐     ┌─────────┐       │
│  │  [-]    │     │         │       │
│  │  80.0   │  ×  │   10    │       │
│  │  [+]    │     │         │       │
│  └─────────┘     └─────────┘       │
│           kg            reps        │
├─────────────────────────────────────┤
│         [ ✓ SAVE SET ]              │
└─────────────────────────────────────┘
```
