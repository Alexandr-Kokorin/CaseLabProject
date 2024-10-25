--liquibase formatted sql

--changeset ghostofendless:1
INSERT INTO application_user(email, display_name, hashed_password)
VALUES ('auth@example.com', 'Name Surname',
        '$2a$10$i58RTrOjK0TvRu2f.eQ6vu/ongBj9SuwzYDrhuq5dYf/t9/rUxRFm'),
       ('user@example.com', 'Name Surname',
        '$2a$10$i58RTrOjK0TvRu2f.eQ6vu/ongBj9SuwzYDrhuq5dYf/t9/rUxRFm')



