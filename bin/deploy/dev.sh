#!/usr/bin/env bash
# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

gcloud container clusters get-credentials xj-prod-us-west-1 --region us-west1

./gradlew --info --no-daemon \
  -PjibFromImage=gcr.io/xj-vpc-host-prod/base:latest \
  -PjibToImageRegistry=gcr.io/xj-vpc-host-prod/dev \
  jib