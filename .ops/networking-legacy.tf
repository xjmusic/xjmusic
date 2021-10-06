# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/vpc
resource "aws_vpc" "xj-vpc" {
  cidr_block           = "172.30.0.0/16"
  instance_tenancy     = "default"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name        = "xj-vpc"
    Environment = "prod"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/subnet
resource "aws_subnet" "xj-vpc-subnet-us-east-1a" {
  vpc_id                  = aws_vpc.xj-vpc.id
  cidr_block              = "172.30.0.0/24"
  map_public_ip_on_launch = true

  tags = {
    Name = "xj-vpc-subnet-us-east-1a"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/subnet
resource "aws_subnet" "xj-vpc-subnet-use1-az1" {
  vpc_id                  = aws_vpc.xj-vpc.id
  cidr_block              = "172.30.1.0/24"
  map_public_ip_on_launch = true

  tags = {
    Name = "xj-vpc-subnet-use1-az1"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/network_acl
resource "aws_network_acl" "xj-vpc-network-acl" {
  vpc_id = aws_vpc.xj-vpc.id
  subnet_ids = [
    aws_subnet.xj-vpc-subnet-us-east-1a.id,
    aws_subnet.xj-vpc-subnet-use1-az1.id,
  ]

  egress {
    cidr_block      = "0.0.0.0/0"
    action          = "allow"
    from_port       = 0
    rule_no         = 100
    to_port         = 0
    icmp_code       = 0
    icmp_type       = 0
    ipv6_cidr_block = ""
    protocol        = "-1"
  }

  ingress {
    cidr_block = "0.0.0.0/0"
    action     = "allow"
    from_port  = -0
    protocol   = "-1"
    rule_no    = 100
    to_port    = -0
  }

  tags = {
    Name = "xj-vpc-network-acl"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route_table
resource "aws_route_table" "xj-vpc-route-table" {
  vpc_id = aws_vpc.xj-vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.xj-vpc-internet-gateway.id
  }

  tags = {
    Name = "xj-vpc-route-table"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/internet_gateway
resource "aws_internet_gateway" "xj-vpc-internet-gateway" {
  vpc_id = aws_vpc.xj-vpc.id

  tags = {
    Name = "xj-vpc-internet-gateway"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/vpc_dhcp_options
resource "aws_vpc_dhcp_options" "xj-vpc-dhcp-option-set" {
  domain_name = "ec2.internal"
  domain_name_servers = [
    "AmazonProvidedDNS"
  ]

  tags = {
    Name = "xj-vpc-dhcp-option-set"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/vpc_dhcp_options_association
resource "aws_vpc_dhcp_options_association" "xj-vpc-dhcp-option-set" {
  vpc_id          = aws_vpc.xj-vpc.id
  dhcp_options_id = aws_vpc_dhcp_options.xj-vpc-dhcp-option-set.id
}