module workstation.gui.main {
  // vendor
  requires ch.qos.logback.classic;
  requires ch.qos.logback.core;
  requires com.fasterxml.jackson.databind;
  requires jakarta.annotation;
  requires java.desktop;
  requires javafx.base;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;
  requires org.bytedeco.ffmpeg;
  requires org.bytedeco.flandmark.platform;
  requires org.bytedeco.flandmark;
  requires org.bytedeco.javacv.platform;
  requires org.bytedeco.javacv;
  requires org.slf4j;
  requires reactor.core;
  requires spring.beans;
  requires spring.boot.autoconfigure;
  requires spring.boot.starter.reactor.netty;
  requires spring.boot;
  requires spring.context;
  requires spring.core;
  requires spring.jcl;
  requires spring.web;
  requires spring.webflux;

  // private
  requires transitive hub.models.main;
  requires transitive workstation.nexus.main;
  requires component.inspector.fx;
  requires java.prefs;
  requires reactor.netty.http;
  requires io.netty.resolver;
  requires org.controlsfx.controls;

  // open
  opens io.xj.gui to ch.qos.logback.core, javafx.base, javafx.controls, javafx.fxml, javafx.graphics, spring.beans, spring.context, spring.core;
  opens io.xj.gui.services to ch.qos.logback.core, javafx.base, javafx.controls, javafx.fxml, javafx.graphics, spring.beans, spring.context, spring.core;
  opens io.xj.gui.controllers to javafx.graphics, javafx.base, javafx.fxml, javafx.controls, spring.beans, spring.core;
  opens io.xj.gui.types to javafx.base, javafx.controls, javafx.fxml, javafx.graphics, spring.beans, spring.context, spring.core, ch.qos.logback.core;
  opens io.xj.gui.services.impl to ch.qos.logback.core, javafx.base, javafx.controls, javafx.fxml, javafx.graphics, spring.beans, spring.context, spring.core;
  opens io.xj.gui.controllers.content to javafx.base, javafx.controls, javafx.fxml, javafx.graphics, spring.beans, spring.core;
  opens io.xj.gui.controllers.template to javafx.base, javafx.controls, javafx.fxml, javafx.graphics, spring.beans, spring.core;
  opens io.xj.gui.controllers.fabrication to javafx.base, javafx.controls, javafx.fxml, javafx.graphics, spring.beans, spring.core;
  opens io.xj.gui.utils to ch.qos.logback.core, javafx.base, javafx.controls, javafx.fxml, javafx.graphics, spring.beans, spring.context, spring.core;
}