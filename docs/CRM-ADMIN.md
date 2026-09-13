# CRM Admin Portal

## Local URLs

- Landing page: http://localhost:8080/crm
- Admin login: http://localhost:8080/crm/login
- Dashboard: http://localhost:8080/crm/admin

## Admin credentials

The admin username and password are supplied through environment variables.

```bash
export CRM_ADMIN_USERNAME=admin
export CRM_ADMIN_PASSWORD='change-this-password'
```

The application has development defaults (`admin` / `change-me`) so it can start without extra configuration. Change these before exposing the application publicly.

## Features

- Public CRM landing page
- Admin-only authentication using Spring Security
- New user creation stored in PostgreSQL
- Existing user search by name, email, or phone
- Full user details page
- BCrypt password hashing for the configured admin password
- Responsive Thymeleaf UI

## Production notes

Before public deployment, use a strong secret supplied by the deployment environment, enable HTTPS, and move admin identities to a persistent user/role store if multiple administrators are required.
