CREATE USER backend WITH ENCRYPTED PASSWORD 'QwEr1234!';

-- CREATE DATABASE ccpr_grafana;
-- CREATE USER grafana WITH ENCRYPTED PASSWORD 'QwEr1234!';
--
-- CREATE DATABASE ccpr_smtp;
-- CREATE USER smtp WITH ENCRYPTED PASSWORD 'QwEr1234!';

\c ccpr_main;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE SCHEMA_TYPE AS ENUM (
    'user_registration',
    'user_profile_template',
    'user_profile',

    'student_registration',
    'student_profile',
    'student_profile_template',

    'mentor_registration',
    'mentor_profile',

    'org_profile_template',
    'org_profile',

    'project',
    'project_request',

    'activity',

    'questionnaire',
    'help'
    );


CREATE TYPE USER_TYPE AS ENUM (
    'administrator',
    'supervisor',
    'manager',
    'mentor',
    'representative',
    'student'
    );


CREATE TYPE STUDENT_STATUS AS ENUM (
    'active'   ,
    'graduated',
    'enrolling',
    'inactive'
    );


CREATE TYPE ORGANISATION_TYPE AS ENUM (
    'bank'      ,
    'IT'        ,
    'other'
    );

CREATE TYPE ORGANISATION_STATUS AS ENUM (
    'validating',
    'invalid'   ,
    'approved'  ,
    'rejected'
    );


CREATE TYPE PROJECT_REQUEST_STATUS AS ENUM (
    'pending',
    'declined',
    'accepted'
    );

CREATE TYPE PROJECT_TYPE AS ENUM (
    'research',
    'technical',
    'other'
    );

CREATE TYPE FACULTY_TYPE AS ENUM (
    'PI',
    'PMI',
    'PAD',
    'other'
    );

CREATE TYPE ACTIVITY_STATUS AS ENUM (
    'not_started',
    'apply_open',
    'started',
    'finished'
    );

CREATE TYPE NOTIFICATION_TYPE AS ENUM (
    'email',
    'profile',
    'all'
    );

CREATE TYPE NOTIFICATION_TARGET_TYPE AS ENUM (
    'user',
    'organisation',
    'activity',
    'project',
    'project-request',
    'post'
    );

CREATE TYPE MG_HISTORY_TARGET_TYPE AS ENUM (
    'user',
    'questionnaire'
    );

CREATE TYPE BLOCKED_STATUS AS ENUM (
    'active',
    'blocked'
    );


CREATE TABLE IF NOT EXISTS attributes (
    id              UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    name            TEXT NOT NULL,
    usage           TEXT NOT NULL,
    title           TEXT NOT NULL,

    step            INTEGER,
    description     TEXT,
    placeholder     TEXT,
    min             INTEGER,
    max             INTEGER,
    hint            TEXT,
    mandatory       BOOLEAN NOT NULL DEFAULT (FALSE),
    value_default   TEXT DEFAULT (NULL),
    search_name     TEXT,

    PRIMARY KEY (id),
    UNIQUE      (name)
);


CREATE TABLE IF NOT EXISTS schemas_dictionary (
    id          UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    created_by  UUID,

    name        TEXT NOT NUll,
    description TEXT,
    schema_type SCHEMA_TYPE,
    button_name TEXT NOT NULL,
    archived    BOOLEAN DEFAULT FALSE,

    PRIMARY KEY (id),
    UNIQUE      (name)
);


CREATE TABLE IF NOT EXISTS schema_attributes (
    id              UUID DEFAULT uuid_generate_v1mc(),

    schema_id       UUID NOT NULL,
    attribute_id    UUID NOT NULL,

    PRIMARY KEY     (id),
    UNIQUE          (schema_id, attribute_id)
);


CREATE TABLE IF NOT EXISTS current_schemas (
    id                  UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    schema_id           UUID NOT NULL,
    type                SCHEMA_TYPE NOT NULL,

    PRIMARY KEY (id),
    UNIQUE      (type)
);


