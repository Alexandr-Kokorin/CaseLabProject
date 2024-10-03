create table document_type_to_attribute
(
    document_type_id        bigint          not null references document_type(id) on delete cascade,
    attribute_id            bigint          not null references attribute(id) on delete cascade
)
