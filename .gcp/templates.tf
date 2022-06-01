# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

module "fabrication_bump_deep" {
  source = "./fabrication"

  notification_channels = local.gcp-alert-notification-channels
  ship_key              = "bump_deep"
  display_name          = "Bump/Deep"
}

module "fabrication_bump_chill" {
  source = "./fabrication"

  notification_channels = local.gcp-alert-notification-channels
  ship_key              = "bump_chill"
  display_name          = "Bump/Chill"
}

module "fabrication_space_flow" {
  source = "./fabrication"

  notification_channels = local.gcp-alert-notification-channels
  ship_key              = "space_flow"
  display_name          = "Space/Flow"
}

module "fabrication_slaps_lofi" {
  source = "./fabrication"

  notification_channels = local.gcp-alert-notification-channels
  ship_key              = "slaps_lofi"
  display_name          = "Slaps/Lofi"
}

