// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.springframework.stereotype.Service;

@Service
public class ContentContainerController implements ReadyAfterBootController {
  private final ProjectService projectService;

  @FXML
  protected BorderPane container;

  @FXML
  protected ImageView startupContainer;

  public ContentContainerController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    startupContainer.visibleProperty().bind(projectService.getIsStateStandby());
    startupContainer.managedProperty().bind(projectService.getIsStateStandby());


  }

  @Override
  public void onStageClose() {
    // FUTURE: close sub controllers
  }

}
