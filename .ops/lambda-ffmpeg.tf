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