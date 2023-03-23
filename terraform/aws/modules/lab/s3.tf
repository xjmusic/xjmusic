# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "website_bucket" {
  count = local.create_bucket ? 1 : 0
  bucket = var.bucket
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_website_configuration
resource "aws_s3_bucket_website_configuration" "public_bucket_website_configuration" {
  count = local.create_bucket ? 1 : 0
  bucket = aws_s3_bucket.website_bucket[0].bucket
  index_document {
    suffix = var.index_document
  }
  error_document {
    key = var.error_document
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_acl
resource "aws_s3_bucket_acl" "public_bucket_acl" {
  count = local.create_bucket ? 1 : 0
  bucket = aws_s3_bucket.website_bucket[0].bucket
  acl    = "public-read"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_policy
resource "aws_s3_bucket_policy" "public_bucket_policy" {
  count = local.create_bucket ? 1 : 0
  bucket = aws_s3_bucket.website_bucket[0].bucket
  policy = data.aws_iam_policy_document.public_bucket_policy_document[0].json
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document
data "aws_iam_policy_document" "public_bucket_policy_document" {
  count = local.create_bucket ? 1 : 0
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
