module workstation.service.main {
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires java.desktop;
  requires org.apache.commons.codec;
  requires org.apache.commons.io;
  requires org.apache.httpcomponents.httpclient;
  requires org.apache.httpcomponents.httpcore;
  requires jakarta.annotation;
  requires me.xdrop.fuzzywuzzy;
  requires org.slf4j;
  requires spring.beans;
  requires spring.core;
  requires spring.boot.autoconfigure;
  requires spring.boot;
  requires spring.context;
  requires spring.web;
  //
  requires workstation.lib.main;
  requires workstation.nexus.main;
  requires transitive hub.models.main;

  opens io.xj.workstation.service to spring.core;
  opens io.xj.workstation.service.api to spring.beans;

  exports io.xj.workstation.service;
}
