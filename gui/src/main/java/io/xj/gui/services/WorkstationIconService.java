package io.xj.gui.services;

import jakarta.annotation.Nullable;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.Objects;

@Service
public class WorkstationIconService {
  private final String applicationTitle;

  private static final String PATH_TO_ICON_ICNS = "/icons/icon.icns";
  private static final String PATH_TO_ICON_ICO = "/icons/icon.ico";
  private static final String PATH_TO_ICON_PNG = "/icons/icon.png";
  private static final String PATH_TO_ICON_SVG = "/icons/icon.svg";

  public WorkstationIconService(
    @Value("${application.ui.title}") String applicationTitle
  ) {
    this.applicationTitle = applicationTitle;
  }

  public void setup(Stage primaryStage, @Nullable String windowName) {
    primaryStage.setTitle(Objects.nonNull(windowName) ? String.format("%s - %s", windowName, applicationTitle) : applicationTitle);
    primaryStage.getIcons().addAll(List.of(
      new Image(PATH_TO_ICON_ICNS),
      new Image(PATH_TO_ICON_ICO),
      new Image(PATH_TO_ICON_PNG),
      new Image(PATH_TO_ICON_SVG)
    ));
  }

  public void setupTaskbar() {
    if (Taskbar.isTaskbarSupported()) {
      var taskbar = Taskbar.getTaskbar();

      if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
        final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        var dockIcon = defaultToolkit.getImage(getClass().getResource(PATH_TO_ICON_PNG));
        taskbar.setIconImage(dockIcon);
      }
    }
  }
}
