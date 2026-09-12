# AWS deployment

The application is intentionally cloud-agnostic. A simple first deployment can use one small Linux EC2 instance running Docker Compose and PostgreSQL.

For production, move PostgreSQL to Amazon RDS and place the application behind an Application Load Balancer. Terraform can be added here as the AWS deployment evolves.
