# CLAUDE.md
WiseArchitect&OlympicSmart&Concise&Ternary

## Mobile Gym Assistant — Android Workout Tracker

Мобильное приложение для трекинга силовых тренировок.

**Stack:** Kotlin + Jetpack Compose + Room + MVVM + Hilt + Coroutines

**Команды:**
```bash
# Сборка
./gradlew assembleDebug

# Тесты
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests

# Lint
./gradlew lint
./gradlew ktlintCheck

# Полная проверка
./gradlew check
```

---

## Архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  (Jetpack Compose + ViewModels + UI State)                  │
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                            │
│  (Use Cases + Domain Models + Repository Interfaces)        │
├─────────────────────────────────────────────────────────────┤
│                       Data Layer                             │
│  (Room DB + DAOs + Entities + Repository Impl)              │
└─────────────────────────────────────────────────────────────┘
```

**Зависимости:** `ui → domain → data` (Clean Architecture)

---

## Структура проекта

```
app/src/main/kotlin/com/gymassistant/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs
│   │   ├── entity/       # Room Entities
│   │   └── database/     # Room Database
│   └── repository/       # Repository implementations
├── domain/
│   ├── model/            # Domain models
│   ├── repository/       # Repository interfaces
│   └── usecase/          # Business logic
├── di/                   # Hilt modules
├── ui/
│   ├── screens/          # Compose screens
│   │   ├── workout/      # Текущая тренировка
│   │   ├── history/      # История тренировок
│   │   ├── progress/     # Графики прогресса
│   │   └── records/      # Персональные рекорды
│   ├── components/       # Reusable UI components
│   ├── navigation/       # Navigation setup
│   └── theme/            # Material Theme
├── MainActivity.kt
└── GymAssistantApp.kt    # Application class + Hilt
```

---

## Contexts (conditional loading)

| Task | Context | Triggers |
|------|---------|----------|
| Database | `.claude/contexts/database.md` | `**/dao/**`, `**/entity/**`, `**/database/**` |
| UI | `.claude/contexts/compose.md` | `**/ui/**`, `**/screens/**` |
| Testing | `.claude/rules/testing.md` | `**/*Test.kt`, `**/test/**` |
| Debugging | `.claude/rules/debugging.md` | `ai/features/BUG-*` |

---

## Skills (v3.0)

**Правило:** Если скилл подходит — ОБЯЗАТЕЛЬНО использовать.

| Skill | Когда |
|-------|-------|
| **spark** | Новая фича, баг, архитектурное решение → создаёт спеку |
| **autopilot** | Выполнение задач (worktree isolation) |
| **coder** | Написание Kotlin/Compose кода |
| **tester** | Unit tests, UI tests, instrumented tests |
| **review** | Code review перед merge |
| **audit** | Аудит кода (READ-ONLY) |
| **scout** | Исследование Android APIs, библиотек |

**Flows:**
- Feature: `spark → autopilot` (план внутри autopilot)
- Bug: `diagnose → spark → autopilot`
- Hotfix (<5 LOC): фиксим напрямую

---

## Key Rules

### Android-специфичные

1. **Compose first** — никаких XML layouts, только Jetpack Compose
2. **State hoisting** — состояние поднимаем в ViewModel
3. **Coroutines** — для async операций, никаких callbacks
4. **Room** — для локального хранения, миграции обязательны
5. **Hilt** — для DI, аннотации на каждом модуле

### Тестирование

- Unit tests: `src/test/` — ViewModels, UseCases, Repositories
- UI tests: `src/androidTest/` — Compose UI tests
- Покрытие: минимум 70% для domain layer

### Git

- Conventional Commits: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`
- Branch naming: `feature/FTR-XXX`, `fix/BUG-XXX`
- PR в `develop`, merge в `main` через PR

### Кодстайл

- ktlint для форматирования
- Detekt для статического анализа
- Макс 200 строк на файл (кроме сгенерированных)

---

## MVP Features

1. **Быстрый ввод подхода** — вес × повторы за 2 тапа
2. **История тренировок** — список по датам
3. **Графики прогресса** — по каждому упражнению
4. **Персональные рекорды** — автоматический трекинг PR

---

## Backlog

Задачи в `ai/backlog.md`, спеки в `ai/features/`

**Статусы:** draft → queued → in_progress → done

---

## Полезные ссылки

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room](https://developer.android.com/training/data-storage/room)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
