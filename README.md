# Camel JPA HRM importer (MySQL legacy → PostgreSQL ERP)

Spring Boot + Apache Camel pipeline that migrates legacy HRM master data from MySQL into the PostgreSQL ERP schema. The `master-import` timer route runs all 30 migration steps once on startup.

## Prerequisites
- Java 25 (or compatible toolchain)
- Gradle 8+
- MySQL legacy HRM database (source)
- PostgreSQL ERP database (target) with schema created by ERP services

## Configure

### Staging / dev (`.env`)

1. Copy `.env.example` to `.env` and point at staging databases:
   ```bash
   cp .env.example .env
   ```

2. Set `MYSQL_URL`, `POSTGRES_URL`, and credentials for the staging pair.

3. Run with the `dev` profile:
   ```bash
   gradlew bootRun --args='--spring.profiles.active=dev'
   ```

The app loads `.env` on startup (`CamelJpaTnApplication`).

### Local defaults

Edit `src/main/resources/application.yml` for local MySQL/PostgreSQL hosts.

## Run migration on staging (P0)

1. **Prepare target DB** — empty ERP schema or truncated migration tables on staging PostgreSQL.
2. **Start importer** — `gradlew bootRun --args='--spring.profiles.active=dev'`.
3. **Watch logs** — each step logs `Total records imported`; final block lists all 30 steps.
4. **Row-count QA** — runs automatically after step 26. Search logs for `Migration row-count QA report`.
   - Compares MySQL source counts vs PostgreSQL `mysql_id` rows.
   - Compares pipeline imported counts vs PostgreSQL totals.
   - Set `migration.qa.fail-on-mismatch=true` to abort on mismatch (default: log only).
5. **Manual spot-check** — `scripts/staging-row-count-qa.sql` has paired MySQL/PG queries.

### QA configuration

```yaml
migration:
  qa:
    enabled: true          # set false to skip post-migration QA
    fail-on-mismatch: false # set true on CI/staging gates
```

Exchange properties after QA: `migrationQaPassed`, `migrationQaFailureCount`.

## Pipeline overview

Timer `master-import` → steps 1–26 (+ 22a–22d profile sub-entities) → row-count QA → summary log.

See `ImportRouteBuilder.java` for the full route list.

## Adjustments
- Page size: `PAGE_SIZE` in `ImportRouteBuilder` (default 100).
- Step throttle: `MIGRATION_THROTTLE_MS` between steps.
