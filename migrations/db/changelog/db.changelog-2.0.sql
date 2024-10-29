--liquibase formatted sql

--changeset ghostofendless:1
INSERT INTO application_user(email, display_name, hashed_password)
VALUES ('auth@example.com', 'Name Surname',
        '$2a$10$i58RTrOjK0TvRu2f.eQ6vu/ongBj9SuwzYDrhuq5dYf/t9/rUxRFm'),
       ('user@example.com', 'Name Surname',
        '$2a$10$i58RTrOjK0TvRu2f.eQ6vu/ongBj9SuwzYDrhuq5dYf/t9/rUxRFm');

INSERT INTO global_permission_to_user(application_user_id, global_permission_id)
VALUES (2, 1), (3, 1);



