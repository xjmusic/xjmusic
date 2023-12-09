// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.nexus.InputMode;
import io.xj.nexus.MacroMode;
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
public class ModalFabricationSettingsController extends ReadyAfterBootModalController {

  static final String FABRICATION_SERVICE_WINDOW_NAME = "Fabrication Settings";
  private final Resource modalFabricationSettingsFxml;
  private final ConfigurableApplicationContext ac;
  private final LabService labService;
  private final FabricationService fabricationService;
  private final ThemeService themeService;
  private final UIStateService uiStateService;

  @FXML
  TextField fieldInputTemplateKey;

  @FXML
  ChoiceBox<InputMode> choiceInputMode;

  @FXML
  ChoiceBox<MacroMode> choiceMacroMode;

  @FXML
  Label labelInputMode;

  @FXML
  Label labelMacroMode;

  @FXML
  TextField fieldContentStoragePathPrefix;

  @FXML
  Label labelContentStoragePathPrefix;

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

  public ModalFabricationSettingsController(
    @Value("classpath:/views/modal-fabrication-settings.fxml") Resource modalFabricationSettingsFxml,
    ConfigurableApplicationContext ac,
    LabService labService,
    FabricationService fabricationService,
    ThemeService themeService,
    UIStateService uiStateService
  ) {
    this.modalFabricationSettingsFxml = modalFabricationSettingsFxml;
    this.ac = ac;
    this.labService = labService;
    this.fabricationService = fabricationService;
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

    choiceMacroMode.valueProperty().bindBidirectional(fabricationService.macroModeProperty());
    choiceMacroMode.setItems(FXCollections.observableArrayList(MacroMode.values()));

    fieldInputTemplateKey.textProperty().bindBidirectional(fabricationService.inputTemplateKeyProperty());

    fieldContentStoragePathPrefix.textProperty().bindBidirectional(fabricationService.contentStoragePathPrefixProperty());

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

  @Override
  void launchModal() {
    doLaunchModal(ac, themeService, modalFabricationSettingsFxml, FABRICATION_SERVICE_WINDOW_NAME);
  }
}
