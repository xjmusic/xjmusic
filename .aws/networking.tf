# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

data "aws_availability_zones" "available" {}

resource "random_string" "xj-prod-vpc-suffix" {
  length  = 8
  special = false
}

module "xj-prod-vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "2.66.0"

  name = local.xj-prod-cluster-name
  cidr = "10.0.0.0/16"
  azs = [
    data.aws_availability_zones.available.names[0],
    data.aws_availability_zones.available.names[1],
    data.aws_availability_zones.available.names[2],
  ]
  private_subnets = [
    "10.0.1.0/24",
    "10.0.2.0/24",
    "10.0.3.0/24",
  ]
  public_subnets = [
    "10.0.4.0/24",
    "10.0.5.0/24",
    "10.0.6.0/24",
  ]
  enable_nat_gateway   = true
  single_nat_gateway   = true
  enable_dns_hostnames = true

  tags = {
    "kubernetes.io/cluster/${local.xj-prod-cluster-name}" = "shared"
  }

  public_subnet_tags = {
    "kubernetes.io/cluster/${local.xj-prod-cluster-name}" = "shared"
    "kubernetes.io/role/elb"                              = "1"
  }

  private_subnet_tags = {
    "kubernetes.io/cluster/${local.xj-prod-cluster-name}" = "shared"
    "kubernetes.io/role/internal-elb"                     = "1"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/security_group
resource "aws_security_group" "xj-prod-worker-mgmt" {
  name_prefix = "xj-prod-worker-mgmt"
  vpc_id      = module.xj-prod-vpc.vpc_id

  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"

    cidr_blocks = [
      "154.21.216.217/32",
      // admin happens to be in this private internet access VPN
    ]
  }
}
