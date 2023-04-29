# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

module "fabrication_bump_deep" {
  source = "./modules/fabrication-legacy"

  notification_channels = local.gcp-alert-notification-channels
  ship_key              = "bump_deep"
  display_name          = "Bump/Deep"
}

module "fabrication_bump_chill" {
  source = "./modules/fabrication-legacy"

  notification_channels = local.gcp-alert-notification-channels
  ship_key              = "bump_chill"
  display_name          = "Bump/Chill"
}

module "fabrication_space_flow" {
  source = "./modules/fabrication-legacy"

  notification_channels = local.gcp-alert-notification-channels
  ship_key              = "space_flow"
  display_name          = "Space/Flow"
}

module "fabrication_space_binaural" {
  source = "./modules/fabrication-legacy"

  notification_channels = local.gcp-alert-notification-channels
  ship_key              = "space_binaural"
  display_name          = "Space/Binaural"
}

module "fabrication_slaps_lofi" {
  source = "./modules/fabrication-legacy"

  notification_channels = local.gcp-alert-notification-channels
  ship_key              = "slaps_lofi"
  display_name          = "Slaps/Lofi"
}

module "fabrication_test_serverless" {
  source = "./modules/fabrication"

  ship_key                      = "test_serverless"
  nexus_resources_limits_cpu    = 2
  nexus_resources_limits_memory = "4Gi"
  ship_resources_limits_cpu     = 2
  ship_resources_limits_memory  = "4Gi"
  display_name                  = "Test/Serverless"
  nexus_image                   = "gcr.io/xj-vpc-host-prod/nexus:latest"
  ship_image                    = "gcr.io/xj-vpc-host-prod/ship:latest"
  audio_base_url                = "https://audio.xj.io/"
  audio_file_bucket             = "xj-prod-audio"
  audio_upload_url              = "https://xj-prod-audio.s3.amazonaws.com/"
  aws_default_region            = "us-east-1"
  environment                   = "prod"
  player_base_url               = "https://play.xj.io/"
  ship_base_url                 = "https://ship.xj.io/"
  ship_bucket                   = "xj-prod-ship"
  stream_bucket                 = "xj-prod-stream"
  stream_base_url               = "https://stream.xj.io/"
  notification_channels         = local.gcp-alert-notification-channels
  project_id                    = local.gcp-project-id
  region                        = local.gcp-region
  secret_id__aws_access_key_id  = google_secret_manager_secret.secret-prod-aws-access-key-id.secret_id
  secret_id__aws_secret_key     = google_secret_manager_secret.secret-prod-aws-secret-key.secret_id
  service_account_email         = module.xj-prod-yard-sa.service_account_email
}

