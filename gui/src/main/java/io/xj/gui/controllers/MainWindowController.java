package io.xj.gui.controllers;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MainWindowController {
  private final HostServices hostServices;
  private final String launchGuideUrl;
  Logger LOG = LoggerFactory.getLogger(MainWindowController.class);

  public MainWindowController(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${gui.launch.guide.url}") String launchGuideUrl
  ) {
    this.hostServices = hostServices;
    this.launchGuideUrl = launchGuideUrl;
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
}
