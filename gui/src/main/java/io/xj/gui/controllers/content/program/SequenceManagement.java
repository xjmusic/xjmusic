package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequence;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SequenceManagement {
  @FXML
  public Button newSequence;
  @FXML
  public Button deleteButton;
  @FXML
  public Button cloneButton;

  private final ProjectService projectService;

  private final ProgramEditorController programEditorController;

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
      ProgramSequence newProgramSequence = projectService.createProgramSequence(programEditorController.getProgramId());
      programEditorController.programSequenceObservableList.add(newProgramSequence);
      programEditorController.activeProgramSequenceItem.set(newProgramSequence);
      updateSequenceUI(newProgramSequence);
      closeWindow();
    } catch (Exception e) {
      LOG.info("Failed to create new Sequence");
    }
  }


  private void updateSequenceUI(ProgramSequence programSequence) {
    programEditorController.setSequenceId(programSequence.getId());
    programEditorController.sequencePropertyName.set(programSequence.getName());
    programEditorController.setSequenceTotal(Integer.valueOf(programSequence.getTotal()));
  }


  private void deleteSequence() {
    try {
      projectService.deleteContent(programSequence);
      programEditorController.programSequenceObservableList.remove(programSequence);
      if (programEditorController.programSequenceObservableList.size() > 0) {
        programEditorController.activeProgramSequenceItem.set(programEditorController.programSequenceObservableList.get(0));
        updateSequenceUI(programEditorController.activeProgramSequenceItem.get());
      } else programEditorController.activeProgramSequenceItem.set(null);
      closeWindow();
    } catch (Exception e) {
      LOG.info("Failed to delete sequence " + programSequence.getName());
    }
  }

  private void cloneSequence() {
    try {
      ProgramSequence clonedProgramSequence = projectService.cloneProgramSequence(programSequence.getId(), "Clone of " + programEditorController.sequencePropertyName.get());
      programEditorController.programSequenceObservableList.add(clonedProgramSequence);
      programEditorController.activeProgramSequenceItem.set(clonedProgramSequence);
      updateSequenceUI(programEditorController.activeProgramSequenceItem.get());
      closeWindow();
    } catch (Exception e) {
      LOG.info("Failed to clone sequence ");
    }
  }
}
