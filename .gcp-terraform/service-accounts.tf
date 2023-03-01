# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_service_account
resource "google_service_account" "xj-prod-yard" {
  account_id   = "xj-prod-yard"
  display_name = "XJ Production Yard"
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_project_iam#google_project_iam_member
resource "google_project_iam_member" "project" {
  project = local.gcp-project-id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${google_service_account.xj-prod-yard.email}"
}
