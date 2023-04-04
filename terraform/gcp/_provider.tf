# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

terraform {
  required_version = ">= 0.15.1" # see https://releases.hashicorp.com/terraform/
  backend "gcs" {
    bucket = "xj-vpc-host-prod-us-west1-terraform"
    prefix = "main"
  }
  required_providers {
    # https://github.com/terraform-providers/terraform-provider-google/releases
    google = {
      source  = "hashicorp/google"
      version = "4.55.0"
    }
    # https://github.com/terraform-providers/terraform-provider-google/releases
    google-beta = {
      source  = "hashicorp/google-beta"
      version = "4.55.0"
    }
  }
}

provider "google" {
  project = "xj-vpc-host-prod"
  region  = "us-west1"
}
