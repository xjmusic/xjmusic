module workstation.service.main {
  // vendor
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires jakarta.annotation;
  requires java.desktop;
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
  requires org.slf4j;
  requires spring.beans;
  requires spring.boot.autoconfigure;
  requires spring.boot;
  requires spring.context;
  requires spring.core;
  requires spring.web;

  // private
  requires transitive hub.models.main;
  requires transitive workstation.lib.main;
  requires transitive workstation.nexus.main;

  // open
  opens io.xj.workstation.service to spring.core;
  opens io.xj.workstation.service.api to spring.beans;
}
