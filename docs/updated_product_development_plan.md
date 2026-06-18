# Nutrition App: обновленный план развития продукта

Дата аудита: 2026-05-22  
Мобильное приложение: `C:\Projects\NutritionApp`  
Backend API: `C:\Projects\api`

## 1. Текущий статус

Проект уже вышел за рамки раннего MVP. В мобильном приложении есть Compose, Hilt, Retrofit, DataStore, Room, WorkManager, Camera/ML Kit barcode scanning, Health Connect, Glance widgets, экраны auth/onboarding/home/diary/products/recipes/meal plan/profile/analytics. На сервере уже есть Spring Boot API с PostgreSQL/JPA, JWT/refresh tokens, глобальная обертка ответов, rate limiting, tracing, endpoints для diary, water, weight, products, recipes, recommendations, meal plans, shopping list, analytics events, vision/OCR и админского crawler/import pipeline.

Главная проблема сейчас не отсутствие функций, а незавершенность связей между ними: контракты местами расходятся, данные рецептов и продуктов требуют очистки, security defaults пока девелоперские, UI имеет дубли и незакрытые сценарии, а "AI" пока в основном rule-based/stub.

## 2. Что нужно изменить в стратегии

Старый план шел по логике "сначала слои, потом экраны, потом ML". Сейчас правильнее идти по логике пользовательской ценности:

1. Сначала доверие: точные КБЖУ, корректные рецепты, безопасная сессия, предсказуемый diary.
2. Потом скорость: пользователь должен добавить еду за 5-10 секунд.
3. Потом персональный помощник: приложение объясняет, что есть дальше, почему, и как это вписывается в день.
4. Потом ML: модель ранжирует уже качественные рецепты и продукты, а не маскирует плохие данные.

Позиционирование: не "еще один счетчик калорий", а "персональный ассистент питания, который планирует, объясняет и экономит время".

## 3. Конкурентная рамка

У лидеров рынка уже есть базовые ожидания:

- MyFitnessPal: дневник, большая база продуктов, barcode, meal scan/voice logging и meal planner в premium.
- Cronometer: сильная ставка на точность нутриентов, barcode и nutrition label flow.
- YAZIO: кастомный план, AI/photo tracking, barcode, fasting, аналитика.
- Lifesum: barcode, meal plans, shopping lists, привычки и более lifestyle-подача.
- Eat This Much: автоматическое планирование питания, grocery list и планирование под цели/вкусы/бюджет.
- Samsung Food: рецепты, meal planner, shopping list, AI-сохранение рецептов и экосистема готовки.

Вывод: barcode, рецепты, планы и shopping list уже не уникальны сами по себе. Уникальность должна быть в связке: "я съел X, у меня осталось Y, дома есть Z, предложи лучший ужин и список покупок, объясни выбор".

## 4. Северная звезда продукта

North Star Metric: процент дней, когда пользователь получил и выполнил персональную рекомендацию питания.

Поддерживающие метрики:

- Time to log food: медиана до 10 секунд.
- Diary completion rate: сколько дней заполнены хотя бы на 2 приема пищи.
- Recommendation acceptance rate: сколько рекомендаций добавляют в дневник/план.
- Meal plan completion rate: сколько блюд из плана реально съедено.
- Data trust score: доля продуктов/рецептов с проверенными нутриентами и единицами измерения.
- Retention D1/D7/D30.

## 5. Roadmap v2

### Фаза 0. Аудит, контракт и данные

Цель: сделать продукту "почву", на которой можно строить лидерские фичи.

Задачи:

