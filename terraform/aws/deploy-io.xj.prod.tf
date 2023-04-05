# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "xj-io" {
  name = "xj.io"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-io-mx" {
  name    = aws_route53_zone.xj-io.name
  ttl     = 172800
  type    = "MX"
  zone_id = aws_route53_zone.xj-io.zone_id

  records = [
    "1\tASPMX.L.GOOGLE.COM.",
    "5\tALT1.ASPMX.L.GOOGLE.COM.",
    "5\tALT2.ASPMX.L.GOOGLE.COM.",
    "10\tALT3.ASPMX.L.GOOGLE.COM.",
    "10\tALT4.ASPMX.L.GOOGLE.COM.",
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-io" {
  name    = "xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-io.domain_name
    zone_id                = module.xj-io.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-io-www" {
  name    = "www.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-redirect.domain_name
    zone_id                = module.xj-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-io-w" {
  name    = "w.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-redirect.domain_name
    zone_id                = module.xj-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-io-docs" {
  name    = "docs.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-redirect.domain_name
    zone_id                = module.xj-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-io-app" {
  name    = "app.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-redirect-lab.domain_name
    zone_id                = module.xj-redirect-lab.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-prod-audio" {
  name    = "audio.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-prod-audio.domain_name
    zone_id                = module.xj-prod-audio.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-prod-ship" {
  name    = "ship.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-prod-ship.domain_name
    zone_id                = module.xj-prod-ship.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-prod-stream" {
  name    = "stream.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-prod-stream.domain_name
    zone_id                = module.xj-prod-stream.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-prod-static" {
  name    = "static.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-static.domain_name
    zone_id                = module.xj-static.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-lab" {
  name    = "lab.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-lab.domain_name
    zone_id                = module.xj-lab.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-lab-gke-prod" {
  name    = "aa2efefd.lab.xj.io"
  records = ["35.230.59.92"]
  ttl     = 300
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-play" {
  name    = "play.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-play.domain_name
    zone_id                = module.xj-play.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-help" {
  name    = "help.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-help.domain_name
    zone_id                = module.xj-help.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-catalog" {
  name    = "catalog.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-catalog.domain_name
    zone_id                = module.xj-catalog.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-content" {
  name    = "content.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-content.domain_name
    zone_id                = module.xj-content.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-status" {
  name    = "status.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-status.domain_name
    zone_id                = module.xj-status.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-hub" {
  name    = "hub.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-redirect-lab.domain_name
    zone_id                = module.xj-redirect-lab.hosted_zone_id
    evaluate_target_health = false
  }
}

module "xj-lab" {
  source                     = "./modules/lab"
  bucket                     = "lab.xj.io"
  region                     = local.aws-region
  acm_certificate_arn        = aws_acm_certificate.xj-io.arn
  hub_origin_domain_name     = aws_route53_record.xj-lab-gke-prod.name
  hub_origin_protocol_policy = "http-only"
  aliases = [
    "lab.xj.io"
  ]
}

module "xj-io" {
  source              = "./modules/website"
  bucket              = "xj.io"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-io.arn
  aliases = [
    "xj.io"
  ]
}

module "xj-static" {
  source              = "./modules/website"
  bucket              = "xj-prod-static"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-io.arn
  aliases = [
    "static.xj.io"
  ]
}

module "xj-play" {
  source              = "./modules/website"
  bucket              = "play.xj.io"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-io.arn
  aliases = [
    "play.xj.io"
  ]
}

module "xj-help" {
  source              = "./modules/website"
  bucket              = "help.xj.io"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-io.arn
  aliases = [
    "help.xj.io"
  ]
}

module "xj-status" {
  source              = "./modules/website"
  bucket              = "status.xj.io"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-io.arn
  aliases = [
    "status.xj.io"
  ]
}

module "xj-catalog" {
  source              = "./modules/website"
  bucket              = "catalog.xj.io"
  region              = local.aws-region
  index_document      = "v4.json"
  acm_certificate_arn = aws_acm_certificate.xj-io.arn
  aliases = [
    "catalog.xj.io"
  ]
}

module "xj-content" {
  source              = "./modules/website"
  bucket              = "content.xj.io"
  region              = local.aws-region
  index_document      = "content-v4.json"
  acm_certificate_arn = aws_acm_certificate.xj-io.arn
  aliases = [
    "content.xj.io"
  ]
}

module "xj-redirect" {
  source              = "./modules/redirect"
  bucket              = "redirect-to-xj-io"
  redirect_host_name  = "xj.io"
  redirect_protocol   = "https"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-io-redirect.arn
  aliases = [
    "w.xj.io",
    "docs.xj.io",
    "www.xj.io",
    "www.xjplatform.com",
    "xj.outright.io",
    "xjplatform.com",
  ]
}

module "xj-redirect-lab" {
  source              = "./modules/redirect"
  bucket              = "redirect-to-lab-xj-io"
  redirect_host_name  = "lab.xj.io"
  redirect_protocol   = "https"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-io-lab-redirect.arn
  aliases = [
    "app.xj.io",
    "hub.xj.io",
  ]
}

module "xj-prod-audio" {
  source = "./modules/audio"
  bucket = "xj-prod-audio"
  admin_user_arn_list = [
    "arn:aws:iam::${local.aws-account-id}:user/charney"
  ]
  write_authenticated_user_arn_list = [
    aws_iam_user.xj-prod.arn
  ]
  cors_allowed_origins = [
    "https://lab.xj.io",
    "lab.xj.io",
  ]
  cors_allowed_methods = [
    "GET",
    "HEAD",
    "POST",
  ]
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-io.arn
  aliases = [
    "audio.xj.io"
  ]
}

module "xj-prod-ship" {
  source = "./modules/ship"
  bucket = "xj-prod-ship"
  admin_user_arn_list = [
    "arn:aws:iam::${local.aws-account-id}:user/charney"
  ]
  write_authenticated_user_arn_list = [
    aws_iam_user.xj-prod.arn
  ]
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-io.arn
  aliases = [
    "ship.xj.io"
  ]
}

module "xj-prod-stream" {
  source = "./modules/stream"
  bucket = "xj-prod-stream"
  admin_user_arn_list = [
    "arn:aws:iam::${local.aws-account-id}:user/charney"
  ]
  write_authenticated_user_arn_list = [
    aws_iam_user.xj-prod.arn
  ]
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-io.arn
  aliases = [
    "stream.xj.io"
  ]
}
