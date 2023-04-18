# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

module "lab_postgres" {
  source = "./modules/postgres"

  name                  = "xj-lab-postgres"
  region                = local.gcp-region
  project               = local.gcp-project-id
  service_account_email = google_service_account.xj-dev-yard.email
}

module "lab_dev_hub" {
  source = "./modules/hub"

  image                           = "gcr.io/xj-vpc-host-prod/dev/hub:latest"
  service_name                    = "xj-dev-lab-hub"
  app_base_url                    = "https://lab.dev.xj.io/"
  audio_base_url                  = "https://audio.dev.xj.io/"
  audio_file_bucket               = "xj-dev-audio"
  audio_upload_url                = "https://xj-dev-audio.s3.amazonaws.com/"
  aws_default_region              = "us-east-1"
  environment                     = "development"
  player_base_url                 = "https://play.dev.xj.io/"
  ship_base_url                   = "https://ship.dev.xj.io/"
  ship_bucket                     = "xj-dev-ship"
  postgres_database               = module.lab_postgres.database_dev
  postgres_gcp_cloud_sql_instance = module.lab_postgres.connection_name
  project_id                      = local.gcp-project-id
  region                          = local.gcp-region
  resources_limits_cpu            = 2
  resources_limits_memory         = "4Gi"
  secret_id__aws_access_key_id    = google_secret_manager_secret.secret-dev-aws-access-key-id.secret_id
  secret_id__aws_secret_key       = google_secret_manager_secret.secret-dev-aws-secret-key.secret_id
  secret_id__google_client_id     = google_secret_manager_secret.secret-dev-google-client-id.secret_id
  secret_id__google_client_secret = google_secret_manager_secret.secret-dev-google-client-secret.secret_id
  secret_id__postgres_username    = module.lab_postgres.username_secret_id
  secret_id__postgres_password    = module.lab_postgres.password_secret_id
  service_account_email           = google_service_account.xj-dev-yard.email
}

/*
module "lab_prod_hub" {
  source = "./modules/hub"

  image                           = "gcr.io/xj-vpc-host-prod/hub:latest"
  service_name                    = "xj-prod-lab-hub"
  app_base_url                    = "https://lab.xj.io/"
  audio_base_url                  = "https://audio.xj.io/"
  audio_file_bucket               = "xj-prod-audio"
  audio_upload_url                = "https://xj-prod-audio.s3.amazonaws.com/"
  aws_default_region              = "us-east-1"
  environment                     = "production"
  player_base_url                 = "https://play.xj.io/"
  ship_base_url                   = "https://ship.xj.io/"
  ship_bucket                     = "xj-prod-ship"
  region                          = local.gcp-region
  project                         = local.gcp-project-id
  service_account_email           = google_service_account.xj-prod-yard.email
  postgres_database               = module.lab_postgres.database_prod
  postgres_user                   = module.lab_postgres.user
  postgres_pass                   = module.lab_postgres.password
  secret_id__aws_access_key_id    = module.secret-prod-aws-access-key-id.secret_id
  secret_id__aws_secret_key       = module.secret-prod-aws-secret-key.secret_id
  secret_id__google_client_id     = module.secret-prod-google-client-id.secret_id
  secret_id__google_client_secret = module.secret-prod-google-client-secret.secret_id
  postgres_gcp_cloud_sql_instance = module.lab_postgres.connection_name
}
*/

