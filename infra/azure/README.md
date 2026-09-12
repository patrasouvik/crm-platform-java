# Azure infrastructure

This folder is a starter Terraform deployment for the Maieveen CRM.

## Current design

Cloudflare/DNS (later) -> Azure Linux VM -> Docker -> Spring Boot CRM + PostgreSQL.

The current Terraform creates a resource group, VNet/subnet, NSG, public IP, NIC and small Ubuntu Linux VM. The VM bootstraps Docker and Git. PostgreSQL is intentionally not provisioned by Terraform yet; for the first lab deployment it can run in Docker Compose.

## Before apply

1. Install Azure CLI and authenticate with `az login`.
2. Copy `terraform.tfvars.example` to `terraform.tfvars`.
3. Replace the SSH public key placeholder.
4. Replace `allowed_ssh_cidr` with your public IP/32.
5. Run:

```bash
terraform init
terraform fmt
terraform validate
terraform plan
terraform apply
```

This creates billable Azure resources. Destroy the environment when finished with `terraform destroy`.

## Production TODO

- Private subnets and restricted inbound access
- Azure Database for PostgreSQL
- Application Gateway + HTTPS
- Managed identity instead of long-lived credentials
- Key Vault for secrets
- Azure Monitor / Log Analytics / alerts
- Backup and disaster recovery
- Cloudflare DNS record
