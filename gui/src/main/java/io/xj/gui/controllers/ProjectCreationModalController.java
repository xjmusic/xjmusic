// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.utils.ProjectUtils;
import io.xj.gui.utils.TextParsingUtils;
import io.xj.hub.util.StringUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
public class ProjectCreationModalController extends ReadyAfterBootModalController {
  static final Map<ProjectCreationMode, String> WINDOW_TITLE = Map.of(
    ProjectCreationMode.NEW_PROJECT, "Create New Project",
    ProjectCreationMode.CLONE_PROJECT, "Clone Project"
  );
  private final SimpleDoubleProperty demoImageSize = new SimpleDoubleProperty(120);
  private final Resource contentProjectCreationModalFxml;
  private final ConfigurableApplicationContext ac;
  private final ProjectService projectService;
  private final ThemeService themeService;
  private final ObjectProperty<ProjectCreationMode> mode = new SimpleObjectProperty<>(ProjectCreationMode.NEW_PROJECT);
  private final ObservableBooleanValue isDemoVisible = Bindings.createBooleanBinding(
    () -> mode.get() == ProjectCreationMode.CLONE_PROJECT, mode
  );

  @FXML
  protected VBox container;

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

  @FXML
  ImageView demoImageBump;

  @FXML
  ImageView demoImageSlaps;

  @FXML
  ImageView demoImageSpace;

  @FXML
  ToggleGroup demoSelection;

  @FXML
  ToggleButton buttonDemoBump;

  @FXML
  ToggleButton buttonDemoSlaps;

  @FXML
  ToggleButton buttonDemoSpace;

  @FXML
  VBox demoContainer;

  public ProjectCreationModalController(
    @Value("classpath:/views/project-creation-modal.fxml") Resource contentProjectCreationModalFxml,
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
    // Add slash to end of "file output projectFilePath prefix"
    // https://www.pivotaltracker.com/story/show/186555998
    fieldPathPrefix.textProperty().bindBidirectional(projectService.basePathPrefixProperty());
    fieldPathPrefix.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
      if (!isNowFocused) {
        TextParsingUtils.addTrailingSlash(fieldPathPrefix);
      }
    });

    demoContainer.visibleProperty().bind(isDemoVisible);
    demoContainer.managedProperty().bind(isDemoVisible);
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
    createAndShowModal(ac, themeService, contentProjectCreationModalFxml, WINDOW_TITLE.get(mode.get()));
  }

  /**
   Set the mode for project creation, e.g. New Project vs Clone Project

   @param mode of project creation
   */
  public void setMode(ProjectCreationMode mode) {
    this.mode.set(mode);
  }

  @FXML
  protected void handlePressSelectDirectory() {
    var path = ProjectUtils.chooseDirectory(
      buttonSelectDirectory.getScene().getWindow(), "Choose destination folder", fieldPathPrefix.getText()
    );
    if (Objects.nonNull(path)) {
      fieldPathPrefix.setText(TextParsingUtils.addTrailingSlash(path));
    }
  }

  @FXML
  protected void handlePressOK() {
    if (Objects.equals(mode.get(), ProjectCreationMode.CLONE_PROJECT)
      && Objects.isNull(demoSelection.getSelectedToggle())) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setGraphic(null);
      alert.setTitle("Cannot clone project");
      alert.setHeaderText("Demo project must be selected");
      alert.setContentText("You must select a project to clone.");
      alert.showAndWait();
      return;
    }

    if (StringUtils.isNullOrEmpty(fieldProjectName.getText())) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setGraphic(null);
      alert.setTitle("Cannot create project");
      alert.setHeaderText("Project Name cannot be empty");
      alert.setContentText("You must specify a project name to create a new project.");
      alert.showAndWait();
      return;
    }

    switch (mode.get()) {
      case CLONE_PROJECT ->
        projectService.cloneFromDemoTemplate(fieldPathPrefix.getText(), ((ToggleButton) demoSelection.getSelectedToggle()).getId(), fieldProjectName.getText());
      case NEW_PROJECT -> projectService.createProject(fieldPathPrefix.getText(), fieldProjectName.getText());
    }

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

  @FXML
  public void handleDemoPressedBump(MouseEvent ignored) {
    fieldProjectName.setText("Deep House Demo");
    buttonDemoBump.setSelected(true);
  }

  @FXML
  public void handleDemoPressedSlaps(MouseEvent ignored) {
    fieldProjectName.setText("Lofi Hip Hop Demo");
    buttonDemoSlaps.setSelected(true);
  }

  @FXML
  public void handleDemoPressedSpace(MouseEvent ignored) {
    fieldProjectName.setText("Ambient Flow Demo");
    buttonDemoSpace.setSelected(true);
  }
}
