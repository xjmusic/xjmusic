// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.impl;

import io.xj.gui.controllers.ContentContainerController;
import io.xj.gui.controllers.ContentProjectCreationModalController;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.utils.DirectoryChooserUtils;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ContentContainerControllerImpl implements ReadyAfterBootController, ContentContainerController {
  private final ContentProjectCreationModalController contentProjectCreationModalController;
  private final ProjectService projectService;

  @FXML
  protected VBox startupContainer;

  public ContentContainerControllerImpl(
    ContentProjectCreationModalController contentProjectCreationModalController,
    ProjectService projectService
  ) {
    this.contentProjectCreationModalController = contentProjectCreationModalController;
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    // todo open sub controllers
  }

  @Override
  public void onStageClose() {
    // todo close sub controllers
  }

  @FXML
  protected void handlePressOpenProject() {
    var path = DirectoryChooserUtils.chooseDirectory(
      startupContainer.getScene().getWindow(), "Choose project folder", projectService.pathPrefixProperty().getValue()
    );
    if (Objects.nonNull(path)) {
      projectService.openProject(path);
    }
  }

  @FXML
  protected void handlePressNewProject() {
    contentProjectCreationModalController.launchModal();
  }

  @FXML
  protected void handlePressCloneProjectFromLab() {
    // todo implement modal to handle Clone Project From Lab
  }

  @FXML
  protected void handlePressCloneProject() {
    // todo implement modal to handle Clone Demo Project
  }
}
