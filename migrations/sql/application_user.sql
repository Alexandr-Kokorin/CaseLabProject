create table application_user
(
    id                  bigint        not null,
    login               text          not null,
    display_name        text          not null,
    hashed_password     text          not null,

    primary key (id)
)
