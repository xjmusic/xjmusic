# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "secret-dev-aws-access-key-id" {
  project   = local.gcp-project-id
  secret_id = "xj-services-dev-aws-access-key-id"
  replication { automatic = true }
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "secret-dev-aws-secret-key" {
  project   = local.gcp-project-id
  secret_id = "xj-services-dev-aws-secret-key"
  replication { automatic = true }
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "secret-dev-google-client-id" {
  project   = local.gcp-project-id
  secret_id = "xj-services-dev-google-client-id"
  replication { automatic = true }
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "secret-dev-google-client-secret" {
  project   = local.gcp-project-id
  secret_id = "xj-services-dev-google-client-secret"
  replication { automatic = true }
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret
resource "google_secret_manager_secret" "secret-dev-ingest-token-value" {
  project   = local.gcp-project-id
  secret_id = "xj-services-dev-ingest-token-value"
  replication { automatic = true }
}