- Сгенерировать OpenAPI спецификацию и сделать ее единственным контрактом между backend и Android.
- Привести все ответы к единому `ApiResponse`/`ApiErrorResponse`; сейчас сервер оборачивает ответы advice-слоем, а контроллеры возвращают разные типы.
- Зафиксировать DTO для auth, home, diary, products, recipes, meal plans, shopping list, analytics, water, weight.
- Исправить diary create/update: если `source=RECIPE` и пришел `recipeId`, сервер обязан сохранять связь с рецептом и возвращать `recipeId`, `recipeName`, корректные КБЖУ.
- Пересчитать и очистить рецепты: ингредиенты, единицы, вода/соль/специи, calories per serving, total calories, portion weight.
- Ввести статус качества данных: `verified`, `imported`, `user_created`, `needs_review`.
- Убрать публичный доступ к `/api/v1/admin/crawler/**` и закрыть `/actuator/**` в production.
- Убрать дефолтные секреты и пароли из `application.yml`.
- На Android убрать hardcoded `http://192.168.0.9:8080/`, сделать `BuildConfig.API_BASE_URL` для dev/stage/prod.

Критерий готовности:

- 20 ключевых API сценариев проходят интеграционные тесты.
- Android не содержит дублирующих API-клиентов для одного сценария.
- Тестовый пользователь может пройти auth -> home -> add food -> diary -> recommendation без ручной чистки БД.

### Фаза 1. Trustworthy MVP

Цель: пользователь верит цифрам и не теряет данные.

Мобильное приложение:

- Завершить diary CRUD: добавить, изменить вес/прием пищи/дату, удалить, повторить вчерашнее, копировать между днями.
- Убрать дубли экранов и репозиториев: оставить один diary flow, один API gateway, один sync worker.
- Сделать полноценные loading/empty/error/offline states для home, diary, products, recipes, meal plan.
- Хранить токены через EncryptedSharedPreferences или Jetpack Security/EncryptedFile подход вместо обычного DataStore для чувствительных данных.
- Развести debug/release logging: в release не логировать body, PII, токены, email, health data.
- Добавить session recovery, forced logout и нормальное поведение на 401/403 без бесконечного refresh.

Backend:

- Валидация всех request DTO через Bean Validation.
- Разделить public, user и admin endpoints.
- Добавить ownership checks для user-specific ресурсов.
- Добавить миграции Flyway/Liquibase вместо `ddl-auto=update` даже в dev-like окружениях.
- Ввести idempotency key для быстрых добавлений еды, чтобы двойной тап не создавал дубликаты.
- Добавить пагинацию там, где есть большие списки.

Критерий готовности:

- Пользователь может пользоваться diary неделю без дубликатов, потери записей и "нулевых" макросов.
- Home обновляется после действий в diary/water/weight без ручного перезапуска.

### Фаза 2. Три wow-момента

Цель: дать причину выбрать приложение, а не конкурента.

Wow 1: Умный Home

- Не просто карточки КБЖУ, а "что делать дальше".
- Показывать остаток калорий/белка/жиров/углеводов до конца дня.
- Давать 1-3 конкретных next best actions: добавить белок, выбрать ужин до 600 kcal, выпить воду, не перебрать сахар.
- Каждая рекомендация должна иметь действие: добавить продукт, открыть рецепт, создать план, добавить в shopping list.

Wow 2: Быстрый Diary

- Быстрые кнопки: повторить последний завтрак, добавить 250 мл воды, сканировать barcode, найти продукт, добавить рецепт.
- Recent/favorites на первом экране добавления еды.
- Recovery для barcode not found: создать продукт из label scan или вручную за 30 секунд.
- Поддержать "не знаю вес": порции, штуки, ложки, стаканы, потом конвертация в граммы.

Wow 3: Smart Meal Plan

- Генерация плана с объяснением: цель, лимиты, диета, исключения, любимые кухни, бюджет, время готовки.
- Автоматический shopping list с группировкой по категориям и объединением одинаковых ингредиентов.
- Добавление блюда из плана в diary одним нажатием.
- План должен адаптироваться: если пользователь съел лишнее на обед, ужин становится легче.

Критерий готовности:

- Новый пользователь за первый день видит план, добавляет еду и получает понятную рекомендацию без обучения приложению.

### Фаза 3. Продукты, рецепты и качество базы

Цель: создать локальное преимущество в данных.

Продукты:

- Barcode database с fallback: если не найдено, предложить label scan/custom product.
- Smart product analysis: сахар, белок, клетчатка, насыщенные жиры, ультраобработанность, "подходит/не подходит под цель".
- Избранное, recent, альтернативы и замены.
- Модерация пользовательских продуктов.

