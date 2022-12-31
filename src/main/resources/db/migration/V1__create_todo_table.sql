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
