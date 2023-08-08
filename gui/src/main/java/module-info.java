module io.xj.workstation.main {
  requires javafx.base;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;
  requires org.jetbrains.annotations;
  requires org.jooq;
  requires org.slf4j;
  requires spring.beans;
  requires spring.boot.autoconfigure;
  requires spring.boot;
  requires spring.context;
  requires spring.core;
  requires spring.web;

  requires transitive workstation.lib.main;
  requires transitive workstation.nexus.main;
  requires transitive workstation.hub_client.main;

  opens io.xj.gui to javafx.graphics, javafx.base, javafx.fxml, javafx.controls, spring.beans, spring.core, spring.context;
  opens io.xj.gui.events to javafx.graphics, javafx.base, javafx.fxml, javafx.controls, spring.beans;
  opens io.xj.gui.controllers to javafx.graphics, javafx.base, javafx.fxml, javafx.controls, spring.beans;
}