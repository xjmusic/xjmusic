package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.ProgramConfig;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.util.StringUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static io.xj.gui.utils.UiUtils.closeWindowOnClickingAway;

@Component
public class ProgramConfigController {
  private final ProjectService projectService;
  @FXML
  public TextArea configTextArea;
  @FXML
  public Button cancelButton;
  @FXML
  public HBox saveAndCancelButtonsContainer;
  @FXML
  public Button cancelConfigChanges;
  @FXML
  public Button saveConfigChanges;
  private String originalText = "";
  private final SimpleBooleanProperty visibleProperty = new SimpleBooleanProperty(false);
  private final Logger LOG = LoggerFactory.getLogger(ProgramConfigController.class);

  public ProgramConfigController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  protected void setup(Stage stage, UUID programId) {
    originalText = projectService.getContent().getProgram(programId)
      .orElseThrow(() -> new RuntimeException("Unable to find current program for config modal"))
      .getConfig();
    configTextArea.setText(originalText);
    cancelButton.setOnAction(e -> stage.close());
    cancelConfigChanges.setOnAction(e -> stage.close());
    saveConfigChanges.setOnAction(e -> {
      try {
        var newValue = new ProgramConfig(configTextArea.getText()).toString();
        projectService.update(Program.class, programId, "config", newValue);
        stage.close();
      } catch (Exception ex) {
        LOG.info("Failed to save config!\n{}", StringUtils.formatStackTrace(ex));
      }
    });
    // Bind the visibility of the Button and HBox to the BooleanProperty
    saveAndCancelButtonsContainer.visibleProperty().bind(visibleProperty);
    cancelButton.visibleProperty().bind(visibleProperty.not());
    closeWindowOnClickingAway(stage);
    detectConfigDataChanges();
  }

  private void detectConfigDataChanges() {
    // Add a listener to detect text changes
    configTextArea.textProperty().addListener(
      (o, ov, value) -> visibleProperty.set(!value.equals(originalText)));
  }

}
