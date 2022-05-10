# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate
resource "aws_acm_certificate" "xj-io" {
  domain_name = "xj.io"
  subject_alternative_names = [
    "*.xj.io",
  ]
  validation_method = "DNS"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate
resource "aws_acm_certificate" "xj-environments" {
  domain_name = "xj.io"
  subject_alternative_names = [
    "*.dev.xj.io",
    "*.stage.xj.io",
    "*.prod.xj.io",
  ]
  validation_method = "DNS"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate
resource "aws_acm_certificate" "xj-io-redirect" {
  domain_name = "xj.io"
  subject_alternative_names = [
    "*.xj.io",
    "www.xjplatform.com",
    "xj.outright.io",
    "xjplatform.com",
  ]
  validation_method = "DNS"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate
resource "aws_acm_certificate" "xj-io-lab-redirect" {
  domain_name = "xj.io"
  subject_alternative_names = [
    "*.xj.io",
    "app.xj.io",
    "hub.xj.io",
  ]
  validation_method = "DNS"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate
resource "aws_acm_certificate" "uxrg" {
  domain_name = "uxresearchgroup.net"
  subject_alternative_names = [
    "*.uxresearchgroup.net",
  ]
  validation_method = "DNS"
}

