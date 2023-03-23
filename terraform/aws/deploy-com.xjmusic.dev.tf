# Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xjmusic-com-dev-podcast" {
  name    = "podcast.dev.xjmusic.com"
  type    = "A"
  zone_id = aws_route53_zone.xjmusic-com.zone_id

  alias {
    name                   = module.xjmusic-com-dev-podcast.domain_name
    zone_id                = module.xjmusic-com-dev-podcast.hosted_zone_id
    evaluate_target_health = false
  }
}

module "xjmusic-com-dev-podcast" {
  source              = "./modules/website"
  bucket              = "podcast.dev.xjmusic.com"
  index_document      = "feed.xml"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xjmusic-com-dev.arn
  aliases = [
    "podcast.dev.xjmusic.com"
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate
resource "aws_acm_certificate" "xjmusic-com-dev" {
  domain_name = "dev.xjmusic.com"
  subject_alternative_names = [
    "*.dev.xjmusic.com",
  ]
  validation_method = "DNS"
}
