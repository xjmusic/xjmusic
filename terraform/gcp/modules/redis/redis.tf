resource "random_id" "db_name_suffix" {
  byte_length = 4
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/redis_instance
resource "google_redis_instance" "redis" {
  name               = "xj-dev-redis-${random_id.db_name_suffix.hex}"
  memory_size_gb     = 1
  authorized_network = "default"
  tier               = "BASIC"
  region             = var.region
  project            = var.project
}
