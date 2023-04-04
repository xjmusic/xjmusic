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

output "dev_google_client_id_secret_id" {
  description = "Name of the Google Cloud Secret comprising the Google client ID"
  value       = google_secret_manager_secret.secret-dev-google-client-id.secret_id
  sensitive   = true
}

output "dev_google_client_secret_secret_id" {
  description = "Name of the Google Cloud Secret comprising the Google client secret"
  value       = google_secret_manager_secret.secret-dev-google-client-secret.secret_id
  sensitive   = true
}

output "lab_postgres_connection_name" {
  description = "Connection name of the Postgres instance"
  value       = module.lab_postgres.connection_name
}
