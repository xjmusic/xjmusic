# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# Configure the AWS Provider
provider "aws" {
  region = "us-east-1"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-terraform-state" {
  bucket = "xj-terraform-state"
  acl    = "private"
}
