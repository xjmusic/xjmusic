# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 3.64.0"
    }
  }

  backend "s3" {
    bucket = "xj-terraform-state"
    key    = "main"
    region = "us-east-1"
  }
}
