// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.WorkstationIcon;
import io.xj.gui.services.LabService;
import io.xj.gui.services.LabStatus;
import io.xj.gui.services.ThemeService;
import io.xj.hub.tables.pojos.User;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ModalLabConnectionController implements ReadyAfterBootController {
  static final List<LabStatus> BUTTON_CONNECT_ACTIVE_IN_LAB_STATES = Arrays.asList(
    LabStatus.Authenticated,
    LabStatus.Unauthorized,
    LabStatus.Failed,
    LabStatus.Offline
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

  @FXML
  Text textUserName;

  @FXML
  Text textUserEmail;

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
    buttonConnect.disableProperty().bind(Bindings.createBooleanBinding(() ->
        BUTTON_CONNECT_ACTIVE_IN_LAB_STATES.contains(labService.statusProperty().get()),
      labService.statusProperty()).not());

    buttonConnect.textProperty().bind(Bindings.createStringBinding(() ->
        labService.statusProperty().get() == LabStatus.Authenticated ? BUTTON_DISCONNECT_TEXT : BUTTON_CONNECT_TEXT,
      labService.statusProperty()));

    fieldLabUrl.textProperty().bindBidirectional(labService.baseUrlProperty());
    fieldLabAccessToken.textProperty().bindBidirectional(labService.accessTokenProperty());
    labelStatus.textProperty().bind(labService.statusProperty().asString());

    textUserName.textProperty().bind(Bindings.createStringBinding(() -> {
      User user = labService.authenticatedUserProperty().get();
      return Objects.nonNull(user) ? user.getName() : "";
    }, labService.authenticatedUserProperty()));

    textUserEmail.textProperty().bind(Bindings.createStringBinding(() -> {
      User user = labService.authenticatedUserProperty().get();
      return Objects.nonNull(user) ? user.getEmail() : "";
    }, labService.authenticatedUserProperty()));

    imageViewUserAvatar.imageProperty().bind(Bindings.createObjectBinding(() -> {
      User user = labService.authenticatedUserProperty().get();
      return Objects.nonNull(user) ? new Image(user.getAvatarUrl()) : null;
    }, labService.authenticatedUserProperty()));
  }

  @Override
  public void onStageClose() {
    // no op
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
      stage.initStyle(StageStyle.UTILITY);
      onStageReady();
      stage.showAndWait();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  void handleClose() {
    Stage stage = (Stage) buttonConnect.getScene().getWindow();
    stage.close();
    onStageClose();
  }

  @FXML
  void handleLaunchLabPreferences() {
    hostServices.showDocument(labService.baseUrlProperty().get() + "preferences");
  }

  @FXML
  void handleConnect() {
    if (labService.statusProperty().get() == LabStatus.Authenticated) {
      labService.disconnect();
    } else {
      labService.connect();
    }
  }
}
