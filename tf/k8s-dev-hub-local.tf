# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/service
resource "kubernetes_service" "xj-dev-hub-local" {
  metadata {
    namespace = kubernetes_namespace.xj-dev.metadata.0.name
    name      = "hub-local"
    labels = {
      k8s-app = "hub-local"
    }
  }
  spec {
    selector = {
      k8s-app = "hub-local"
    }
    port {
      port        = 8080
      target_port = 3001
    }

    type = "LoadBalancer"
  }
}

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/deployment
resource "kubernetes_deployment" "xj-dev-hub-local" {
  metadata {
    namespace = kubernetes_namespace.xj-dev.metadata.0.name
    name      = "hub-local"
    labels = {
      k8s-app = "hub-local"
    }
  }
  spec {
    replicas = 1

    selector {
      match_labels = {
        k8s-app = "hub-local"
      }
    }

    template {
      metadata {
        labels = {
          k8s-app = "hub-local"
        }
      }

      spec {
        container {
          name  = "hub"
          image = "${aws_ecr_repository.xj-dev-hub.repository_url}:latest"

          env {
            name  = "TELEMETRY_NAMESPACE"
            value = "Dev/Hub"
          }

          env {
            name  = "AWS_DEFAULT_REGION"
            value = local.aws-region
          }

          env {
            name  = "APP_BASE_URL"
            value = "http://localhost:3000/"
          }

          env {
            name  = "AWS_SECRET_NAME"
            value = aws_secretsmanager_secret.xj-dev-env.name
          }

          liveness_probe {
            http_get {
              path = "/healthz"
              port = 3001
            }

            initial_delay_seconds = 30
            period_seconds        = 15
          }
        }
      }
    }
  }
}