Рецепты:

- Фильтры: калории, белок, время, сложность, кухня, диета, исключенные продукты, аллергии, "из того, что есть".
- Нормальная карточка: ингредиенты с количеством, шаги, порции, КБЖУ на порцию, цена/время.
- Подбор замен: нет ингредиента -> предложить альтернативу.
- Favorite recipes и history должны реально влиять на рекомендации.

Критерий готовности:

- 80% топовых рецептов имеют валидные ингредиенты, единицы и КБЖУ.
- Поиск продуктов и рецептов не выглядит как сырая выгрузка базы.

### Фаза 4. Retention и аналитика

Цель: возвращать пользователя через прогресс и понятные инсайты.

- Графики веса, калорий, белка, воды, streak, weekly summary.
- Human-readable insights: "3 дня подряд не хватает белка", "ужины дают 45% калорий", "вес стоит, но средняя калорийность выше плана".
- Push notifications с контекстом, а не спамом.
- Health Connect: шаги/активность могут корректировать план, но только прозрачно.
- Analytics events: `food_logged`, `recipe_added`, `recommendation_seen`, `recommendation_accepted`, `plan_generated`, `plan_meal_logged`, `barcode_not_found`.

Критерий готовности:

- Есть понятная weekly review страница.
- Можно оценивать, какие рекомендации реально помогают.

### Фаза 5. ML и персонализация

Цель: перейти от правил к персональному ranking engine.

Не начинать с "большой ML модели". Последовательность:

1. Rule-based engine: нормы, остаток дневного бюджета, цель, ограничения, аллергии, история.
2. Scoring service: формула ранжирования рецептов по calories fit, protein fit, preference fit, novelty, prep time, ingredients availability.
3. Feature store: пользовательские признаки, история приемов пищи, любимые категории, отказанные рекомендации, время дня, сезонность.
4. Candidate generation: рецепты, продукты, замены, планы.
5. ML reranker: LightGBM/XGBoost или нейронный ranking после накопления событий.
6. Embeddings: user/recipe/product embeddings для похожих рецептов и персональных рекомендаций.
7. Online learning/A-B tests: проверять acceptance rate, retention, completion rate.

Обязательные guardrails:

- Объяснимость: почему рекомендация показана.
- Nutrition safety: не предлагать экстремальные дефициты, учитывать противопоказания как ограничения.
- Privacy: явное согласие на персонализацию и обработку health data.
- Fallback: если ML недоступен, rule-based рекомендации продолжают работать.

### Фаза 6. Release engineering

Android:

- Release build с minify/R8, baseline profiles, startup performance budget.
- 16 KB page size compatibility.
- Network Security Config отдельно для dev/prod.
- Crash reporting и non-fatal errors.
- Accessibility: content descriptions, font scaling, contrast, touch targets.
- Localization: RU как основной язык, EN позже; убрать mojibake в строках и комментариях.

Backend:

- Docker Compose для local, stage, prod-like.
- CI: test, build, dependency scan, SAST, container scan.
- Observability: structured logs, request id, metrics, dashboards, alerts.
- Backups, migration rollback, seed data.
- Rate limiting не in-memory для production, а Redis/Bucket4j или gateway-level.

Тесты:

- Unit tests: use cases, token flow, mappers, recommendation scoring.
- API integration tests: Testcontainers + PostgreSQL.
- Contract tests: OpenAPI snapshot + Android DTO compatibility.
- UI smoke tests: auth, home, diary, product search, barcode fallback, recipe detail, meal plan.

## 6. Архитектура целевого продукта

Backend как modular monolith на Spring Boot:

- Identity & Session
- Profile & Goals
- Diary
- Product Catalog
- Recipe Catalog
- Planning
- Recommendation
- Analytics
- Notification
- Vision/OCR
- Admin/Data Quality

Это лучше микросервисов на текущей стадии: быстрее разработка, проще транзакции, меньше операционных рисков. Но границы модулей нужно держать чистыми, чтобы позже вынести Recommendation/ML отдельно.

