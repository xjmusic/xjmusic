# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

variable "region" {
  type = string
}

variable "bucket" {
  type = string
}

variable "index_document" {
  type    = string
  default = "index.html"
}

variable "error_document" {
  type    = string
  default = "error.html"
}

variable "write_authenticated_user_arn_list" {
  type = list(string)
}

variable "admin_user_arn_list" {
  type = list(string)
}

variable "cors_allowed_origins" {
  type    = list(string)
  default = [
    "*",
  ]
}

variable "cors_allowed_methods" {
  type    = list(string)
  default = [
    "GET",
    "HEAD",
  ]
}

variable "cors_allowed_headers" {
  type    = list(string)
  default = [
    "*",
  ]
}

variable "cors_expose_headers" {
  type    = list(string)
  default = [
    "ETag",
    "Access-Control-Allow-Origin",
  ]
}

variable "aliases" {
  type = list(string)
}

variable "acm_certificate_arn" {
  type = string
}

variable "blacklist_locations" {
  type = list(string)
  default = [
    "CN",
  ]
}
