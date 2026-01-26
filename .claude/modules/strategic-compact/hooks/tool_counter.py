#!/usr/bin/env python3
"""
Strategic Compact Hook — считает tool calls и предлагает compact.
"""

import json
import sys
import os
from pathlib import Path

# Конфигурация
THRESHOLD = int(os.environ.get("COMPACT_THRESHOLD", 50))
REMINDER_INTERVAL = int(os.environ.get("COMPACT_REMINDER", 25))
COUNTER_FILE = Path("/tmp/claude_tool_counter.json")


def load_counter() -> dict:
    """Загружает счётчик."""
    if COUNTER_FILE.exists():
        try:
            return json.loads(COUNTER_FILE.read_text())
        except:
            pass
    return {"count": 0, "last_reminder": 0}


def save_counter(data: dict):
    """Сохраняет счётчик."""
    COUNTER_FILE.write_text(json.dumps(data))


def should_remind(count: int, last_reminder: int) -> bool:
    """Проверяет нужно ли напомнить о compact."""
    if count < THRESHOLD:
        return False

    if last_reminder == 0:
        return True

    return (count - last_reminder) >= REMINDER_INTERVAL


def get_suggestion_message(count: int) -> str:
    """Формирует сообщение с рекомендацией."""
    if count >= THRESHOLD * 2:
        urgency = "⚠️ РЕКОМЕНДУЕТСЯ"
    else:
        urgency = "💡 Рекомендация"

    return f"""{urgency}: Использовано {count} tool calls в этой сессии.

Хорошее время для `/compact`:
- Завершили планирование → перед кодированием
- Закончили дебаг → перед следующей задачей
- Между разными фичами

Это сохранит контекст и улучшит качество ответов."""


def main():
    try:
        hook_data = json.load(sys.stdin)
        tool_name = hook_data.get("tool_name", "")

        # Не считаем некоторые лёгкие операции
        skip_tools = ["Read", "Glob", "Grep"]
        if tool_name in skip_tools:
            sys.exit(0)

        # Загружаем и обновляем счётчик
        counter = load_counter()
        counter["count"] += 1

        # Проверяем нужно ли напомнить
        if should_remind(counter["count"], counter["last_reminder"]):
            message = get_suggestion_message(counter["count"])
            print(message, file=sys.stderr)
            counter["last_reminder"] = counter["count"]

        save_counter(counter)
        sys.exit(0)

    except Exception as e:
        sys.exit(0)


if __name__ == "__main__":
    main()
