# Based on official
FROM ubuntu:20.10

# Install Software Properties
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install software-properties-common

# Install C Build Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install build-essential make

# Install Java 14
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install openjdk-14-jdk

# Install FFMPEG
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install ffmpeg
