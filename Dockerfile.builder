# Based on official
FROM ubuntu:12.04.5

# Software Properties (Ubuntu 12 uses python-software-properties; newer versions use softare-properties-common)
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python-software-properties

# OpenJDK 8
RUN DEBIAN_FRONTEND=noninteractive add-apt-repository ppa:openjdk-r/ppa && apt-get update && apt-get install -y openjdk-8-jdk

# C Build Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install build-essential make

# Python Build Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python-dev
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python3-dev

# Python PIP
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install python-pip

# Maven
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install maven

# Networking Tools
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install net-tools

# DNS Utilities
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install dnsutils

# cURL
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install curl

# MySQL Client Libraries
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install libmysqlclient-dev libmysql-ruby

# Build
CMD cd /var/app/current && mvn clean package
