# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "distribution" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = var.aliases[0]
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases         = var.aliases

  origin {
    domain_name = local.s3_bucket_regional_domain_name
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

  origin {
    domain_name = var.hub_origin_domain_name
    origin_id   = "hub-origin"
    origin_path = ""
    custom_origin_config {
      http_port              = 8080
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
      "OPTIONS"
    ]
    cached_methods = [
      "GET",
      "HEAD",
      "OPTIONS"
    ]
    compress               = true
    default_ttl            = 86400
    max_ttl                = 31536000
    min_ttl                = 1
    target_origin_id       = "s3-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers      = []
      query_string = false
      cookies {
        forward = "none"
      }
    }
  }

  ordered_cache_behavior {
    path_pattern    = "auth"
    allowed_methods = [
      "GET",
      "HEAD",
      "OPTIONS"
    ]
    cached_methods = [
      "GET",
      "HEAD",
    ]
    compress               = true
    default_ttl            = 0
    max_ttl                = 0
    min_ttl                = 0
    target_origin_id       = "hub-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers = [
        "Accept",
        "Accept-Language",
        "Cache-Control",
        "Content-Length",
        "Content-Type",
        "Origin",
        "Pragma",
        "Referer",
      ]
      query_string = true
      cookies {
        forward           = "whitelist"
        whitelisted_names = [
          "access_token"
        ]
      }
    }
  }

  ordered_cache_behavior {
    path_pattern    = "config"
    allowed_methods = [
      "GET",
      "HEAD",
      "OPTIONS"
    ]
    cached_methods = [
      "GET",
      "HEAD",
    ]
    compress               = true
    default_ttl            = 0
    max_ttl                = 0
    min_ttl                = 0
    target_origin_id       = "hub-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers = [
        "Accept",
        "Accept-Language",
        "Cache-Control",
        "Content-Length",
        "Content-Type",
        "Origin",
        "Pragma",
        "Referer",
      ]
      query_string = false
      cookies {
        forward           = "whitelist"
        whitelisted_names = [
          "access_token"
        ]
      }
    }
  }

  ordered_cache_behavior {
    path_pattern    = "auth/*"
    allowed_methods = [
      "GET",
      "HEAD",
      "OPTIONS",
      "PUT",
      "POST",
      "PATCH",
      "DELETE",
    ]
    cached_methods = [
      "GET",
      "HEAD",
    ]
    compress               = true
    default_ttl            = 0
    max_ttl                = 0
    min_ttl                = 0
    target_origin_id       = "hub-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers = [
        "Accept",
        "Accept-Language",
        "Cache-Control",
        "Content-Length",
        "Content-Type",
        "Origin",
        "Pragma",
        "Referer",
      ]
      query_string = true
      cookies {
        forward           = "whitelist"
        whitelisted_names = [
          "access_token"
        ]
      }
    }
  }

  ordered_cache_behavior {
    path_pattern    = "api/1/*"
    allowed_methods = [
      "GET",
      "HEAD",
      "OPTIONS",
      "PUT",
      "POST",
      "PATCH",
      "DELETE",
    ]
    cached_methods = [
      "GET",
      "HEAD",
    ]
    compress               = true
    default_ttl            = 0
    max_ttl                = 0
    min_ttl                = 0
    target_origin_id       = "hub-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers = [
        "Accept",
        "Accept-Language",
        "Cache-Control",
        "Content-Length",
        "Content-Type",
        "Origin",
        "Pragma",
        "Referer",
      ]
      query_string = true
      cookies {
        forward           = "whitelist"
        whitelisted_names = [
          "access_token"
        ]
      }
    }
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = var.acm_certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "prod"
  }
}
