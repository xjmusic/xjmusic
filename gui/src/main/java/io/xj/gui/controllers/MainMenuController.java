// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
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
  private final FabricationService fabricationService;
  private final PreloaderService preloaderService;
  final ThemeService themeService;
  final GuideService guideService;
  final LabService labService;
  final UIStateService guiService;
  private final ModalFabricationSettingsController modalFabricationSettingsController;
  final ModalAboutController modalAboutController;
  final ModalLabAuthenticationController modalLabAuthenticationController;

  @FXML
  protected MenuItem itemFabricationMainAction;

  @FXML
  protected CheckMenuItem checkboxFabricationFollow;

  @FXML
  protected MenuItem itemOpenFabricationSettings;

  @FXML
  protected MenuItem itemPreload;

  @FXML
  protected CheckMenuItem checkboxDarkTheme;

  @FXML
  protected CheckMenuItem checkboxShowLogs;

  @FXML
  protected CheckMenuItem checkboxTailLogs;

  public MainMenuController(
    ConfigurableApplicationContext ac,
    ModalFabricationSettingsController modalFabricationSettingsController,
    ModalAboutController modalAboutController,
    ModalLabAuthenticationController modalLabAuthenticationController,
    FabricationService fabricationService,
    PreloaderService preloaderService,
    ThemeService themeService,
    GuideService guideService,
    LabService labService,
    UIStateService guiService
  ) {
    this.ac = ac;
    this.modalFabricationSettingsController = modalFabricationSettingsController;
    this.modalAboutController = modalAboutController;
    this.modalLabAuthenticationController = modalLabAuthenticationController;
    this.fabricationService = fabricationService;
    this.preloaderService = preloaderService;
    this.themeService = themeService;
    this.guideService = guideService;
    this.labService = labService;
    this.guiService = guiService;
  }

  @Override
  public void onStageReady() {
    checkboxFabricationFollow.disableProperty().bind(preloaderService.runningProperty());
    checkboxFabricationFollow.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());
    checkboxFabricationFollow.setAccelerator(computeFabricationFollowButtonAccelerator());

    checkboxTailLogs.disableProperty().bind(logsVisible.not());

    itemFabricationMainAction.disableProperty().bind(guiService.fabricationActionDisabledProperty());
    itemFabricationMainAction.setAccelerator(computeMainActionButtonAccelerator());
    itemFabricationMainAction.textProperty().bind(fabricationService.mainActionButtonTextProperty().map(this::addLeadingUnderscore));

    itemOpenFabricationSettings.disableProperty().bind(guiService.fabricationSettingsDisabledProperty());

    itemPreload.disableProperty().bind(fabricationService.isStatusActive());
    itemPreload.textProperty().bind(preloaderService.actionTextProperty().map(this::addLeadingUnderscore));

    logsTailing.bindBidirectional(checkboxTailLogs.selectedProperty());
    logsVisible.bindBidirectional(checkboxShowLogs.selectedProperty());

    themeService.isDarkThemeProperty().bindBidirectional(checkboxDarkTheme.selectedProperty());
  }

  private String addLeadingUnderscore(String s) {
    return String.format("_%s", s);
  }

  /**
   Compute the accelerator for the main action button.
   Depending on the platform, it will be either SHORTCUT+SPACE or SHORTCUT+B (on Mac because of conflict).

   @return the accelerator
   */
  private KeyCombination computeMainActionButtonAccelerator() {
    return KeyCombination.valueOf("SHORTCUT+" + (System.getProperty("os.name").toLowerCase().contains("mac") ? "B" : "SPACE"));
  }

  /**
   Compute the accelerator for the fabricator follow toggle button.
   Depending on the platform, it will be either SHORTCUT+ALT+SPACE or SHORTCUT+ALT+B (on Mac because of conflict).

   @return the accelerator
   */
  private KeyCombination computeFabricationFollowButtonAccelerator() {
    return KeyCombination.valueOf("SHORTCUT+ALT+" + (System.getProperty("os.name").toLowerCase().contains("mac") ? "B" : "SPACE"));
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

  @FXML
  protected void handleOpenFabricationSettings() {
    modalFabricationSettingsController.launchModal();
  }

  @FXML
  protected void handlePreload() {
    if (preloaderService.isRunning()) {
      preloaderService.cancel();
    } else {
      preloaderService.resetAndStart();
    }
  }

  public BooleanProperty logsTailingProperty() {
    return logsTailing;
  }

  public BooleanProperty logsVisibleProperty() {
    return logsVisible;
  }

  @FXML
  public void handleFabricationMainAction(ActionEvent ignored) {
    fabricationService.handleMainAction();
  }
}
