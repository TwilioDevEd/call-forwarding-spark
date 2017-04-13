CREATE SEQUENCE public.states_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 444
  CACHE 1;

CREATE TABLE public.states
(
  id integer NOT NULL DEFAULT nextval('states_id_seq'::regclass),
  name character varying(255),
  CONSTRAINT states_pkey PRIMARY KEY (id)
);