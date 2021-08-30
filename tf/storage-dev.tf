# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-dev-audio" {
  bucket = "xj-dev-audio"
  acl    = "public-read"
  policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Id" : "xjAudioBucketPolicyDev",
      "Statement" : [
        {
          Sid    = "AuthenticatedCanWrite",
          Effect = "Allow",
          Principal = {
            "AWS" : aws_iam_user.xj-dev.arn
          },
          Action = [
            "s3:Get*",
            "s3:Put*",
            "s3:DeleteObject"
          ],
          Resource = "arn:aws:s3:::xj-dev-audio/*"
        },
        {
          Sid       = "AnyoneCanRead",
          Effect    = "Allow",
          Principal = "*",
          Action    = "s3:GetObject",
          Resource  = "arn:aws:s3:::xj-dev-audio/*"
        },
        {
          Sid    = "AdminAccess",
          Effect = "Allow",
          Principal = {
            "AWS" : "arn:aws:iam::${local.aws-account-id}:user/charney"
          },
          Action = "s3:*",
          Resource = [
            "arn:aws:s3:::xj-dev-audio",
            "arn:aws:s3:::xj-dev-audio/*"
          ]
        }
      ]
    }
  )

  cors_rule {
    allowed_headers = [
      "Authorization"
    ]
    allowed_methods = [
      "GET",
      "HEAD",
      "POST"
    ]
    allowed_origins = [
      "*",
    ]
    expose_headers = [
    ]
    max_age_seconds = 3000
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-dev-ship" {
  bucket = "xj-dev-ship"
  acl    = "public-read"
  policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Id" : "xjShipBucketPolicyDev",
      "Statement" : [
        {
          Sid    = "AuthenticatedCanWrite",
          Effect = "Allow",
          Principal = {
            "AWS" : aws_iam_user.xj-dev.arn
          },
          Action = [
            "s3:Get*",
            "s3:Put*",
            "s3:DeleteObject"
          ],
          Resource = "arn:aws:s3:::xj-dev-ship/*"
        },
        {
          Sid       = "AnyoneCanRead",
          Effect    = "Allow",
          Principal = "*",
          Action    = "s3:GetObject",
          Resource  = "arn:aws:s3:::xj-dev-ship/*"
        },
        {
          Sid    = "AdminAccess",
          Effect = "Allow",
          Principal = {
            "AWS" : "arn:aws:iam::${local.aws-account-id}:user/charney"
          },
          Action = "s3:*",
          Resource = [
            "arn:aws:s3:::xj-dev-ship",
            "arn:aws:s3:::xj-dev-ship/*"
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
    expose_headers = [
    ]
    max_age_seconds = 3000
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-dev-static" {
  bucket = "xj-dev-static"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Id" : "xjAudioBucketPolicyDev",
    "Statement" : [
      {
        Sid    = "AuthenticatedCanWrite",
        Effect = "Allow",
        Principal = {
          "AWS" : aws_iam_user.xj-dev.arn
        },
        Action = [
          "s3:Get*",
          "s3:Put*",
          "s3:DeleteObject"
        ],
        Resource = "arn:aws:s3:::xj-dev-static/*"
      },
      {
        Sid       = "AnyoneCanRead",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::xj-dev-static/*"
      },
      {
        Sid    = "AdminAccess",
        Effect = "Allow",
        Principal = {
          "AWS" : "arn:aws:iam::${local.aws-account-id}:user/charney"
        },
        Action = "s3:*",
        Resource = [
          "arn:aws:s3:::xj-dev-static",
          "arn:aws:s3:::xj-dev-static/*"
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
resource "aws_s3_bucket" "xj-dev" {
  bucket = "dev.xj.io"
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
          "arn:aws:s3:::dev.xj.io/*",
          "arn:aws:s3:::dev.xj.io",
        ]
      }
    ]
  })

  website {
    index_document = "index.html"
    error_document = "index.html"
  }
}


