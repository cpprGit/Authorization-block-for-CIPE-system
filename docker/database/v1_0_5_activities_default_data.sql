\c ccpr_main;


-- Activity Schema


insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('40000044-0000-0000-0000-03b7f3a00001', 'Activity Name', 'short_text', 'Наименование активности', '', 'Введите полное название активности', false, 'name');

insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('40000044-0000-0000-0000-03b7f3a00002', 'Activity Description', 'long_text', 'Описание', '', 'Введите описание', false, 'description');

insert into attributes(id, name, usage, title, description, min, mandatory, search_name)
values('40000044-0000-0000-0000-03b7f3a00004', 'Activity Year', 'number', 'Год проведения', '', 2000, true, 'year');

insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('40000044-0000-0000-0000-03b7f3a00006', 'Activity Status', 'radio', 'Статус', '', '', true, 'status');

insert into attributes(id, name, usage, title, description, hint, mandatory, search_name)
values('40000044-0000-0000-0000-03b7f3a00007', 'Activity Stages', 'stage', 'Этапы', '', '', true, 'stages');


insert into variants(id, variant) values ('00000000-0000-0000-fbcf-8864aaa00001', 'Ожидает начала');
insert into variants(id, variant) values ('00000000-0000-0000-fbcf-8864aaa00004', 'Доступна подача заявок');
insert into variants(id, variant) values ('00000000-0000-0000-fbcf-8864aaa00002', 'Выполняется');
insert into variants(id, variant) values ('00000000-0000-0000-fbcf-8864aaa00003', 'Завершена');

insert into attribute_variants(attribute_id, variant_id) values('40000044-0000-0000-0000-03b7f3a00006', '00000000-0000-0000-fbcf-8864aaa00001');
insert into attribute_variants(attribute_id, variant_id) values('40000044-0000-0000-0000-03b7f3a00006', '00000000-0000-0000-fbcf-8864aaa00004');
insert into attribute_variants(attribute_id, variant_id) values('40000044-0000-0000-0000-03b7f3a00006', '00000000-0000-0000-fbcf-8864aaa00002');
insert into attribute_variants(attribute_id, variant_id) values('40000044-0000-0000-0000-03b7f3a00006', '00000000-0000-0000-fbcf-8864aaa00003');



insert into default_attributes(attribute_id, schema_type) values ('40000044-0000-0000-0000-03b7f3a00001', 'activity');
insert into default_attributes(attribute_id, schema_type) values ('40000044-0000-0000-0000-03b7f3a00002', 'activity');
insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00004', 'activity');
insert into default_attributes(attribute_id, schema_type) values ('40000044-0000-0000-0000-03b7f3a00004', 'activity');
insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00005', 'activity');
insert into default_attributes(attribute_id, schema_type) values ('40000044-0000-0000-0000-03b7f3a00006', 'activity');
insert into default_attributes(attribute_id, schema_type) values ('40000044-0000-0000-0000-03b7f3a00007', 'activity');


-- /Activity Schema
