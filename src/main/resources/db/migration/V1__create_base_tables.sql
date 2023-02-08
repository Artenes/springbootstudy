CREATE TABLE users (
	id uuid NOT NULL,
	email varchar(255) UNIQUE NOT NULL,
	name varchar(255) NOT NULL,
	picture_url varchar(255),
	role varchar(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE projects(
    id uuid NOT NULL,
    title varchar(255) NOT NULL,
    user_id uuid NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
            REFERENCES users(id)
);

CREATE TABLE tasks (
	id uuid NOT NULL,
	title varchar(255) NOT NULL,
	description varchar(255),
	complete boolean NOT NULL,
	due_date timestamp with time zone,
	priority varchar(255),
	parent_id uuid,
	project_id uuid,
	user_id uuid NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT fk_task
	    FOREIGN KEY(parent_id)
	        REFERENCES tasks(id),
    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
            REFERENCES users(id),
    CONSTRAINT fk_projects
        FOREIGN KEY(project_id)
            REFERENCES projects(id)
);

CREATE TABLE tags(
    id uuid NOT NULL,
    name varchar(255) NOT NULL,
    user_id uuid NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
            REFERENCES users(id)
);

CREATE TABLE tasks_tags(
    task_id uuid NOT NULL,
    tag_id uuid NOT NULL,
    CONSTRAINT fk_task_id
        FOREIGN KEY(task_id)
            REFERENCES tasks(id),
    CONSTRAINT fk_tag_id
            FOREIGN KEY(tag_id)
                REFERENCES tags(id)
);

CREATE TABLE comments(
    id uuid NOT NULL,
    text varchar(255) NOT NULL,
    user_id uuid NOT NULL,
    task_id uuid NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
            REFERENCES users(id),
    CONSTRAINT fk_task
            FOREIGN KEY(task_id)
                REFERENCES tasks(id)
);