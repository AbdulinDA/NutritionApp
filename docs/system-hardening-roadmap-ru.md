# План системного усиления приложения и API

Цель этого плана: довести продукт до состояния, где он выглядит не как набор удачных фич для демонстрации, а как современная, безопасная и поддерживаемая система. Ниже я разделил работу на три части: что уже видно по текущему коду, какие глобальные риски остаются и в каком порядке это лучше внедрять.

## 1. Что уже есть в проекте

Сильные стороны текущей реализации:

- Android уже использует `EncryptedSharedPreferences` для access/refresh токенов.
- На API уже есть JWT-аутентификация, refresh token, rate limiting для auth-эндпоинтов, request tracing, actuator и базовая валидация production-настроек.
- На Android есть `Room`, `WorkManager`, `DataStore`, `Hilt`, разделение на `presentation / domain / data`.
- На backend уже есть сервисный слой, тесты для части бизнес-логики и отдельные deploy-скрипты.

То есть фундамент не пустой. Но между “функция работает” и “система готова к реальным пользователям” еще есть заметный зазор.

## 2. Главные проблемы, которые видны по коду

### 2.1. Безопасность

#### Android

- В проекте есть два `AndroidManifest`, и один из них лишний: [app/src/main/xml/AndroidManifest.xml](/abs/path/c:/Projects/NutritionApp/app/src/main/xml/AndroidManifest.xml:8). Это риск путаницы и ложных правок.
- Debug-сборка зашита на локальный `http://192.168.0.9:8080/`: [app/build.gradle.kts](/abs/path/c:/Projects/NutritionApp/app/build.gradle.kts:23). Для команды это неудобно, для безопасности и воспроизводимости тоже плохо.
- Release пока использует заглушки для домена и certificate pin: [app/build.gradle.kts](/abs/path/c:/Projects/NutritionApp/app/build.gradle.kts:30). Это значит, что production-конфиг формально есть, но реально не доведен.
- В локальной БД включено `fallbackToDestructiveMigration()`: [DatabaseModule.kt](/abs/path/c:/Projects/NutritionApp/app/src/main/java/com/abdulin/nutritionapp/core/di/DatabaseModule.kt:27). При изменении схемы это может тихо удалить пользовательские локальные данные.
- `Room` не экспортирует schema и не имеет явных миграций: [AppDatabase.kt](/abs/path/c:/Projects/NutritionApp/app/src/main/java/com/abdulin/nutritionapp/data/local/AppDatabase.kt:16).

#### Backend

- В dev-профиле есть дефолтный пароль БД `12345678`: [application-dev.yml](/abs/path/c:/Projects/api/src/main/resources/application-dev.yml:3).
- В dev-профиле включено `ddl-auto: update`: [application-dev.yml](/abs/path/c:/Projects/api/src/main/resources/application-dev.yml:6). Для прототипа терпимо, для управляемой схемы БД это плохо.
- Нет Flyway/Liquibase: это видно по `pom.xml`, где нет зависимостей миграций: [pom.xml](/abs/path/c:/Projects/api/pom.xml:1).
- Swagger открыт без авторизации: [WebSecurityConfig.java](/abs/path/c:/Projects/api/src/main/java/com/rest/api/security/WebSecurityConfig.java:79). Для публичного продакшена это лишний риск.
- `actuator/health` и `actuator/info` открыты наружу, а `management.endpoint.health.show-details` выставлен в `always`: [application.yml](/abs/path/c:/Projects/api/src/main/resources/application.yml:21). Это раскрывает больше информации, чем нужно.
- `VisionController` пока фактически демо-заглушка, но доступен как обычный API: [VisionController.java](/abs/path/c:/Projects/api/src/main/java/com/rest/api/controller/VisionController.java:26). Там нет ограничений на размер файла, нет антивирусной/типовой проверки и есть прямые сообщения об ошибках.
- `AIController` сейчас возвращает захардкоженные инсайты: [AIController.java](/abs/path/c:/Projects/api/src/main/java/com/rest/api/controller/AIController.java:17). Это не security-баг, но архитектурно опасно: UI может казаться “умным”, хотя backend не опирается на реальные данные.

### 2.2. Архитектурный долг

