# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

output "region" {
  description = "AWS region"
  value       = local.aws-region
}

output "xj_prod_cluster_name" {
  description = "Kubernetes Cluster Name"
  value       = local.xj-prod-cluster-name
}

output "aws_availability_zones" {
  description = "All AWS availability zones based on current region"
  value       = data.aws_availability_zones.available.names
}
