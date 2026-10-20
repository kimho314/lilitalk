create table if not exists book
(
    book_id bigserial
    constraint book_pk
    primary key,
    title   varchar(300) not null,
    isbn    varchar(30)
);