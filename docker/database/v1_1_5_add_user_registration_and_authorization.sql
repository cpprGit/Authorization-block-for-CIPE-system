\c ccpr_main;

alter type schema_type add value 'authorization';
alter type schema_type add value 'sign_in';

insert into schemas_dictionary(id, name, description, schema_type, button_name) values ('547ddb6c-1c3b-4949-80c4-b7000ff2c0c1', 'Авторизация пользователя', 'Введите данные, чтобы авторизоваться', 'authorization', 'Авторизоваться');
insert into current_schemas(schema_id, type) values ('547ddb6c-1c3b-4949-80c4-b7000ff2c0c1', 'authorization');

insert into default_attributes(attribute_id, schema_type) values ('00000000-fecb-b7b5-0000-03b7f3a00001', 'authorization');
insert into default_attributes(attribute_id, schema_type) values ('00000000-0000-0000-0000-03b7f3a00001', 'authorization');
