# Enables the Google Cloud Redis API
resource "google_project_service" "redis_api" {
  service = "redis.googleapis.com"

  disable_on_destroy = false
}
