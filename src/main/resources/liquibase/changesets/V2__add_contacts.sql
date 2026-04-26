--liquibase formatted sql

--changeset videochat:2.0.0-1 failOnError:true
--comment: Создание таблицы контактов

CREATE TABLE videochat.contacts
(
    id         BIGSERIAL PRIMARY KEY,
    owner_id   BIGINT    NOT NULL,
    contact_id BIGINT    NOT NULL,
    alias      VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES videochat.users (id) ON DELETE CASCADE,
    FOREIGN KEY (contact_id) REFERENCES videochat.users (id) ON DELETE CASCADE,
    UNIQUE (owner_id, contact_id),
    CHECK (owner_id != contact_id)
);

COMMENT ON TABLE videochat.contacts IS 'Таблица контактов пользователей';
COMMENT ON COLUMN videochat.contacts.id IS 'Уникальный идентификатор записи контакта';
COMMENT ON COLUMN videochat.contacts.owner_id IS 'Идентификатор пользователя, которому принадлежит контакт';
COMMENT ON COLUMN videochat.contacts.contact_id IS 'Идентификатор пользователя, добавленного в контакты';
COMMENT ON COLUMN videochat.contacts.alias IS 'Псевдоним контакта в списке контактов пользователя';
COMMENT ON COLUMN videochat.contacts.created_at IS 'Дата и время добавления контакта';
COMMENT ON COLUMN videochat.contacts.updated_at IS 'Дата и время последнего обновления записи';

-- Индексы для оптимизации запросов
CREATE INDEX idx_contacts_owner ON videochat.contacts (owner_id);
CREATE INDEX idx_contacts_contact ON videochat.contacts (contact_id);
CREATE INDEX idx_contacts_owner_contact ON videochat.contacts (owner_id, contact_id);

--rollback DROP INDEX IF EXISTS videochat.idx_contacts_owner_contact;
--rollback DROP INDEX IF EXISTS videochat.idx_contacts_contact;
--rollback DROP INDEX IF EXISTS videochat.idx_contacts_owner;
--rollback DROP TABLE IF EXISTS videochat.contacts;

--changeset videochat:2.0.0-2 failOnError:true splitStatements:false
--comment: Добавление функции для автоматического обновления updated_at

CREATE OR REPLACE FUNCTION videochat.update_modified_column_contacts()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Создаем триггер для автоматического обновления поля updated_at
CREATE TRIGGER update_contacts_updated_at
    BEFORE UPDATE
    ON videochat.contacts
    FOR EACH ROW
EXECUTE FUNCTION videochat.update_modified_column_contacts();

--rollback DROP TRIGGER IF EXISTS update_contacts_updated_at ON videochat.contacts;
--rollback DROP FUNCTION IF EXISTS videochat.update_modified_column_contacts();