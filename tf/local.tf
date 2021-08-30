# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

locals {
  aws-account-id            = "027141088039"
  aws-region                = "us-east-1"
  xj-prod-cluster-name      = "xj-prod-${random_string.xj-prod-vpc-suffix.result}"
  xj-prod-postgres-pvc-name = "xj-prod-postgres-pvc"
  xj-dev-postgres-pvc-name  = "xj-dev-postgres-pvc"
}

