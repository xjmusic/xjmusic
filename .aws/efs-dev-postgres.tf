# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/efs_file_system
resource "aws_efs_file_system" "xj-dev-postgres-efs" {
  creation_token = "xj-dev-postgres-efs"

  tags = {
    Name = "xj-dev-postgres-efs"
  }
}

resource "aws_efs_mount_target" "xj-dev-postgres-efs-mount0" {
  subnet_id      = module.xj-prod-vpc.private_subnets[0]
  file_system_id = aws_efs_file_system.xj-dev-postgres-efs.id
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/efs_mount_target
resource "aws_efs_mount_target" "xj-dev-postgres-efs-mount1" {
  subnet_id      = module.xj-prod-vpc.private_subnets[1]
  file_system_id = aws_efs_file_system.xj-dev-postgres-efs.id
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/efs_mount_target
resource "aws_efs_mount_target" "xj-dev-postgres-efs-mount2" {
  subnet_id      = module.xj-prod-vpc.private_subnets[2]
  file_system_id = aws_efs_file_system.xj-dev-postgres-efs.id
}
