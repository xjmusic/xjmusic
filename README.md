# XJ

Continuous Music Platform

# Usage

## Setup

Setup workflow, and build Java server-side application and build Web user interface:
 
    make

Setup workflow:
 
    cmd/setup

## Compile server-side platform

Compile the Java server-side application:

    cmd/build
    
Compile & Install the Java server-side application:

    cmd/install
    
Compile & Package the Java server-side application, e.g. as JAR files:

    cmd/package

## Database migration
    
Migrate the local database (also run before compile tasks):
    
    cmd/migrate

## Web user interface (UI)    
    
Build the Web user interface:
 
    cmd/ui-build
    
Build and serve (with live updates) the Web user interface:
 
    cmd/ui-serve

## Run local platform

Run a local **Hub** on its default port 8042:
 
    cmd/hub    

Run a local **Craft** on its default port 8043:
 
    cmd/craft    

Run a local **Ship** on its default port 8044:
 
    cmd/ship    

## Release Java platform for deployment to AWS Elastic Beanstalk

Release as a zip file (e.g. target/xj-release-2016.12.05-UTC.00.37.07.zip) containing the Procfile and shaded .JAR files required to run the application.

    cmd/release
    
To skip the build and just repeat the packaging:

    cmd/release-package

## Cleanup    
    
Clean all build targets:

    cmd/clean
    
Clean even more thoroughly "down to the distribution":

    cmd/clean-distro

Clean away all IntelliJ IDEA related files:

    cmd/clean-idea

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

## cmd

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
