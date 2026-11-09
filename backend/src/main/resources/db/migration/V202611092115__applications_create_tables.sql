CREATE TABLE applications
(
    id               BIGSERIAL PRIMARY KEY,
    applicant_id     BIGINT                   NOT NULL,
    animal_id        BIGINT                   NOT NULL,
    status           VARCHAR(20)              NOT NULL,
    street           VARCHAR(200)             NOT NULL,
    city             VARCHAR(100)             NOT NULL,
    municipality     VARCHAR(100)             NOT NULL,
    latitude         DOUBLE PRECISION,
    longitude        DOUBLE PRECISION,
    distance_km      DOUBLE PRECISION,
    rejection_reason VARCHAR(500),
    submitted_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    turn_deadline    TIMESTAMP WITH TIME ZONE,
    version          BIGINT                   NOT NULL DEFAULT 0
);

CREATE TABLE application_answers
(
    id                  BIGSERIAL PRIMARY KEY,
    application_id      BIGINT      NOT NULL UNIQUE REFERENCES applications (id) ON DELETE CASCADE,
    housing_type        VARCHAR(20) NOT NULL,
    has_yard            BOOLEAN     NOT NULL,
    has_children_under6 BOOLEAN     NOT NULL,
    has_cats            BOOLEAN     NOT NULL,
    has_dogs            BOOLEAN     NOT NULL,
    has_experience      BOOLEAN     NOT NULL,
    hours_alone_per_day INTEGER     NOT NULL CHECK (hours_alone_per_day BETWEEN 0 AND 24),
    motivation          TEXT        NOT NULL
);

CREATE UNIQUE INDEX uq_applications_active_per_animal
    ON applications (applicant_id, animal_id) WHERE status IN ('SUBMITTED', 'WAITING', 'YOUR_TURN', 'WALK_SCHEDULED', 'APPROVED');

CREATE INDEX idx_applications_animal_queue
    ON applications (animal_id, status, submitted_at);