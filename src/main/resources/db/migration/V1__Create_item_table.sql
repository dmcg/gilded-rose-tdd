create table if not exists public.items
(
    id           varchar(100) not null,
    modified     timestamp with time zone   not null,
    name         varchar(100) not null,
    "sellByDate" date,
    quality      integer      not null
    );

create index if not exists items_modified
    on public.items (modified);
