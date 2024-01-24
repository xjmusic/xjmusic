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
  requires org.apache.httpcomponents.httpclient;
  requires org.apache.httpcomponents.httpcore;
  requires org.bytedeco.ffmpeg;
  requires org.bytedeco.flandmark.platform;
  requires org.bytedeco.flandmark;
  requires org.bytedeco.javacv.platform;
  requires org.bytedeco.javacv;
  requires org.jooq;
  requires org.reflections;
  requires org.slf4j;
  requires org.yaml.snakeyaml;
  requires typesafe.config;

  // deps
  requires transitive hub.models.main;

  // export
  exports io.xj.nexus.audio_cache;
  exports io.xj.nexus.craft.background;
  exports io.xj.nexus.craft.beat;
  exports io.xj.nexus.craft.detail;
  exports io.xj.nexus.craft.hook;
  exports io.xj.nexus.craft.macro_main;
  exports io.xj.nexus.craft.perc_loop;
  exports io.xj.nexus.craft.transition;
  exports io.xj.nexus.craft;
  exports io.xj.nexus.fabricator;
  exports io.xj.nexus.http;
  exports io.xj.nexus.hub_client.access;
  exports io.xj.nexus.hub_client;
  exports io.xj.nexus.mixer;
  exports io.xj.nexus.model;
  exports io.xj.nexus.persistence;
  exports io.xj.nexus.ship.broadcast;
  exports io.xj.nexus.ship;
  exports io.xj.nexus.telemetry;
  exports io.xj.nexus.util;
  exports io.xj.nexus.work;
  exports io.xj.nexus;
}
