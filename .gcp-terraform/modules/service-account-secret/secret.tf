# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "xj-secret" {
  secret_id = var.secret_id
  project   = var.project
  replication { automatic = true }
}
