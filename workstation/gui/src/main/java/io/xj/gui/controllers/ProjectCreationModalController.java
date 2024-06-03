// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.ProjectModalController;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.ProjectUtils;
import io.xj.gui.utils.UiUtils;
import io.xj.model.pojos.Project;
import io.xj.model.util.LocalFileUtils;
import io.xj.model.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class ProjectCreationModalController extends ProjectModalController {
  static final Map<ProjectCreationMode, String> WINDOW_TITLE = Map.of(
    ProjectCreationMode.NEW_PROJECT, "Create New Project",
    ProjectCreationMode.SAVE_AS_PROJECT, "Save As New Project",
    ProjectCreationMode.DEMO_PROJECT, "Demo Projects"
  );
  private final SimpleDoubleProperty demoImageSize = new SimpleDoubleProperty(120);
  private final FabricationService fabricationService;
  private final ObjectProperty<ProjectCreationMode> mode = new SimpleObjectProperty<>(ProjectCreationMode.NEW_PROJECT);
  private final ObservableBooleanValue isDemoVisible = Bindings.createBooleanBinding(
    () -> mode.get() == ProjectCreationMode.DEMO_PROJECT, mode
  );
  private final ObjectProperty<Project> selectedProject = new SimpleObjectProperty<>();

  @FXML
  VBox container;

  @FXML
  TextField fieldProjectName;

  @FXML
  TextField fieldPathPrefix;

  @FXML
  Button buttonSelectDirectory;

  @FXML
  Button buttonOK;

  @FXML
  Button buttonCancel;

  @FXML
  ImageView demoImageVgm;

  @FXML
  ImageView demoImageBump;

  @FXML
  ImageView demoImageSlaps;

  @FXML
  ImageView demoImageSpace;

  @FXML
  ToggleGroup demoSelection;

  @FXML
  ToggleButton buttonDemoVgm;

  @FXML
  ToggleButton buttonDemoBump;

  @FXML
  ToggleButton buttonDemoSlaps;

  @FXML
  ToggleButton buttonDemoSpace;

  @FXML
  VBox demoContainer;

  public ProjectCreationModalController(
    @Value("classpath:/views/project-creation-modal.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    UIStateService uiStateService,
    ProjectService projectService,
    FabricationService fabricationService,
    ThemeService themeService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.fabricationService = fabricationService;
  }

  @Override
  public void onStageReady() {
    // Add slash to end of file output projectFilePath prefix
    fieldPathPrefix.textProperty().bindBidirectional(projectService.projectsPathPrefixProperty());
    fieldPathPrefix.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
      if (!isNowFocused) {
        UiUtils.addTrailingSlash(fieldPathPrefix);
      }
    });

    demoContainer.visibleProperty().bind(isDemoVisible);
    demoContainer.managedProperty().bind(isDemoVisible);
    demoImageVgm.fitHeightProperty().bind(demoImageSize);
    demoImageVgm.fitWidthProperty().bind(demoImageSize);
    demoImageBump.fitHeightProperty().bind(demoImageSize);
    demoImageBump.fitWidthProperty().bind(demoImageSize);
    demoImageSlaps.fitHeightProperty().bind(demoImageSize);
    demoImageSlaps.fitWidthProperty().bind(demoImageSize);
    demoImageSpace.fitHeightProperty().bind(demoImageSize);
    demoImageSpace.fitWidthProperty().bind(demoImageSize);
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @Override
  public void launchModal() {
    projectService.promptToSaveChanges(() -> createAndShowModal(WINDOW_TITLE.get(mode.get()), null));
  }

  /**
   Set the mode for project creation, e.g. New Project vs Demo Project

   @param mode of project creation
   */
  public void setMode(ProjectCreationMode mode) {
    this.mode.set(mode);
  }

  @FXML
  void handlePressSelectDirectory() {
    var path = ProjectUtils.chooseDirectory(
      buttonSelectDirectory.getScene().getWindow(), "Choose destination folder", fieldPathPrefix.getText()
    );
    if (Objects.nonNull(path)) {
      fieldPathPrefix.setText(LocalFileUtils.addTrailingSlash(path));
    }
  }

  @FXML
  void handlePressOK() {
    var projectName = fieldProjectName.getText().replaceAll("[^a-zA-Z0-9 ]", "");

    if (Objects.equals(mode.get(), ProjectCreationMode.DEMO_PROJECT)
      && Objects.isNull(demoSelection.getSelectedToggle())
      && Objects.isNull(selectedProject.get())) {
      projectService.showWarningAlert(
        "Cannot fetch demo project",
        "Must select a demo project.",
        "Select either a Demo project or, if authenticated, a project from the Lab."
      );
      return;
    }

    if (StringUtils.isNullOrEmpty(projectName)) {
      projectService.showWarningAlert(
        "Cannot create project",
        "Project name is required.",
        "Please enter a name for the project."
      );
      return;
    }

    fabricationService.cancel();
    Platform.runLater(() -> {
      switch (mode.get()) {
        case DEMO_PROJECT ->
          projectService.fetchDemoTemplate(fieldPathPrefix.getText(), ((ToggleButton) demoSelection.getSelectedToggle()).getId(), projectName);
        case NEW_PROJECT ->
          projectService.createProject(fieldPathPrefix.getText(), projectName);
        case SAVE_AS_PROJECT ->
          projectService.saveAsProject(fieldPathPrefix.getText(), projectName);
      }

      Stage stage = (Stage) buttonOK.getScene().getWindow();
      stage.close();
      onStageClose();
    });
  }

  @FXML
  void handlePressCancel() {
    Stage stage = (Stage) buttonCancel.getScene().getWindow();
    stage.close();
    onStageClose();
  }

  @FXML
  void handleDemoPressedVgm(MouseEvent ignored) {
    if (StringUtils.isNullOrEmpty(fieldProjectName.getText()))
      fieldProjectName.setText("Video Game Demo");
    buttonDemoVgm.setSelected(true);
  }

  @FXML
  void handleDemoPressedBump(MouseEvent ignored) {
    if (StringUtils.isNullOrEmpty(fieldProjectName.getText()))
      fieldProjectName.setText("Deep House Demo");
    buttonDemoBump.setSelected(true);
  }

  @FXML
  void handleDemoPressedSlaps(MouseEvent ignored) {
    if (StringUtils.isNullOrEmpty(fieldProjectName.getText()))
      fieldProjectName.setText("Lofi Hip Hop Demo");
    buttonDemoSlaps.setSelected(true);
  }

  @FXML
  void handleDemoPressedSpace(MouseEvent ignored) {
    if (StringUtils.isNullOrEmpty(fieldProjectName.getText()))
      fieldProjectName.setText("Ambient Flow Demo");
    buttonDemoSpace.setSelected(true);
  }
}
