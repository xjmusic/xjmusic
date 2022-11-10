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

# Privileged user for connecting to EC2 instances
resource "aws_key_pair" "xjmusicinc" {
  key_name   = "xjmusicinc"
  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDRTmR16l0wUblkN/g1knqwH5Cgco7orLO9RFKNNaSPN9yRFW+KDmPkr6FGtaX5h0aMFgy9+uwSKGkQaVCY9z8I2uMoKhbji6qFhBmwwP1ASI/fDusDwkIivHj1j4HDskamlj9j2MK57Pase4J/fiAYIZij4gsYBIiR7rmWGBKRQp3WFnLhOB7SYNZlwbqVKT6WDY6wtrX5DE2w4OG3Ex3MPn3+HHOZBU3QMjfuoMD8ojirw7Y4A/25J8D6vmX4QGley/DDD7ppkeeJp5o9wa5ukK0f9+Ckx3OmjNeTQjw7Ip5a648EUobS3vh6J/Wds6IyzaUXDQqto695q42dlTwN"
}
