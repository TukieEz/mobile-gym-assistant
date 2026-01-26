# Autopilot: Git Worktrees

**v3.0 Change:** Worktree is now the DEFAULT for ALL task types!

## Why ALWAYS Worktree?

- **Isolation** — can discard without traces
- **PR Review** — code review before merge
- **Parallel work** — multiple tasks simultaneously
- **Safety** — develop branch stays clean

## Branch Naming by Type

| Task Type | Branch | Worktree Path |
|-----------|--------|---------------|
| FTR-XXX | `feature/FTR-XXX` | `.worktrees/FTR-XXX/` |
| BUG-XXX | `fix/BUG-XXX` | `.worktrees/BUG-XXX/` |
| TECH-XXX | `tech/TECH-XXX` | `.worktrees/TECH-XXX/` |
| ARCH-XXX | `arch/ARCH-XXX` | `.worktrees/ARCH-XXX/` |

## Skip Worktree (rare)

```bash
autopilot --no-worktree
```

Use ONLY for:
- Hotfixes <5 LOC
- Doc-only changes
- Config tweaks

---

## Worktree Setup (PHASE 0)

### Step 0: CI Health Check (FIRST!)

```bash
./scripts/ci-status.sh
```

| Result | Action |
|--------|--------|
| ✅ Green | Proceed to Step 1 |
| ❌ Red | **BLOCK IMMEDIATELY** |

**On red CI:**
```
STATUS: blocked
REASON: CI is red
URL: {workflow_url}
ACTION REQUIRED: Fix CI separately, then resume with `autopilot`
```

**⛔ NEVER attempt to fix CI!**
- Autopilot's job = execute spec
- CI fix = separate task (possibly separate spec)

**Override (rare):**
```bash
autopilot --skip-ci-check
```

### Step 1: Directory Selection

```bash
# Check existing directories
ls -d .worktrees 2>/dev/null     # Preferred (hidden)
ls -d worktrees 2>/dev/null      # Alternative
```

| Found | Action |
|-------|--------|
| `.worktrees/` exists | Use it |
| `worktrees/` exists | Use it |
| Neither exists | Create `.worktrees/` |

### Step 2: Safety Verification

**MUST verify directory is gitignored:**
```bash
git check-ignore -q .worktrees 2>/dev/null
```

**If NOT ignored** → auto-fix:
```bash
echo ".worktrees/" >> .gitignore
git add .gitignore
git commit -m "chore: add .worktrees to gitignore"
```

### Step 3: Create Worktree

```bash
TASK_ID="FTR-100"
TASK_TYPE="FTR"
WORKTREE_DIR=".worktrees"

# Determine branch prefix by type
case $TASK_TYPE in
  FTR)      BRANCH_PREFIX="feature" ;;
  BUG)      BRANCH_PREFIX="fix" ;;
  TECH)     BRANCH_PREFIX="tech" ;;
  REFACTOR) BRANCH_PREFIX="refactor" ;;
  SEC)      BRANCH_PREFIX="security" ;;
  ARCH)     BRANCH_PREFIX="arch" ;;
  *)        BRANCH_PREFIX="task" ;;
esac

WORKTREE_PATH="${WORKTREE_DIR}/${TASK_ID}"
git worktree add "$WORKTREE_PATH" -b "${BRANCH_PREFIX}/${TASK_ID}"
cd "$WORKTREE_PATH"
```

### Step 4: Copy Environment Files

```bash
MAIN_REPO=$(git rev-parse --show-toplevel)
cp "${MAIN_REPO}/.env" "${WORKTREE_PATH}/.env" 2>/dev/null || true
cp -r "${MAIN_REPO}/.supabase" "${WORKTREE_PATH}/.supabase" 2>/dev/null || true
```

**Why:** `.env` and `.supabase/` are gitignored → `git worktree add` doesn't copy them.

### Step 5: Environment Setup (idempotent, spec-driven)

