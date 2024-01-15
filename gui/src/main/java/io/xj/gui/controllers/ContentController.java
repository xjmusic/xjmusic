// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ProjectViewMode;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.springframework.stereotype.Service;

@Service
public class ContentController implements ReadyAfterBootController {
  private final ProjectService projectService;
  private final ContentBrowserController contentBrowserController;

  @FXML
  protected BorderPane container;

  @FXML
  protected StackPane startupContainer;

  public ContentController(
    ProjectService projectService,
    ContentBrowserController contentBrowserController
  ) {
    this.projectService = projectService;
    this.contentBrowserController = contentBrowserController;
  }

  @Override
  public void onStageReady() {
    contentBrowserController.onStageReady();

    container.visibleProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.CONTENT));
    container.managedProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.CONTENT));

    startupContainer.visibleProperty().bind(projectService.isStateStandbyProperty());
    startupContainer.managedProperty().bind(projectService.isStateStandbyProperty());
  }

  @Override
  public void onStageClose() {
    contentBrowserController.onStageClose();

    // FUTURE: close sub controllers
  }

}
