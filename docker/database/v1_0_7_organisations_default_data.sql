\c ccpr_main;


-- Organisation Schema

insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('bb8000bc-effe-33aa-45fc-000000000001', 'Стандартная форма организации', '', 'org_profile', 'Создать организацию');

insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('60000054-0000-4321-0000-03b7f3a00000', 'Additional Info', 'long_text', 'Дополнительная информация', '', '', false, 'additionalInfo');

insert into schema_attributes(schema_id, attribute_id) values ('bb8000bc-effe-33aa-45fc-000000000001', '60000054-0000-4321-0000-03b7f3a00000');


-- Default Attributes
insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('60000054-0000-4321-0000-03b7f3a00001', 'Organisation Name', 'short_text', 'Наименование организации', '', 'Введите полное название организации', true, 'orgName');

insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('60000054-0000-4321-0000-03b7f3a00002', 'Hse department?', 'switch', 'Департамент НИУ ВШЭ?', '', '', true, 'hseDepartment');

insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('60000054-0000-4321-0000-03b7f3a00003', 'Organisation Created by', 'short_text', 'Создатель', '', '', false, 'createdBy');

insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('60000054-0000-4321-0000-03b7f3a00004', 'Organisation Last Modified Time', 'short_text', 'Дата последнего изменения', '', '', false, 'lastModifiedTime');

insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('60000054-0000-4321-0000-03b7f3a00005', 'Organisation Last Modified By', 'short_text', 'Последние изменения совершены', '', '', false, 'lastModifiedBy');

insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('60000054-0000-4321-0000-03b7f3a00006', 'Organisation Type', 'radio', 'Тип организации', '', '', true, 'orgType');

insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('60000054-0000-4321-0000-03b7f3a00007', 'Organisation Head', 'organisation', 'Верховная организация', '', '', false, 'parent');


--
-- insert into variants(id, variant) values ('00fce000-0000-0000-bbca-7864aaa00001', 'Да');
-- insert into variants(id, variant) values ('00fce000-0000-0000-bbca-7864aaa00002', 'Нет');
--
-- insert into attribute_variants(attribute_id, variant_id) values('60000054-0000-4321-0000-03b7f3a00002', '00fce000-0000-0000-bbca-7864aaa00001');
-- insert into attribute_variants(attribute_id, variant_id) values('60000054-0000-4321-0000-03b7f3a00002', '00fce000-0000-0000-bbca-7864aaa00002');
--

insert into variants(id, variant) values ('00fce000-0000-0000-bbca-7864aaa00003', 'IT');
insert into variants(id, variant) values ('00fce000-0000-0000-bbca-7864aaa00004', 'Banking');
insert into variants(id, variant) values ('00fce000-0000-0000-bbca-7864aaa00005', 'Consulting');

insert into attribute_variants(attribute_id, variant_id) values('60000054-0000-4321-0000-03b7f3a00006', '00fce000-0000-0000-bbca-7864aaa00003');
insert into attribute_variants(attribute_id, variant_id) values('60000054-0000-4321-0000-03b7f3a00006', '00fce000-0000-0000-bbca-7864aaa00004');
insert into attribute_variants(attribute_id, variant_id) values('60000054-0000-4321-0000-03b7f3a00006', '00fce000-0000-0000-bbca-7864aaa00005');


insert into default_attributes(attribute_id, schema_type) values ('60000054-0000-4321-0000-03b7f3a00001', 'org_profile');
insert into default_attributes(attribute_id, schema_type) values ('60000054-0000-4321-0000-03b7f3a00002', 'org_profile');
-- insert into default_attributes(attribute_id, schema_type) values ('60000054-0000-4321-0000-03b7f3a00003', 'org_profile');
-- insert into default_attributes(attribute_id, schema_type) values ('60000054-0000-4321-0000-03b7f3a00004', 'org_profile');
-- insert into default_attributes(attribute_id, schema_type) values ('60000054-0000-4321-0000-03b7f3a00005', 'org_profile');
-- insert into default_attributes(attribute_id, schema_type) values ('60000054-0000-4321-0000-03b7f3a00006', 'org_profile');
insert into default_attributes(attribute_id, schema_type) values ('60000054-0000-4321-0000-03b7f3a00007', 'org_profile');



-- /Organisation Schema


-- Organisations

insert into schema_content (id, schema_id, content) values ('99fa608a-6aeb-33ae-907c-27d668cb822a', 'bb8000bc-effe-33aa-45fc-000000000001', '{"Additional Info":""}');

insert into organisations(id, name, schema_content_id, is_hse_department, last_modified_by, created_by, type, status)
values('00fa600a-9aeb-33ae-907c-0d668aa8033a', 'Национальный исследовательский университет «Высшая школа экономики»', '99fa608a-6aeb-33ae-907c-27d668cb822a', true, '123e4567-e89b-12d3-a000-000000000000', '123e4567-e89b-12d3-a000-000000000000','other', 'approved');

insert into organisation_family(parent_id, child_id) values (null, '00fa600a-9aeb-33ae-907c-0d668aa8033a');
-- /Organisations

