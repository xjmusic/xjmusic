# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/monitoring_alert_policy
resource "google_monitoring_alert_policy" "fabricated_ahead" {
  enabled      = true
  combiner     = "OR"
  display_name = "[${var.display_name}] Fabricated Ahead"
  conditions {
    display_name = "Not Ahead"
    condition_threshold {
      comparison      = "COMPARISON_LT"
      duration        = "60s"
      filter          = "resource.type = \"k8s_container\" AND metric.type = \"custom.googleapis.com/opencensus/${var.ship_key}_nexus_fabricated_ahead_seconds\""
      threshold_value = 180
      trigger { count = 1 }
      aggregations {
        alignment_period   = "300s"
        per_series_aligner = "ALIGN_MEAN"
      }
    }
  }
  notification_channels = var.notification_channels
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/monitoring_alert_policy
resource "google_monitoring_alert_policy" "fabricating" {
  enabled      = true
  combiner     = "OR"
  display_name = "[${var.display_name}] Fabricating"
  conditions {
    display_name = "No Data"
    condition_absent {
      duration = "300s"
      filter          = "resource.type = \"k8s_container\" AND metric.type = \"custom.googleapis.com/opencensus/${var.ship_key}_nexus_fabricated_ahead_seconds\""
      trigger {percent = 100}
      aggregations {
        alignment_period   = "300s"
        per_series_aligner = "ALIGN_MEAN"
      }
    }
  }
  notification_channels = var.notification_channels
}


# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/monitoring_alert_policy
resource "google_monitoring_alert_policy" "hls_playlist_ahead_seconds" {
  enabled      = true
  combiner     = "OR"
  display_name = "[${var.display_name}] HLS Playlist Ahead Seconds"
  conditions {
    display_name = "Not Ahead"
    condition_threshold {
      comparison      = "COMPARISON_LT"
      duration        = "60s"
      filter          = "resource.type = \"k8s_container\" AND metric.type = \"custom.googleapis.com/opencensus/${var.ship_key}_ship_hls_playlist_ahead_seconds\""
      threshold_value = 0.005
      trigger { count = 1 }
      aggregations {
        alignment_period   = "300s"
        per_series_aligner = "ALIGN_MEAN"
      }
    }
  }
  notification_channels = var.notification_channels
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/monitoring_alert_policy
resource "google_monitoring_alert_policy" "hls_playlist_shipping" {
  enabled      = true
  combiner     = "OR"
  display_name = "[${var.display_name}] HLS Playlist Shipping"
  conditions {
    display_name = "No Data"
    condition_absent {
      duration = "300s"
      filter          = "resource.type = \"k8s_container\" AND metric.type = \"custom.googleapis.com/opencensus/${var.ship_key}_ship_hls_playlist_size\""
      aggregations {
        alignment_period   = "300s"
        per_series_aligner = "ALIGN_MEAN"
      }
      trigger {percent = 100}
    }
  }
  notification_channels = var.notification_channels
}

# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/monitoring_alert_policy
resource "google_monitoring_alert_policy" "ship_loaded_audio_ahead" {
  enabled      = true
  combiner     = "OR"
  display_name = "[${var.display_name}] Ship Loaded Audio Ahead"
  conditions {
    display_name = "No Data"
    condition_threshold {
      comparison = "COMPARISON_LT"
      duration   = "300s"
      filter="resource.type = \"k8s_container\" AND metric.type = \"custom.googleapis.com/opencensus/${var.ship_key}_ship_segment_audio_loaded_ahead_seconds\""
      threshold_value = 40
      trigger {count = 1}
      aggregations {
        alignment_period   = "60s"
        per_series_aligner = "ALIGN_MEAN"
      }
    }
  }
  notification_channels = var.notification_channels
}

