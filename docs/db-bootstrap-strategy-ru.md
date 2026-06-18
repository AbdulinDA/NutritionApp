# Стратегия чистого развертывания БД и перехода на Flyway

Цель: уйти от переходного режима, где development еще опирается на `spring.jpa.hibernate.ddl-auto=update`, и перейти к состоянию, где новая база поднимается предсказуемо, а приложение работает с `ddl-auto=validate`.

## Текущее состояние

Сейчас в проекте уже сделано следующее:

- Flyway подключен в backend.
- Введен `baseline-on-migrate=true`.
- Исторические schema-скрипты уже частично перенесены в `src/main/resources/db/migration`.
- Deploy-процесс переведен на Flyway-first.

Но development-профиль пока еще оставляет:

```yaml
spring.jpa.hibernate.ddl-auto=update
```

Это временный компромисс, потому что у проекта пока нет полностью проверенной initial migration chain для абсолютно пустой новой базы.

## Целевое состояние

Нужно прийти к такой модели:

1. Пустая PostgreSQL БД создается.
2. Приложение стартует.
3. Flyway поднимает всю схему с нуля.
4. Hibernate работает только в режиме `validate`.
5. Любые новые изменения схемы идут только через новые `V*.sql` миграции.

## Практическая стратегия перехода

### Этап 1. Зафиксировать реальную каноничную схему

На сервере или локальной машине, где БД уже в корректном состоянии, выгрузить schema-only baseline:

```bash
cd ~/nutrition-api-release
scripts/deploy/export-schema-baseline.sh
```

Или с явным путем:

```bash
cd ~/nutrition-api-release
OUTPUT_PATH=./artifacts/schema-baseline.sql scripts/deploy/export-schema-baseline.sh
```

Скрипт использует настройки из `/etc/nutrition-api/api.env` и делает `pg_dump --schema-only`.

Результат нужен не для ручного применения в production, а для сравнения:

- что уже покрыто текущими Flyway migrations;
- чего в migration chain еще не хватает.

### Этап 2. Дособрать initial migration chain

После выгрузки baseline нужно сравнить:

- актуальную схему из `schema-baseline.sql`;
- цепочку `src/main/resources/db/migration/V*.sql`.

Для удобства текущий список покрытого и непокрытого surface зафиксирован отдельно в:

- [flyway-schema-gap-checklist-ru.md](/abs/path/c:/Projects/api/docs/flyway-schema-gap-checklist-ru.md:1)

Задача на этом этапе:

- найти таблицы, индексы, ограничения, sequence и колонки, которые еще не выражены через Flyway;
- добавить недостающие migrations;
- убедиться, что цепочка может поднять пустую БД с нуля.

### Этап 3. Проверить на чистой БД

Нужен отдельный прогон на пустой PostgreSQL базе:

1. создать новую пустую БД;
2. запустить API с `FLYWAY_ENABLED=true`;
3. убедиться, что все migrations отрабатывают без ручных SQL patch-скриптов;
4. проверить старт приложения и базовые endpoints.

Критерий успеха:

- приложение стартует без `ddl-auto=update`;
- таблицы и ограничения создаются только за счет Flyway;
- healthcheck и базовые API работают.

### Этап 4. Перевести dev на validate

Только после успешной проверки clean bootstrap:

- заменить в `application-dev.yml`:

```yaml
spring.jpa.hibernate.ddl-auto=validate
```

После этого любая нехватка схемы будет всплывать сразу, а не silently дорисовываться Hibernate.

## Что уже можно делать прямо сейчас

Уже сейчас рекомендуется:

- новые schema changes делать только через Flyway migrations;
- не добавлять новые production schema SQL в `/scripts`;
- использовать legacy SQL patch mode только как временный fallback.

## Что пока остается переходным

Пока не завершен clean bootstrap:

- `ddl-auto=update` в `dev` остается временно допустимым;
- `nutrition_db_dump.sql` остается быстрым способом поднять новый сервер;
- часть исторического знания о схеме все еще живет в старой БД, а не только в migration chain.

## Критерий завершения перехода

Переход можно считать завершенным, когда одновременно выполнены все условия:

- есть проверенная initial migration chain для пустой БД;
- `application-dev.yml` использует `ddl-auto=validate`;
- deploy не опирается на legacy schema patch mode;
- новые изменения схемы добавляются только в Flyway.
