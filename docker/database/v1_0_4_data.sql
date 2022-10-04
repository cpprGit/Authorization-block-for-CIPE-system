\c ccpr_main;



-- TO REPLACE OR DELETE
insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('eff17fe4-5bb3-11ea-87ee-13ae5748cec9', 'test schema_1', 'desc', 'student_profile', 'button_name');
insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('baa17fe4-5bb3-11ea-87ee-13ae5748cec1', 'test schema_2', 'desc', 'user_profile', 'button_name');
insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('aee17fe4-5bb3-11ea-87ee-13ae5748ce37', 'test schema_4', 'desc', 'user_profile', 'button_name');
-- /TO REPLACE OR DELETE

insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('fcc17fe4-5bb3-11ea-87ee-13ae5748ce22', 'Создание активности', '', 'activity', 'Создать');

insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('01000010-0280-27ea-87ee-000057480001', 'Регистрация пользователя', '', 'user_registration', 'Зарегистрировать');
insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('01000010-0280-27ea-87ee-000057480002', 'Профиль пользователя', 'Временное описание', 'user_profile', 'Сохранить');



-- Supervisor
insert into schema_content (id, schema_id, content) values  ('11112222-5baf-12ea-907c-edcba0000006', '01000010-0280-27ea-87ee-000057480002', '{}');
insert into users (id, name, schema_content_id, type, email, password, created_time) values ('123e4567-e89b-12d3-a000-000000000000','Руководитель ЦППР','11112222-5baf-12ea-907c-edcba0000006', 'supervisor', 'supervisor@edu.hse.ru', 'Qwerty12345',NOW());
-- /Supervisor


-- Help Schema
insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('bee22fe5-5bb3-11ea-87ee-13ae5748ce38', 'Форма помощи', 'Ждем от Вас предложений по улучшению сервиса, баг репорты и вопросы!', 'help', 'Отправить');

insert into attributes(id, usage, name, description, title, mandatory, hint)
values ('5587600b-8bb8-11ea-10dd-07ae8aacce01', 'short_text', 'Help Full Name', '', 'ФИО', false, 'Вы можете оставить заявку анонимно');

insert into attributes(id, usage, name, description, title, mandatory, hint)
values ('5587600b-8bb8-11ea-10dd-07ae8aacce02', 'short_text', 'Help Email', '', 'Электронная почта', false, 'Необходима только для получения обратной связи');

insert into attributes(id, usage, name, description, title, mandatory)
values ('5587600b-8bb8-11ea-10dd-07ae8aacce03', 'long_text', 'Help Problem Statement', '', 'Описание проблемы/пожелания', true);

insert into schema_attributes(schema_id,  attribute_id ) values ('bee22fe5-5bb3-11ea-87ee-13ae5748ce38', '5587600b-8bb8-11ea-10dd-07ae8aacce01');
insert into schema_attributes(schema_id,  attribute_id ) values ('bee22fe5-5bb3-11ea-87ee-13ae5748ce38', '5587600b-8bb8-11ea-10dd-07ae8aacce02');
insert into schema_attributes(schema_id,  attribute_id ) values ('bee22fe5-5bb3-11ea-87ee-13ae5748ce38', '5587600b-8bb8-11ea-10dd-07ae8aacce03');

-- /Help Schema


insert into schema_content(id, schema_id, content) values ('8a00008a-7777-3344-108f-56dcccc70000', 'fcc17fe4-5bb3-11ea-87ee-13ae5748ce22', '{"attribute_1":"attribute_1","attribute_2":"attribute_2"}');
insert into schema_content(id, schema_id, content) values ('8a00008a-7777-3344-108f-56dcccc70001', 'bb0000aa-eeee-33aa-44ee-000000000001', '{"attribute_1":"attribute_1","attribute_2":"attribute_2"}');
insert into schema_content(id, schema_id, content) values ('8a00008a-7777-3344-108f-56dcccc70002', 'fcc17fe4-5bb3-11ea-87ee-13ae5748ce22', '{"attribute_1":"attribute_1","attribute_2":"attribute_2"}');


