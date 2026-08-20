# Camel JPA importer (MySQL 5 -> Postgres)

Spring Boot + Apache Camel JPA route that pulls rows from a MySQL source table and writes them into a Postgres target table while marking the source row as exported.

## Prerequisites
- Java 25 (or newer toolchain)
- Gradle 8+ (or use the Gradle wrapper if added)
- MySQL 5.x (or compatible) with a `customers` table
- PostgreSQL with a `customers_import` table

## Configure

### Using Environment Variables (.env file)

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and update with your actual database credentials:
   ```env
   MYSQL_URL=jdbc:mysql://localhost:3305/tender?...
   MYSQL_USERNAME=root
   MYSQL_PASSWORD=your_actual_password
   POSTGRES_URL=jdbc:postgresql://localhost:5432/tender1?...
   POSTGRES_USERNAME=postgres
   POSTGRES_PASSWORD=your_actual_password
   ```

3. Run with the `dev` profile:
   ```bash
   gradle bootRun --args='--spring.profiles.active=dev'
   ```

The application will automatically load the `.env` file and use those values. The `.env` file is git-ignored for security.

### Using application.yml directly

Alternatively, update `src/main/resources/application.yml` (or `application-dev.yml`) for your DB hosts, users, and passwords. The important properties are:
- `spring.datasource.mysql.*` (source)
- `spring.datasource.postgres.*` (target)

Both Hibernate contexts default to `none` for DDL; create tables yourself:
```sql
-- MySQL
create table customers (
  id bigint auto_increment primary key,
  name varchar(255) not null,
  email varchar(255) not null unique,
  created_at timestamp not null,
  exported boolean not null default 0
);

-- Postgres
create table customers_import (
  id bigserial primary key,
  source_id bigint not null,
  name varchar(255) not null,
  email varchar(255) not null unique,
  created_at timestamp not null
);
```

## Run

### Development mode (with .env file)
```bash
gradle bootRun --args='--spring.profiles.active=dev'
```

### Default mode
```bash
gradle bootRun
```

The Camel route `mysql-to-postgres-import` polls MySQL every 5s using the named query `SourceCustomer.fetchUnexported`. Each row is mapped in `CustomerProcessor` and persisted to Postgres; the source row is flagged `exported=true` so it is skipped on the next poll.

## Adjustments
- Tweak poll size and delay via `maximumResults` and `delay` in `ImportRouteBuilder`.
- Replace the simple processor with your own transformations or validations.
- To delete rows after import, change the JPA consumer URI `consumeDelete=true` in the route.
