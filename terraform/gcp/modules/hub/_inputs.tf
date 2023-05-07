# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

variable "image" {
  type = string
}

variable "service_name" {
  type = string
}

variable "region" {
  type = string
}

variable "project_id" {
  type = string
}

variable "service_account_email" {
  type = string
}

variable "postgres_database" {
  type = string
}

variable "postgres_gcp_cloud_sql_instance" {
  type = string
}

variable "app_base_url" {
  type = string
}

variable "environment" {
  type = string
}

variable "audio_base_url" {
  type = string
}

variable "audio_file_bucket" {
  type = string
}

variable "audio_upload_url" {
  type = string
}

variable "aws_default_region" {
  type = string
}

variable "player_base_url" {
  type = string
}

variable "ship_base_url" {
  type = string
}

variable "ship_bucket" {
  type = string
}

variable "sleep_when_idle" {
  type = bool
  default = false
}

variable "resources_limits_cpu" {
  type = string
}

variable "resources_limits_memory" {
  type = string
}

variable "secret_id__ingest_token_value" {
  description = "Reference the value for 'ingest_token_value' by secret manager id"
  type = string
}

variable "secret_id__aws_access_key_id" {
  description = "Reference the value for 'aws_access_key_id' by secret manager id"
  type = string
}

variable "secret_id__aws_secret_key" {
  description = "Reference the value for 'aws_secret_key' by secret manager id"
  type = string
}

variable "secret_id__google_client_id" {
  description = "Reference the value for 'google_client_id' by secret manager id"
  type = string
}

variable "secret_id__google_client_secret" {
  description = "Reference the value for 'google_client_secret' by secret manager id"
  type = string
}

variable "secret_id__postgres_username" {
  description = "Reference the value for 'postgres_username' by secret manager id"
  type = string
}

variable "secret_id__postgres_password" {
  description = "Reference the value for 'postgres_password' by secret manager id"
  type = string
}

