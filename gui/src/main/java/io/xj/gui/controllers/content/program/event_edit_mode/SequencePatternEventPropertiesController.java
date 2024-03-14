package io.xj.gui.controllers.content.program.event_edit_mode;

import io.xj.gui.services.ProjectService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
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
public class SequencePatternEventPropertiesController {
  private final ProjectService projectService;
  private final ObjectProperty<ProgramSequencePatternEvent> event = new SimpleObjectProperty<>();

  @FXML
  VBox container;

  @FXML
  TextField fieldTones;

  @FXML
  TextField fieldVelocity;

  @FXML
  TextField fieldPosition;

  @FXML
  TextField fieldDuration;

  @FXML
  Button saveButton;

  public SequencePatternEventPropertiesController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  /**
   Set up the event properties modal

   @param eventId for which to open the event properties modal
   */
  public void setup(UUID eventId) {
    event.set(projectService.getContent().getProgramSequencePatternEvent(eventId)
      .orElseThrow(() -> new RuntimeException("Unable to find current program for config modal")));

    // Set up the fields
    fieldTones.setText(event.get().getTones());
    fieldVelocity.setText(formatVelocity(event.get().getVelocity()));
    fieldPosition.setText(formatPosition(event.get().getPosition()));
    fieldDuration.setText(event.get().getDuration().toString());

    // Press enter when in any field to submit form
    UiUtils.onSpecialKeyPress(fieldTones, this::handleSavePressed, this::teardown);
    UiUtils.onSpecialKeyPress(fieldVelocity, this::handleSavePressed, this::teardown);
    UiUtils.onSpecialKeyPress(fieldPosition, this::handleSavePressed, this::teardown);
    UiUtils.onSpecialKeyPress(fieldDuration, this::handleSavePressed, this::teardown);

    // The form is dirty if any of the values differ from the original
    var dirty = Bindings.createBooleanBinding(
      () -> !fieldTones.getText().equals(event.get().getTones()) ||
        !fieldVelocity.getText().equals(formatVelocity(event.get().getVelocity())) ||
        !fieldPosition.getText().equals(formatPosition(event.get().getPosition())) ||
        !fieldDuration.getText().equals(event.get().getDuration().toString()),
      fieldTones.textProperty(),
      fieldVelocity.textProperty(),
      fieldPosition.textProperty(),
      fieldDuration.textProperty()
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
        event.get().setTones(fieldTones.getText());
        event.get().setVelocity(parseVelocity(fieldVelocity.getText()));
        event.get().setPosition(parsePosition(fieldPosition.getText()));
        event.get().setDuration(Float.parseFloat(fieldDuration.getText()));
        projectService.update(event.get());
        teardown();
      } catch (Exception ex) {
        projectService.showErrorDialog("Could not save", "Could not save event properties", String.format("Could not save event properties because %s", ex.getMessage()));
      }
    }
  }

  /**
   Format the position starting from 1 (not 0)

   @param position to format
   @return the formatted position
   */
  private String formatPosition(Float position) {
    return String.valueOf(position + 1);
  }

  /**
   Format the velocity as a percentage

   @param velocity to format
   @return the formatted velocity
   */
  private String formatVelocity(Float velocity) {
    return String.format("%d%%", Math.round(velocity * 100));
  }

  /**
   Parse the velocity from beats starting at 1 to starting at 0

   @param position to parse
   @return the parsed position value
   */
  private Float parsePosition(String position) {
    return Float.parseFloat(position) - 1;
  }

  /**
   Parse the velocity from a percentage to a float

   @param velocity to parse
   @return the parsed velocity value
   */
  private Float parseVelocity(String velocity) {
    return Float.parseFloat(velocity.replace("%", "")) / 100;
  }

}