```bash
echo "=== Environment Setup ==="

# 5.1 Python (always)
echo "Setting up Python..."
/Users/desperado/.local/bin/uv venv 2>/dev/null && echo "Created venv" || echo "Using existing venv"
/Users/desperado/.local/bin/uv pip install -e ".[dev]"
echo "Python ready"

# 5.2 Node.js (if present)
if [ -f package.json ]; then
  echo "Installing Node dependencies..."
  npm install
  echo "Node ready"
fi

# 5.3 Docker (if spec requires)
if grep -q "docker: true" "${SPEC_PATH}" 2>/dev/null; then
  echo "Starting Docker services..."
  docker-compose up -d
  echo "Docker ready"
fi

# 5.4 Supabase (if spec requires)
if grep -q "supabase: true" "${SPEC_PATH}" 2>/dev/null; then
  echo "Starting Supabase local..."
  supabase start
  echo "Supabase ready"
fi

echo "=== Environment Setup Complete ==="
```

### Step 6: Baseline Verification

```bash
./test fast
```

| Result | Action |
|--------|--------|
| ✅ Pass | Proceed with tasks |
| ❌ Fail | STOP + report: "Baseline tests failing. Fix before proceeding." |

**⛔ NO RETRY LOOP!** If fails after proper setup → real problem.

---

## Worktree Workflow Summary

```
0. CI HEALTH CHECK — ./scripts/ci-status.sh (must be green!)
1. DIRECTORY SELECTION — .worktrees/ > worktrees/ > create
2. SAFETY VERIFICATION — git check-ignore + auto-fix
3. CREATE worktree + branch
4. COPY .env — from main repo
5. ENVIRONMENT SETUP — spec-driven (python + docker/supabase if needed)
6. BASELINE VERIFICATION — ./test fast
7. WORK in isolated directory
8. COMMIT + PUSH to {type}/{TASK_ID}
9. PRE-DONE CHECKLIST passes
10. REBASE onto develop + FAST-FORWARD MERGE (rebase-first workflow)
11. CLEANUP worktree + delete branch
```

---

## Resumed Status Handling

When task has `status: resumed`:

1. **Delete stale worktree (if exists):**
   ```bash
   if [ -d ".worktrees/${TASK_ID}" ]; then
     git worktree remove ".worktrees/${TASK_ID}" --force
   fi
   ```

2. **Start fresh:** Full PHASE 0 (new worktree)

3. **Re-read spec:** Plan may have been updated

**Rationale:** Fresh start safer than resuming corrupted state.

---

## Conflict Handling

If rebase on develop has conflicts:

```
STATUS: blocked
ACTION REQUIRED: "Rebase conflict on {type}/{TASK_ID}. Files: [list]. Need human resolution."
```

⛔ **NEVER resolve rebase conflicts automatically** — requires human judgment.

Note: With rebase-first workflow, conflicts surface during `git rebase origin/develop` in the worktree, NOT during merge. The `git merge --ff-only` step should never have conflicts if rebase succeeded.

---

## Worktree Cleanup

After successful merge to develop:

```bash
TASK_ID="FTR-100"  # Use actual task ID
TASK_TYPE="${TASK_ID%%-*}"  # Extract type from ID (e.g., FTR from FTR-100)

# Determine branch prefix (same logic as setup)
case $TASK_TYPE in
  FTR)      BRANCH_PREFIX="feature" ;;
  BUG)      BRANCH_PREFIX="fix" ;;
  TECH)     BRANCH_PREFIX="tech" ;;
  REFACTOR) BRANCH_PREFIX="refactor" ;;
  SEC)      BRANCH_PREFIX="security" ;;
  ARCH)     BRANCH_PREFIX="arch" ;;
  *)        BRANCH_PREFIX="task" ;;
esac

# Delete worktree
git worktree remove ".worktrees/${TASK_ID}" --force

# Delete feature branch (local only)
git branch -D "${BRANCH_PREFIX}/${TASK_ID}"

# Verify clean state
git worktree list
```
