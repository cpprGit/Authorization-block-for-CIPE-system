\c ccpr_main;


-- Questionnaire Schema

--
-- insert into attributes(id, name, usage, title, description, mandatory, search_name)
-- values('40000045-0000-0000-0000-55b8c3a00001', 'Questionnaire Name', 'short_text', 'Название', '',  true, 'name');
--
-- insert into attributes(id, name, usage, title, description, mandatory, search_name)
-- values('40000045-0000-0000-0000-55b8c3a00002', 'Questionnaire Fill By', 'short_text', 'Респондент', '', false, 'fillBy');

-- insert into attributes(id, name, usage, title, description, mandatory, search_name)
-- values('40000045-0000-0000-0000-55b8c3a00003', 'Questionnaire Is Filled By', 'radio', 'Статус', '', true, 'isFilled');
--
--
-- insert into variants(id, variant) values ('00000000-0000-0000-ebca-7864aaa00001', 'Заполнена');
-- insert into variants(id, variant) values ('00000000-0000-0000-ebca-7864aaa00002', 'Не заполнена');
--
-- insert into attribute_variants(attribute_id, variant_id) values('40000045-0000-0000-0000-55b8c3a00003', '00000000-0000-0000-ebca-7864aaa00001');
-- insert into attribute_variants(attribute_id, variant_id) values('40000045-0000-0000-0000-55b8c3a00003', '00000000-0000-0000-ebca-7864aaa00002');



-- insert into default_attributes(attribute_id, schema_type) values ('40000045-0000-0000-0000-55b8c3a00001', 'questionnaire');
-- insert into default_attributes(attribute_id, schema_type) values ('40000045-0000-0000-0000-55b8c3a00002', 'questionnaire');
-- insert into default_attributes(attribute_id, schema_type) values ('40000045-0000-0000-0000-55b8c3a00003', 'questionnaire');


-- /Questionnaire Schema
