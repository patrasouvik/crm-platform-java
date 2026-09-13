CREATE TABLE IF NOT EXISTS role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_role_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS admin_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_admin_user_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS admin_user_role (
    admin_user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (admin_user_id, role_id),
    CONSTRAINT fk_admin_user_role_user
        FOREIGN KEY (admin_user_id) REFERENCES admin_user(id),
    CONSTRAINT fk_admin_user_role_role
        FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    admin_user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    details VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_audit_log_admin_user
        FOREIGN KEY (admin_user_id) REFERENCES admin_user(id)
);

CREATE INDEX IF NOT EXISTS idx_audit_log_admin_user_id
    ON audit_log (admin_user_id);

CREATE INDEX IF NOT EXISTS idx_audit_log_entity
    ON audit_log (entity_type, entity_id);

CREATE INDEX IF NOT EXISTS idx_audit_log_created_at
    ON audit_log (created_at);
