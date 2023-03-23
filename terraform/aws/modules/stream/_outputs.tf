output "arn" {
  value = aws_s3_bucket.website_bucket.arn
}

output "bucket" {
  value = aws_s3_bucket.website_bucket.bucket
}

output "bucket_regional_domain_name" {
  value = aws_s3_bucket.website_bucket.bucket_regional_domain_name
}

output "domain_name" {
  value = aws_cloudfront_distribution.distribution.domain_name
}

output "hosted_zone_id" {
  value = aws_cloudfront_distribution.distribution.hosted_zone_id
}
