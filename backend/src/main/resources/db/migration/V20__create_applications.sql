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
    submitted_at     TIMESTAMP WITH TIME ZONE NOT NULL
);