- Android пока монолитный single-module app. Для учебного проекта это норм, но масштабировать так дальше тяжело.
- На Android есть очень крупные экраны, например аналитика: [AnalyticsScreen.kt](/abs/path/c:/Projects/NutritionApp/app/src/main/java/com/abdulin/nutritionapp/presentation/analytics/AnalyticsScreen.kt:1) содержит 1070 строк. Это признак перегруженного UI-слоя.
- На backend есть сверхкрупный сервис `MealPlanService`: 3263 строки, файл [MealPlanService.java](/abs/path/c:/Projects/api/src/main/java/com/rest/api/service/MealPlanService.java:1). Это главный кандидат на декомпозицию по use-case и policy-слоям.
- В backend бизнес-правила, ML-эвристики, DTO-маппинг и orchestration местами смешаны в одних классах.
- В проекте уже много ML/recommendation-логики, но пока не видно отдельного контура feature engineering, model versioning и quality monitoring.

### 2.3. Надежность и эксплуатация

- Нет полноценной схемы миграций БД и отката изменений.
- Нет явного CI/CD-контура с обязательной сборкой, тестами, линтами и проверкой deploy bundle.
- Нет наблюдаемости production-уровня: structured logs есть частично, но нет нормальной метрики latency/error rate per endpoint, бизнес-метрик и alerting.
- Нет явного механизма idempotency/retry policy для критичных пользовательских действий.
- Для локального sync/offline слоя в Android пока нет стратегии conflict resolution.

### 2.4. Производительность

- Для auth rate limit используется in-memory `ConcurrentHashMap`: [RateLimitingFilter.java](/abs/path/c:/Projects/api/src/main/java/com/rest/api/security/RateLimitingFilter.java:21). На одном инстансе это работает, на нескольких уже нет.
- Нет распределенного cache/limit слоя для рекомендаций, популярных рецептов, справочников и auth throttling.
- Не видно отдельной работы с N+1 запросами, entity graph, read-model DTO query layer.
- Нет явного профилирования тяжелых сценариев: генерация плана, аналитика, домашняя лента, поиск рецептов.

### 2.5. Качество продукта как современного приложения

Вот что современный продукт обычно должен иметь, а у нас это либо не реализовано, либо не доведено:

- удаление аккаунта самим пользователем;
- экспорт персональных данных;
- явный privacy/consent flow для Health Connect и аналитики поведения;
- crash reporting и release diagnostics;
- push-стратегия, а не только локальные reminder-воркеры;
- нормальная release-конфигурация окружений;
- feature flags;
- серверная модерация и data governance для рецептов и изображений;
- SLA на генерацию рекомендаций и graceful degradation, если ML/эвристика недоступна;
- серверная anti-abuse стратегия шире, чем только login rate limit;
- резервные копии и проверяемый disaster recovery сценарий.

## 3. Поэтапный roadmap

## Этап 0. Stop-the-bleeding

Срок: 2-4 дня.

Что делаем:

- Удаляем лишний manifest-файл и приводим Android manifest к одному источнику истины.
- Выносим `API_BASE_URL`, домен, pin и прочие env-параметры в безопасимую схему конфигов по окружениям.
- Убираем дефолтный пароль БД из `application-dev.yml`.
- Закрываем Swagger в production хотя бы basic auth / role / profile-gate.
- Меняем `health.show-details` на безопасный режим для production.
- Решаем судьбу демо-эндпоинтов `AIController` и `VisionController`: либо скрываем за feature flag/admin profile, либо временно выключаем.

Результат:

- проект перестает содержать очевидные security/demo-дыры;
- production-конфиг становится управляемым, а не декоративным.

## Этап 1. Управляемая схема данных

Срок: 3-5 дней.

Что делаем:

- Подключаем Flyway.
- Переносим все ручные SQL-изменения в версионируемые миграции.
- Фиксируем baseline текущей production/dev схемы.
- Для Android локальной БД добавляем export schema и реальные migration-объекты вместо destructive migration.

Результат:

- можно безопасно обновлять API и приложение без потери данных;
- схема БД становится воспроизводимой на любом сервере.

## Этап 2. Усиление backend security

Срок: 4-7 дней.

Что делаем:

- Добавляем rate limiting не только на auth, но и на upload, recommendations, meal-plan generation, search.
- Переводим throttling на Redis или другой shared-store слой.
- Добавляем ограничения на multipart upload: размер, MIME whitelist, проверка расширений, sanitation имени файла.
- Вводим аудит чувствительных действий: login fail, refresh fail, password change, profile update, delete account.
- Проверяем CORS-политику по окружениям и убираем лишние origins.
- Добавляем rotation policy и cleanup policy для refresh token.
- Добавляем account lock / cooldown после серии неудачных логинов.

