# Audit — Systematic Code Analysis

Structured analysis for tasks that are NOT features or bugs.

**Activation:** `audit`, `/audit`

## When to Use

- Code audit: "найди все места где X"
- Review: "посмотри логи, найди паттерн"
- Consistency check: "нигде не осталось Y?"
- Refactor-scan: "где ещё обновить после Z"
- Security scan: "проверь на уязвимости"
- Documentation check: "актуальна ли документация"

**Don't use:**
- New features → `/spark`
- Bug fixes → `/spark` (5 whys mode)
- Hotfixes <5 LOC → fix directly

## Principles

1. **READ-ONLY** — Audit NEVER modifies files (except creating report in `ai/`)
2. **Systematic** — Cover ALL relevant files, not just obvious ones
3. **Structured Output** — Tables, lists, clear categories
4. **Actionable** — End with recommendations or task list
5. **Thorough** — Check code, tests, configs, docs, migrations, scripts

## Process (4 Phases)

### Phase 1: Scope Definition

Ask/clarify:
```
1. What pattern/issue are we looking for?
2. Where to search? (default: entire codebase)
3. What output format? (list, table, report)
```

**Examples:**
- "копейки vs рубли" → search for currency unit mismatches
- "deprecated imports" → search for imports from legacy paths
- "hardcoded values" → search for magic numbers/strings

### Phase 2: Systematic Search

**Tools to use:**
| Tool | Purpose |
|------|---------|
| `Grep` | Pattern search across files |
| `Glob` | File discovery by pattern |
| `Read` | Content analysis |
| `Task(Explore)` | Deep codebase exploration |

**Search locations (check ALL):**
- `src/` — main code
- `tests/` — test code
- `supabase/migrations/` — database
- `scripts/` — automation
- `docs/` — documentation
- `.claude/` — Claude configs
- `ai/` — specs and architecture

**Multiple patterns:**
Search for synonyms and variations:
```
# For "kopecks" search:
- kopeck, копейк, _kop
- * 100, / 100, *100, /100
- rubles, рубл, _rub
```

### Phase 3: Findings Report

**Output format:**
```markdown
## Findings: [Topic]

### Summary
- Total occurrences: N
- Files affected: M
- Severity breakdown: X critical, Y warning, Z info

### Details

| # | File | Line | Issue | Severity |
|---|------|------|-------|----------|
| 1 | path/file.py | 42 | description | 🔴 Critical |
| 2 | path/other.py | 17 | description | 🟡 Warning |
| 3 | path/test.py | 88 | description | 🔵 Info |

### Patterns Observed
- [Pattern 1]: found in N places
- [Pattern 2]: found in M places
```

**Severity levels:**
| Level | Meaning |
|-------|---------|
| 🔴 Critical | Breaks functionality, security issue |
| 🟡 Warning | Inconsistency, tech debt |
| 🔵 Info | Style, documentation |

### Phase 4: Recommendations

**End with actionable items:**
```markdown
## Recommendations

### Immediate (P0)
1. **[Action]** — [Why] — Files: [list]

### Soon (P1)
2. **[Action]** — [Why] — Files: [list]

### Eventually (P2)
3. **[Action]** — [Why] — Files: [list]

---

## Create Tasks?

Found N issues requiring fixes. Create backlog tasks?
- [ ] TECH-XXX: Fix pattern A (N occurrences)
- [ ] BUG-XXX: Fix pattern B (M occurrences)
```

## Key Rules

| ✅ Allowed | ⛔ Forbidden |
|-----------|-------------|
| Read any file | Modify code files |
| Search entire codebase | Skip locations |
| Create report in `ai/` | Create files elsewhere |
| Add tasks to backlog | Execute fixes |
| Use all search tools | Guess without verifying |

## Examples

### Example 1: Currency Units Audit

**Input:** "Прочеши код на копейки vs рубли"

**Phase 1:**
```
Looking for: currency unit mismatches (kopecks vs rubles)
Where: entire codebase
Output: table with file:line:issue
```

**Phase 2:**
```bash
# Grep patterns
Grep: "kopeck|копейк|_kop"
Grep: "\\* 100|\\/ 100"
Grep: "_rub|rubles|рубл"
```

**Phase 3:**
```markdown
## Findings: Currency Units

### Summary
- 12 occurrences found
- 8 files affected
- 3 critical, 5 warning, 4 info

### Details
| # | File | Line | Issue | Severity |
|---|------|------|-------|----------|
| 1 | billing/payment.py | 142 | amount * 100 without comment | 🔴 |
...
```

**Phase 4:**
```markdown
## Recommendations

1. **Unify to kopecks** — all internal amounts in kopecks
2. **Add conversion helpers** — `to_kopecks()`, `to_rubles()`
3. **Document convention** — update BILLING.md

Create TECH-XXX for unification?
```

### Example 2: Deprecated Imports

**Input:** "Найди все deprecated imports"

**Phase 1:**
```
Looking for: imports from legacy paths
Paths: src/services/, src/db/, src/utils/, src/agents/
Output: list by file
```

**Phase 2:**
```bash
Grep: "from src\\.services\\."
Grep: "from src\\.db\\."
Grep: "from src\\.utils\\."
Grep: "from src\\.agents\\."
```

**Phase 3:** Table of all deprecated imports

**Phase 4:** Migration plan with priority order

## Integration with Other Skills

| After Audit | Next Step |
|-------------|-----------|
| Found bugs | `/spark` for each bug |
| Need refactoring | Create REFACTOR-XXX spec |
| Need new feature | `/spark` for feature |
| Just reporting | Done, no action |

## Output

After audit completes:
```yaml
status: complete
findings: N issues in M files
report: ai/audit/YYYY-MM-DD-topic.md (if saved)
tasks_created: [TECH-XXX, BUG-YYY] (if any)
```
