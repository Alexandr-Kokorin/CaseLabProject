--liquibase formatted sql

--changeset seshxyz:1
INSERT INTO department(name, is_active, is_top_department, head_id_of_department, parent_department_id)
VALUES   ('Администрация', TRUE, FALSE, NULL, NULL),
         ('Департамент информационных технологий', TRUE, FALSE, NULL, NULL),
         ('Управление информационных технологий и сервисов', TRUE, FALSE, NULL, NULL),
         ('Отдел разработки', TRUE, FALSE, NULL, NULL),
         ('Отдел сопровождения и тестирования', TRUE, TRUE, NULL, NULL),
         ('Отдел маркетинга', TRUE, FALSE, NULL, NULL);

INSERT INTO application_user(email, display_name, hashed_password, position, department_id)
VALUES
    ('user2@test.com', 'Борисов Иван Петрович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Разработчик', 4),
    ('user3@test.com', 'Иванов Борис Сидорович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Старший разработчик', 4),
    ('user4@test.com', 'Петров Сидор Александрович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Начальник отдела', 4),
    ('user5@test.com', 'Сидоров Александр Михайлович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Специалист', 5),
    ('user6@test.com', 'Александров Михаил Александрович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Старший специалист', 6),
    ('user7@test.com', 'Михаилов Георгий Иванович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Директор департамента', 2),
    ('user8@test.com', 'Смирнов Михаил Георгиевич', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Начальник отдела', 5),
    ('user9@test.com', 'Георгиев Андрей Михайлович', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Системный администратор', 5),
    ('user10@test.com', 'Протасов Виктор Дмитриевич', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Помощник руководителя', 3),
    ('user11@test.com', 'Викторов Дмитрий Евгеньевич', '$2a$10$WFRQhlz7Ul85HsRjMg3XNutiB//3HLloe3vTuW6GDPD9eeXeYXiJe', 'Заместитель директора департамента - начальник управления', 3);

--changeset seshxyz:2
UPDATE application_user SET department_id = 1, position = 'Администратор' WHERE id = 1;
UPDATE department set head_id_of_department = 1 WHERE id = 1;

UPDATE department
    SET head_id_of_department = 4
WHERE id = 2;

UPDATE department
    SET head_id_of_department = 11,
    parent_department_id = 2
WHERE id = 3;

UPDATE department
    SET head_id_of_department = 1,
    parent_department_id = 3
WHERE id = 4;

UPDATE department
    SET head_id_of_department = 8,
    parent_department_id = 3
WHERE id = 5;
