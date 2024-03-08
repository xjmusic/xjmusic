package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.ProgramConfig;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.util.StringUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProgramConfigController {
  private final Logger LOG = LoggerFactory.getLogger(ProgramConfigController.class);
  private final ProjectService projectService;
  private String originalText = "";
  private final SimpleBooleanProperty visibleProperty = new SimpleBooleanProperty(false);

  @FXML
  AnchorPane container;

  @FXML
  TextArea configTextArea;

  @FXML
  Button cancelButton;

  @FXML
  HBox saveAndCancelButtonsContainer;

  @FXML
  Button cancelConfigChanges;

  @FXML
  Button saveConfigChanges;

  public ProgramConfigController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  /**
   Setup the config modal

   @param programId for which to open the config modal
   */
  public void setup(UUID programId) {
    originalText = projectService.getContent().getProgram(programId)
      .orElseThrow(() -> new RuntimeException("Unable to find current program for config modal"))
      .getConfig();
    configTextArea.setText(originalText);
    saveConfigChanges.setOnAction(e -> {
      try {
        var newValue = new ProgramConfig(configTextArea.getText()).toString();
        projectService.update(Program.class, programId, "config", newValue);
        teardown();
      } catch (Exception ex) {
        LOG.info("Failed to save config! {}\n{}", ex, StringUtils.formatStackTrace(ex));
      }
    });
    // Bind the visibility of the Button and HBox to the BooleanProperty
    saveAndCancelButtonsContainer.visibleProperty().bind(visibleProperty);
    cancelButton.visibleProperty().bind(visibleProperty.not());
    configTextArea.textProperty().addListener(
      (o, ov, value) -> visibleProperty.set(!value.equals(originalText)));
  }

  /**
   Close the modal
   */
  @FXML
  void teardown() {
    Stage stage = (Stage) container.getScene().getWindow();
    stage.close();
  }

}
