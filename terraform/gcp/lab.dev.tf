# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/sql_database_instance

# Serverless operations (Spring Boot Refactoring)
# https://www.pivotaltracker.com/story/show/184580235

module "dev_lab" {
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
  postgres_pass                   = module.lab_postgres.password
  postgres_user                   = module.lab_postgres.user
  project                         = local.gcp-project-id
  redis_host                      = module.lab_redis.host
  redis_port                      = module.lab_redis.port
  region                          = local.gcp-region
  secret_id__aws_access_key_id    = module.secret-dev-aws-access-key-id.secret_id
  secret_id__aws_secret_key       = module.secret-dev-aws-secret-key.secret_id
  secret_id__google_client_id     = module.secret-dev-google-client-id.secret_id
  secret_id__google_client_secret = module.secret-dev-google-client-secret.secret_id
  service_account_email           = google_service_account.xj-dev-yard.email
}
