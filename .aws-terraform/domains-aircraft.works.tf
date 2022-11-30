
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

