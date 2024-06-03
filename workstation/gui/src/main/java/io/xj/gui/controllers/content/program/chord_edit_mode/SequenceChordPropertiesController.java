package io.xj.gui.controllers.content.program.chord_edit_mode;

import io.xj.gui.services.ProjectService;
import io.xj.gui.utils.UiUtils;
import io.xj.model.pojos.ProgramSequenceChord;
import io.xj.model.util.StringUtils;
import jakarta.annotation.Nullable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

import static io.xj.gui.services.UIStateService.INVALID_PSEUDO_CLASS;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequenceChordPropertiesController {
  private final ProjectService projectService;
  private @Nullable UUID chordId;
  private UUID programId;
  private UUID sequenceId;
  private StringBinding invalidPositionText;
  private BooleanBinding invalidPosition;

  @FXML
  VBox container;

  @FXML
  TextField nameField;

  @FXML
  TextField positionField;

  @FXML
  Label positionWarning;

  @FXML
  Button saveButton;

  public SequenceChordPropertiesController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  /**
   Set up the chord properties modal for editing an existing chord

   @param chordId to edit in the chord properties modal
   */
  public void setupEditing(UUID chordId) {
    var chord = projectService.getContent().getProgramSequenceChord(chordId)
      .orElseThrow(() -> new RuntimeException("Unable to find chord for editing modal"));
    this.chordId = chordId;
    this.programId = chord.getProgramId();
    this.sequenceId = chord.getProgramSequenceId();
    setup(chord.getName(), chord.getPosition());
  }

  /**
   Set up the chord properties modal for creating a new chord
   */
  public void setupCreating(UUID programSequenceId) {
    var sequence = projectService.getContent().getProgramSequence(programSequenceId)
      .orElseThrow(() -> new RuntimeException("Unable to find program sequence for chord creation modal"));
    chordId = null;
    programId = sequence.getProgramId();
    sequenceId = sequence.getId();
    setup("", null);
  }

  /**
   Set up the chord properties modal for either editing or creating a new chord
   */
  private void setup(String name, Double position) {
    var sequence = projectService.getContent().getProgramSequence(sequenceId)
      .orElseThrow(() -> new RuntimeException("Unable to find program sequence for chord properties modal"));

    // Set up the invalid position binding
    invalidPositionText = Bindings.createStringBinding(
      () -> {
        var newPosition = parsePosition(positionField.getText());
        if (Objects.isNull(newPosition)) return "";
        if (newPosition < 0) {
          return "Must be >= 1";
        } else if (newPosition >= sequence.getTotal()) {
          return "Must be < " + sequence.getTotal() + 1;
        } else if (projectService.getContent().getChordsOfSequence(sequenceId).stream()
          .anyMatch(chord -> chord.getPosition().equals(newPosition) && !Objects.equals(chordId, chord.getId()))) {
          return "Duplicate position";
        }
        return "";
      },
      positionField.textProperty()
    );
    invalidPosition = Bindings.createBooleanBinding(
      () -> !StringUtils.isNullOrEmpty(invalidPositionText.get()),
      invalidPositionText
    );
    invalidPosition.addListener((o, ov, invalid) -> positionField.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, invalid));
    positionWarning.textProperty().bind(invalidPositionText);
    positionWarning.visibleProperty().bind(invalidPosition);

    // Set up the fields
    nameField.setText(name);
    positionField.setText(formatPosition(position));

    // The form is dirty if any of the values differ from the original
    var dirty = Bindings.createBooleanBinding(
      () -> !nameField.getText().equals(name) ||
        !positionField.getText().equals(formatPosition(position)),
      nameField.textProperty(),
      positionField.textProperty()
    );

    // Disable the save button if the form is not dirty
    saveButton.disableProperty().bind(invalidPosition.or(dirty.not()).or(nameField.textProperty().isEmpty()).or(positionField.textProperty().isEmpty()));

    // Press enter when in any field to submit form
    UiUtils.onSpecialKeyPress(nameField, this::handleSavePressed, this::teardown);
    UiUtils.onSpecialKeyPress(positionField, this::handleSavePressed, this::teardown);
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
      if (invalidPosition.get()) return;

      var name = nameField.getText();
      var position = parsePosition(positionField.getText());
      if (StringUtils.isNullOrEmpty(name) || Objects.isNull(position)) return;

      ProgramSequenceChord chord;
      if (Objects.nonNull(chordId)) {
        chord = projectService.getContent().getProgramSequenceChord(chordId)
          .orElseThrow(() -> new RuntimeException("Unable to find chord for editing"));
      } else {
        chord = new ProgramSequenceChord();
        chord.setId(UUID.randomUUID());
        chord.setProgramId(programId);
        chord.setProgramSequenceId(sequenceId);
      }
      chord.setName(name);
      chord.setPosition(position);
      projectService.update(chord);
      teardown();

    } catch (Exception ex) {
      projectService.showErrorDialog("Could not save", "Could not save chord properties", String.format("Could not save chord properties because %s", ex.getMessage()));
    }
  }

  /**
   Format the position starting from 1 (not 0)

   @param position to format
   @return the formatted position
   */
  private String formatPosition(@Nullable Double position) {
    return Objects.nonNull(position) ? String.valueOf(position + 1) : "";
  }

  /**
   Parse the velocity from beats starting at 1 to starting at 0

   @param position to parse
   @return the parsed position value
   */
  private @Nullable Double parsePosition(String position) {
    return StringUtils.isNullOrEmpty(position) ? null : Double.parseDouble(position) - 1;
  }

}
