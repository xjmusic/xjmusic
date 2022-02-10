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
    name                   = aws_cloudfront_distribution.xj-io.domain_name
    zone_id                = aws_cloudfront_distribution.xj-io.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-io-www" {
  name    = "www.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.xj-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-io-w" {
  name    = "w.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.xj-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-io-app" {
  name    = "app.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-lab-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.xj-lab-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-prod-audio" {
  name    = "audio.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-prod-audio.domain_name
    zone_id                = aws_cloudfront_distribution.xj-prod-audio.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-prod-ship" {
  name    = "ship.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-prod-ship.domain_name
    zone_id                = aws_cloudfront_distribution.xj-prod-ship.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-prod-stream" {
  name    = "stream.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-prod-stream.domain_name
    zone_id                = aws_cloudfront_distribution.xj-prod-stream.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-prod-static" {
  name    = "static.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-prod-static.domain_name
    zone_id                = aws_cloudfront_distribution.xj-prod-static.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-lab" {
  name    = "lab.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-lab.domain_name
    zone_id                = aws_cloudfront_distribution.xj-lab.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-play" {
  name    = "play.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-play.domain_name
    zone_id                = aws_cloudfront_distribution.xj-play.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
#resource "aws_route53_record" "xj-help" {
#  name    = "help.xj.io"
#  type    = "A"
#  zone_id = aws_route53_zone.xj-io.zone_id
#
#  alias {
#    name                   = aws_cloudfront_distribution.xj-help.domain_name
#    zone_id                = aws_cloudfront_distribution.xj-help.hosted_zone_id
#    evaluate_target_health = false
#  }
#}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
#resource "aws_route53_record" "xj-status" {
#  name    = "status.xj.io"
#  type    = "A"
#  zone_id = aws_route53_zone.xj-io.zone_id
#
#  alias {
#    name                   = aws_cloudfront_distribution.xj-status.domain_name
#    zone_id                = aws_cloudfront_distribution.xj-status.hosted_zone_id
#    evaluate_target_health = false
#  }
#}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-hub" {
  name    = "hub.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-lab-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.xj-lab-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-help" {
  name    = "help.xj.io"
  type    = "CNAME"
  ttl     = 5
  zone_id = aws_route53_zone.xj-io.zone_id
  records = ["custom.crisp.help."]
  # FUTURE ditch this for the self-hosted ones when ready
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-help-verification" {
  name    = "_crisp.help.xj.io"
  type    = "TXT"
  ttl     = 5
  zone_id = aws_route53_zone.xj-io.zone_id
  records = ["crisp-website-id=d7d13bad-bc2b-4546-a18e-547f16ef6ab2"]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-status" {
  name    = "status.xj.io"
  type    = "CNAME"
  ttl     = 5
  zone_id = aws_route53_zone.xj-io.zone_id
  records = ["custom.crisp.watch."]
  # FUTURE ditch this for the self-hosted ones when ready
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-status-verification" {
  name    = "_crisp.status.xj.io"
  type    = "TXT"
  ttl     = 5
  zone_id = aws_route53_zone.xj-io.zone_id
  records = ["crisp-website-id=d7d13bad-bc2b-4546-a18e-547f16ef6ab2"]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "xjplatform-com" {
  name = "xjplatform.com"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xjplatform-com" {
  name    = "xjplatform.com"
  type    = "A"
  zone_id = aws_route53_zone.xjplatform-com.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.xj-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xjplatform-com-www" {
  name    = "www.xjplatform.com"
  type    = "A"
  zone_id = aws_route53_zone.xjplatform-com.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.xj-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}
