package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.tables.pojos.ProgramSequence;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

@Component
public class SequenceManagementController {
  private final Logger LOG = LoggerFactory.getLogger(SequenceManagementController.class);
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final ObjectProperty<UUID> programId = new SimpleObjectProperty<>();

  private Stage stage;

  @FXML
  public Button newSequenceButton;

  @FXML
  public Button deleteButton;

  @FXML
  public Button cloneButton;

  public SequenceManagementController(
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  public void setup(
    UUID programId,
    Stage stage
  ) {
    this.stage = stage;
    this.programId.set(programId);
  }

  private void closeWindow() {
    stage.close();
  }

  @FXML
  protected void handlePressNewSequence() {
    try {
      ProgramSequence newProgramSequence = projectService.createProgramSequence(programId.get());
      uiStateService.currentProgramSequenceProperty().set(newProgramSequence);
      closeWindow();
    } catch (Exception e) {
      LOG.info("Failed to create new Sequence");
    }
  }

  @FXML
  protected void handlePressDelete() {
    var currentSequence = uiStateService.currentProgramSequenceProperty().get();
    if (Objects.isNull(currentSequence)) return;
    if (!projectService.getContent().getBindingsOfSequence(currentSequence.getId()).isEmpty()) {
      projectService.showWarningAlert("Cannot Delete Sequence", "Must delete Sequence Bindings first!", "Cannot delete a sequence while it is still referenced by sequence bindings.");
      return;
    }
    if (!projectService.showConfirmationDialog("Delete Sequence?", "This action cannot be undone.", String.format("Are you sure you want to delete the Sequence \"%s\"?", currentSequence.getName())))
      return;
    try {
      projectService.deleteContent(currentSequence);
      var sequences = projectService.getContent().getSequencesOfProgram(programId.get()).stream()
        .sorted(Comparator.comparing(ProgramSequence::getName)).toList();
      if (sequences.size() > 0) {
        uiStateService.currentProgramSequenceProperty().set(sequences.get(0));
      } else {
        uiStateService.currentProgramSequenceProperty().set(null);
      }
      closeWindow();
    } catch (Exception e) {
      LOG.info("Failed to delete sequence " + currentSequence.getName());
    }
  }

  @FXML
  protected void handlePressClone() {
    var currentSequence = uiStateService.currentProgramSequenceProperty().get();
    if (Objects.isNull(currentSequence)) return;
    try {
      ProgramSequence clonedProgramSequence = projectService.cloneProgramSequence(currentSequence.getId(), "Clone of " + currentSequence.getName());
      uiStateService.currentProgramSequenceProperty().set(clonedProgramSequence);
      closeWindow();
    } catch (Exception e) {
      LOG.info("Failed to clone sequence ");
    }
  }
}
