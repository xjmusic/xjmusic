# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "website_bucket" {
  bucket = var.bucket
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_website_configuration
resource "aws_s3_bucket_website_configuration" "public_bucket_website_configuration" {
  bucket = aws_s3_bucket.website_bucket.bucket
  index_document {
    suffix = var.index_document
  }
  error_document {
    key = var.error_document
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_acl
resource "aws_s3_bucket_acl" "public_bucket_acl" {
  bucket = aws_s3_bucket.website_bucket.bucket
  acl    = "public-read"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_policy
resource "aws_s3_bucket_policy" "public_bucket_policy" {
  bucket = aws_s3_bucket.website_bucket.bucket
  policy = data.aws_iam_policy_document.public_bucket_policy_document.json
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document
data "aws_iam_policy_document" "public_bucket_policy_document" {
  version   = "2008-10-17"
  policy_id = "${var.bucket}-policy"
  statement {
    sid    = "PublicReadGetObject"
    effect = "Allow"
    principals {
      identifiers = ["*"]
      type        = "*"
    }
    actions = [
      "s3:GetObject",
    ]
    resources = [
      "arn:aws:s3:::${var.bucket}",
      "arn:aws:s3:::${var.bucket}/*",
    ]
  }
}

