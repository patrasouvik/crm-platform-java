# Azure deployment

The application is intentionally cloud-agnostic. A simple first deployment can use one small Linux VM running Docker Compose and PostgreSQL.

For production, move PostgreSQL to Azure Database for PostgreSQL and place the application behind an Azure Application Gateway or load balancer. Terraform can be added here as the Azure deployment evolves.
