---
paths:
  - "supabase/**"
  - "*.sql"
  - "src/infra/db/**"
applies_to: all
---

# Database Rules (Supabase)

## Stack

PostgreSQL via Supabase (hosted). Extensions: `uuid-ossp`, `vector`.

## Environments

| Environment | Git Branch | Supabase Project |
|-------------|------------|------------------|
| development | `develop` | `ualoolkrejjmvmqihgen` |
| production | `main` | `tactedclhiqsyfwajubz` |

Local `.env` → always development credentials.

## Key Tables

| Table | Purpose |
|-------|---------|
| `sellers` | Seller accounts (B2B) |
| `buyers` | Buyer accounts (B2C) |
| `campaigns` | Purchase campaigns |
| `offers` | Available offers |
| `slots` | Individual tasks |
| `transactions` | Financial operations |

## Migrations

**Format:** `YYYYMMDDHHmmss_description.sql`

### Git-First Workflow (TECH-059)

⛔ **Autopilot НИКОГДА не применяет миграции! CI — единственный источник apply.**

```
CODER → VALIDATE → COMMIT → PUSH → CI applies
```

### Rules

| Forbidden | Allowed |
|-----------|---------|
| `supabase db push` | `squawk supabase/migrations/*.sql` |
| `./scripts/supabase-push.sh` (без --dry-run) | `./scripts/supabase-push.sh --dry-run` |
| Apply before commit | `supabase migration new` |
| Direct SQL via Dashboard | Commit + Push → CI |

### Validation (MANDATORY)

1. `squawk supabase/migrations/*.sql` — passes
2. Destructive ops? → require user approval
3. `./scripts/supabase-push.sh --dry-run` — optional

### Destructive Operations (REQUIRE APPROVAL)

`DROP TABLE`, `DROP COLUMN`, `TRUNCATE`, `DELETE FROM`, `ALTER ... DROP`

### Timestamp Conflicts (TECH-072)

Pre-push hook validates ordering. If conflict → `./scripts/migration-rename.sh` BEFORE commit.

### Ghost Migrations

Record in remote without git file. Fix: `supabase migration repair <ts> --status reverted --linked`

### Smart Validation (TECH-088)

**STRICT** (file has `$$`): ONE statement per file
**RELAXED** (simple DDL): Batch allowed, use `DO/EXECUTE` for 4+ statements

### Migration Safety (ARCH-226)

- Migrations = main context ONLY (no background agents)
- Never parallel specs for same DB functions
- Verify generated SQL: squawk + dry-run + manual review

## Hard Reset (TECH-082)

Reset develop DB. **Only develop, never production!**

```bash
# 1. Disable hooks
cp .claude/settings.json .claude/settings.json.bak
jq 'del(.hooks)' .claude/settings.json > tmp && mv tmp .claude/settings.json

# 2. Reset schema
source .env && psql "$DATABASE_URL" << 'EOF'
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
ALTER SCHEMA public OWNER TO pg_database_owner;
TRUNCATE supabase_migrations.schema_migrations;
EOF

# 3. Push + restore hooks
git commit --no-verify --allow-empty -m "chore: trigger CI (hard reset)"
git push --no-verify origin develop
cp .claude/settings.json.bak .claude/settings.json
```

## Squawk Linter (TECH-052)

```bash
squawk supabase/migrations/*.sql
```

| Rule | Description |
|------|-------------|
| `adding-required-field` | NOT NULL without DEFAULT locks table |
| `ban-drop-column` | DROP COLUMN causes downtime |
| `require-concurrent-index-creation` | CREATE INDEX blocks writes |

Config: `.squawk.toml`

## CLI Reference

```bash
supabase migration new <name>     # Create
supabase migration list           # Status
supabase db push                  # Apply (CI only!)
supabase inspect db table-sizes   # Inspect
```

## Repositories

Domain repos in `src/domains/{domain}/repositories/`:

```python
from src.domains.billing import seller_balances_repo
from src.domains.campaigns import campaign_repo, slot_repo
```

## Schema Reference

CHECK constraints → `supabase/migrations/00000000000000_baseline.sql`

## Other

- **Soft Delete:** `archived_at` + `archived_reason`
- **RLS:** Enabled on all public tables
- **Edge Functions:** `supabase/functions/`
- **Protection:** Scripts blocked from production by hardcoded project ref
