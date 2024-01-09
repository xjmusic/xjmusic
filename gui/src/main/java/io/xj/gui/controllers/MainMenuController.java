// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.WorkstationGuiFxApplication;
import io.xj.gui.controllers.impl.MainAboutModalControllerImpl;
import io.xj.gui.controllers.impl.MainLabAuthenticationModalControllerImpl;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.GuideService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCombination;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class MainMenuController extends MenuBar implements ReadyAfterBootController {
  final static String DEBUG = "DEBUG";
  final static String INFO = "INFO";
  final static String WARN = "WARN";
  final static String ERROR = "ERROR";

  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final ThemeService themeService;
  final GuideService guideService;
  final UIStateService uiStateService;
  final LabService labService;
  final UIStateService guiService;
  final FabricationSettingsModalController fabricationSettingsModalController;
  final MainAboutModalControllerImpl mainAboutModalController;
  final MainLabAuthenticationModalControllerImpl mainLabAuthenticationModalController;

  @FXML
  protected MenuItem itemFabricationMainAction;

  @FXML
  protected CheckMenuItem checkboxFabricationFollow;

  @FXML
  protected MenuItem itemOpenFabricationSettings;

  @FXML
  protected CheckMenuItem checkboxDarkTheme;

  @FXML
  protected CheckMenuItem checkboxShowLogs;

  @FXML
  protected CheckMenuItem checkboxTailLogs;

  @FXML
  RadioMenuItem logLevelDebug;

  @FXML
  RadioMenuItem logLevelInfo;

  @FXML
  RadioMenuItem logLevelWarn;

  @FXML
  RadioMenuItem logLevelError;

  @FXML
  ToggleGroup logLevelToggleGroup;

  public MainMenuController(
    ConfigurableApplicationContext ac,
    FabricationService fabricationService,
    GuideService guideService,
    LabService labService,
    MainAboutModalControllerImpl mainAboutModalController,
    FabricationSettingsModalController fabricationSettingsModalController,
    MainLabAuthenticationModalControllerImpl mainLabAuthenticationModalController,
    ThemeService themeService,
    UIStateService guiService,
    UIStateService uiStateService
  ) {
    this.ac = ac;
    this.fabricationService = fabricationService;
    this.guiService = guiService;
    this.guideService = guideService;
    this.labService = labService;
    this.mainAboutModalController = mainAboutModalController;
    this.fabricationSettingsModalController = fabricationSettingsModalController;
    this.mainLabAuthenticationModalController = mainLabAuthenticationModalController;
    this.themeService = themeService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    checkboxFabricationFollow.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());
    checkboxFabricationFollow.setAccelerator(computeFabricationFollowButtonAccelerator());

    checkboxTailLogs.disableProperty().bind(uiStateService.logsVisibleProperty().not());

    itemFabricationMainAction.setAccelerator(computeMainActionButtonAccelerator());
    itemFabricationMainAction.textProperty().bind(fabricationService.mainActionButtonTextProperty().map(this::addLeadingUnderscore));

    itemOpenFabricationSettings.disableProperty().bind(guiService.isFabricationSettingsDisabledProperty());

    checkboxTailLogs.selectedProperty().bindBidirectional(uiStateService.logsTailingProperty());
    checkboxShowLogs.selectedProperty().bindBidirectional(uiStateService.logsVisibleProperty());

    themeService.isDarkThemeProperty().bindBidirectional(checkboxDarkTheme.selectedProperty());

    logLevelToggleGroup = new ToggleGroup();
    logLevelDebug.setToggleGroup(logLevelToggleGroup);
    logLevelInfo.setToggleGroup(logLevelToggleGroup);
    logLevelWarn.setToggleGroup(logLevelToggleGroup);
    logLevelError.setToggleGroup(logLevelToggleGroup);
    switch (uiStateService.logLevelProperty().getValue()) {
      case DEBUG -> logLevelDebug.setSelected(true);
      case INFO -> logLevelInfo.setSelected(true);
      case WARN -> logLevelWarn.setSelected(true);
      case ERROR -> logLevelError.setSelected(true);
    }
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @FXML
  protected void onQuit() {
    WorkstationGuiFxApplication.exit(ac);
  }

  @FXML
  protected void onLaunchUserGuide() {
    guideService.launchGuideInBrowser();
  }

  @FXML
  protected void onPressAbout() {
    mainAboutModalController.launchModal();
  }

  @FXML
  protected void handleLabAuthentication() {
    mainLabAuthenticationModalController.launchModal();
  }

  @FXML
  protected void handleLabOpenInBrowser() {
    labService.launchInBrowser();
  }

  @FXML
  protected void handleOpenFabricationSettings() {
    fabricationSettingsModalController.launchModal();
  }

  @FXML
  public void handleFabricationMainAction(ActionEvent ignored) {
    fabricationService.handleMainAction();
  }

  @FXML
  public void handleSetLogLevel(ActionEvent ignored) {
    uiStateService.logLevelProperty().set(((RadioMenuItem) logLevelToggleGroup.getSelectedToggle()).getText());
  }

  String addLeadingUnderscore(String s) {
    return String.format("_%s", s);
  }

  /**
   Compute the accelerator for the main action button.
   Depending on the platform, it will be either SHORTCUT+SPACE or SHORTCUT+B (on Mac because of conflict).

   @return the accelerator
   */
  KeyCombination computeMainActionButtonAccelerator() {
    return KeyCombination.valueOf("SHORTCUT+" + (System.getProperty("os.name").toLowerCase().contains("mac") ? "B" : "SPACE"));
  }

  /**
   Compute the accelerator for the fabricator follow toggle button.
   Depending on the platform, it will be either SHORTCUT+ALT+SPACE or SHORTCUT+ALT+B (on Mac because of conflict).

   @return the accelerator
   */
  KeyCombination computeFabricationFollowButtonAccelerator() {
    return KeyCombination.valueOf("SHORTCUT+ALT+" + (System.getProperty("os.name").toLowerCase().contains("mac") ? "B" : "SPACE"));
  }
}
