--liquibase formatted sql

--changeset hottabych04:1
CREATE TABLE IF NOT EXISTS application_user
(
    id                  BIGSERIAL     NOT NULL,
    login               TEXT          NOT NULL,
    display_name        TEXT          NOT NULL,
    hashed_password     TEXT          NOT NULL,
    role                TEXT          NOT NULL,
    PRIMARY KEY (id)
)

--changeset hottabych04:2
CREATE TABLE IF NOT EXISTS attribute
(
    id             BIGSERIAL         NOT NULL,
    name           TEXT           NOT NULL,
    type           TEXT           NOT NULL,

    PRIMARY KEY (id)
)

--changeset hottabych04:3
CREATE TABLE IF NOT EXISTS document_type
(
    id              BIGSERIAL,
    name            TEXT          NOT NULL,

    PRIMARY KEY (id)
)

--changeset hottabych04:4
CREATE TABLE IF NOT EXISTS document
(
    id                     BIGSERIAL           NOT NULL,
    document_type_id       BIGINT           NOT NULL,

    PRIMARY KEY (id)
)

--changeset hottabych04:5
CREATE TABLE IF NOT EXISTS user_to_document
(
    document_id        BIGINT          NOT NULL REFERENCES document(id) ON DELETE CASCADE,
    user_id            BIGINT          NOT NULL REFERENCES application_user(id) ON DELETE CASCADE
)

--changeset hottabych04:6
CREATE TABLE IF NOT EXISTS document_attribute_value
(
    document_id         BIGINT          NOT NULL REFERENCES document(id) ON DELETE CASCADE,
    attribute_id        BIGINT          NOT NULL REFERENCES attribute(id) ON DELETE CASCADE,
    app_value           TEXT            NOT NULL,
    PRIMARY KEY (document_id, attribute_id)
)

--changeset hottabych04:7
CREATE TABLE IF NOT EXISTS document_type_to_attribute
(
    document_type_id        BIGINT          NOT NULL REFERENCES document_type(id) ON DELETE CASCADE,
    attribute_id            BIGINT          NOT NULL REFERENCES attribute(id) ON DELETE CASCADE
)
