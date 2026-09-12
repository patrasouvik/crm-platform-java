variable "aws_region" {
  description = "AWS region for the CRM environment"
  type        = string
  default     = "ap-southeast-2"
}

variable "project_name" {
  description = "Project/resource name prefix"
  type        = string
  default     = "maieveen-crm"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.micro"
}

variable "key_name" {
  description = "Existing EC2 key pair name. Set this before apply."
  type        = string
  default     = "REPLACE_WITH_EC2_KEY_PAIR"
}

variable "allowed_ssh_cidr" {
  description = "CIDR allowed to SSH to the instance; use your public IP/32 rather than 0.0.0.0/0"
  type        = string
  default     = "REPLACE_WITH_YOUR_PUBLIC_IP/32"
}

variable "allowed_http_cidr" {
  description = "CIDR allowed to access the CRM over HTTP"
  type        = string
  default     = "0.0.0.0/0"
}
