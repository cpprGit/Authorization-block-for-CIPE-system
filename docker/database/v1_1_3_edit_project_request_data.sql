\c ccpr_main;


-- remove consultant
delete from schema_attributes
where attribute_id='30000033-0000-0000-0000-03b7f3a00006';

delete from default_attributes
where attribute_id='30000033-0000-0000-0000-03b7f3a00006';

delete from attributes
where id='30000033-0000-0000-0000-03b7f3a00006';


-- project leader mandatory=false
update attributes
set mandatory=false
where id='30000033-0000-0000-0000-03b7f3a00005';
