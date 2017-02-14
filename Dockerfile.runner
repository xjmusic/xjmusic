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

# Less
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install less

# Tree
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install tree

# Telnet
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install telnet

# Redis CLI
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install libjemalloc1 libjemalloc-dev
RUN \
  cd /tmp && \
  wget http://download.redis.io/redis-stable.tar.gz && \
  tar xvzf redis-stable.tar.gz && \
  cd redis-stable && \
  make && \
  cp src/redis-cli /usr/local/bin/ && \
  chmod 755 /usr/local/bin/redis-cli

# Netcat (traditional)
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install netcat-traditional

# MySQL Client
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install mysql-client

###
### Nginx
###

# Install Nginx
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install nginx
EXPOSE 80

# Nginx config (parity with AWS Elastic Beanstalk deployment)
ADD \
  .docker/nginx/nginx.conf \
  /etc/nginx/nginx.conf
RUN mkdir -p /etc/nginx/conf.d/elasticbeanstalk
ADD \
  .nginx/locations.conf \
  /etc/nginx/conf.d/elasticbeanstalk/00_application.conf

###
### App-specific content follows
###

# Java system properties
ADD runtime.env /data/runtime.env

# 'hub'
EXPOSE 8042
RUN mkdir -p /var/log/hub && chmod a+w /var/log/hub

# 'craftworker'
EXPOSE 8043
RUN mkdir -p /var/log/craftworker && chmod a+w /var/log/craftworker

# 'dubworker'
EXPOSE 8044
RUN mkdir -p /var/log/dubworker && chmod a+w /var/log/dubworker

# App run script
ADD \
  .docker/bin/boot-docker \
  /data/bin/boot-docker

# App bootstrap
ADD \
  bin/common/bootstrap \
  /data/bin/bootstrap

# Docker bootstrap
CMD bin/boot-docker
