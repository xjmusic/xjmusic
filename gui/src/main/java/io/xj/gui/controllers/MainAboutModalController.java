// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.ProjectModalController;
import io.xj.gui.services.LabService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.SupportService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.services.VersionService;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class MainAboutModalController extends ProjectModalController {
  static final String ABOUT_WINDOW_NAME = "About";
  final LabService labService;
  private final VersionService versionService;
  private final SupportService supportService;

  @FXML
  public Button buttonClose;

  @FXML
  Label labelVersion;


  public MainAboutModalController(
    @Value("classpath:/views/main-about-modal.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    LabService labService,
    ThemeService themeService,
    VersionService versionService,
    UIStateService uiStateService,
    ProjectService projectService,
    SupportService supportService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.labService = labService;
    this.versionService = versionService;
    this.supportService = supportService;
  }

  @Override
  public void onStageReady() {
    labelVersion.textProperty().setValue("v" + versionService.getVersion());
    labelVersion.setOnMouseClicked(event -> {
      if (event.getClickCount()==5) {
        handleClose();
        uiStateService.isLabFeatureEnabledProperty().set(!uiStateService.isLabFeatureEnabledProperty().get());
        var state = uiStateService.isLabFeatureEnabledProperty().get() ? "enabled":"disabled";
        Platform.runLater(() -> projectService.showAlert(Alert.AlertType.INFORMATION, "Lab Features", "Lab features " + state, "Lab features are now " + state + "!"));
      }
    });
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
  void handleHyperlinkWebsite() {
    supportService.launchWebsiteInBrowser();
  }

  @Override
  public void launchModal() {
    createAndShowModal(ABOUT_WINDOW_NAME);
  }
}
