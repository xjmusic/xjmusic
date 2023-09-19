// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.GuideService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.ThemeService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class MainMenuController extends MenuBar implements ReadyAfterBootController {
  Logger LOG = LoggerFactory.getLogger(MainMenuController.class);
  final BooleanProperty logsVisible = new SimpleBooleanProperty(false);
  final BooleanProperty logsTailing = new SimpleBooleanProperty(true);
  final ConfigurableApplicationContext ac;
  final ThemeService themeService;
  final GuideService guideService;
  final LabService labService;
  final ModalAboutController modalAboutController;
  final ModalLabAuthenticationController modalLabAuthenticationController;

  @FXML
  protected CheckMenuItem checkboxDarkTheme;

  @FXML
  protected CheckMenuItem checkboxShowLogs;

  @FXML
  protected CheckMenuItem checkboxTailLogs;

  public MainMenuController(
    ConfigurableApplicationContext ac,
    ModalAboutController modalAboutController,
    ModalLabAuthenticationController modalLabAuthenticationController,
    ThemeService themeService,
    GuideService guideService,
    LabService labService
  ) {
    this.ac = ac;
    this.modalAboutController = modalAboutController;
    this.modalLabAuthenticationController = modalLabAuthenticationController;
    this.themeService = themeService;
    this.guideService = guideService;
    this.labService = labService;
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
    guideService.launchGuideInBrowser();
  }

  @FXML
  protected void onPressAbout() {
    modalAboutController.launchModal();
  }

  @FXML
  protected void handleLabAuthentication() {
    modalLabAuthenticationController.launchModal();
  }

  @FXML
  protected void handleLabOpenInBrowser() {
    labService.launchInBrowser();
  }

  public BooleanProperty logsTailingProperty() {
    return logsTailing;
  }

  public BooleanProperty logsVisibleProperty() {
    return logsVisible;
  }
}
