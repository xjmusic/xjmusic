// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.fabrication;

import io.xj.gui.controllers.MainPaneRightController;
import io.xj.gui.ProjectController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class FabricationController extends ProjectController {
  private final MainPaneRightController mainPaneRightController;
  private final FabricationTimelineController fabricationTimelineController;

  public FabricationController(
    @Value("classpath:/views/fabrication.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService,
    FabricationTimelineController fabricationTimelineController,
    MainPaneRightController mainPaneRightController
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.fabricationTimelineController = fabricationTimelineController;
    this.mainPaneRightController = mainPaneRightController;
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
