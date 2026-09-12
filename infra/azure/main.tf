resource "azurerm_resource_group" "crm" {
  name     = var.resource_group_name
  location = var.location
}

resource "azurerm_virtual_network" "crm" {
  name                = "${var.project_name}-vnet"
  location            = azurerm_resource_group.crm.location
  resource_group_name = azurerm_resource_group.crm.name
  address_space       = ["10.20.0.0/16"]
}

resource "azurerm_subnet" "crm" {
  name                 = "${var.project_name}-subnet"
  resource_group_name  = azurerm_resource_group.crm.name
  virtual_network_name = azurerm_virtual_network.crm.name
  address_prefixes     = ["10.20.1.0/24"]
}

resource "azurerm_network_security_group" "crm" {
  name                = "${var.project_name}-nsg"
  location            = azurerm_resource_group.crm.location
  resource_group_name = azurerm_resource_group.crm.name

  security_rule {
    name                       = "Allow-CRM-8080"
    priority                   = 100
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "8080"
    source_address_prefix      = var.allowed_http_cidr
    destination_address_prefix = "*"
  }

  security_rule {
    name                       = "Allow-SSH"
    priority                   = 110
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "22"
    source_address_prefix      = var.allowed_ssh_cidr
    destination_address_prefix = "*"
  }
}

resource "azurerm_public_ip" "crm" {
  name                = "${var.project_name}-pip"
  location            = azurerm_resource_group.crm.location
  resource_group_name = azurerm_resource_group.crm.name
  allocation_method   = "Static"
  sku                 = "Standard"
}

resource "azurerm_network_interface" "crm" {
  name                = "${var.project_name}-nic"
  location            = azurerm_resource_group.crm.location
  resource_group_name = azurerm_resource_group.crm.name

  ip_configuration {
    name                          = "internal"
    subnet_id                     = azurerm_subnet.crm.id
    private_ip_address_allocation = "Dynamic"
    public_ip_address_id          = azurerm_public_ip.crm.id
  }
}

resource "azurerm_network_interface_security_group_association" "crm" {
  network_interface_id      = azurerm_network_interface.crm.id
  network_security_group_id = azurerm_network_security_group.crm.id
}

resource "azurerm_linux_virtual_machine" "crm" {
  name                            = "${var.project_name}-vm"
  resource_group_name             = azurerm_resource_group.crm.name
  location                        = azurerm_resource_group.crm.location
  size                            = var.vm_size
  admin_username                  = var.admin_username
  disable_password_authentication = true

  network_interface_ids = [azurerm_network_interface.crm.id]

  admin_ssh_key {
    username   = var.admin_username
    public_key = var.ssh_public_key
  }

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }

  source_image_reference {
    publisher = "Canonical"
    offer     = "0001-com-ubuntu-server-jammy"
    sku       = "22_04-lts-gen2"
    version   = "latest"
  }

  custom_data = base64encode(<<-EOF
              #cloud-config
              package_update: true
              packages:
                - docker.io
                - git
              runcmd:
                - systemctl enable docker
                - systemctl start docker
                - usermod -aG docker ${var.admin_username}
              EOF
  )

  tags = {
    Project = "maieveen-crm"
  }
}
