# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

variable "notification_channels" {
  type = list(string)
}

variable "ship_image" {
  type = string
  default = "gcr.io/xj-vpc-host-prod/ship:latest"
}

variable "nexus_image" {
  type = string
  default = "gcr.io/xj-vpc-host-prod/nexus:latest"
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
  default = "prod"
}

variable "audio_base_url" {
  type = string
  default = "https://audio.xj.io/"
}

variable "audio_file_bucket" {
  type = string
  default = "xj-prod-audio"
}

variable "audio_upload_url" {
  type = string
  default = "https://xj-prod-audio.s3.amazonaws.com/"
}

variable "aws_default_region" {
  type = string
  default = "us-east-1"
}

variable "player_base_url" {
  type = string
  default = "https://play.xj.io/"
}

variable "ship_base_url" {
  type = string
  default = "https://ship.xj.io/"
}

variable "ship_bucket" {
  type = string
  default = "xj-prod-ship"
}

variable "stream_base_url" {
  type = string
  default = "https://stream.xj.io/"
}

variable "stream_bucket" {
  type = string
  default = "xj-prod-stream"
}

variable "nexus_resources_limits_cpu" {
  type = string
  default = 2
}

variable "nexus_resources_limits_memory" {
  type = string
  default = "6Gi"
}

variable "ship_resources_limits_cpu" {
  type = string
  default = 2
}

variable "ship_resources_limits_memory" {
  type = string
  default = "6Gi"
}

variable "secret_id__aws_access_key_id" {
  description = "Reference the value for 'aws_access_key_id' by secret manager id"
  type = string
}

variable "secret_id__aws_secret_key" {
  description = "Reference the value for 'aws_secret_key' by secret manager id"
  type = string
}

