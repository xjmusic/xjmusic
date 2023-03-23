# Serverless operations (Spring Boot Refactoring)
# https://www.pivotaltracker.com/story/show/184580235

#module "fabrication_bump_chill_serverless" {
#  source = "./modules/fabrication"
#
#  region                          = local.gcp-region
#  project                         = local.gcp-project-id
#  service_account_email           = google_service_account.xj-prod-yard.email
#  display_name                    = "Bump/ChillServerless"
#  notification_channels           = local.gcp-alert-notification-channels
#  ship_key                        = "bump_chill_serverless"
#  secret_id__aws_access_key_id    = module.secret-prod-aws-access-key-id.secret_id
#  secret_id__aws_secret_key       = module.secret-prod-aws-secret-key.secret_id
#  secret_id__google_client_id     = module.secret-prod-google-client-id.secret_id
#  secret_id__google_client_secret = module.secret-prod-google-client-secret.secret_id
#}
#
