package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequence;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class SequenceManagement {
  @FXML
  public Button newSequence;
  @FXML
  public Button deleteButton;
  @FXML
  public Button cloneButton;

  private final ProjectService projectService;

  private ProgramEditorController programEditorController;

  private ProgramSequence programSequence;
  private Stage stage;
  private final Logger LOG = LoggerFactory.getLogger(SequenceManagement.class);

  public SequenceManagement(ProjectService projectService, ProgramEditorController programEditorController) {
    this.projectService = projectService;
    this.programEditorController = programEditorController;
  }

  public void setUp(ProgramSequence programSequence, Stage stage) {
    this.stage = stage;
    this.programSequence = programSequence;
    newSequence.setOnAction(e -> createNewSequence());
    cloneButton.setOnAction(e -> cloneSequence());
    deleteButton.setOnAction(e -> deleteSequence());
  }

  private void closeWindow() {
    stage.close();
  }

  private void createNewSequence() {
    try {
      ProgramSequence newProgramSequence = new ProgramSequence(UUID.randomUUID(), programEditorController.programId.get(), "New Sequence", programEditorController.sequenceKey.getText(),
        programEditorController.intensityChooser.getValue().floatValue(), programEditorController.sequenceTotalChooser.valueProperty().get().shortValue());
      projectService.getContent().put(newProgramSequence);
      programEditorController.programSequenceObservableList.add(newProgramSequence);
      programEditorController.activeProgramSequenceItem.set(newProgramSequence);
      updateSequenceUIproperties(newProgramSequence);
      closeWindow();
    } catch (Exception e) {
      LOG.info("Failed to create new Sequence");
    }
  }


  private void updateSequenceUIproperties(ProgramSequence programSequence){
    programEditorController.sequenceId.set(programSequence.getId());
    programEditorController.sequencePropertyName.set(programSequence.getName());
    programEditorController.sequenceTotalValueFactory.setValue(Integer.valueOf(programSequence.getTotal()));
    programEditorController.key.set(programSequence.getKey());
  }


  private void deleteSequence() {
    try {
      projectService.deleteContent(programSequence);
      programEditorController.programSequenceObservableList.remove(programSequence);
      if (programEditorController.programSequenceObservableList.size() > 0){
        programEditorController.activeProgramSequenceItem.set(programEditorController.programSequenceObservableList.get(0));
        updateSequenceUIproperties(programEditorController.activeProgramSequenceItem.get());
      }else programEditorController.activeProgramSequenceItem.set(null);
      closeWindow();
    } catch (Exception e) {
      LOG.info("Failed to delete sequence " + programSequence.getName());
    }
  }

  private void cloneSequence() {
    try {
      ProgramSequence clonedProgramSequence= projectService.cloneProgramSequence(programSequence.getId(), "Clone of " + programEditorController.sequencePropertyName.get());
      programEditorController.programSequenceObservableList.add(clonedProgramSequence);
      programEditorController.activeProgramSequenceItem.set(clonedProgramSequence);
      updateSequenceUIproperties(programEditorController.activeProgramSequenceItem.get());
      closeWindow();
    } catch (Exception e) {
      LOG.info("Failed to clone sequence ");
    }
  }
}
