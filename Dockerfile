# Based on official
FROM ubuntu:18.04

# Install Software Properties
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install software-properties-common

# Install C Build Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install build-essential make

# Install Snappy C Library
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install libsnappy-dev

# Install Networking Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install net-tools

# Install DNS Utilities
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install dnsutils

# Install cURL
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install curl

# Install FFI dev lib
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install libffi-dev

# Install PhantomJS
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install phantomjs

# Install Python Build Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python-dev
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python3-dev

# Install Python PIP
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python-pip

# Install Java 11
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install openjdk-11-jdk

# Install Fraunhofer
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install libfdk-aac-dev

# Define commonly used JAVA_HOME variable
# ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# Define working directory.
WORKDIR /data

# Install Tomcat 6
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install tomcat8

# Install Less
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install less

# Install Tree
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install tree

# Install Telnet
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install telnet

# Install Redis CLI
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install redis-tools

# Install Network tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install net-tools

# Install Postgres Client
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install postgresql-client

# Install wget
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install wget

# Install unzip
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install unzip

# YourKit Java Profiler
RUN wget https://www.yourkit.com/download/docker/YourKit-JavaProfiler-2020.9-docker.zip -P /tmp/ && \
  unzip /tmp/YourKit-JavaProfiler-2020.9-docker.zip -d /usr/local && \
  rm /tmp/YourKit-JavaProfiler-2020.9-docker.zip

# Datadog Java Agent
RUN wget -O /usr/local/dd-java-agent.jar https://dtdg.co/latest-java-tracer

###
### App-specific content follows
###
RUN mkdir -p /var/log/profiler && chmod a+w /var/log/profiler
RUN mkdir -p /var/log/hub && chmod a+w /var/log/hub
RUN mkdir -p /var/log/nexus && chmod a+w /var/log/nexus
RUN mkdir -p /var/cache/nexus && chmod a+w /var/cache/nexus

# App bootstrap
ADD \
  ops/docker/bin/boot-docker \
  /data/bin/boot-docker

# Docker bootstrap
CMD bin/boot-docker

