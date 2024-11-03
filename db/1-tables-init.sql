CREATE TABLE public.t_training_session (
	id int4 NOT NULL,
	session_date date NOT NULL,
	"data" text NOT NULL,
	CONSTRAINT pk_training_session PRIMARY KEY (id)
);