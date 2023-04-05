# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-audio" {
  name    = "audio.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-dev-audio.domain_name
    zone_id                = module.xj-dev-audio.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-ship" {
  name    = "ship.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-dev-ship.domain_name
    zone_id                = module.xj-dev-ship.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-static" {
  name    = "static.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-dev-static.domain_name
    zone_id                = module.xj-dev-static.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev" {
  name    = "dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-dev.domain_name
    zone_id                = module.xj-dev.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-lab" {
  name    = "lab.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-dev-lab.domain_name
    zone_id                = module.xj-dev-lab.hosted_zone_id
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
    name                   = module.xj-dev-local.domain_name
    zone_id                = module.xj-dev-local.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-stream" {
  name    = "stream.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-dev-stream.domain_name
    zone_id                = module.xj-dev-stream.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-help" {
  name    = "help.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-dev-help.domain_name
    zone_id                = module.xj-dev-help.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-catalog" {
  name    = "catalog.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-dev-catalog.domain_name
    zone_id                = module.xj-dev-catalog.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-content" {
  name    = "content.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-dev-content.domain_name
    zone_id                = module.xj-dev-content.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xj-dev-status" {
  name    = "status.dev.xj.io"
  type    = "A"
  zone_id = aws_route53_zone.xj-io.zone_id

  alias {
    name                   = module.xj-dev-status.domain_name
    zone_id                = module.xj-dev-status.hosted_zone_id
    evaluate_target_health = false
  }
}

module "xj-dev-lab" {
  source              = "./modules/lab"
  bucket              = "lab.dev.xj.io"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-environments.arn
  aliases = [
    "lab.dev.xj.io"
  ]
  hub_origin_domain_name = "xj-dev-lab-hub-pxo2raxupa-uw.a.run.app"
}

module "xj-dev-local" {
  source              = "./modules/lab"
  bucket              = "local.dev.xj.io"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-environments.arn
  aliases = [
    "local.dev.xj.io"
  ]
  hub_origin_domain_name = "xj-dev-lab-hub-pxo2raxupa-uw.a.run.app" # future: set this to the local dev hub
  existing_bucket_name   = module.xj-dev-lab.bucket_name
  existing_bucket_arn    = module.xj-dev-lab.bucket_arn
}

module "xj-dev" {
  source              = "./modules/website"
  bucket              = "dev.xj.io"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-io.arn
  aliases = [
    "dev.xj.io"
  ]
}

module "xj-dev-static" {
  source              = "./modules/website"
  bucket              = "xj-dev-static"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-environments.arn
  aliases = [
    "static.dev.xj.io"
  ]
}

module "xj-dev-help" {
  source              = "./modules/website"
  bucket              = "help.dev.xj.io"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-environments.arn
  aliases = [
    "help.dev.xj.io"
  ]
}

module "xj-dev-catalog" {
  source              = "./modules/website"
  bucket              = "catalog.dev.xj.io"
  index_document      = "v4.json"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-environments.arn
  aliases = [
    "catalog.dev.xj.io"
  ]
}

module "xj-dev-content" {
  source              = "./modules/website"
  bucket              = "content.dev.xj.io"
  index_document      = "content-v4.json"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-environments.arn
  aliases = [
    "content.dev.xj.io"
  ]
}

module "xj-dev-status" {
  source              = "./modules/website"
  bucket              = "status.dev.xj.io"
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-environments.arn
  aliases = [
    "status.dev.xj.io"
  ]
}

module "xj-dev-audio" {
  source = "./modules/audio"
  bucket = "xj-dev-audio"
  admin_user_arn_list = [
    "arn:aws:iam::${local.aws-account-id}:user/charney"
  ]
  write_authenticated_user_arn_list = [
    aws_iam_user.xj-dev.arn
  ]
  cors_allowed_origins = [
    "https://lab.dev.xj.io",
    "lab.dev.xj.io",
  ]
  cors_allowed_methods = [
    "GET",
    "HEAD",
    "POST",
  ]
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-environments.arn
  aliases = [
    "audio.dev.xj.io"
  ]
}

module "xj-dev-ship" {
  source = "./modules/ship"
  bucket = "xj-dev-ship"
  admin_user_arn_list = [
    "arn:aws:iam::${local.aws-account-id}:user/charney"
  ]
  write_authenticated_user_arn_list = [
    aws_iam_user.xj-dev.arn
  ]
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-environments.arn
  aliases = [
    "ship.dev.xj.io"
  ]
}

module "xj-dev-stream" {
  source = "./modules/stream"
  bucket = "xj-dev-stream"
  admin_user_arn_list = [
    "arn:aws:iam::${local.aws-account-id}:user/charney"
  ]
  write_authenticated_user_arn_list = [
    aws_iam_user.xj-dev.arn
  ]
  region              = local.aws-region
  acm_certificate_arn = aws_acm_certificate.xj-environments.arn
  aliases = [
    "stream.dev.xj.io"
  ]
}