insert into schema_content (id, schema_id, content) values ('90fa608a-6aeb-33ae-907c-17d628cb722b', 'eff17fe4-5bb3-11ea-87ee-13ae5748cec9', '{"attribute_1":"attribute_1","attribute_2":"attribute_2"}');
insert into schema_content (id, schema_id, content) values  ('111a1111-e89b-12d3-a000-000000000000', 'eff17fe4-5bb3-11ea-87ee-13ae5748cec9', '{"attribute_1":"attribute_1","attribute_2":"attribute_2"}');
insert into schema_content (id, schema_id, content) values  ('24eb508a-5baf-11ea-907c-17d628cb511a', 'eff17fe4-5bb3-11ea-87ee-13ae5748cec9', '{"attribute_1":"attribute_1","attribute_2":"attribute_2"}');
insert into schema_content (id, schema_id, content) values  ('25eb508a-5baf-11ea-907c-17d628cb511a', 'aaa00ee1-5bb3-11ea-87ee-13ae57480000', '{"Личная электронная почта студента":"blackheart@gmail.com","attribute_2":"attribute_2"}');

insert into schema_content (id, schema_id, content) values  ('30eb508a-5baf-11ea-907c-17d628cb511a', 'aaa00ee1-5bb3-11ea-87ee-13ae57480000', '{"Личная электронная почта студента":"nastya.k.r@mail.ru","attribute_2":"attribute_2"}');

insert into schema_content (id, schema_id, content) values  ('27eb608a-5baf-12ea-907c-17d628cb655a', '01000010-0280-27ea-87ee-000057480002', '{"attribute_1":"attribute_1","attribute_2":"attribute_2"}');
insert into schema_content (id, schema_id, content) values  ('11112222-5baf-12ea-907c-edcba0000001', '01000010-0280-27ea-87ee-000057480002', '{"attribute_3":"Анатолий Германович Краснов"}');
insert into schema_content (id, schema_id, content) values  ('11112222-5baf-12ea-907c-edcba0000002', '01000010-0280-27ea-87ee-000057480002', '{"attribute_3":"Виктория Александровна Посадская"}');
insert into schema_content (id, schema_id, content) values  ('11112222-5baf-12ea-907c-edcba0000003', '01000010-0280-27ea-87ee-000057480002', '{"attribute_3":"Леонид Павлович Гудин"}');

insert into users (id, name, email, password, type, schema_content_id, created_by, created_time)
values('7f5c1678-61f2-11ea-8cdc-dfb38868a744','Василий Иванович Скворцов','email', '0000','student', '25eb508a-5baf-11ea-907c-17d628cb511a', '111a1111-e89b-12d3-a000-000000000000', current_timestamp);


insert into attributes(id, usage, name, description, title, step, placeholder, min, max, hint, mandatory, value_default)
values ('8f3a803c-5bb4-11ea-91dd-f7ae0ab89533', 'email', 'attribute_1', 'description', 'title', 3,  'placeholder', 0, 5, 'hint', false, 'value_default');
insert into attributes(id, usage, name, description, title, step, placeholder, min, max, hint, mandatory, value_default)
values ('a69bf79c-5bb4-11ea-87ee-03b7f37be8e6', 'password', 'attribute_2', 'description',  'title', 4,'placeholder',  0, 5, 'hint', false, 'value_default');
insert into attributes(id, usage, name, description, title, placeholder, hint, mandatory, value_default)
values ('b44bf79c-5bb4-11ea-87ee-03b7f37be8e8', 'short_text', 'attribute_3', '', 'ФИО',  '', '', false, '');

insert into attributes(id, usage, name, description, title, placeholder, hint, mandatory, value_default)
values ('aaabf79c-5bb4-11ea-87ee-03b7f37be111', 'email', 'Личная электронная почта студента', '',  'Личная электронная почта', '',  '', false, '');


-- Students

-- 1. создать schema-content (уникальный для пользователя)
-- 2. заполнить контент в schema-content (сейчас student_profile schema id = aaa00ee1-5bb3-11ea-87ee-13ae57480000), лучше не меняй
-- 3. задать контент пользователю (студенту)
-- 4. далее по аналогии

insert into schema_content (id, schema_id, content) values  ('26eb508a-5baf-11ea-907c-17d628cb511a', 'aaa00ee1-5bb3-11ea-87ee-13ae57480000', '{"Личная электронная почта студента":"dimdim@ya.ru","Eye color":"Зелёный", "Student Email":"randomemail@edu.hse.ru", "Student Full Name": "Дмитрий Петрович Воробьев"}');

insert into users (id, name, email, password, type, schema_content_id, created_time)
values('00000000-eeee-67aa-87ee-000099340000','Дмитрий Петрович Воробьев','sparrowich@edu.hse.ru', '0000', 'student', '26eb508a-5baf-11ea-907c-17d628cb511a', current_timestamp);

