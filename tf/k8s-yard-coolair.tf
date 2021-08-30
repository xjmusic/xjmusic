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
            name = "CHAIN_BOOTSTRAP_JSON"
            value = jsonencode({
              chain = {
                accountId = "14e58a74-16eb-11ea-8a37-27b15e9fd30c",
                embedKey  = "coolair",
                name      = "Cool Air",
                state     = "Fabricate",
                type      = "Production",
                config = join("\n", [
                  "outputContainer = OGG",
                  "outputEncoding = PCM_FLOAT",
                ])
              },
              chainBindings = [

                // Cool Macro Programs
                {
                  targetId = "55ab659c-fb3c-11eb-85f4-87f8295902e9"
                  type     = "Library"
                },

                // Cool Main Programs
                {
                  targetId = "1509102a-16eb-11ea-8a37-4764c2a771db"
                  type     = "Library"
                },

                // Cool Drum Instruments
                {
                  targetId = "3a11cdf4-fb1d-11eb-bda4-8f35e39004fe"
                  type     = "Library"
                },

                // Cool Rhythm Programs
                {
                  targetId = "9d966418-fb20-11eb-a2de-172ecc754afd"
                  type     = "Library"
                },

                // Cool Pad Detail Programs
                {
                  targetId = "d124e460-fb22-11eb-b9ed-a36c2b39adb7"
                  type     = "Library"
                },

                // Cool Stab Detail Programs
                {
                  targetId = "ff8ce21c-fb22-11eb-981d-37143596a20d"
                  type     = "Library"
                },

                // Cool Stripe Detail Programs
                {
                  targetId = "ff8def18-fb22-11eb-928c-2b02f39362fc"
                  type     = "Library"
                },

                // Cool Sticky Detail Programs
                {
                  targetId = "55461ec6-fb23-11eb-ad24-972f33ad9115"
                  type     = "Library"
                },

                // Cool Bass Detail Programs
                {
                  targetId = "5545dcfe-fb23-11eb-80a6-4372a28aca52"
                  type     = "Library"
                },

                // Cool Pad Instruments
                {
                  targetId = "ede3384e-fb23-11eb-80a6-4f4aa09cf3ef"
                  type     = "Library"
                },

                // Cool Stab Instruments
                {
                  targetId = "ede3b224-fb23-11eb-ad24-1bf1b475662e"
                  type     = "Library"
                },

                // Cool Stripe Instruments
                {
                  targetId = "2f621894-fb24-11eb-80a6-63ed42857ac1"
                  type     = "Library"
                },

                // Cool Sticky Instruments
                {
                  targetId = "460830b0-fb24-11eb-80a6-7b4f92dccaea"
                  type     = "Library"
                },

                // Cool Bass Instruments
                {
                  targetId = "53b95f54-fb24-11eb-ad24-8f1c28086c69"
                  type     = "Library"
                },

              ]
            })
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
