\c ccpr_main;

-- insert into current_schemas(schema_id, type) values ('eff17fe4-5bb3-11ea-87ee-13ae5748cec9', 'user_profile');
insert into current_schemas(schema_id, type) values ('aaa00ee1-5bb3-11ea-87ee-13ae57480000', 'student_profile');
insert into current_schemas(schema_id, type) values ('00000000-0000-11ea-87ee-000057480000', 'student_registration');
insert into current_schemas(schema_id, type) values ('baa17fe4-5bb3-11ea-87ee-13ae5748cec1', 'student_profile_template');
insert into current_schemas(schema_id, type) values ('bb8000bc-effe-33aa-45fc-000000000001', 'org_profile');
insert into current_schemas(schema_id, type) values ('bb8000bc-effe-33aa-45fc-000000000001', 'org_profile_template');
insert into current_schemas(schema_id, type) values ('bb8000bc-effe-33aa-45fc-000000000001', 'questionnaire');
insert into current_schemas(schema_id, type) values ('bee22fe5-5bb3-11ea-87ee-13ae5748ce38', 'help');
insert into current_schemas(schema_id, type) values ('01000010-0280-27ea-87ee-000057480001', 'user_registration');
insert into current_schemas(schema_id, type) values ('01000010-0280-27ea-87ee-000057480002', 'user_profile');
insert into current_schemas(schema_id, type) values ('fcc17fe4-5bb3-11ea-87ee-13ae5748ce22', 'activity');
insert into current_schemas(schema_id, type) values ('bb0000aa-eeee-33aa-44ee-000000000000', 'project_request');
insert into current_schemas(schema_id, type) values ('bb0000aa-eeee-33aa-44ee-000000000001', 'project');
insert into current_schemas(schema_id, type) values ('baa17fe4-5bb3-11ea-87ee-13ae5748cec1', 'user_profile_template');

-- Managers
insert into schema_content (id, schema_id, content) values  ('11112222-5baf-12ea-907c-edcba0000004', '01000010-0280-27ea-87ee-000057480002', '{}');
insert into schema_content (id, schema_id, content) values  ('11112222-5baf-12ea-907c-edcba0000005', '01000010-0280-27ea-87ee-000057480002', '{}');

insert into users (id, name, schema_content_id, type, email, password, created_time) values ('22200000-e89b-12d3-a000-000000000004','Ченцова Элина Александровна','11112222-5baf-12ea-907c-edcba0000004', 'manager', 'echenczova@hse.ru', '0000', NOW());
insert into users (id, name, schema_content_id, type, email, password, created_time) values ('22200000-e89b-12d3-a000-000000000005','Воропаев Владислав Владиславович','11112222-5baf-12ea-907c-edcba0000005', 'representative', 'vp@ya.ru', '0000', NOW());
-- /Managers


