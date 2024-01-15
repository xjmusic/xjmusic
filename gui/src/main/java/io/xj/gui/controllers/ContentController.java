// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import org.springframework.stereotype.Service;

@Service
public class ContentController implements ReadyAfterBootController {
  private final ProjectService projectService;
  private final ContentBrowserController contentBrowserController;
  private final ContentEditorController contentEditorController;

  @FXML
  protected SplitPane container;

  public ContentController(
    ProjectService projectService,
    ContentBrowserController contentBrowserController,
    ContentEditorController contentEditorController
  ) {
    this.projectService = projectService;
    this.contentBrowserController = contentBrowserController;
    this.contentEditorController = contentEditorController;
  }

  @Override
  public void onStageReady() {
    contentBrowserController.onStageReady();
    contentEditorController.onStageReady();

    container.visibleProperty().bind(projectService.isStateReadyProperty().and(projectService.isViewModeContentProperty()));
    container.managedProperty().bind(projectService.isStateReadyProperty().and(projectService.isViewModeContentProperty()));
  }

  @Override
  public void onStageClose() {
    contentBrowserController.onStageClose();
    contentEditorController.onStageClose();
  }

}
