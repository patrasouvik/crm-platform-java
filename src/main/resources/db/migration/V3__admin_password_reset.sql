ALTER TABLE admin_user
    ADD COLUMN email VARCHAR(255);

CREATE UNIQUE INDEX uk_admin_user_email
    ON admin_user (lower(email))
    WHERE email IS NOT NULL;

CREATE TABLE password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    admin_user_id BIGINT NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_password_reset_admin_user
        FOREIGN KEY (admin_user_id) REFERENCES admin_user(id)
);

CREATE INDEX idx_password_reset_admin_user
    ON password_reset_token (admin_user_id);

CREATE INDEX idx_password_reset_expires_at
    ON password_reset_token (expires_at);
