# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-io" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "xj.io"
  default_root_object = "index.html"
  http_version        = "http2"
  price_class         = "PriceClass_100"
  aliases = [
    "xj.io"
  ]

  origin {
    # AWS Cloudfront won't properly resolve /index.html files unless the full region is specified here:
    domain_name = "${aws_s3_bucket.xj-io.bucket}.s3-website-${local.aws-region}.amazonaws.com"
    origin_id   = "xj-io-s3-origin"
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
    target_origin_id = "xj-io-s3-origin"

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
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate.xj-io.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "prod"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-redirect" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "redirect to xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"

  aliases = [
    "w.xj.io",
    "www.xj.io",
    "www.xjplatform.com",
    "xj.outright.io",
    "xjplatform.com",
  ]

  origin {
    # AWS Cloudfront won't properly resolve /index.html files unless the full region is specified here:
    domain_name = "${aws_s3_bucket.xj-redirect.bucket}.s3-website-${local.aws-region}.amazonaws.com"
    origin_id   = "redirect-to-xj-io-s3-origin"
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
    target_origin_id = "redirect-to-xj-io-s3-origin"

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
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate.xj-io-redirect.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "prod"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-play" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "play.xj.io"
  default_root_object = "index.html"
  http_version        = "http2"
  price_class         = "PriceClass_100"
  aliases = [
    "play.xj.io"
  ]

  origin {
    domain_name = aws_s3_bucket.xj-play.bucket_regional_domain_name
    origin_id   = "xj-play-s3-origin"
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
    target_origin_id = "xj-play-s3-origin"

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
    acm_certificate_arn      = aws_acm_certificate.xj-io.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "prod"
  }
}
