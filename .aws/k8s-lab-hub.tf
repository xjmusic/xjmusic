# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/service
resource "kubernetes_service" "xj-prod-lab-hub" {
  metadata {
    namespace = kubernetes_namespace.xj-prod-lab.metadata.0.name
    name      = "hub"
    labels = {
      k8s-app = "hub"
    }
  }
  spec {
    selector = {
      k8s-app = "hub"
    }
    port {
      port        = 8080
      target_port = 3000
    }

    type = "LoadBalancer"
  }
}

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/deployment
resource "kubernetes_deployment" "xj-prod-lab-hub" {
  metadata {
    namespace = kubernetes_namespace.xj-prod-lab.metadata.0.name
    name      = "hub"
    labels = {
      k8s-app = "hub"
    }
  }
  spec {
    replicas = 1

    selector {
      match_labels = {
        k8s-app = "hub"
      }
    }

    template {
      metadata {
        labels = {
          k8s-app = "hub"
        }
      }

      spec {
        container {
          name  = "hub"
          image = "${aws_ecr_repository.xj-hub.repository_url}:latest"

          env {
            name  = "AWS_DEFAULT_REGION"
            value = local.aws-region
          }

          env {
            name  = "AWS_SECRET_NAME"
            value = aws_secretsmanager_secret.xj-prod-env.name
          }

          env {
            name  = "APP_BASE_URL"
            value = "https://lab.xj.io/"
          }

          liveness_probe {
            http_get {
              path = "/healthz"
              port = 3000
            }

            initial_delay_seconds = 30
            period_seconds        = 15
          }
        }
      }
    }
  }
}

