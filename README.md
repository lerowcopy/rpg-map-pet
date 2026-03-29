# RPG Map Pet 🗺️

Android-приложение для исследования города с игровой механикой посещения достопримечательностей.

---

## 📱 Возможности

### 🎮 Основные функции
- **Интерактивная карта** — Yandex MapKit с отображением достопримечательностей
- **Кластеризация меток** — умная группировка близких объектов
- **Посещение мест** — автоматическое определение посещения по геолокации
- **Фотографии** — добавление фото через камеру или галерею
- **Список посещённых** — просмотр всех посещённых мест

### 📍 Работа с метками
- **Отображение типа объекта** — памятник, мемориал, бюст, достопримечательность, храм и др.
- **Цветовая индикация** — посещённые (зелёный) / непосещённые (красный)
- **Детальная информация** — название с типом, описание, координаты
- **Галерея фотографий** — сетка 2 колонки с возможностью удаления

### 🗺️ Навигация
- **Плавающие кнопки**:
  - 🔵 "Я здесь" (справа внизу) — центрирование на текущем местоположении
  - 🔵 "Посещённые" (слева внизу) — переход к списку посещённых мест
- **Маркер пользователя** — синяя точка с белой обводкой
- **Автоматическое центрирование** при запуске

---

## 🛠️ Технологии

### Стек
- **Kotlin** — основной язык разработки
- **Jetpack Compose** — современный declarative UI
- **Yandex MapKit** — картографическая основа
- **Room** — локальная база данных
- **Hilt** — dependency injection
- **ViewModel + StateFlow** — архитектура MVVM
- **Coroutines** — асинхронные операции
- **Coil** — загрузка изображений
- **Gson** — парсинг GeoJSON

### Архитектура
```
├── data/          # Слой данных (Room, Repository, GeoJSON)
├── domain/        # Бизнес-логика (Use Cases, Models)
├── presentation/  # UI слой (ViewModels, Screens)
│   ├── map/       # Экран карты с кластеризацией
│   ├── quest/     # Детали метки
│   └── visited/   # Посещённые места
└── core/          # Утилиты, навигация, результаты
```

### Ключевые компоненты
- **GridBasedClusterAlgorithm** — сеточный алгоритм кластеризации
- **ClusterRenderer** — отрисовка кластеров с цветов кодировкой
- **LandmarkDetailScreen** — экран деталей с добавлением фото
- **GeoJsonImporter** — импорт достопримечательностей из GeoJSON
- **FileProvider** — безопасный доступ к файлам камеры

---

## 📦 Структура базы данных

### Таблицы Room
```sql
-- Метки (достопримечательности)
landmarks (
    id TEXT PRIMARY KEY,
    name TEXT,
    description TEXT,
    type TEXT,
    latitude REAL,
    longitude REAL,
    isCompleted BOOLEAN,
    completedAt INTEGER
)

-- Фотографии меток
landmark_photos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    landmarkId TEXT,
    photoPath TEXT,
    createdAt INTEGER,
    FOREIGN KEY (landmarkId) REFERENCES landmarks(id)
)
```

---

## 🎯 Особенности реализации

### Кластеризация
- **Алгоритм**: Grid-based (сеточный)
- **Порог**: zoom ≤ 12
- **Цветовая схема**:
  - 🟢 Зелёный — все метки посещённые
  - 🟡 Жёлтый — некоторые посещённые
  - 🔵🟡🟠🔴 Синий/Жёлтый/Оранжевый/Красный — ни одной посещённой (зависит от размера)

### Работа с фото
- **Камера**: `ActivityResultContracts.TakePicture()`
- **Галерея**: `ActivityResultContracts.GetMultipleContents()`
- **Хранение**: внутренняя память приложения (`/files/landmark_photos/`)
- **FileProvider**: безопасный доступ к файлам

### Геолокация
- **Разрешения**: `ACCESS_FINE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`
- **Обновления**: фоновое отслеживание для гео-фенсинга
- **Диалог**: предложение включить GPS при отключённой геолокации

---

## 🔧 Конфигурация сборки

### Версии
- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 36 (Android 14)
- **compileSdk**: 36

### Зависимости
```kotlin
// Карты
implementation("com.yandex.android:maps.mobile:4.33.1-lite")

// Room
implementation("androidx.room:room-runtime:2.8.4")
implementation("androidx.room:room-ktx:2.8.4")
ksp("androidx.room:room-compiler:2.8.4")

// Hilt
implementation("com.google.dagger:hilt-android:2.59.2")
ksp("com.google.dagger:hilt-compiler:2.59.2")

// Coil
implementation("io.coil-kt:coil-compose:2.5.0")

// Gson (для GeoJSON)
implementation("com.google.code.gson:gson:2.13.2")
```

---

## 🚀 Быстрый старт

### Требования
- Android Studio Hedgehog или новее
- JDK 11+
- Android SDK 26+ (target 36)
- **API-ключ Yandex MapKit** (обязательно!)

### Установка

1. **Клонируйте репозиторий**
```bash
git clone https://github.com/yourusername/rpgmappet.git
cd rpgmappet
```

2. **Настройте API-ключ Yandex MapKit**

Создайте файл `local.properties` в корне проекта:
```properties
yandexMapsApiKey=ВАШ_API_КЛЮЧ_YANDEX_MAPKIT
```

3. **Получите API-ключ Yandex MapKit**
   - Перейдите в [Кабинет разработчика Yandex](https://developer.tech.yandex.ru/)
   - Создайте новый проект
   - Подключите **MapKit Mobile для Android**
   - Скопируйте ключ в `local.properties`

4. **Запустите проект**
```bash
./gradlew assembleDebug
```

> ⚠️ **Важно:** Без API-ключа карта не будет работать!

---

## 🐛 Решение проблем

### Карта не отображается / "API key is empty"
1. Проверьте файл `local.properties` в корне проекта
2. Убедитесь, что ключ указан правильно:
   ```properties
   yandexMapsApiKey=ваш_ключ_из_кабинета_разработчика
   ```
3. Пересоберите проект: `Build → Clean Project → Rebuild Project`

### Геолокация не работает
- Проверьте разрешения в настройках приложения
- Включите GPS в настройках устройства
- Для Android 13+ проверьте разрешение на доступ к местоположению

### Фотографии не сохраняются
- Проверьте разрешение на доступ к файлам (Android 13+)
- Убедитесь, что есть свободное место на устройстве

### Ошибки компиляции
- Убедитесь, что JDK 11+ установлен
- Проверьте версию Android SDK (требуется 36)
- Выполните `File → Invalidate Caches → Invalidate and Restart`

---

## 📄 Лицензия

MIT License — подробности в файле [LICENSE](LICENSE).

---

> **Примечание**: Проект создан в образовательных целях и демонстрирует навыки Android-разработки.