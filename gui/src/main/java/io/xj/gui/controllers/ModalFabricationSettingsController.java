// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
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

import java.io.File;

@Service
public class ModalFabricationSettingsController extends ReadyAfterBootModalController {

  static final String FABRICATION_SERVICE_WINDOW_NAME = "Fabrication Settings";
  private final LabService labService;
  private final FabricationService fabricationService;
  private final UIStateService uiStateService;

  @FXML
  TextField fieldInputTemplateKey;

  @FXML
  ChoiceBox<InputMode> choiceInputMode;

  @FXML
  ChoiceBox<ControlMode> choiceControlMode;

  @FXML
  Label labelInputMode;

  @FXML
  Label labelControlMode;

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

  @FXML
  public Button buttonReset;

  public ModalFabricationSettingsController(
    @Value("classpath:/views/modal-fabrication-settings.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    LabService labService,
    FabricationService fabricationService,
    ThemeService themeService,
    UIStateService uiStateService
  ) {
    super(ac, themeService, fxml);
    this.labService = labService;
    this.fabricationService = fabricationService;
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

    fieldInputTemplateKey.textProperty().bindBidirectional(fabricationService.inputTemplateKeyProperty());

    fieldCraftAheadSeconds.textProperty().bindBidirectional(fabricationService.craftAheadSecondsProperty());
    fieldDubAheadSeconds.textProperty().bindBidirectional(fabricationService.dubAheadSecondsProperty());
    fieldMixerLengthSeconds.textProperty().bindBidirectional(fabricationService.mixerLengthSecondsProperty());
    fieldOutputFrameRate.textProperty().bindBidirectional(fabricationService.outputFrameRateProperty());
    fieldOutputChannels.textProperty().bindBidirectional(fabricationService.outputChannelsProperty());

    fieldTimelineSegmentViewLimit.textProperty().bindBidirectional(fabricationService.timelineSegmentViewLimitProperty());

    // Add slash to end of "file output path prefix" https://www.pivotaltracker.com/story/show/186555998
    fieldContentStoragePathPrefix.textProperty().bindBidirectional(fabricationService.contentStoragePathPrefixProperty());
    fieldContentStoragePathPrefix.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
      if (!isNowFocused) {
        addTrailingSlash(fieldContentStoragePathPrefix);
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
  void handleReset() {
    fabricationService.resetSettingsToDefaults();
  }

  @Override
  public void launchModal() {
    createAndShowModal(FABRICATION_SERVICE_WINDOW_NAME);
  }

  /**
   Add slash to end of "file output path prefix"
   https://www.pivotaltracker.com/story/show/186555998

   @param textField in which to add a trailing slash
   */
  private void addTrailingSlash(TextField textField) {
    String text = textField.getText();
    if (!text.endsWith(File.separator)) {
      textField.setText(text + File.separator);
    }
  }
}
