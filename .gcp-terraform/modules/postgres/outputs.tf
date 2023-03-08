# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

output "public_ip_address" {
  value = google_sql_database_instance.postgres.public_ip_address
}

output "connection_name" {
  value = google_sql_database_instance.postgres.connection_name
}

output "user" {
  value = google_sql_user.lab.name
}

output "password" {
  value     = google_sql_user.lab.password
  sensitive = true
}

output "database_dev" {
  value = google_sql_database.dev.name
}

output "database_prod" {
  value = google_sql_database.prod.name
}