Android:

- `presentation`: Compose screens + ViewModel state machines.
- `domain`: use cases и repository interfaces.
- `data`: repositories, remote DTO, local Room, sync.
- `core`: network, auth/session, error handling, design system, analytics.

Запретить новые "параллельные" реализации одного сценария без удаления старой.

## 7. Security baseline

Минимальный стандарт:

- OWASP API Security Top 10 для backend.
- OWASP MASVS для Android.
- TLS only в production.
- No default secrets.
- No public admin endpoints.
- No PII/body logging in production.
- Secure token storage.
- Refresh token rotation и invalidation.
- Ownership checks для всех user data.
- Input validation и output encoding.
- Dependency audit.
- Privacy policy и consent для health/ML data.
- Threat model для auth, diary, recommendations, admin crawler, image upload.

## 8. Приоритетный backlog

### Выполнено 2026-05-22

- Закрыт `/api/v1/admin/crawler/**`: теперь требуется `ROLE_ADMIN`.
- Закрыты actuator endpoints: публичными остались только `/actuator/health` и `/actuator/info`, остальные требуют `ROLE_ADMIN`.
- Убраны небезопасные дефолты `DB_PASSWORD` и `JWT_SECRET` из backend `application.yml`; значения должны приходить из окружения.
- Исправлен приоритет создания diary entry на backend: `productId`/`recipeId` теперь важнее `customName`, поэтому рецепты не превращаются в custom-записи.
- Android `BASE_URL` вынесен из Kotlin-кода в `BuildConfig.API_BASE_URL`.
- Android certificate pinning переведен на `BuildConfig.API_DOMAIN`/`API_CERT_PIN` и включается только для release.
- Main network security config запрещает cleartext; debug overlay отдельно разрешает локальный `192.168.0.9`.
- Проверки: `mvnw test` на backend прошел, `:app:compileDebugKotlin` на Android прошел после очистки поврежденного KSP cache.
- User management endpoints (`GET/POST/PUT/DELETE /api/v1/users...`) ограничены `ROLE_ADMIN`; обычный пользователь работает через `/users/me`.
- Product create/update/delete ограничены `ROLE_ADMIN`; публичные пользовательские сценарии поиска/избранного сохранены.
- Recipe create/update/delete, управление ингредиентами и пересчет нутриентов ограничены `ROLE_ADMIN`.
- `POST /api/v1/food-diary` получил Bean Validation для обязательного приема пищи, веса, даты и источника записи.
- Добавлен regression-тест `FoodDiaryServiceTest`: recipe diary entry с `recipeId + customName` сохраняет связь с рецептом.
- Android `SafeApiCall` больше не логирует весь успешный DTO; debug-логи показывают только тип ответа, ошибки не раскрывают сырой body.
- Исправлен Android DTO обновления веса записи дневника: `weight` -> `weightGrams`, чтобы совпадало с backend contract.
- Проверки: `mvnw test` на backend прошел, 7 тестов; `:app:compileDebugKotlin` прошел.
- Добавлен `idempotencyKey` для `POST /api/v1/food-diary`: повторный запрос возвращает уже созданную запись вместо дубля.
- Backend `food_diary_entries` получил поле `idempotency_key` и индекс `(user_id, idempotency_key)`; для production нужна управляемая миграция.
- Android `CreateFoodDiaryRequestDto` генерирует и отправляет idempotency key, а `CreateDiaryEntryDto` передает его в API.
- Добавлен regression-тест на idempotency: повтор `addEntry` не вызывает `save`.
- Проверки: `mvnw test` на backend прошел, 8 тестов; `:app:compileDebugKotlin` прошел.
- Android access/refresh tokens перенесены из обычного DataStore в `EncryptedSharedPreferences` через Jetpack Security Crypto.
- Добавлена миграция старых токенов при первом чтении: существующая сессия сохраняется, после миграции legacy token keys удаляются из DataStore.
- `TokenManager.clearTokens()` больше не очищает весь DataStore: theme и `lastMealPlanId` не сбрасываются при logout/session reset.
- Проверки: `:app:compileDebugKotlin` прошел.
- Android auth-flow нормализован: refresh token выполняется только на `401`, а `403` остается ошибкой доступа без попытки "лечить" роль/права новым токеном.
- Добавлен предел retry для OkHttp authenticator через `X-Token-Retry` и подсчет prior responses.
- При временной ошибке refresh, например network/5xx, сессия не очищается; токены удаляются только при явном `401/403` от refresh endpoint.
- Проверки: `:app:compileDebugKotlin` прошел.
- Удален неиспользуемый Android `FoodDiaryApi`: diary contract остается в едином `NutritionApi`, без второго рассинхронизированного интерфейса.
- Удалена дублирующая реализация `HomeRepositoryImpl` из domain-слоя; DI использует единственную data-реализацию.
- Проверки: `:app:compileDebugKotlin` прошел.
- `MealPlanMapper` очищен от мертвых nullable fallback для non-null shopping-list DTO полей, чтобы contract warnings не прятали реальные проблемы.
- Проверки: `:app:compileDebugKotlin` прошел.
- Удален устаревший Android `DiaryRepository` path: `DiaryRepositoryImpl` и старые `Add/Get/DeleteDiaryEntryUseCase` больше не дублируют рабочий `FoodDiaryRepository`.
- Проверки: `:app:compileDebugKotlin` прошел.
- `EditProfileViewModel` получил submit-guard: повторные нажатия save во время загрузки больше не запускают параллельные PATCH/GET цепочки.
- Проверки: `:app:compileDebugKotlin` прошел.
- `UserRepositoryImpl.updateProfile()` больше не отправляет сеть, если профиль и вес не менялись; PATCH профиля и PATCH веса теперь вызываются только при реальных изменениях.
- `EditProfileScreen` исправлен по label mapping активности: `ACTIVE` и `VERY_ACTIVE` больше не подписываются как более высокие уровни, чем есть на самом деле.
- Проверки: `:app:compileDebugKotlin` прошел.
- В profile-сценариях обновлен deprecated `hiltViewModel` import на `androidx.hilt.lifecycle.viewmodel.compose`, чтобы убрать локальный compile warning и подготовить код к актуальному Compose API.
- Проверки: `:app:compileDebugKotlin` прошел.
- Добавлен единый source of truth для UI activity levels: `SupportedUiActivityLevels`, `normalizeUiActivityLevel()` и `toActivityLabelRes()`.
- Register, Onboarding и Edit Profile теперь используют один и тот же список уровней активности и одинаковые подписи, без скрытых расхождений между экранами.
- `CalculateNutritionTargetsUseCase` теперь корректно учитывает уровень `ACTIVE`; раньше этот сценарий выпадал в fallback и давал искаженный расчет.
- В auth flow обновлены deprecated Compose API: `hiltViewModel` импорт переведен на актуальный пакет, а `ScrollableTabRow` заменен на `PrimaryScrollableTabRow`.
- Проверки: `:app:compileDebugKotlin` прошел.
- `EditProfileScreen` получил локальную валидацию формы: save недоступен без имени, с невалидным весом и при отсутствии изменений.
- Hardcoded тексты `EditProfile` вынесены в string resources, а read-only target weight и ошибки формы теперь оформлены как нормальная UI-подсказка.
- Проверки: `:app:compileDebugKotlin` прошел.
- `RegisterScreen` очищен от основных hardcoded текстов: поля, CTA, gender/diet labels и часть ошибок переведены на string resources.
- В `RegisterScreen` добавлена базовая клиентская валидация email, пароля, имени, даты, роста, веса и целевого веса; submit блокируется, пока форма невалидна.
- Проверки: `:app:compileDebugKotlin` прошел.
- `LoginScreen` переведен на string resources и актуальный `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`, чтобы auth flow использовал единый текстовый слой и актуальный Compose API.
- `OnboardingScreen` целиком выровнен по UX: основные hardcoded тексты вынесены в ресурсы, шаги используют единый product copy, а поля получили заметную step-level валидацию для email, пароля, даты, роста, веса и target weight.
- `OnboardingViewModel` очищен от hardcoded ошибок регистрации/auto-login и теперь использует string resources через `@ApplicationContext`.
- Проверки: `:app:compileDebugKotlin` прошел.