insert into student_info(id, group_name, faculty, course, email) values ('00000000-dddd-88aa-88ee-000099340000', 'БПИ172', 'Программная Инженерия', '4', 'randomemail123@edu.hse.ru');
insert into student_info_students(student_id, student_info_id) values ('00000000-eeee-67aa-87ee-000099340000', '00000000-dddd-88aa-88ee-000099340000');


insert into schema_content (id, schema_id, content) values  ('29aaaaaa-5baf-11ea-907c-17d628cb0000', 'aaa00ee1-5bb3-11ea-87ee-13ae57480000', '{"Личная электронная почта студента":"cat@mail.ru","Eye color":"Голубой", "Student Email":"siamcat@edu.hse.ru", "Student Full Name": "Екатерина Игнатьева Скворцова"}');

insert into users (id, name, email, password, type, schema_content_id, created_time)
values('00000000-eeee-67aa-87ee-000099340001','Екатерина Игнатьева Скворцова','siamcat@edu.hse.ru', '0000','student', '29aaaaaa-5baf-11ea-907c-17d628cb0000', current_timestamp);

insert into student_info(id, group_name, faculty, course, email) values ('00000000-dddd-88aa-88ee-000099340001', 'БПМИ182', 'Прикладная Математика и Информатика', '2', 'siamcat@edu.hse.ru');
insert into student_info_students(student_id, student_info_id) values ('00000000-eeee-67aa-87ee-000099340001', '00000000-dddd-88aa-88ee-000099340001');


-- /Students

insert into users (id, name, schema_content_id, type, email, password, created_time) values ('0a9728ac-9d00-11ea-b58a-4b77bf19dd29','Казанцева Анастасия Романовна','30eb508a-5baf-11ea-907c-17d628cb511a', 'student', 'arkazantseva@edu.hse.ru', '0000', NOW());
insert into student_info(id, group_name, faculty, course, email) values ('00000000-dddd-88aa-88ee-000099340012', 'БПИ162', 'Программная Инженерия', '4', 'arkazantseva@edu.hse.ru');
insert into student_info_students(student_id, student_info_id) values ('0a9728ac-9d00-11ea-b58a-4b77bf19dd29', '00000000-dddd-88aa-88ee-000099340012');

-- Organisations

insert into attributes(id, usage, name, description, title, mandatory)
values ('9999900a-7bb7-11ea-90dd-07ae0affff01', 'checkbox', 'Organisation type', '', 'Тип организации', true);

insert into variants(id, variant) VALUES ('00004555-0000-0000-cccc-7772aaa00001', 'Банк');
insert into variants(id, variant) VALUES ('00004555-0000-0000-cccc-7772aaa00002', 'IT');
insert into variants(id, variant) VALUES ('00004555-0000-0000-cccc-7772aaa00003', 'Консалтинг');


insert into attribute_variants(attribute_id, variant_id) VALUES ('9999900a-7bb7-11ea-90dd-07ae0affff01', '00004555-0000-0000-cccc-7772aaa00001');
insert into attribute_variants(attribute_id, variant_id) VALUES ('9999900a-7bb7-11ea-90dd-07ae0affff01', '00004555-0000-0000-cccc-7772aaa00002');
insert into attribute_variants(attribute_id, variant_id) VALUES ('9999900a-7bb7-11ea-90dd-07ae0affff01', '00004555-0000-0000-cccc-7772aaa00003');


insert into attributes(id, usage, name, description, title, placeholder, mandatory)
values ('9999900a-7bb7-11ea-90dd-07ae0affff02', 'short_text', 'Organization name', '', 'Наименование организации', 'OOO "Организация"', true);


insert into default_attributes(attribute_id, schema_type) values ('9999900a-7bb7-11ea-90dd-07ae0affff01', 'org_profile_template');
insert into default_attributes(attribute_id, schema_type) values ('9999900a-7bb7-11ea-90dd-07ae0affff02', 'org_profile_template');

-- /Organisations



-- Activity

-- name
-- description
-- year
-- stages
-- faculty 00000000-0000-0000-0000-03b7f3a00004

insert into attributes(id, usage, name, description, title, mandatory)
values ('1987600a-7bb7-11ea-90dd-07ae0affee01', 'short_text', 'Activity Name 1', '', 'Наименование Активности', true);

insert into attributes(id, usage, name, description, title, mandatory)
values ('1987600a-7bb7-11ea-90dd-07ae0affee02', 'long_text', 'Activity Description 1', '', 'Описание', true);

