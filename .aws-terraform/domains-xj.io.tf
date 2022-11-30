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
resource "aws_route53_record" "xj-io-docs" {
  name    = "docs.xj.io"
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
    name                   = aws_cloudfront_distribution.xj-play.domain_name
    zone_id                = aws_cloudfront_distribution.xj-play.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-help" {
  name    = "help.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-help.domain_name
    zone_id                = aws_cloudfront_distribution.xj-help.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-content" {
  name    = "content.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-content.domain_name
    zone_id                = aws_cloudfront_distribution.xj-content.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-status" {
  name    = "status.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xj-status.domain_name
    zone_id                = aws_cloudfront_distribution.xj-status.hosted_zone_id
    evaluate_target_health = false
  }
}

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
resource "aws_route53_record" "xj-lab-gke-dev" {
  name    = "cbf2e597.lab.xj.io"
  records = ["34.105.63.9"]
  ttl     = 300
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-lab-gke-dev-local" {
  name    = "9e38338a.lab.xj.io"
  records = ["34.82.243.188"]
  ttl     = 300
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id
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
