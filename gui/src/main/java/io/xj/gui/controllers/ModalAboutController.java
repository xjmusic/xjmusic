// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.LabService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.VersionService;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ModalAboutController extends ReadyAfterBootModalController {
  static final String ABOUT_WINDOW_NAME = "About";
  final ConfigurableApplicationContext ac;
  final HostServices hostServices;
  final Resource modalAboutFxml;
  final LabService labService;
  final ThemeService themeService;
  private final VersionService versionService;

  @FXML
  public Button buttonClose;

  @FXML
  Label labelVersion;


  public ModalAboutController(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("classpath:/views/modal-about.fxml") Resource modalAboutFxml,
    ConfigurableApplicationContext ac,
    LabService labService,
    ThemeService themeService,
    VersionService versionService
  ) {
    this.ac = ac;
    this.hostServices = hostServices;
    this.modalAboutFxml = modalAboutFxml;
    this.labService = labService;
    this.themeService = themeService;
    this.versionService = versionService;
  }

  @Override
  public void onStageReady() {
    labelVersion.textProperty().setValue("v" + versionService.getVersion());
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

  @Override
  void launchModal() {
    doLaunchModal(ac, themeService, modalAboutFxml, ABOUT_WINDOW_NAME);
  }
}
