// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.LabService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.VersionService;
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
  final LabService labService;
  private final VersionService versionService;

  @FXML
  public Button buttonClose;

  @FXML
  Label labelVersion;


  public ModalAboutController(
    @Value("classpath:/views/modal-about.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    LabService labService,
    ThemeService themeService,
    VersionService versionService
  ) {
    super(ac, themeService, fxml);
    this.labService = labService;
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
  public void launchModal() {
    createAndShowModal(ABOUT_WINDOW_NAME);
  }
}
