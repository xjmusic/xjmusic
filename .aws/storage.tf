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
