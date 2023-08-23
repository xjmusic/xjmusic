package io.xj.gui.listeners;

import io.xj.gui.WorkstationIcon;
import io.xj.gui.controllers.MainWindowController;
import io.xj.gui.events.StageReadyEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

@Component
public class MainWindowStageReadyListener implements ApplicationListener<StageReadyEvent> {
  static final Logger LOG = LoggerFactory.getLogger(MainWindowStageReadyListener.class);
  private final Resource mainWindowFxml;
  private final MainWindowController mainWindowController;
  private final ApplicationContext ac;

  public MainWindowStageReadyListener(
    @Value("classpath:/views/main-window.fxml") Resource mainWindowFxml,
    MainWindowController mainWindowController,
    ApplicationContext ac
  ) {
    this.mainWindowFxml = mainWindowFxml;
    this.mainWindowController = mainWindowController;
    this.ac = ac;
  }

  @Override
  public void onApplicationEvent(StageReadyEvent event) {
    try {
      var primaryStage = event.getStage();
      FXMLLoader mainWindowFxmlLoader = new FXMLLoader(mainWindowFxml.getURL());
      mainWindowFxmlLoader.setControllerFactory(ac::getBean);
      mainWindowController.setMainWindowScene(new Scene(mainWindowFxmlLoader.load()));
      primaryStage.setScene(mainWindowController.getMainWindowScene());
      WorkstationIcon.setup(primaryStage, null);
      WorkstationIcon.setupTaskbar();

      mainWindowController.onStageReady();
      primaryStage.show();

    } catch (IOException e) {
      LOG.error("Failed to set the scene on the primary stage!", e);
    }
  }

  /**
   Load an image from a resource

   @param image The image resource
   @return The loaded image
   */
  private Image loadImage(Resource image) {
    try {
      return new Image(new BufferedInputStream(new FileInputStream(image.getFile())));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
