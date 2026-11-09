CREATE TABLE walk_settings (
                               id            BIGINT      PRIMARY KEY,
                               work_start    TIME        NOT NULL,
                               work_end      TIME        NOT NULL,
                               slot_minutes  INTEGER     NOT NULL,
                               staff_count   INTEGER     NOT NULL,
                               CONSTRAINT chk_walk_settings_hours CHECK (work_start < work_end),
                               CONSTRAINT chk_walk_settings_slot CHECK (slot_minutes > 0),
                               CONSTRAINT chk_walk_settings_staff CHECK (staff_count > 0)
);

INSERT INTO walk_settings (id, work_start, work_end, slot_minutes, staff_count)
VALUES (1, '09:00', '17:00', 60, 2);

CREATE TABLE walk (
                      id              BIGSERIAL    PRIMARY KEY,
                      application_id  BIGINT       NOT NULL,
                      animal_id       BIGINT       NOT NULL,
                      applicant_id    BIGINT       NOT NULL,
                      starts_at       TIMESTAMPTZ  NOT NULL,
                      ends_at         TIMESTAMPTZ  NOT NULL,
                      status          VARCHAR(20)  NOT NULL DEFAULT 'BOOKED',
                      created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
                      CONSTRAINT chk_walk_times CHECK (ends_at > starts_at),
                      CONSTRAINT chk_walk_status CHECK (status IN ('BOOKED', 'CANCELLED', 'COMPLETED'))
);

CREATE UNIQUE INDEX uq_walk_animal_slot
    ON walk (animal_id, starts_at) WHERE status = 'BOOKED';

CREATE UNIQUE INDEX uq_walk_applicant_slot
    ON walk (applicant_id, starts_at) WHERE status = 'BOOKED';

CREATE UNIQUE INDEX uq_walk_application
    ON walk (application_id) WHERE status = 'BOOKED';

CREATE INDEX idx_walk_starts_at ON walk (starts_at);