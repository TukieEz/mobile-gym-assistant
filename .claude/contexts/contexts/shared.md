---
paths:
  - "src/infra/**"
  - "src/shared/**"
description: Shared infrastructure context
---

# Shared Infrastructure

## Database

[Document your database setup here]

### Key Tables

| Table | Description |
|-------|-------------|
| `users` | User accounts |
| ... | ... |

## External APIs

[Document external services here]

### Services

| Service | Purpose | Docs |
|---------|---------|------|
| ... | ... | ... |

## Utilities

### Result Pattern

```python
from src.shared.result import Ok, Err, Result

def process(data: Data) -> Result[Output, Error]:
    if not valid(data):
        return Err(Error.INVALID)
    return Ok(transform(data))
```

## Configuration

Environment variables in `.env`:

```
DATABASE_URL=...
API_KEY=...
```

See `.env.example` for full list.
