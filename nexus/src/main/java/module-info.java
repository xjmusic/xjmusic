module services.nexus.main {
  requires java.annotation;
  requires java.desktop;
  requires java.validation;
  requires java.ws.rs;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires fuzzywuzzy;
  requires logback.classic;
  requires org.apache.commons.codec;
  requires org.apache.commons.io;
  requires org.apache.httpcomponents.httpclient;
  requires org.apache.httpcomponents.httpcore;
  requires org.slf4j;
  requires spring.beans;
  requires spring.boot;
  requires spring.boot.autoconfigure;
  requires spring.context;
  requires spring.web;
  requires wiremock.jre8.standalone;
  requires services.hub.main;
  requires services.hub.testFixtures;
  requires services.lib.main;

  exports io.xj.nexus.hub_client;
  exports io.xj.nexus.model;
  exports io.xj.nexus.persistence;
}
