---
name: memory
description: |
  Memory persistence между сессиями.
  Автоматически сохраняет паттерны и предпочтения.
  Загружается при старте сессии.
---

# Memory Persistence

## Назначение
Сохраняет "память" между сессиями Claude Code:
- Твои предпочтения (стиль кода, любимые паттерны)
- Частые ошибки и как их избегать
- Выученные паттерны проекта

## Структура памяти

```
~/.claude/memory/
├── identity.json       # Глобальные предпочтения
├── projects/
│   └── {project-hash}/
│       ├── patterns.jsonl    # Выученные паттерны
│       ├── mistakes.jsonl    # Ошибки и решения
│       └── preferences.json  # Предпочтения проекта
└── instincts/
    └── *.md              # Автосгенерированные правила
```

## Как работает

### При старте сессии (SessionStart hook):
1. Загружает `identity.json`
2. Загружает `projects/{hash}/preferences.json`
3. Добавляет в контекст как system reminder

### При завершении сессии (Stop hook):
1. Анализирует сессию через Haiku
2. Извлекает паттерны (повторяющиеся действия 3+ раз)
3. Сохраняет в `patterns.jsonl`
4. Если паттерн стабильный (5+ раз) → генерирует instinct

## Identity.json пример

```json
{
  "coding_style": {
    "prefer_explicit_types": true,
    "max_function_length": 30,
    "naming": "snake_case"
  },
  "workflow": {
    "always_run_tests": true,
    "commit_style": "conventional"
  },
  "communication": {
    "language": "ru",
    "verbosity": "concise"
  }
}
```

## Instinct пример

После 5 раз исправления одной и той же ошибки:

```markdown
# Instinct: Python async context managers

Когда вижу `async with` в Python:
- Проверь что ресурс правильно закрывается
- Используй `asynccontextmanager` декоратор
- Не забудь `await` при выходе

Источник: 5 исправлений в проекте AwardyBot (2026-01)
```

## Интеграция

Модуль автоматически активируется через hooks в settings.json.
Не требует ручного вызова.
