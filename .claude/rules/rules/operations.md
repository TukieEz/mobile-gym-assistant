---
paths:
  - "scripts/**"
  - "docker-compose*"
  - "supabase/functions/**"
  - ".env*"
description: Operations handbook - deployment, payouts, marketing, server management
---

# Operations Handbook

Quick reference for periodic operational tasks. Full details: `OLEG.md`

---

## Server Access

```bash
ssh deploy@194.87.93.31
docker compose logs -f              # all logs
docker compose logs -f seller-bot   # specific service
docker compose ps                   # container status
docker compose restart seller-bot   # restart service
```

**Edit .env:** `nano .env` → Ctrl+O save → Ctrl+X exit → `docker compose down && docker compose up -d`

---

## OpenAI Proxy

> OpenAI blocked in Russia. Using HTTP proxy.

```
Bots → HTTP Proxy (216.74.104.74:5431) → api.openai.com
```

**Config:** `OPENAI_HTTP_PROXY` in `.env`
**Used in:** `src/infra/llm/client.py`, `src/infra/llm/embeddings.py`

**Test:**
```bash
curl -x http://user344764:ztfkw7@216.74.104.74:5431 https://api.openai.com/v1/models -H "Authorization: Bearer $OPENAI_API_KEY"
```

---

## Finances (Wallet-First)

> FTR-064: Seller balance as single wallet. No `pending_payment` status.

**Flow:** Seller pays → Operator deposits → Seller activates via bot

### Deposit balance

```bash
python -m scripts.deposit_balance                              # interactive
python -m scripts.deposit_balance --inn 0800013016 --amount 50000
python -m scripts.deposit_balance --tg-id 773774897608 --amount 10000
python -m scripts.deposit_balance --inn 7707083893 --amount 100000 --ref "PAY-12345"
```

### Check balance

```bash
python -c "
from src.db.client import db
inn = '0800013016'
seller = db.table('sellers').select('id, legal_name').eq('inn', inn).single().execute()
balance = db.table('seller_balances').select('*').eq('seller_id', seller.data['id']).single().execute()
print(f\"{seller.data['legal_name']}: {balance.data['available_rub']:,}₽ available, {balance.data['hold_rub']:,}₽ hold\")
"
```

---

## Buyer Payouts

```bash
python scripts/process_payouts.py           # interactive menu
python scripts/process_payouts.py --approve # approve all requested
python scripts/process_payouts.py --export  # CSV for bank
python scripts/process_payouts.py --confirm # mark as paid
python scripts/process_payouts.py --fail 550e8400 --reason "Invalid phone"
```

**Interactive keys:** E=export, C=confirm, F=fail, A=approve, R=refresh, Q=quit

---

## Marketing & Leads

### Import WB articles

1. Open [WB Рубли за отзывы](https://www.wildberries.ru/promotions/rubli-za-otzyvy)
2. Scroll to load products
3. DevTools Console:
```javascript
let a=new Set();performance.getEntriesByType('resource').forEach(r=>{let m=r.name.match(/part\d+\/(\d+)\/images/);if(m)a.add(m[1])});copy([...a].join('\n'));console.log('Copied:',a.size);
```
4. Save to `articles.txt` and run:
```bash
python -m scripts.parse_wb_cashback --import articles.txt
```

### Market Intel Orchestrator

```bash
python -m scripts.run_market_intel              # full pipeline
python -m scripts.run_market_intel --dry-run    # preview only
python -m scripts.run_market_intel --skip-llm   # save $
```

### Outreach

```bash
python -m scripts.run_outreach --dry-run   # preview
python -m scripts.run_outreach --limit 10  # send limited
```

---

## Data Export

### Export chat history (incident investigation)

```bash
# Find user
python -c "
from src.db.client import db
r = db.table('buyers').select('id, tg_id, tg_username, phone').ilike('tg_username', '%username%').execute()
for b in r.data: print(b)
"

# Export
python scripts/export_chat_html.py --buyer UUID
python scripts/export_chat_html.py --tg-id 123456789 --type buyer
python scripts/export_chat_html.py --seller UUID
```

**Output:** ZIP with `messages.html` + photos

### Logs location

| Bot | Table | Format |
|-----|-------|--------|
| SellerBot | `chat_logs` | Daily session, messages[] |
| BuyerBot | `buyer_conversation_logs` | Each turn separate |

---

## Cron (Edge Functions)

Managed via `pg_cron` in Supabase SQL Editor.

```sql
-- View schedule
SELECT jobid, jobname, schedule FROM cron.job;

-- Change schedule
SELECT cron.alter_job(4, '0 18 * * *');  -- 18:00 UTC = 21:00 MSK
```

| Function | Cron | MSK |
|----------|------|-----|
| daily-report | `0 18 * * *` | 21:00 |
| accrual | `0 3 * * *` | 06:00 |
| sla-checker | `*/15 * * * *` | every 15m |
| notification-worker | `*/1 * * * *` | every 1m |

---

## Quick SQL

```sql
-- Active campaigns
SELECT id, product_name, status, slots_filled FROM campaigns WHERE status = 'active';

-- Draft campaigns (ready to launch)
SELECT id, product_name, budget_planned_rub FROM campaigns WHERE status = 'draft';

-- Today's payouts
SELECT * FROM withdrawal_requests WHERE created_at::date = CURRENT_DATE;

-- Top earners
SELECT b.tg_username, bb.total_earned_rub FROM buyer_balances bb
JOIN buyers b ON b.id = bb.buyer_id ORDER BY bb.total_earned_rub DESC LIMIT 10;
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| SSH permission denied | `ssh-add ~/.ssh/id_rsa` |
| Bot not responding | `docker compose logs -f seller-bot` → `docker compose restart` |
| Proxy not working | Check payment at proxys.io, verify `OPENAI_HTTP_PROXY` in .env |

---

## Testing

```bash
./test fast    # lint + unit
./test         # full + LLM
./test llm     # LLM only
```

**BuyerBot testing checklist:** See `OLEG.md` section "Чек-лист тестирования BuyerBot"

---

## Links

- [Supabase Dashboard](https://supabase.com/dashboard)
- @DowryBot (Seller) | @AwardyBot (Buyer)
