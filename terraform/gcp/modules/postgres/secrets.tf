# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/random/latest/docs/resources/string
resource "random_string" "postgres_username" {
  length  = 16
  special = false
  upper = false
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "postgres_username" {
  provider  = google
  project   = var.project
  secret_id = "${var.name}-username"

  replication {
    automatic = true
  }
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret_version
resource "google_secret_manager_secret_version" "postgres_username" {
  provider = google
  secret   = google_secret_manager_secret.postgres_username.id

  secret_data = random_string.postgres_username.result
}

# https://registry.terraform.io/providers/hashicorp/random/latest/docs/resources/password
resource "random_password" "postgres_password" {
  length  = 64
  special = false
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "postgres_password" {
  provider  = google
  project   = var.project
  secret_id = "${var.name}-password"

  replication {
    automatic = true
  }
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret_version
resource "google_secret_manager_secret_version" "postgres_password" {
  provider = google
  secret   = google_secret_manager_secret.postgres_password.id

  secret_data = random_password.postgres_password.result
}
