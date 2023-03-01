# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

variable "project" {
  type = string
}

variable "notification_channels" {
  type = list(string)
}

variable "ship_key" {
  type = string
}

variable "display_name" {
  type = string
}

variable "service_account_email" {
  type = string
}

variable "region" {
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
