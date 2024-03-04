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
  public Button newSequence;

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
    newSequence.setOnAction(e -> createNewSequence());
    cloneButton.setOnAction(e -> cloneSequence());
    deleteButton.setOnAction(e -> deleteSequence());
  }

  private void closeWindow() {
    stage.close();
  }

  private void createNewSequence() {
    try {
      ProgramSequence newProgramSequence = projectService.createProgramSequence(programId.get());
      uiStateService.currentProgramSequenceProperty().set(newProgramSequence);
      closeWindow();
    } catch (Exception e) {
      LOG.info("Failed to create new Sequence");
    }
  }

  private void deleteSequence() {
    var currentSequence = uiStateService.currentProgramSequenceProperty().get();
    if (Objects.isNull(currentSequence)) return;
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

  private void cloneSequence() {
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
