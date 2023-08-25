package io.xj.gui.controllers;


import io.xj.gui.services.LabService;
import io.xj.gui.services.LabStatus;
import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ModalLabConnectionController implements ReadyAfterBootController {
  private static final List<LabStatus> BUTTON_CONNECT_ACTIVE_IN_LAB_STATES = Arrays.asList(
    LabStatus.Ready,
    LabStatus.Authenticated,
    LabStatus.Unauthorized,
    LabStatus.Failed,
    LabStatus.Disconnected
  );
  private static final String BUTTON_DISCONNECT_TEXT = "Disconnect";
  private static final String BUTTON_CONNECT_TEXT = "Connect";
  private final HostServices hostServices;
  private final LabService labService;
  @FXML
  public Button connectButton;
  @FXML
  public Button buttonClose;
  @FXML
  public Button buttonConnect;
  @FXML
  TextField fieldLabUrl;
  @FXML
  PasswordField fieldLabAccessToken;
  @FXML
  Label labelStatus;

  public ModalLabConnectionController(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    LabService labService
  ) {
    this.hostServices = hostServices;
    this.labService = labService;
  }

  @Override
  public void onStageReady() {
    labService.statusProperty().addListener(new LabStatusChangeListener());
    fieldLabUrl.textProperty().bindBidirectional(labService.urlProperty());
    fieldLabAccessToken.textProperty().bindBidirectional(labService.accessTokenProperty());
    labelStatus.textProperty().bind(labService.statusProperty().asString());
  }

  @FXML
  void handleClose() {
    closeStage();
  }

  @FXML
  void handleLaunchLabPreferences() {
    hostServices.showDocument(labService.urlProperty().get() + "preferences");
  }

  @FXML
  void handleConnect() {
    if (labService.statusProperty().get() == LabStatus.Authenticated) {
      labService.disconnect();
    } else {
      labService.connect();
    }
  }

  class LabStatusChangeListener implements ChangeListener<LabStatus> {
    @Override
    public void changed(ObservableValue<? extends LabStatus> observable, LabStatus ignored, LabStatus status) {
      buttonConnect.setDisable(!BUTTON_CONNECT_ACTIVE_IN_LAB_STATES.contains(status));
      buttonConnect.setText(status == LabStatus.Authenticated ? BUTTON_DISCONNECT_TEXT : BUTTON_CONNECT_TEXT);
    }
  }

  void closeStage() {
    Stage stage = (Stage) connectButton.getScene().getWindow();
    stage.close();
  }
}
