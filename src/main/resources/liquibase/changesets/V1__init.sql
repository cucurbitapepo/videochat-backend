--liquibase formatted sql

--changeset videochat:1.0.0-1 failOnError:true
--comment: Создание таблицы пользователей

CREATE TABLE videochat.users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE videochat.users IS 'Таблица пользователей системы';
COMMENT ON COLUMN videochat.users.id IS 'Уникальный идентификатор пользователя';
COMMENT ON COLUMN videochat.users.username IS 'Уникальное имя пользователя (логин)';
COMMENT ON COLUMN videochat.users.password IS 'Хеш пароля (BCrypt)';
COMMENT ON COLUMN videochat.users.enabled IS 'Активен ли пользователь';
COMMENT ON COLUMN videochat.users.created_at IS 'Дата и время создания записи';
COMMENT ON COLUMN videochat.users.updated_at IS 'Дата и время последнего обновления';

CREATE INDEX idx_users_username ON videochat.users USING btree (username);
--rollback DROP INDEX IF EXISTS videochat.idx_users_username;

CREATE INDEX idx_users_username_trgm ON videochat.users USING gin (username gin_trgm_ops);
--rollback DROP INDEX IF EXISTS videochat.idx_users_username_trgm;

--changeset videochat:1.0.0-2 failOnError:true
--comment: Создание таблицы ролей пользователей

CREATE TABLE videochat.user_roles
(
    user_id BIGINT      NOT NULL,
    role    VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES videochat.users (id) ON DELETE CASCADE
);

COMMENT ON TABLE videochat.user_roles IS 'Таблица ролей пользователей';
COMMENT ON COLUMN videochat.user_roles.user_id IS 'Ссылка на пользователя';
COMMENT ON COLUMN videochat.user_roles.role IS 'Роль пользователя (USER, ADMIN)';

--rollback DROP TABLE IF EXISTS videochat.user_roles;

--changeset videochat:1.0.0-3 failOnError:true splitStatements:false
--comment: Добавление функции для автоматического обновления updated_at

CREATE OR REPLACE FUNCTION videochat.update_modified_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE
    ON videochat.users
    FOR EACH ROW
EXECUTE FUNCTION videochat.update_modified_column();

--rollback DROP TRIGGER IF EXISTS update_users_updated_at ON videochat.users;
--rollback DROP FUNCTION IF EXISTS videochat.update_modified_column();