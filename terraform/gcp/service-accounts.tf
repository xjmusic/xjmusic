module "xj-dev-hub-sa" {
  source       = "./modules/service-account"
  region       = local.gcp-region
  project_id   = local.gcp-project-id
  account_id   = "xj-dev-hub"
  display_name = "XJ Development Hub"
  roles        = [
    "roles/cloudsql.client",
    "roles/compute.serviceAgent",
    "roles/container.serviceAgent",
    "roles/iam.serviceAccountUser",
    "roles/logging.viewer",
    "roles/run.admin",
    "roles/secretmanager.secretAccessor"
  ]
}

module "xj-prod-hub-sa" {
  source       = "./modules/service-account"
  region       = local.gcp-region
  project_id   = local.gcp-project-id
  account_id   = "xj-prod-hub"
  display_name = "XJ Production Hub"
  roles        = [
    "roles/cloudsql.client",
    "roles/compute.serviceAgent",
    "roles/container.serviceAgent",
    "roles/iam.serviceAccountUser",
    "roles/logging.viewer",
    "roles/run.admin",
    "roles/secretmanager.secretAccessor"
  ]
}

module "github-actions-sa" {
  source       = "./modules/service-account"
  region       = local.gcp-region
  project_id   = local.gcp-project-id
  account_id   = "github-actions"
  display_name = "XJ GitHub Actions"
  roles        = [
    "roles/artifactregistry.serviceAgent",
    "roles/compute.serviceAgent",
    "roles/container.serviceAgent",
    "roles/containerregistry.ServiceAgent",
    "roles/iam.serviceAccountUser",
    "roles/run.admin",
    "roles/storage.admin"
  ]
}

