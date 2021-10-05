# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/service
resource "kubernetes_service" "xj-dev-redis" {
  metadata {
    namespace = kubernetes_namespace.xj-dev.metadata.0.name
    name      = "redis"
    labels = {
      k8s-app = "redis"
    }
  }
  spec {
    selector = {
      k8s-app = "redis"
    }
    port {
      port        = 6379
      target_port = 6379
      protocol    = "TCP"
    }

    type = "ClusterIP"
  }
}

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/deployment
resource "kubernetes_deployment" "xj-dev-redis" {
  metadata {
    namespace = kubernetes_namespace.xj-dev.metadata.0.name
    name      = "redis"
    labels = {
      k8s-app = "redis"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        k8s-app = "redis"
      }
    }

    template {
      metadata {
        labels = {
          k8s-app = "redis"
        }
      }

      spec {
        container {
          image = "redis:latest"
          name  = "redis"

          command = [
            "redis-server",
            "/redis-master/redis.conf",
          ]

          env {
            name  = "MASTER"
            value = "true"
          }

          volume_mount {
            mount_path = "/redis-master-data"
            name       = "data"
          }

          volume_mount {
            mount_path = "/redis-master"
            name       = "config"
          }
        }

        volume {
          name = "data"
          empty_dir {}
        }

        volume {
          name = "config"
          config_map {
            name = "xj-dev-redis-config"
            items {
              key  = "redis-config"
              path = "redis.conf"
            }
          }
        }
      }
    }
  }

  depends_on = [
    kubernetes_config_map.xj-dev-redis
  ]
}

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/config_map
resource "kubernetes_config_map" "xj-dev-redis" {
  metadata {
    namespace = kubernetes_namespace.xj-dev.metadata.0.name
    name      = "xj-dev-redis-config"
    labels = {
      k8s-app = "redis"
    }
    annotations = {}
  }
  data = {
    redis-config = join("\n", [
      "bind 0.0.0.0"
    ])
  }
}

