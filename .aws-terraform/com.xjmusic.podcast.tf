# Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xjmusic-com-podcast" {
  name    = "podcast.xjmusic.com"
  type    = "A"
  zone_id = aws_route53_zone.xjmusic-com.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xjmusic-com-podcast.domain_name
    zone_id                = aws_cloudfront_distribution.xjmusic-com-podcast.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "xjmusic-com-dev-podcast" {
  name    = "podcast.dev.xjmusic.com"
  type    = "A"
  zone_id = aws_route53_zone.xjmusic-com.zone_id

  alias {
    name                   = aws_cloudfront_distribution.xjmusic-com-dev-podcast.domain_name
    zone_id                = aws_cloudfront_distribution.xjmusic-com-dev-podcast.hosted_zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xjmusic-com-podcast" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "podcast.xjmusic.com"
  default_root_object = "feed.xml"
  http_version        = "http2"
  price_class         = "PriceClass_100"
  aliases = [
    "podcast.xjmusic.com"
  ]

  origin {
    domain_name = aws_s3_bucket.xjmusic-com-podcast.bucket_regional_domain_name
    origin_id   = "xjmusic-com-podcast-s3-origin"
    origin_path = ""
    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols = [
        "TLSv1",
        "TLSv1.1",
        "TLSv1.2"
      ]
    }
  }

  default_cache_behavior {
    allowed_methods = [
      "GET",
      "HEAD",
    ]
    cached_methods = [
      "GET",
      "HEAD"
    ]
    target_origin_id = "xjmusic-com-podcast-s3-origin"

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
      headers = []
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
  }

  restrictions {
    geo_restriction {
      restriction_type = "blacklist"
      locations = [
        "CN"
      ]
    }
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate.xjmusic-com.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "prod"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xjmusic-com-dev-podcast" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "podcast.dev.xjmusic.com"
  default_root_object = "feed.xml"
  http_version        = "http2"
  price_class         = "PriceClass_100"
  aliases = [
    "podcast.dev.xjmusic.com"
  ]

  origin {
    domain_name = aws_s3_bucket.xjmusic-com-dev-podcast.bucket_regional_domain_name
    origin_id   = "xjmusic-com-dev-podcast-s3-origin"
    origin_path = ""
    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols = [
        "TLSv1",
        "TLSv1.1",
        "TLSv1.2"
      ]
    }
  }

  default_cache_behavior {
    allowed_methods = [
      "GET",
      "HEAD",
    ]
    cached_methods = [
      "GET",
      "HEAD"
    ]
    target_origin_id = "xjmusic-com-dev-podcast-s3-origin"

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
      headers = []
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
  }

  restrictions {
    geo_restriction {
      restriction_type = "blacklist"
      locations = [
        "CN"
      ]
    }
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate.xjmusic-com-dev.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "dev"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xjmusic-com-podcast" {
  bucket = "podcast.xjmusic.com"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2008-10-17",
    "Statement" : [
      {
        Sid       = "PublicReadGetObject",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::podcast.xjmusic.com/*"
      }
    ]
  })

  website {
    index_document = "feed.xml"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xjmusic-com-dev-podcast" {
  bucket = "podcast.dev.xjmusic.com"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2008-10-17",
    "Statement" : [
      {
        Sid       = "PublicReadGetObject",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::podcast.dev.xjmusic.com/*"
      }
    ]
  })

  website {
    index_document = "feed.xml"
  }
}

