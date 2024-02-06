package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static io.xj.gui.controllers.content.program.ProgramEditorController.LOG;
import static io.xj.gui.controllers.content.program.ProgramEditorController.closeWindowOnClickingAway;

@Component
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
  private final ProjectService projectService;
  private final ThemeService themeService;
  public CloneMenuController(ProjectService projectService, ThemeService themeService){
    this.projectService=projectService;
    this.themeService = themeService;
  }

  public void setUp(Program program, Stage stage) {

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
    Library programLibrary = projectService.getContent().getLibrary(program.getLibraryId())
      .orElseThrow(() -> new RuntimeException("Could not find Library"));
    libraryDropdown.setValue(programLibrary);
    //use the program library to get the program project
    Project programProject = projectService.getContent().getProject(programLibrary.getProjectId())
      .orElseThrow(() -> new RuntimeException("Could not find Project"));
    projectDropdown.setValue(programProject);
    library.setText(programLibrary.getName());
    project.setText(programProject.getName());
    programName.setText(program.getName());
    cancelButton.setOnAction(e -> stage.close());
    closeWindowOnClickingAway(stage);
    cloneProgram(program, projectService, stage);

//    libraryDropdown.visibleProperty().addListener((observable, oldValue, newValue) -> {
//      if (newValue) {
//        libraryComponentsContainer.setVisible(!libraryDropdown.isVisible());
//      }
//    });

//    libraryComponentsContainer.visibleProperty().addListener((observable, oldValue, newValue) -> {
//      if (newValue) {
//        libraryDropdown.setVisible(!libraryComponentsContainer.isVisible());
//      }
//    });
//    projectDropdown.visibleProperty().addListener((observable, oldValue, newValue) -> {
//      if (newValue) {
//        projectComponentsContainer.setVisible(!projectDropdown.isVisible());
//      }
//    });

//    projectComponentsContainer.visibleProperty().addListener((observable, oldValue, newValue) -> {
//      if (newValue) {
//        projectDropdown.setVisible(!projectComponentsContainer.isVisible());
//      }
//    });
    setUpDropDownVisibleProperty(libraryDropdown,libraryComponentsContainer);
    setUpDropDownVisibleProperty(libraryComponentsContainer,libraryDropdown);
    setUpDropDownVisibleProperty(projectDropdown,projectComponentsContainer);
    setUpDropDownVisibleProperty(projectComponentsContainer,projectDropdown);
    projectDropdown.setVisible(false);
    libraryDropdown.setVisible(false);
    showLibraryDropDown.setOnAction(e -> setShowProjectAndLibraryDropDowns());
    showProjectDropDown.setOnAction(e -> setShowProjectAndLibraryDropDowns());
    setUpCloneName(program);
  }

  private void setShowProjectAndLibraryDropDowns() {
    libraryDropdown.setVisible(true);
    projectDropdown.setVisible(true);
  }

  private void setUpDropDownVisibleProperty(Node firstNode, Node secondNode){
    firstNode.visibleProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        secondNode.setVisible(!firstNode.isVisible());
      }
    });
  }

  private void setUpCloneName(Program originalProgram){
    Collection<Program> programs = projectService.getContent().getPrograms();
    String originalName = originalProgram.getName();
    String newName = "Copy of " + originalName;

    // Check if the program has been cloned previously
    boolean isCloned = programs.stream()
      .anyMatch(p -> p.getName().startsWith("Copy of " + originalName));

    if (isCloned) {
      // Find the highest numerical suffix and increment it by one
      int highestSuffix = 0;
      for (Program program : programs) {
        if (program.getName().matches("Copy of " + originalName + " \\d+")) {
          String[] parts = program.getName().split(" ");
          int suffix = Integer.parseInt(parts[parts.length - 1]);
          if (suffix > highestSuffix) {
            highestSuffix = suffix;
          }
        }
      }
      // Increment the suffix
      newName = "Copy of " + originalName + " " + (highestSuffix + 1);
    }
    programName.setText(newName);
  }

  public boolean isProgramNameAlreadyExists(String programName) {
    Collection<Program> programs = projectService.getContent().getPrograms();

    for (Program program : programs) {
      if (program.getName().toLowerCase().replaceAll("\\s","")
        .equals(programName.toLowerCase().replaceAll("\\s",""))) {
        return true; // Program with the same name already exists
      }
    }

    return false; // Program with the same name does not exist
  }


  @FXML
  protected void cloneProgram(Program program, ProjectService projectService, Stage stage) {
    cloneButton.setOnAction(event -> {
      String name = programName.getText();
      try {
        if (!isProgramNameAlreadyExists(name)){
          projectService.getContent().getPrograms();
          projectService.cloneProgram(program.getId(), program.getLibraryId(), programName.getText());
          stage.close();
        }else {
          showAlert("Program "+name+" already exists!!");
        }
      } catch (Exception e) {
        LOG.info("Error cloning program "+name+"!!");
      }
    });
  }

  public void showAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle("Warning");
    alert.initOwner(themeService.getMainScene().getWindow());
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
