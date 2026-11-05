-- PostgreSQL Migration Script: Rotating QR Attendance System
-- Tables: attendance_sessions, attendance_records

-- 1. Table: public.attendance_sessions
CREATE TABLE IF NOT EXISTS public.attendance_sessions (
    id SERIAL PRIMARY KEY,
    event_id INTEGER NOT NULL,
    secret_key VARCHAR(255) NOT NULL,
    start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by_id INTEGER NOT NULL,
    token_expiry_seconds INTEGER NOT NULL DEFAULT 5,
    grace_period_seconds INTEGER NOT NULL DEFAULT 5,
    participant_xp INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_attendance_sessions_event FOREIGN KEY (event_id)
        REFERENCES public.events (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_sessions_creator FOREIGN KEY (created_by_id)
        REFERENCES public.users (id) ON DELETE RESTRICT
);

-- Indexes for attendance_sessions
CREATE INDEX IF NOT EXISTS idx_attendance_sessions_event_status
    ON public.attendance_sessions (event_id, status);

CREATE INDEX IF NOT EXISTS idx_attendance_sessions_status_end_time
    ON public.attendance_sessions (status, end_time);

-- 2. Table: public.attendance_records
CREATE TABLE IF NOT EXISTS public.attendance_records (
    id SERIAL PRIMARY KEY,
    event_id INTEGER NOT NULL,
    session_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    marked_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    method VARCHAR(32) NOT NULL DEFAULT 'QR_SCAN',
    marked_by_admin_id INTEGER,
    xp_awarded BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_attendance_event_user UNIQUE (event_id, user_id),
    CONSTRAINT fk_attendance_records_event FOREIGN KEY (event_id)
        REFERENCES public.events (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_records_session FOREIGN KEY (session_id)
        REFERENCES public.attendance_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_records_user FOREIGN KEY (user_id)
        REFERENCES public.users (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_records_admin FOREIGN KEY (marked_by_admin_id)
        REFERENCES public.users (id) ON DELETE SET NULL
);

-- Indexes for attendance_records
CREATE INDEX IF NOT EXISTS idx_attendance_records_session_id
    ON public.attendance_records (session_id);

CREATE INDEX IF NOT EXISTS idx_attendance_records_event_xp
    ON public.attendance_records (event_id, xp_awarded);
