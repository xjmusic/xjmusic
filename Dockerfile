# This is the base container for all Java services in this repository, built by CI when this file changes.
# Build & ship to GCP: https://github.com/xjmusic/services/actions/workflows/services-base.yaml

# Based on official
FROM ubuntu:22.04

# Install Software Properties
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install software-properties-common

# Install C Build Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install build-essential make

# Install JDK 17
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install openjdk-17-jdk

# Install FFMPEG
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install ffmpeg

# Install GPAC (MP4BOX)
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install gpac

# Install curl
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install curl

# Downloading gcloud package
RUN curl https://dl.google.com/dl/cloudsdk/release/google-cloud-sdk.tar.gz > /tmp/google-cloud-sdk.tar.gz

# Installing the package
RUN mkdir -p /usr/local/gcloud \
  && tar -C /usr/local/gcloud -xvf /tmp/google-cloud-sdk.tar.gz \
  && /usr/local/gcloud/google-cloud-sdk/install.sh

# Adding the package path to local
ENV PATH $PATH:/usr/local/gcloud/google-cloud-sdk/bin

