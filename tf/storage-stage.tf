# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-stage-audio" {
  bucket = "xj-stage-audio"
  acl    = "public-read"
  policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Id" : "xjAudioBucketPolicyStage",
      "Statement" : [
        {
          Sid    = "AuthenticatedCanWrite",
          Effect = "Allow",
          Principal = {
            "AWS" : aws_iam_user.xj-stage.arn
          },
          Action = [
            "s3:Get*",
            "s3:Put*",
            "s3:DeleteObject"
          ],
          Resource = "arn:aws:s3:::xj-stage-audio/*"
        },
        {
          Sid       = "AnyoneCanRead",
          Effect    = "Allow",
          Principal = "*",
          Action    = "s3:GetObject",
          Resource  = "arn:aws:s3:::xj-stage-audio/*"
        },
        {
          Sid    = "AdminAccess",
          Effect = "Allow",
          Principal = {
            "AWS" : "arn:aws:iam::${local.aws-account-id}:user/charney"
          },
          Action = "s3:*",
          Resource = [
            "arn:aws:s3:::xj-stage-audio",
            "arn:aws:s3:::xj-stage-audio/*"
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
      "https://lab.stage.xj.io"
    ]
    expose_headers = [
    ]
    max_age_seconds = 3000
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-stage-ship" {
  bucket = "xj-stage-ship"
  acl    = "public-read"
  policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Id" : "xjShipBucketPolicyStage",
      "Statement" : [
        {
          Sid    = "AuthenticatedCanWrite",
          Effect = "Allow",
          Principal = {
            "AWS" : aws_iam_user.xj-stage.arn
          },
          Action = [
            "s3:Get*",
            "s3:Put*",
            "s3:DeleteObject"
          ],
          Resource = "arn:aws:s3:::xj-stage-ship/*"
        },
        {
          Sid       = "AnyoneCanRead",
          Effect    = "Allow",
          Principal = "*",
          Action    = "s3:GetObject",
          Resource  = "arn:aws:s3:::xj-stage-ship/*"
        },
        {
          Sid    = "AdminAccess",
          Effect = "Allow",
          Principal = {
            "AWS" : "arn:aws:iam::${local.aws-account-id}:user/charney"
          },
          Action = "s3:*",
          Resource = [
            "arn:aws:s3:::xj-stage-ship",
            "arn:aws:s3:::xj-stage-ship/*"
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
    expose_headers  = []
    max_age_seconds = 3000
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "xj-stage-static" {
  bucket = "xj-stage-static"
  acl    = "public-read"
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Id" : "xjAudioBucketPolicyStage",
    "Statement" : [
      {
        Sid    = "AuthenticatedCanWrite",
        Effect = "Allow",
        Principal = {
          "AWS" : aws_iam_user.xj-stage.arn
        },
        Action = [
          "s3:Get*",
          "s3:Put*",
          "s3:DeleteObject"
        ],
        Resource = "arn:aws:s3:::xj-stage-static/*"
      },
      {
        Sid       = "AnyoneCanRead",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "arn:aws:s3:::xj-stage-static/*"
      },
      {
        Sid    = "AdminAccess",
        Effect = "Allow",
        Principal = {
          "AWS" : "arn:aws:iam::${local.aws-account-id}:user/charney"
        },
        Action = "s3:*",
        Resource = [
          "arn:aws:s3:::xj-stage-static",
          "arn:aws:s3:::xj-stage-static/*"
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
