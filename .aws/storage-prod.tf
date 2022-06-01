# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-io" {
  bucket = "xj.io"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2008-10-17",
    "Statement" : [
      {
        Sid       = "PublicReadGetObject",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::xj.io/*"
      }
    ]
  })

  website {
    index_document = "index.html"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-play" {
  bucket = "play.xj.io"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2008-10-17",
    "Statement" : [
      {
        Sid       = "PublicReadGetObject",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::play.xj.io/*"
      }
    ]
  })

  website {
    index_document = "index.html"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-help" {
  bucket = "help.xj.io"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2008-10-17",
    "Statement" : [
      {
        Sid       = "PublicReadGetObject",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::help.xj.io/*"
      }
    ]
  })

  website {
    index_document = "index.html"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-content" {
  bucket = "content.xj.io"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2008-10-17",
    "Statement" : [
      {
        Sid       = "PublicReadGetObject",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::content.xj.io/*"
      }
    ]
  })

  website {
    index_document = "content.json"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-status" {
  bucket = "status.xj.io"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2008-10-17",
    "Statement" : [
      {
        Sid       = "PublicReadGetObject",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::status.xj.io/*"
      }
    ]
  })

  website {
    index_document = "index.html"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-redirect" {
  bucket = "redirect-to-xj-io"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2008-10-17",
    "Statement" : [
      {
        Sid       = "PublicReadGetObject",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::redirect-to-xj-io/*"
      }
    ]
  })

  website {
    redirect_all_requests_to = "https://xj.io"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-redirect-lab" {
  bucket = "redirect-to-lab-xj-io"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2008-10-17",
    "Statement" : [
      {
        Sid       = "PublicReadGetObject",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::redirect-to-lab-xj-io/*"
      }
    ]
  })

  website {
    redirect_all_requests_to = "https://lab.xj.io"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-prod-audio" {
  bucket = "xj-prod-audio"
  acl    = "public-read"
  policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Id" : "xjAudioBucketPolicyProd",
      "Statement" : [
        {
          Sid    = "AuthenticatedCanWrite",
          Effect = "Allow",
          Principal = {
            "AWS" : aws_iam_user.xj-prod.arn
          },
          Action = [
            "s3:Get*",
            "s3:Put*",
            "s3:DeleteObject"
          ],
          Resource = "arn:aws:s3:::xj-prod-audio/*"
        },
        {
          Sid       = "AnyoneCanRead",
          Effect    = "Allow",
          Principal = "*",
          Action    = "s3:GetObject",
          Resource  = "arn:aws:s3:::xj-prod-audio/*"
        },
        {
          Sid    = "AdminAccess",
          Effect = "Allow",
          Principal = {
            "AWS" : "arn:aws:iam::${local.aws-account-id}:user/charney"
          },
          Action = "s3:*",
          Resource = [
            "arn:aws:s3:::xj-prod-audio",
            "arn:aws:s3:::xj-prod-audio/*"
          ]
        }
      ]
    }
  )

  cors_rule {
    allowed_headers = [
      "*",
    ]
    allowed_methods = [
      "GET",
      "HEAD",
      "POST",
    ]
    allowed_origins = [
      "https://lab.xj.io",
      "lab.xj.io",
    ]
    expose_headers = [
      "Access-Control-Allow-Origin",
    ]
    max_age_seconds = 3000
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-prod-ship" {
  bucket = "xj-prod-ship"
  acl    = "public-read"
  policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Id" : "xjShipBucketPolicyProd",
      "Statement" : [
        {
          Sid    = "AuthenticatedCanWrite",
          Effect = "Allow",
          Principal = {
            "AWS" : aws_iam_user.xj-prod.arn
          },
          Action = [
            "s3:Get*",
            "s3:Put*",
            "s3:DeleteObject"
          ],
          Resource = "arn:aws:s3:::xj-prod-ship/*"
        },
        {
          Sid       = "AnyoneCanRead",
          Effect    = "Allow",
          Principal = "*",
          Action    = "s3:GetObject",
          Resource  = "arn:aws:s3:::xj-prod-ship/*"
        },
        {
          Sid    = "AdminAccess",
          Effect = "Allow",
          Principal = {
            "AWS" : "arn:aws:iam::${local.aws-account-id}:user/charney"
          },
          Action = "s3:*",
          Resource = [
            "arn:aws:s3:::xj-prod-ship",
            "arn:aws:s3:::xj-prod-ship/*"
          ]
        }
      ]
    }
  )

  cors_rule {
    allowed_headers = [
      "*"
    ]
    allowed_methods = [
      "GET",
      "HEAD"
    ]
    allowed_origins = [
      "*"
    ]
    max_age_seconds = 3000
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-prod-stream" {
  bucket = "xj-prod-stream"
  acl    = "public-read"
  policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Id" : "xjStreamBucketPolicyProd",
      "Statement" : [
        {
          Sid    = "AuthenticatedCanWrite",
          Effect = "Allow",
          Principal = {
            "AWS" : aws_iam_user.xj-prod.arn
          },
          Action = [
            "s3:Get*",
            "s3:Put*",
            "s3:DeleteObject"
          ],
          Resource = "arn:aws:s3:::xj-prod-stream/*"
        },
        {
          Sid       = "AnyoneCanRead",
          Effect    = "Allow",
          Principal = "*",
          Action    = "s3:GetObject",
          Resource  = "arn:aws:s3:::xj-prod-stream/*"
        },
        {
          Sid    = "AdminAccess",
          Effect = "Allow",
          Principal = {
            "AWS" : "arn:aws:iam::${local.aws-account-id}:user/charney"
          },
          Action = "s3:*",
          Resource = [
            "arn:aws:s3:::xj-prod-stream",
            "arn:aws:s3:::xj-prod-stream/*"
          ]
        }
      ]
    }
  )

  website {
    index_document = "index.html"
    error_document = "error.html"
    routing_rules = <<EOF
[{
    "Condition": {
        "KeyPrefixEquals": "coolair.m3u8"
    },
    "Redirect": {
        "ReplaceKeyPrefixWith": "bump_deep.m3u8"
    }
},{
    "Condition": {
        "KeyPrefixEquals": "openspace.m3u8"
    },
    "Redirect": {
        "ReplaceKeyPrefixWith": "space_flow.m3u8"
    }
}]
EOF
  }

  cors_rule {
    allowed_headers = [
      "*"
    ]
    allowed_methods = [
      "GET",
      "HEAD"
    ]
    allowed_origins = [
      "*"
    ]
    max_age_seconds = 3000
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-prod-static" {
  bucket = "xj-prod-static"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Id" : "xjAudioBucketPolicyProd",
    "Statement" : [
      {
        Sid    = "AuthenticatedCanWrite",
        Effect = "Allow",
        Principal = {
          "AWS" : aws_iam_user.xj-prod.arn
        },
        Action = [
          "s3:Get*",
          "s3:Put*",
          "s3:DeleteObject"
        ],
        Resource = "arn:aws:s3:::xj-prod-static/*"
      },
      {
        Sid       = "AnyoneCanRead",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::xj-prod-static/*"
      },
      {
        Sid    = "AdminAccess",
        Effect = "Allow",
        Principal = {
          "AWS" : "arn:aws:iam::${local.aws-account-id}:user/charney"
        },
        Action = "s3:*",
        Resource = [
          "arn:aws:s3:::xj-prod-static",
          "arn:aws:s3:::xj-prod-static/*"
        ]
      }
    ]
  })

  cors_rule {
    allowed_headers = [
      "Authorization"
    ]
    allowed_methods = [
      "GET"
    ]
    allowed_origins = [
      "*"
    ]
    expose_headers  = []
    max_age_seconds = 3000
  }

  website {
    index_document = "index.html"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-lab" {
  bucket = "lab.xj.io"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2008-10-17",
    "Statement" : [
      {
        Sid       = "PublicReadGetObject",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource = [
          "arn:aws:s3:::lab.xj.io/*",
          "arn:aws:s3:::lab.xj.io",
        ]
      }
    ]
  })

  website {
    index_document = "index.html"
    error_document = "index.html"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "uxrg-prod" {
  bucket = "uxresearchgroup.net"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2008-10-17",
    "Statement" : [
      {
        Sid       = "PublicReadGetObject",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::uxresearchgroup.net/*"
      }
    ]
  })

  website {
    index_document = "index.html"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "uxrg-redirect" {
  bucket = "redirect-to-uxresearchgroup-net"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2008-10-17",
    "Statement" : [
      {
        Sid       = "PublicReadGetObject",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::redirect-to-uxresearchgroup-net/*"
      }
    ]
  })

  website {
    redirect_all_requests_to = "https://uxresearchgroup.net"
  }
}

