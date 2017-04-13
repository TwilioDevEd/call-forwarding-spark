CREATE SEQUENCE public.senators_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 728
  CACHE 1;

CREATE TABLE public.senators
(
  id integer NOT NULL DEFAULT nextval('senators_id_seq'::regclass),
  name character varying(255),
  phone character varying(50),
  state_id integer NOT NULL,
  CONSTRAINT senators_pkey PRIMARY KEY (id)
)