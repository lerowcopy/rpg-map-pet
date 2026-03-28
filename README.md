# The City Layer (Городской Слой)

Android-приложение с процедурной генерацией квестов на основе реальной карты города.

## 📋 Концепция

Приложение не просто даёт задания, а анализирует контекст:
- Если идёшь быстро — «погоня»
- Если стоишь на остановке — «ожидание связного»

## 🏗 Архитектура

- **UI**: Jetpack Compose + Material 3
- **DI**: Hilt
- **Карта**: Yandex MapKit 3.x
- **Локальное хранилище**: Room
- **Архитектура**: MVVM + Repository pattern

## 🚀 Начало работы

### 1. Получите API-ключ Yandex MapKit

1. Перейдите на https://developer.tech.yandex.ru/
2. Создайте новый проект
3. Подключите **MapKit Mobile SDK**
4. Создайте API-ключ
5. Скопируйте ключ

### 2. Настройте проект

Создайте файл `gradle-api.properties` в корне проекта (он добавлен в .gitignore):

```properties
yandexMapkitApiKey=ВАШ_API_КЛЮЧ
```

Или вставьте ключ в `gradle.properties` (не коммитьте в git!).

### 3. Запустите проект

```bash
./gradlew assembleDebug
```

## 📁 Структура проекта

```
app/src/main/java/com/example/rpg_map_pet/
├── MapPetApp.kt              # Application класс
├── MainActivity.kt           # Главная активность с картой
├── data/                     # Data layer
│   ├── repository/           # Репозитории
│   ├── local/                # Room DAO и Entities
│   └── remote/               # API клиенты
├── domain/                   # Domain layer
│   ├── model/                # Бизнес-модели
│   ├── repository/           # Интерфейсы репозиториев
│   └── usecase/              # Use cases
└── ui/                       # UI layer
    ├── theme/                # Тема Compose
    ├── components/           # UI компоненты
    └── screens/              # Экраны
```

## 🎯 План разработки

### Этап 1: Базовая карта ✅
- [x] Интеграция Yandex MapKit
- [x] Настройка зависимостей (Hilt, Room)
- [x] Базовая структура проекта
- [ ] Отображение текущей локации пользователя
- [ ] Разрешения на доступ к геолокации

### Этап 2: Location Services
- [ ] Fused Location Provider
- [ ] Фоновый трекинг
- [ ] Geofencing API

### Этап 3: Процедурная генерация квестов
- [ ] Анализ POI (Places of Interest)
- [ ] Генерация названий локаций
- [ ] Система типов квестов

### Этап 4: Компьютерное зрение
- [ ] ML Kit Object Detection
- [ ] AR-слой для «сканера реальности»
- [ ] Распознавание объектов по форме/цвету

### Этап 5: Социальный слой
- [ ] Firebase Firestore
- [ ] Система «призраков» других игроков
- [ ] «Ловушки» и взаимодействия

## 🔧 Технологии

- Kotlin 2.0.21
- Jetpack Compose
- Yandex MapKit 3.3.1
- Hilt 2.52
- Room 2.6.1
- Google Play Services Location

## 📝 Заметки

- Минимальная версия Android: API 24 (Android 7.0)
- Целевая версия: API 36
- Версия Gradle Plugin: 8.7.3

## 📄 Лицензия

Проект в разработке.
