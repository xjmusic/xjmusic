// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.utils;

import io.xj.hub.util.StringUtils;
import jakarta.annotation.Nullable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.awt.Taskbar;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static io.xj.gui.services.UIStateService.OPEN_PSEUDO_CLASS;

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
   Utility to launch a menu controller
   - apply the pseudo-class :open to the button and remove it after the menu closes
   - darken the background behind the menu
   - position the menu behind the button

   @param <T>                 the type of the controller
   @param button              that opened the menu
   @param fxml                comprising the menu contents
   @param ac                  application context
   @param window              the window to which the menu is attached
   @param positionUnderButton whether to position the menu under the button
   @param setupController     function to set up the controller
   */
  static <T> void launchModalMenu(Button button, Resource fxml, ApplicationContext ac, Window window, boolean positionUnderButton, Consumer<T> setupController) {
    try {
      button.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, true);
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(fxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      T controller = loader.getController();
      setupController.accept(controller);
      stage.setScene(new Scene(root));
      stage.initOwner(window);
      stage.show();
      darkenBackgroundUntilClosed(stage, button.getScene(),
        () -> button.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, false));
      closeWindowOnClickingAway(stage);
      if (positionUnderButton) UiUtils.setStagePositionBelowParentNode(stage, button);

    } catch (IOException e) {
      LOG.error("Failed to launch menu from {}! {}\n{}", fxml.getFilename(), e, StringUtils.formatStackTrace(e));
    }
  }

  /**
   Utility to take an action on blur (loss of focus)

   @param control the control
   @param action  the action to take
   */
  static void onBlur(Control control, Runnable action) {
    control.focusedProperty().addListener((obs, oldValue, newValue) -> {
      if (!newValue) {
        action.run();
      }
    });
  }
}
