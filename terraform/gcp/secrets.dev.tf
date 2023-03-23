# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

module "secret-dev-aws-access-key-id" {
  project               = local.gcp-project-id
  source                = "./modules/service-account-secret"
  secret_id             = "xj-services-dev-aws-access-key-id"
  service_account_email = google_service_account.xj-dev-yard.email
}

module "secret-dev-aws-secret-key" {
  project               = local.gcp-project-id
  source                = "./modules/service-account-secret"
  secret_id             = "xj-services-dev-aws-secret-key"
  service_account_email = google_service_account.xj-dev-yard.email
}

module "secret-dev-google-client-id" {
  project               = local.gcp-project-id
  source                = "./modules/service-account-secret"
  secret_id             = "xj-services-dev-google-client-id"
  service_account_email = google_service_account.xj-dev-yard.email
}

module "secret-dev-google-client-secret" {
  project               = local.gcp-project-id
  source                = "./modules/service-account-secret"
  secret_id             = "xj-services-dev-google-client-secret"
  service_account_email = google_service_account.xj-dev-yard.email
}

module "secret-dev-ingest-token-value" {
  project               = local.gcp-project-id
  source                = "./modules/service-account-secret"
  secret_id             = "xj-services-dev-ingest-token-value"
  service_account_email = google_service_account.xj-dev-yard.email
}
