# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/efs_file_system
resource "aws_efs_file_system" "xj-prod-lab-postgres-efs" {
  creation_token = "xj-prod-postgres-efs"

  tags = {
    Name = "xj-prod-postgres-efs"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/efs_mount_target
resource "aws_efs_mount_target" "xj-prod-lab-postgres-efs-mount0" {
  subnet_id      = module.xj-prod-vpc.private_subnets[0]
  file_system_id = aws_efs_file_system.xj-prod-lab-postgres-efs.id
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/efs_mount_target
resource "aws_efs_mount_target" "xj-prod-lab-postgres-efs-mount1" {
  subnet_id      = module.xj-prod-vpc.private_subnets[1]
  file_system_id = aws_efs_file_system.xj-prod-lab-postgres-efs.id
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/efs_mount_target
resource "aws_efs_mount_target" "xj-prod-lab-postgres-efs-mount2" {
  subnet_id      = module.xj-prod-vpc.private_subnets[2]
  file_system_id = aws_efs_file_system.xj-prod-lab-postgres-efs.id
}
