# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

variable "region" {
  type = string
}

variable "bucket" {
  type = string
}

variable "index_document" {
  type = string
  default = "index.html"
}

variable "error_document" {
  type = string
  default = "error.html"
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

variable "hub_origin_domain_name" {
  type = string
}

# If you want to use an existing bucket, you must provide the bucket name
variable "existing_bucket_name" {
  type= string
  default = ""
}

# If you want to use an existing bucket, you must provide the bucket arn
variable "existing_bucket_arn" {
  type= string
  default = ""
}

# Use an existing bucket if we are provided a bucket name and  bucket arn
locals {
  create_bucket = var.existing_bucket_arn == "" || var.existing_bucket_name == "" ? true : false
  s3_bucket_regional_domain_name = local.create_bucket ? "${var.bucket}.s3-website-${var.region}.amazonaws.com" : "${var.existing_bucket_name}.s3-website-${var.region}.amazonaws.com"
}

