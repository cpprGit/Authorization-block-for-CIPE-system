\c ccpr_main;

-- Student UsersRegistration Schema Default Attributes

-- Student Email - 00000000-0000-0000-0000-03b7f3a00000
-- Password - 00000000-0000-0000-0000-03b7f3a00001
-- Student Full Name - 00000000-0000-0000-0000-03b7f3a00002
-- Student Group - 00000000-0000-0000-0000-03b7f3a00003
-- Department - 00000000-0000-0000-0000-03b7f3a00004
-- Course - 00000000-0000-0000-0000-03b7f3a00005

insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('00000000-0000-11ea-87ee-000057480000', 'Регистрация студента', 'Временное описание', 'student_registration', 'Зарегистрироваться');
insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('aaa00ee1-5bb3-11ea-87ee-13ae57480000', 'Профиль студента', 'Временное описание', 'student_profile', 'Сохранить');


-- Email
insert into attributes(id, usage, name, description, title, placeholder, hint, mandatory, search_name)
values ('00000000-0000-0000-0000-03b7f3a00000', 'email', 'Student Email', '',  'Корпоративная электронная почта студента', 'youremail@edu.hse.ru', 'Почта должна заканчивать на @edu.hse.ru', true, 'email');

insert into validators(id, validator, message) values ('00000000-0000-0000-0012-4455aaa00001','^([a-z0-9_-]+\\.)*[a-z0-9_-]+@edu.hse.ru$', 'Неверный e-mail адрес');
insert into attribute_validators(attribute_id, validator_id) values('00000000-0000-0000-0000-03b7f3a00000', '00000000-0000-0000-0012-4455aaa00001');

-- Password
insert into attributes(id, usage, name, description, title, placeholder, min, max, hint, mandatory, search_name)
values ('00000000-0000-0000-0000-03b7f3a00001', 'password', 'Password', '', 'Пароль', 'Qwerty1234!',  8, 30, 'Пароль должен содержать заглавные буквы, строчные буквы и цифры. Длина пароля от 8 до 30 символов.', true, 'password');

insert into validators(id, validator, message) values ('00000000-0000-0000-0012-4455aaa00002','.*[A-ZА-Я].*', 'Необходимо ввести по-крайней мере одну заглавную букву');
insert into attribute_validators(attribute_id, validator_id) values('00000000-0000-0000-0000-03b7f3a00001', '00000000-0000-0000-0012-4455aaa00002');

insert into validators(id, validator, message) values ('00000000-0000-0000-0012-4455aaa00003','.*[a-zа-я].*', 'Необходимо ввести по-крайней мере одну строчную букву');
insert into attribute_validators(attribute_id, validator_id) values('00000000-0000-0000-0000-03b7f3a00001', '00000000-0000-0000-0012-4455aaa00003');

insert into validators(id, validator, message) values ('00000000-0000-0000-0012-4455aaa00004','.*[0-9].*', 'Необходимо ввести по-крайней мере одну цифру');
insert into attribute_validators(attribute_id, validator_id) values('00000000-0000-0000-0000-03b7f3a00001', '00000000-0000-0000-0012-4455aaa00004');

-- Name
insert into attributes(id, usage, name, description, title,  placeholder,  hint, mandatory, search_name)
values ('00000000-0000-0000-0000-03b7f3a00002', 'short_text', 'Student Full Name', '',  'ФИО', 'Иванов Иван Иванович', 'Введите полное имя.', true, 'name');

-- Student Group
insert into attributes(id, usage, name, description, title, mandatory, search_name)
values ('00000000-0000-0000-0000-03b7f3a00003', 'student_group', 'Student Group', '', 'Группа', true, 'group');

insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00001', 'БПИ162');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00002', 'БПИ161');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00003', 'БПИ171');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00004', 'БПИ181');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00005', 'БПИ191');

insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00006', 'БПМИ161');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00007', 'БПМИ171');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00008', 'БПМИ181');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00009', 'БПМИ191');

insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00010', 'ПАД181');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00011', 'ПАД191');

insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00003', '00000000-0000-0000-bbbb-6675aaa00001');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00003', '00000000-0000-0000-bbbb-6675aaa00002');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00003', '00000000-0000-0000-bbbb-6675aaa00003');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00003', '00000000-0000-0000-bbbb-6675aaa00004');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00003', '00000000-0000-0000-bbbb-6675aaa00005');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00003', '00000000-0000-0000-bbbb-6675aaa00006');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00003', '00000000-0000-0000-bbbb-6675aaa00007');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00003', '00000000-0000-0000-bbbb-6675aaa00008');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00003', '00000000-0000-0000-bbbb-6675aaa00009');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00003', '00000000-0000-0000-bbbb-6675aaa00010');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00003', '00000000-0000-0000-bbbb-6675aaa00011');

-- Department
insert into attributes(id, usage, name, description, title, mandatory, search_name)
values ('00000000-0000-0000-0000-03b7f3a00004', 'radio', 'Department', '', 'Образовательная программа', true, 'faculty');

insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00012', 'Программная Инженерия');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00013', 'Прикладная Математика и Информатика');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00014', 'Прикладной Анализ Данных');

insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00004', '00000000-0000-0000-bbbb-6675aaa00012');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00004', '00000000-0000-0000-bbbb-6675aaa00013');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00004', '00000000-0000-0000-bbbb-6675aaa00014');

-- Course
insert into attributes(id, usage, name, description, title, mandatory, search_name)
values ('00000000-0000-0000-0000-03b7f3a00005', 'radio', 'Course', '', 'Курс', true, 'course');

insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00015', '1');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00016', '2');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00017', '3');
insert into variants(id, variant) values ('00000000-0000-0000-bbbb-6675aaa00018', '4');

insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00005', '00000000-0000-0000-bbbb-6675aaa00015');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00005', '00000000-0000-0000-bbbb-6675aaa00016');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00005', '00000000-0000-0000-bbbb-6675aaa00017');
insert into attribute_variants(attribute_id, variant_id) values('00000000-0000-0000-0000-03b7f3a00005', '00000000-0000-0000-bbbb-6675aaa00018');

-- Status
insert into attributes(id, usage, name, description, title, mandatory, search_name)
values ('00000000-0000-0000-0000-03b7f3a00006', 'radio', 'Student status', '', 'Статус', false, 'status');

insert into schema_attributes(schema_id, attribute_id)
values ('00000000-0000-11ea-87ee-000057480000', '00000000-0000-0000-0000-03b7f3a00000');

insert into schema_attributes(schema_id, attribute_id)
values ('00000000-0000-11ea-87ee-000057480000', '00000000-0000-0000-0000-03b7f3a00001');

insert into schema_attributes(schema_id, attribute_id)
values ('00000000-0000-11ea-87ee-000057480000', '00000000-0000-0000-0000-03b7f3a00002');

insert into schema_attributes(schema_id, attribute_id)
values ('00000000-0000-11ea-87ee-000057480000', '00000000-0000-0000-0000-03b7f3a00003');

insert into schema_attributes(schema_id, attribute_id)
values ('00000000-0000-11ea-87ee-000057480000', '00000000-0000-0000-0000-03b7f3a00004');

insert into schema_attributes(schema_id, attribute_id)
values ('00000000-0000-11ea-87ee-000057480000', '00000000-0000-0000-0000-03b7f3a00005');


insert into schema_attributes(schema_id, attribute_id)  values ('aaa00ee1-5bb3-11ea-87ee-13ae57480000', '00000000-0000-0000-0000-03b7f3a00000');
insert into schema_attributes(schema_id, attribute_id)  values ('aaa00ee1-5bb3-11ea-87ee-13ae57480000', '00000000-0000-0000-0000-03b7f3a00001');
insert into schema_attributes(schema_id, attribute_id)  values ('aaa00ee1-5bb3-11ea-87ee-13ae57480000', '00000000-0000-0000-0000-03b7f3a00002');
insert into schema_attributes(schema_id, attribute_id)  values ('aaa00ee1-5bb3-11ea-87ee-13ae57480000', '00000000-0000-0000-0000-03b7f3a00003');
insert into schema_attributes(schema_id, attribute_id)  values ('aaa00ee1-5bb3-11ea-87ee-13ae57480000', '00000000-0000-0000-0000-03b7f3a00004');
insert into schema_attributes(schema_id, attribute_id)  values ('aaa00ee1-5bb3-11ea-87ee-13ae57480000', '00000000-0000-0000-0000-03b7f3a00005');


insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00000', 'student_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00001', 'student_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00002', 'student_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00003', 'student_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00004', 'student_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00005', 'student_registration');

-- insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00006', 'student_profile_template');


-- /Student UsersRegistration Schema
