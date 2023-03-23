# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

variable "region" {
  type = string
}

variable "bucket" {
  type = string
}

variable "redirect_protocol" {
  type = string
}

variable "redirect_host_name" {
  type = string
}

variable "aliases" {
  type = list(string)
}

variable "acm_certificate_arn" {
  type = string
}
