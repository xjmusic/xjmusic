package io.xj.gui.controllers.content.program.chord_edit_mode;

import io.xj.gui.services.ProjectService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChordPropertiesController {
  private final ProjectService projectService;
  private final ObjectProperty<ProgramSequenceChord> chord = new SimpleObjectProperty<>();

  @FXML
  VBox container;

  @FXML
  TextField fieldName;

  @FXML
  TextField fieldPosition;

  @FXML
  Button saveButton;

  public ChordPropertiesController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  /**
   Set up the chord properties modal

   @param chordId for which to open the chord properties modal
   */
  public void setup(UUID chordId) {
    chord.set(projectService.getContent().getProgramSequenceChord(chordId)
      .orElseThrow(() -> new RuntimeException("Unable to find current program for config modal")));

    // Set up the fields
    fieldName.setText(chord.get().getName());
    fieldPosition.setText(formatPosition(chord.get().getPosition()));

    // Press enter when in any field to submit form
    UiUtils.onSpecialKeyPress(fieldName, this::handleSavePressed, this::teardown);
    UiUtils.onSpecialKeyPress(fieldPosition, this::handleSavePressed, this::teardown);

    // The form is dirty if any of the values differ from the original
    var dirty = Bindings.createBooleanBinding(
      () -> !fieldName.getText().equals(chord.get().getName()) ||
        !fieldPosition.getText().equals(formatPosition(chord.get().getPosition())),
      fieldName.textProperty(),
      fieldPosition.textProperty()
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
    {
      try {
        chord.get().setName(fieldName.getText());
        chord.get().setPosition(parsePosition(fieldPosition.getText()));
        projectService.update(chord.get());
        teardown();
      } catch (Exception ex) {
        projectService.showErrorDialog("Could not save", "Could not save chord properties", String.format("Could not save chord properties because %s", ex.getMessage()));
      }
    }
  }

  /**
   Format the position starting from 1 (not 0)

   @param position to format
   @return the formatted position
   */
  private String formatPosition(Double position) {
    return String.valueOf(position + 1);
  }

  /**
   Parse the velocity from beats starting at 1 to starting at 0

   @param position to parse
   @return the parsed position value
   */
  private Double parsePosition(String position) {
    return Double.parseDouble(position) - 1;
  }

}
