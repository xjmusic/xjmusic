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

