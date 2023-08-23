package io.xj.gui;

import jakarta.annotation.Nullable;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public interface WorkstationIcon {
  String APPLICATION_TITLE = "XJ music workstation";
  String PATH_TO_ICON_ICNS = "/icons/icon.icns";
  String PATH_TO_ICON_ICO = "/icons/icon.ico";
  String PATH_TO_ICON_PNG = "/icons/icon.png";
  String PATH_TO_ICON_SVG = "/icons/icon.svg";

  static void setup(Stage primaryStage, @Nullable String windowName) {
    primaryStage.setTitle(Objects.nonNull(windowName) ? String.format("%s - %s", windowName, APPLICATION_TITLE) : APPLICATION_TITLE);
    primaryStage.getIcons().addAll(List.of(
      new Image(PATH_TO_ICON_ICNS),
      new Image(PATH_TO_ICON_ICO),
      new Image(PATH_TO_ICON_PNG),
      new Image(PATH_TO_ICON_SVG)
    ));
  }

  static void setupTaskbar() {
    if (Taskbar.isTaskbarSupported()) {
      var taskbar = Taskbar.getTaskbar();

      if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
        final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        var dockIcon = defaultToolkit.getImage(WorkstationIcon.class.getResource(PATH_TO_ICON_PNG));
        taskbar.setIconImage(dockIcon);
      }
    }
  }
}
