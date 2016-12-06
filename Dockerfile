# Based on official
FROM ubuntu:12.04.5

# Software Properties (Ubuntu 12 uses python-software-properties; newer versions use softare-properties-common)
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python-software-properties

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

# MySQL Client Libraries
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install libmysqlclient-dev libmysql-ruby

# PHP5 CLI
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install php5-cli

# FFI dev lib
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install libffi-dev

# PhantomJS
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install phantomjs

# Python Build Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python-dev
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python3-dev

# Python PIP
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python-pip

# Install Java.
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

# Define working directory.
WORKDIR /data

# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# Tomcat 6
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install tomcat6

# Linux User `ontic`
RUN useradd -ms /bin/bash ontic
USER ontic
WORKDIR /home/ontic

## hub
#VOLUME [ "hub":"/home/ontic/hub" ]
#EXPOSE 8042
#
## craft
#VOLUME [ "craft":"/home/ontic/craft" ]
#EXPOSE 8043
#
## ship
#VOLUME [ "ship":"/home/ontic/ship" ]
#EXPOSE 8044

# App run script
ADD docker/internal/run /home/ontic/run
