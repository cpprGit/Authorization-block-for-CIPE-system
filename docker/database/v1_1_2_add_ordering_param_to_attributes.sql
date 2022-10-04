\c ccpr_main;

ALTER TABLE attributes ADD ordering INT;
UPDATE attributes SET ordering = 0 WHERE id = '00000000-0000-0000-0000-03b7f3a00000';
UPDATE attributes SET ordering = 1 WHERE id = '00000000-0000-0000-0000-03b7f3a00001';
UPDATE attributes SET ordering = 2 WHERE id = '00000000-0000-0000-0000-03b7f3a00007';
UPDATE attributes SET ordering = 3 WHERE id = '00000000-0000-0000-0000-03b7f3a00002';
UPDATE attributes SET ordering = 4 WHERE id = '00000000-0000-0000-0000-03b7f3a00003';
UPDATE attributes SET ordering = 5 WHERE id = '00000000-0000-0000-0000-03b7f3a00004';
UPDATE attributes SET ordering = 6 WHERE id = '00000000-0000-0000-0000-03b7f3a00005';
