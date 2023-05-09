# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

module "fabrication_bump_deep" {
  source = "./modules/fabrication"

  ship_key                      = "bump_deep"
  display_name                  = "Bump/Deep"
  region                        = local.gcp-region
  project_id                    = local.gcp-project-id
  notification_channels         = local.gcp-alert-notification-channels
  secret_id__aws_access_key_id  = google_secret_manager_secret.secret-prod-aws-access-key-id.secret_id
  secret_id__aws_secret_key     = google_secret_manager_secret.secret-prod-aws-secret-key.secret_id
  service_account_email         = module.xj-prod-yard-sa.service_account_email
}

module "fabrication_bump_chill" {
  source = "./modules/fabrication"

  ship_key                      = "bump_chill"
  display_name                  = "Bump/Chill"
  region                        = local.gcp-region
  project_id                    = local.gcp-project-id
  notification_channels         = local.gcp-alert-notification-channels
  secret_id__aws_access_key_id  = google_secret_manager_secret.secret-prod-aws-access-key-id.secret_id
  secret_id__aws_secret_key     = google_secret_manager_secret.secret-prod-aws-secret-key.secret_id
  service_account_email         = module.xj-prod-yard-sa.service_account_email
}

module "fabrication_space_flow" {
  source = "./modules/fabrication"

  ship_key                      = "space_flow"
  display_name                  = "Space/Flow"
  region                        = local.gcp-region
  project_id                    = local.gcp-project-id
  notification_channels         = local.gcp-alert-notification-channels
  secret_id__aws_access_key_id  = google_secret_manager_secret.secret-prod-aws-access-key-id.secret_id
  secret_id__aws_secret_key     = google_secret_manager_secret.secret-prod-aws-secret-key.secret_id
  service_account_email         = module.xj-prod-yard-sa.service_account_email
}

module "fabrication_space_binaural" {
  source = "./modules/fabrication"

  ship_key                      = "space_binaural"
  display_name                  = "Space/Binaural"
  region                        = local.gcp-region
  project_id                    = local.gcp-project-id
  notification_channels         = local.gcp-alert-notification-channels
  secret_id__aws_access_key_id  = google_secret_manager_secret.secret-prod-aws-access-key-id.secret_id
  secret_id__aws_secret_key     = google_secret_manager_secret.secret-prod-aws-secret-key.secret_id
  service_account_email         = module.xj-prod-yard-sa.service_account_email
}

module "fabrication_slaps_lofi" {
  source = "./modules/fabrication"

  ship_key                      = "slaps_lofi"
  display_name                  = "Slaps/Lofi"
  region                        = local.gcp-region
  project_id                    = local.gcp-project-id
  notification_channels         = local.gcp-alert-notification-channels
  secret_id__aws_access_key_id  = google_secret_manager_secret.secret-prod-aws-access-key-id.secret_id
  secret_id__aws_secret_key     = google_secret_manager_secret.secret-prod-aws-secret-key.secret_id
  service_account_email         = module.xj-prod-yard-sa.service_account_email
}

module "fabrication_test_serverless" {
  source = "./modules/fabrication"

  ship_key                      = "test_serverless"
  display_name                  = "Test/Serverless"
  region                        = local.gcp-region
  project_id                    = local.gcp-project-id
  notification_channels         = local.gcp-alert-notification-channels
  secret_id__aws_access_key_id  = google_secret_manager_secret.secret-prod-aws-access-key-id.secret_id
  secret_id__aws_secret_key     = google_secret_manager_secret.secret-prod-aws-secret-key.secret_id
  service_account_email         = module.xj-prod-yard-sa.service_account_email
}

