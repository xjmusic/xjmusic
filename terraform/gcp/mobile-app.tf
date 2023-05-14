module "mobile_app_firebase" {
  source = "./modules/firebase"

  region = local.gcp-region
  project_id = local.gcp-project-id
}
