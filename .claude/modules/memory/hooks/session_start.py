#!/usr/bin/env python3
"""
Session Start Hook — загружает память при старте сессии.
"""

import json
import sys
import hashlib
from pathlib import Path

MEMORY_DIR = Path.home() / ".claude" / "memory"
IDENTITY_FILE = MEMORY_DIR / "identity.json"


def get_project_hash(cwd: str) -> str:
    """Генерирует hash для проекта по пути."""
    return hashlib.md5(cwd.encode()).hexdigest()[:12]


def load_identity() -> dict:
    """Загружает глобальные предпочтения."""
    if IDENTITY_FILE.exists():
        try:
            return json.loads(IDENTITY_FILE.read_text())
        except:
            pass
    return {}


def load_project_memory(project_hash: str) -> dict:
    """Загружает память проекта."""
    project_dir = MEMORY_DIR / "projects" / project_hash
    prefs_file = project_dir / "preferences.json"

    if prefs_file.exists():
        try:
            return json.loads(prefs_file.read_text())
        except:
            pass
    return {}


def load_recent_patterns(project_hash: str, limit: int = 5) -> list:
    """Загружает последние паттерны."""
    patterns_file = MEMORY_DIR / "projects" / project_hash / "patterns.jsonl"
    patterns = []

    if patterns_file.exists():
        try:
            lines = patterns_file.read_text().strip().split('\n')
            for line in lines[-limit:]:
                if line:
                    patterns.append(json.loads(line))
        except:
            pass

    return patterns


def load_instincts() -> list:
    """Загружает все instincts."""
    instincts_dir = MEMORY_DIR / "instincts"
    instincts = []

    if instincts_dir.exists():
        for f in instincts_dir.glob("*.md"):
            try:
                instincts.append(f.read_text())
            except:
                pass

    return instincts


def main():
    try:
        hook_data = json.load(sys.stdin)
        cwd = hook_data.get("cwd", "")

        if not cwd:
            sys.exit(0)

        project_hash = get_project_hash(cwd)

        # Загружаем всё
        identity = load_identity()
        project_memory = load_project_memory(project_hash)
        patterns = load_recent_patterns(project_hash)
        instincts = load_instincts()

        # Формируем контекст для Claude
        context_parts = []

        if identity:
            context_parts.append(f"## User Preferences\n```json\n{json.dumps(identity, indent=2)}\n```")

        if project_memory:
            context_parts.append(f"## Project Memory\n```json\n{json.dumps(project_memory, indent=2)}\n```")

        if patterns:
            patterns_text = "\n".join([f"- {p.get('pattern', '')}" for p in patterns])
            context_parts.append(f"## Recent Patterns\n{patterns_text}")

        if instincts:
            instincts_text = "\n\n---\n\n".join(instincts[:3])  # Max 3 instincts
            context_parts.append(f"## Learned Instincts\n{instincts_text}")

        if context_parts:
            # Выводим как system reminder
            output = {
                "message": "\n\n".join(context_parts),
                "type": "context"
            }
            print(json.dumps(output))

        sys.exit(0)

    except Exception as e:
        # Silent fail — не блокируем сессию
        sys.exit(0)


if __name__ == "__main__":
    main()
