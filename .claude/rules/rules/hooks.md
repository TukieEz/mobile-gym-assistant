---
paths:
  - ".claude/hooks/**"
  - ".claude/settings.json"
description: Deterministic hooks - automated rule enforcement
---

# Deterministic Hooks (TECH-060)

Hooks enforce rules that CLAUDE.md cannot — deterministic, not suggestions.

## Active Hooks

| Hook | Event | Enforcement |
|------|-------|-------------|
| `pre_bash.py` | PreToolUse:Bash | Block migrations, edge functions deploy, push to main |
| `pre_edit.py` | PreToolUse:Edit | Block protected tests, Ask on LOC limits |
| `pre_status_sync.py` | PreToolUse:Edit | Warn on spec ↔ backlog status drift (ARCH-233-09) |
| `post_edit.py` | PostToolUse:Edit | Auto-format Python files with ruff |
| `post_bash_diary.py` | PostToolUse:Bash | Log file ops to diary triggers (ARCH-233-20) |
| `prompt_guard.py` | UserPromptSubmit | Suggest spark for complex tasks |
| `session-end.sh` | Stop | Soft reminder if >5 pending diary entries |

## Hook Behavior

| Type | Symbol | Meaning |
|------|--------|---------|
| Block | 🔴 | Command/edit rejected, must fix |
| Ask | 🟡 | Requires user confirmation to proceed |

## Override

Only with explicit user confirmation in dialogue.

## Configuration

**Location:** `.claude/settings.json` → hooks section

```json
{
  "hooks": {
    "pre_bash": { "enabled": true },
    "pre_edit": { "enabled": true },
    "prompt_guard": { "enabled": true }
  }
}
```

## When Hooks Trigger

### pre_bash.py
- `supabase db push` without `--dry-run` → 🔴 Block
- `supabase functions deploy` → 🔴 Block (ARCH-233-07: CI only)
- `git push origin main` → 🔴 Block
- `./scripts/supabase-push.sh` without `--dry-run` → 🔴 Block

### pre_edit.py
- Edit file outside `## Allowed Files` in spec → 🔴 Block (ARCH-233-22)
- Edit file in `tests/contracts/` → 🔴 Block
- Edit file in `tests/regression/` → 🔴 Block
- File > 400 LOC after edit → 🟡 Ask

**Allowed Files enforcement:**
- Gets spec path from `CLAUDE_CURRENT_SPEC_PATH` env var or git branch
- Always-allowed: `ai/features/*.md`, `ai/backlog.md`, `.claude/**`, `.gitignore`
- No spec found → graceful degradation (allow all)

### pre_status_sync.py
- Edit `ai/features/*.md` or `ai/backlog.md` with status drift → 🟡 Ask

### prompt_guard.py
- Complex task keywords → 🟡 Suggest spark

### post_edit.py (PostToolUse)
- Python file edited → Auto-format with `ruff format`
- Lint warnings detected → Show warnings (non-blocking)

### post_bash_diary.py (PostToolUse)
- Bash command with file ops (cat, echo >, sed -i) → Log to `.claude/diary-triggers.jsonl`
- Triggers processed by Autopilot before COMMIT → Diary Recorder

---

## Hook Response Schemas (ARCH-233-04)

### PreToolUse hooks (pre_bash.py, pre_edit.py)

**Helpers:** `allow_tool()`, `deny_tool(reason)`, `ask_tool(reason)`

```json
{
  "hookSpecificOutput": {
    "permissionDecision": "allow" | "deny" | "ask",
    "permissionDecisionReason": "string (required for deny/ask)"
  }
}
```

### PostToolUse hooks (post_edit.py)

**Helpers:** `post_continue(message)`, `post_block(reason)`

```json
{
  "decision": "continue" | "block",
  "hookSpecificOutput": {
    "additionalContext": "string (optional for continue, required for block)"
  }
}
```

### UserPromptSubmit hooks (prompt_guard.py)

**Helpers:** `approve_prompt()`, `block_prompt(reason)`

```json
{
  "decision": "approve" | "block",
  "reason": "string (required for block)"
}
```

### Stop hooks (session-end.sh)

**Helpers:** Bash script, outputs JSON

```json
{
  "decision": "approve" | "block",
  "reason": "Injected to Claude's context"
}
```

Allow stop: `{}` or `{"decision": "approve"}`.

**Current behavior:**
- Counts pending diary entries
- Soft reminder if > 5 pending
- Never blocks (soft enforcement)

**Pre-Done Checklist enforcement (ARCH-233-21):**
Stop hooks can't do semantic validation (bash only). Pre-Done Checklist
enforcement is **instruction-based** in SKILL.md section "Pre-Done Checklist (BLOCKING)".

### Usage Example

```python
# PreToolUse hook (pre_bash.py)
from utils import allow_tool, deny_tool, ask_tool

if dangerous_command:
    deny_tool("Command blocked: reason")
elif needs_confirmation:
    ask_tool("Confirm action?")
else:
    allow_tool()

# UserPromptSubmit hook (prompt_guard.py)
from utils import approve_prompt, block_prompt

if blocked:
    block_prompt("Reason for block")
else:
    approve_prompt()

# Stop hook (session-end.sh)
# Block stop with guidance:
echo '{"decision": "block", "reason": "Work remaining..."}'
# Allow stop:
echo '{}'
```
