# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

variable "notification_channels" {
  type = list(string)
}

variable "ship_image" {
  type = string
}

variable "nexus_image" {
  type = string
}

variable "ship_key" {
  type = string
}

variable "display_name" {
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

variable "stream_base_url" {
  type = string
}

variable "stream_bucket" {
  type = string
}

variable "nexus_resources_limits_cpu" {
  type = string
}

variable "nexus_resources_limits_memory" {
  type = string
}

variable "ship_resources_limits_cpu" {
  type = string
}

variable "ship_resources_limits_memory" {
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

