# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

variable "service_name" {
  type = string
}

variable "region" {
  type = string
}

variable "project" {
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

variable "postgres_user" {
  type = string
}

variable "postgres_pass" {
  type = string
}

# Reference the secret manager secret by its id
variable "secret_id__aws_access_key_id" {
  type = string
}

# Reference the secret manager secret by its id
variable "secret_id__aws_secret_key" {
  type = string
}

# Reference the secret manager secret by its id
variable "secret_id__google_client_id" {
  type = string
}

# Reference the secret manager secret by its id
variable "secret_id__google_client_secret" {
  type = string
}
