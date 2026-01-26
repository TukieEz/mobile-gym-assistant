# 🏋️ Mobile Gym Assistant

Минималистичный Android-трекер силовых тренировок с фокусом на быстрый ввод и отслеживание прогресса.

## ✨ Фичи

- ⚡ **Быстрый ввод** — залогируй подход за 2 тапа (вес × повторы)
- 📊 **Графики прогресса** — визуализация роста по каждому упражнению
- 🏆 **Персональные рекорды** — автоматический трекинг PR
- 📱 **Офлайн** — работает без интернета (Room DB)
- 🎨 **Material You** — современный дизайн с Jetpack Compose

## 🛠️ Стек

| Слой | Технологии |
|------|------------|
| **UI** | Jetpack Compose, Material 3 |
| **Architecture** | MVVM, Clean Architecture |
| **DI** | Hilt |
| **Database** | Room |
| **Async** | Kotlin Coroutines + Flow |
| **Charts** | Vico |

## 📁 Структура

```
app/src/main/kotlin/com/gymassistant/
├── data/           # Room DB, DAOs, Repositories
├── domain/         # Use Cases, Domain Models
├── di/             # Hilt Modules
└── ui/             # Compose Screens, Components
    ├── screens/
    │   ├── workout/    # Текущая тренировка
    │   ├── history/    # История
    │   ├── progress/   # Графики
    │   └── records/    # PR
    ├── components/     # Reusable UI
    └── theme/          # Material Theme
```

## 🚀 Запуск

```bash
# Клонировать
git clone https://github.com/sowecanya/mobile-gym-assistant.git
cd mobile-gym-assistant

# Открыть в Android Studio
# File → Open → выбрать папку проекта

# Или через командную строку
./gradlew assembleDebug
./gradlew installDebug
```

## 🧪 Тесты

```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
./gradlew check                   # Полная проверка
```

## 📝 Roadmap

- [x] Архитектура проекта
- [ ] Quick Set Input
- [ ] Exercise Picker
- [ ] Workout Session
- [ ] History Screen
- [ ] Progress Charts
- [ ] Personal Records

## 📄 License

MIT
