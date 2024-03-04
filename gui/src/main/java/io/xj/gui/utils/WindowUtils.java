// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.utils;

import jakarta.annotation.Nullable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
  String PATH_TO_ICON_ICNS = "/icons/xj-symbol.icns";
  String PATH_TO_ICON_ICO = "/icons/xj-symbol.ico";
  String PATH_TO_ICON_PNG = "/icons/xj-symbol.png";

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
        LOG.debug("Taskbar does not support setting the progress value");
      }
    } else {
      LOG.debug("Taskbar is not supported");
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

  /**
   Closes the stage when clicking outside it (loses focus)
   */
  static void closeWindowOnClickingAway(Stage window) {
    window.focusedProperty().addListener((obs, oldValue, newValue) -> {
      if (!newValue) {
        window.close();
      }
    });
  }

  /**
   Positions the stage scene centered below the mouse click.

   @param child  of which to set position
   @param parent to reference for mouse position
   */
  static void setStagePositionBelowParentNode(Stage child, Node parent) {
    var p = parent.localToScene(0, 0);
    child.setX(parent.getScene().getWindow().getX() + p.getX() + parent.getBoundsInLocal().getWidth() / 2 - child.getWidth() / 2);
    child.setY(parent.getScene().getWindow().getY() + p.getY() + parent.getBoundsInLocal().getHeight() + child.getHeight());
  }

  /**
   Darkens the background of the given stage until it is closed.

   @param stage       behind which to darken background
   @param parentScene on which to attach the darkening effect
   @param onClose     optional function to run after closed
   */
  static void darkenBackgroundUntilClosed(Stage stage, Scene parentScene, @Nullable Runnable onClose) {
    // darken the background
    ColorAdjust darken = new ColorAdjust();
    darken.setBrightness(-0.5);
    parentScene.getRoot().setEffect(darken);

    // remove the background when this element is closed
    stage.setOnHidden(e -> {
      parentScene.getRoot().setEffect(null);
      if (Objects.nonNull(onClose)) onClose.run();
    });
  }

  /**
   Transfers focus to the given pane when the enter key is pressed on the given field.

   @param field on which to listen
   */
  static void transferFocusOnEnterKeyPress(TextField field) {
    field.setOnKeyPressed((KeyEvent e) -> {
      if (e.getCode() == KeyCode.ENTER) {
        field.getParent().requestFocus();
      }
    });
  }

  /**
   Transfers focus to the given pane when the enter key is pressed on the given field.

   @param field on which to listen
   */
  static void transferFocusOnEnterKeyPress(Spinner<?> field) {
    field.setOnKeyPressed((KeyEvent e) -> {
      if (e.getCode() == KeyCode.ENTER) {
        field.getParent().requestFocus();
      }
    });
  }
}
