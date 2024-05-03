package io.xj.gui.services;

public interface ReadyAfterBoot {

  /**
   In order for JavaFX to play well with Spring Boot, we need to wait until the Spring Boot application context is ready before we can start the JavaFX application.
   */
  void onStageReady();

  /**
   Called when the stage is closing.
   */
  void onStageClose();

}
