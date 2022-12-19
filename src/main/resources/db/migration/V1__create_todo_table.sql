CREATE TABLE todos (
	id uuid NOT NULL,
	description varchar(255) NOT NULL,
	complete boolean NOT NULL,
	PRIMARY KEY (id)
);