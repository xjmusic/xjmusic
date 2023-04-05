# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

output "region" {
  description = "AWS region"
  value       = local.aws-region
}

output "cdn_catalog_dev" {
  description = "CloudFront distribution ID for catalog dev"
  value       = module.xj-dev-catalog.cdn_id
}

output "cdn_catalog_prod" {
  description = "CloudFront distribution ID for catalog prod"
  value       = module.xj-catalog.cdn_id
}

output "cdn_content_dev" {
  description = "CloudFront distribution ID for content dev"
  value       = module.xj-dev-content.cdn_id
}

output "cdn_content_prod" {
  description = "CloudFront distribution ID for content prod"
  value       = module.xj-content.cdn_id
}

output "cdn_io_xj_dev" {
  description = "CloudFront distribution ID for xj.io dev"
  value       = module.xj-dev.cdn_id
}

output "cdn_io_xj_prod" {
  description = "CloudFront distribution ID for xj.io prod"
  value       = module.xj-io.cdn_id
}

output "cdn_io_xj_help_dev" {
  description = "CloudFront distribution ID for help.xj.io dev"
  value       = module.xj-dev-help.cdn_id
}

output "cdn_io_xj_help_prod" {
  description = "CloudFront distribution ID for help.xj.io prod"
  value       = module.xj-help.cdn_id
}

output "cdn_io_xj_status_dev" {
  description = "CloudFront distribution ID for status.xj.io dev"
  value       = module.xj-dev-status.cdn_id
}

output "cdn_io_xj_status_prod" {
  description = "CloudFront distribution ID for status.xj.io prod"
  value       = module.xj-status.cdn_id
}

output "cdn_works_aircraft_dev" {
  description = "CloudFront distribution ID for dev.aircraft.works"
  value       = module.aircraft-works-dev.cdn_id
}

output "cdn_works_aircraft_prod" {
  description = "CloudFront distribution ID for aircraft.works"
  value       = module.aircraft-works.cdn_id
}

output "cdn_net_uxresearchgroup_dev" {
  description = "CloudFront distribution ID for dev.uxresearchgroup.net"
  value       = module.uxrg-dev.cdn_id
}

output "cdn_net_uxresearchgroup_prod" {
  description = "CloudFront distribution ID for uxresearchgroup.net"
  value       = module.uxrg-prod.cdn_id
}

output "cdn_com_xjmusic_podcast_dev" {
  description = "CloudFront distribution ID for podcast.dev.xjmusic.com"
  value       = module.xjmusic-com-dev-podcast.cdn_id
}

output "cdn_com_xjmusic_podcast_prod" {
  description = "CloudFront distribution ID for podcast.xjmusic.com"
  value       = module.xjmusic-com-podcast.cdn_id
}
