package io.xj.gui.listeners;

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

import java.awt.*;
import java.awt.Taskbar.Feature;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Component
public class MainWindowStageReadyListener implements ApplicationListener<StageReadyEvent> {
  static final Logger LOG = LoggerFactory.getLogger(MainWindowStageReadyListener.class);
  private static final String PATH_TO_ICON_ICNS = "/icons/icon.icns";
  private static final String PATH_TO_ICON_ICO = "/icons/icon.ico";
  private static final String PATH_TO_ICON_PNG = "/icons/icon.png";
  private static final String PATH_TO_ICON_SVG = "/icons/icon.svg";
  private final String applicationTitle;
  private final Resource mainWindowFxml;
  private final MainWindowController mainWindowController;
  private final ApplicationContext ac;

  public MainWindowStageReadyListener(
    @Value("${application.ui.title}") String applicationTitle,
    @Value("classpath:/views/main-window.fxml") Resource mainWindowFxml,
    MainWindowController mainWindowController,
    ApplicationContext ac
  ) {
    this.applicationTitle = applicationTitle;
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
      primaryStage.getIcons().addAll(List.of(
        new Image(PATH_TO_ICON_ICNS),
        new Image(PATH_TO_ICON_ICO),
        new Image(PATH_TO_ICON_PNG),
        new Image(PATH_TO_ICON_SVG)
      ));
      if (Taskbar.isTaskbarSupported()) {
        var taskbar = Taskbar.getTaskbar();

        if (taskbar.isSupported(Feature.ICON_IMAGE)) {
          final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
          var dockIcon = defaultToolkit.getImage(getClass().getResource(PATH_TO_ICON_PNG));
          taskbar.setIconImage(dockIcon);
        }
      }

      primaryStage.setTitle(applicationTitle);
      primaryStage.setScene(mainWindowController.getMainWindowScene());
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
