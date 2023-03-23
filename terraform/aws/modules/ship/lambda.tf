# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/archive/latest/docs/data-sources/archive_file
data "archive_file" "lambda_ffmpeg" {
  type             = "zip"
  source_dir       = "${path.module}/lambda/ffmpeg"
  output_file_mode = "0666"
  output_path      = "${path.module}/build/lambda_ffmpeg.zip"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_function
resource "aws_lambda_layer_version" "ffmpeg" {
  layer_name          = "ffmpeg"
  description         = "ffmpeg layer for Lambda functions running on Amazon Linux"
  filename            = data.archive_file.lambda_ffmpeg.output_path
  compatible_runtimes = ["python3.8"]
  license_info        = "https://www.ffmpeg.org/legal.html"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "lambda_shipped_file_compressor_src" {
  bucket = "xj-shipped-file-compressor-src"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_function
resource "aws_lambda_function" "shipped_file_compressor_ogg_to_mp3" {
  function_name = "shipped_file_compressor_ogg_to_mp3"
  description   = "When an .ogg file is shipped, compress it to .mp3"
  s3_bucket     = aws_s3_bucket.lambda_shipped_file_compressor_src.bucket
  s3_key        = "shipped_file_compressor.zip"
  role          = aws_iam_role.lambda_runner.arn
  handler       = "shipped_file_compressor.lambda_handler"
  timeout       = 600
  runtime       = "python3.8"

  environment {
    variables = {
      TARGET_BITRATE = "112k"
    }
  }

  depends_on = [
    aws_iam_role_policy_attachment.lambda_logs,
  ]

  layers = [
    aws_lambda_layer_version.ffmpeg.arn
  ]
}

resource "random_string" "lambda_permission_name_suffix" {
  length  = 8
  upper   = true
  special = false
  lower   = false
  numeric = false
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_permission
resource "aws_lambda_permission" "run_from_bucket_notification" {
  statement_id  = "AllowExecutionFromS3BucketProd${random_string.lambda_permission_name_suffix.result}"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.shipped_file_compressor_ogg_to_mp3.arn
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.website_bucket.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_notification
resource "aws_s3_bucket_notification" "bucket_notification_prod" {
  bucket = aws_s3_bucket.website_bucket.bucket
  lambda_function {
    lambda_function_arn = aws_lambda_function.shipped_file_compressor_ogg_to_mp3.arn
    events              = [
      "s3:ObjectCreated:*",
    ]
    filter_suffix = ".ogg"
  }
  depends_on = [
    aws_lambda_permission.run_from_bucket_notification
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_notification
resource "aws_s3_bucket_notification" "bucket_notification_dev" {
  bucket = aws_s3_bucket.website_bucket.bucket
  lambda_function {
    lambda_function_arn = aws_lambda_function.shipped_file_compressor_ogg_to_mp3.arn
    events              = [
      "s3:ObjectCreated:*",
    ]
    filter_suffix = ".ogg"
  }
  depends_on = [
    aws_lambda_permission.run_from_bucket_notification
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy
resource "aws_iam_policy" "lambda_shipment_access" {
  name        = "lambda_shipment_access"
  path        = "/"
  description = "IAM policy for shipped audio from a lambda"

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Sid    = "ShipAudio",
        Effect = "Allow",
        Action = [
          "s3:Get*",
          "s3:List*",
          "s3:Put*"
        ],
        Resource = [
          aws_s3_bucket.website_bucket.arn,
          "${aws_s3_bucket.website_bucket.arn}/*",
          aws_s3_bucket.website_bucket.arn,
          "${aws_s3_bucket.website_bucket.arn}/*",
        ]
      },
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "lambda_shipment_access" {
  role       = aws_iam_role.lambda_runner.name
  policy_arn = aws_iam_policy.lambda_shipment_access.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy
resource "aws_iam_policy" "shipped_file_compressor_src_access" {
  name        = "shipped_file_compressor_src_access"
  path        = "/"
  description = "IAM policy for accessing shipment compression source code from a lambda"

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Sid    = "SourceCode",
        Effect = "Allow",
        Action = [
          "s3:Get*",
          "s3:List*",
        ],
        Resource = [
          aws_s3_bucket.lambda_shipped_file_compressor_src.arn,
          "${aws_s3_bucket.lambda_shipped_file_compressor_src.arn}/*",
        ]
      },
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "shipped_file_compressor_src_access" {
  role       = aws_iam_role.lambda_runner.name
  policy_arn = aws_iam_policy.shipped_file_compressor_src_access.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy
resource "aws_iam_policy" "shipped_file_compressor_src_writing" {
  name        = "shipped_file_compressor_src_writing"
  path        = "/"
  description = "IAM policy for writing shipment compression source code from CI"

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Sid    = "SourceCode",
        Effect = "Allow",
        Action = [
          "s3:List*",
          "s3:Get*",
          "s3:Put*",
          "s3:Delete*",
        ],
        Resource = [
          aws_s3_bucket.lambda_shipped_file_compressor_src.arn,
          "${aws_s3_bucket.lambda_shipped_file_compressor_src.arn}/*",
        ]
      },
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_user_policy_attachment" "shipped_file_compressor_src_writing" {
  policy_arn = aws_iam_policy.shipped_file_compressor_src_writing.arn
  user       = var.lambda_iam_user
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role
resource "aws_iam_role" "lambda_runner" {
  name = "iam_for_lambda"

  assume_role_policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Action    = "sts:AssumeRole",
        Principal = {
          "Service" : "lambda.amazonaws.com"
        },
        Effect = "Allow",
        Sid    = ""
      },
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy
resource "aws_iam_policy" "lambda_logging" {
  name        = "lambda_logging"
  path        = "/"
  description = "IAM policy for logging from a lambda"

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        Resource = "arn:aws:logs:*:*:*",
        Effect   = "Allow"
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "lambda_logs" {
  role       = aws_iam_role.lambda_runner.name
  policy_arn = aws_iam_policy.lambda_logging.arn
}
