# Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate
resource "aws_acm_certificate" "xjmusic-com" {
  domain_name = "xjmusic.com"
  subject_alternative_names = [
    "*.xjmusic.com",
  ]
  validation_method = "DNS"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "xjmusic-com" {
  name = "xjmusic.com"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xjmusic-com-mx" {
  name    = aws_route53_zone.xjmusic-com.name
  ttl     = 172800
  type    = "MX"
  zone_id = aws_route53_zone.xjmusic-com.zone_id

  records = [
    "1\tASPMX.L.GOOGLE.COM.",
    "5\tALT1.ASPMX.L.GOOGLE.COM.",
    "5\tALT2.ASPMX.L.GOOGLE.COM.",
    "10\tALT3.ASPMX.L.GOOGLE.COM.",
    "10\tALT4.ASPMX.L.GOOGLE.COM.",
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xjmusic-com" {
  name    = "xjmusic.com"
  type    = "A"
  zone_id = aws_route53_zone.xjmusic-com.zone_id
  ttl     = 86400

  records = [
    "198.185.159.144",
    "198.185.159.145",
    "198.49.23.144",
    "198.49.23.145"
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xjmusic-com-www" {
  name    = "www.xjmusic.com"
  type    = "CNAME"
  zone_id = aws_route53_zone.xjmusic-com.zone_id
  ttl     = 86400

  records = [
    "ext-sq.squarespace.com"
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xjmusic-com-verification" {
  name    = "xjmusic.com"
  type    = "TXT"
  zone_id = aws_route53_zone.xjmusic-com.zone_id
  ttl     = 86400

  records = [
    "google-site-verification=L7FhL6FUfLC9adOI9gkzWXrwdPA0m0sKtyV5DF3Xf_g"
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xjmusic-com-podcast" {
  name    = "podcast.xjmusic.com"
  type    = "A"
  zone_id = aws_route53_zone.xjmusic-com.zone_id

  alias {
    name                   = module.xjmusic-com-podcast.domain_name
    zone_id                = module.xjmusic-com-podcast.hosted_zone_id
    evaluate_target_health = false
  }
}

module "xjmusic-com-podcast" {
  source              = "./modules/website"
  bucket              = "podcast.xjmusic.com"
  index_document      = "feed.xml"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xjmusic-com.arn
  aliases = [
    "podcast.xjmusic.com"
  ]
}

