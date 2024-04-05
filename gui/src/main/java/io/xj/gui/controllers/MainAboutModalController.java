// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.ProjectModalController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.SupportService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
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
public class MainAboutModalController extends ProjectModalController {
  static final String ABOUT_WINDOW_NAME = "About";
  private final VersionService versionService;
  private final SupportService supportService;

  @FXML
  Button buttonClose;

  @FXML
  Label labelVersion;


  public MainAboutModalController(
    @Value("classpath:/views/main-about-modal.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    ThemeService themeService,
    VersionService versionService,
    UIStateService uiStateService,
    ProjectService projectService,
    SupportService supportService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.versionService = versionService;
    this.supportService = supportService;
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

  @FXML
  void handleHyperlinkWebsite() {
    supportService.launchWebsiteInBrowser();
  }

  @Override
  public void launchModal() {
    createAndShowModal(ABOUT_WINDOW_NAME);
  }
}
