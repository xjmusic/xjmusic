// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.fabrication;

import io.xj.gui.controllers.MainPaneRightController;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import org.springframework.stereotype.Service;

@Service
public class FabricationController implements ReadyAfterBootController {
  private final MainPaneRightController mainPaneRightController;
  private final UIStateService uiStateService;
  private final FabricationTimelineController fabricationTimelineController;
  private final ProjectService projectService;

  public FabricationController(
    ProjectService projectService,
    UIStateService uiStateService,
    FabricationTimelineController fabricationTimelineController,
    MainPaneRightController mainPaneRightController
  ) {
    this.uiStateService = uiStateService;
    this.fabricationTimelineController = fabricationTimelineController;
    this.mainPaneRightController = mainPaneRightController;
    this.projectService = projectService;
  }

  @FXML
  protected BorderPane container;

  @Override
  public void onStageReady() {
    mainPaneRightController.onStageReady();
    fabricationTimelineController.onStageReady();

    container.visibleProperty().bind(projectService.isStateReadyProperty().and(uiStateService.isViewModeFabricationProperty()));
    container.managedProperty().bind(projectService.isStateReadyProperty().and(uiStateService.isViewModeFabricationProperty()));
  }

  @Override
  public void onStageClose() {
    mainPaneRightController.onStageClose();
    fabricationTimelineController.onStageClose();
  }
}
