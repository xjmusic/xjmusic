// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import org.springframework.stereotype.Service;

@Service
public class ContentBrowserController implements ReadyAfterBootController {
  private final ProjectService projectService;
  private final ContentBrowserLibrariesController contentBrowserLibrariesController;

  @FXML
  protected AnchorPane browserLibraries;

  public ContentBrowserController(
    ProjectService projectService,
    ContentBrowserLibrariesController contentBrowserLibrariesController
  ) {
    this.projectService = projectService;
    this.contentBrowserLibrariesController = contentBrowserLibrariesController;
  }

  @Override
  public void onStageReady() {
    contentBrowserLibrariesController.onStageReady();
  }

  @Override
  public void onStageClose() {
    contentBrowserLibrariesController.onStageClose();
  }

}
