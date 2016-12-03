# XJ

Continuous Music Platform

# Usage

## Makefile

To check workflow dependencies, setup filesystem and build both the Java server-side application and Web user interface:
 
    make
    
To build both the Java server-side application and Web user interface:

    make build
    
To build only the Web user interface:
 
    make ui
    
To clean all build targets:

    make clean
    
To clean even more thoroughly "down to the distribution":

    make distclean
    
To clean away all IntelliJ IDEA related files:

    make ideaclean

## Maven

To clean, build and install all artifacts:

    mvn clean install
    
To clean, build and package artifacts for shipment:

    mvn clean package
    
To run local migrations (in the `core` submodule via the Flyway plugin):

    mvn flyway:migrate

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
