--liquibase formatted sql

--changeset videochat:0.0.1-1 runAlways:false runOnChange:true failOnError:true
--comment: Создание расширений PostgreSQL (требует прав суперпользователя)

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

--rollback DROP EXTENSION IF EXISTS "uuid-ossp";
--rollback DROP EXTENSION IF EXISTS pg_trgm;