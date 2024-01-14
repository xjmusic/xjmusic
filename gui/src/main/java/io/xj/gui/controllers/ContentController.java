// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
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

  @FXML
  protected AnchorPane contentBrowser;

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

    contentBrowser.visibleProperty().bind(projectService.isStateReadyProperty());
    contentBrowser.managedProperty().bind(projectService.isStateReadyProperty());

    startupContainer.visibleProperty().bind(projectService.isStateStandbyProperty());
    startupContainer.managedProperty().bind(projectService.isStateStandbyProperty());
  }

  @Override
  public void onStageClose() {
    contentBrowserController.onStageClose();

    // FUTURE: close sub controllers
  }

}
