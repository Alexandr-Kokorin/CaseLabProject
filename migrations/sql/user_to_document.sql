create table user_to_document
(
    document_id        bigint          not null references document(id) on delete cascade,
    user_id            bigint          not null references application_user(id) on delete cascade
)
