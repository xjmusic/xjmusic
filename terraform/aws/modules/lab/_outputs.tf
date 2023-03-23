output "bucket_arn" {
  value = 0 < length(aws_s3_bucket.website_bucket) ? aws_s3_bucket.website_bucket[0].arn : var.existing_bucket_arn
}

output "bucket_name" {
  value = 0 < length(aws_s3_bucket.website_bucket) ? aws_s3_bucket.website_bucket[0].bucket : var.existing_bucket_name
}

output "bucket_regional_domain_name" {
  value = 0 < length(aws_s3_bucket.website_bucket) ? aws_s3_bucket.website_bucket[0].bucket_regional_domain_name : local.s3_bucket_regional_domain_name
}

output "domain_name" {
  value = aws_cloudfront_distribution.distribution.domain_name
}

output "hosted_zone_id" {
  value = aws_cloudfront_distribution.distribution.hosted_zone_id
}

output "s3_origin_domain_name" {
  value = local.s3_bucket_regional_domain_name
}
