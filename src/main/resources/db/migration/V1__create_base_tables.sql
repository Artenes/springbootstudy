CREATE TABLE todos (
	id uuid NOT NULL,
	title varchar(255) NOT NULL,
	description varchar(255),
	complete boolean NOT NULL,
	due_date timestamp with time zone,
	priority varchar(255),
	parent_id uuid,
	PRIMARY KEY (id),
	CONSTRAINT fk_todo
	    FOREIGN KEY(parent_id)
	        REFERENCES todos(id)
);

CREATE TABLE tags(
    id uuid NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE todo_tags(
    todo_id uuid NOT NULL,
    tag_id uuid NOT NULL,
    CONSTRAINT fk_todo_id
        FOREIGN KEY(todo_id)
            REFERENCES todos(id),
    CONSTRAINT fk_tag_id
            FOREIGN KEY(tag_id)
                REFERENCES tags(id)
);

CREATE TABLE users (
	id uuid NOT NULL,
	email varchar(255) UNIQUE NOT NULL,
	name varchar(255) NOT NULL,
	picture_url varchar(255),
	PRIMARY KEY (id)
);