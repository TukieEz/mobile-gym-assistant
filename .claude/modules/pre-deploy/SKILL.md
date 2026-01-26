---
name: pre-deploy
description: |
  Pre-deploy checks. Блокирует деплой если есть проблемы.
  Запускай перед push в main или production deploy.
---

# Pre-Deploy Checks

Проверяет готовность к production перед деплоем.

## Когда использовать

- Перед `git push origin main`
- Перед merge PR в main
- Перед production deploy

## Чеклист проверок

### 1. Build Check
```bash
npm run build
# или
uv run python -m build
# или
docker build .
```
**Блокирует если:** Build fails

### 2. Tests
```bash
npm test
# или
uv run pytest
```
**Блокирует если:** Tests fail

### 3. Lint
```bash
npm run lint
# или
uv run ruff check .
```
**Блокирует если:** Lint errors (warnings OK)

### 4. Type Check
```bash
npx tsc --noEmit
# или
uv run mypy src/
```
**Блокирует если:** Type errors

### 5. Security Scan (если настроен)
```bash
npm audit --audit-level=high
# или
uv run bandit -r src/
```
**Блокирует если:** High/Critical vulnerabilities

### 6. Stack-Specific Checks

**Web Frontend:**
- [ ] SEO: title, description, og:image на всех страницах
- [ ] A11y: No critical WCAG violations
- [ ] Performance: Bundle < 200KB gzipped

**Python Backend:**
- [ ] No DEBUG=True
- [ ] No hardcoded secrets
- [ ] Migrations applied

**Telegram Bot:**
- [ ] Bot token not in code
- [ ] Error handlers configured

## Процесс

```
1. Запусти все проверки
2. Собери результаты
3. Если всё ✅ → "Ready to deploy"
4. Если есть ❌ → Покажи проблемы, БЛОКИРУЙ деплой
```

## Формат отчёта

```markdown
## Pre-Deploy Report

### ✅ Passed
- [x] Build successful
- [x] All tests pass (42/42)
- [x] No lint errors
- [x] Types OK

### ❌ Failed
- [ ] Security: 2 high vulnerabilities in dependencies
  - lodash < 4.17.21 (prototype pollution)
  - Fix: npm update lodash

### Verdict: ❌ NOT READY

Fix the issues above before deploying.
```

## Автоматизация

Можно добавить как pre-push hook:

```bash
# .git/hooks/pre-push
#!/bin/bash
claude --skill pre-deploy
if [ $? -ne 0 ]; then
  echo "Pre-deploy checks failed. Push blocked."
  exit 1
fi
```

## Bypass (только для hotfix)

Если критический hotfix:
```bash
git push --no-verify
```

⚠️ Используй только в экстренных случаях!