Результат:

- API становится устойчивее к перебору, abuse и ошибочным интеграциям.

## Этап 3. Архитектурная декомпозиция

Срок: 1-2 недели.

Что делаем на backend:

- Разрезаем `MealPlanService` на отдельные части:
  - generation orchestration;
  - scoring policy;
  - personalization policy;
  - replacement logic;
  - analytics/report builder;
  - persistence facade.
- Отделяем read-model запросы от mutation-сценариев.
- Явно оформляем слой recommendation/ML features.

Что делаем на Android:

- Разбиваем крупные экраны на feature-level компоненты и state holders.
- Выносим общие design/state-компоненты для аналитики, плана питания, профиля.
- Подготавливаем переход к модульности:
  - `core`;
  - `feature-auth`;
  - `feature-profile`;
  - `feature-mealplan`;
  - `feature-analytics`;
  - `feature-recipes`.

Результат:

- кодовая база станет заметно проще для сопровождения;
- новые фичи не будут ломать полпроекта.

## Этап 4. Производительность и масштабирование

Срок: 1 неделя.

Что делаем:

- Профилируем генерацию плана питания и recommendations.
- Добавляем индексы в PostgreSQL под частые запросы.
- Переходим там, где нужно, с entity loading на DTO projections.
- Кэшируем справочники, агрегаты home-screen и тяжелые ML-модели.
- Вводим таймауты и graceful fallback для долгих сценариев.

Результат:

- home, search и meal-plan generation работают предсказуемее;
- backend готов к росту числа пользователей.

## Этап 5. Observability и эксплуатация

Срок: 3-5 дней.

Что делаем:

- Добавляем полноценные метрики:
  - latency по эндпоинтам;
  - error rate;
  - meal plan generation duration;
  - recommendation CTR / replacement rate;
  - OCR request failures.
- Вводим correlation IDs как обязательную часть логов.
- Готовим healthcheck не только “процесс жив”, но и “БД доступна / модель доступна / диск не переполнен”.
- Настраиваем backup/restore playbook и тест восстановления.

Результат:

- проблемы можно обнаруживать и чинить до жалоб пользователей.

## Этап 6. Product-grade функции, которых ждут от современного сервиса

Срок: 1-2 недели, можно параллелить.

Что делаем:

- Самостоятельное удаление аккаунта.
- Экспорт пользовательских данных.
- Экран privacy/consent для Health Connect и персонализации.
- Push-уведомления по реальным событиям, а не только локальные напоминания.
- Feature flags для рискованных изменений.
- Crash reporting и release monitoring.
- Семейный режим / meal prep / сценарии “готовлю на несколько дней” как отдельный продуктовый контур, а не как допущение внутри одной порции.

Результат:

- приложение начинает выглядеть как зрелый продукт, а не только как хорошая ВКР.

## 4. Приоритеты именно для вас сейчас

Если смотреть прагматично, я бы делал в таком порядке:

1. Этап 0: убрать опасные конфиги, заглушки и двусмысленные места.
2. Этап 1: миграции БД и отказ от destructive migration.
3. Этап 2: усиление upload/auth/rate limit/security policy.
4. Этап 3: декомпозиция `MealPlanService` и крупных Android-экранов.
5. Этап 5: observability.
6. Этап 6: privacy/export/delete account/push/crash reporting.
7. Этап 4: глубокая производительность и масштабирование, если ожидается рост нагрузки.

## 5. Что я считаю самыми критичными рисками прямо сейчас

Самые важные вещи, которые я бы не откладывал:

- отсутствие миграций БД;
- `fallbackToDestructiveMigration()` на Android;
- demo/stub API в открытом сервере;
- слабая конфигурационная дисциплина по окружениям;
- слишком крупные сервисы и UI-файлы, которые уже тяжело безопасно менять;
- недостаточная production-observability.

## 6. Что можно сделать следующим шагом

Следующий разумный ход: не пытаться чинить все сразу, а взять Этап 0 и Этап 1 как первый технический спринт. Это даст самый большой выигрыш по надежности и не развалит текущую разработку новых фич.

После этого уже можно отдельно пройтись по:

- backend security hardening;
- декомпозиции meal-plan движка;
- release-ready Android-конфигурации;
- production-ready ML-контуру.
