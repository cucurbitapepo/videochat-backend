--liquibase formatted sql

--changeset videochat:4.0.0-1 failOnError:true
--comment: Создание таблицы звонков

CREATE TABLE videochat.calls
(
    id         BIGSERIAL PRIMARY KEY,
    call_id    VARCHAR(255) NOT NULL UNIQUE,
    caller_id  BIGINT       NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at   TIMESTAMP,
    FOREIGN KEY (caller_id) REFERENCES videochat.users (id) ON DELETE CASCADE
);

COMMENT ON TABLE videochat.calls IS 'Таблица сессий звонков';
COMMENT ON COLUMN videochat.calls.id IS 'Уникальный идентификатор звонка';
COMMENT ON COLUMN videochat.calls.call_id IS 'Глобальный идентификатор звонка (UUID)';
COMMENT ON COLUMN videochat.calls.caller_id IS 'Идентификатор инициатора звонка';
COMMENT ON COLUMN videochat.calls.status IS 'Статус звонка (WAITING, ACTIVE, ENDED, REJECTED)';
COMMENT ON COLUMN videochat.calls.created_at IS 'Дата и время создания звонка';
COMMENT ON COLUMN videochat.calls.ended_at IS 'Дата и время завершения звонка';

--rollback DROP TABLE IF EXISTS videochat.calls;

--changeset videochat:4.0.0-2 failOnError:true
--comment: Создание таблицы участников звонков

CREATE TABLE videochat.call_participants
(
    call_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (call_id, user_id),
    FOREIGN KEY (call_id) REFERENCES videochat.calls (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES videochat.users (id) ON DELETE CASCADE
);

COMMENT ON TABLE videochat.call_participants IS 'Таблица участников звонков';
COMMENT ON COLUMN videochat.call_participants.call_id IS 'Идентификатор звонка';
COMMENT ON COLUMN videochat.call_participants.user_id IS 'Идентификатор пользователя';

--rollback DROP TABLE IF EXISTS videochat.call_participants;