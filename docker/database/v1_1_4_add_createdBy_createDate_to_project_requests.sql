\c ccpr_main;

ALTER TABLE project_requests ADD created_by UUID;
ALTER TABLE project_requests ADD create_date TIMESTAMP NOT NULL DEFAULT now();

