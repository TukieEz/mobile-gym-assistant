---
name: autopilot
description: Autonomous task execution with subagents (coder, tester, debugger, reviewer)
model: opus
---

# Autopilot v3.0 — Fresh Subagents + Worktree Always

Autonomous execution: Plan → Fresh subagent per task → commit → next.

**Activation:**
- `autopilot` — creates worktree, plans, executes (default)
- `autopilot --no-worktree` — skip worktree (for tiny fixes only)

## TL;DR — Quick Reference

```
PHASE 0: Worktree Setup
  └─ CI check → save MAIN_REPO → worktree → env copy → baseline

PHASE 1: Plan
  └─ [Plan Agent] opus → tasks in spec

PHASE 2: Execute (per task)
  └─ [Coder] sonnet → files
  └─ [Tester] sonnet → pass?
      └─ fail? → [Debugger] opus (max 3)
  └─ DOCUMENTER (inline)
  └─ [Spec Reviewer] sonnet → approved?
  └─ [Code Quality] opus → approved?
  └─ COMMIT (no push)

PHASE 3: Finish
  └─ Final test → status done → push feature (3a) → merge+push develop (3b) → cleanup
```

**Limits:** Debug 3, Refactor 2, ./test fast 5, ./test llm 2
**Escalate:** Bug → Spark (BUG spec) | Architecture → Council | Unclear → Human

---

## Agent & Skill Architecture

**Design Pattern:** Skill = Wrapper over Agent (ARCH-230)

### Two-Layer Architecture

```
.claude/
├── agents/                    ← SOURCE OF TRUTH (pure prompts)
│   ├── planner.md             ← THE prompt (reusable)
│   ├── coder.md               ← THE prompt
│   ├── tester.md              ← THE prompt
│   ├── debugger.md            ← THE prompt
│   ├── scout.md               ← THE prompt
│   ├── spec-reviewer.md       ← THE prompt
│   ├── documenter.md          ← THE prompt
│   └── review.md              ← THE prompt (Code Quality)
│
└── skills/                    ← USER INTERFACE (wrappers + orchestrators)
    ├── autopilot/SKILL.md     ← Orchestrator (dispatches agents/)
    ├── spark/SKILL.md         ← Orchestrator (creates spec)
    ├── plan/SKILL.md          ← Wrapper → agents/planner.md
    ├── scout/SKILL.md         ← Wrapper → agents/scout.md
    ├── review/SKILL.md        ← Wrapper → agents/review.md
    ├── council/SKILL.md       ← Standalone (multi-expert)
    └── reflect/SKILL.md       ← Standalone (synthesis)
```

**Key Principle:**
- **agents/*.md** = pure execution prompts (reusable by Autopilot & user)
- **skills/*/SKILL.md** = user-facing interface (UX, validation, dispatch)

### Skill Types Taxonomy

