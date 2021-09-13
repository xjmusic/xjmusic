# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/deployment
resource "kubernetes_deployment" "xj-prod-yard-coolair" {
  metadata {
    namespace = kubernetes_namespace.xj-prod-yard.metadata.0.name
    name      = "coolair"
    labels = {
      k8s-app = "coolair"
    }
  }
  spec {
    replicas = 1

    strategy {
      type = "Recreate"
    }

    selector {
      match_labels = {
        k8s-app = "coolair"
      }
    }

    template {
      metadata {
        labels = {
          k8s-app = "coolair"
        }
      }

      spec {
        container {
          name  = "coolair"
          image = "${aws_ecr_repository.xj-nexus.repository_url}:latest"

          resources {
            requests = {
              cpu    = "2000m"
              memory = "3.5Gi"
            }
            limits = {
              cpu    = "2000m"
              memory = "4Gi"
            }
          }

          env {
            name  = "TELEMETRY_NAMESPACE"
            value = "Yard/CoolAir"
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
            name  = "BOOTSTRAP_TEMPLATE_ID"
            value = "eb0cf5ce-09ba-11ec-8016-d72d36e2270c"
          }

          liveness_probe {
            http_get {
              path = "/healthz"
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
