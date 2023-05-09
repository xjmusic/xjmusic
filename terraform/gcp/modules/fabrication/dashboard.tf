resource "google_monitoring_dashboard" "fabrication" {
  dashboard_json = jsonencode({
    "dashboardFilters": [],
    "displayName": "Yard/${var.display_name}",
    "labels": {},
    "mosaicLayout": {
      "columns": 12,
      "tiles": [
        {
          "height": 3,
          "widget": {
            "alertChart": {
              "name": google_monitoring_alert_policy.nexus_fabricating.name
            }
          },
          "width": 12,
          "yPos": 1
        },
        {
          "height": 3,
          "widget": {
            "alertChart": {
              "name": google_monitoring_alert_policy.nexus_fabricated_ahead.name
            }
          },
          "width": 12,
          "yPos": 4
        },
        {
          "height": 3,
          "widget": {
            "alertChart": {
              "name": google_monitoring_alert_policy.ship_hls_playlist_shipping.name
            }
          },
          "width": 12,
          "yPos": 7
        },
        {
          "height": 3,
          "widget": {
            "alertChart": {
              "name": google_monitoring_alert_policy.ship_loaded_audio_ahead.name
            }
          },
          "width": 12,
          "yPos": 10
        },
        {
          "height": 3,
          "widget": {
            "alertChart": {
              "name": google_monitoring_alert_policy.ship_hls_playlist_ahead_seconds.name
            }
          },
          "width": 12,
          "yPos": 13
        },
        {
          "height": 1,
          "widget": {
            "text": {
              "content": "",
              "format": "RAW",
              "style": {
                "backgroundColor": "",
                "fontSize": "FS_LARGE",
                "horizontalAlignment": "H_LEFT",
                "padding": "P_EXTRA_SMALL",
                "textColor": "#000000",
                "verticalAlignment": "V_TOP"
              }
            },
            "title": var.display_name
          },
          "width": 12
        }
      ]
    }
  })
}