P0:

- Закрыть admin crawler и actuator.
- Убрать hardcoded secrets/passwords/base URL.
- Исправить diary recipe persistence и idempotency.
- Привести response contracts к одному стандарту.
- Исправить encoding/mojibake.
- Данные рецептов: единицы, ингредиенты, КБЖУ.

P1:

- Diary CRUD + repeat/copy.
- Home next best action.
- Recipe detail/filters/favorites.
- Product barcode fallback.
- Meal plan -> diary -> shopping list.
- Token storage/session recovery.

P2:

- Analytics weekly review.
- Product health analysis.
- Smart substitutions.
- Health Connect adaptive plan.
- Push notifications.

P3:

- ML ranking.
- Embeddings.
- A/B testing.
- Personal pantry/fridge.
- Voice/photo logging.

## 9. Источники для конкурентного и security-контекста

- MyFitnessPal Meal Planner: https://support.myfitnesspal.com/hc/en-us/articles/34347103172877-Meal-Planner
- MyFitnessPal Meal Scan: https://support.myfitnesspal.com/hc/en-us/articles/360045761612-Meal-Scan-FAQ
- Cronometer Scan Food: https://support.cronometer.com/hc/en-us/articles/360020441392-Mobile-Scan-Food
- YAZIO features: https://www.yazio.com/en
- Lifesum features: https://lifesum.com/features/
- Eat This Much planner: https://help.eatthismuch.com/help/what-do-i-get-for-subscribing
- Samsung Food: https://samsungfood.com/
- OWASP API Security Top 10: https://owasp.org/API-Security/
- OWASP MASVS: https://mas.owasp.org/MASVS/

