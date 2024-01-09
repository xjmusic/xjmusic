// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.impl;

import io.xj.gui.controllers.MainLabAuthenticationModalController;
import io.xj.gui.controllers.impl.ReadyAfterBootModalControllerImpl;
import io.xj.gui.services.LabService;
import io.xj.gui.services.LabStatus;
import io.xj.gui.services.ThemeService;
import io.xj.hub.tables.pojos.User;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class MainLabAuthenticationModalControllerImpl extends ReadyAfterBootModalControllerImpl implements MainLabAuthenticationModalController {
  static final List<LabStatus> BUTTON_CONNECT_ACTIVE_IN_LAB_STATES = Arrays.asList(
    LabStatus.Authenticated,
    LabStatus.Unauthorized,
    LabStatus.Failed,
    LabStatus.Offline
  );
  static final String CONNECT_TO_LAB_WINDOW_NAME = "Lab Authentication";
  static final String BUTTON_DISCONNECT_TEXT = "Disconnect";
  static final String BUTTON_CONNECT_TEXT = "Connect";
  private static final Integer USER_AVATAR_SIZE = 120;
  final ConfigurableApplicationContext ac;
  final Resource modalLabAuthenticationFxml;
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

  public MainLabAuthenticationModalControllerImpl(
    @Value("classpath:/views/main-lab-authentication-modal.fxml") Resource modalLabAuthenticationFxml,
    ConfigurableApplicationContext ac,
    LabService labService,
    ThemeService themeService
  ) {
    this.ac = ac;
    this.modalLabAuthenticationFxml = modalLabAuthenticationFxml;
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
      return Objects.nonNull(user) ? new Image(computeHighResImageUrl(user)) : null;
    }, labService.authenticatedUserProperty()));
  }

  private String computeHighResImageUrl(User user) {
    return user.getAvatarUrl()
      .replace("=s50", String.format("=s%d", USER_AVATAR_SIZE))
      .replace("/s50", String.format("/s%d", USER_AVATAR_SIZE))
      .replace("sz=50", String.format("sz=%d", USER_AVATAR_SIZE));
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @FXML
  void handleClose() {
    Stage stage = (Stage) buttonClose.getScene().getWindow();
    stage.close();
    onStageClose();
  }

  @FXML
  void handleLaunchLabPreferences() {
    labService.launchPreferencesInBrowser();
  }

  @FXML
  void handleConnect() {
    if (labService.statusProperty().get() == LabStatus.Authenticated) {
      labService.disconnect();
    } else {
      labService.connect();
    }
  }

  @Override
  public void launchModal() {
    doLaunchModal(ac, themeService, modalLabAuthenticationFxml, CONNECT_TO_LAB_WINDOW_NAME);
  }
}
