-- Seed data for SQLite (dev profile)

-- Events
INSERT OR IGNORE INTO events (id, branch, description, end_date, ended, related_link, start_date, title, venue)
VALUES (1, 'DEVELOPMENT', 'A hackathon for developers', '2023-11-03 13:09:12.677', 1, 'https://example.com', '2023-11-01 13:09:12.677', 'Ideathon 3.0', 'Auditorium');
INSERT OR IGNORE INTO events (id, branch, description, end_date, ended, related_link, start_date, title, venue)
VALUES (2, 'AI', 'ML competition', '2023-10-31 13:09:12.677', 1, 'https://example.com', '2023-10-30 13:09:12.677', 'Kaggle challenge 3.0', 'Lab 1');
INSERT OR IGNORE INTO events (id, branch, description, end_date, ended, related_link, start_date, title, venue)
VALUES (3, 'WIT', 'Women in Tech event', '2023-10-29 13:09:12.677', 0, 'https://example.com', '2023-10-28 13:09:12.677', 'Women in Tech', 'Seminar Hall');

-- Users (passwords are BCrypt hashed)
INSERT OR IGNORE INTO users (id, batch, branch, designation, display_picture, email, name, password, student_id, verified, xp_total)
VALUES (1, 2024, 'CSE', 'Admin', NULL, 'harshpreetsinghjohar.btcse20@pec.edu.in', 'Harshpreet Singh Johar',
        '$2a$10$RJzQiTkb/ZbBDwSYXTYTYeo7gKBDn7Q0ntqLBW5nCIYZZd8/NkuFu', 20103076, 1, 10);
INSERT OR IGNORE INTO users (id, batch, branch, designation, display_picture, email, name, password, student_id, verified, xp_total)
VALUES (2, 2024, 'CSE', 'Member', NULL, 'manjotsinghoberoi.btcse20@pec.edu.in', 'Manjot Singh Oberoi',
        '$2a$10$flz6mYi4mo1RCw4YgheMRO5T90yNPQujsDaZ.6pCM2W4Il2uX9REm', 20103075, 0, 1);

-- Transactions
INSERT OR IGNORE INTO transactions (id, date, role, xp_awarded, event_id, user_id)
VALUES (1, '2023-11-01 18:49:48.881507', 'ORGANIZER', 5, 1, 1);
INSERT OR IGNORE INTO transactions (id, date, role, xp_awarded, event_id, user_id)
VALUES (2, '2023-11-01 18:49:48.881551', 'PARTICIPANT', 1, 1, 2);
INSERT OR IGNORE INTO transactions (id, date, role, xp_awarded, event_id, user_id)
VALUES (3, '2023-11-01 18:50:24.607074', 'ORGANIZER', 5, 2, 1);

-- Verification tokens
INSERT OR IGNORE INTO verification_token (token, created_date, id)
VALUES ('564add94-f324-4d99-8fec-a825e656d04e', '2023-11-01 18:46:23.322393', 2);

-- Demo users (password: password123)
INSERT OR IGNORE INTO users (id, batch, branch, designation, display_picture, email, name, password, student_id, verified, xp_total)
VALUES (3, 2024, 'CSE', 'Member', NULL, 'ayushdubey.bt24cse@pec.edu.in', 'Ayush Dubey',
        '$2a$10$flz6mYi4mo1RCw4YgheMRO5T90yNPQujsDaZ.6pCM2W4Il2uX9REm', 24000002, 1, 0);
INSERT OR IGNORE INTO users (id, batch, branch, designation, display_picture, email, name, password, student_id, verified, xp_total)
VALUES (4, 2024, 'CSE', 'Member', NULL, 'ayushalokdubey@gmail.com', 'Alok Dubey',
        '$2a$10$flz6mYi4mo1RCw4YgheMRO5T90yNPQujsDaZ.6pCM2W4Il2uX9REm', 24000003, 1, 0);
INSERT OR IGNORE INTO users (id, batch, branch, designation, display_picture, email, name, password, student_id, verified, xp_total)
VALUES (5, 2024, 'CSE', 'Member', NULL, '24f2004275@ds.study.iitm.ac.in', 'IITM Student',
        '$2a$10$flz6mYi4mo1RCw4YgheMRO5T90yNPQujsDaZ.6pCM2W4Il2uX9REm', 24000004, 1, 0);

-- Register all 3 users as PARTICIPANTs in Ideathon 3.0 (event_id=1)
INSERT OR IGNORE INTO transactions (id, date, role, xp_awarded, event_id, user_id)
VALUES (4, '2023-11-01 18:49:48.881507', 'PARTICIPANT', 1, 1, 3);
INSERT OR IGNORE INTO transactions (id, date, role, xp_awarded, event_id, user_id)
VALUES (5, '2023-11-01 18:49:48.881507', 'PARTICIPANT', 1, 1, 4);
INSERT OR IGNORE INTO transactions (id, date, role, xp_awarded, event_id, user_id)
VALUES (6, '2023-11-01 18:49:48.881507', 'PARTICIPANT', 1, 1, 5);
