--liquibase formatted sql

-- Создаем организацию и администратора
--changeset Alexandr-Kokorin:1
INSERT INTO organization (name, inn, tenant_id)
VALUES ('Tech Innovations', '1234567890', 'Tech Innovations');

INSERT INTO application_user (email, display_name, hashed_password, position, is_working, department_id,
                              substitution_id, organization_id, tenant_id)
VALUES ('admin@example.com', 'Администратор', '$2a$10$9uEGIjUsyeGPzjKxA29Bw.msMU0h7fmOotH7vhMp1MhNOSZjhJdm.', 'Администратор', TRUE, NULL, NULL, 1, 'Tech Innovations');

INSERT INTO global_permission_to_user (application_user_id, global_permission_id)
VALUES (2, 2); -- Администратор получает глобальные права администратора

-- Создаем 10 пользователей
--changeset Alexandr-Kokorin:2
INSERT INTO application_user (email, display_name, hashed_password, position, is_working, department_id,
                              substitution_id, organization_id, tenant_id)
VALUES
    ('user1@example.com', 'Иван Иванов', '$2a$10$9uEGIjUsyeGPzjKxA29Bw.msMU0h7fmOotH7vhMp1MhNOSZjhJdm.', 'Разработчик', TRUE, NULL, NULL, 1, 'Tech Innovations'),
    ('user2@example.com', 'Мария Смирнова', '$2a$10$9uEGIjUsyeGPzjKxA29Bw.msMU0h7fmOotH7vhMp1MhNOSZjhJdm.', 'Тестировщик', TRUE, NULL, NULL, 1, 'Tech Innovations'),
    ('user3@example.com', 'Алексей Кузнецов', '$2a$10$9uEGIjUsyeGPzjKxA29Bw.msMU0h7fmOotH7vhMp1MhNOSZjhJdm.', 'Менеджер проекта', TRUE, NULL, NULL, 1, 'Tech Innovations'),
    ('user4@example.com', 'Ольга Попова', '$2a$10$9uEGIjUsyeGPzjKxA29Bw.msMU0h7fmOotH7vhMp1MhNOSZjhJdm.', 'Дизайнер', TRUE, NULL, NULL, 1, 'Tech Innovations'),
    ('user5@example.com', 'Дмитрий Васильев', '$2a$10$9uEGIjUsyeGPzjKxA29Bw.msMU0h7fmOotH7vhMp1MhNOSZjhJdm.', 'Аналитик', TRUE, NULL, NULL, 1, 'Tech Innovations'),
    ('user6@example.com', 'Елена Михайлова', '$2a$10$9uEGIjUsyeGPzjKxA29Bw.msMU0h7fmOotH7vhMp1MhNOSZjhJdm.', 'HR-менеджер', TRUE, NULL, NULL, 1, 'Tech Innovations'),
    ('user7@example.com', 'Сергей Фёдоров', '$2a$10$9uEGIjUsyeGPzjKxA29Bw.msMU0h7fmOotH7vhMp1MhNOSZjhJdm.', 'Разработчик', TRUE, NULL, NULL, 1, 'Tech Innovations'),
    ('user8@example.com', 'Анна Соколова', '$2a$10$9uEGIjUsyeGPzjKxA29Bw.msMU0h7fmOotH7vhMp1MhNOSZjhJdm.', 'Технический писатель', TRUE, NULL, NULL, 1, 'Tech Innovations'),
    ('user9@example.com', 'Павел Семёнов', '$2a$10$9uEGIjUsyeGPzjKxA29Bw.msMU0h7fmOotH7vhMp1MhNOSZjhJdm.', NULL, FALSE, NULL, NULL, 1, 'Tech Innovations'),
    ('user10@example.com', 'Виктория Романова', '$2a$10$9uEGIjUsyeGPzjKxA29Bw.msMU0h7fmOotH7vhMp1MhNOSZjhJdm.', NULL, FALSE, NULL, NULL, 1, 'Tech Innovations');

-- Выдаем права пользователям
--changeset Alexandr-Kokorin:3
INSERT INTO global_permission_to_user (application_user_id, global_permission_id)
VALUES
    (3, 1), (4, 1), (5, 1), (6, 1), (7, 1),
    (8, 1), (9, 1), (10, 1), (11, 1), (12, 1); -- Пользователи получают роль USER

-- Создаем два подразделения
--changeset Alexandr-Kokorin:4
INSERT INTO department (name, is_active, is_top_department, head_email_of_department, tenant_id)
VALUES
    ('Разработка', TRUE, TRUE, 'user1@example.com', 'Tech Innovations'),
    ('Тестирование', TRUE, TRUE, 'user2@example.com', 'Tech Innovations');

-- Добавляем пользователей в подразделения
--changeset Alexandr-Kokorin:5
UPDATE application_user
SET department_id = 1
WHERE email IN ('user1@example.com', 'user3@example.com', 'user4@example.com', 'user7@example.com');

UPDATE application_user
SET department_id = 2
WHERE email IN ('user2@example.com', 'user5@example.com', 'user6@example.com', 'user8@example.com');

-- Создаем 5 атрибутов
--changeset Alexandr-Kokorin:6
INSERT INTO attribute (name, type, tenant_id)
VALUES
    ('Уровень приоритета', 'TEXT', 'Tech Innovations'),
    ('Код подразделения', 'NUMBER', 'Tech Innovations'),
    ('Срок действия', 'DATE', 'Tech Innovations'),
    ('Конфиденциальность', 'BOOLEAN', 'Tech Innovations'),
    ('Ответственное лицо', 'TEXT', 'Tech Innovations');

-- Создаем 3 типа документов
--changeset Alexandr-Kokorin:7
INSERT INTO document_type (name, tenant_id)
VALUES
    ('Техническая спецификация', 'Tech Innovations'),
    ('Отчет о проекте', 'Tech Innovations'),
    ('Кадровая политика', 'Tech Innovations');

-- Связываем атрибуты с типами документов
--changeset Alexandr-Kokorin:8
INSERT INTO document_type_to_attribute (document_type_id, attribute_id, is_optional, tenant_id)
VALUES
    (1, 1, TRUE, 'Tech Innovations'),
    (1, 3, TRUE, 'Tech Innovations'),
    (2, 4, TRUE, 'Tech Innovations'),
    (3, 1, TRUE, 'Tech Innovations'),
    (3, 2, TRUE, 'Tech Innovations'),
    (3, 4, TRUE, 'Tech Innovations');
