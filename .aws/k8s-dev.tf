# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/namespace
resource "kubernetes_namespace" "xj-dev" {
  metadata {
    annotations = {
      name = "dev"
    }

    labels = {
      Environment = "dev"
    }

    name = "dev"
  }
}

