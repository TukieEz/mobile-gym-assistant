---
paths:
  - "ai/features/BUG-*"
  - "ai/features/*bug*"
  - "ai/features/*incident*"
description: Debugging and incident investigation - server access, logs, troubleshooting
alwaysApply: false
---

# Debugging & Incident Investigation

## Local Test Logs

| Тип | Путь | Формат |
|-----|------|--------|
| LLM тесты (текущие) | `test_logs/*.md` | Markdown с conversation |
| LLM тесты (по датам) | `test_logs_YYYY-MM-DD_runN/*.md` | То же, архив по запускам |
| pytest вывод | stdout | Не сохраняется в файл |

**Быстрый поиск ошибок:**
```bash
# Последний упавший тест
ls -t test_logs*/*FAILED*.md | head -1

# Все ошибки за сегодня
grep -l "FAILED\|Error" test_logs_$(date +%Y-%m-%d)*/*.md
```

**Структура лог-файла (TECH-047):**
- `## Timing` — длительность
- `## Conversation` — диалог user/bot
- `## Tool Calls` — вызовы инструментов с args и duration
- `## Token Usage` — токены и стоимость
- `## Evaluation` — оценка и причина fail

---

## Server Access

**Full details:** `.claude/rules/operations.md` (SSH, Docker, logs)

**Quick ref:** `ssh deploy@194.87.93.31`

---

## Supabase CLI (No Restrictions)

Branch Protection блокирует только **запись** на main. Чтение — без ограничений!

```bash
# Логи (Edge Functions, Auth, etc.)
supabase logs --linked

# История миграций
supabase migration list --linked

# Инспекция БД (размеры, индексы, etc.)
supabase inspect db --linked

# Статус проекта
supabase status --linked
```

**Когда использовать:**
- Дебаг Edge Functions → `supabase logs --linked`
- Проверка миграций → `supabase migration list --linked`
- Анализ производительности → `supabase inspect db --linked`

---

## Database Logs

| Bot | Table | Format |
|-----|-------|--------|
| SellerBot | `chat_logs` | Daily session, messages[] |
| BuyerBot | `buyer_conversation_logs` | Each turn separate |

```sql
-- Recent seller conversations
SELECT * FROM chat_logs
WHERE created_at > NOW() - INTERVAL '1 hour'
ORDER BY created_at DESC;

-- Recent buyer conversations
SELECT * FROM buyer_conversation_logs
WHERE created_at > NOW() - INTERVAL '1 hour'
ORDER BY created_at DESC;

-- Find by telegram ID
SELECT * FROM chat_logs WHERE tg_id = 123456789 ORDER BY created_at DESC LIMIT 10;
```

## Quick Troubleshooting

| Problem | Command |
|---------|---------|
| Bot not responding | `docker compose logs -f seller-bot` → check errors |
| Container crashed | `docker compose ps` → `docker compose restart <service>` |
| SSH permission denied | `ssh-add ~/.ssh/id_rsa` |
| Need full restart | `docker compose down && docker compose up -d` |

## Export Chat for Investigation

```bash
python scripts/export_chat_html.py --buyer UUID
python scripts/export_chat_html.py --tg-id 123456789 --type buyer
python scripts/export_chat_html.py --seller UUID
```

---

## Deep Root Cause Analysis (BUG-302 Methodology)

Когда обычный "5 Whys" недостаточен — баг возвращается после "фикса".

### Признаки что нужен глубокий анализ

| Red Flag | Что значит |
|----------|------------|
| Несколько fix-миграций за короткий период | Лечим симптомы, не причину |
| Тест проходит, потом падает снова | Есть внешний фактор создающий проблему |
| "Правильный" фикс не помогает | Неверное понимание системы |

### Методология: 4 фазы

#### Phase 1: PATTERN RECOGNITION

**Цель:** Найти паттерн проблемы, не симптом.

```bash
# Пример: сколько fix-миграций на эту тему?
ls supabase/migrations/*slots_available* | wc -l

# Git history: когда и что меняли
git log --oneline --all -- '**/slots_available*'
```

**Вопросы:**
- Сколько раз это уже "чинили"?
- Когда проблема началась?
- Есть ли корреляция с другими изменениями?

#### Phase 2: SOURCE OF TRUTH HUNTING

**Цель:** Найти КАНОНИЧЕСКОЕ определение правильного поведения.

```
❌ НЕПРАВИЛЬНО: "slots_available должен быть quantity - claimed"
   (это формула из старого fix-а, не source of truth)

✅ ПРАВИЛЬНО: "slots_available определяется триггером sync_slots_available_deferred"
   (это actual code path)
```

**Методы:**
1. Найти триггер/функцию которая СОЗДАЁТ значение
2. Читать код, не комментарии к fix-миграциям
3. Искать `CONSTRAINT`, `TRIGGER`, `DEFAULT` в schema

