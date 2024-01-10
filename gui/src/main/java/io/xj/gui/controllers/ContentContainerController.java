// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import io.xj.gui.utils.DirectoryChooserUtils;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ContentContainerController implements ReadyAfterBootController {
  private final ProjectCreationModalController projectCreationModalController;
  private final ProjectService projectService;

  @FXML
  protected VBox startupContainer;

  public ContentContainerController(
    ProjectCreationModalController projectCreationModalController,
    ProjectService projectService
  ) {
    this.projectCreationModalController = projectCreationModalController;
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
      startupContainer.getScene().getWindow(), "Choose project folder", projectService.basePathPrefixProperty().getValue()
    );
    if (Objects.nonNull(path)) {
      projectService.openProject(path);
    }
  }

  @FXML
  protected void handlePressNewProject() {
    projectCreationModalController.setMode(ProjectCreationMode.NEW_PROJECT);
    projectCreationModalController.launchModal();
  }

  @FXML
  protected void handlePressCloneProject() {
    projectCreationModalController.setMode(ProjectCreationMode.CLONE_PROJECT);
    projectCreationModalController.launchModal();
  }
}
