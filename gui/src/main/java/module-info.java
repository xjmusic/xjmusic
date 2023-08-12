module workstation.gui.main {
  requires jakarta.annotation;
  requires java.desktop;
  requires javafx.base;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;
  requires org.jooq;
  requires org.slf4j;
  requires spring.beans;
  requires spring.boot.autoconfigure;
  requires spring.jcl;
  requires spring.boot;
  requires spring.context;
  requires spring.core;
  requires ch.qos.logback.classic;
  requires ch.qos.logback.core;

  requires transitive hub.models.main;
  requires workstation.lib.main;
  requires workstation.nexus.main;

  opens io.xj.gui to javafx.graphics, javafx.base, javafx.fxml, javafx.controls, spring.beans, spring.core, spring.context;
  opens io.xj.gui.controllers to javafx.graphics, javafx.base, javafx.fxml, javafx.controls, spring.beans;
  opens io.xj.gui.events to javafx.base, javafx.controls, javafx.fxml, javafx.graphics, spring.beans, spring.context, spring.core;
  opens io.xj.gui.listeners to javafx.base, javafx.controls, javafx.fxml, javafx.graphics, spring.beans, spring.context, spring.core, ch.qos.logback.core;
  opens io.xj.gui.services to javafx.base, javafx.controls, javafx.fxml, javafx.graphics, spring.beans, spring.context, spring.core;
}
