CREATE TABLE IF NOT EXISTS crm_user (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(30),
    date_of_birth DATE,
    address VARCHAR(255),
    suburb VARCHAR(100),
    state VARCHAR(10),
    postcode VARCHAR(10),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_crm_user_email
    ON crm_user (lower(email))
    WHERE email IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crm_user_phone
    ON crm_user (phone)
    WHERE phone IS NOT NULL;
