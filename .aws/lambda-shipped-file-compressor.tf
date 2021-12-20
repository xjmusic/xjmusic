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

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_permission
resource "aws_lambda_permission" "run_from_bucket_notification_prod" {
  statement_id  = "AllowExecutionFromS3BucketProd"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.shipped_file_compressor_ogg_to_mp3.arn
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.xj-prod-ship.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_notification
resource "aws_s3_bucket_notification" "bucket_notification_prod" {
  bucket = aws_s3_bucket.xj-prod-ship.id
  lambda_function {
    lambda_function_arn = aws_lambda_function.shipped_file_compressor_ogg_to_mp3.arn
    events = [
      "s3:ObjectCreated:*",
    ]
    filter_suffix = ".ogg"
  }
  depends_on = [
    aws_lambda_permission.run_from_bucket_notification_prod
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_permission
resource "aws_lambda_permission" "run_from_bucket_notification_dev" {
  statement_id  = "AllowExecutionFromS3BucketDev"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.shipped_file_compressor_ogg_to_mp3.arn
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.xj-dev-ship.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_notification
resource "aws_s3_bucket_notification" "bucket_notification_dev" {
  bucket = aws_s3_bucket.xj-dev-ship.id
  lambda_function {
    lambda_function_arn = aws_lambda_function.shipped_file_compressor_ogg_to_mp3.arn
    events = [
      "s3:ObjectCreated:*",
    ]
    filter_suffix = ".ogg"
  }
  depends_on = [
    aws_lambda_permission.run_from_bucket_notification_dev
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
          aws_s3_bucket.xj-prod-ship.arn,
          "${aws_s3_bucket.xj-prod-ship.arn}/*",
          aws_s3_bucket.xj-dev-ship.arn,
          "${aws_s3_bucket.xj-dev-ship.arn}/*",
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
  user       = aws_iam_user.xj-ci.name
}

