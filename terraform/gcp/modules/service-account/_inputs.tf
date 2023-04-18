# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

variable "region" {
  type = string
}

variable "project_id" {
  type = string
}

variable "account_id" {
  type = string
}

variable "display_name" {
  type = string
}

variable "roles" {
  description = "An array of IAM roles to be applied to the service account."
  type        = list(string)
}
