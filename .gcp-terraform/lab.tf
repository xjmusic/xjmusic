# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/sql_database_instance

# Serverless operations (Spring Boot Refactoring)
# https://www.pivotaltracker.com/story/show/184580235

module "dev_lab" {
  source = "./modules/hub"

  service_name                    = "xj-dev-lab-hub"
  region                          = local.gcp-region
  project                         = local.gcp-project-id
  service_account_email           = google_service_account.xj-prod-yard.email
  postgres_database               = module.lab_postgres.database_dev
  postgres_user                   = module.lab_postgres.user
  postgres_pass                   = module.lab_postgres.password
  secret_id__aws_access_key_id    = module.secret-prod-aws-access-key-id.secret_id
  secret_id__aws_secret_key       = module.secret-prod-aws-secret-key.secret_id
  secret_id__google_client_id     = module.secret-prod-google-client-id.secret_id
  secret_id__google_client_secret = module.secret-prod-google-client-secret.secret_id
  postgres_gcp_cloud_sql_instance = module.lab_postgres.connection_name
}

module "lab_postgres" {
  source = "./modules/postgres"

  region                = local.gcp-region
  project               = local.gcp-project-id
  service_account_email = google_service_account.xj-prod-yard.email
}

