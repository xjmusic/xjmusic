package io.xj.gui.controllers;


import io.xj.gui.WorkstationIcon;
import io.xj.gui.services.LabService;
import io.xj.gui.services.LabStatus;
import io.xj.gui.services.ThemeService;
import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class ModalLabConnectionController implements ReadyAfterBootController {
  static final List<LabStatus> BUTTON_CONNECT_ACTIVE_IN_LAB_STATES = Arrays.asList(
    LabStatus.Ready,
    LabStatus.Authenticated,
    LabStatus.Unauthorized,
    LabStatus.Failed,
    LabStatus.Disconnected
  );
  static final String CONNECT_TO_LAB_WINDOW_NAME = "Connect to Lab";
  static final String BUTTON_DISCONNECT_TEXT = "Disconnect";
  static final String BUTTON_CONNECT_TEXT = "Connect";
  final ConfigurableApplicationContext ac;
  final HostServices hostServices;
  final Resource modalLabConnectionFxml;
  final LabService labService;
  final ThemeService themeService;
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

  @FXML
  ImageView imageViewUserAvatar;

  public ModalLabConnectionController(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("classpath:/views/modal-lab-connection.fxml") Resource modalLabConnectionFxml,
    ConfigurableApplicationContext ac,
    LabService labService,
    ThemeService themeService
  ) {
    this.ac = ac;
    this.hostServices = hostServices;
    this.modalLabConnectionFxml = modalLabConnectionFxml;
    this.labService = labService;
    this.themeService = themeService;
  }

  @Override
  public void onStageReady() {
    labService.statusProperty().addListener(new LabStatusChangeListener());
    fieldLabUrl.textProperty().bindBidirectional(labService.urlProperty());
    fieldLabAccessToken.textProperty().bindBidirectional(labService.accessTokenProperty());
    labelStatus.textProperty().bind(labService.statusProperty().asString());
  }

  public void launchModal() {
    try {
      // Load the FXML file
      FXMLLoader loader = new FXMLLoader(modalLabConnectionFxml.getURL());
      loader.setControllerFactory(ac::getBean);

      // Create a new stage (window)
      Stage stage = new Stage();
      WorkstationIcon.setup(stage, CONNECT_TO_LAB_WINDOW_NAME);

      Scene scene = new Scene(loader.load());
      themeService.setup(scene);

      // Set the scene and show the stage
      stage.setScene(scene);
      stage.initModality(Modality.APPLICATION_MODAL); // make it a modal window
      onStageReady();
      stage.showAndWait();
    } catch (IOException e) {
      e.printStackTrace();
    }
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
    Stage stage = (Stage) buttonConnect.getScene().getWindow();
    stage.close();
  }
}
