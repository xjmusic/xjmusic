package io.xj.gui.controllers;

import io.xj.gui.MainWindowScene;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MainWindowController {
  private final HostServices hostServices;
  private final MainWindowScene mainWindowScene;
  private final String launchGuideUrl;
  private final String lightTheme;
  private final String darkTheme;

  Logger LOG = LoggerFactory.getLogger(MainWindowController.class);

  public MainWindowController(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    MainWindowScene mainWindowScene,
    @Value("${gui.launch.guide.url}") String launchGuideUrl,
    @Value("${gui.theme.light}") String lightTheme,
    @Value("${gui.theme.dark}") String darkTheme
  ) {
    this.hostServices = hostServices;
    this.mainWindowScene = mainWindowScene;
    this.launchGuideUrl = launchGuideUrl;
    this.lightTheme = lightTheme;
    this.darkTheme = darkTheme;
  }

  @FXML
  protected CheckMenuItem darkThemeCheck;

  @FXML
  private void toggleDarkTheme() {
    if (darkThemeCheck.isSelected()) {
      enableDarkTheme();
    } else {
      disableDarkTheme();
    }
  }

  @FXML
  protected void onQuit() {
    LOG.info("Will exit application");
    Platform.exit();
  }

  @FXML
  protected void onLaunchUserGuide() {
    LOG.info("Will launch user guide");
    hostServices.showDocument(launchGuideUrl);
  }

  private void enableDarkTheme() {
    mainWindowScene.get().getStylesheets().remove(lightTheme);
    mainWindowScene.get().getStylesheets().add(darkTheme);
  }

  private void disableDarkTheme() {
    mainWindowScene.get().getStylesheets().remove(darkTheme);
    mainWindowScene.get().getStylesheets().add(lightTheme);
  }
}
