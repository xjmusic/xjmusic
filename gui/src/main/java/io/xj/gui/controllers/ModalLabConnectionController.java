package io.xj.gui.controllers;


import io.xj.gui.services.FabricationService;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.work.WorkConfiguration;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Service;

@Service
public class ModalLabConnectionController implements ReadyAfterBootController {
  private final FabricationService fabricationService;
  @FXML
  public Button saveButton;
  @FXML
  public Button cancelButton;
  @FXML
  protected TextField fieldInputTemplateKey;
  @FXML
  protected TextField fieldOutputPathPrefix;
  @FXML
  protected TextField fieldOutputSeconds;
  @FXML
  protected ChoiceBox<InputMode> choiceInputMode;
  @FXML
  protected ChoiceBox<OutputMode> choiceOutputMode;
  @FXML
  protected ChoiceBox<OutputFileMode> choiceOutputFileMode;

  @Override
  public void onStageReady() {
    choiceInputMode.getItems().setAll(InputMode.values());
    choiceOutputMode.getItems().setAll(OutputMode.values());
    choiceOutputFileMode.getItems().setAll(OutputFileMode.values());
  }

  public ModalLabConnectionController(
    FabricationService fabricationService
  ) {
    this.fabricationService = fabricationService;
  }

  @FXML
  private void handleCancel() {
    closeStage();
  }

  @FXML
  private void handleSave() {
    fabricationService.setConfiguration(new WorkConfiguration()
      .setInputMode(choiceInputMode.getValue())
      .setInputTemplateKey(fieldInputTemplateKey.getText())
      .setOutputFileMode(choiceOutputFileMode.getValue())
      .setOutputMode(choiceOutputMode.getValue())
      .setOutputPathPrefix(fieldOutputPathPrefix.getText())
      .setOutputSeconds(Integer.parseInt(fieldOutputSeconds.getText())));
    closeStage();
  }

  public void setConfiguration(WorkConfiguration configuration) {
    fieldOutputSeconds.setText(Integer.toString(configuration.getOutputSeconds()));
    fieldOutputPathPrefix.setText(configuration.getOutputPathPrefix());
    fieldInputTemplateKey.setText(configuration.getInputTemplateKey());
    choiceInputMode.setValue(configuration.getInputMode());
    choiceOutputMode.setValue(configuration.getOutputMode());
    choiceOutputFileMode.setValue(configuration.getOutputFileMode());
  }


  private void closeStage() {
    Stage stage = (Stage) saveButton.getScene().getWindow();
    stage.close();
  }
}
