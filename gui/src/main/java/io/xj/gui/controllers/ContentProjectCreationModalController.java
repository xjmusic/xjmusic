// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.utils.DirectoryChooserUtils;
import io.xj.gui.utils.TextParsingUtils;
import io.xj.hub.util.StringUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ContentProjectCreationModalController extends ReadyAfterBootModalController {
  static final String WINDOW_TITLE = "Create New Project";
  private final Resource contentProjectCreationModalFxml;
  private final ConfigurableApplicationContext ac;
  private final ProjectService projectService;
  private final ThemeService themeService;
  private final ObjectProperty<ContentProjectCreationMode> mode = new SimpleObjectProperty<>(ContentProjectCreationMode.NEW_PROJECT);

  @FXML
  protected TextField fieldProjectName;

  @FXML
  protected TextField fieldPathPrefix;

  @FXML
  protected Button buttonSelectDirectory;

  @FXML
  protected Button buttonOK;

  @FXML
  protected Button buttonCancel;

  public ContentProjectCreationModalController(
    @Value("classpath:/views/content-project-creation-modal.fxml") Resource contentProjectCreationModalFxml,
    ConfigurableApplicationContext ac,
    ProjectService projectService,
    ThemeService themeService
  ) {
    this.contentProjectCreationModalFxml = contentProjectCreationModalFxml;
    this.ac = ac;
    this.projectService = projectService;
    this.themeService = themeService;
  }

  @Override
  public void onStageReady() {
    // Add slash to end of "file output path prefix"
    // https://www.pivotaltracker.com/story/show/186555998
    fieldPathPrefix.textProperty().bindBidirectional(projectService.pathPrefixProperty());
    fieldPathPrefix.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
      if (!isNowFocused) {
        TextParsingUtils.addTrailingSlash(fieldPathPrefix);
      }
    });
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @Override
  public void launchModal() {
    doLaunchModal(ac, themeService, contentProjectCreationModalFxml, WINDOW_TITLE);
  }

  /**
   Set the mode for project creation, e.g. New Project vs Clone Project

   @param mode of project creation
   */
  public void setMode(ContentProjectCreationMode mode) {
    this.mode.set(mode);
  }

  @FXML
  protected void handlePressSelectDirectory() {
    var path = DirectoryChooserUtils.chooseDirectory(
      buttonSelectDirectory.getScene().getWindow(), "Choose destination folder", fieldPathPrefix.getText()
    );
    if (Objects.nonNull(path)) {
      fieldPathPrefix.setText(path);
    }
  }

  @FXML
  protected void handlePressOK() {
    if (StringUtils.isNullOrEmpty(fieldProjectName.getText())) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setGraphic(null);
      alert.setTitle("Cannot create project");
      alert.setHeaderText("Project Name cannot be empty");
      alert.setContentText("You must specify a project name to create a new project.");
      alert.showAndWait();
      return;
    }
    projectService.createProject(fieldPathPrefix.getText(), fieldProjectName.getText());
    Stage stage = (Stage) buttonOK.getScene().getWindow();
    stage.close();
    onStageClose();
  }

  @FXML
  protected void handlePressCancel() {
    Stage stage = (Stage) buttonCancel.getScene().getWindow();
    stage.close();
    onStageClose();
  }
}
