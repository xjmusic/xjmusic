package io.xj.gui.controllers.content.program;

import io.xj.hub.ProgramConfig;
import io.xj.hub.util.ValueException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static io.xj.gui.utils.WindowUtils.closeWindowOnClickingAway;

@Component
public class ProgramConfigController {
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
  private final Logger LOG= LoggerFactory.getLogger(ProgramConfigController.class);
  private final ProgramEditorController programEditorController;

  public ProgramConfigController(ProgramEditorController programEditorController){
    this.programEditorController=programEditorController;
  }

  protected void setUp(Stage stage) {
    originalText = programEditorController.getConfig();
    configTextArea.setText(originalText);
    cancelButton.setOnAction(e -> stage.close());
    cancelConfigChanges.setOnAction(e -> stage.close());
    saveConfigChanges.setOnAction(e -> {
      try {
        programEditorController.setConfig(new ProgramConfig(configTextArea.getText()).toString());
        stage.close();
      } catch (ValueException ex) {
        LOG.info("Failed to save config");
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
    configTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
      visibleProperty.set(!newValue.equals(originalText));
    });
  }

}
