// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.ControlMode;
import io.xj.nexus.InputMode;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class FabricationSettingsModalController extends ReadyAfterBootModalController {

  static final String FABRICATION_SERVICE_WINDOW_NAME = "Fabrication Settings";
  private final Resource fabricationSettingsModalFxml;
  private final ConfigurableApplicationContext ac;
  private final LabService labService;
  private final FabricationService fabricationService;
  private final ProjectService projectService;
  private final ThemeService themeService;
  private final UIStateService uiStateService;

  @FXML
  TextField fieldInputTemplateKey;

  @FXML
  ChoiceBox<InputMode> choiceInputMode;

  @FXML
  ChoiceBox<ControlMode> choiceControlMode;

  @FXML
  ChoiceBox<Template> choiceTemplate;

  @FXML
  Label labelInputMode;

  @FXML
  Label labelControlMode;

  @FXML
  TextField fieldCraftAheadSeconds;

  @FXML
  TextField fieldDubAheadSeconds;

  @FXML
  TextField fieldMixerLengthSeconds;

  @FXML
  TextField fieldOutputChannels;

  @FXML
  TextField fieldOutputFrameRate;

  @FXML
  TextField fieldTimelineSegmentViewLimit;

  @FXML
  public Button buttonClose;

  @FXML
  public Button buttonReset;

  public FabricationSettingsModalController(
    @Value("classpath:/views/fabrication-settings-modal.fxml") Resource fabricationSettingsModalFxml,
    ConfigurableApplicationContext ac,
    LabService labService,
    FabricationService fabricationService,
    ProjectService projectService, ThemeService themeService,
    UIStateService uiStateService
  ) {
    this.fabricationSettingsModalFxml = fabricationSettingsModalFxml;
    this.ac = ac;
    this.labService = labService;
    this.fabricationService = fabricationService;
    this.projectService = projectService;
    this.themeService = themeService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    choiceInputMode.valueProperty().bindBidirectional(fabricationService.inputModeProperty());
    choiceInputMode.disableProperty().bind(uiStateService.isInputModeDisabledProperty());
    labelInputMode.disableProperty().bind(uiStateService.isInputModeDisabledProperty());

    // Input mode is locked in PRODUCTION unless we are connected to a Lab
    if (labService.isAuthenticated()) {
      choiceInputMode.getItems().setAll(FXCollections.observableArrayList(InputMode.values()));
    } else {
      choiceInputMode.getItems().setAll(FXCollections.observableArrayList(InputMode.PRODUCTION));
      fabricationService.inputModeProperty().set(InputMode.PRODUCTION);
    }

    choiceControlMode.valueProperty().bindBidirectional(fabricationService.controlModeProperty());
    choiceControlMode.setItems(FXCollections.observableArrayList(ControlMode.values()));

    choiceTemplate.valueProperty().bindBidirectional(fabricationService.inputTemplateProperty());
    choiceTemplate.setItems(FXCollections.observableArrayList(projectService.getContent().getTemplates()));

    fieldCraftAheadSeconds.textProperty().bindBidirectional(fabricationService.craftAheadSecondsProperty());
    fieldDubAheadSeconds.textProperty().bindBidirectional(fabricationService.dubAheadSecondsProperty());
    fieldMixerLengthSeconds.textProperty().bindBidirectional(fabricationService.mixerLengthSecondsProperty());
    fieldOutputFrameRate.textProperty().bindBidirectional(fabricationService.outputFrameRateProperty());
    fieldOutputChannels.textProperty().bindBidirectional(fabricationService.outputChannelsProperty());

    fieldTimelineSegmentViewLimit.textProperty().bindBidirectional(fabricationService.timelineSegmentViewLimitProperty());
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
  void handleReset() {
    fabricationService.resetSettingsToDefaults();
  }

  @Override
  void launchModal() {
    createAndShowModal(ac, themeService, fabricationSettingsModalFxml, FABRICATION_SERVICE_WINDOW_NAME);
  }

}
