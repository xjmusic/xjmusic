// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ProjectViewMode;
import io.xj.gui.services.UIStateService;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MainController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(MainController.class);
  private final ContentController contentController;
  private final FabricationController fabricationController;
  private final MainMenuController mainMenuController;
  private final MainPaneBottomController mainPaneBottomController;
  private final MainPaneTopController mainPaneTopController;
  private final ProjectService projectService;
  private final UIStateService uiStateService;

  public MainController(
    ContentController contentController,
    FabricationController fabricationController,
    MainMenuController mainMenuController,
    MainPaneBottomController mainPaneBottomController,
    MainPaneTopController mainPaneTopController,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.contentController = contentController;
    this.fabricationController = fabricationController;
    this.mainMenuController = mainMenuController;
    this.mainPaneBottomController = mainPaneBottomController;
    this.mainPaneTopController = mainPaneTopController;
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  @FXML
  public BorderPane fabricationContainer;

  @FXML
  public BorderPane contentContainer;

  @FXML
  public VBox mainPaneBottom;

  @FXML
  public VBox mainPaneTop;

  @FXML
  public MenuBar mainMenu;

  @Override
  public void onStageReady() {
    try {
      mainMenuController.onStageReady();
      mainPaneBottomController.onStageReady();
      mainPaneTopController.onStageReady();
      uiStateService.onStageReady();

      contentController.onStageReady();
      contentContainer.visibleProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.CONTENT));
      contentContainer.managedProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.CONTENT));

      fabricationController.onStageReady();
      fabricationContainer.visibleProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.FABRICATION));
      fabricationContainer.managedProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.FABRICATION));

    } catch (Exception e) {
      LOG.error("Error initializing main controller!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  @Override
  public void onStageClose() {
    contentController.onStageClose();
    fabricationController.onStageClose();
    mainMenuController.onStageClose();
    mainPaneBottomController.onStageClose();
    mainPaneTopController.onStageClose();
    uiStateService.onStageClose();
  }
}
