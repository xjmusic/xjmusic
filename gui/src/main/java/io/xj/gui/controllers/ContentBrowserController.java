// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.springframework.stereotype.Service;

@Service
public class ContentBrowserController implements ReadyAfterBootController {
  private final ProjectService projectService;

  public ContentBrowserController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    // TODO on stage ready
  }

  @Override
  public void onStageClose() {
    // FUTURE: close sub controllers
  }

}
