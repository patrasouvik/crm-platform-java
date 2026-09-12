# AWS infrastructure

This folder is a starter Terraform deployment for the Maieveen CRM.

## Current design

Cloudflare/DNS (later) -> EC2 -> Docker -> Spring Boot CRM + PostgreSQL.

The current Terraform creates a small EC2 instance in the default VPC and a security group. The instance bootstraps Docker and Git. PostgreSQL is intentionally not provisioned by Terraform yet; for the first lab deployment it can run in Docker Compose.

## Before apply

1. Configure AWS credentials locally (prefer AWS CLI profiles or environment credentials; never commit credentials).
2. Copy `terraform.tfvars.example` to `terraform.tfvars`.
3. Set an existing EC2 key pair name.
4. Replace `allowed_ssh_cidr` with your public IP/32.
5. Run:

```bash
terraform init
terraform fmt
terraform validate
terraform plan
terraform apply
```

This creates billable AWS resources. Destroy the environment when finished with `terraform destroy`.

## Production TODO

- Dedicated VPC/private subnets
- ALB + HTTPS/ACM
- RDS PostgreSQL in private subnets
- IAM role/SSM instead of SSH
- Secrets Manager/Parameter Store
- CloudWatch logs/metrics/alarms
- Auto Scaling or ECS/EKS if scale requires it
- Remote Terraform state with S3 + locking
- Cloudflare DNS record
