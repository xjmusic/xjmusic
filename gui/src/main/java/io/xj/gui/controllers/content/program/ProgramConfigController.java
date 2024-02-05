package io.xj.gui.controllers.content.program;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import static io.xj.gui.controllers.content.program.ProgramEditorController.closeWindowOnClickingAway;

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

  protected void setup(Stage stage, StringProperty config) {
    originalText = config.get();
    configTextArea.setText(config.get());
    cancelButton.setOnAction(e -> stage.close());
    cancelConfigChanges.setOnAction(e -> stage.close());
    saveConfigChanges.setOnAction(e -> {
      config.set(configTextArea.getText());
      stage.close();
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
