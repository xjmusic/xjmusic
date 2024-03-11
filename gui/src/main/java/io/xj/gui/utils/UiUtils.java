// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.utils;

import jakarta.annotation.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleGroup;
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

public interface UiUtils {
  Logger LOG = LoggerFactory.getLogger(UiUtils.class);
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
        var dockIcon = defaultToolkit.getImage(UiUtils.class.getResource(PATH_TO_ICON_PNG));
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
   Transfers focus away from the given field when the enter key is pressed.

   @param field on which to listen
   */
  static void blurOnEnterKeyPress(TextInputControl field) {
    onSpecialKeyPress(field, () -> field.getParent().requestFocus(), null);
  }

  /**
   Transfers focus away from the given field when the enter key is pressed.

   @param field         on which to listen
   @param onEnterPress  action to take when enter is pressed
   @param onEscapePress action to take when escape is pressed
   */
  static void onSpecialKeyPress(
    TextInputControl field,
    @Nullable Runnable onEnterPress,
    @Nullable Runnable onEscapePress
  ) {
    field.setOnKeyPressed((KeyEvent e) -> {
      if (e.getCode() == KeyCode.ENTER && Objects.nonNull(onEnterPress)) onEnterPress.run();
      if (e.getCode() == KeyCode.ESCAPE && Objects.nonNull(onEscapePress)) onEscapePress.run();
    });
  }

  /**
   Transfers focus away from the given field when a selection is made

   @param chooser on which to listen
   */
  static void blurOnSelection(ComboBox<?> chooser) {
    chooser.setOnAction(e -> chooser.getParent().requestFocus());
  }

  /**
   Prevents the given toggle group from being deselected.

   @param toggleGroup the toggle group
   */
  static void toggleGroupPreventDeselect(ToggleGroup toggleGroup) {
    toggleGroup.selectedToggleProperty().addListener((o, ov, nv) -> {
      if (nv == null) {
        toggleGroup.selectToggle(ov);
      }
    });
  }

  /**
   Utility to take an action on blur (loss of focus)

   @param control the control
   @param action  the action to take
   @return callback to clear the listener
   */
  static Runnable onBlur(Control control, Runnable action) {
    ChangeListener<Boolean> listener = (o, ov, focus) -> {
      if (!focus) {
        action.run();
      }
    };
    control.focusedProperty().addListener(listener);
    return () -> control.focusedProperty().removeListener(listener);
  }

  /**
   Utility to take an action on change of value

   @param property the control
   @param action   the action to take
   @return callback to clear the listener
   */
  static <V> Runnable onChange(ObservableValue<V> property, Runnable action) {
    ChangeListener<V> listener = (o, ov, value) -> action.run();
    property.addListener(listener);
    return () -> property.removeListener(listener);
  }
}
