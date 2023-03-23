# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# Production templates are run on serverless GCP cloud run for optimal cost
# https://www.pivotaltracker.com/story/show/184580235

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret_iam#google_cloud_run_service_iam_member
resource "google_cloud_run_service_iam_member" "xj-yard-bump-chill-serverless-ship-invoker" {
  service  = google_cloud_run_service.xj-yard-bump-chill-serverless-ship.name
  location = google_cloud_run_service.xj-yard-bump-chill-serverless-ship.location
  role     = "roles/run.invoker"
  member   = "serviceAccount:${var.service_account_email}"
  project  = var.project
}


# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/cloud_run_service
resource "google_cloud_run_service" "xj-yard-bump-chill-serverless-ship" {
  name     = "yard-bump-chill-serverless-ship"
  location = var.region
  project  = var.project
  provider = google-beta

  template {
    metadata {
      annotations = {
        "run.googleapis.com/cpu-throttling" = false
      }
    }
    spec {
      service_account_name = var.service_account_email
      containers {
        env {
          name  = "SHIP_KEY"
          value = "bump_chill_serverless"
        }
        env {
          name  = "SHIP_CHUNK_AUDIO_ENCODER"
          value = "mp3"
        }
        env {
          name  = "SHIP_CHUNK_CONTENT_TYPE"
          value = "audio/mpeg"
        }
        env {
          name  = "AWS_DEFAULT_REGION"
          value = "us-east-1"
        }
        env {
          name = "AWS_ACCESS_KEY_ID"
          value_from {
            secret_key_ref {
              key  = "latest"
              name = var.secret_id__aws_access_key_id
            }
          }
        }
        env {
          name = "AWS_SECRET_KEY"
          value_from {
            secret_key_ref {
              key  = "latest"
              name = var.secret_id__aws_secret_key
            }
          }
        }
        env {
          name = "GOOGLE_CLIENT_ID"
          value_from {
            secret_key_ref {
              key  = "latest"
              name = var.secret_id__google_client_id
            }
          }
        }
        env {
          name = "GOOGLE_CLIENT_SECRET"
          value_from {
            secret_key_ref {
              key  = "latest"
              name = var.secret_id__google_client_secret
            }
          }
        }
        env {
          name  = "AWS_SNS_TOPIC_ARN"
          value = "arn:aws:sns:us-east-1:027141088039:xj-prod-chain-fabrication"
        }
        env {
          name  = "ENVIRONMENT"
          value = "production"
        }
        env {
          name  = "SHIP_BASE_URL"
          value = "https://ship.xj.io/"
        }
        env {
          name  = "SHIP_BUCKET"
          value = "xj-prod-ship"
        }
        env {
          name  = "STREAM_BUCKET"
          value = "xj-prod-stream"
        }
        env {
          name  = "STREAM_BASE_URL"
          value = "https://stream.xj.io/"
        }

        image = "gcr.io/xj-vpc-host-prod/dev/ship:latest"
        startup_probe {
          initial_delay_seconds = 30
          timeout_seconds       = 2
          period_seconds        = 3
          failure_threshold     = 1
          tcp_socket {
            port = 8080
          }
        }
        liveness_probe {
          timeout_seconds = 2
          http_get {
            path = "/healthz"
          }
        }
        resources {
          requests = {
            cpu    = 1
            memory = "3G"
          }
          limits = {
            cpu    = 2
            memory = "5Gi"
          }
        }
      }
    }
  }

  traffic {
    percent         = 100
    latest_revision = true
  }

  depends_on = [google_project_service.run_api]
}

