# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-lab" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "lab.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "lab.xj.io"
  ]

  origin {
    domain_name = "${aws_s3_bucket.xj-lab.bucket}.s3-website-${local.aws-region}.amazonaws.com"
    origin_id   = "xj-lab-s3-origin"
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

  origin {
    domain_name = "${aws_s3_bucket.xj-lab-mk1.bucket}.s3-website-${local.aws-region}.amazonaws.com"
    origin_id   = "xj-lab-mk1-s3-origin"
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

  origin {
    // noinspection HILUnresolvedReference
    domain_name = kubernetes_service.xj-prod-lab-hub.status.0.load_balancer.0.ingress.0.hostname
    // domain_name = "ec2-54-92-183-119.compute-1.amazonaws.com"
    origin_id   = "xj-lab-hub-origin"
    origin_path = ""
    custom_origin_config {
      http_port              = 8080
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
    target_origin_id       = "xj-lab-s3-origin"
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
    path_pattern = "auth"
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
    target_origin_id       = "xj-lab-hub-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers = [
        "Origin",
        "Accept",
        "Cache-Control",
        "Referer",
        "Accept-Language",
        "Pragma",
        "Content-Type",
      ]
      query_string = true
      cookies {
        forward = "whitelist"
        whitelisted_names = [
          "access_token"
        ]
      }
    }
  }

  ordered_cache_behavior {
    path_pattern = "config"
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
    target_origin_id       = "xj-lab-hub-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers = [
        "Origin",
        "Accept",
        "Cache-Control",
        "Referer",
        "Accept-Language",
        "Pragma",
        "Content-Type",
      ]
      query_string = false
      cookies {
        forward = "whitelist"
        whitelisted_names = [
          "access_token"
        ]
      }
    }
  }

  ordered_cache_behavior {
    path_pattern = "auth/*"
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
    target_origin_id       = "xj-lab-hub-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers = [
        "Origin",
        "Accept",
        "Cache-Control",
        "Referer",
        "Accept-Language",
        "Pragma",
        "Content-Type",
      ]
      query_string = true
      cookies {
        forward = "whitelist"
        whitelisted_names = [
          "access_token"
        ]
      }
    }
  }

  ordered_cache_behavior {
    path_pattern = "mk1/*"
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
    default_ttl            = 86400
    max_ttl                = 31536000
    min_ttl                = 1
    target_origin_id       = "xj-lab-mk1-s3-origin"
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
    path_pattern = "api/1/*"
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
    target_origin_id       = "xj-lab-hub-origin"
    viewer_protocol_policy = "redirect-to-https"
    forwarded_values {
      headers = [
        "Origin",
        "Accept",
        "Cache-Control",
        "Referer",
        "Accept-Language",
        "Pragma",
        "Content-Type",
      ]
      query_string = true
      cookies {
        forward = "whitelist"
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
    acm_certificate_arn      = aws_acm_certificate.xj-io.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "prod"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-lab-redirect" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "redirect to lab.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "app.xj.io",
    "hub.xj.io",
  ]

  origin {
    # AWS Cloudfront won't properly resolve /index.html files unless the full region is specified here:
    domain_name = "${aws_s3_bucket.xj-redirect-lab.bucket}.s3-website-${local.aws-region}.amazonaws.com"
    origin_id   = "redirect-to-lab-xj-io-s3-origin"
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
    target_origin_id = "redirect-to-lab-xj-io-s3-origin"

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
    acm_certificate_arn      = aws_acm_certificate.xj-io-lab-redirect.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "prod"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-prod-audio" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "audio.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "audio.xj.io"
  ]

  origin {
    domain_name = aws_s3_bucket.xj-prod-audio.bucket_regional_domain_name
    origin_id   = "audio-prod-xj-io-s3-origin"
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
    target_origin_id = "audio-prod-xj-io-s3-origin"

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
resource "aws_cloudfront_distribution" "xj-prod-ship" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "ship.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "ship.xj.io"
  ]

  origin {
    domain_name = aws_s3_bucket.xj-prod-ship.bucket_regional_domain_name
    origin_id   = "ship-prod-xj-io-s3-origin"
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
    default_ttl            = 2
    max_ttl                = 2
    min_ttl                = 2
    target_origin_id       = "ship-prod-xj-io-s3-origin"
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
    target_origin_id       = "ship-prod-xj-io-s3-origin"
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
    Environment = "prod"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-prod-stream" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "stream.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "stream.xj.io"
  ]

  origin {
    domain_name = aws_s3_bucket.xj-prod-stream.bucket_regional_domain_name
    origin_id   = "stream-prod-xj-io-s3-origin"
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
    default_ttl            = 1
    max_ttl                = 1
    min_ttl                = 1
    target_origin_id       = "stream-prod-xj-io-s3-origin"
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
      "OPTIONS"
    ]
    cached_methods = [
      "GET",
      "HEAD",
      "OPTIONS"
    ]
    compress               = true
    default_ttl            = 3600
    max_ttl                = 86400
    min_ttl                = 0
    path_pattern           = "*-*.*"
    target_origin_id       = "stream-prod-xj-io-s3-origin"
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
    Environment = "prod"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-prod-static" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "static.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "static.xj.io"
  ]

  origin {
    domain_name = aws_s3_bucket.xj-prod-static.bucket_regional_domain_name
    origin_id   = "xj-prod-static-s3-origin"
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
    target_origin_id = "xj-prod-static-s3-origin"

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
