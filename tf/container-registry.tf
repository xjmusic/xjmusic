// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecr_repository
resource "aws_ecr_repository" "xj-hub" {
  name = "xj/hub"

  image_scanning_configuration {
    scan_on_push = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecr_repository
resource "aws_ecr_repository" "xj-nexus" {
  name = "xj/nexus"

  image_scanning_configuration {
    scan_on_push = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecr_repository
resource "aws_ecr_repository" "xj-dev-hub" {
  name = "xj/dev/hub"

  image_scanning_configuration {
    scan_on_push = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecr_repository
resource "aws_ecr_repository" "xj-dev-nexus" {
  name = "xj/dev/nexus"

  image_scanning_configuration {
    scan_on_push = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecr_repository
resource "aws_ecr_repository" "xj-base-nexus" {
  name = "xj/base/nexus"

  image_scanning_configuration {
    scan_on_push = false
  }
}