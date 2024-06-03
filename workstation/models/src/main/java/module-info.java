module models.main {
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires jakarta.annotation;
  requires java.annotation;
  requires java.desktop;
  requires java.sql;
  requires org.apache.commons.codec;
  requires org.reflections;
  requires org.slf4j;
  requires typesafe.config;

  exports io.xj.hub.entity;
  exports io.xj.hub.enums;
  exports io.xj.hub.json;
  exports io.xj.hub.jsonapi;
  exports io.xj.hub.meme;
  exports io.xj.hub.music;
  exports io.xj.hub.pojos;
  exports io.xj.hub.util;
  exports io.xj.hub;
}
