# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-audio" {
  name    = "audio.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-dev-audio.domain_name
    zone_id                = aws_cloudfront_distribution.xj-dev-audio.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-ship" {
  name    = "ship.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-dev-ship.domain_name
    zone_id                = aws_cloudfront_distribution.xj-dev-ship.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-static" {
  name    = "static.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-dev-static.domain_name
    zone_id                = aws_cloudfront_distribution.xj-dev-static.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev" {
  name    = "dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-dev.domain_name
    zone_id                = aws_cloudfront_distribution.xj-dev.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-lab" {
  name    = "lab.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-dev-lab.domain_name
    zone_id                = aws_cloudfront_distribution.xj-dev-lab.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-local" {
  name    = "local.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-dev-local.domain_name
    zone_id                = aws_cloudfront_distribution.xj-dev-local.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-stream" {
  name    = "stream.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-dev-stream.domain_name
    zone_id                = aws_cloudfront_distribution.xj-dev-stream.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-help" {
  name    = "help.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-dev-help.domain_name
    zone_id                = aws_cloudfront_distribution.xj-dev-help.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-content" {
  name    = "content.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-dev-content.domain_name
    zone_id                = aws_cloudfront_distribution.xj-dev-content.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-status" {
  name    = "status.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-dev-status.domain_name
    zone_id                = aws_cloudfront_distribution.xj-dev-status.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "uxrg-dev" {
  name    = "dev.uxresearchgroup.net"
  type    = "A"
  zone_id = aws_route53_zone.uxrg-zone.zone_id

  alias {
    name                   = aws_cloudfront_distribution.uxrg-dev.domain_name
    zone_id                = aws_cloudfront_distribution.uxrg-dev.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "aircraft-works-dev" {
  name    = "dev.aircraft.works"
  type    = "A"
  zone_id = aws_route53_zone.aircraft-works.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-works-dev.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-works-dev.hosted_zone_id
    evaluate_target_health = false
  }
}

