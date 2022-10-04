\c ccpr_main;

-- Confirm Password field 
insert into attributes(id, usage, name, description, title, placeholder, min, max, hint, mandatory, search_name)
values ('00000000-0000-0000-0000-03b7f3a00007', 'password', 'Confirm Password', '', 'Подтвердите Пароль', 'Qwerty1234!',  8, 30, '', true, 'password_confirm');

insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00007', 'student_registration');