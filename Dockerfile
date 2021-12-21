# This is the base container for all Java services in this repository, built by CI when this file changes.
# Build & ship to GCP: https://github.com/xjmusic/services/actions/workflows/services-base-gcp.yaml
# Build & ship to AWS: https://github.com/xjmusic/services/actions/workflows/services-base-aws.yaml

# Based on official
FROM ubuntu:20.10

# Install Software Properties
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install software-properties-common

# Install C Build Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install build-essential make

# Install Java 16
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install openjdk-16-jdk

# Install FFMPEG
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install ffmpeg

# Install GPAC (MP4BOX)
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install gpac
