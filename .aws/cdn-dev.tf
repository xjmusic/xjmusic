# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-dev" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "dev.xj.io"
  default_root_object = "index.html"
  http_version        = "http2"
  price_class         = "PriceClass_100"
  aliases = [
    "dev.xj.io"
  ]

  origin {
    # AWS Cloudfront won't properly resolve /index.html files unless the full region is specified here:
    domain_name = "${aws_s3_bucket.xj-dev.bucket}.s3-website-${local.aws-region}.amazonaws.com"
    origin_id   = "xj-dev-s3-origin"
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
    target_origin_id = "xj-dev-s3-origin"

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
    Environment = "dev"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-dev-lab" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "lab.dev.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "lab.dev.xj.io"
  ]

  origin {
    domain_name = "${aws_s3_bucket.xj-dev-lab.bucket}.s3-website-${local.aws-region}.amazonaws.com"
    origin_id   = "xj-dev-lab-s3-origin"
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
    domain_name = "ae4e7b4ec1c20491b88a46dd9ece0308-328336479.us-east-1.elb.amazonaws.com"
    origin_id   = "xj-dev-hub-origin"
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
    target_origin_id       = "xj-dev-lab-s3-origin"
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
    target_origin_id       = "xj-dev-hub-origin"
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
    target_origin_id       = "xj-dev-hub-origin"
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
    target_origin_id       = "xj-dev-hub-origin"
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
    target_origin_id       = "xj-dev-hub-origin"
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
    acm_certificate_arn      = aws_acm_certificate.xj-environments.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "dev"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-dev-help" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "help.dev.xj.io"
  default_root_object = "index.html"
  http_version        = "http2"
  price_class         = "PriceClass_100"
  aliases = [
    "help.dev.xj.io"
  ]

  origin {
    domain_name = "${aws_s3_bucket.xj-dev-help.bucket}.s3-website-${local.aws-region}.amazonaws.com"
    origin_id   = "xj-dev-help-s3-origin"
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
    target_origin_id = "xj-dev-help-s3-origin"

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
    acm_certificate_arn      = aws_acm_certificate.xj-environments.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "prod"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-dev-status" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "status.dev.xj.io"
  default_root_object = "index.html"
  http_version        = "http2"
  price_class         = "PriceClass_100"
  aliases = [
    "status.dev.xj.io"
  ]

  origin {
    domain_name = "${aws_s3_bucket.xj-dev-status.bucket}.s3-website-${local.aws-region}.amazonaws.com"
    origin_id   = "xj-dev-status-s3-origin"
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
    target_origin_id = "xj-dev-status-s3-origin"

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
    acm_certificate_arn      = aws_acm_certificate.xj-environments.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "prod"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-dev-local" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "local.dev.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "local.dev.xj.io"
  ]

  origin {
    // noinspection HILUnresolvedReference
    domain_name = "a198fa20233514fdd89cfe8efbab3aec-1592877423.us-east-1.elb.amazonaws.com"
    origin_id   = "xj-dev-local-hub-origin"
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
    target_origin_id       = "xj-dev-local-hub-origin"
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
    target_origin_id       = "xj-dev-local-hub-origin"
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
    target_origin_id       = "xj-dev-local-hub-origin"
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

  default_cache_behavior {
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
    target_origin_id       = "xj-dev-local-hub-origin"
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
    acm_certificate_arn      = aws_acm_certificate.xj-environments.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "dev"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-dev-audio" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "audio.dev.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "audio.dev.xj.io"
  ]

  origin {
    domain_name = aws_s3_bucket.xj-dev-audio.bucket_regional_domain_name
    origin_id   = "audio-dev-xj-io-s3-origin"
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
    target_origin_id = "audio-dev-xj-io-s3-origin"

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
    acm_certificate_arn      = aws_acm_certificate.xj-environments.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "dev"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-dev-ship" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "ship.dev.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "ship.dev.xj.io"
  ]

  origin {
    domain_name = aws_s3_bucket.xj-dev-ship.bucket_regional_domain_name
    origin_id   = "ship-dev-xj-io-s3-origin"
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
    target_origin_id       = "ship-dev-xj-io-s3-origin"
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
    default_ttl            = 10
    max_ttl                = 20
    min_ttl                = 1
    path_pattern           = "*-*.*"
    target_origin_id       = "ship-dev-xj-io-s3-origin"
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
    acm_certificate_arn      = aws_acm_certificate.xj-environments.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "dev"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-dev-stream" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "stream.dev.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "stream.dev.xj.io"
  ]

  origin {
    domain_name = aws_s3_bucket.xj-dev-stream.bucket_regional_domain_name
    origin_id   = "stream-dev-xj-io-s3-origin"
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
    target_origin_id       = "stream-dev-xj-io-s3-origin"
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
    default_ttl            = 10
    max_ttl                = 20
    min_ttl                = 1
    path_pattern           = "*-*.*"
    target_origin_id       = "stream-dev-xj-io-s3-origin"
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
    acm_certificate_arn      = aws_acm_certificate.xj-environments.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "dev"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "xj-dev-static" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "static.dev.xj.io"
  http_version    = "http2"
  price_class     = "PriceClass_100"
  aliases = [
    "static.dev.xj.io"
  ]

  origin {
    domain_name = aws_s3_bucket.xj-dev-static.bucket_regional_domain_name
    origin_id   = "xj-dev-static-s3-origin"
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
    target_origin_id = "xj-dev-static-s3-origin"

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
    acm_certificate_arn      = aws_acm_certificate.xj-environments.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "dev"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
resource "aws_cloudfront_distribution" "uxrg-dev" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "dev.uxresearchgroup.net"
  default_root_object = "index.html"
  http_version        = "http2"
  price_class         = "PriceClass_100"
  aliases = [
    "dev.uxresearchgroup.net"
  ]

  origin {
    # AWS Cloudfront won't properly resolve /index.html files unless the full region is specified here:
    domain_name = "${aws_s3_bucket.uxrg-dev.bucket}.s3-website-${local.aws-region}.amazonaws.com"
    origin_id   = "uxresearchgroup-dev-s3-origin"
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
    target_origin_id = "uxresearchgroup-dev-s3-origin"

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
    acm_certificate_arn      = aws_acm_certificate.uxrg.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = {
    Environment = "prod"
  }
}

