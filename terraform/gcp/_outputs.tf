output "lab_postgres_username_secret_id" {
  description = "Name of the Google Cloud Secret comprising the Postgres username"
  value       = module.lab_postgres.username_secret_id
  sensitive   = true
}

output "lab_postgres_password_secret_id" {
  description = "Name of the Google Cloud Secret comprising the Postgres password"
  value       = module.lab_postgres.password_secret_id
  sensitive   = true
}

output "dev_aws_access_key_id_secret_id" {
  description = "Name of the Google Cloud Secret comprising the AWS access key ID"
  value       = google_secret_manager_secret.secret-dev-aws-access-key-id.secret_id
  sensitive   = true
}

output "dev_aws_secret_key_secret_id" {
  description = "Name of the Google Cloud Secret comprising the AWS secret key"
  value       = google_secret_manager_secret.secret-dev-aws-secret-key.secret_id
  sensitive   = true
}

output "dev_gcp_client_id_secret_id" {
  description = "Name of the Google Cloud Secret comprising the Google client ID"
  value       = google_secret_manager_secret.secret-dev-google-client-id.secret_id
  sensitive   = true
}

output "dev_gcp_client_secret_secret_id" {
  description = "Name of the Google Cloud Secret comprising the Google client secret"
  value       = google_secret_manager_secret.secret-dev-google-client-secret.secret_id
  sensitive   = true
}

output "dev_gcp_service_account_email" {
  description = "GCP service account to use"
  value       = google_service_account.xj-dev-yard.email
}

output "dev_gcp_project_id" {
  description = "GCP project id to use"
  value       = local.gcp-project-id
}

output "dev_gcp_region" {
  description = "GCP region to use"
  value       = local.gcp-region
}

output "lab_postgres_connection_name" {
  description = "Connection name of the Postgres instance"
  value       = module.lab_postgres.connection_name
}

output "secret_id__aws_access_key_id" {
  description = "GCP Secret ID to retrieve aws_access_key_id"
  value       = google_secret_manager_secret.secret-dev-aws-access-key-id.secret_id
  sensitive   = true
}

output "secret_id__aws_secret_key" {
  description = "GCP Secret ID to retrieve aws_secret_key"
  value       = google_secret_manager_secret.secret-dev-aws-secret-key.secret_id
  sensitive   = true
}

output "secret_id__google_client_id" {
  description = "GCP Secret ID to retrieve google_client_id"
  value       = google_secret_manager_secret.secret-dev-google-client-id.secret_id
  sensitive   = true
}

output "secret_id__google_client_secret" {
  description = "GCP Secret ID to retrieve google_client_secret"
  value       = google_secret_manager_secret.secret-dev-google-client-secret.secret_id
  sensitive   = true
}

output "secret_id__postgres_username" {
  description = "GCP Secret ID to retrieve postgres_username"
  value       = module.lab_postgres.username_secret_id
  sensitive   = true
}

output "secret_id__postgres_password" {
  description = "GCP Secret ID to retrieve postgres_password"
  value       = module.lab_postgres.password_secret_id
  sensitive   = true
}
