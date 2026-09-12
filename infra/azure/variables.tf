variable "location" {
  description = "Azure region for the CRM environment"
  type        = string
  default     = "australiaeast"
}

variable "project_name" {
  description = "Project/resource name prefix"
  type        = string
  default     = "maieveen-crm"
}

variable "resource_group_name" {
  description = "Resource group name"
  type        = string
  default     = "rg-maieveen-crm"
}

variable "vm_size" {
  description = "Azure Linux VM size"
  type        = string
  default     = "Standard_B1s"
}

variable "admin_username" {
  description = "Linux VM administrator username"
  type        = string
  default     = "maieveenadmin"
}

variable "ssh_public_key" {
  description = "SSH public key content. Replace before apply."
  type        = string
  default     = "REPLACE_WITH_SSH_PUBLIC_KEY"
}

variable "allowed_ssh_cidr" {
  description = "CIDR allowed to SSH; use your public IP/32"
  type        = string
  default     = "REPLACE_WITH_YOUR_PUBLIC_IP/32"
}

variable "allowed_http_cidr" {
  description = "CIDR allowed to access CRM HTTP"
  type        = string
  default     = "0.0.0.0/0"
}
