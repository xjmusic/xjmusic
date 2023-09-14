// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ThemeService;
import javafx.application.HostServices;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class MainMenuController extends MenuBar implements ReadyAfterBootController {
  Logger LOG = LoggerFactory.getLogger(MainMenuController.class);
  final BooleanProperty logsVisible = new SimpleBooleanProperty(false);
  final BooleanProperty logsTailing = new SimpleBooleanProperty(true);
  final HostServices hostServices;
  final ConfigurableApplicationContext ac;
  final String launchGuideUrl;
  final ThemeService themeService;
  final ModalAboutController modalAboutController;
  final ModalLabConnectionController modalLabConnectionController;

  @FXML
  protected CheckMenuItem checkboxDarkTheme;

  @FXML
  protected CheckMenuItem checkboxShowLogs;

  @FXML
  protected CheckMenuItem checkboxTailLogs;

  public MainMenuController(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${gui.launch.guide.url}") String launchGuideUrl,
    ConfigurableApplicationContext ac,
    ModalAboutController modalAboutController,
    ModalLabConnectionController modalLabConnectionController,
    ThemeService themeService
  ) {
    this.ac = ac;
    this.hostServices = hostServices;
    this.launchGuideUrl = launchGuideUrl;
    this.modalAboutController = modalAboutController;
    this.modalLabConnectionController = modalLabConnectionController;
    this.themeService = themeService;
  }

  @Override
  public void onStageReady() {
    themeService.isDarkThemeProperty().bindBidirectional(checkboxDarkTheme.selectedProperty());
    logsVisible.bindBidirectional(checkboxShowLogs.selectedProperty());
    logsTailing.bindBidirectional(checkboxTailLogs.selectedProperty());
    checkboxTailLogs.disableProperty().bind(logsVisible.not());
  }

  @Override
  public void onStageClose() {
    // no op
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
  protected void onPressAbout() {
    modalAboutController.launchModal();
  }

  @FXML
  protected void onConnectToLab() {
    modalLabConnectionController.launchModal();
  }

  public BooleanProperty logsTailingProperty() {
    return logsTailing;
  }

  public BooleanProperty logsVisibleProperty() {
    return logsVisible;
  }
}
