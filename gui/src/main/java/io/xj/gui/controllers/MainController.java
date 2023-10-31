// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import jakarta.annotation.Nullable;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

@Service
public class MainController implements ReadyAfterBootController {
  final FabricationService fabricationService;
  final MainMenuController mainMenuController;
  final MainPaneBottomController mainPaneBottomController;
  final MainPaneTopController mainPaneTopController;
  final MainTimelineController mainTimelineController;
  final ThemeService themeService;
  final UIStateService uiStateService;

  @Nullable
  Scene mainWindowScene;

  public MainController(
    FabricationService fabricationService,
    MainMenuController mainMenuController,
    MainPaneBottomController mainPaneBottomController,
    MainPaneTopController mainPaneTopController,
    MainTimelineController mainTimelineController,
    ThemeService themeService,
    UIStateService uiStateService
  ) {
    this.fabricationService = fabricationService;
    this.mainMenuController = mainMenuController;
    this.mainPaneBottomController = mainPaneBottomController;
    this.mainPaneTopController = mainPaneTopController;
    this.mainTimelineController = mainTimelineController;
    this.themeService = themeService;
    this.uiStateService = uiStateService;
  }

  @FXML
  public ScrollPane mainTimeline;

  @FXML
  public VBox mainPaneBottom;

  @FXML
  public VBox mainPaneTop;

  @FXML
  public MenuBar mainMenu;

  @Override
  public void onStageReady() {
    themeService.setup(mainWindowScene);
    themeService.isDarkThemeProperty().addListener((observable, oldValue, newValue) -> themeService.setup(mainWindowScene));

    mainMenuController.onStageReady();
    mainPaneTopController.onStageReady();
    mainPaneBottomController.onStageReady();
    mainTimelineController.onStageReady();
    uiStateService.onStageReady();
  }

  @Override
  public void onStageClose() {
    mainMenuController.onStageClose();
    mainPaneBottomController.onStageClose();
    mainPaneTopController.onStageClose();
    mainTimelineController.onStageClose();
    uiStateService.onStageClose();
  }

  public @Nullable Scene getMainWindowScene() {
    return mainWindowScene;
  }

  public void setMainWindowScene(@Nullable Scene mainWindowScene) {
    this.mainWindowScene = mainWindowScene;
  }

}
