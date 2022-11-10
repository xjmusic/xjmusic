# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

terraform {
  required_version = ">= 0.13.1" # see https://releases.hashicorp.com/terraform/
  backend "gcs" {
    bucket = "xj-vpc-host-prod-us-west1-terraform"
    prefix = "main"
  }
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "4.23.0" # see https://github.com/terraform-providers/terraform-provider-google/releases
    }
  }
}

provider "google" {
  project = "xj-vpc-host-prod"
  region  = "us-west1"
}
