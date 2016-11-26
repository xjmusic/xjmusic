# XJ

Continuous Music Platform

## ui

User interface web application. Built with Javascript, Ember, Bower, Node.

Connects to:

  * xj/hub

## xj/core

Common models and utilities. Built with Java, Guice, Tomcat, Maven.

## xj/hub

Central structured data and business logic. Built with Java, Guice, Tomcat, Maven.

Depends on:

  * xj/core

Connects to:

  * SQL Database
  * Filesystem
  * Messaging

## xj/engine

Fabricates a continuous musical audio composite from source ideas and instrument-audio. Built with Java, Guice, Tomcat, Maven.

Depends on:

  * xj/core

Connects to:

  * SQL Database
  * Filesystem
  * Messaging

## cmd

Workflow tooling
