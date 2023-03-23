# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

output "host" {
  value = google_redis_instance.redis.host
}

output "port" {
  value = google_redis_instance.redis.port
}

