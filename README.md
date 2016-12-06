# XJ

Continuous Music Platform

# Usage

## Setup

Setup workflow, and build Java server-side application and build Web user interface:
 
    make

Setup workflow:
 
    bin/setup

## Compile server-side platform

Compile the Java server-side application:

    bin/build
    
Compile & Install the Java server-side application:

    bin/install
    
Compile & Package the Java server-side application, e.g. as JAR files:

    bin/package

## Database migration
    
Migrate the local database (also run before compile tasks):
    
    bin/migrate

## Web user interface (UI)    
    
Build the Web user interface:
 
    bin/ui-build
    
Build and serve (with live updates) the Web user interface:
 
    bin/ui-serve

## Run local platform in Docker container

Before running the docker container, be sure to `bin/package` the latest Java build artifacts.

This will build and run the docker container with an Nginx server on port 80, which proxies backend requests to its own Hub (port 8042):
 
    bin/dock
    
The script uses `--expose` and `--volume` flags at `docker run` runtime in order to have the latest build artifacts available without having to rebuild the docker container. The container runs as user `root` by default. Project folders are available inside the container as:

    /var/app/current/

Also, it ought to be possible to proxy UI content to a local host machine port, e.g. `ember server` running on local machine. (instead of static serving from /var/app/current/ui/dist).

## Run local platform manually

Run a local **Hub** on its default port 8042:
 
    bin/hub    

Run a local **Craft** on its default port 8043:
 
    bin/craft    

Run a local **Ship** on its default port 8044:
 
    bin/ship    

## Release Java platform for deployment to AWS Elastic Beanstalk

Release as a zip file (e.g. target/xj-release-2016.12.05-UTC.00.37.07.zip) containing the Procfile and shaded .JAR files required to run the application.

    bin/release
    
To skip the build and just repeat the packaging:

    bin/release-package

## Cleanup    
    
Clean all build targets:

    bin/clean
    
Clean even more thoroughly "down to the distribution":

    bin/clean-distro

Clean away all IntelliJ IDEA related files:

    bin/clean-idea

## Maven

To clean, build and install all artifacts:

    mvn clean install
    
To clean, build and package artifacts for shipment:

    mvn clean package
    
To run local migrations (in the `core` submodule via the Flyway plugin):

    mvn flyway:migrate

## DNS

For development, your local machine needs to have the domain `xj.outright.dev` pointed to `127.0.0.1` in `/etc/hosts` like:

    127.0.0.1 xj.outright.dev

## Google Authentication

Login to the app using Google authentication. The redirect URL for local development is http://xj.outright.io/auth/google/callback

# Components

## ui

User interface web application. Built with Javascript, Ember, Bower, Node.

Connects to:

  * xj/hub

## core

Common models and utilities. Built with Java, Guice, Tomcat, Maven.

## hub

Central structured data and business logic. Built with Java, Guice, Tomcat, Maven.

Depends on:

  * xj/core

Connects to:

  * SQL Database
  * Filesystem

## craft

Fabricates a continuous musical audio composite from source ideas and instrument-audio. Built with Java, Guice, Tomcat, Maven.

Depends on:

  * xj/core

Connects to:

  * SQL Database
  * Filesystem

## ship

Ships finished audio data to any destination. Built with Java, Guice, Tomcat, Maven.

Depends on:

  * xj/core

Connects to:

  * SQL Database
  * Filesystem

## z

Workflow tooling

# App Standards

## System Properties
 
### host

Default `0.0.0.0`

### port

Default:

  * hub: 8042
  * craft: 8043
  * ship: 8044

## Healthcheck Endpoint

**GET /o2**

# References

## Intro to Google OAuth2

https://developers.google.com/+/web/samples/java

## Intro to Jersey and Grizzly2

See [Java SE 8: Creating a Basic REST Web Service using Grizzly, Jersey, and Maven](http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/griz_jersey_intro/Grizzly-Jersey-Intro.html)

Bootstrap a Grizzly2 quickstart with:

mvn archetype:generate -DarchetypeArtifactId=jersey-quickstart-grizzly2 -DarchetypeGroupId=org.glassfish.jersey.archetypes -DinteractiveMode=false -DgroupId=io.outright.xj -DartifactId=strap -Dpackage=io.outright.xj.strap -DarchetypeVersion=2.17

## AWS Elastic Beanstalk

The `.ebextensions` and `.ebsettings` folder contain configurations proprietary to [AWS Elastic Beanstalk](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-tomcat-platform.html#java-tomcat-proxy).

## Jersey

[Latest User Guide](https://jersey.java.net/documentation/latest/user-guide.html)
