# Based on official
FROM ubuntu:18.04

# Software Properties
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install software-properties-common

# C Build Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install build-essential make

# Snappy C Library
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install libsnappy-dev

# Networking Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install net-tools

# DNS Utilities
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install dnsutils

# cURL
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install curl

# FFI dev lib
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install libffi-dev

# PhantomJS
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install phantomjs

# Python Build Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python-dev
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python3-dev

# Python PIP
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python-pip

# Install Java 11
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install openjdk-11-jdk

# Install Fraunhofer
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install libfdk-aac-dev

# Define commonly used JAVA_HOME variable
# ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# Define working directory.
WORKDIR /data

# Tomcat 6
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install tomcat8

# Less
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install less

# Tree
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install tree

# Telnet
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install telnet

# Redis CLI
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install redis-tools

# Network tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install net-tools

# Postgres Client
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install postgresql-client

###
### App-specific content follows
###

# 'hub'
EXPOSE 8042
RUN mkdir -p /var/log/hub && chmod a+w /var/log/hub

# 'nexus'
EXPOSE 8043
RUN mkdir -p /var/log/nexus && chmod a+w /var/log/nexus
RUN mkdir -p /var/cache/nexus && chmod a+w /var/cache/nexus

# App bootstrap
ADD \
  ops/docker/bin/boot-docker \
  /data/bin/boot-docker

# Docker bootstrap
CMD bin/boot-docker

