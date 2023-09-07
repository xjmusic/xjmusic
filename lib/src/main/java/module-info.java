module workstation.lib.main {
  // vendor
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires jakarta.annotation;
  requires java.desktop;
  requires java.prefs;
  requires java.sql;
  requires org.apache.commons.codec;
  requires org.apache.commons.io;
  requires org.apache.httpcomponents.httpclient;
  requires org.bytedeco.ffmpeg;
  requires org.bytedeco.flandmark.platform;
  requires org.bytedeco.flandmark;
  requires org.bytedeco.javacv.platform;
  requires org.bytedeco.javacv;
  requires org.reflections;
  requires org.slf4j;
  requires spring.beans;
  requires spring.context;
  requires spring.core;
  requires typesafe.config;

  // private
  requires transitive hub.models.main;

  // open
  opens io.xj.lib.filestore to spring.core;
  opens io.xj.lib.telemetry to spring.core;
  opens io.xj.lib to spring.core;

  // export
  exports io.xj.lib;
  exports io.xj.lib.app;
  exports io.xj.lib.entity.common;
  exports io.xj.lib.entity;
  exports io.xj.lib.filestore;
  exports io.xj.lib.http;
  exports io.xj.lib.json;
  exports io.xj.lib.jsonapi;
  exports io.xj.lib.mixer;
  exports io.xj.lib.notification;
  exports io.xj.lib.telemetry;
  exports io.xj.lib.util;
}
