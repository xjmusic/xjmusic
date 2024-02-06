package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
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

  public SequenceManagement(ProjectService projectService){
    this.projectService=projectService;
  }

  public void setUp(ObjectProperty<UUID> sequenceId, Stage stage) {

  }
}
