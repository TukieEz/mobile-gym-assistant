---
name: coder
description: Write/modify code for autopilot tasks
agent: .claude/agents/coder.md
---

# Coder Skill (Wrapper)

Вызывает coder субагент для написания/изменения кода в рамках одной задачи.

> **Architecture:** This skill is a WRAPPER over `.claude/agents/coder.md`.
> The agent file is the source of truth for the coder prompt.

## When to Use

**Internal:** Called by autopilot for each task in implementation plan

**Standalone:** Rarely used directly — prefer using autopilot workflow

## Invocation

```yaml
Task tool:
  description: "Implement task N"
  subagent_type: "coder"
  prompt: |
    TASK: {task description from plan}
    ALLOWED FILES: {from spec}

    Execute and report files_changed: [list]
```

## Output

```yaml
files_changed:
  - path/to/file1.py
  - path/to/file2.py
status: completed | blocked
```

## Notes

- Coder MUST respect File Allowlist from spec
- Coder MUST use Edit/Write tools, not Bash
- Coder outputs only file paths, not implementation details
