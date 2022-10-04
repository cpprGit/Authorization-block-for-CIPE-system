\c ccpr_main;

-- Mentor



-- Email
insert into attributes(id, usage, name, description, title, placeholder, hint, mandatory, search_name)
values ('00000000-fecb-b7b5-0000-03b7f3a00001', 'email', 'User Email', '',  'Электронная почта', 'youremail@edu.hse.ru', '', true, 'email');

-- Password - 00000000-0000-0000-0000-03b7f3a00001

-- Name
insert into attributes(id, usage, name, description, title,  placeholder,  hint, mandatory, search_name)
values ('00000000-fecb-b7b5-0000-03b7f3a00002', 'short_text', 'User Full Name', '',  'ФИО', 'Иванов Иван Иванович', 'Введите полное имя.', true, 'name');


-- Organisation
insert into attributes(id, usage, name, description, title,  placeholder,  hint, mandatory, search_name)
values ('00000000-fecb-b7b5-0000-03b7f3a00003', 'organisation', 'User Organisation', '',  'Организация', 'Google', 'Выберите организацию', false, 'organisation');


-- HSE Department
insert into attributes(id, usage, name, description, title,  placeholder,  hint, mandatory, search_name)
values ('00000000-fecb-b7b5-0000-03b7f3a00004', 'organisation', 'User HSE Department', '',  'Департамент НИУ ВШЭ', 'ФКН', 'Выберите департамент НИУ ВШЭ', false, 'hseDepartment');


-- User Role
insert into attributes(id, usage, name, description, title, hint, mandatory, search_name)
values ('00000000-fecb-b7b5-0000-03b7f3a00005', 'radio', 'User Role', '',  'Роль пользователя в системе', 'Выберите роль',  true, 'role');

insert into variants(id, variant) VALUES ('00004774-0000-0000-bafb-2115aca00001', 'Ментор');
insert into variants(id, variant) VALUES ('00004774-0000-0000-bafb-2115aca00002', 'Контактное лицо');
insert into variants(id, variant) VALUES ('00004774-0000-0000-bafb-2115aca00003', 'Менеджер');


insert into attribute_variants(attribute_id, variant_id) VALUES ('00000000-fecb-b7b5-0000-03b7f3a00005', '00004774-0000-0000-bafb-2115aca00001');
insert into attribute_variants(attribute_id, variant_id) VALUES ('00000000-fecb-b7b5-0000-03b7f3a00005', '00004774-0000-0000-bafb-2115aca00002');
insert into attribute_variants(attribute_id, variant_id) VALUES ('00000000-fecb-b7b5-0000-03b7f3a00005', '00004774-0000-0000-bafb-2115aca00003');


-- insert into schema_attributes(schema_id, attribute_id)  values ('01000010-0280-27ea-87ee-000057480001', '00000000-fecb-b7b5-0000-03b7f3a00001');
-- insert into schema_attributes(schema_id, attribute_id)  values ('01000010-0280-27ea-87ee-000057480001', '00000000-0000-0000-0000-03b7f3a00001');
-- insert into schema_attributes(schema_id, attribute_id)  values ('01000010-0280-27ea-87ee-000057480001', '00000000-fecb-b7b5-0000-03b7f3a00002');
-- insert into schema_attributes(schema_id, attribute_id)  values ('01000010-0280-27ea-87ee-000057480001', '00000000-fecb-b7b5-0000-03b7f3a00003');
-- insert into schema_attributes(schema_id, attribute_id)  values ('01000010-0280-27ea-87ee-000057480001', '00000000-fecb-b7b5-0000-03b7f3a00004');
-- insert into schema_attributes(schema_id, attribute_id)  values ('01000010-0280-27ea-87ee-000057480001', '00000000-fecb-b7b5-0000-03b7f3a00005');


-- insert into schema_attributes(schema_id, attribute_id)  values ('01000010-0280-27ea-87ee-000057480002', '00000000-fecb-b7b5-0000-03b7f3a00001');
-- insert into schema_attributes(schema_id, attribute_id)  values ('01000010-0280-27ea-87ee-000057480002', '00000000-0000-0000-0000-03b7f3a00001');
-- insert into schema_attributes(schema_id, attribute_id)  values ('01000010-0280-27ea-87ee-000057480002', '00000000-fecb-b7b5-0000-03b7f3a00002');
-- insert into schema_attributes(schema_id, attribute_id)  values ('01000010-0280-27ea-87ee-000057480002', '00000000-fecb-b7b5-0000-03b7f3a00003');
-- insert into schema_attributes(schema_id, attribute_id)  values ('01000010-0280-27ea-87ee-000057480002', '00000000-fecb-b7b5-0000-03b7f3a00004');
-- insert into schema_attributes(schema_id, attribute_id)  values ('01000010-0280-27ea-87ee-000057480002', '00000000-fecb-b7b5-0000-03b7f3a00005');


insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00001', 'mentor_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00001', 'mentor_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00002', 'mentor_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00003', 'mentor_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00004', 'mentor_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00005', 'mentor_registration');


insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00001', 'user_profile');
insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00001', 'user_profile');
insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00002', 'user_profile');
insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00003', 'user_profile');
insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00004', 'user_profile');
insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00005', 'user_profile');


insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00001', 'user_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00001', 'user_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00002', 'user_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00003', 'user_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00004', 'user_registration');
insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00005', 'user_registration');

-- /Mentor
