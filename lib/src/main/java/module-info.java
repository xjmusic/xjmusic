module workstation.lib.main {
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
  requires hub.models.main;

  opens io.xj.lib.filestore to spring.core;
  opens io.xj.lib.telemetry to spring.core;

  exports io.xj.lib.app;
  exports io.xj.lib.entity.common;
  exports io.xj.lib.entity;
  exports io.xj.lib.filestore;
  exports io.xj.lib.http;
  exports io.xj.lib.json;
  exports io.xj.lib.jsonapi;
  exports io.xj.lib.lock;
  exports io.xj.lib.mixer;
  exports io.xj.lib.notification;
  exports io.xj.lib.telemetry;
}
