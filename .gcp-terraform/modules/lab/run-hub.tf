# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/secret_manager_secret_iam#google_cloud_run_service_iam_member
resource "google_cloud_run_service_iam_member" "xj-prod-yard-run-invoker" {
  service  = google_cloud_run_service.xj-yard-bump_chill_serverless-nexus.name
  location = google_cloud_run_service.xj-yard-bump_chill_serverless-nexus.location
  role     = "roles/run.invoker"
  member   = "serviceAccount:${var.service_account_email}"
}


# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/cloud_run_service
resource "google_cloud_run_service" "xj-yard-bump_chill_serverless-nexus" {
  name     = "yard-bump_chill_serverless-nexus"
  location = var.region

  template {
    spec {
      service_account_name = var.service_account_email
      containers {
        env {
          name  = SHIP_KEY
          value = "bump_chill_serverless"
        }
        env {
          name  = SHIP_CHUNK_AUDIO_ENCODER
          value = "mp3"
        }
        env {
          name  = SHIP_CHUNK_CONTENT_TYPE
          value = "audio/mpeg"
        }
        env {
          name  = AUDIO_BASE_URL
          value = "https://audio.xj.io/"
        }
        env {
          name  = AUDIO_FILE_BUCKET
          value = "xj-prod-audio"
        }
        env {
          name  = AUDIO_UPLOAD_URL
          value = "https://xj-prod-audio.s3.amazonaws.com/"
        }
        env {
          name  = AWS_ACCESS_KEY_ID
          value = "AKIAI6WWJJMATIYGTTLQ"
        }
        env {
          name  = AWS_DEFAULT_REGION
          value = us-east-1
        }
        env {
          name  = AWS_SECRET_KEY
          value = "..."
        }
        env {
          name  = AWS_SNS_TOPIC_ARN
          value = "..."
        }
        env {
          name  = ENVIRONMENT
          value = production
        }
        env {
          name  = GOOGLE_CLIENT_ID
          value = "..."
        }
        env {
          name  = GOOGLE_CLIENT_SECRET
          value = "..."
        }
        env {
          name  = INGEST_TOKEN_VALUE
          value = "..."
        }
        env {
          name  = PLAYER_BASE_URL
          value = "https://play.xj.io/"
        }
        env {
          name  = POSTGRES_DATABASE
          value = xj_dev
        }
        env {
          name  = POSTGRES_HOST
          value = "postgres.lab.svc.cluster.local"
        }
        env {
          name  = POSTGRES_PASS
          value = postgres
        }
        env {
          name  = POSTGRES_PORT
          value = 5432
        }
        env {
          name  = POSTGRES_USER
          value = postgres
        }
        env {
          name  = REDIS_HOST
          value = "redis.lab.svc.cluster.local"
        }
        env {
          name  = REDIS_PORT
          value = 6379
        }
        env {
          name  = SHIP_BASE_URL
          value = "https://ship.xj.io/"
        }
        env {
          name  = SHIP_BUCKET
          value = xj-prod-ship
        }
        env {
          name  = STREAM_BUCKET
          value = xj-prod-stream
        }
        env {
          name  = STREAM_BASE_URL
          value = "https://stream.xj.io/"
        }

        image           = "gcr.io/xj-vpc-host-prod/nexus:latest"
        imagePullPolicy = Always
        liveness_probe  = {
          failureThreshold = 3
          http_get         = {
            path   = "/healthz"
            port   = 8080
            scheme = HTTP
          }
          initialDelaySeconds = 30
          periodSeconds       = 5
          successThreshold    = 1
          timeoutSeconds      = 1
        }
        name = yard-bump_chill_serverless-nexus
        resources {
          requests = {
            cpu    = 1.25
            memory = "3G"
          }
          limits = {
            cpu    = 2.5
            memory = "5Gi"
          }
        }
        terminationMessagePath   = "/dev/termination-log"
        terminationMessagePolicy = File
      }
    }
  }

  traffic {
    percent         = 100
    latest_revision = true
  }
}

