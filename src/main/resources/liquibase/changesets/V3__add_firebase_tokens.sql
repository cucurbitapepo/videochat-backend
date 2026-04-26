--liquibase formatted sql

--changeset videochat:3.0.0-1 failOnError:true
--comment: Добавление колонок для хранения FCM-токена

ALTER TABLE videochat.users
    ADD COLUMN fcm_token VARCHAR(500),
    ADD COLUMN fcm_token_expiry TIMESTAMP;

COMMENT ON COLUMN videochat.users.fcm_token IS 'FCM токен для push-уведомлений';
COMMENT ON COLUMN videochat.users.fcm_token_expiry IS 'Срок действия FCM токена';

--rollback ALTER TABLE videochat.users DROP COLUMN fcm_token;
--rollback ALTER TABLE videochat.users DROP COLUMN fcm_token_expiry;