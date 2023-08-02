module workstation.nexus.main {
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires java.desktop;
  requires java.validation;
  requires org.apache.commons.codec;
  requires org.apache.commons.io;
  requires org.apache.httpcomponents.httpclient;
  requires org.apache.httpcomponents.httpcore;
  requires org.jetbrains.annotations;
  requires org.slf4j;
  requires spring.beans;
  requires spring.boot.autoconfigure;
  requires spring.boot;
  requires spring.context;
  requires spring.web;
  requires transitive workstation.lib.main;

  exports io.xj.nexus.hub_client;
  exports io.xj.nexus.model;
  exports io.xj.nexus.persistence;
  exports io.xj.nexus;
}
