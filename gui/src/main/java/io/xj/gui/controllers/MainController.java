// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ProjectViewMode;
import io.xj.gui.services.UIStateService;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

@Service
public class MainController implements ReadyAfterBootController {
  private final ContentContainerController contentContainerController;
  private final FabricationContainerController fabricationContainerController;
  private final MainMenuController mainMenuController;
  private final MainPaneBottomController mainPaneBottomController;
  private final MainPaneTopController mainPaneTopController;
  private final ProjectService projectService;
  private final UIStateService uiStateService;

  public MainController(
    ContentContainerController contentContainerController,
    FabricationContainerController fabricationContainerController,
    MainMenuController mainMenuController,
    MainPaneBottomController mainPaneBottomController,
    MainPaneTopController mainPaneTopController,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.contentContainerController = contentContainerController;
    this.fabricationContainerController = fabricationContainerController;
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
    mainMenuController.onStageReady();
    mainPaneBottomController.onStageReady();
    mainPaneTopController.onStageReady();
    uiStateService.onStageReady();

    contentContainerController.onStageReady();
    contentContainer.visibleProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.CONTENT));
    contentContainer.managedProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.CONTENT));

    fabricationContainerController.onStageReady();
    fabricationContainer.visibleProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.FABRICATION));
    fabricationContainer.managedProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.FABRICATION));
  }

  @Override
  public void onStageClose() {
    contentContainerController.onStageClose();
    fabricationContainerController.onStageClose();
    mainMenuController.onStageClose();
    mainPaneBottomController.onStageClose();
    mainPaneTopController.onStageClose();
    uiStateService.onStageClose();
  }
}