```bash
# Найти trigger
grep -r "CREATE.*TRIGGER.*sync" supabase/migrations/

# Найти функцию
grep -r "CREATE.*FUNCTION.*sync" supabase/migrations/
```

#### Phase 3: TIMELINE RECONSTRUCTION

**Цель:** Понять последовательность событий.

Создать таблицу:

| Время | Событие | Компонент | Правильно? |
|-------|---------|-----------|------------|
| T1 | Создан row-level trigger | sync_slots_available | ✅ |
| T2 | Edge Function добавлена | daily-slot-rebalance | ❌ баги |
| T3 | Fix migration | fix_data_001 | ✅ |
| T4 | Edge Function creates more data | daily-slot-rebalance | ❌ |
| T5 | Deferred trigger added | sync_slots_available_deferred | ✅ |
| T6 | Wrong fix migration | fix_data_wrong_formula | ❌ **МОЯ ОШИБКА** |

**Ключевой вопрос:** Какой компонент ПРОДОЛЖАЕТ создавать проблему?

#### Phase 4: EXTERNAL RESEARCH

**Цель:** Валидировать подход через best practices.

```
# Exa MCP для паттернов
mcp__exa__web_search_exa: "PostgreSQL counter cache pattern best practices"
mcp__exa__get_code_context_exa: "denormalized counter trigger postgres"

# Context7 для библиотек
mcp__plugin_context7_context7__query-docs: "supabase triggers"
```

**Что искать:**
- Как другие решают эту проблему?
- Есть ли известные pitfalls?
- Какая архитектура рекомендуется?

### Чеклист перед "финальным" фиксом

После анализа, перед коммитом:

```
[ ] Source of Truth найден и понят
[ ] Timeline показывает ВСЕ источники проблемы
[ ] Старые "плохие" компоненты удалены/заменены
[ ] Формула фикса СОВПАДАЕТ с Source of Truth
[ ] Есть тест который поймает рецидив
[ ] Нет других путей создания невалидных данных
```

### Anti-patterns

| Anti-pattern | Почему плохо |
|--------------|--------------|
| "Тест прошёл = готово" | Внешний фактор может создать проблему позже |
| "Скопирую формулу из старого фикса" | Старый фикс мог быть неправильным |
| "Добавлю ещё один fix migration" | Симптом, не лечение |
| "Это legacy, не трогаем" | Legacy создаёт новые проблемы |
| Читать комментарии вместо кода | Читай actual CODE который создаёт значение |
| Фиксить симптомы повторно | Найди что СОЗДАЁТ проблему, не что её чинит |
| Фиксить без спеки | Даже очевидный фикс требует спеки (docs + review) |

### Documentation Rule (from diary)

При фиксе багов в сложных подсистемах — проверить есть ли документация.
Если нет — включить создание документации в спеку фикса.

### Пример: BUG-302 slots_available

**Симптом:** Contract test падает периодически.

**Naive fix:** `UPDATE offers SET slots_available = quantity - claimed`

**Deep analysis:**
1. Pattern: 6 fix-миграций за день → лечим симптомы
2. Source of Truth: `sync_slots_available_deferred` trigger → `COUNT(unassigned)`
3. Timeline: Edge Function создавала данные после fixes
4. Research: PostgreSQL deferred triggers — best practice для counter cache

**Root cause:** Buggy Edge Function + wrong formula in my fix.

**Real fix:**
1. Delete Edge Function (replaced by atomic RPC)
2. Use correct formula from trigger
3. Add stress tests for regression

---

## Deep RCA for Flaky Tests (BUG-327)

**Success pattern:** Quantitative timeline + git correlation.

### Method

1. **Build Timeline**
   - Collect test runs from CI logs + local test_logs/
   - Calculate success rate by date ranges
   - Find inflection point (when rate changed)

2. **Git Correlation**
   - `git log --since="<inflection_date>" --oneline`
   - Identify changes in period
   - Read relevant specs/commits

3. **Evidence Collection**
   - Extract exact errors from failed runs
   - Quote specific values (e.g., "Свеча ароматическая" instead of expected)
   - Prove correlation between code change and failure

4. **5 Whys Until Root**
   - Don't stop at symptoms
   - Check test isolation (setup/teardown)
   - Verify shared state (flow_state, DB records)

### Example Output

```
Timeline:
- Jan 14-15: 100% pass rate (10/10) ← stable!
- Jan 16: 75% pass rate (9/12) ← regression!

Correlation:
- Commit 3cb5200: BUG-317 (force tool calls based on flow_state)

Root cause:
- setup() clears campaigns but NOT flow_state
- BUG-317 reads stale flow_state → wrong tool call
```

**Metrics:**
- Time: ~30 min
- Files reviewed: ~20
- Root cause found: Yes
