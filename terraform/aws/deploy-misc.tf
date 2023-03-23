# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

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
    name                   = module.aircraft-works.domain_name
    zone_id                = module.aircraft-works.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "aircraft-works-www" {
  name    = "www.aircraft.works"
  type    = "A"
  zone_id = aws_route53_zone.aircraft-works.zone_id

  alias {
    name                   = module.redirect-to-aircraft-works.domain_name
    zone_id                = module.redirect-to-aircraft-works.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "aircraft-works-coolair" {
  name    = "cool.aircraft.works"
  type    = "A"
  zone_id = aws_route53_zone.aircraft-works.zone_id

  alias {
    name                   = module.aircraft-works-coolair.domain_name
    zone_id                = module.aircraft-works-coolair.hosted_zone_id
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
    name                   = module.redirect-to-aircraft-works.domain_name
    zone_id                = module.redirect-to-aircraft-works.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "ambience-co-www" {
  name    = "www.ambience.co"
  type    = "A"
  zone_id = aws_route53_zone.ambience-co.zone_id

  alias {
    name                   = module.redirect-to-aircraft-works.domain_name
    zone_id                = module.redirect-to-aircraft-works.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "ambience-co-cool" {
  name    = "cool.ambience.co"
  type    = "A"
  zone_id = aws_route53_zone.ambience-co.zone_id

  alias {
    name                   = module.redirect-to-aircraft-works.domain_name
    zone_id                = module.redirect-to-aircraft-works.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "aircraft-works-dev" {
  name    = "dev.aircraft.works"
  type    = "A"
  zone_id = aws_route53_zone.aircraft-works.zone_id

  alias {
    name                   = module.aircraft-works-dev.domain_name
    zone_id                = module.aircraft-works-dev.hosted_zone_id
    evaluate_target_health = false
  }
}

module "aircraft-works" {
  source              = "./modules/website"
  bucket              = "aircraft.works"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.aircraft-works.arn
  aliases = [
    "aircraft.works"
  ]
}


module "redirect-to-cool-aircraft-works" {
  source              = "./modules/redirect"
  bucket              = "redirect-to-cool-aircraft-works"
  redirect_host_name  = "xj.io"
  redirect_protocol   = "https"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.aircraft-works-coolair-redirect.arn
  aliases = [
    "coolambience.com",
    "www.coolambience.com",
  ]
}

module "redirect-to-aircraft-works" {
  source              = "./modules/redirect"
  bucket              = "redirect-to-aircraft-works"
  redirect_host_name  = "aircraft.works"
  redirect_protocol   = "https"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.redirect-to-aircraft-works.arn
  aliases = [
    "www.aircraft.works",
    "aircraftcreative.com",
    "aircraftproductivity.com",
    "ambience.co",
  ]
}

module "aircraft-works-coolair" {
  source              = "./modules/redirect"
  bucket              = "cool.aircraft.works"
  redirect_host_name  = "xj.io/download"
  redirect_protocol   = "https"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.aircraft-works.arn
  aliases = [
    "cool.aircraft.works"
  ]
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
    name                   = module.xj-redirect.domain_name
    zone_id                = module.xj-redirect.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xjplatform-com-www" {
  name    = "www.xjplatform.com"
  type    = "A"
  zone_id = aws_route53_zone.xjplatform-com.zone_id

  alias {
    name                   = module.xj-redirect.domain_name
    zone_id                = module.xj-redirect.hosted_zone_id
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
    name                   = module.uxrg-prod.domain_name
    zone_id                = module.uxrg-prod.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "uxrg-prod-www" {
  name    = "www.uxresearchgroup.net"
  type    = "A"
  zone_id = aws_route53_zone.uxrg-zone.zone_id

  alias {
    name                   = module.uxrg-redirect.domain_name
    zone_id                = module.uxrg-redirect.hosted_zone_id
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
    name                   = module.redirect-to-ambientmusicfoundation-org.domain_name
    zone_id                = module.redirect-to-ambientmusicfoundation-org.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "ambientmusicfoundation-org-www" {
  name    = "www.ambientmusicfoundation.org"
  type    = "A"
  zone_id = aws_route53_zone.ambientmusicfoundation-org.zone_id

  alias {
    name                   = module.redirect-to-ambientmusicfoundation-org.domain_name
    zone_id                = module.redirect-to-ambientmusicfoundation-org.hosted_zone_id
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
    name                   = module.redirect-to-aircraft-works.domain_name
    zone_id                = module.redirect-to-aircraft-works.hosted_zone_id
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
    name                   = module.redirect-to-cool-aircraft-works.domain_name
    zone_id                = module.redirect-to-cool-aircraft-works.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "coolambience-co-www" {
  name    = "www.coolambience.co"
  type    = "A"
  zone_id = aws_route53_zone.coolambience-co.zone_id

  alias {
    name                   = module.redirect-to-cool-aircraft-works.domain_name
    zone_id                = module.redirect-to-cool-aircraft-works.hosted_zone_id
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
    name                   = module.redirect-to-cool-aircraft-works.domain_name
    zone_id                = module.redirect-to-cool-aircraft-works.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "coolambience-com-www" {
  name    = "www.coolambience.com"
  type    = "A"
  zone_id = aws_route53_zone.coolambience-com.zone_id

  alias {
    name                   = module.redirect-to-cool-aircraft-works.domain_name
    zone_id                = module.redirect-to-cool-aircraft-works.hosted_zone_id
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
    name                   = module.redirect-to-aircraft-works.domain_name
    zone_id                = module.redirect-to-aircraft-works.hosted_zone_id
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
    name                   = module.redirect-to-cool-aircraft-works.domain_name
    zone_id                = module.redirect-to-cool-aircraft-works.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "dopeambience-com-www" {
  name    = "www.dopeambience.com"
  type    = "A"
  zone_id = aws_route53_zone.dopeambience-com.zone_id

  alias {
    name                   = module.redirect-to-cool-aircraft-works.domain_name
    zone_id                = module.redirect-to-cool-aircraft-works.hosted_zone_id
    evaluate_target_health = false
  }
}


# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "uxrg-dev" {
  name    = "dev.uxresearchgroup.net"
  type    = "A"
  zone_id = aws_route53_zone.uxrg-zone.zone_id

  alias {
    name                   = module.uxrg-dev.domain_name
    zone_id                = module.uxrg-dev.hosted_zone_id
    evaluate_target_health = false
  }
}

module "uxrg-redirect" {
  source              = "./modules/redirect"
  bucket              = "redirect-to-uxresearchgroup-net"
  redirect_host_name  = "uxresearchgroup.net"
  redirect_protocol   = "https"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.uxrg.arn
  aliases = [
    "www.uxresearchgroup.net",
  ]
}

module "redirect-to-ambientmusicfoundation-org" {
  source              = "./modules/redirect"
  bucket              = "redirect-to-ambientmusicfoundation-org"
  redirect_host_name  = "ambientmusicfoundation.org"
  redirect_protocol   = "https"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.ambientmusicfoundation-org-redirect.arn
  aliases = [
    "www.ambientmusicfoundation.org",
  ]
}

module "uxrg-prod" {
  source              = "./modules/website"
  bucket              = "uxresearchgroup.net"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.uxrg.arn
  aliases = [
    "uxresearchgroup.net"
  ]
}

module "ambientmusicfoundation-org" {
  source              = "./modules/website"
  bucket              = "ambientmusicfoundation.org"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.ambientmusicfoundation-org.arn
  aliases = [
    "ambientmusicfoundation.org"
  ]
}

# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

module "uxrg-dev" {
  source              = "./modules/website"
  bucket              = "dev.uxresearchgroup.net"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.uxrg.arn
  aliases = [
    "dev.uxresearchgroup.net"
  ]
}

module "aircraft-works-dev" {
  source              = "./modules/website"
  bucket              = "dev.aircraft.works"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.aircraft-works.arn
  aliases = [
    "dev.aircraft.works"
  ]
}

