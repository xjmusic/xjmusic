output "service_account_email" {
  value = google_service_account.user.email
}

output "service_account_key" {
  value = google_service_account_key.key.private_key
}
