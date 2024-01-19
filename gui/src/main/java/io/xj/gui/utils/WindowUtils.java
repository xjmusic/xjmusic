// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.utils;

import jakarta.annotation.Nullable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.awt.Taskbar;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public interface WindowUtils {
  Logger LOG = LoggerFactory.getLogger(WindowUtils.class);
  String APPLICATION_TITLE = "XJ music workstation";
  String PATH_TO_ICON_ICNS = "/icons/icon.icns";
  String PATH_TO_ICON_ICO = "/icons/icon.ico";
  String PATH_TO_ICON_PNG = "/icons/icon.png";

  /**
   Sets up the icon for the given stage.

   @param primaryStage the stage
   */
  static void setupIcon(Stage primaryStage) {
    primaryStage.getIcons().addAll(List.of(
      new Image(PATH_TO_ICON_ICNS),
      new Image(PATH_TO_ICON_ICO),
      new Image(PATH_TO_ICON_PNG)
    ));
  }

  /**
   Computes the title for the given window name.

   @param windowName the name of the window
   @return the title
   */
  static String computeTitle(@Nullable String windowName) {
    return Objects.nonNull(windowName) ? String.format("%s - %s", windowName, APPLICATION_TITLE) : APPLICATION_TITLE;
  }

  /**
   Sets up the taskbar icon.
   */
  static void setupTaskbar() {
    if (Taskbar.isTaskbarSupported()) {
      var taskbar = Taskbar.getTaskbar();

      if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
        final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        var dockIcon = defaultToolkit.getImage(WindowUtils.class.getResource(PATH_TO_ICON_PNG));
        taskbar.setIconImage(dockIcon);
      } else {
        LOG.info("Taskbar does not support setting the dock icon");
      }
    } else {
      LOG.info("Taskbar is not supported");
    }
  }

  /**
   Sets the taskbar progress.

   @param progress the progress
   */
  static void setTaskbarProgress(double progress) {
    if (Taskbar.isTaskbarSupported()) {
      var taskbar = Taskbar.getTaskbar();
      if (taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
        taskbar.setProgressValue(progress < 1.0 && progress > 0.0 ? (int) (progress * 100) : -1);
      } else {
        LOG.info("Taskbar does not support setting the progress value");
      }
    } else {
      LOG.info("Taskbar is not supported");
    }
  }

  /**
   Load the scene from the given FXML file.

   @param ac             the application context
   @param mainWindowFxml the FXML file
   @return the scene
   @throws IOException if the scene cannot be loaded
   */
  static Scene loadSceneFxml(ApplicationContext ac, Resource mainWindowFxml) throws IOException {
    FXMLLoader mainWindowFxmlLoader = new FXMLLoader(mainWindowFxml.getURL());
    mainWindowFxmlLoader.setControllerFactory(ac::getBean);
    return new Scene(mainWindowFxmlLoader.load());
  }
}
