CREATE TABLE animals
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100)             NOT NULL,
    species       VARCHAR(10)              NOT NULL,
    age_months    INTEGER                  NOT NULL,
    size          VARCHAR(10)              NOT NULL,
    description   TEXT                     NOT NULL,
    vaccinated    BOOLEAN                  NOT NULL DEFAULT FALSE,
    neutered      BOOLEAN                  NOT NULL DEFAULT FALSE,
    medical_notes VARCHAR(1000),
    status        VARCHAR(20)              NOT NULL DEFAULT 'DRAFT',
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT chk_animals_species CHECK (species IN ('DOG', 'CAT')),
    CONSTRAINT chk_animals_size CHECK (size IN ('SMALL', 'MEDIUM', 'LARGE')),
    CONSTRAINT chk_animals_status CHECK (status IN ('DRAFT', 'AVAILABLE', 'RESERVED', 'ADOPTED')),
    CONSTRAINT chk_animals_age CHECK (age_months >= 0)
);

CREATE TABLE animal_requirements
(
    animal_id BIGINT      NOT NULL REFERENCES animals (id) ON DELETE CASCADE,
    type      VARCHAR(30) NOT NULL,
    PRIMARY KEY (animal_id, type),
    CONSTRAINT chk_animal_requirements_type
        CHECK (type IN ('YARD', 'NO_CATS', 'NO_DOGS', 'NO_SMALL_CHILDREN', 'EXPERIENCE'))
);

CREATE INDEX idx_animals_status ON animals (status);