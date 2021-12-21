# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/service
resource "kubernetes_service" "xj-dev-postgres" {
  metadata {
    namespace = kubernetes_namespace.xj-dev.metadata.0.name
    name      = "postgres"
    labels = {
      k8s-app = "postgres"
    }
  }
  spec {
    selector = {
      k8s-app = "postgres"
    }
    port {
      port        = 5432
      target_port = 5432
      protocol    = "TCP"
    }

    type = "ClusterIP"
  }
}

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/stateful_set
resource "kubernetes_stateful_set" "xj-dev-postgres" {
  metadata {
    namespace = kubernetes_namespace.xj-dev.metadata.0.name
    name      = "postgres"
    labels = {
      k8s-app                           = "postgres"
      "kubernetes.io/cluster-service"   = "true"
      "addonmanager.kubernetes.io/mode" = "Reconcile"
    }
  }

  spec {
    pod_management_policy  = "Parallel"
    replicas               = 1
    revision_history_limit = 5

    selector {
      match_labels = {
        k8s-app = "postgres"
      }
    }

    service_name = "postgres"

    template {
      metadata {
        labels = {
          k8s-app = "postgres"
        }
        annotations = {}
      }

      spec {
        container {
          name              = "postgres"
          image             = "postgres:13"
          image_pull_policy = "Always"

          env_from {
            config_map_ref {
              name = kubernetes_config_map.xj-dev-postgres.metadata.0.name
            }
          }

          volume_mount {
            name       = local.xj-dev-postgres-pvc-name
            mount_path = "/var/lib/postgresql/data"
          }
        }

        termination_grace_period_seconds = 300
      }
    }

    update_strategy {
      type = "RollingUpdate"

      rolling_update {
        partition = 1
      }
    }

    volume_claim_template {
      metadata {
        name = local.xj-dev-postgres-pvc-name
      }
      spec {
        storage_class_name = "efs-sc"
        access_modes = [
          "ReadWriteMany"
        ]
        resources {
          requests = {
            storage = "16Gi"
          }
        }
        volume_name = kubernetes_persistent_volume.xj-dev-postgres-pv.metadata.0.name
      }
    }
  }
}

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/persistent_volume
resource "kubernetes_persistent_volume" "xj-dev-postgres-pv" {
  metadata {
    name = "xj-dev-postgres-pv"
  }
  spec {
    capacity = {
      storage = "16Gi"
    }
    storage_class_name = "efs-sc"
    access_modes = [
      "ReadWriteMany"
    ]
    persistent_volume_source {
      //noinspection HCLUnknownBlockType
      csi {
        driver        = "efs.csi.aws.com"
        volume_handle = aws_efs_file_system.xj-dev-postgres-efs.id
      }
    }
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/efs_file_system
resource "aws_efs_file_system" "xj-dev-postgres-efs" {
  creation_token = "xj-dev-postgres-efs"

  tags = {
    Name = "xj-dev-postgres-efs"
  }
}

resource "aws_efs_mount_target" "xj-dev-postgres-efs-mount0" {
  subnet_id      = module.xj-prod-vpc.private_subnets[0]
  file_system_id = aws_efs_file_system.xj-dev-postgres-efs.id
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/efs_mount_target
resource "aws_efs_mount_target" "xj-dev-postgres-efs-mount1" {
  subnet_id      = module.xj-prod-vpc.private_subnets[1]
  file_system_id = aws_efs_file_system.xj-dev-postgres-efs.id
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/efs_mount_target
resource "aws_efs_mount_target" "xj-dev-postgres-efs-mount2" {
  subnet_id      = module.xj-prod-vpc.private_subnets[2]
  file_system_id = aws_efs_file_system.xj-dev-postgres-efs.id
}

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/config_map
resource "kubernetes_config_map" "xj-dev-postgres" {
  metadata {
    namespace = kubernetes_namespace.xj-dev.metadata.0.name
    name      = "xj-dev-postgres-config"
    labels = {
      k8s-app = "postgres"
    }
    annotations = {}
  }
  data = {
    POSTGRES_DB       = "xj_dev"
    POSTGRES_USER     = "postgres"
    POSTGRES_PASSWORD = "postgres"
    PGDATA            = "/var/lib/postgresql/data"
  }
}
