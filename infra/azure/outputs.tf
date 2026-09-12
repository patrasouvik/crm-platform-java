output "resource_group_name" {
  value = azurerm_resource_group.crm.name
}

output "public_ip" {
  value = azurerm_public_ip.crm.ip_address
}

output "crm_url" {
  value = "http://${azurerm_public_ip.crm.ip_address}:8080"
}
