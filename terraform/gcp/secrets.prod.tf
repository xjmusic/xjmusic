# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "secret-prod-aws-access-key-id" {
  project   = local.gcp-project-id
  secret_id = "xj-services-prod-aws-access-key-id"
  replication { automatic = true }
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "secret-prod-aws-secret-key" {
  project   = local.gcp-project-id
  secret_id = "xj-services-prod-aws-secret-key"
  replication { automatic = true }
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "secret-prod-google-client-id" {
  project   = local.gcp-project-id
  secret_id = "xj-services-prod-google-client-id"
  replication { automatic = true }
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "secret-prod-google-client-secret" {
  project   = local.gcp-project-id
  secret_id = "xj-services-prod-google-client-secret"
  replication { automatic = true }
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "secret-prod-ingest-token-value" {
  project   = local.gcp-project-id
  secret_id = "xj-services-prod-ingest-token-value"
  replication { automatic = true }
}
