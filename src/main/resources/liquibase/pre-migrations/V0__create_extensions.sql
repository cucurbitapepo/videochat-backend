--liquibase formatted sql

--changeset videochat:0.0.1-1 runAlways:false runOnChange:true failOnError:true
--comment: Создание расширений PostgreSQL (требует прав суперпользователя)

CREATE SCHEMA IF NOT EXISTS videochat;

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA videochat;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA videochat;

--rollback DROP EXTENSION IF EXISTS "uuid-ossp";
--rollback DROP EXTENSION IF EXISTS pg_trgm;