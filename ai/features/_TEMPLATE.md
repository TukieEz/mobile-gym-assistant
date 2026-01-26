# [FTR/BUG/ARCH]-XXX: Title

**Status:** draft | queued | in_progress | blocked | done
**Priority:** P0 | P1 | P2 | P3
**Created:** YYYY-MM-DD

---

## Описание

Краткое описание задачи.

## User Story (для FTR)

**Как** [пользователь],
**я хочу** [действие],
**чтобы** [результат].

## Acceptance Criteria

- [ ] Критерий 1
- [ ] Критерий 2
- [ ] Критерий 3

---

## Technical Design

### Затрагиваемые слои

- [ ] UI (Compose)
- [ ] ViewModel
- [ ] UseCase
- [ ] Repository
- [ ] DAO/Entity
- [ ] Database migration

### Новые файлы

```
app/src/main/kotlin/com/gymassistant/
├── ui/screens/[screen]/
│   ├── [Screen]Screen.kt
│   ├── [Screen]ViewModel.kt
│   └── [Screen]UiState.kt
├── domain/usecase/[feature]/
│   └── [Name]UseCase.kt
└── data/...
```

### Изменения в существующих файлах

- `file1.kt` — описание изменений
- `file2.kt` — описание изменений

---

## Allowed Files

Список файлов которые можно изменять:

```
app/src/main/kotlin/com/gymassistant/ui/screens/[screen]/**
app/src/main/kotlin/com/gymassistant/domain/usecase/[feature]/**
app/src/test/kotlin/com/gymassistant/**
```

---

## Testing

### Unit Tests

- [ ] ViewModel tests
- [ ] UseCase tests
- [ ] Repository tests

### UI Tests

- [ ] Screen navigation
- [ ] User interactions
- [ ] Edge cases

---

## Notes

Дополнительные заметки, ссылки, референсы.
