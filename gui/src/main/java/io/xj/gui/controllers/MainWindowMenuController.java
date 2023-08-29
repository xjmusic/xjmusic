package io.xj.gui.controllers;

import io.xj.gui.services.ThemeService;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class MainWindowMenuController extends MenuBar implements ReadyAfterBootController {
  Logger LOG = LoggerFactory.getLogger(MainWindowMenuController.class);
  final HostServices hostServices;
  final ConfigurableApplicationContext ac;
  final String launchGuideUrl;
  final ThemeService themeService;
  final ModalLabConnectionController modalLabConnectionController;

  @FXML
  protected CheckMenuItem checkboxDarkTheme;

  public MainWindowMenuController(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${gui.launch.guide.url}") String launchGuideUrl,
    ConfigurableApplicationContext ac,
    ModalLabConnectionController modalLabConnectionController,
    ThemeService themeService
  ) {
    this.hostServices = hostServices;
    this.ac = ac;
    this.launchGuideUrl = launchGuideUrl;
    this.modalLabConnectionController = modalLabConnectionController;
    this.themeService = themeService;
  }

  @Override
  public void onStageReady() {
    themeService.isDarkThemeProperty().bindBidirectional(checkboxDarkTheme.selectedProperty());
  }

  @FXML
  protected void onQuit() {
    var exitCode = SpringApplication.exit(ac, () -> 0);
    LOG.info("Will exit with code {}", exitCode);
    System.exit(exitCode);
  }

  @FXML
  protected void onLaunchUserGuide() {
    LOG.info("Will launch user guide");
    hostServices.showDocument(launchGuideUrl);
  }

  @FXML
  protected void onConnectToLab() {
    modalLabConnectionController.launchModal();
  }

}
