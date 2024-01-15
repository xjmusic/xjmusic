// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.controllers.content.LibraryBrowserController;
import io.xj.gui.controllers.fabrication.FabricationController;
import io.xj.gui.controllers.template.TemplateBrowserController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MainController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(MainController.class);
  private final FabricationController fabricationController;
  private final MainMenuController mainMenuController;
  private final MainPaneBottomController mainPaneBottomController;
  private final MainPaneTopController mainPaneTopController;
  private final UIStateService uiStateService;
  private final ProjectService projectService;
  private final LibraryBrowserController libraryBrowserController;
  private final TemplateBrowserController templateBrowserController;

  @FXML
  protected ImageView startupContainer;

  public MainController(
    LibraryBrowserController libraryBrowserController,
    TemplateBrowserController templateBrowserController,
    FabricationController fabricationController,
    MainMenuController mainMenuController,
    MainPaneBottomController mainPaneBottomController,
    MainPaneTopController mainPaneTopController,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.libraryBrowserController = libraryBrowserController;
    this.templateBrowserController = templateBrowserController;
    this.fabricationController = fabricationController;
    this.mainMenuController = mainMenuController;
    this.mainPaneBottomController = mainPaneBottomController;
    this.mainPaneTopController = mainPaneTopController;
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    try {
      libraryBrowserController.onStageReady();
      templateBrowserController.onStageReady();
      fabricationController.onStageReady();
      mainMenuController.onStageReady();
      mainPaneBottomController.onStageReady();
      mainPaneTopController.onStageReady();
      uiStateService.onStageReady();

      startupContainer.visibleProperty().bind(projectService.isStateStandbyProperty());
      startupContainer.managedProperty().bind(projectService.isStateStandbyProperty());
    } catch (Exception e) {
      LOG.error("Error initializing main controller!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  @Override
  public void onStageClose() {
    libraryBrowserController.onStageClose();
    templateBrowserController.onStageClose();
    fabricationController.onStageClose();
    mainMenuController.onStageClose();
    mainPaneBottomController.onStageClose();
    mainPaneTopController.onStageClose();
    uiStateService.onStageClose();
  }
}
