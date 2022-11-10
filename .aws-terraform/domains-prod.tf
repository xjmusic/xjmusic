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

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "uxrg-zone" {
  name = "uxresearchgroup.net"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "uxrg-prod" {
  name    = "uxresearchgroup.net"
  type    = "A"
  zone_id = aws_route53_zone.uxrg-zone.zone_id

  alias {
    name                   = aws_cloudfront_distribution.uxrg-prod.domain_name
    zone_id                = aws_cloudfront_distribution.uxrg-prod.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "uxrg-prod-www" {
  name    = "www.uxresearchgroup.net"
  type    = "A"
  zone_id = aws_route53_zone.uxrg-zone.zone_id

  alias {
    name                   = aws_cloudfront_distribution.uxrg-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.uxrg-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "uxrg-prod-mx" {
  name    = aws_route53_zone.uxrg-zone.name
  ttl     = 172800
  type    = "MX"
  zone_id = aws_route53_zone.uxrg-zone.zone_id

  records = [
    "1\tASPMX.L.GOOGLE.COM.",
    "5\tALT1.ASPMX.L.GOOGLE.COM.",
    "5\tALT2.ASPMX.L.GOOGLE.COM.",
    "10\tALT3.ASPMX.L.GOOGLE.COM.",
    "10\tALT4.ASPMX.L.GOOGLE.COM.",
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "aircraft-works" {
  name = "aircraft.works"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "aircraft-works-mx" {
  name    = aws_route53_zone.aircraft-works.name
  ttl     = 172800
  type    = "MX"
  zone_id = aws_route53_zone.aircraft-works.zone_id

  records = [
    "1\tASPMX.L.GOOGLE.COM.",
    "5\tALT1.ASPMX.L.GOOGLE.COM.",
    "5\tALT2.ASPMX.L.GOOGLE.COM.",
    "10\tALT3.ASPMX.L.GOOGLE.COM.",
    "10\tALT4.ASPMX.L.GOOGLE.COM.",
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "aircraft-works" {
  name    = "aircraft.works"
  type    = "A"
  zone_id = aws_route53_zone.aircraft-works.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-works.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-works.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "aircraft-works-www" {
  name    = "www.aircraft.works"
  type    = "A"
  zone_id = aws_route53_zone.aircraft-works.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-works-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-works-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "aircraft-works-coolair" {
  name    = "cool.aircraft.works"
  type    = "A"
  zone_id = aws_route53_zone.aircraft-works.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-works-coolair.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-works-coolair.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "ambience-cc" {
  name = "ambience.cc"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "ambience-cloud" {
  name = "ambience.cloud"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "ambience-co" {
  name = "ambience.co"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "ambience-co-mx" {
  name    = aws_route53_zone.ambience-co.name
  ttl     = 172800
  type    = "MX"
  zone_id = aws_route53_zone.ambience-co.zone_id

  records = [
    "1\tASPMX.L.GOOGLE.COM.",
    "5\tALT1.ASPMX.L.GOOGLE.COM.",
    "5\tALT2.ASPMX.L.GOOGLE.COM.",
    "10\tALT3.ASPMX.L.GOOGLE.COM.",
    "10\tALT4.ASPMX.L.GOOGLE.COM.",
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "ambience-co" {
  name    = "ambience.co"
  type    = "A"
  zone_id = aws_route53_zone.ambience-co.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-works-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-works-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "ambience-co-www" {
  name    = "www.ambience.co"
  type    = "A"
  zone_id = aws_route53_zone.ambience-co.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-works-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-works-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "ambience-co-cool" {
  name    = "cool.ambience.co"
  type    = "A"
  zone_id = aws_route53_zone.ambience-co.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-redirect-coolair.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-redirect-coolair.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "ambientmusic-gallery" {
  name = "ambientmusic.gallery"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "ambientmusic-place" {
  name = "ambientmusic.place"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "ambientmusicfestival-com" {
  name = "ambientmusicfestival.com"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "ambientmusicfoundation-org" {
  name = "ambientmusicfoundation.org"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "ambientmusicfoundation-org" {
  name    = "ambientmusicfoundation.org"
  type    = "A"
  zone_id = aws_route53_zone.ambientmusicfoundation-org.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-ambientmusicfoundation-org.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-ambientmusicfoundation-org.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "ambientmusicfoundation-org-www" {
  name    = "www.ambientmusicfoundation.org"
  type    = "A"
  zone_id = aws_route53_zone.ambientmusicfoundation-org.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-ambientmusicfoundation-org-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-ambientmusicfoundation-org-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "ambientmusicgroup-com" {
  name = "ambientmusicgroup.com"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "ambientmusiconline-com" {
  name = "ambientmusiconline.com"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "ambientmusicplatform-com" {
  name = "ambientmusicplatform.com"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "theambientmusic-com" {
  name = "theambientmusic.com"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "theambientmusic-com" {
  name    = "theambientmusic.com"
  type    = "A"
  zone_id = aws_route53_zone.theambientmusic-com.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-works-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-works-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "ambientmusicstore-com" {
  name = "ambientmusicstore.com"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "coolambience-co" {
  name = "coolambience.co"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "coolambience-co-mx" {
  name    = aws_route53_zone.coolambience-co.name
  ttl     = 172800
  type    = "MX"
  zone_id = aws_route53_zone.coolambience-co.zone_id

  records = [
    "1\tASPMX.L.GOOGLE.COM.",
    "5\tALT1.ASPMX.L.GOOGLE.COM.",
    "5\tALT2.ASPMX.L.GOOGLE.COM.",
    "10\tALT3.ASPMX.L.GOOGLE.COM.",
    "10\tALT4.ASPMX.L.GOOGLE.COM.",
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "coolambience-co" {
  name    = "coolambience.co"
  type    = "A"
  zone_id = aws_route53_zone.coolambience-co.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-redirect-coolair.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-redirect-coolair.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "coolambience-co-www" {
  name    = "www.coolambience.co"
  type    = "A"
  zone_id = aws_route53_zone.coolambience-co.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-redirect-coolair.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-redirect-coolair.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "coolambience-com" {
  name = "coolambience.com"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "coolambience-com-mx" {
  name    = aws_route53_zone.coolambience-com.name
  ttl     = 172800
  type    = "MX"
  zone_id = aws_route53_zone.coolambience-com.zone_id

  records = [
    "1\tASPMX.L.GOOGLE.COM.",
    "5\tALT1.ASPMX.L.GOOGLE.COM.",
    "5\tALT2.ASPMX.L.GOOGLE.COM.",
    "10\tALT3.ASPMX.L.GOOGLE.COM.",
    "10\tALT4.ASPMX.L.GOOGLE.COM.",
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "coolambience-com" {
  name    = "coolambience.com"
  type    = "A"
  zone_id = aws_route53_zone.coolambience-com.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-redirect-coolair.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-redirect-coolair.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "coolambience-com-www" {
  name    = "www.coolambience.com"
  type    = "A"
  zone_id = aws_route53_zone.coolambience-com.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-redirect-coolair.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-redirect-coolair.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "moodambience-com" {
  name = "moodambience.com"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "moodambience-com" {
  name    = "moodambience.com"
  type    = "A"
  zone_id = aws_route53_zone.moodambience-com.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-works-redirect.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-works-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_zone
resource "aws_route53_zone" "dopeambience-com" {
  name = "dopeambience.com"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "dopeambience-com" {
  name    = "dopeambience.com"
  type    = "A"
  zone_id = aws_route53_zone.dopeambience-com.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-redirect-coolair.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-redirect-coolair.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "dopeambience-com-www" {
  name    = "www.dopeambience.com"
  type    = "A"
  zone_id = aws_route53_zone.dopeambience-com.zone_id

  alias {
    name                   = aws_cloudfront_distribution.aircraft-redirect-coolair.domain_name
    zone_id                = aws_cloudfront_distribution.aircraft-redirect-coolair.hosted_zone_id
    evaluate_target_health = false
  }
}