CREATE TABLE IF NOT EXISTS schemas_parents (
    id              UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    schema_id       UUID NOT NULL,
    parent_id       UUID NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS schema_content (
    id             UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    schema_id      UUID NOT NULL,
    content        TEXT NOT NULL,

    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS users (
    id                 UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    name               TEXT NOT NULL,
    email              TEXT NOT NULL,
    password           TEXT NOT NULL,
    type               USER_TYPE NOT NULL,
    status             BLOCKED_STATUS NOT NULL DEFAULT 'active',

    schema_content_id  UUID NOT NULL,

    created_by         UUID,
    created_time       TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (id),
    UNIQUE      (schema_content_id, email),
    UNIQUE      (email)
);


CREATE TABLE IF NOT EXISTS organisations (
    id                 UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    name               TEXT NOT NULL,

    schema_content_id  UUID NOT NULL,

    is_hse_department  BOOLEAN NOT NULL DEFAULT false,

    last_modified_by   UUID NOT NULL,
    created_by         UUID NOT NULL,

    type               ORGANISATION_TYPE NOT NULL,
    status             ORGANISATION_STATUS NOT NULL DEFAULT 'validating',
    blocked_status     BLOCKED_STATUS NOT NULL DEFAULT 'active',

    created_time       TIMESTAMP NOT NULL DEFAULT NOW(),
    last_modified_time TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (id),
    UNIQUE      (schema_content_id),
    UNIQUE      (name)
);


CREATE TABLE IF NOT EXISTS projects (
    id                  UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    schema_content_id   UUID NOT NULL,
    leader_id           UUID NOT NULL,
    consultant_id       UUID,


    name_rus            TEXT NOT NULL,
    name_eng            TEXT,
    type                PROJECT_TYPE,
    is_group_project    BOOLEAN DEFAULT FALSE,
    activity_id         UUID NOT NULL DEFAULT uuid_nil(),

    max_students        INT,


    pi                  TEXT,
    pmi                 TEXT,
    pad                 TEXT,

    PRIMARY KEY (id),
    UNIQUE (name_rus),
    UNIQUE (name_eng)
);

CREATE TABLE IF NOT EXISTS project_requests (
    id                  UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    schema_content_id   UUID NOT NULL,
    leader_id           UUID NOT NULL,
    consultant_id       UUID,

    name_rus            TEXT NOT NULL,
    name_eng            TEXT,
    status              PROJECT_REQUEST_STATUS,
    type                PROJECT_TYPE,
    is_group_project    BOOLEAN DEFAULT FALSE,

    pi                  TEXT,
    pmi                 TEXT,
    pad                 TEXT,


    PRIMARY KEY (id),
    UNIQUE (name_rus),
    UNIQUE (name_eng)
);


CREATE TABLE IF NOT EXISTS questionaires (
    id                  UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    schema_content_id   UUID NOT NULL,

    name                TEXT NOT NULL,
    is_filled           BOOLEAN DEFAULT FALSE,
    fill_by             UUID,


    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS activity (
    id                  UUID                NOT NULL DEFAULT uuid_generate_v1mc(),
    schema_content_id   UUID                NOT NULL,

    name                TEXT                NOT NULL,
    course              INTEGER             NOT NULL,
    faculty             FACULTY_TYPE        NOT NULL,
    year                INTEGER             NOT NULL,
    status              ACTIVITY_STATUS     NOT NULL DEFAULT 'not_started',

    description         TEXT,


    PRIMARY KEY (id),
    UNIQUE (name)
);


CREATE TABLE IF NOT EXISTS files (
    id                  UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    name                TEXT NOT NULL,
    mimetype            TEXT NOT NULL,
    location            TEXT,
    profile_id          UUID,
    task_id             UUID,
    uploader_id         UUID,

    PRIMARY KEY (id),
    UNIQUE (task_id, uploader_id)
);


CREATE TABLE IF NOT EXISTS posts (
    id                  UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    created_by          UUID NOT NULL,
    profile_id          UUID NOT NULL,
    message             TEXT NOT NULL,
    date_created        TIMESTAMP NOT NULL DEFAULT now(),
    file_id             UUID,
    text                TEXT,

    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS notifications (
    id                  UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    for_user            UUID NOT NULL,

    created_by          UUID NOT NULL,
    action              TEXT NOT NULL,
    text                TEXT,
    target_id           UUID,
    target_name         TEXT,
    target_type         NOTIFICATION_TARGET_TYPE,
    date                TIMESTAMP NOT NULL DEFAULT NOW(),

    type                NOTIFICATION_TYPE,
    is_viewed           BOOLEAN NOT NULL DEFAULT FALSE,

    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS student_grades (
    id                  UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    student_id          UUID NOT NULL,
    stage_id            UUID NOT NULL,
    activity_id         UUID NOT NULL,
    project_id          UUID NOT NULL,
    mentor_grade        INTEGER,
    manager_grade       INTEGER,

    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS stage (
    id                  UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    name                TEXT NOT NULL,
    description         TEXT,
    stage_number        INTEGER,
    grade_coefficient   FLOAT NOT NULL ,
    mentor_grade_final  BOOLEAN NOT NULL DEFAULT true,
    start_date          TIMESTAMP NOT NULL,
    end_date            TIMESTAMP NOT NULL,

    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS task (
    id                  UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    name                TEXT NOT NULL,
    description         TEXT,
    is_uploadable       BOOLEAN DEFAULT FALSE,

    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS settings (
    id                               UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    notification_type                TEXT NOT NULL,
    max_students_per_mentor          NOTIFICATION_TYPE,

    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS student_info(
    id              UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    group_name      TEXT NOT NULL,
    faculty         TEXT,
    course          INTEGER NOT NULL,
    email           TEXT NOT NULL,
    status          STUDENT_STATUS NOT NULL DEFAULT ('active'),

    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS project_students (
    id         UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    student_id UUID NOT NULL,
    project_id UUID NOT NULL,

    PRIMARY KEY (id),
    UNIQUE      (student_id, project_id)
);


CREATE TABLE IF NOT EXISTS student_project_request (
    id         UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    student_id UUID NOT NULL,
    project_id UUID NOT NULL,

    PRIMARY KEY (id),
    UNIQUE      (student_id, project_id)
);

CREATE TABLE IF NOT EXISTS activity_student (
    id         UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    activity_id  UUID NOT NULL,
    student_id UUID NOT NULL,

    PRIMARY KEY (id),
    UNIQUE (activity_id, student_id)
);



CREATE TABLE IF NOT EXISTS organisation_family (
    id                      UUID NOT NULL DEFAULT uuid_generate_v1mc(),

    parent_id               UUID,
    child_id                UUID NOT NULL,

    PRIMARY KEY (id),
    UNIQUE (parent_id, child_id),
    UNIQUE (child_id)
);


CREATE TABLE IF NOT EXISTS student_info_students (
    id                      UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    student_id              UUID NOT NULL,
    student_info_id         UUID NOT NULL,

    PRIMARY KEY (id),
    UNIQUE (student_id, student_info_id)
);

CREATE TABLE IF NOT EXISTS organisation_user (
   id                      UUID NOT NULL DEFAULT uuid_generate_v1mc(),

   organisation_id         UUID NOT NULL,
   user_id                 UUID NOT NULL,

   PRIMARY KEY (id),
   UNIQUE (organisation_id, user_id),
   UNIQUE (user_id)
);



CREATE TABLE IF NOT EXISTS department_user (
    id                               UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    organisation_id                  UUID NOT NULL,
    user_id                          UUID NOT NULL,

    PRIMARY KEY (id),
    UNIQUE (organisation_id, user_id),
    UNIQUE (user_id)
);

--
-- CREATE TABLE IF NOT EXISTS hse_department_mentor (
--     id                               UUID NOT NULL DEFAULT uuid_generate_v1mc(),
--     organisation_id                  UUID NOT NULL,
--     mentor_id                        UUID NOT NULL,
--
--     PRIMARY KEY (id),
--     UNIQUE (organisation_id, mentor_id),
--     UNIQUE (mentor_id)
-- );

CREATE TABLE IF NOT EXISTS activity_mentor (
    id                               UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    activity_id                      UUID NOT NULL,
    mentor_id                        UUID NOT NULL,

    PRIMARY KEY (id),
    UNIQUE (activity_id, mentor_id)
);



CREATE TABLE IF NOT EXISTS activity_stage (
    id                               UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    activity_id                      UUID NOT NULL,
    stage_id                         UUID NOT NULL,

    PRIMARY KEY (id),
    UNIQUE (activity_id, stage_id)
);

CREATE TABLE IF NOT EXISTS task_stage (
    id                               UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    task_id                          UUID NOT NULL,
    stage_id                         UUID NOT NULL,

    PRIMARY KEY (id),
    UNIQUE (task_id, stage_id)
);

CREATE TABLE IF NOT EXISTS user_settings (
    id                               UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    user_id                          UUID NOT NULL,
    settings_id                      UUID NOT NULL,

    PRIMARY KEY (id),
    UNIQUE (user_id, settings_id)
);


CREATE TABLE IF NOT EXISTS validators (
    id              UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    validator       TEXT NOT NULL,
    message         TEXT NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS attribute_validators (
    id              UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    attribute_id    UUID NOT NULL,
    validator_id    UUID NOT NULL,

    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS variants (
    id              UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    variant         TEXT NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS attribute_variants (
    id              UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    attribute_id    UUID NOT NULL,
    variant_id      UUID NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS default_attributes(
    id              UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    attribute_id    UUID NOT NULL,
    schema_type     SCHEMA_TYPE NOT NULL,

    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS mail_group(
    id              UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    name            TEXT NOT NULL,
    created_by      UUID NOT NULL,


    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS mail_group_history(
     id              UUID NOT NULL DEFAULT uuid_generate_v1mc(),
     mail_group      UUID NOT NULL,

     message         TEXT NOT NULL,
     target_id       UUID,
     target_name     TEXT,
     target_type     TEXT,
     date            TIMESTAMP NOT NULL DEFAULT NOW(),
     created_by      UUID,

     PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS user_mail_group(
    id              UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    user_id         UUID NOT NULL,
    mail_group_id   UUID NOT NULL,

    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS user_complaints(
    id              UUID NOT NULL DEFAULT uuid_generate_v1mc(),
    created_by      UUID NOT NULL,

    profile_id      UUID NOT NULL,
    profile_name    TEXT NOT NULL,
    profile_type    TEXT NOT NULL,
    complaint       TEXT NOT NULL,
    is_viewed       BOOLEAN NOT NULL DEFAULT false,

    PRIMARY KEY (id)
);


ALTER TABLE mail_group
    ADD CONSTRAINT created_by_id_fkey FOREIGN KEY (created_by) REFERENCES users (id);


ALTER TABLE notifications
    ADD CONSTRAINT created_by_id_fkey FOREIGN KEY (created_by) REFERENCES users (id),
    ADD CONSTRAINT for_user_id_fkey FOREIGN KEY (for_user) REFERENCES users (id);

ALTER TABLE user_mail_group
    ADD CONSTRAINT userid_fkey FOREIGN KEY (user_id) REFERENCES users (id),
    ADD CONSTRAINT mailgroupid_fkey FOREIGN KEY (mail_group_id) REFERENCES mail_group (id);

ALTER TABLE default_attributes
    ADD CONSTRAINT attribute_fkey FOREIGN KEY (attribute_id) REFERENCES attributes (id);

ALTER TABLE files
    ADD CONSTRAINT profileid_fkey FOREIGN KEY (profile_id) REFERENCES schema_content (id);


ALTER TABLE attribute_variants
    ADD CONSTRAINT attribute_fkey FOREIGN KEY (attribute_id) REFERENCES attributes (id),
    ADD CONSTRAINT validator_fkey FOREIGN KEY (variant_id) REFERENCES variants (id);

ALTER TABLE attribute_validators
    ADD CONSTRAINT attribute_fkey FOREIGN KEY (attribute_id) REFERENCES attributes (id),
    ADD CONSTRAINT validator_fkey FOREIGN KEY (validator_id) REFERENCES validators (id);

ALTER TABLE schemas_dictionary
    ADD CONSTRAINT createdby_schema_fkey FOREIGN KEY (created_by) REFERENCES users (id);

ALTER TABLE schema_attributes
    ADD CONSTRAINT sch_schema_fkey FOREIGN KEY (schema_id) REFERENCES schemas_dictionary (id),
    ADD CONSTRAINT att_schema_fkey FOREIGN KEY (attribute_id) REFERENCES attributes (id);

ALTER TABLE current_schemas
    ADD CONSTRAINT curr_schema_fkey FOREIGN KEY (schema_id) REFERENCES schemas_dictionary (id);


ALTER TABLE schemas_parents
    ADD CONSTRAINT sch_schema_fkey FOREIGN KEY (schema_id) REFERENCES schemas_dictionary (id),
    ADD CONSTRAINT par_schema_fkey FOREIGN KEY (parent_id) REFERENCES schemas_dictionary (id);


ALTER TABLE schema_content
    ADD CONSTRAINT sch_schema_fkey FOREIGN KEY (schema_id) REFERENCES schemas_dictionary (id);

ALTER TABLE users
    ADD CONSTRAINT schema_content_fkey FOREIGN KEY (schema_content_id) REFERENCES schema_content (id);

ALTER TABLE activity
    ADD CONSTRAINT schema_content_fkey FOREIGN KEY (schema_content_id) REFERENCES schema_content (id);


ALTER TABLE organisations
    ADD CONSTRAINT schema_content_fkey FOREIGN KEY (schema_content_id) REFERENCES schema_content (id),
    ADD CONSTRAINT lastmodified_fkey FOREIGN KEY (last_modified_by) REFERENCES users (id),
    ADD CONSTRAINT createdby_fkey FOREIGN KEY (created_by) REFERENCES users (id);

ALTER TABLE projects
    ADD CONSTRAINT schema_content_fkey FOREIGN KEY (schema_content_id) REFERENCES schema_content (id),
    ADD CONSTRAINT leader_fkey FOREIGN KEY (leader_id) REFERENCES users (id),
    ADD CONSTRAINT consultant_fkey FOREIGN KEY (consultant_id) REFERENCES users (id),
    ADD CONSTRAINT activity_fkey FOREIGN KEY (activity_id) REFERENCES activity (id);

ALTER TABLE project_requests
    ADD CONSTRAINT schema_content_fkey FOREIGN KEY (schema_content_id) REFERENCES schema_content (id),
    ADD CONSTRAINT leader_fkey FOREIGN KEY (leader_id) REFERENCES users (id),
    ADD CONSTRAINT consultant_fkey FOREIGN KEY (consultant_id) REFERENCES users (id);

ALTER TABLE questionaires
    ADD CONSTRAINT schema_content_fkey FOREIGN KEY (schema_content_id) REFERENCES schema_content (id);

ALTER TABLE project_students
    ADD CONSTRAINT student_fkey FOREIGN KEY (student_id) REFERENCES users (id),
    ADD CONSTRAINT project_fkey FOREIGN KEY (project_id) REFERENCES projects (id);

ALTER TABLE student_project_request
    ADD CONSTRAINT student_fkey FOREIGN KEY (student_id) REFERENCES users (id),
    ADD CONSTRAINT project_fkey FOREIGN KEY (project_id) REFERENCES projects (id);

ALTER TABLE user_settings
    ADD CONSTRAINT user_fkey FOREIGN KEY (user_id) REFERENCES users (id),
    ADD CONSTRAINT settings_fkey FOREIGN KEY (settings_id) REFERENCES settings (id);

ALTER TABLE task_stage
    ADD CONSTRAINT task_fkey FOREIGN KEY (task_id) REFERENCES task (id),
    ADD CONSTRAINT stage_fkey FOREIGN KEY (stage_id) REFERENCES stage (id);


ALTER TABLE activity_student
    ADD CONSTRAINT student_fkey FOREIGN KEY (student_id) REFERENCES users (id),
    ADD CONSTRAINT activity_fkey FOREIGN KEY (activity_id) REFERENCES activity (id);


ALTER TABLE organisation_user
    ADD CONSTRAINT organisation_fkey FOREIGN KEY (organisation_id) REFERENCES organisations (id),
    ADD CONSTRAINT representative_fkey FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE department_user
    ADD CONSTRAINT organisation_fkey FOREIGN KEY (organisation_id) REFERENCES organisations (id),
    ADD CONSTRAINT mentor_fkey FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE student_info_students
    ADD CONSTRAINT student_fkey FOREIGN KEY (student_id) REFERENCES users (id),
    ADD CONSTRAINT group_fkey FOREIGN KEY (student_info_id) REFERENCES student_info (id);


ALTER TABLE organisation_family
    ADD CONSTRAINT organisation_fkey FOREIGN KEY (parent_id) REFERENCES organisations (id);

ALTER TABLE activity_mentor
    ADD CONSTRAINT activity_fkey FOREIGN KEY (activity_id) REFERENCES activity (id),
    ADD CONSTRAINT mentor_fkey FOREIGN KEY (mentor_id) REFERENCES users (id);


-- ALTER TABLE user_notification
--     ADD CONSTRAINT user_fkey FOREIGN KEY (user_id) REFERENCES users (id),
--     ADD CONSTRAINT notification_fkey FOREIGN KEY (notification_id) REFERENCES notifications (id);

ALTER TABLE activity_stage
    ADD CONSTRAINT activity_fkey FOREIGN KEY (activity_id) REFERENCES activity (id),
    ADD CONSTRAINT stage_fkey FOREIGN KEY (stage_id) REFERENCES stage (id);

ALTER TABLE posts
    ADD CONSTRAINT createdby_fkey FOREIGN KEY (created_by) REFERENCES users (id),
    ADD CONSTRAINT profileif_fkey FOREIGN KEY (profile_id) REFERENCES schema_content (id),
    ADD CONSTRAINT fileid_fkey FOREIGN KEY (file_id) REFERENCES files (id);



ALTER TABLE mail_group_history
    ADD CONSTRAINT createdby_fkey FOREIGN KEY (created_by) REFERENCES users (id);


ALTER TABLE student_grades
    ADD CONSTRAINT studentid_fkey FOREIGN KEY (student_id) REFERENCES users (id),
    ADD CONSTRAINT activityid_fkey FOREIGN KEY (activity_id) REFERENCES activity (id),
    ADD CONSTRAINT projectid_fkey FOREIGN KEY (project_id) REFERENCES projects (id),
    ADD CONSTRAINT stageid_fkey FOREIGN KEY (stage_id) REFERENCES stage (id);

--
--
-- \c ccpr_smtp;
--
-- CREATE TABLE transport (
--   domain VARCHAR(128) NOT NULL,
--   transport VARCHAR(128) NOT NULL,
--   PRIMARY KEY (domain)
-- );
--
-- CREATE TABLE users (
--   userid VARCHAR(128) NOT NULL,
--   password VARCHAR(128),
--   realname VARCHAR(128),
--   uid INTEGER NOT NULL,
--   gid INTEGER NOT NULL,
--   home VARCHAR(128),
--   mail VARCHAR(255),
--   PRIMARY KEY (userid)
-- );
--
-- CREATE TABLE virtual (
--   address VARCHAR(255) NOT NULL,
--   userid VARCHAR(255) NOT NULL,
--   PRIMARY KEY (address)
-- );

-- create view postfix_mailboxes as
--   select userid, home||'/' as mailbox from users
--   union all
--   select domain as userid, 'dummy' as mailbox from transport;
--
-- create view postfix_virtual as
--   select userid, userid as address from users
--   union all
--   select userid, address from virtual;


\c postgres;

GRANT ALL PRIVILEGES ON DATABASE ccpr_main TO backend;

-- GRANT ALL PRIVILEGES ON DATABASE ccpr_grafana TO grafana;
--
-- GRANT ALL PRIVILEGES ON DATABASE ccpr_smtp TO smtp;


