package io.xj.gui.controllers;


import io.xj.gui.services.HubService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ModalLabConnectionController implements ReadyAfterBootController {
  private final String defaultHubBaseUrl;
  private final HubService hubService;
  @FXML
  public Button connectButton;
  @FXML
  public Button cancelButton;
  @FXML
  TextField fieldLabUrl;
  @FXML
  TextField fieldAccessToken;

  public ModalLabConnectionController(
    @Value("${hub.base.url}") String defaultHubBaseUrl,
    HubService hubService
  ) {
    this.defaultHubBaseUrl = defaultHubBaseUrl;
    this.hubService = hubService;
  }

  @Override
  public void onStageReady() {
    fieldLabUrl.setText(defaultHubBaseUrl);
  }

  @FXML
  private void handleCancel() {
    closeStage();
  }

  @FXML
  private void handleConnect() {
    closeStage();
  }

  private void closeStage() {
    Stage stage = (Stage) connectButton.getScene().getWindow();
    stage.close();
  }
}
