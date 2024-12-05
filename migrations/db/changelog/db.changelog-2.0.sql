--liquibase formatted sql

--changeset DenisKarpov:2
INSERT INTO organization (name, inn, is_active, tenant_id)
VALUES ('Organization A', '1234567890', TRUE, 'tenant_1');

--changeset ghostofendless:1
INSERT
INTO application_user(email, display_name, hashed_password, organization_id, tenant_id)

VALUES ('admin@gmail.com', 'Admin', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 1, 'tenant_1'),
       ('auth@example.com', 'Name Surname', '$2a$10$i58RTrOjK0TvRu2f.eQ6vu/ongBj9SuwzYDrhuq5dYf/t9/rUxRFm', 1,
        'tenant_1'),
       ('user@example.com', 'Name Surname', '$2a$10$i58RTrOjK0TvRu2f.eQ6vu/ongBj9SuwzYDrhuq5dYf/t9/rUxRFm', 1,
        'tenant_1');

INSERT INTO global_permission_to_user(application_user_id, global_permission_id)
VALUES (2, 2),
       (3, 1),
       (4, 1);

