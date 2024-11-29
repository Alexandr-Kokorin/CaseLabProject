--liquibase formatted sql

--changeset DenisKarpov:22
CREATE TABLE IF NOT EXISTS organization (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(255)    NOT NULL,
    inn         INT             NOT NULL,
    is_active   BOOLEAN         DEFAULT TRUE NOT NULL,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--changeset hottabych04:1
CREATE TABLE IF NOT EXISTS application_user
(
    id              BIGSERIAL NOT NULL,
    email           TEXT      NOT NULL,
    display_name    TEXT      NOT NULL,
    hashed_password TEXT      NOT NULL,
    organization_id BIGINT    DEFAULT NULL,
    tenant_id       BIGINT    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (organization_id) REFERENCES organization(id)
);

--changeset hottabych04:2
CREATE TABLE IF NOT EXISTS attribute
(
    id   BIGSERIAL NOT NULL,
    name TEXT      NOT NULL,
    type TEXT      NOT NULL,
    tenant_id   BIGINT    NOT NULL,

    PRIMARY KEY (id)
);

--changeset hottabych04:3
CREATE TABLE IF NOT EXISTS document_type
(
    id            BIGSERIAL NOT NULL,
    name          TEXT      NOT NULL,
    template_name TEXT,
    tenant_id     BIGINT    NOT NULL,

    PRIMARY KEY (id)
);

--changeset TimurTimergalin:8
CREATE TABLE IF NOT EXISTS document
(
    id               BIGSERIAL NOT NULL,
    document_type_id BIGINT    NOT NULL REFERENCES document_type (id),
    name             TEXT      NOT NULL,
    status           TEXT      NOT NULL,
    tenant_id        BIGINT    NOT NULL,

    PRIMARY KEY (id)
);

--changeset hottabych04:4
CREATE TABLE IF NOT EXISTS document_version
(
    id           BIGSERIAL   NOT NULL,
    name         TEXT        NOT NULL,
    created_at   timestamptz NOT NULL,
    content_name TEXT,
    document_id  BIGINT      NOT NULL REFERENCES document (id) ON DELETE CASCADE,
    tenant_id    BIGINT      NOT NULL,

    PRIMARY KEY (id)
);

--changeset hottabych04:5
CREATE TABLE IF NOT EXISTS user_to_document
(
    id                  BIGSERIAL NOT NULL,
    document_id         BIGINT    NOT NULL REFERENCES document (id) ON DELETE CASCADE,
    application_user_id BIGINT    NOT NULL REFERENCES application_user (id) ON DELETE CASCADE,
    tenant_id           BIGINT    NOT NULL,

    PRIMARY KEY (id)
);

--changeset hottabych04:6
CREATE TABLE IF NOT EXISTS document_attribute_value
(
    document_version_id BIGINT NOT NULL REFERENCES document_version (id) ON DELETE CASCADE,
    attribute_id        BIGINT NOT NULL REFERENCES attribute (id) ON DELETE CASCADE,
    app_value           TEXT,
    tenant_id   BIGINT    NOT NULL,

    PRIMARY KEY (document_version_id, attribute_id)
);

--changeset hottabych04:7
CREATE TABLE IF NOT EXISTS document_type_to_attribute
(
    document_type_id BIGINT  NOT NULL REFERENCES document_type (id) ON DELETE CASCADE,
    attribute_id     BIGINT  NOT NULL REFERENCES attribute (id) ON DELETE CASCADE,
    is_optional      BOOLEAN NOT NULL,
    tenant_id        BIGINT  NOT NULL,

    PRIMARY KEY (document_type_id, attribute_id)
);

--changeset TimurTimergalin:9
CREATE TABLE IF NOT EXISTS document_permission
(
    id   BIGSERIAL NOT NULL,
    name TEXT      NOT NULL,

    PRIMARY KEY (id)
);

--changeset TimurTimergalin:10
CREATE TABLE IF NOT EXISTS global_permission
(
    id   BIGSERIAL NOT NULL,
    name TEXT      NOT NULL,

    PRIMARY KEY (id)
);

--changeset TimurTimergalin:11
CREATE TABLE IF NOT EXISTS signature
(
    id                  BIGSERIAL   NOT NULL,
    name                TEXT        NOT NULL,
    status              TEXT        NOT NULL,
    sent_at             timestamptz NOT NULL,
    signed_at           timestamptz,
    document_version_id BIGINT      NOT NULL REFERENCES document_version (id) ON DELETE CASCADE,
    application_user_id BIGINT      NOT NULL REFERENCES application_user (id) ON DELETE CASCADE,
    signature_data      TEXT,
    tenant_id           BIGINT      NOT NULL,

    PRIMARY KEY (id)
);

--changeset TimurTimergalin:12
CREATE TABLE IF NOT EXISTS document_permissions
(
    user_to_document_id    BIGINT NOT NULL REFERENCES user_to_document (id) ON DELETE CASCADE,
    document_permission_id BIGINT NOT NULL REFERENCES document_permission (id) ON DELETE CASCADE,

    PRIMARY KEY (user_to_document_id, document_permission_id)
);

--changeset TimurTimergalin:13
CREATE TABLE IF NOT EXISTS voting_process
(
    id                  BIGSERIAL        NOT NULL,
    name                TEXT             NOT NULL,
    threshold           DOUBLE PRECISION NOT NULL,
    status              TEXT             NOT NULL,
    created_at          timestamptz      NOT NULL,
    deadline            timestamptz,
    document_version_id BIGINT           NOT NULL REFERENCES document_version (id) ON DELETE CASCADE,
    tenant_id           BIGINT           NOT NULL,

    PRIMARY KEY (id)
);

--changeset TimurTimergalin:14
CREATE TABLE IF NOT EXISTS vote
(
    id                  BIGSERIAL NOT NULL,
    status              TEXT      NOT NULL,
    application_user_id BIGINT    NOT NULL REFERENCES application_user (id) ON DELETE CASCADE,
    voting_process_id   BIGINT    NOT NULL REFERENCES voting_process (id) ON DELETE CASCADE,
    tenant_id           BIGINT    NOT NULL,

    PRIMARY KEY (id)
);

--changeset TimurTimergalin:15
CREATE TABLE IF NOT EXISTS global_permission_to_user
(
    application_user_id  BIGINT NOT NULL REFERENCES application_user (id) ON DELETE CASCADE,
    global_permission_id BIGINT NOT NULL REFERENCES global_permission (id) ON DELETE CASCADE,

    PRIMARY KEY (application_user_id, global_permission_id)
);

--changeset PAZderev:16
INSERT INTO document_permission (name)
SELECT 'READ'
    WHERE NOT EXISTS (SELECT 1 FROM document_permission WHERE name = 'READ');

INSERT INTO document_permission (name)
SELECT 'EDIT'
    WHERE NOT EXISTS (SELECT 1 FROM document_permission WHERE name = 'EDIT');

INSERT INTO document_permission (name)
SELECT 'SEND_FOR_SIGNING'
    WHERE NOT EXISTS (SELECT 1 FROM document_permission WHERE name = 'SEND_FOR_SIGNING');

INSERT INTO document_permission (name)
SELECT 'SEND_FOR_VOTING'
    WHERE NOT EXISTS (SELECT 1 FROM document_permission WHERE name = 'SEND_FOR_VOTING');

INSERT INTO document_permission (name)
SELECT 'CREATOR'
    WHERE NOT EXISTS (SELECT 1 FROM document_permission WHERE name = 'CREATOR');

--changeset ???:17
INSERT INTO global_permission (name)
VALUES ('USER');

INSERT INTO global_permission (name)
VALUES ('ADMIN');

INSERT INTO application_user (email, display_name, hashed_password, organization_id, tenant_id)
VALUES ('admin@gmail.com', 'SUPER ADMIN', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', NULL, 0);

INSERT INTO global_permission_to_user (application_user_id, global_permission_id)
VALUES (1, 2);

--changeset DenisKarpov:23
INSERT INTO organization (name, inn, is_active)
VALUES
    ('Organization A', 123, TRUE),
    ('Organization B', 321, TRUE);

INSERT INTO application_user (email, display_name, hashed_password, organization_id, tenant_id)
VALUES
    ('orgA_admin@gmail.com', 'Org A Admin', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 1, 1),
    ('orgB_admin@gmail.com', 'Org B Admin', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 2, 2);

INSERT INTO global_permission_to_user (application_user_id, global_permission_id)
VALUES (2, 2);

INSERT INTO global_permission_to_user (application_user_id, global_permission_id)
VALUES (3, 2);

--changeset ghostofendless:18
CREATE TABLE IF NOT EXISTS subscription
(
    id          BIGSERIAL PRIMARY KEY,
    document_id BIGINT    NOT NULL,
    user_email  TEXT      NOT NULL,
    tenant_id   BIGINT    NOT NULL
);

--changeset FkishDaniels:19
CREATE TABLE IF NOT EXISTS refresh_tokens(
    id                  BIGSERIAL                PRIMARY KEY,
    token               TEXT                     NOT NULL,
    expires_date        TIMESTAMP WITH TIME ZONE NOT NULL,
    application_user_id BIGSERIAL                NOT NULL REFERENCES application_user(id) ON DELETE CASCADE,
    tenant_id           BIGINT    NOT NULL
);

-- changeset FkishDaneils:20
CREATE TABLE IF NOT EXISTS tariff(
    id              BIGSERIAL                PRIMARY KEY,
    name            TEXT                     NOT NULL,
    price           DECIMAL                  NOT NULL,
    tariff_details  TEXT                     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    user_count      INTEGER                  NOT NULL
);

-- changeset hottabych04:21
CREATE TABLE IF NOT EXISTS bill(
    id          BIGSERIAL                PRIMARY KEY,
    tariff_id   BIGINT                   NOT NULL REFERENCES tariff(id) ON DELETE CASCADE,
--  заменить user на company в будущем
    user_id   BIGINT                   NOT NULL REFERENCES application_user(id) ON DELETE CASCADE,
    issued_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    is_paid BOOLEAN DEFAULT FALSE NOT NULL,
    paid_until TIMESTAMP WITH TIME ZONE NOT NULL
);
--
