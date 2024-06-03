package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.gui.utils.UiUtils;
import io.xj.model.ProgramConfig;
import io.xj.model.pojos.Program;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProgramConfigController {
  private final ProjectService projectService;
  private final ObjectProperty<Program> program = new SimpleObjectProperty<>();

  @FXML
  VBox container;

  @FXML
  TextArea configField;

  @FXML
  Button saveButton;

  public ProgramConfigController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  /**
   Set up the config modal

   @param programId for which to open the config modal
   */
  public void setup(UUID programId) {
    program.set(projectService.getContent().getProgram(programId)
      .orElseThrow(() -> new RuntimeException("Unable to find current program for config modal")));

    // Set up field
    configField.setText(program.get().getConfig());

    // Close window on escape pressed
    UiUtils.onSpecialKeyPress(configField, null, this::teardown);

    // The form is dirty if any of the values differ from the original
    var dirty = Bindings.createBooleanBinding(
      () -> !configField.getText().equals(program.get().getConfig()),
      configField.textProperty()
    );

    // Disable the save button if the form is not dirty
    saveButton.disableProperty().bind(dirty.not());
  }

  /**
   Close the modal
   */
  @FXML
  void teardown() {
    Stage stage = (Stage) container.getScene().getWindow();
    stage.close();
  }

  @FXML
  void handleSavePressed() {
    try {
      var newValue = new ProgramConfig(configField.getText()).toString();
      projectService.update(Program.class, program.get().getId(), "config", newValue);
      teardown();
    } catch (Exception ex) {
      projectService.showErrorDialog("Could not save", "Could not save event properties", String.format("Could not save event properties because %s", ex.getMessage()));
    }
  }

}