## Progress Log

- `DiaryViewModel` now loads both diary entries and daily macro summary, and deletion shows user feedback before re-syncing the screen.
- `DiaryScreen` now has complete day-state UX: single empty state for the day, retry flow for errors, snackbar feedback after delete, and a summary card above meal sections.
- `AddFoodScreen` was completed as a polished flow: search/manual modes share resource-based copy, local validation, saveable form state, and submit stays disabled until input is valid.
- Verification: `:app:compileDebugKotlin` passed.
- Recipe flow was upgraded from "works" to a usable product surface: `RecipeSearchScreen` now has proper resource-based copy, smart meal-plan CTA, better browse context, and dedicated empty/error states with retry.
- `RecipeDetailScreen`, `MealPlanScreen`, and `ShoppingListScreen` were aligned into one connected flow: recipe -> add to diary, meal plan -> shopping list, and fallback states now guide the user toward generating a plan instead of dead-ending.
- `GeneratePlanScreen` now uses normalized copy and diet labels, while navigation was updated so meal plan and shopping list screens can recover by routing the user back into plan generation.
- Verification: `:app:compileDebugKotlin` passed.
- Products flow was upgraded into a real acquisition surface: `ProductSearchScreen` now has recovery-first UX for empty/error/barcode-not-found states, plus clear quick actions for manual search, barcode scan, and camera recognition.
- `ProductDetailScreen`, `BarcodeScannerScreen`, and `FoodRecognitionScreen` were polished as a connected flow: better resource-based copy, permission recovery, safer scan behavior, and a more useful product card with AI analysis and alternative suggestions.
- `ProductSearchViewModel` now remembers the last scanned barcode for retry/recovery, and the products/scanner stage compiles cleanly with updated lifecycle/flow usage.
- Verification: `:app:compileDebugKotlin` passed.
- `HomeScreen` was upgraded from a raw dashboard to a daily control center: better error/empty states, weekly momentum header, clearer meal/recommendation sections, Health Connect banner polish, and quick water/weight actions through lightweight dialogs.
- `AnalyticsScreen` was reworked to be easier to read and more retention-friendly: resource-based copy, refresh action, clearer quality summary, empty-chart fallback, and a better separation between AI insight, charts, and recommendations.
- `AnalyticsViewModel` now exposes an explicit refresh entry point so home/retention work can be iterated without reworking navigation or lifecycle assumptions.
- Verification: `:app:compileDebugKotlin` passed.
- Release hardening was completed for the Android client: release builds now enable minification and resource shrinking, the repo no longer depends on a hardcoded local JDK path, WorkManager logging is quieter in release, app backups are disabled, and the shared OkHttp setup now applies consistent timeouts to both authenticated and basic clients.
- `GetAiAdviceUseCase` was repaired and productized: mojibake text was replaced with readable Russian copy, and advice now respects `homeData.targetCalories` instead of a hardcoded 2000 kcal ceiling.
- Added a real unit-test layer for key product logic: `CalculateNutritionTargetsUseCase`, `CalculateStreakUseCase`, and `GetAiAdviceUseCase` now have regression coverage under `app/src/test`.
- Added GitHub Actions CI at `.github/workflows/android-ci.yml` to compile debug Kotlin, run unit tests, and assemble a release artifact on every push/PR.
- Backend contract hardening was completed for meal-plan and validation flows: meal-plan create/read/delete/shopping-list endpoints are now bound to the current authenticated user by default, while cross-user listing is explicitly admin-only.
- `FoodDiaryController` now validates PUT/PATCH payloads the same way as POST, and `GlobalExceptionHandler` no longer breaks on class-level validation errors such as `@AssertTrue`; invalid date/type formats now return a consistent `BAD_REQUEST` instead of bubbling into a generic 500.
- Server test infrastructure is now CI-friendly: `ApiApplicationTests` runs on an isolated H2 `test` profile with its own JWT secret, so `mvn test` no longer depends on a locally configured PostgreSQL instance.
- Added regression coverage for the backend hardening layer: `MealPlanServiceTest` verifies user ownership behavior, and `GlobalExceptionHandlerTest` now covers invalid-format request handling.
- Production backend / security / data quality stage is complete: backend now fails fast in `prod` on invalid Base64 JWT secrets, too-short signing keys, and wildcard CORS origins.
- Backend CORS now explicitly allows `PATCH`, so the mobile profile update flow matches the declared HTTP contract instead of relying on permissive local behavior.
- Server-side response normalization now cleans noisy imported product and recipe names before they reach Android flows such as diary, meal plans, shopping lists, products, and recipes.
- Added focused backend regression coverage for production settings validation and display-name normalization.
- Remaining large stages: `1` - post-MVP AI / personalization / ranking.
- Post-MVP AI / personalization stage is now active in product code: backend recipe recommendations are no longer generic top-rated stubs and are ranked against user context such as meal type, recent diary history, excluded products, favorite products/cuisines, protein deficit, calorie context, and recipe analytics events.
- `Home` now consumes backend-generated personalized insight text, so the daily assistant card can reflect the recommendation layer instead of relying only on static local heuristics.
- Added regression coverage for the personalization layer with `PersonalizedRecommendationServiceTest`.
- Android now preserves recommendation metadata instead of flattening everything into generic recipe cards: smart recommendations keep `reason` and `score`, and the recipes tab renders them as personalized cards with explainability copy.
- Recommendation context is now carried into `RecipeDetailScreen`, where users can see why the recipe was suggested, and analytics events include recommendation source/reason for both opens and diary logging.
- This closes the first real recommendation feedback loop on the client: `RECIPE_RECOMMENDATION_OPENED` and enriched `RECIPE_LOGGED` events now flow back to the backend ranking layer.
- The recommendation backend now has a stable A/B layer: users are deterministically assigned to `CONTROL` or `EXPLORATION`, and `/api/v1/recommendations/recipes` also supports explicit `variant` override for evaluation and tuning.
- Recommendation responses now carry `experimentVariant` and `explanationTags`, while the ranking model accounts for recommendation opens in addition to views and logs.
- Android recipe recommendations now surface the exploration badge and short explanation tags, so experiment behavior is visible in the product rather than hidden inside the API.
