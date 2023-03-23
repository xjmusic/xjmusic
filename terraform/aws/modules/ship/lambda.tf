# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_user
resource "aws_iam_user" "lambda_iam_user" {
  name = "${var.bucket}-lambda"
}

# https://registry.terraform.io/providers/hashicorp/archive/latest/docs/data-sources/archive_file
data "archive_file" "ffmpeg" {
  type             = "zip"
  source_dir       = "${path.module}/ffmpeg"
  output_file_mode = "0666"
  output_path      = "${path.module}/build/lambda_ffmpeg.zip"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_function
resource "aws_lambda_layer_version" "ffmpeg" {
  layer_name          = "${var.bucket}-ffmpeg"
  description         = "ffmpeg layer for Lambda functions running on Amazon Linux"
  filename            = data.archive_file.ffmpeg.output_path
  compatible_runtimes = ["python3.8"]
  license_info        = "https://www.ffmpeg.org/legal.html"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
resource "aws_s3_bucket" "ogg_to_mp3_src" {
  bucket = "${var.bucket}-ogg-to-mp3-src"
}

# https://registry.terraform.io/providers/hashicorp/archive/latest/docs/data-sources/archive_file
data "archive_file" "ogg_to_mp3" {
  type             = "zip"
  source_dir       = "${path.module}/ogg_to_mp3"
  output_file_mode = "0666"
  output_path      = "${path.module}/build/ogg_to_mp3.zip"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_object
resource "aws_s3_object" "ogg_to_mp3" {
  bucket = aws_s3_bucket.ogg_to_mp3_src.bucket
  key    = "ogg_to_mp3.zip"
  source = data.archive_file.ogg_to_mp3.output_path
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_function
resource "aws_lambda_function" "ogg_to_mp3" {
  function_name = "${var.bucket}-ogg-to-mp3"
  description   = "When an .ogg file is shipped, compress it to .mp3"
  s3_bucket     = aws_s3_bucket.ogg_to_mp3_src.bucket
  s3_key        = "ogg_to_mp3.zip"
  role          = aws_iam_role.lambda_runner.arn
  handler       = "ogg_to_mp3.lambda_handler"
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

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_permission
resource "aws_lambda_permission" "run_from_bucket_notification" {
  statement_id  = "${var.bucket}-AllowExecutionFromS3BucketProd"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.ogg_to_mp3.arn
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.website_bucket.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_notification
resource "aws_s3_bucket_notification" "bucket_notification_prod" {
  bucket = aws_s3_bucket.website_bucket.bucket
  lambda_function {
    lambda_function_arn = aws_lambda_function.ogg_to_mp3.arn
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
resource "aws_s3_bucket_notification" "ogg_to_mp3_notification" {
  bucket = aws_s3_bucket.website_bucket.bucket
  lambda_function {
    lambda_function_arn = aws_lambda_function.ogg_to_mp3.arn
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
resource "aws_iam_policy" "ogg_to_mp3_access" {
  name        = "${var.bucket}-ogg-to-mp3-access"
  path        = "/"
  description = "IAM policy for lambda ogg_to_mp3 to access S3 bucket"

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


# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy
resource "aws_iam_policy" "ogg_to_mp3_src_access" {
  name        = "${var.bucket}-ogg-to-mp3-src-access"
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
          aws_s3_bucket.ogg_to_mp3_src.arn,
          "${aws_s3_bucket.ogg_to_mp3_src.arn}/*",
        ]
      },
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy
resource "aws_iam_policy" "ogg_to_mp3_src_writing" {
  name        = "${var.bucket}-ogg-to-mp3-src-writing"
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
          aws_s3_bucket.ogg_to_mp3_src.arn,
          "${aws_s3_bucket.ogg_to_mp3_src.arn}/*",
        ]
      },
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role
resource "aws_iam_role" "lambda_runner" {
  name = "${var.bucket}-lambda-runner"

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
  name        = "${var.bucket}-lambda-logging"
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
resource "aws_iam_user_policy_attachment" "shipped_file_compressor_src_writing" {
  user       = aws_iam_user.lambda_iam_user.name
  policy_arn = aws_iam_policy.ogg_to_mp3_src_writing.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "shipped_file_compressor_src_access" {
  role       = aws_iam_role.lambda_runner.name
  policy_arn = aws_iam_policy.ogg_to_mp3_src_access.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "lambda_logs" {
  role       = aws_iam_role.lambda_runner.name
  policy_arn = aws_iam_policy.lambda_logging.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "ogg_to_mp3_access" {
  role       = aws_iam_role.lambda_runner.name
  policy_arn = aws_iam_policy.ogg_to_mp3_access.arn
}
