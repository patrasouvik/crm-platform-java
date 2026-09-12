output "instance_id" {
  value       = aws_instance.crm.id
  description = "CRM EC2 instance ID"
}

output "public_ip" {
  value       = aws_instance.crm.public_ip
  description = "CRM EC2 public IP"
}

output "crm_url" {
  value       = "http://${aws_instance.crm.public_ip}:8080"
  description = "Starter CRM URL"
}
