
alter table items add check(id <> '');
alter table items add check(name <> '');
alter table items add check(quality >= 0);
