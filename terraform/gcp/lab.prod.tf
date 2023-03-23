# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/sql_database_instance

# Serverless operations (Spring Boot Refactoring)
# https://www.pivotaltracker.com/story/show/184580235

/*
module "prod_lab" {
  source = "./modules/hub"

  image                           = "gcr.io/xj-vpc-host-prod/prod/hub:latest"
  service_name                    = "xj-prod-lab-hub"
  app_base_url                    = "https://lab.xj.io/"
  audio_base_url                  = "https://audio.xj.io/"
  audio_file_bucket               = "xj-prod-audio"
  audio_upload_url                = "https://xj-prod-audio.s3.amazonaws.com/"
  aws_default_region              = "us-east-1"
  environment                     = "production"
  player_base_url                 = "https://play.xj.io/"
  redis_host                      = module.lab_redis.database_prod
  redis_port                      = 6379
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

module "lab_postgres" {
  source = "./modules/postgres"

  region                = local.gcp-region
  project               = local.gcp-project-id
  service_account_email = google_service_account.xj-dev-yard.email
}

module "lab_redis" {
  source = "./modules/redis"

  region                = local.gcp-region
  project               = local.gcp-project-id
  service_account_email = google_service_account.xj-dev-yard.email
}

