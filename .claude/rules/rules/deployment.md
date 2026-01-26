---
paths:
  - ".github/**"
  - "Dockerfile*"
  - "docker-compose*.yml"
  - "deploy/**"
  - "merge_env.py"
---

# Deployment Rules

## Environments

| Environment | Branch | Trigger | URL |
|-------------|--------|---------|-----|
| **development** | `develop` | Push to develop | VDS: `/home/deploy/AwardyBot` |
| **production** | `main` | Manual only | VDS: `/home/deploy/AwardyBot-prod` |

## CI Pipeline (ci.yml)

Runs on every push/PR to `main` and `develop`:

1. **Lint** — Ruff, docs sync, spec compliance, file sizes, imports
2. **Migrations** — Dry-run check against development DB
3. **Test** — pytest with coverage

## Deploy to DEV (deploy-dev.yml)

**Trigger:** Push to `develop`

```
1. Apply migrations (supabase db push)
2. Deploy Edge Functions (if changed)
3. SSH to VDS
4. git pull origin develop
5. merge_env.py (sync .env with .env.example)
6. docker compose down/build/up
```

### Edge Functions Deploy (ARCH-233-07)

**Trigger:** Automatically on push to `develop` (if `supabase/functions/**` changed)

```bash
# CI deploys all functions:
for func in supabase/functions/*/; do
  supabase functions deploy "$(basename $func)" --linked
done
```

**Local validation only:**
```bash
deno check supabase/functions/{name}/index.ts
```

⛔ **NO manual deploy** — blocked by `pre_bash.py` hook.

## Deploy to PROD (deploy-prod.yml)

**Trigger:** Manual workflow_dispatch only

**Requires:** Type "deploy" to confirm

```
1. Checkout main branch
2. Apply migrations to production DB
3. SSH to VDS
4. git reset --hard origin/main
5. merge_env.py
6. docker compose -f ... -f docker-compose.prod.yml up -d
```

## Docker Compose

```bash
# Development (local)
docker compose up -d

# Production (VDS)
docker compose -f docker-compose.yml -f deploy/docker-compose.prod.yml up -d
```

## Entry Points (uvicorn)

| Service | Module | Port |
|---------|--------|------|
| API | `src.api.http.main:app` | 8000 |
| SellerBot | `src.api.telegram.seller_bot:main` | — |
| BuyerBot | `src.api.telegram.buyer_bot:main` | — |

## ENV Management

**merge_env.py** — syncs `.env.example` → `.env`:
- Adds new vars from example
- Fails on missing required vars
- Preserves existing values

## Secrets (GitHub)

| Secret | Used In |
|--------|---------|
| `DATABASE_URL` | Migrations |
| `VDS_HOST`, `VDS_USER`, `VDS_SSH_KEY` | SSH deploy |
| `TELEGRAM_BOT_TOKEN_*` | Bot tokens |
| `SUPABASE_URL`, `SUPABASE_KEY` | DB access |
| `OPENAI_API_KEY` | LLM |

## Nightly (nightly.yml)

Scheduled jobs:
- WB cashback parsing
- Market intel orchestration

## Safety Rules

- Never push to `main` directly — PR only
- Production deploy requires manual confirmation
- Migrations dry-run in CI before merge
- `autopilot` can push to `develop`, never to `main`
