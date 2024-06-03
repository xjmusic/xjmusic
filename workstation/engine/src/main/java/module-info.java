module workstation.nexus.main {
  // vendor deps
  requires ch.qos.logback.classic;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires jakarta.annotation;
  requires java.desktop;
  requires java.prefs;
  requires java.sql;
  requires javafx.graphics;
  requires me.xdrop.fuzzywuzzy;
  requires org.apache.commons.codec;
  requires org.apache.commons.io;
  requires org.apache.httpcomponents.client5.httpclient5;
  requires org.apache.httpcomponents.core5.httpcore5;
  requires org.bytedeco.ffmpeg;
  requires org.bytedeco.flandmark.platform;
  requires org.bytedeco.flandmark;
  requires org.bytedeco.javacv.platform;
  requires org.bytedeco.javacv;
  requires org.reflections;
  requires org.slf4j;
  requires org.yaml.snakeyaml;
  requires typesafe.config;

  // deps
  requires transitive hub.models.main;

  // export
  exports io.xj.engine.audio;
  exports io.xj.engine.craft.background;
  exports io.xj.engine.craft.beat;
  exports io.xj.engine.craft.detail;
  exports io.xj.engine.craft.macro_main;
  exports io.xj.engine.craft.transition;
  exports io.xj.engine.craft;
  exports io.xj.engine.fabricator;
  exports io.xj.engine.http;
  exports io.xj.engine.hub_client;
  exports io.xj.engine.mixer;
  exports io.xj.engine.model;
  exports io.xj.engine.project;
  exports io.xj.engine.ship.broadcast;
  exports io.xj.engine.ship;
  exports io.xj.engine.telemetry;
  exports io.xj.engine.util;
  exports io.xj.engine.work;
  exports io.xj.engine;
}
