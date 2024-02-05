package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.UUID;

public class SequenceManagement {
  @FXML
  public Button newSequence;
  @FXML
  public Button deleteButton;
  @FXML
  public Button cloneButton;

  public void sequenceManagementUIInitializer(ObjectProperty<UUID> sequenceId, ProjectService projectService, Stage stage) {
  }
}
