--liquibase formatted sql

--changeset ghostofendless:1
INSERT INTO document_type (id, name)
VALUES
    (1, 'Contract'),
    (2, 'Invoice'),
    (3, 'Report'),
    (4, 'Specification')
ON CONFLICT (id) DO NOTHING;

--changeset ghostofendless:2
INSERT INTO document (document_type_id, name)
VALUES
    (1, 'Customer Agreement 2024'),
    (1, 'Employee Contract'),
    (2, 'Invoice #12345'),
    (2, 'Invoice #12346'),
    (3, 'Annual Report 2023'),
    (3, 'Monthly Report March'),
    (4, 'Product Specification v1'),
    (4, 'Technical Requirements');

--changeset ghostofendless:3
INSERT INTO document_version (name, created_at, content_name, document_id)
SELECT
    d.name || ' v' || v.version as name,
    NOW() - (v.version || ' days')::interval as created_at,
    'https://storage.example.com/documents/' || d.id || '/version-' || v.version || '.pdf' as content_name,
    d.id as document_id
FROM document d
         CROSS JOIN (VALUES (1), (2), (3)) as v(version)
WHERE d.id <= 4;

--changeset ghostofendless:4
INSERT INTO document_version (name, created_at, content_name, document_id)
VALUES
    ('Draft Report 2023', NOW() - interval '1 day', NULL, 5),
    ('Draft Report 2023 v2', NOW(), NULL, 5);
