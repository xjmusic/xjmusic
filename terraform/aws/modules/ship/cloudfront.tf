# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "distribution" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = var.aliases[0]
  default_root_object = var.index_document
  http_version        = "http2"
  price_class         = "PriceClass_100"
  aliases             = var.aliases

  origin {
    # AWS Cloudfront won't properly resolve /index.html files unless the full region is specified here:
    domain_name = "${var.bucket}.s3-website-${var.region}.amazonaws.com"
    origin_id   = "s3-origin"
    origin_path = ""
    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols   = [
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
    default_ttl            = 1
    max_ttl                = 1
    min_ttl                = 1
    target_origin_id       = "s3-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers = [
        "Access-Control-Request-Headers",
        "Access-Control-Request-Method",
        "Content-Length",
        "Content-Type",
        "Origin",
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
      "OPTIONS"
    ]
    cached_methods = [
      "GET",
      "HEAD",
      "OPTIONS"
    ]
    compress               = true
    default_ttl            = 2
    max_ttl                = 2
    min_ttl                = 2
    path_pattern           = "*-*.*"
    target_origin_id       = "s3-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers = [
        "Access-Control-Request-Headers",
        "Access-Control-Request-Method",
        "Content-Length",
        "Content-Type",
        "Origin",
      ]
      query_string = false
      cookies {
        forward = "none"
      }
    }
  }


  restrictions {
    geo_restriction {
      restriction_type = "blacklist"
      locations        = var.blacklist_locations
    }
  }

  viewer_certificate {
    acm_certificate_arn      = var.acm_certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }
}

