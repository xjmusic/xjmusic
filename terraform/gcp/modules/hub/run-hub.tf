# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# Serverless operations (Spring Boot Refactoring)
# https://www.pivotaltracker.com/story/show/184580235

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/iam_policy
data "google_iam_policy" "noauth" {
  binding {
    role    = "roles/run.invoker"
    members = [
      "allUsers",
    ]
  }
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/cloud_run_service_iam_policy
resource "google_cloud_run_service_iam_policy" "noauth" {
  location = google_cloud_run_v2_service.hub.location
  project  = google_cloud_run_v2_service.hub.project
  service  = google_cloud_run_v2_service.hub.name

  policy_data = data.google_iam_policy.noauth.policy_data
  depends_on  = [google_project_service.run_api]
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/cloud_run_v2_service
resource "google_cloud_run_v2_service" "hub" {
  name     = var.service_name
  location = var.region
  project  = var.project_id
  provider = google-beta

  template {
    scaling {
      min_instance_count = var.sleep_when_idle ? 0 : 1
      max_instance_count = 1
    }
    service_account = var.service_account_email
    containers {
      env {
        name  = "APP_BASE_URL"
        value = var.app_base_url
      }
      env {
        name  = "ENVIRONMENT"
        value = var.environment
      }
      env {
        name  = "AUDIO_BASE_URL"
        value = var.audio_base_url
      }
      env {
        name  = "AUDIO_FILE_BUCKET"
        value = var.audio_file_bucket
      }
      env {
        name  = "AUDIO_UPLOAD_URL"
        value = var.audio_upload_url
      }
      env {
        name  = "AWS_DEFAULT_REGION"
        value = var.aws_default_region
      }
      env {
        name  = "PLAYER_BASE_URL"
        value = var.player_base_url
      }
      env {
        name  = "POSTGRES_DATABASE"
        value = var.postgres_database
      }
      env {
        name  = "GCP_CLOUD_SQL_INSTANCE"
        value = var.postgres_gcp_cloud_sql_instance
      }
      env {
        name  = "GCP_REGION"
        value = var.region
      }
      env {
        name  = "GCP_PROJECT_ID"
        value = var.project_id
      }
      env {
        name  = "GCP_SERVICE_ACCOUNT_EMAIL"
        value = var.service_account_email
      }
      env {
        name  = "SHIP_BASE_URL"
        value = var.ship_base_url
      }
      env {
        name  = "SHIP_BUCKET"
        value = var.ship_bucket
      }
      env {
        name  = "SERVICE_NEXUS_IMAGE"
        value = var.nexus_image
      }
      env {
        name = "INGEST_TOKEN_VALUE"
        value_source {
          secret_key_ref {
            version = "latest"
            secret  = var.secret_id__ingest_token_value
          }
        }
      }
      env {
        name = "AWS_ACCESS_KEY_ID"
        value_source {
          secret_key_ref {
            version = "latest"
            secret  = var.secret_id__aws_access_key_id
          }
        }
      }
      env {
        name = "AWS_SECRET_KEY"
        value_source {
          secret_key_ref {
            version = "latest"
            secret  = var.secret_id__aws_secret_key
          }
        }
      }
      env {
        name = "GOOGLE_CLIENT_ID"
        value_source {
          secret_key_ref {
            version = "latest"
            secret  = var.secret_id__google_client_id
          }
        }
      }
      env {
        name = "GOOGLE_CLIENT_SECRET"
        value_source {
          secret_key_ref {
            version = "latest"
            secret  = var.secret_id__google_client_secret
          }
        }
      }
      env {
        name = "POSTGRES_PASS"
        value_source {
          secret_key_ref {
            version = "latest"
            secret  = var.secret_id__postgres_password
          }
        }
      }
      env {
        name = "POSTGRES_USER"
        value_source {
          secret_key_ref {
            version = "latest"
            secret  = var.secret_id__postgres_username
          }
        }
      }
      image = var.image
      //noinspection HCLUnknownBlockType
      startup_probe {
        initial_delay_seconds = 30
        timeout_seconds       = 2
        period_seconds        = 3
        failure_threshold     = 1
        //noinspection HCLUnknownBlockType
        http_get {
          path = "/healthz"
        }
      }
      //noinspection HCLUnknownBlockType
      liveness_probe {
        timeout_seconds = 2
        //noinspection HCLUnknownBlockType
        http_get {
          path = "/healthz"
        }
      }
      resources {
        cpu_idle = var.sleep_when_idle
        limits   = {
          cpu    = var.resources_limits_cpu
          memory = var.resources_limits_memory
        }
      }
    }
  }

  lifecycle {
    //noinspection HILUnresolvedReference
    ignore_changes = [
      client,
      client_version,
      annotations,
      template[0].annotations,
    ]
  }

  depends_on = [google_project_service.run_api]
}

