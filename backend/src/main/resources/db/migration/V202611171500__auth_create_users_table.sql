CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(254)             NOT NULL,
    password_hash VARCHAR(100)             NOT NULL,
    full_name     VARCHAR(100)             NOT NULL,
    role          VARCHAR(20)              NOT NULL,
    active        BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_email_lower CHECK (email = lower(email)),
    CONSTRAINT chk_users_role CHECK (role IN ('APPLICANT', 'STAFF', 'ADMIN'))
);
