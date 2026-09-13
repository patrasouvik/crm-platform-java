# CRM Admin Portal

## Local URLs

- Landing page: http://localhost:8080/crm
- Admin login: http://localhost:8080/crm/login
- Dashboard: http://localhost:8080/crm/admin

## Local admin credentials

On startup, the application bootstraps an admin account into PostgreSQL if the configured username does not already exist. The password is BCrypt-hashed before it is stored.

```bash
export CRM_ADMIN_USERNAME=admin
export CRM_ADMIN_PASSWORD='change-this-password'
export CRM_ADMIN_BOOTSTRAP_ENABLED=true
```

Development defaults are `admin` / `change-me` so local development works without extra configuration. Change these before exposing the application publicly.

If the admin already exists, restarting the application does not overwrite its password.

## Database and migrations

- PostgreSQL is the application database.
- Terraform is intended to provision infrastructure such as AWS networking and RDS; it does not manage application tables.
- Flyway manages application schema migrations under `src/main/resources/db/migration`.
- Hibernate uses `ddl-auto: validate`, so it validates the schema rather than changing it.
- `baseline-on-migrate` is enabled at version `1` so an existing local database can be adopted without recreating its current CRM data. New migrations continue from there.

## Current schema

- `crm_user` — CRM customer records
- `admin_user` — persistent admin identities and BCrypt password hashes
- `role` — application roles such as `ADMIN`
- `admin_user_role` — admin-to-role mapping
- `audit_log` — records CRM create, update, and delete actions with the authenticated admin
- `flyway_schema_history` — Flyway migration tracking

## Features

- Public CRM landing page
- Admin-only authentication using Spring Security
- Persistent database-backed admin authentication
- Role-based access control with an `ADMIN` role
- New user creation stored in PostgreSQL
- Duplicate protection for email and phone
- Existing user search by name, email, or phone
- Full user details page
- Edit and delete user operations
- Audit logging for user create, update, and delete actions
- Responsive Thymeleaf UI

## Local development without AWS

No AWS infrastructure is required for development. Run PostgreSQL locally (for example with the existing Docker setup) and start Spring Boot with Maven. Flyway will create the required tables on a fresh database, or apply new migrations to an existing local database.

## Production notes

Before public deployment, use strong credentials supplied by the deployment environment or a secrets manager, disable bootstrap after the initial administrator has been provisioned, enable HTTPS, and review database backups, access controls, audit retention, and monitoring.
