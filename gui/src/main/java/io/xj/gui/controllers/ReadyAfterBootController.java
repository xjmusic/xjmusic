package io.xj.gui.controllers;

public interface ReadyAfterBootController {
  /**
   In order for JavaFX to play well with Spring Boot, we need to wait until the Spring Boot application context is ready before we can start the JavaFX application.
   */
  void onStageReady();
}
