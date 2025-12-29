--
-- PostgreSQL database dump
--

-- Dumped from database version 15.3 (Debian 15.3-1.pgdg120+1)
-- Dumped by pg_dump version 15.3 (Debian 15.3-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: acmsecy
--

-- COPY public.events (id, branch, description, end_date, ended, related_link, start_date, title, venue) FROM stdin;
-- 3	WIT	string	2023-10-29 13:09:12.677	f	string	2023-10-28 13:09:12.677	Women in Tech	string
-- 1	DEVELOPMENT	string	2023-11-03 13:09:12.677	t	string	2023-11-01 13:09:12.677	Ideathon 3.0	string
-- 2	AI	string	2023-10-31 13:09:12.677	t	string	2023-10-30 13:09:12.677	Kaggle challenge 3.0	string
-- \.

INSERT INTO public.events (id, branch, description, end_date, ended, related_link, start_date, title, venue)
VALUES (3, 'WIT', 'string', '2023-10-29 13:09:12.677', 'f', 'string', '2023-10-28 13:09:12.677', 'Women in Tech',
        'string');
INSERT INTO public.events (id, branch, description, end_date, ended, related_link, start_date, title, venue)
VALUES (1, 'DEVELOPMENT', 'string', '2023-11-03 13:09:12.677', 't', 'string', '2023-11-01 13:09:12.677', 'Ideathon 3.0',
        'string');
INSERT INTO public.events (id, branch, description, end_date, ended, related_link, start_date, title, venue)
VALUES (2, 'AI', 'string', '2023-10-31 13:09:12.677', 't', 'string', '2023-10-30 13:09:12.677', 'Kaggle challenge 3.0',
        'string');



--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: acmsecy
--

-- COPY public.users (id, batch, branch, designation, display_picture, email, name, password, student_id, verified,
--                    xp_total) FROM stdin;
-- 2	2024	CSE	Member	\N	manjotsinghoberoi.btcse20@pec.edu.in	Manjot Singh Oberoi	$2a$10$flz6mYi4mo1RCw4YgheMRO5T90yNPQujsDaZ.6pCM2W4Il2uX9REm	20103075	f	1
-- 1	2024	CSE	Admin	\N	harshpreetsinghjohar.btcse20@pec.edu.in	Harshpreet Singh Johar	$2a$10$d/mEUVISoHIQumX3MZE2iuWbhtqIlCuW5xIEw9vxEuIbWx/TMfcWO	20103076	t	10
-- \.

INSERT INTO public.users (id, batch, branch, designation, display_picture, email, name, password, student_id, verified,
                          xp_total)
VALUES (2, 2024, 'CSE', 'Member', NULL, 'manjotsinghoberoi.btcse20@pec.edu.in', 'Manjot Singh Oberoi',
        '$2a$10$flz6mYi4mo1RCw4YgheMRO5T90yNPQujsDaZ.6pCM2W4Il2uX9REm', 20103075, 'f', 1);
INSERT INTO public.users (id, batch, branch, designation, display_picture, email, name, password, student_id, verified,
                          xp_total)
VALUES (1, 2024, 'CSE', 'Admin', NULL, 'harshpreetsinghjohar.btcse20@pec.edu.in', 'Harshpreet Singh Johar',
        '$2a$10$RJzQiTkb/ZbBDwSYXTYTYeo7gKBDn7Q0ntqLBW5nCIYZZd8/NkuFu', 20103076, 't', 10);

--
-- Data for Name: transactions; Type: TABLE DATA; Schema: public; Owner: acmsecy
--

-- COPY public.transactions (id, date, role, xp_awarded, event_id, user_id) FROM stdin;
-- 1	2023-11-01 18:49:48.881507	ORGANIZER	5	1	1
-- 2	2023-11-01 18:49:48.881551	PARTICIPANT	1	1	2
-- 3	2023-11-01 18:50:24.607074	ORGANIZER	5	2	1
-- \.
INSERT INTO public.transactions (id, date, role, xp_awarded, event_id, user_id)
VALUES (1, '2023-11-01 18:49:48.881507', 'ORGANIZER', 5, 1, 1);
INSERT INTO public.transactions (id, date, role, xp_awarded, event_id, user_id)
VALUES (2, '2023-11-01 18:49:48.881551', 'PARTICIPANT', 1, 1, 2);
INSERT INTO public.transactions (id, date, role, xp_awarded, event_id, user_id)
VALUES (3, '2023-11-01 18:50:24.607074', 'ORGANIZER', 5, 2, 1);


--
-- Data for Name: verification_token; Type: TABLE DATA; Schema: public; Owner: acmsecy
--

-- COPY public.verification_token (token, created_date, id) FROM stdin;
-- 564add94-f324-4d99-8fec-a825e656d04e	2023-11-01 18:46:23.322393	2
-- \.
INSERT INTO public.verification_token (token, created_date, id)
VALUES ('564add94-f324-4d99-8fec-a825e656d04e', '2023-11-01 18:46:23.322393', 2);


--
-- Name: events_id_seq; Type: SEQUENCE SET; Schema: public; Owner: acmsecy
--

SELECT pg_catalog.setval('public.events_id_seq', 3, true);


--
-- Name: transactions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: acmsecy
--

SELECT pg_catalog.setval('public.transactions_id_seq', 3, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: acmsecy
--

SELECT pg_catalog.setval('public.users_id_seq', 2, true);


--
-- PostgreSQL database dump complete
--