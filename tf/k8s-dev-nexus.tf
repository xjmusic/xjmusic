# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/service
resource "kubernetes_service" "xj-dev-nexus" {
  metadata {
    namespace = kubernetes_namespace.xj-dev.metadata.0.name
    name      = "nexus"
    labels = {
      k8s-app = "nexus"
    }
  }
  spec {
    selector = {
      k8s-app = "nexus"
    }
    port {
      port        = 8080
      target_port = 3002
    }

    type = "LoadBalancer"
  }
}

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/deployment
resource "kubernetes_deployment" "xj-dev-nexus" {
  metadata {
    namespace = kubernetes_namespace.xj-dev.metadata.0.name
    name      = "nexus"
    labels = {
      k8s-app = "nexus"
    }
  }
  spec {
    replicas = 1

    strategy {
      type = "Recreate"
    }

    selector {
      match_labels = {
        k8s-app = "nexus"
      }
    }

    template {
      metadata {
        labels = {
          k8s-app = "nexus"
        }
      }

      spec {
        container {
          name  = "nexus"
          image = "${aws_ecr_repository.xj-dev-nexus.repository_url}:latest"

          env {
            name  = "TELEMETRY_NAMESPACE"
            value = "Dev/Nexus"
          }

          env {
            name  = "AWS_DEFAULT_REGION"
            value = local.aws-region
          }

          env {
            name  = "AWS_SECRET_NAME"
            value = aws_secretsmanager_secret.xj-dev-env.name
          }

          env {
            name = "INGEST_URL"
            //noinspection HttpUrlsUsage
            value = "http://hub.dev.svc.cluster.local:8080/"
          }

          liveness_probe {
            http_get {
              path = "/-/health"
              port = 3002
            }

            initial_delay_seconds = 20
            period_seconds        = 5
          }
        }
      }
    }
  }
}
