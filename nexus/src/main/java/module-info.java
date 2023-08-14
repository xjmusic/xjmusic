module workstation.nexus.main {
  requires TarsosDSP.core;
  requires TarsosDSP.jvm;
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
  requires org.jooq;
  requires org.slf4j;
  requires spring.beans;
  requires spring.boot.autoconfigure;
  requires spring.boot;
  requires spring.context;
  requires spring.core;
  requires humble.video.all;
  //
  requires workstation.lib.main;
  requires transitive hub.models.main;

  opens io.xj.nexus to spring.core;
  opens io.xj.nexus.craft to spring.beans;
  opens io.xj.nexus.dub to spring.beans;
  opens io.xj.nexus.fabricator to spring.beans;
  opens io.xj.nexus.ship.broadcast to spring.beans;
  opens io.xj.nexus.work to spring.beans;
  opens io.xj.nexus.analysis to spring.core;

  exports io.xj.nexus.craft;
  exports io.xj.nexus.dub;
  exports io.xj.nexus.fabricator;
  exports io.xj.nexus.hub_client.access;
  exports io.xj.nexus.hub_client;
  exports io.xj.nexus.model;
  exports io.xj.nexus.persistence;
  exports io.xj.nexus.ship.broadcast;
  exports io.xj.nexus.work;
  exports io.xj.nexus;
}
