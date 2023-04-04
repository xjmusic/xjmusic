# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/sql_database_instance
resource "google_sql_database_instance" "postgres" {
  name             = var.name
  database_version = "POSTGRES_14"
  deletion_protection = true

  settings {
    tier = "db-f1-micro" # $7.65/month as of 2023-03-20
    # tier = "db-g1-small" # $25.55/month as of 2023-03-20

    database_flags {
      name  = "max_connections"
      value = "100"
    }

    ip_configuration {
      ipv4_enabled = true
    }

    backup_configuration {
      enabled    = true
      start_time = "00:00"
      backup_retention_settings {
        retention_unit   = "COUNT"
        retained_backups = 7
      }
    }
  }
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_sql_user
resource "google_sql_user" "lab" {
  name     = random_string.postgres_username.result
  instance = google_sql_database_instance.postgres.name
  password = random_password.postgres_password.result
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/postgres_database
resource "google_sql_database" "dev" {
  name     = "xj_dev"
  instance = google_sql_database_instance.postgres.name
}

resource "google_sql_database" "prod" {
  name     = "xj_prod"
  instance = google_sql_database_instance.postgres.name
}

