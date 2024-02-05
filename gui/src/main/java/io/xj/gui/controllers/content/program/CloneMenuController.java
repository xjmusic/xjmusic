package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Optional;

import static io.xj.gui.controllers.content.program.ProgramEditorController.LOG;
import static io.xj.gui.controllers.content.program.ProgramEditorController.closeWindowOnClickingAway;

public class CloneMenuController {
  @FXML
  public TextField programName;
  @FXML
  public ComboBox<Project> projectDropdown;
  @FXML
  public HBox projectComponentsContainer;
  @FXML
  public Label project;
  @FXML
  public ComboBox<Library> libraryDropdown;
  @FXML
  public HBox libraryComponentsContainer;
  @FXML
  public Label library;
  @FXML
  public Button cloneButton;
  @FXML
  public Button cancelButton;
  @FXML
  public Button showProjectDropDown;
  @FXML
  public Button showLibraryDropDown;
  ObservableList<Library> libraries = FXCollections.observableArrayList();

  ObservableList<Project> projects = FXCollections.observableArrayList();


  public void cloneProgramInitializer(Program program, ProjectService projectService, Stage stage) {

    //add libraries to the observableList
    libraries.addAll(projectService.getLibraries());
    projects.addAll(projectService.getContent().getProjects());
    //populate the comboboxes
    libraryDropdown.setItems(libraries);
    projectDropdown.setItems(projects);

    // Set a custom StringConverter to display only the library names
    libraryDropdown.setConverter(new StringConverter<>() {
      @Override
      public String toString(Library object) {
        return (object != null) ? object.getName() : null;
      }

      @Override
      public Library fromString(String string) {
        return null;
      }
    });
    projectDropdown.setConverter(new StringConverter<>() {
      @Override
      public String toString(Project object) {
        return (object != null) ? object.getName() : null;
      }

      @Override
      public Project fromString(String string) {
        return null;
      }
    });

    //instantiate the program library
    Optional<Library> programLibrary = projectService.getContent().getLibrary(program.getLibraryId());
    //use the program library to get the program project
    if (programLibrary.isEmpty())
      return;
    libraryDropdown.setValue(programLibrary.get());
    Optional<Project> programProject = projectService.getContent().getProject(programLibrary.get().getProjectId());
    //check if the project is available
    if (programProject.isEmpty())
      return;
    projectDropdown.setValue(programProject.get());
    library.setText(programLibrary.get().getName());
    project.setText(programProject.get().getName());
    programName.setText(program.getName());
    cancelButton.setOnAction(e -> stage.close());
    closeWindowOnClickingAway(stage);
    cloneProgram(program, projectService, stage);

    libraryDropdown.visibleProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        libraryComponentsContainer.setVisible(!libraryDropdown.isVisible());
      }
    });

    libraryComponentsContainer.visibleProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        libraryDropdown.setVisible(!libraryComponentsContainer.isVisible());
      }
    });
    projectDropdown.visibleProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        projectComponentsContainer.setVisible(!projectDropdown.isVisible());
      }
    });

    projectComponentsContainer.visibleProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        projectDropdown.setVisible(!projectComponentsContainer.isVisible());
      }
    });
    projectDropdown.setVisible(false);
    libraryDropdown.setVisible(false);
    showLibraryDropDown.setOnAction(e -> setShowProjectAndLibraryDropDowns());
    showProjectDropDown.setOnAction(e -> setShowProjectAndLibraryDropDowns());
  }

  private void setShowProjectAndLibraryDropDowns() {
    libraryDropdown.setVisible(true);
    projectDropdown.setVisible(true);
  }

  @FXML
  protected void cloneProgram(Program program, ProjectService projectService, Stage stage) {
    cloneButton.setOnAction(event -> {
      try {
        projectService.cloneProgram(program.getId(), program.getLibraryId(), programName.getText());
        stage.close();
      } catch (Exception e) {
        LOG.info("Error cloning program!!");
      }
    });
  }
}
