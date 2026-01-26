# Context Loader Protocol

> Выполняется ПЕРЕД началом работы (spark, planner, coder, review, debugger)

## Алгоритм

### 1. Определи изменённые файлы

```bash
# Staged changes
git diff --cached --name-only

# Unstaged changes
git diff --name-only

# Untracked files
git ls-files --others --exclude-standard
```

### 2. Сопоставь с контекстами

Используй таблицу из CLAUDE.md:

| Паттерн файлов | Контекст |
|----------------|----------|
| `src/domains/seller/**` | `.claude/contexts/seller.md` |
| `src/domains/buyer/**` | `.claude/contexts/buyer.md` |
| `src/infra/**` | `.claude/contexts/shared.md` |
| `tests/**` | `.claude/rules/testing.md` |
| `supabase/**` | `.claude/rules/database.md` |

### 3. Загрузи нужные контексты

```
Для каждого matching контекста:
1. Прочитай файл
2. Добавь в рабочий контекст
3. Отметь что загружено
```

### 4. Выведи summary

```markdown
## Loaded Contexts
- ✅ seller.md (src/domains/seller/agent.py changed)
- ✅ testing.md (tests/unit/test_seller.py changed)
- ⏭️ buyer.md (no changes in buyer domain)
```

## Пример использования

**Перед spark:**
```
1. User: "Добавь новый tool для seller"
2. Context Loader:
   - Детектирует: seller domain
   - Загружает: .claude/contexts/seller.md
   - Загружает: текущий prompts/seller_agent/v7.X.md
3. Spark работает с полным контекстом
```

## Важно

- НЕ загружай ВСЕ контексты — только релевантные
- Если непонятно какой домен — спроси пользователя
- Максимум 3 контекста за раз (экономия токенов)
