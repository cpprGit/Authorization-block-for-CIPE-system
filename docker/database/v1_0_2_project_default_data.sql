\c ccpr_main;


-- Project Schema

insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('bb0000aa-eeee-33aa-44ee-000000000000', 'Создание проекта', '', 'project_request', 'Создать проект');
insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('bb0000aa-eeee-33aa-44ee-000000000001', 'Проект', '', 'project', 'Создать проект');

insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('30000033-0000-0000-0000-03b7f3a00001', 'Project Name Rus', 'short_text', 'Название проекта на русском языке', '', 'Введите полное название проекта на русском языке', true, 'projectNameRus');

insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('30000033-0000-0000-0000-03b7f3a00002', 'Project Name Eng', 'short_text', 'Название проекта на английском языке', '', 'Введите полное название проекта на английском языке', true, 'projectNameEng');

insert into attributes(id, name, usage, title, description, mandatory, search_name)
values('30000033-0000-0000-0000-03b7f3a00003', 'Individual or Group type', 'radio', 'Тип проекта', '', true, 'projectIndividuality');

insert into attributes(id, name, usage, title, description, mandatory, search_name)
values('30000033-0000-0000-0000-03b7f3a00004', 'Project Type', 'radio', 'Исследовательская или Программная работа', '', true, 'projectType');

insert into attributes(id, name, usage, title, description, hint,  mandatory, search_name)
values('30000033-0000-0000-0000-03b7f3a00005', 'Project Leader', 'mentor', 'Руководитель проекта', 'Введите ФИО руководителя проекта из НИУ ВШЭ', 'Введите ФИО руководителя проекта', true, 'mentor');

insert into attributes(id, name, usage, title, description, hint,  mandatory, search_name)
values('30000033-0000-0000-0000-03b7f3a00006', 'Project Consultant', 'mentor', 'Консультант проекта', 'Введите ФИО консультанта проекта', 'Введите ФИО консультанта проекта', false, 'consultant');

insert into attributes(id, name, usage, title, hint, mandatory, search_name)
values('30000033-0000-0000-0000-03b7f3a00007', 'Activity', 'activity', 'Активность', 'Введите активность', false, 'activity');


insert into attributes(id, name, usage, title, description, mandatory, search_name)
values('30000033-0000-0000-0000-03b7f3a00008', 'PI Courses', 'checkbox', 'Программная Инженерия', '',  false, 'piCourses');


insert into attributes(id, name, usage, title, description, mandatory, search_name)
values('30000033-0000-0000-0000-03b7f3a00009', 'PMI Courses', 'checkbox', 'Прикладная Математика и Информатика', '',  false, 'pmiCourses');


insert into attributes(id, name, usage, title, description, mandatory, search_name)
values('30000033-0000-0000-0000-03b7f3a00010', 'PAD Courses', 'checkbox', 'Прикладной Анализ Данных', '',  false, 'padCourses');

insert into attributes(id, name, usage, title, description, mandatory, search_name)
values('30000033-0000-0000-0000-03b7f3a00011', 'Project Status', 'checkbox', 'Статус', '',  false, 'status');



insert into variants(id, variant) VALUES ('00004444-0000-0000-bbbb-6675aaa00001', 'Групповой');
insert into variants(id, variant) VALUES ('00004444-0000-0000-bbbb-6675aaa00002', 'Индивидуальный');

insert into variants(id, variant) VALUES ('00004444-0000-0000-bbbb-6675aaa00003', 'Исследовательский');
insert into variants(id, variant) VALUES ('00004444-0000-0000-bbbb-6675aaa00004', 'Программный');

insert into variants(id, variant) VALUES ('00004444-0000-0000-bbbb-6675aaa00005', '1 курс');
insert into variants(id, variant) VALUES ('00004444-0000-0000-bbbb-6675aaa00006', '2 курс');
insert into variants(id, variant) VALUES ('00004444-0000-0000-bbbb-6675aaa00007', '3 курс');
insert into variants(id, variant) VALUES ('00004444-0000-0000-bbbb-6675aaa00008', '4 курс');


insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00003', '00004444-0000-0000-bbbb-6675aaa00001');
insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00003', '00004444-0000-0000-bbbb-6675aaa00002');

insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00004', '00004444-0000-0000-bbbb-6675aaa00003');
insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00004', '00004444-0000-0000-bbbb-6675aaa00004');

insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00008', '00004444-0000-0000-bbbb-6675aaa00005');
insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00008', '00004444-0000-0000-bbbb-6675aaa00006');
insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00008', '00004444-0000-0000-bbbb-6675aaa00007');
insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00008', '00004444-0000-0000-bbbb-6675aaa00008');

insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00009', '00004444-0000-0000-bbbb-6675aaa00005');
insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00009', '00004444-0000-0000-bbbb-6675aaa00006');
insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00009', '00004444-0000-0000-bbbb-6675aaa00007');
insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00009', '00004444-0000-0000-bbbb-6675aaa00008');

insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00010', '00004444-0000-0000-bbbb-6675aaa00005');
insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00010', '00004444-0000-0000-bbbb-6675aaa00006');
insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00010', '00004444-0000-0000-bbbb-6675aaa00007');
insert into attribute_variants(attribute_id, variant_id) VALUES ('30000033-0000-0000-0000-03b7f3a00010', '00004444-0000-0000-bbbb-6675aaa00008');


insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000000', '30000033-0000-0000-0000-03b7f3a00001');
insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000000', '30000033-0000-0000-0000-03b7f3a00002');
insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000000', '30000033-0000-0000-0000-03b7f3a00003');
insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000000', '30000033-0000-0000-0000-03b7f3a00004');
insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000000', '30000033-0000-0000-0000-03b7f3a00005');
insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000000', '30000033-0000-0000-0000-03b7f3a00006');
insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000000', '30000033-0000-0000-0000-03b7f3a00007');
--insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000000', '30000033-0000-0000-0000-03b7f3a00008');
--insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000000', '30000033-0000-0000-0000-03b7f3a00009');
--insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000000', '30000033-0000-0000-0000-03b7f3a00010');
-- insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000000', '30000033-0000-0000-0000-03b7f3a00011');



insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000001', '30000033-0000-0000-0000-03b7f3a00001');
insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000001', '30000033-0000-0000-0000-03b7f3a00002');
insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000001', '30000033-0000-0000-0000-03b7f3a00003');
insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000001', '30000033-0000-0000-0000-03b7f3a00004');
insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000001', '30000033-0000-0000-0000-03b7f3a00005');
insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000001', '30000033-0000-0000-0000-03b7f3a00006');
insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000001', '30000033-0000-0000-0000-03b7f3a00007');
-- insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000001', '30000033-0000-0000-0000-03b7f3a00008');
-- insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000001', '30000033-0000-0000-0000-03b7f3a00009');
-- insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000001', '30000033-0000-0000-0000-03b7f3a00010');
-- insert into schema_attributes(schema_id, attribute_id) values ('bb0000aa-eeee-33aa-44ee-000000000001', '30000033-0000-0000-0000-03b7f3a00011');


insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00001', 'project_request');
insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00002', 'project_request');
insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00003', 'project_request');
insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00004', 'project_request');
insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00005', 'project_request');
insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00006', 'project_request');
-- insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00008', 'project_request');
-- insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00009', 'project_request');
-- insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00010', 'project_request');



insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00001', 'project');
insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00002', 'project');
insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00003', 'project');
insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00004', 'project');
insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00005', 'project');
insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00006', 'project');
insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00007', 'project');
-- insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00008', 'project');
-- insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00009', 'project');
-- insert into default_attributes(attribute_id, schema_type) values ('30000033-0000-0000-0000-03b7f3a00010', 'project');

-- /Project Schema
