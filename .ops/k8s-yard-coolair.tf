# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# Nexus
# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/deployment
resource "kubernetes_deployment" "xj-prod-yard-coolair-nexus" {
  metadata {
    namespace = kubernetes_namespace.xj-prod-yard.metadata.0.name
    name      = "coolair-nexus"
    labels = {
      k8s-app = "coolair-nexus"
    }
  }
  spec {
    replicas = 1

    strategy {
      type = "Recreate"
    }

    selector {
      match_labels = {
        k8s-app = "coolair-nexus"
      }
    }

    template {
      metadata {
        labels = {
          k8s-app = "coolair-nexus"
        }
      }

      spec {
        container {
          name  = "coolair-nexus"
          image = "${aws_ecr_repository.xj-nexus.repository_url}:latest"

          resources {
            requests = {
              cpu    = "2000m"
              memory = "3Gi"
            }
            limits = {
              cpu    = "3000m"
              memory = "6Gi"
            }
          }

          env {
            name  = "TELEMETRY_NAMESPACE"
            value = "Yard/CoolAir/Nexus"
          }

          env {
            name  = "AWS_DEFAULT_REGION"
            value = local.aws-region
          }

          env {
            name  = "AWS_SECRET_NAME"
            value = aws_secretsmanager_secret.xj-prod-env.name
          }

          env {
            name = "INGEST_URL"
            //noinspection HttpUrlsUsage
            value = "http://hub.lab.svc.cluster.local:8080/"
          }

          env {
            name  = "BOOTSTRAP_SHIP_KEYS"
            value = "coolair"
          }

          liveness_probe {
            http_get {
              path = "/healthz"
              port = 3000
            }

            initial_delay_seconds = 20
            period_seconds        = 5
          }
        }
      }
    }
  }
}

# Ship
# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/deployment
resource "kubernetes_deployment" "xj-prod-yard-coolair-ship" {
  metadata {
    namespace = kubernetes_namespace.xj-prod-yard.metadata.0.name
    name      = "coolair-ship"
    labels = {
      k8s-app = "coolair-ship"
    }
  }
  spec {
    replicas = 1

    strategy {
      type = "Recreate"
    }

    selector {
      match_labels = {
        k8s-app = "coolair-ship"
      }
    }

    template {
      metadata {
        labels = {
          k8s-app = "coolair-ship"
        }
      }

      spec {
        container {
          name  = "coolair-ship"
          image = "${aws_ecr_repository.xj-ship.repository_url}:latest"

          resources {
            requests = {
              cpu    = "1500m"
              memory = "2Gi"
            }
            limits = {
              cpu    = "2000m"
              memory = "3Gi"
            }
          }

          env {
            name  = "TELEMETRY_NAMESPACE"
            value = "Yard/CoolAir/Ship"
          }

          env {
            name  = "AWS_DEFAULT_REGION"
            value = local.aws-region
          }

          env {
            name  = "AWS_SECRET_NAME"
            value = aws_secretsmanager_secret.xj-prod-env.name
          }

          env {
            name = "INGEST_URL"
            //noinspection HttpUrlsUsage
            value = "http://hub.lab.svc.cluster.local:8080/"
          }

          env {
            name  = "BOOTSTRAP_SHIP_KEYS"
            value = "coolair"
          }

          env {
            name  = "SHIP_FRAGMENT_CONSTRUCTION_METHOD"
            value = "manual"
          }

          liveness_probe {
            http_get {
              path = "/healthz"
              port = 3000
            }

            initial_delay_seconds = 20
            period_seconds        = 5
          }
        }
      }
    }
  }
}