| Type | Description | Has Agent File? | Examples |
|------|-------------|-----------------|----------|
| **Orchestrator** | Dispatches multiple agents, manages flow | No | autopilot, spark |
| **Wrapper** | Thin layer over single agent | Yes (agents/*.md) | plan, scout, review |
| **Standalone** | Self-contained logic, no reusable agent | No | council, reflect |

### Wrapper Skill Template

```markdown
---
name: planner
description: Detailed implementation planning
agent: .claude/agents/planner.md
---

# Planner Skill

## Activation
"plan", "create plan"

## Process
1. Parse arguments (find spec file)
2. Validate preconditions
3. **Dispatch agent:**
   ```yaml
   Task tool:
     description: "Plan for {TASK_ID}"
     subagent_type: "planner"   # Model from agents/planner.md frontmatter
     prompt: |
       INPUT:
         SPEC_PATH: {spec_path}
         TASK_ID: {task_id}
   ```
4. Report result to user
5. Suggest next step (autopilot)

## User Experience
[Error handling, progress updates, etc.]
```

### Agent File Template

```markdown
# {Name} Agent

You are the {Name} Agent. Your mission: {one-line mission}.

## Input Contract
```yaml
SPEC_PATH: string   # required
TASK_ID: string     # required
```

## Process
[Detailed steps]

## Output Contract
```yaml
status: success | blocked
result: ...
```

## Quality Gates
[Validation rules]
```

### Benefits

1. **SSOT** — prompt lives in one place (agents/)
2. **Reusability** — Autopilot and user call same agent
3. **Separation** — UX logic (skill) ≠ execution logic (agent)
4. **Testability** — can test agent independently

---

## Architecture

```
PHASE 0: WORKTREE SETUP (ALWAYS by default!)
  0. ./scripts/ci-status.sh (CI must be green!)
  1. git worktree add ".worktrees/{ID}" -b "{type}/{ID}"
  2. cd to worktree
  3. Environment setup (spec-driven: python + docker/supabase if needed)
  4. ./test fast (baseline must pass)

PHASE 1: PLAN (if no detailed plan exists)
  [Plan Subagent] → deep analysis → detailed tasks
  Output: "## Detailed Implementation Plan" in spec

PHASE 2: FOR EACH TASK (fresh subagent per task!):
  [Fresh CODER Subagent] → code → files_changed
  [Fresh TESTER Subagent] → Smart Testing
  [DEPLOY CHECK] → migrations? edge functions?
  [DOCUMENTER]
  [TWO-STAGE REVIEW]:
    Stage 1: SPEC REVIEWER — matches spec?
    Stage 2: CODE QUALITY REVIEWER — architecture ok?
  COMMIT to {type}/{ID} branch (NO PUSH yet!)

  (subagent contexts die after each task → main context stays clean)

PHASE 3: FINISHING (SINGLE PUSH!)
  1. ./test fast (final verification)
  2. Pre-Done Checklist
  3. Status → done + commit
  4. Push feature branch (backup)
  5. Merge → develop + SINGLE push
  6. Cleanup worktree + delete branch

DEBUG LOOP (if TESTER fails, max 3):
  - In-scope? → DEBUGGER → CODER fix → TESTER
  - Out-of-scope? → Log + Skip
  - After 3 → ESCALATE (see Escalation Types below)

REFACTOR LOOP (if CODE QUALITY REVIEWER needs_refactor, max 2):
  CODER fix → TESTER → DOCUMENTER → CODE QUALITY REVIEWER
  After 2 → ESCALATE (see Escalation Types below)

ESCALATION TYPES:
  | Type | Trigger | Target | Action |
  |------|---------|--------|--------|
  | bug_unresolved | Debug 3x, code bug | Spark | Create BUG-XXX spec |
  | decision_needed | Architecture unclear | Council | Expert review |
  | human_required | Blocked, unclear | STOP | Ask user |
```

## Plan vs Workflow Separation (CRITICAL)

**План определяет ЧТО:**
- Какой код писать
- Какие файлы создавать/изменять
- Acceptance criteria для каждой задачи

**Autopilot определяет КАК:**
- Фиксированный workflow для КАЖДОЙ задачи
- Gates и checkpoints
- Review процесс

### ⛔ BLOCKING RULE

```
План НЕ определяет workflow!

Даже если план содержит:
  "Task 1: Create file → Task 2: Commit"

Autopilot ОБЯЗАН выполнить:
  CODER (Task 1) → TESTER → DOCUMENTER → REVIEWER → COMMIT

Пропуск любого шага = VIOLATION
```

### Task Execution Template

Для КАЖДОЙ задачи из плана:

```
┌─────────────────────────────────────────────────────┐
│ 1. CODER                                            │
│    Input: Task description from plan                │
│    Output: files_changed: [...]                     │
├─────────────────────────────────────────────────────┤
│ 2. MIGRATION VALIDATION (if migrations/*.sql)       │
│    VALIDATE ONLY — NO APPLY!                        │
│    → squawk lint (local)                            │
│    → dry-run (optional)                             │
│    CI will apply after push!                        │
├─────────────────────────────────────────────────────┤
│ 3. TESTER                                           │
│    Run: Smart Testing based on files_changed        │
│    Gate: All in-scope tests pass                    │
├─────────────────────────────────────────────────────┤
│ 4. DOCUMENTER                                       │
│    Update related docs if needed                    │
├─────────────────────────────────────────────────────┤
│ 5. SPEC REVIEWER (Stage 1)                          │
│    Question: Does code match spec exactly?          │
│    Gate: approved | needs_implementation | needs_removal │
├─────────────────────────────────────────────────────┤
│ 6. CODE QUALITY REVIEWER (Stage 2)                  │
│    Question: Architecture, duplication, quality?    │
│    Gate: approved | needs_refactor                  │
├─────────────────────────────────────────────────────┤
│ 7. COMMIT (NO PUSH yet!)                            │
│    Only after BOTH reviewers approved               │
│    Push happens once in PHASE 3                     │
└─────────────────────────────────────────────────────┘
```

## Pre-Commit Verification (BLOCKING)

⛔ **ПЕРЕД КАЖДЫМ git commit — верифицируй ВСЕ шаги:**

```
[ ] CODER completed — files created/modified
[ ] MIGRATION VALIDATION completed — squawk passed (if any)
[ ] TESTER completed — ./test fast passed OR Smart Testing passed
[ ] DOCUMENTER completed — docs updated (if needed)
[ ] SPEC REVIEWER — status: approved
[ ] CODE QUALITY REVIEWER — status: approved
```

**Если ЛЮБОЙ шаг пропущен:**
1. STOP — не коммитить
2. Вернуться к пропущенному шагу
3. Выполнить
4. Продолжить

**Если в Autopilot Log нет Tester/Reviewer записи → VIOLATION**

### Pre-Commit Self-Check (SAY OUT LOUD — BUG-358)

Before running `git commit`, **verbally confirm** each step:

```
"Coder: ✓ completed — files_changed: [list files]"
"Tester: ✓ passed" (or "skipped — reason: [doc-only/no tests for .md]")
"Spec Reviewer: ✓ approved"
"Code Quality: ✓ approved"
```

⛔ **If ANY is missing or not "approved" → STOP → go back → complete step!**

This verbal confirmation creates explicit checkpoint in conversation — easier to audit.

## Commit Gate

```
⛔ NO COMMIT without BOTH reviewers approved!

Only path:
  SPEC REVIEWER: approved
    → CODE QUALITY REVIEWER: approved
      → COMMIT + PUSH
```

**Note:** Autopilot has explicit permission to push to `develop` branch without asking. This is an exception to CLAUDE.md "don't push without asking" rule.

## Git Worktrees (ALWAYS by Default)

**v3.0 Change:** Worktree is now the DEFAULT for ALL task types!

**Details:** See `autopilot-worktree.md` for full setup guide.

### Quick Reference

| Task Type | Branch | Worktree Path |
|-----------|--------|---------------|
| FTR-XXX | `feature/FTR-XXX` | `.worktrees/FTR-XXX/` |
| BUG-XXX | `fix/BUG-XXX` | `.worktrees/BUG-XXX/` |
| TECH-XXX | `tech/TECH-XXX` | `.worktrees/TECH-XXX/` |

### Skip Worktree

`autopilot --no-worktree` — ONLY for hotfixes <5 LOC or doc-only changes.

## Push Strategy (CI Cost Optimization) — TECH-085

**Rule:** Minimize pushes. ONE push per spec = 80% CI cost reduction.

**Details:** See `autopilot-worktree.md` for complete workflow.

### Plan Subagent (PHASE 1)

If spec doesn't have `## Detailed Implementation Plan`, dispatch Plan subagent first.

**Detection:**
```python
# Check if plan exists
plan_exists = "## Detailed Implementation Plan" in spec_content
             or "## Implementation Plan" in spec_content  # legacy format
```

**If no plan → dispatch Plan subagent:**
```yaml
Task tool:
  description: "Create detailed plan for {TASK_ID}"
  subagent_type: "planner"
  prompt: |
    INPUT:
      SPEC_PATH: ai/features/{TASK_ID}-*.md
      TASK_ID: {task_id}
```

**Wait for plan before proceeding to PHASE 2!**

### Fresh Subagent per Task (PHASE 2)

Each task gets FRESH subagents — no shared context pollution!

**Why fresh subagents:**
- Context isolation → errors caught earlier
- No accumulated garbage
- Main context only sees: "Task N: completed, 3 files, tests pass"
- **Model routing** → cost optimization (sonnet for routine, opus for complex)

**Subagent dispatch (frontmatter provides model + tools):**
```yaml
# PLAN (once at start, if no detailed plan)
Task tool:
  subagent_type: "planner"
  prompt: |
    INPUT:
      SPEC_PATH: ai/features/{TASK_ID}-*.md
      TASK_ID: {task_id}

### Task Parsing Algorithm (MANDATORY)

Before dispatching to CODER, extract these values from spec:

**Step 1: Find current task in spec**
```
Read spec file → find "## Implementation Plan" or "## Detailed Implementation Plan"
Search for "### Task N:" where N = current task number
```

**Step 2: Extract TASK description**
```
# Pattern: ### Task N: [Title]\n\n[Context paragraph]
# Or: ### Task N: [Title]\n**Context:**\n[text]

TASK = title + context (first 2-3 sentences)
```

**Step 3: Extract ALLOWED FILES**
```
Read spec file → find "## Allowed Files"
Parse list of files (numbered or bulleted)
Files = [path1, path2, path3, ...]
```

**Step 4: Extract task-specific files**
```
In "### Task N:" section, find "**Files:**"
Parse: "Create:", "Modify:", "Test:" entries
Task_Files = subset of ALLOWED FILES for this task
```

**Example extraction:**
```
Spec contains:
  ## Allowed Files
  1. src/domains/billing/service.py
  2. src/domains/billing/service_test.py

  ### Task 1: Add calculate_price function
  **Context:** We need pricing logic for campaigns.
  **Files:**
  - Modify: src/domains/billing/service.py:45-60

Extracted:
  TASK = "Add calculate_price function. We need pricing logic for campaigns."
  ALLOWED FILES = ["src/domains/billing/service.py", "src/domains/billing/service_test.py"]
  TASK FILES = ["src/domains/billing/service.py:45-60"]
```

# CODER (for each task)
# First, export spec path for pre_edit.py hook (ARCH-233-22)
export CLAUDE_CURRENT_SPEC_PATH="ai/features/{TASK_ID}-*.md"

Task tool:
  subagent_type: "coder"
  prompt: |
    task: "Task {N}/{M} — {title}"
    type: {code|test|migrate}
    files:
      create: [{from task "Create:" entries}]
      modify: [{from task "Modify:" entries}]
    pattern: "{Research Source URL if any, else 'none'}"
    acceptance: "{from task acceptance criteria}"

# TESTER
Task tool:
  subagent_type: "tester"
  prompt: |
    files_changed: [{list}]
    task_scope: "{TASK_ID}: {current task description}"

# DEBUGGER (if tester fails)
Task tool:
  subagent_type: "debugger"
  prompt: |
    failure:
      test: "{failed_test_name}"
      error: "{traceback}"
    files_changed: [{list}]
    attempt: {debug_attempts}

# SPEC REVIEWER (Stage 1)
Task tool:
  subagent_type: "spec-reviewer"
  prompt: |
    feature_spec: "ai/features/{TASK_ID}*.md"
    task: "Task {N}/{M} — {title}"
    files_changed:
      - path: "{path}"
        action: "{created|modified}"

# CODE QUALITY REVIEWER (Stage 2)
Task tool:
  subagent_type: "review"
  prompt: |
    TASK: {description}
    FILES CHANGED: {list}
```

### Finishing Workflow (SINGLE PUSH!)

After all tasks complete and PRE-DONE CHECKLIST passes:

#### Step 1: Verify Tests
```bash
./test fast
```
**If fail** → STOP, fix before finishing.

#### Step 2: Update Status (BEFORE push!)

```bash
# 1. Update status in spec file: **Status:** done
# 2. Update status in backlog: done
# 3. Commit the status update
git add ai/features/{TASK_ID}*.md ai/backlog.md
git commit -m "docs: mark {TASK_ID} as done"
```

**Why before push:** Status update is part of the feature completion, not a separate action.

#### Step 3a: Push Feature Branch

**Why:** Backup work before merging.

```bash
# Push feature branch to remote (PUSH #1)
git push -u origin {type}/{TASK_ID}
```

**Result:** Feature branch available for review/rollback if needed.

#### Step 3b: Merge to Develop & Push

**No questions asked — full autonomy!**

```bash
# MAIN_REPO was saved at PHASE 0:8 (see worktree setup)
# WORKTREE_PATH was saved at PHASE 0:13 (see worktree setup)

# 1. Rebase feature branch on fresh develop (IN WORKTREE!)
cd "${WORKTREE_PATH}"
git fetch origin develop
git rebase origin/develop

# If rebase conflict → STATUS: blocked, ACTION REQUIRED: "Rebase conflict on {type}/{TASK_ID}. Files: [list]. Need human resolution."

# 2. Force push rebased branch (feature branch only!)
git push -f origin {type}/{TASK_ID}

# 3. Switch to develop in main repo
cd "${MAIN_REPO}"  # exit worktree
git checkout develop
git pull origin develop

# 4. Fast-forward merge (linear history, no overwrites!)
git merge --ff-only {type}/{TASK_ID}

# 5. Push to develop (PUSH #2 — single push to develop per spec)
git push origin develop
```

**Why rebase-first:**
- Conflicts surface during rebase (early), not merge (late)
- Fast-forward merge cannot overwrite — it's linear
- Agent forced to resolve conflicts explicitly
- No silent data loss from parallel agents

**Result:** ONE push to develop per spec (all commits + status update included).

**Note:** Total = 2 pushes (feature + develop), but only 1 push to develop — not 3-5 like before worktrees!

**Rebase Conflict Handling:**
- If `git rebase origin/develop` fails → stop immediately
- Collect conflict files: `git diff --name-only --diff-filter=U`
- Set STATUS: blocked with clear error message
- Do NOT attempt automatic resolution

#### Step 4: Cleanup Worktree

```bash
git worktree remove ".worktrees/${TASK_ID}"
git branch -d {type}/{TASK_ID}  # delete local branch (already merged)
git worktree list  # verify removed
```

#### Manual Override (rare)

If user explicitly requests PR instead of merge:
```bash
git push -u origin {type}/{TASK_ID}
gh pr create --base develop --title "{TASK_ID}: {title}" --body "..."
# Keep worktree until PR merged
```

---

## Forbidden

### Parallel Safety (CRITICAL for multi-autopilot environments)
- ⛔ **NEVER take task with status `in_progress`** — another autopilot is working on it!
- ⛔ **ONLY take tasks with `queued` or `resumed`**
- Before taking ANY task: READ backlog → VERIFY status is `queued` or `resumed`
- If you see `in_progress` → SKIP immediately, find next `queued` task

### Git Safety (CRITICAL for parallel work)
- ⛔ **NEVER push to main** — only `develop` branch
- ⛔ **NEVER force push develop/main** — protected branches
- ✅ **Force push feature branches OK after rebase** — required for rebase workflow
- ⛔ **NEVER resolve conflicts automatically** (rebase or merge)
- Before EVERY push to develop: `git pull origin develop`
- If rebase has conflicts → STATUS: `blocked`, ACTION REQUIRED: "Git conflict, need human"

### Code Quality
- Commit without Reviewer approved
- Group multiple tasks before review
- Skip Documenter or Reviewer
- Run ALL LLM tests without reason (Smart Testing!)
- Fix out-of-scope test failures (Scope Protection!)
- Check DoD at start — DoD is FINAL checklist!

### File Safety
- **Modify files NOT in `## Allowed Files` section** (File Allowlist!)
- **Take tasks with status `draft`** (no plan yet!)

### Test Safety
- **Modify tests in `tests/contracts/` or `tests/regression/`**
- **Change test assertions without user approval**

### Multi-Agent Safety (BUG-314)

**CRITICAL:** Multiple agents may work in parallel (spark, autopilot, background tasks).

**FORBIDDEN git operations in main repo:**
```bash
# ❌ NEVER — destroys untracked files from other agents
git clean -fd

# ❌ DANGEROUS — wipes uncommitted work
git reset --hard
```

**Safe alternatives:**
```bash
# ✅ Only reset tracked files
git checkout -- .

# ✅ Stash with recovery option
git stash -u  # Can restore later

# ✅ Check before deleting
git clean -fdn  # dry-run first
```

**Why:** Untracked files (specs, logs, temp data) may belong to other agents. Destructive operations = data loss.

## Test Safety Protocol

### Immutable Tests (NEVER modify)

These tests are **sacred** — fix code, not tests:

| Path | Marker | Rule |
|------|--------|------|
| `tests/contracts/**` | `@pytest.mark.contract` | API contracts — NEVER touch |
| `tests/regression/**` | `@pytest.mark.regression` | Bug prevention — NEVER touch |

### When Test Fails — Decision Tree

```
TEST FAILED
    │
    ├─ Is test in contracts/ or regression/?
    │   └─ YES → CODE IS WRONG. Fix code, not test.
    │
    ├─ Was test created in current session?
    │   └─ YES → May update if requirements changed
    │
    ├─ Is this a refactoring task?
    │   └─ YES → Behavior should NOT change. Fix code.
    │
    └─ UNCLEAR? → ASK USER:
        "Test `test_X` failed.
         Expected: Y, Got: Z
         Should I fix code or update test?"
```

### Red Flags — STOP and ASK

Before modifying ANY test, check:

- ⛔ About to change assertion values → ASK
- ⛔ About to delete or `@pytest.mark.skip` a test → ASK
- ⛔ About to modify `tests/contracts/**` → REFUSE
- ⛔ About to modify `tests/regression/**` → REFUSE
- ⛔ Test fails after "refactoring" → CODE IS WRONG

### Pre-Commit Test Check

Before REVIEWER approves, verify:

```
[ ] No tests in contracts/ were modified
[ ] No tests in regression/ were modified
[ ] If existing tests modified — user approved
```

## File Allowlist (STRICT)

```
Before CODER modifies ANY file:
1. Check feature spec → "## Allowed Files" section
2. File in list? → proceed
3. File NOT in list? → REFUSE + log:

   "⛔ BLOCKED: {file} not in Allowed Files. Refusing to modify."

4. Need to modify unlisted file? → STATUS → blocked + ACTION REQUIRED
```

**No exceptions.** Spark defines scope, Autopilot enforces it.

## LLM-Friendly Code Gates (ARCH-211)

These rules prevent codebase degradation. Coder and Reviewer enforce them.

### File Size Rule
- ⛔ **NEVER create file > 400 LOC** (600 for tests)
- If implementation requires more → split into multiple files
- Check: `wc -l {file}` before completing

### Export Rule
- ⛔ **NEVER add > 5 exports to `__init__.py`**
- Public API must be minimal
- Check: count items in `__all__` list

### Domain Placement Rule
- ⛔ **NEVER put new code in legacy folders** (`src/services/`, `src/db/`, `src/utils/`)
- New code goes to `src/domains/{domain}/` or `src/infra/`
- Exception: modifying existing legacy code during migration

### Import Direction Rule
- ⛔ **NEVER import "upward"** in dependency graph
- Allowed direction: `shared ← infra ← domains ← api`
- billing → (nothing), campaigns → billing, seller/buyer → campaigns

**Reference:** See ARCH-211 for full LLM-friendly architecture principles.

## How to Read Feature Doc

```
## Зачем (RU)     ← context for understanding
## Контекст (RU)  ← context for understanding
---
## Scope (EN)            ← what to do
## Allowed Files         ← ONLY these can be modified!
## Implementation Plan   ← YOUR TASKS!
  ### Task 1             ← execute in order
  ### Execution Order
---
## Definition of Done    ← check AFTER all tasks!
```

**Rule:**
1. Read `Implementation Plan` → find tasks
2. Execute by `Execution Order`
3. AFTER all tasks → check `Definition of Done`
4. DoD met → feature status → done

## Subagents

**v3.1:** Agent classification by invocation pattern + model routing.

### Internal Agents (called only by Autopilot)

| Agent | subagent_type | When |
|-------|---------------|------|
| Plan | `planner` | PHASE 1 (always) |
| Coder | `coder` | PHASE 2 per task |
| Tester | `tester` | PHASE 2 per task |
| Debugger | `debugger` | If Tester fails (max 3) |
| Spec Reviewer | `spec-reviewer` | PHASE 2 per task |
| Documenter | *(inline)* | PHASE 2 per task (runs in main context) |
| Diary Recorder | `diary-recorder` | On problems detected |

### External Agents (can be called by user OR Autopilot)

| Agent | subagent_type | When |
|-------|---------------|------|
| Scout | `scout` | Research (optional) |
| Code Quality | via `review` skill | PHASE 2 per task |

**Model SSOT:** Model defined in agent frontmatter (`agents/*.md:4`), not here.

### Inline Phases (main context, no Task tool)

| Phase | Why Inline |
|-------|------------|
| Worktree Setup | Simple bash commands |
| Commit/Push | Git operations |

**Model Routing Rationale:**
- **Opus** for: Plan (architecture), Debugger (root cause), Code Quality (deep analysis)
- **Sonnet** for: Coder (90% capability, 2x speed), Tester, Spec Reviewer, Scout
- **Haiku** for: Diary Recorder (fast, cheap)

**Dispatch via Task tool:**
```yaml
Task tool:
  subagent_type: "coder"  # frontmatter provides model + tools
  prompt: |
    TASK: {task description from plan}
    ALLOWED FILES: {from spec}
```

## Context Management — Auto-Compact

### Problem

После работы над несколькими спеками контекст накапливается (~200K tokens).
Качество деградирует, costs растут.

### Solution: Compact After Each Spec

```
AUTOPILOT SESSION
│
├─ Spec 1: FTR-100
│  ├── PHASE 0: Worktree setup
│  ├── PHASE 1: Plan
│  ├── PHASE 2: Tasks 1-5 (context needed between tasks!)
│  └── PHASE 3: Merge + Push
│      └── AUTO-COMPACT ← HERE
│
├─ Spec 2: BUG-200
│  ├── PHASE 0-3...
│  └── AUTO-COMPACT ← HERE
│
└─ No more queued → END
```

### Why Between Specs (Not Tasks)

| Compact After | Pros | Cons |
|---------------|------|------|
| Each task | Max freshness | Loses inter-task context |
| Each spec | Balance: fresh + context | Slightly more accumulation |
| Session end | No overhead | Context explosion |

**Decision:** Compact after each spec — natural boundary, tasks stay connected.

### Implementation

Add to PHASE 3 (Finishing) after cleanup:

```
6. **AUTO-COMPACT:**
   Invoke: /compact
7. Continue to next spec (or END)
```

## Main Loop (v3.0)

### Pre-Loop Validation (MANDATORY — BUG-358)

Before starting loop, detect orphan specs:
```bash
# 1. Count queued in backlog
backlog_count=$(grep -c "| queued |" ai/backlog.md 2>/dev/null || echo 0)

# 2. Count queued in spec files
spec_count=$(grep -l "Status:.*queued" ai/features/*.md 2>/dev/null | wc -l | tr -d ' ')

# 3. Compare
if [ "$spec_count" -gt "$backlog_count" ]; then
  echo "⚠️ Found $(($spec_count - $backlog_count)) orphan specs not in backlog!"
  # List orphan specs
  grep -l "Status:.*queued" ai/features/*.md | while read f; do
    id=$(basename "$f" | grep -oE '^[A-Z]+-[0-9]+')
    if ! grep -q "$id" ai/backlog.md; then
      echo "  - $f (missing from backlog)"
    fi
  done
  # ASK USER: "Add to backlog or skip?"
fi
```

⛔ If orphan specs found → report to user before proceeding!

```
while (queued/resumed tasks in ai/backlog.md):
  1. Read ai/backlog.md
  2. Find first queued/resumed (P0 first)
  3. Status → in_progress (BOTH spec + backlog!)

  ┌─────────────────────────────────────────────────────────────┐
  │ PHASE 0: WORKTREE SETUP (ALWAYS by default!)               │
  └─────────────────────────────────────────────────────────────┘
  4. CI health check: ./scripts/ci-status.sh (must be green!)
  5. Determine branch type from task ID (FTR→feature, BUG→fix, etc.)
  6. Directory selection: .worktrees/ > worktrees/ > create
  7. Safety verification: git check-ignore + auto-fix .gitignore
  8. **Save MAIN_REPO:** `MAIN_REPO="$(git rev-parse --show-toplevel)"` (for Step 3b)
  9. Create worktree: git worktree add ".worktrees/{ID}" -b "{type}/{ID}"
  10. Copy .env: from main repo (gitignored, not in worktree!)
  11. Environment setup: spec-driven (python + docker/supabase if spec requires)
  12. Baseline verification: ./test fast must pass
  13. cd to worktree directory

  ┌─────────────────────────────────────────────────────────────┐
  │ PHASE 1: PLAN (if no detailed plan exists)                 │
  └─────────────────────────────────────────────────────────────┘
  14. Read feature file
  15. Check for "## Detailed Implementation Plan"
  16. If missing → dispatch Plan Subagent
  17. Wait for plan → verify it was added to spec

  ┌─────────────────────────────────────────────────────────────┐
  │ PHASE 2: EXECUTE (fresh subagent per task!)                │
  └─────────────────────────────────────────────────────────────┘
  18. FOR EACH TASK from plan:

      a. [Fresh CODER Subagent] → code → files_changed: [...]
         (subagent context dies after returning)

      b. [Fresh TESTER Subagent] → Smart Testing
         In-scope fail? → DEBUG LOOP (max 3, track retry count!)
         If retry_count > 1 → DIARY RECORDER (test_retry)
         (subagent context dies after returning)

      c. **DEPLOY VALIDATION** (if migrations or functions changed):
         - files_changed has `supabase/migrations/*.sql`?
           → squawk lint (local validation)
           → dry-run (optional, if connected)
           → ⛔ NO APPLY! CI applies after push!
         - files_changed has `supabase/functions/{name}/*`?
           → deno check supabase/functions/{name}/index.ts (local validation)
           → ⛔ NO DEPLOY! CI deploys after push!
         - Validation failed? → STATUS: blocked, fix first

      d. DOCUMENTER → update docs

      e. **TWO-STAGE REVIEW:**
         ┌───────────────────────────────────────────────────┐
         │ Stage 1: SPEC REVIEWER                            │
         │   - Does code match spec exactly?                 │
         │   - needs_implementation → CODER adds             │
         │   - needs_removal → CODER removes                 │
         │   - approved → Stage 2                            │
         │   - Max 2 loops → status: blocked                 │
         └───────────────────────────────────────────────────┘

         **Spec Reviewer Loop (Stage 1):**
         ```
         SPEC REVIEWER returns:
         ├─ approved → proceed to Stage 2
         ├─ needs_implementation:
         │   └─ CODER adds missing features
         │   └─ TESTER verifies
         │   └─ SPEC REVIEWER re-reviews
         │   └─ Max 2 loops → status: blocked
         ├─ needs_removal:
         │   └─ CODER removes extra features
         │   └─ TESTER verifies
         │   └─ SPEC REVIEWER re-reviews
         │   └─ Max 2 loops → status: blocked
         ```
                              ↓
         ┌───────────────────────────────────────────────────┐
         │ Stage 2: CODE QUALITY REVIEWER                    │
         │   - Architecture, duplication, quality            │
         │   - needs_refactor → CODER fix → re-review        │
         │   - approved → COMMIT                             │
         │   - Max 2 loops → Council                         │
         └───────────────────────────────────────────────────┘

      f. COMMIT to {type}/{ID} branch (NO PUSH yet!)
         (main context only sees: "Task N: completed, 3 files")

  ┌─────────────────────────────────────────────────────────────┐
  │ PHASE 3: FINISHING (SINGLE PUSH!)                          │
  └─────────────────────────────────────────────────────────────┘
  18. ./test fast (final verification)
  19. PRE-DONE CHECKLIST — verify ALL items
  20. Update status → done (BOTH spec + backlog!)
  21. Commit: "docs: mark TYPE-XXX as done"
  22. Push feature branch to origin (backup)
  23. Rebase feature branch on develop (IN WORKTREE!)
  24. Force push feature branch (after rebase)
  25. Switch to develop in main repo, pull latest
  26. Fast-forward merge feature → develop
  27. Push develop to origin (SINGLE PUSH — all commits!)
  28. Cleanup worktree + delete local feature branch
  29. AUTO-COMPACT: Run /compact to free context before next spec

  30. Goto 1 (next task)
```

## Status Sync (MANDATORY)

**Статус должен совпадать в ДВУХ местах:**

| Transition | Feature File | Backlog |
|------------|--------------|---------|
| Start work | `**Status:** in_progress` | `in_progress` |
| Blocked | `**Status:** blocked` | `blocked` |
| Complete | `**Status:** done` | `done` |

**При КАЖДОМ изменении статуса:**
1. Обнови `**Status:** X` в файле спеки
2. Обнови статус в строке backlog
3. Верифицируй — оба совпадают?

**⛔ Рассинхрон = путаница. Всегда обновляй ОБА!**

### Status Sync Self-Check (SAY OUT LOUD — BUG-358)

When changing status, **verbally confirm**:

```
"Updating spec file: Status → {new_status}" [Edit spec]
"Updating backlog: Status → {new_status}"   [Edit backlog]
"Both updated? ✓"                           [Verify match]
```

⛔ **Одно место = рассинхрон = путаница для следующего autopilot!**

## Pre-flight Check

Before taking a task from backlog:

1. **Status check:** Must be `queued` or `resumed`
   - `draft` → SKIP (spec not finished by Spark)
   - `in_progress` → SKIP (already working)
   - `blocked` → SKIP (needs human)
   - `done` → SKIP (completed)

2. **Plan check:** Feature file must contain `## Implementation Plan` or `## Detailed Implementation Plan`
   - No plan section → SKIP + log: `"⚠️ Skipping FTR-XXX: no implementation plan"`

Only proceed if BOTH checks pass.

## Smart Testing

**SSOT:** `.claude/agents/tester.md#smart-testing`

## Migration Validation — Git-First (TECH-059)

**SSOT:** `.claude/rules/database.md#migrations`

## Edge Functions — CI Deploy Only (ARCH-233-07)

**Аналогично миграциям — CI единственный источник deploy!**

После CODER завершил работу, проверь `files_changed`:
- Есть `supabase/functions/{name}/*`? → **LOCAL VALIDATION ONLY**

```bash
# Local validation (NO deploy!)
deno check supabase/functions/{function-name}/index.ts
```

⛔ **НИКОГДА не делать `supabase functions deploy` вручную!**
→ CI deploys after push to develop (blocked by pre_bash.py hook).

## Scope Protection

```
If test fails:
1. Related to files_changed? → fix (DEBUGGER)
2. NOT related? → DON'T FIX! Log:

   "⚠️ Out-of-scope: test_X. SKIPPED."

Continue: DOCUMENTER → REVIEWER → COMMIT
```

## Limits

| Situation | Limit | After |
|-----------|-------|-------|
| Debug retry (code bug) | 3 | → Spark (BUG spec) |
| Debug retry (architecture) | 3 | → Council |
| ./test fast fail | 5 | → STOP (ask human) |
| ./test llm fail | 2 | → STOP (ask human) |
| Reviewer refactor | 2 | → Council |
| Out-of-scope | ∞ | skip |

## Escalation Decision Tree

After debug/refactor limits exhausted — choose escalation path:

```
After 3 debug attempts:
├── Is it a CODE BUG in current scope?
│   └── YES → Spark (create BUG-XXX spec)
│       └── "Баг требует отдельной спеки."
│
├── Is it an ARCHITECTURE question?
│   └── YES → Council (expert review)
│       └── "Архитектурный вопрос."
│
├── Is it OUT OF SCOPE?
│   └── YES → Log + Continue
│       └── "Out-of-scope. Skipping."
│
└── UNCLEAR?
    └── STOP → Ask Human
        └── "Не могу определить. Нужна помощь."
```

## Spark Escalation (for bugs)

When code bug can't be fixed after 3 attempts — create BUG spec via Spark.

**How to call:**
```yaml
Skill tool:
  skill: "spark"
  args: |
    MODE: bug
    SYMPTOM: "{test failure or error}"
    ATTEMPTS: [list of what was tried]
    FILES: [files_changed]
```

**Spark will:**
1. Run 5 Whys analysis
2. Find root cause
3. Create BUG-XXX spec
4. Hand off to autopilot (fresh context)

## Council Escalation (for architecture)

When architecture decision needed — escalate to Council via Skill tool.

**How to call:**
```yaml
Skill tool:
  skill: "council"
  args: |
    escalation_type: debug_stuck | refactor_stuck
    feature: "{TASK_ID}"
    task: "{N}/{M} — {name}"
    attempts:
      - attempt: 1
        action: "what did"
        result: "what got"
    current_error: "..."
    hypotheses_rejected:
      - hypothesis: "..."
        reason: "..."
    question: "Specific question"
```

**Council returns:**
- `solution_found` → apply fix, continue
- `architecture_change` → update plan, restart task
- `needs_human` → status: blocked

## Diary Recording (Learning from Problems)

Auto-capture problems for future /reflect analysis.

### Triggers (auto-detect)

| Trigger | Detection | Action |
|---------|-----------|--------|
| `bash_instead_of_tools` | Coder used Bash for file ops (cat, echo >, sed) | Record anti-pattern |
| `test_retry > 1` | Debug loop ran 2+ times | Record debugging difficulty |
| `escalation_used` | Spark or Council was called | Record escalation reason |

### When to Call Diary Recorder

```
After DEBUG LOOP (if retry > 1):
  Task tool:
    subagent_type: "diary-recorder"
    prompt: |
      task_id: "{TASK_ID}"
      problem_type: test_retry
      error_message: "{last error}"
      files_changed: [...]
      attempts: "{what was tried}"

After detecting Bash for file ops:
  Task tool:
    subagent_type: "diary-recorder"
    prompt: |
      task_id: "{TASK_ID}"
      problem_type: bash_instead_of_tools
      error_message: "Used Bash instead of Edit/Write"
      files_changed: [...]
      attempts: "N/A"

After escalation (Spark or Council):
  Task tool:
    subagent_type: "diary-recorder"
    prompt: |
      task_id: "{TASK_ID}"
      problem_type: escalation_used
      error_message: "{escalation reason}"
      files_changed: [...]
      attempts: "{debug attempts before escalation}"
```

### Integration in Main Loop

Track retry counter during DEBUG LOOP:
```
debug_attempts = 0
WHILE test fails AND debug_attempts < 3:
    debug_attempts += 1
    DEBUGGER → CODER → TESTER

# RATE LIMIT: Call ONCE after loop exits, NOT on each retry
# Creates 1 aggregated entry with retry_count, not N separate entries
IF debug_attempts > 1:
    CALL Diary Recorder (test_retry, retry_count=debug_attempts)
```

### Trigger File Processing (ARCH-233-20)

PostToolUse hook `post_bash_diary.py` logs triggers to `.claude/diary-triggers.jsonl`.

**Before COMMIT phase:**
1. Check if `.claude/diary-triggers.jsonl` exists and has entries
2. If > 0 triggers → invoke diary-recorder agent
3. Clear triggers file after processing

```bash
# Check for triggers
if [ -s .claude/diary-triggers.jsonl ]; then
    cat .claude/diary-triggers.jsonl  # Read triggers for prompt
fi
```

```yaml
# DIARY RECORDER (if triggers exist)
Task tool:
  subagent_type: "diary-recorder"
  prompt: |
    TRIGGERS:
    {content of .claude/diary-triggers.jsonl}

    TASK_ID: {current task}

    Process triggers and record learnings to diary.
```

```bash
# Clear after processing
> .claude/diary-triggers.jsonl
```

## Pre-Done Checklist (BLOCKING)

⛔ **Before setting status=done, verify ALL items:**

### Code Quality
- [ ] `./test fast` passes (run it!)
- [ ] No `# TODO` or `# FIXME` in changed files
- [ ] All tasks from Implementation Plan completed

### Definition of Done
- [ ] Each item in spec's "Definition of Done" section checked
- [ ] E2E user journey works (for UI features)

### Documentation
- [ ] If BREAKING/FEATURE change → changelog entry added (MANDATORY)
- [ ] Related docs updated (see `.claude/DOCUMENTATION.md`)

### Autopilot Log Completeness
For EACH task in log, verify:
- [ ] Coder entry present
- [ ] Tester entry present
- [ ] Spec Reviewer entry with status
- [ ] Code Quality entry with status
- [ ] Commit hash present

### Git State
- [ ] All changes committed
- [ ] Pushed to develop (or feature branch if worktree)
- [ ] `git status` shows clean working directory

### Cleanup
- [ ] Autopilot Log updated in spec file
- [ ] Status synced: spec=done AND backlog=done
- [ ] If worktree used → cleanup (see Worktree Cleanup)

**❌ Any item unchecked → status stays `in_progress`, fix first!**

---

## Statuses

| Status | Meaning | Who sets |
|--------|---------|----------|
| draft | Spark in progress / not finished | Spark (interrupted) |
| queued | Spec ready, autopilot can take | Spark (completed) |
| in_progress | Autopilot working | Autopilot (start) |
| blocked | Needs human (see ACTION REQUIRED) | Autopilot (stuck) |
| resumed | Human resolved, continue | Human |
| done | Completed + merged to develop | Autopilot (finish) |

## Autopilot Log

In feature file:
```markdown
## Autopilot Log

### Task N/M: [Name] — YYYY-MM-DD HH:MM
- Coder: completed (N files: file1.py, file2.py)
- Tester: passed | failed → debug loop | skipped (no tests for .md)
- Deploy: applied | skipped (no migrations)
- Documenter: completed | skipped (no docs needed)
- Spec Reviewer: approved | needs_implementation | needs_removal
- Code Quality Reviewer: approved | needs_refactor
- Commit: abc1234 | BLOCKED (reviewer not approved)
```

**Required fields (MANDATORY):**
- Coder — always present
- Tester — always present (even "skipped" is explicit)
- Spec Reviewer — always present
- Code Quality Reviewer — always present
- Commit — always present

**Если в логе отсутствует любое из полей → INCOMPLETE TASK**

## Diary Recorder (v3.1) — Auto-Capture Problems

Автоматически записывает проблемы в дневник когда что-то идёт не так.

### Триггеры (автоматические)

| Триггер | Условие | Действие |
|---------|---------|----------|
| `bash_instead_of_tools` | CODER использовал Bash вместо Edit/Write | Diary Recorder |
| `test_retry > 1` | Тесты падали больше 1 раза | Diary Recorder |

### Где встраивается

```
PHASE 2: FOR EACH TASK:
  CODER → files_changed (track: bash_usage)
  TESTER →
    ├─ pass → continue
    └─ fail → DEBUG LOOP (track: retry_count)
              └─ IF retry > 1 OR bash_used:
                 → [DIARY RECORDER Subagent]
  ...
```

### Diary Recorder Dispatch

```yaml
Task tool:
  subagent_type: "diary-recorder"  # Model from agents/diary-recorder.md frontmatter
  prompt: |
    TASK: {TASK_ID}
    PROBLEM DETECTED:
      - Type: {bash_instead_of_tools | test_retry > 1 | escalation_used}
      - Error: {error_message}
      - Files: {files_changed}
      - Attempts: {what_was_tried}
```

Prompt defined in `agents/diary-recorder.md` — this is just dispatch.

### Формат записи (минимальный)

```markdown
# Session: {TASK_ID} — YYYY-MM-DD

## Проблемы
- [что пошло не так]

## TODO для рефлексии
- Разобрать причину
```

### Структура дневника

```
ai/diary/
├── index.md                    # Таблица со статусами
├── 2026-01-15-BUG-320.md      # Детальная запись
├── 2026-01-15-TECH-087-problem.md  # Auto-captured problem
└── .last_reflect              # Timestamp
```

### index.md формат

```markdown
| Дата | ID | Тип | Суть | Status | Файл |
|------|----|-----|------|--------|------|
| 2026-01-15 | TECH-087 | problem | Bash вместо Edit при миграции | pending | [->](...) |
```

### Терминология

| Действие | Триггеры |
|----------|----------|
| **Запись в дневник** | "запиши в дневник", "сохрани в дневник", auto-capture |
| **Синтез (рефлексия)** | "/reflect", "рефлексия", "давай разберём дневник" |

### Periodic Reflect

После 5+ pending entries — запустить `/reflect`:
- 2+ occurrences = pattern
- 3+ occurrences = MUST add to CLAUDE.md

See `.claude/skills/reflect/SKILL.md` for details.
