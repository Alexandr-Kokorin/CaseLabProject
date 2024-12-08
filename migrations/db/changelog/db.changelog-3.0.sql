--liquibase formatted sql

--changeset seshxyz:1
INSERT INTO organization(name, inn, is_active, tenant_id)
VALUES ('ООО "Номер один"', '1111111111', TRUE, 'tenant-1'),
       ('АО "Номер два"', '2222222222', TRUE, 'tenant-2'),
       ('ЗАО "Номер три"', '3333333333', TRUE, 'tenant-3');

INSERT INTO department(name, is_active, is_top_department, head_email_of_department, parent_department_id, tenant_id)
VALUES   ('Администратор', TRUE, TRUE, NULL, NULL, 'tenant-1'),
         ('Департамент информационных технологий', TRUE, TRUE, NULL, NULL, 'tenant-1'),
         ('Управление информационных технологий и сервисов', TRUE, FALSE, NULL, NULL, 'tenant-1'),
         ('Отдел разработки', TRUE, FALSE, NULL, NULL, 'tenant-1'),
         ('Отдел сопровождения и тестирования', TRUE, FALSE, NULL, NULL, 'tenant-1'),
         ('Отдел маркетинга', TRUE, TRUE, NULL, NULL, 'tenant-1'),
         ('Администратор', TRUE, TRUE, NULL, NULL, 'tenant-2'),
         ('Отдел продаж', TRUE, TRUE, NULL, NULL, 'tenant-2'),
         ('Администратор', TRUE, TRUE, NULL, NULL, 'tenant-3');

INSERT INTO application_user(email, is_working, display_name, hashed_password, position, department_id, tenant_id, organization_id)
VALUES
    ('user2@test.com', true, 'Борисов Иван Петрович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Разработчик', 4, 'tenant-1', 1),
    ('user3@test.com', true, 'Иванов Борис Сидорович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Старший разработчик', 4, 'tenant-1', 1),
    ('user4@test.com', true, 'Петров Сидор Александрович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Начальник отдела', 4, 'tenant-1', 1),
    ('user5@test.com', true, 'Сидоров Александр Михайлович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Специалист', 5, 'tenant-1', 1),
    ('user6@test.com', true, 'Александров Михаил Александрович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Старший специалист', 6, 'tenant-1', 1),
    ('user7@test.com', true, 'Михаилов Георгий Иванович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Директор департамента', 2, 'tenant-1', 1),
    ('user8@test.com', true, 'Смирнов Михаил Георгиевич', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Начальник отдела', 5, 'tenant-1', 1),
    ('admin_1@test.com', true, 'Георгиев Андрей Михайлович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Системный администратор', 1, 'tenant-1', 1),
    ('user10@test.com', true, 'Протасов Виктор Дмитриевич', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Помощник руководителя', 3, 'tenant-1', 1),
    ('user11@test.com', true, 'Викторов Дмитрий Евгеньевич', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Заместитель директора департамента - начальник управления', 3, 'tenant-1', 1),
    ('admin_2@test.com', true, 'Разбор Полётов', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Системный администратор', 7, 'tenant-2', 2),
    ('user13@test.com', true, 'Налог Сдоходов', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Специалист', 8, 'tenant-2', 2),
    ('admin_3@test.com', true, 'Админи Страторов', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Системный администратор', 9, 'tenant-3', 3);

--changeset seshxyz:2
UPDATE department
    SET head_email_of_department = 'admin_1@test.com'
WHERE id = 1;

UPDATE department
    SET head_email_of_department = 'user7@test.com'
WHERE id = 2;

UPDATE department
    SET head_email_of_department = 'user11@test.com',
    parent_department_id = 2
WHERE id = 3;

UPDATE department
    SET head_email_of_department = 'user3@test.com',
    parent_department_id = 3
WHERE id = 4;

UPDATE department
    SET head_email_of_department = 'user8@test.com',
    parent_department_id = 3
WHERE id = 5;

UPDATE department
    SET head_email_of_department = 'admin_2@test.com'
WHERE id = 7;

UPDATE department
SET head_email_of_department = 'admin_3@test.com'
WHERE id = 9;

INSERT INTO global_permission_to_user(application_user_id, global_permission_id)
VALUES (9, 2), (12, 2);
