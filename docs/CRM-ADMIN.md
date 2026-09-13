# CRM Admin Portal

## Local URLs

- Landing page: http://localhost:8080/crm
- Admin login: http://localhost:8080/crm/login
- Dashboard: http://localhost:8080/crm/admin
- Forgot password: http://localhost:8080/crm/forgot-password

## Local admin credentials

On startup, the application bootstraps an admin account into PostgreSQL if the configured username does not already exist. The password is BCrypt-hashed before it is stored.

```bash
export CRM_ADMIN_USERNAME=admin
export CRM_ADMIN_EMAIL='souvik.k.patra@gmail.com'
export CRM_ADMIN_PASSWORD='change-this-password'
export CRM_ADMIN_BOOTSTRAP_ENABLED=true
```

Development defaults are `admin` / `change-me` with `souvik.k.patra@gmail.com` as the configured reset email so local development works without extra configuration. Change these before exposing the application publicly.

If the admin already exists, restarting the application does not overwrite its password. The configured email is kept in sync for the bootstrap account.

## Password management

### Change password

A logged-in administrator can use **Change Password** from the dashboard. The current password is verified before the new BCrypt hash is stored. A successful change invalidates the current HTTP session and requires the administrator to log in again.

Password requirements:

- At least 12 characters
- At least one letter
- At least one number
- At least one special character
- Must not be the current password

### Forgot password / OTP reset

The login page provides **Forgot password?**. The administrator enters the registered email address, receives a six-digit OTP, verifies it, and sets a new password.

- OTP expires after 10 minutes by default.
- OTP is stored as a BCrypt hash, not plaintext.
- OTPs are single-use.
- Maximum five verification attempts are allowed for an OTP.
- A successful reset invalidates the reset session and requires a fresh login.
- Password reset is recorded in the audit log.

### Local development mail mode

Email delivery is disabled by default so the application can be developed without an SMTP provider:

```bash
CRM_MAIL_ENABLED=false
```

In this mode, the generated OTP is written to the application log. This is for local development only.

To send real email, configure SMTP through environment variables and enable mail:

```bash
export CRM_MAIL_ENABLED=true
export CRM_MAIL_FROM='noreply@skp-lab.com'
export SPRING_MAIL_HOST='your-smtp-host'
export SPRING_MAIL_PORT=587
export SPRING_MAIL_USERNAME='your-smtp-username'
export SPRING_MAIL_PASSWORD='your-smtp-password'
```

Never commit SMTP credentials or Gmail/app passwords to Git. For production, use a secrets manager or deployment secret store.

## Database and migrations

- PostgreSQL is the application database.
- Terraform is intended to provision infrastructure such as AWS networking and RDS; it does not manage application tables.
- Flyway manages application schema migrations under `src/main/resources/db/migration`.
- Hibernate uses `ddl-auto: validate`, so it validates the schema rather than changing it.
- `baseline-on-migrate` is enabled at version `1` so an existing local database can be adopted without recreating its current CRM data. New migrations continue from there.

## Current schema

- `crm_user` — CRM customer records
- `admin_user` — persistent admin identities, email addresses and BCrypt password hashes
- `role` — application roles such as `ADMIN`
- `admin_user_role` — admin-to-role mapping
- `audit_log` — records CRM create, update, delete, password change and password reset actions
- `password_reset_token` — hashed OTPs, expiry, verification and usage state
- `flyway_schema_history` — Flyway migration tracking

## Features

- Public CRM landing page
- Admin-only authentication using Spring Security
- Persistent database-backed admin authentication
- Role-based access control with an `ADMIN` role
- Change admin password while authenticated
- Forgot password with OTP verification
- Configurable SMTP email delivery
- Local OTP development mode without SMTP infrastructure
- New user creation stored in PostgreSQL
- Duplicate protection for email and phone
- Existing user search by name, email, or phone
- Full user details page
- Edit and delete user operations
- Audit logging for CRM and password-management actions
- Responsive Thymeleaf UI

## Local development without AWS

No AWS infrastructure is required for development. Run PostgreSQL locally (for example with the existing Docker setup) and start Spring Boot with Maven. Flyway will create the required tables on a fresh database, or apply new migrations to an existing local database.

## Production notes

Before public deployment, use strong credentials supplied by the deployment environment or a secrets manager, disable bootstrap after the initial administrator has been provisioned, configure a transactional email provider, enable HTTPS, and review database backups, access controls, audit retention, OTP rate limiting, and monitoring.
