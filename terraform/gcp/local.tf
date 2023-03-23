# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

locals {
  gcp-project-id = "xj-vpc-host-prod"
  gcp-region     = "us-west1"
  gcp-alert-notification-channels = [
    "projects/xj-vpc-host-prod/notificationChannels/10654798962631154220",
    "projects/xj-vpc-host-prod/notificationChannels/17866939383405512222",
  ]
}

