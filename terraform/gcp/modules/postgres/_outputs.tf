# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

output "public_ip_address" {
  value = google_sql_database_instance.postgres.public_ip_address
}

output "connection_name" {
  value = google_sql_database_instance.postgres.connection_name
}

output "username_secret_id" {
  value = google_secret_manager_secret.postgres_username.secret_id
  sensitive = true
}

output "password_secret_id" {
  value     = google_secret_manager_secret.postgres_password.secret_id
  sensitive = true
}

output "database_dev" {
  value = google_sql_database.dev.name
}

output "database_prod" {
  value = google_sql_database.prod.name
}
