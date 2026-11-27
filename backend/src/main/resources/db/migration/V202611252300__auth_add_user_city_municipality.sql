-- City and municipality of the applicant, entered at registration.
-- Nullable: staff accounts created by the admin and users registered before this change have none.
ALTER TABLE users
    ADD COLUMN city         VARCHAR(100),
    ADD COLUMN municipality VARCHAR(100);
