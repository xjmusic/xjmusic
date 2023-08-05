module workstation.hub_client.main {
  requires TarsosDSP.core;
  requires TarsosDSP.jvm;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires java.desktop;
  requires java.prefs;
  requires java.sql;
  requires org.apache.commons.codec;
  requires org.apache.commons.io;
  requires org.apache.httpcomponents.httpclient;
  requires org.jetbrains.annotations;
  requires org.jooq;
  requires org.reflections;
  requires org.slf4j;
  requires software.amazon.awssdk.auth;
  requires software.amazon.awssdk.awscore;
  requires software.amazon.awssdk.core;
  requires software.amazon.awssdk.regions;
  requires software.amazon.awssdk.services.s3;
  requires software.amazon.awssdk.services.sns;
  requires spring.beans;
  requires spring.context;
  requires spring.core;
  requires spring.web;
  requires typesafe.config;
  //
  requires transitive workstation.lib.main;

  opens io.xj.hub.access to spring.core;
  opens io.xj.hub.enums to spring.core;
  opens io.xj.hub.ingest to spring.core;
  opens io.xj.hub.tables.pojos to spring.core;
  opens io.xj.hub to spring.core;

  exports io.xj.hub.access;
  exports io.xj.hub.enums;
  exports io.xj.hub.ingest;
  exports io.xj.hub.tables.pojos;
  exports io.xj.hub;
 }
