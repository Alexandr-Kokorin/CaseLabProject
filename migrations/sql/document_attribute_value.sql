create table document_attribute_value
(
    document_id         bigint          not null references document(id) on delete cascade,
    attribute_id        bigint          not null references attribute(id) on delete cascade,
    app_value           text            not null
)
