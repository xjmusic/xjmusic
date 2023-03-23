# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

module "secret-prod-aws-access-key-id" {
  project               = local.gcp-project-id
  source                = "./modules/service-account-secret"
  secret_id             = "xj-services-prod-aws-access-key-id"
  service_account_email = google_service_account.xj-prod-yard.email
}

module "secret-prod-aws-secret-key" {
  project               = local.gcp-project-id
  source                = "./modules/service-account-secret"
  secret_id             = "xj-services-prod-aws-secret-key"
  service_account_email = google_service_account.xj-prod-yard.email
}

module "secret-prod-google-client-id" {
  project               = local.gcp-project-id
  source                = "./modules/service-account-secret"
  secret_id             = "xj-services-prod-google-client-id"
  service_account_email = google_service_account.xj-prod-yard.email
}

module "secret-prod-google-client-secret" {
  project               = local.gcp-project-id
  source                = "./modules/service-account-secret"
  secret_id             = "xj-services-prod-google-client-secret"
  service_account_email = google_service_account.xj-prod-yard.email
}

module "secret-prod-ingest-token-value" {
  project               = local.gcp-project-id
  source                = "./modules/service-account-secret"
  secret_id             = "xj-services-prod-ingest-token-value"
  service_account_email = google_service_account.xj-prod-yard.email
}
