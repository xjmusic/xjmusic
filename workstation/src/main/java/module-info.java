module io.xj.workstation.main {
  requires javafx.base;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;
  requires org.jetbrains.annotations;
  requires org.slf4j;
  requires spring.beans;
  requires spring.boot.autoconfigure;
  requires spring.boot;
  requires spring.context;
  requires spring.core;
  requires spring.web;

  opens io.xj.workstation to javafx.graphics, javafx.base, javafx.fxml, javafx.controls;
  opens io.xj.workstation.events to javafx.graphics, javafx.base, javafx.fxml, javafx.controls;
  opens io.xj.workstation.controllers to javafx.graphics, javafx.base, javafx.fxml, javafx.controls;

  requires transitive workstation.lib.main;
  requires transitive workstation.nexus.main;
}
