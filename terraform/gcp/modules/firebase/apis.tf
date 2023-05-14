# Enables the Cloud Run API
resource "google_project_service" "firestore_api" {
  project = var.project_id
  service = "firestore.googleapis.com"

  disable_on_destroy = false
}
