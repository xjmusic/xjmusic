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

  exports io.xj.model.entity;
  exports io.xj.model.enums;
  exports io.xj.model.json;
  exports io.xj.model.jsonapi;
  exports io.xj.model.meme;
  exports io.xj.model.music;
  exports io.xj.model.pojos;
  exports io.xj.model.util;
  exports io.xj.model;
}
