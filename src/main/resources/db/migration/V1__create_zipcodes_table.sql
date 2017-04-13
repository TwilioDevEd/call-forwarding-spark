CREATE SEQUENCE public.zipcodes_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 212415
  CACHE 1;
CREATE TABLE public.zipcodes
(
  id integer NOT NULL DEFAULT nextval('zipcodes_id_seq'::regclass),
  zipcode integer,
  state character varying(4),
  CONSTRAINT zipcodes_pkey PRIMARY KEY (id)
);