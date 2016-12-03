# XJ

Continuous Music Platform

# Usage

Setup workflow, and build Java server-side application and build Web user interface:
 
    make

Setup workflow:
 
    cmd/setup

Compile the Java server-side application:

    cmd/build
    
Compile & Install the Java server-side application:

    cmd/install
    
Compile & Package the Java server-side application, e.g. as JAR files:

    cmd/package
    
Compile & Package the Java server-side application, e.g. as JAR files:

    cmd/package

Migration on local database (also run before compile tasks):
    
    cmd/migrate
    
Build the Web user interface:
 
    cmd/ui-build
    
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

Default `localhost`

### port

Default `8080`

## Healthcheck Endpoint

**GET /o2**

# References

## Intro to Jersey and Grizzly2

See [Java SE 8: Creating a Basic REST Web Service using Grizzly, Jersey, and Maven](http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/griz_jersey_intro/Grizzly-Jersey-Intro.html)

Bootstrap a Grizzly2 quickstart with:

mvn archetype:generate -DarchetypeArtifactId=jersey-quickstart-grizzly2 -DarchetypeGroupId=org.glassfish.jersey.archetypes -DinteractiveMode=false -DgroupId=io.outright.xj -DartifactId=strap -Dpackage=io.outright.xj.strap -DarchetypeVersion=2.17

## Jersey

[Latest User Guide](https://jersey.java.net/documentation/latest/user-guide.html)
