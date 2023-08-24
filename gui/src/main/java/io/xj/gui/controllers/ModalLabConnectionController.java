package io.xj.gui.controllers;


import io.xj.gui.services.HubService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ModalLabConnectionController implements ReadyAfterBootController {
  private final HubService hubService;
  @FXML
  public Button connectButton;
  @FXML
  public Button closeButton;
  @FXML
  TextField fieldHubUrl;
  @FXML
  PasswordField fieldHubAccessToken;
  @FXML
  Label labelStatus;

  public ModalLabConnectionController(
    HubService hubService
  ) {
    this.hubService = hubService;
  }

  @Override
  public void onStageReady() {
    fieldHubUrl.textProperty().bindBidirectional(hubService.hubUrlProperty());
    fieldHubAccessToken.textProperty().bindBidirectional(hubService.hubAccessTokenProperty());
    labelStatus.textProperty().bind(hubService.statusProperty().asString());
  }

  @FXML
  private void handleClose() {
    closeStage();
  }

  @FXML
  private void handleConnect() {
    // todo handle connect
  }

  private void closeStage() {
    Stage stage = (Stage) connectButton.getScene().getWindow();
    stage.close();
  }
}
