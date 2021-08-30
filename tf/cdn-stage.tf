# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-stage-audio" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "audio.stage.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "audio.stage.xj.io"
  ]

  origin {
    domain_name = aws_s3_bucket.xj-stage-audio.bucket_regional_domain_name
    origin_id   = "audio-stage-xj-io-s3-origin"
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
    target_origin_id = "audio-stage-xj-io-s3-origin"

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
    Environment = "stage"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-stage-ship" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "ship.stage.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "ship.stage.xj.io"
  ]

  origin {
    domain_name = aws_s3_bucket.xj-stage-ship.bucket_regional_domain_name
    origin_id   = "ship-stage-xj-io-s3-origin"
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
      "OPTIONS",
    ]
    cached_methods = [
      "GET",
      "HEAD",
      "OPTIONS",
    ]
    compress               = true
    default_ttl            = 10
    max_ttl                = 20
    min_ttl                = 1
    target_origin_id       = "ship-stage-xj-io-s3-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers = [
        "Origin",
        "Access-Control-Request-Method",
        "Access-Control-Request-Headers",
      ]
      query_string = false
      cookies {
        forward = "none"
      }
    }
  }

  # Cache behavior with precedence 1
  ordered_cache_behavior {
    allowed_methods = [
      "GET",
      "HEAD",
      "OPTIONS",
    ]
    cached_methods = [
      "GET",
      "HEAD",
      "OPTIONS",
    ]
    compress               = true
    default_ttl            = 3600
    max_ttl                = 86400
    min_ttl                = 0
    path_pattern           = "*-*.*"
    target_origin_id       = "ship-stage-xj-io-s3-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers = [
        "Origin",
        "Access-Control-Request-Method",
        "Access-Control-Request-Headers",
      ]
      query_string = false
      cookies {
        forward = "none"
      }
    }
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
    Environment = "stage"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-stage-static" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "static.stage.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "static.stage.xj.io"
  ]

  origin {
    domain_name = aws_s3_bucket.xj-stage-static.bucket_regional_domain_name
    origin_id   = "xj-stage-static-s3-origin"
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
    target_origin_id = "xj-stage-static-s3-origin"

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
    Environment = "stage"
  }
}
