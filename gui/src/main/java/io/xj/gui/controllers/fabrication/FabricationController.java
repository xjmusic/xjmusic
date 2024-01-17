// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.fabrication;

import io.xj.gui.controllers.MainPaneRightController;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.services.ProjectService;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import org.springframework.stereotype.Service;

@Service
public class FabricationController implements ReadyAfterBootController {
  private final MainPaneRightController mainPaneRightController;
  private final FabricationTimelineController fabricationTimelineController;
  private final ProjectService projectService;

  public FabricationController(
    FabricationTimelineController fabricationTimelineController,
    MainPaneRightController mainPaneRightController,
    ProjectService projectService
  ) {
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

    container.visibleProperty().bind(projectService.isStateReadyProperty().and(projectService.isViewModeFabricationProperty()));
    container.managedProperty().bind(projectService.isStateReadyProperty().and(projectService.isViewModeFabricationProperty()));
  }

  @Override
  public void onStageClose() {
    mainPaneRightController.onStageClose();
    fabricationTimelineController.onStageClose();
  }
}
