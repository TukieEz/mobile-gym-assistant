#!/usr/bin/env python3
"""
Session End Hook — анализирует сессию и сохраняет паттерны.
"""

import json
import sys
import hashlib
from pathlib import Path
from datetime import datetime

MEMORY_DIR = Path.home() / ".claude" / "memory"


def get_project_hash(cwd: str) -> str:
    """Генерирует hash для проекта по пути."""
    return hashlib.md5(cwd.encode()).hexdigest()[:12]


def ensure_dirs(project_hash: str):
    """Создаёт директории если не существуют."""
    project_dir = MEMORY_DIR / "projects" / project_hash
    project_dir.mkdir(parents=True, exist_ok=True)

    instincts_dir = MEMORY_DIR / "instincts"
    instincts_dir.mkdir(parents=True, exist_ok=True)

    return project_dir


def extract_patterns_from_transcript(transcript_path: str) -> list:
    """
    Извлекает паттерны из транскрипта сессии.
    В реальной реализации — анализ через Haiku.
    Здесь — упрощённая версия.
    """
    patterns = []

    try:
        transcript = Path(transcript_path)
        if not transcript.exists():
            return patterns

        # Простой анализ: ищем повторяющиеся tool calls
        tool_counts = {}

        for line in transcript.read_text().split('\n'):
            if not line:
                continue
            try:
                entry = json.loads(line)
                if entry.get("type") == "tool_use":
                    tool_name = entry.get("tool", "")
                    tool_input = str(entry.get("input", {}))[:100]
                    key = f"{tool_name}:{tool_input}"
                    tool_counts[key] = tool_counts.get(key, 0) + 1
            except:
                continue

        # Паттерны = действия повторённые 3+ раз
        for key, count in tool_counts.items():
            if count >= 3:
                patterns.append({
                    "pattern": key.split(":")[0],
                    "count": count,
                    "timestamp": datetime.now().isoformat()
                })

    except Exception:
        pass

    return patterns


def save_patterns(project_dir: Path, patterns: list):
    """Сохраняет паттерны в JSONL файл."""
    if not patterns:
        return

    patterns_file = project_dir / "patterns.jsonl"

    with open(patterns_file, "a") as f:
        for pattern in patterns:
            f.write(json.dumps(pattern) + "\n")


def check_for_instincts(project_dir: Path):
    """
    Проверяет нужно ли создать instinct.
    Если паттерн появился 5+ раз — создаём instinct.
    """
    patterns_file = project_dir / "patterns.jsonl"

    if not patterns_file.exists():
        return

    # Считаем частоту паттернов
    pattern_counts = {}

    for line in patterns_file.read_text().strip().split('\n'):
        if not line:
            continue
        try:
            entry = json.loads(line)
            pattern = entry.get("pattern", "")
            pattern_counts[pattern] = pattern_counts.get(pattern, 0) + 1
        except:
            continue

    # Создаём instincts для частых паттернов
    instincts_dir = MEMORY_DIR / "instincts"

    for pattern, count in pattern_counts.items():
        if count >= 5:
            instinct_file = instincts_dir / f"{pattern.lower().replace(' ', '_')}.md"

            if not instinct_file.exists():
                content = f"""# Instinct: {pattern}

Этот паттерн был обнаружен {count} раз.

Автоматически сгенерировано: {datetime.now().strftime('%Y-%m-%d')}
"""
                instinct_file.write_text(content)


def main():
    try:
        hook_data = json.load(sys.stdin)
        cwd = hook_data.get("cwd", "")
        transcript_path = hook_data.get("transcript_path", "")

        if not cwd:
            sys.exit(0)

        project_hash = get_project_hash(cwd)
        project_dir = ensure_dirs(project_hash)

        # Извлекаем и сохраняем паттерны
        if transcript_path:
            patterns = extract_patterns_from_transcript(transcript_path)
            save_patterns(project_dir, patterns)

        # Проверяем нужно ли создать instincts
        check_for_instincts(project_dir)

        sys.exit(0)

    except Exception as e:
        # Silent fail
        sys.exit(0)


if __name__ == "__main__":
    main()
