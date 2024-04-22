
ALTER table items add check(id <> '');
ALTER table items add check(name <> '');
ALTER table items add check(quality >= 0);