insert into attributes(id, usage, name, description, title, mandatory)
values ('1987600a-7bb7-11ea-90dd-07ae0affee03', 'number', 'Activity Year 1', '', 'Год проведения', true);

insert into attributes(id, usage, name, description, title, mandatory)
values ('1987600a-7bb7-11ea-90dd-07ae0affee04', 'stage', 'Activity Stage 1', '', 'Этапы', false);

-- /Activity

insert into schema_attributes(schema_id, attribute_id)
values ('eff17fe4-5bb3-11ea-87ee-13ae5748cec9', '8f3a803c-5bb4-11ea-91dd-f7ae0ab89533');

insert into schema_attributes( schema_id, attribute_id)
values ('eff17fe4-5bb3-11ea-87ee-13ae5748cec9', 'a69bf79c-5bb4-11ea-87ee-03b7f37be8e6');

--insert into schema_attributes(schema_id,  attribute_id )
--values ('baa17fe4-5bb3-11ea-87ee-13ae5748cec1', 'b44bf79c-5bb4-11ea-87ee-03b7f37be8e8');

insert into activity (id, name, description, schema_content_id, course, faculty, year, status)
values('cabd6860-61f2-11ea-8cdc-87986b169274', 'Групповой проект ПИ 2 курс 2020', '', '8a00008a-7777-3344-108f-56dcccc70000', 2, 'PI', 2020, 'not_started');

insert into projects (id, name_rus, schema_content_id, type, leader_id, activity_id)
values('4879faa2-61f3-11ea-8cdc-0b15d71a20ca','Агрегатор образовательных онлайн курсов', '8a00008a-7777-3344-108f-56dcccc70001', 'research', '123e4567-e89b-12d3-a000-000000000000', 'cabd6860-61f2-11ea-8cdc-87986b169274');

insert into project_students (project_id, student_id)
values('4879faa2-61f3-11ea-8cdc-0b15d71a20ca', '7f5c1678-61f2-11ea-8cdc-dfb38868a744');

insert into activity_student(activity_id, student_id)
values('cabd6860-61f2-11ea-8cdc-87986b169274', '7f5c1678-61f2-11ea-8cdc-dfb38868a744');

insert into student_info (id, group_name, faculty, course, email) values ('c77688fe-66ba-11ea-bd52-7fdd60665167','БПИ151', 'Программная Инженерия', 4, 'randomemail@edu.hse.ru');
insert into student_info_students (student_id, student_info_id) values ('7f5c1678-61f2-11ea-8cdc-dfb38868a744', 'c77688fe-66ba-11ea-bd52-7fdd60665167');


-- Mentors

insert into users (id, name, schema_content_id, type, email, password, created_time) values ('22200000-e89b-12d3-a000-000000000001','Анатолий Германович Краснов','11112222-5baf-12ea-907c-edcba0000001', 'mentor', 'mentorthefirst@ya.ru', '0000', NOW());
insert into users (id, name, schema_content_id, type, email, password, created_time) values ('22200000-e89b-12d3-a000-000000000002','Виктория Александровна Посадская','11112222-5baf-12ea-907c-edcba0000002', 'mentor', 'mentorthesecond@ya.ru', '0000', NOW());
insert into users (id, name, schema_content_id, type, email, password, created_time) values ('22200000-e89b-12d3-a000-000000000003','Леонид Павлович Гудин','11112222-5baf-12ea-907c-edcba0000003', 'mentor', 'mentorthethird@ya.ru', '0000', NOW());

-- /Mentors


insert into activity (id, schema_content_id, name, description, course, faculty, year, status)
values (uuid_nil(), '8a00008a-7777-3344-108f-56dcccc70002', 'null activity', '', 0, 'other', 2000, 'not_started');


-- MailGroups

insert into mail_group(id, name, created_by) values ('aefffe12-4b43-11ea-87ee-28ae56690001', 'Менторы ПИ 2020', '123e4567-e89b-12d3-a000-000000000000');
insert into user_mail_group(mail_group_id, user_id) values('aefffe12-4b43-11ea-87ee-28ae56690001', '00000000-eeee-67aa-87ee-000099340000');
insert into user_mail_group(mail_group_id, user_id) values('aefffe12-4b43-11ea-87ee-28ae56690001', '00000000-eeee-67aa-87ee-000099340001');
-- /MailGroups



-- TO DELETE:

insert into schema_attributes(schema_id, attribute_id)  values ('aaa00ee1-5bb3-11ea-87ee-13ae57480000', 'aaabf79c-5bb4-11ea-87ee-03b7f37be111');

