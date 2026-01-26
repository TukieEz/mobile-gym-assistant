---
paths:
  - "tests/**"
  - "*_test.py"
  - "conftest.py"
  - "pytest.ini"
---

# Test Architecture

## Test Types

**Always use `-n auto`** for parallel execution (pytest-xdist).

| Type | Location | Pattern | Run |
|------|----------|---------|-----|
| Unit | `src/domains/**` | `*_test.py` | `pytest src/domains/X/ -n auto` |
| Integration | `tests/integration/` | `test_*.py` | `pytest tests/integration/ -n auto` |
| E2E | `tests/e2e/` | `test_*.py` | `pytest tests/e2e/ --e2e -n auto` |
| E2E Telegram | `tests/e2e_telegram/` | `test_*.py` | `pytest tests/e2e_telegram/ -v` |
| LLM | `tests/llm_agent/` | `test_*.py` | `./test llm` |

## Rules

- Unit tests live next to code: `foo.py` → `foo_test.py`
- Cross-domain tests go to `tests/integration/`
- Shared fixtures in root `conftest.py`

## Commands

```bash
./test fast         # lint + unit (skip LLM)
./test              # full cycle (+ LLM)
./test llm          # only LLM tests
pytest --cov=src tests/  # coverage report
```

## E2E Telegram (Telethon)

Real Telegram tests via Telethon — send actual messages to `@AwardyDevBot`.

**Run by area:**

| Changed Area | Run Tests |
|--------------|-----------|
| `handlers/onboarding.py` | `pytest tests/e2e_telegram/test_onboarding.py -v` |
| `handlers/menu.py`, `keyboards/` | `pytest tests/e2e_telegram/test_navigation.py -v` |
| `handlers/offers.py` | `pytest tests/e2e_telegram/test_offers.py -v` |
| `handlers/orders.py` | `pytest tests/e2e_telegram/test_slots.py -v` |
| `handlers/balance.py` | `pytest tests/e2e_telegram/test_balance.py -v` |
| Any buyer bot change | `pytest tests/e2e_telegram/test_smoke.py -v` |

## LLM Tests

**SimUser v2:** Doesn't play bot role, adapts to context, detects loops
**Evaluator v2:** `failure_type` = bot_logic | sim_user | data | unknown

**Files:** `tests/llm_agent/simulator.py`, `evaluator.py`, `scenarios/`
**Logs:** `test_logs/{scenario}_{timestamp}_{STATUS}.md`

## Test Safety (CI enforced)

- Never modify `tests/contracts/` — fix code, not tests
- Never modify `tests/regression/` — prevent bugs from returning
- Never delete/skip tests without user approval

## Test Data Isolation (BUG-320)

**MANDATORY:** All test data MUST have `is_test=True`.

```python
# ❌ WRONG — pollutes dev DB, breaks contract tests
campaign_data = {"seller_id": seller_id, "name": "Test"}

# ✅ CORRECT — isolated from production queries
campaign_data = {"seller_id": seller_id, "name": "Test", "is_test": True}
```

**Tables with is_test flag:**
- `campaigns` — ALWAYS set `is_test=True`
- `offers` — ALWAYS set `is_test=True`
- `sellers` — ALWAYS set `is_test=True`
- `buyers` — ALWAYS set `is_test=True`

**Why:** Contract tests filter `WHERE is_test=FALSE`. Test data without flag pollutes production queries and causes flaky CI.

## Fixtures

Shared fixtures in `conftest.py`:
- `db` — test database client
- `test_seller` — seller fixture with balance
- `test_buyer` — buyer fixture
- `test_campaign` — campaign with slots
