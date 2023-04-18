# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_service_account
resource "google_service_account" "xj-prod-yard" {
  project      = local.gcp-project-id
  account_id   = "xj-prod-yard"
  display_name = "XJ Production Yard"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "xj-prod-yard-secret_accessor" {
  project = local.gcp-project-id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${google_service_account.xj-prod-yard.email}"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "xj-prod-yard-cloudsql_client" {
  project = local.gcp-project-id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.xj-prod-yard.email}"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "xj-prod-yard-compute" {
  project = local.gcp-project-id
  role    = "roles/compute.serviceAgent"
  member  = "serviceAccount:${google_service_account.xj-prod-yard.email}"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "xj-prod-yard-container" {
  project = local.gcp-project-id
  role    = "roles/container.serviceAgent"
  member  = "serviceAccount:${google_service_account.xj-prod-yard.email}"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "xj-prod-yard-iam" {
  project = local.gcp-project-id
  role    = "roles/iam.serviceAccountUser"
  member  = "serviceAccount:${google_service_account.xj-prod-yard.email}"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "xj-prod-yard-run-admin" {
  project = local.gcp-project-id
  role    = "roles/run.admin"
  member  = "serviceAccount:${google_service_account.xj-prod-yard.email}"
}

#
# GitHub Actions
#

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_service_account
resource "google_service_account" "github-actions" {
  project      = local.gcp-project-id
  account_id   = "github-actions"
  display_name = "XJ GitHub Actions"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "github-actions-artifact-registry" {
  project = local.gcp-project-id
  role    = "roles/artifactregistry.serviceAgent"
  member  = "serviceAccount:${google_service_account.github-actions.email}"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "github-actions-compute" {
  project = local.gcp-project-id
  role    = "roles/compute.serviceAgent"
  member  = "serviceAccount:${google_service_account.github-actions.email}"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "github-actions-container" {
  project = local.gcp-project-id
  role    = "roles/container.serviceAgent"
  member  = "serviceAccount:${google_service_account.github-actions.email}"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "github-actions-container-registry" {
  project = local.gcp-project-id
  role    = "roles/containerregistry.ServiceAgent"
  member  = "serviceAccount:${google_service_account.github-actions.email}"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "github-actions-iam" {
  project = local.gcp-project-id
  role    = "roles/iam.serviceAccountUser"
  member  = "serviceAccount:${google_service_account.github-actions.email}"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "github-actions-storage-admin" {
  project = local.gcp-project-id
  role    = "roles/storage.admin"
  member  = "serviceAccount:${google_service_account.github-actions.email}"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "github-actions-run-admin" {
  project = local.gcp-project-id
  role    = "roles/run.admin"
  member  = "serviceAccount:${google_service_account.github-actions.email}"
}

