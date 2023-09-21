// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.LabStatus;
import io.xj.gui.services.ThemeService;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
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

  @FXML
  TextField fieldInputTemplateKey;

  @FXML
  ChoiceBox<InputMode> choiceInputMode;

  @FXML
  ChoiceBox<OutputMode> choiceOutputMode;

  @FXML
  ChoiceBox<OutputFileMode> choiceOutputFileMode;

  @FXML
  Label labelInputMode;

  @FXML
  Label labelOutputFileMode;

  @FXML
  TextField fieldOutputSeconds;

  @FXML
  Label labelOutputSeconds;

  @FXML
  TextField fieldOutputPathPrefix;

  @FXML
  Label labelOutputPathPrefix;

  @FXML
  TextField fieldCraftAheadSeconds;

  @FXML
  TextField fieldDubAheadSeconds;

  @FXML
  TextField fieldShipAheadSeconds;

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
    ThemeService themeService
  ) {
    this.modalFabricationSettingsFxml = modalFabricationSettingsFxml;
    this.ac = ac;
    this.labService = labService;
    this.fabricationService = fabricationService;
    this.themeService = themeService;
  }

  @Override
  public void onStageReady() {
    choiceInputMode.valueProperty().bindBidirectional(fabricationService.inputModeProperty());
    choiceInputMode.disableProperty().bind(labService.statusProperty().isEqualTo(LabStatus.Authenticated).not());
    labelInputMode.disableProperty().bind(labService.statusProperty().isEqualTo(LabStatus.Authenticated).not());
    fieldInputTemplateKey.textProperty().bindBidirectional(fabricationService.inputTemplateKeyProperty());

    choiceOutputMode.getItems().setAll(OutputMode.values());
    choiceOutputMode.valueProperty().bindBidirectional(fabricationService.outputModeProperty());

    choiceOutputFileMode.getItems().setAll(OutputFileMode.values());
    choiceOutputFileMode.valueProperty().bindBidirectional(fabricationService.outputFileModeProperty());
    choiceOutputFileMode.disableProperty().bind(fabricationService.outputModeProperty().isEqualTo(OutputMode.FILE).not());
    labelOutputFileMode.disableProperty().bind(fabricationService.outputModeProperty().isEqualTo(OutputMode.FILE).not());

    fieldOutputSeconds.textProperty().bindBidirectional(fabricationService.outputSecondsProperty());
    fieldOutputSeconds.disableProperty().bind(fabricationService.outputModeProperty().isEqualTo(OutputMode.FILE).not());
    labelOutputSeconds.disableProperty().bind(fabricationService.outputModeProperty().isEqualTo(OutputMode.FILE).not());

    fieldOutputPathPrefix.textProperty().bindBidirectional(fabricationService.outputPathPrefixProperty());
    fieldOutputPathPrefix.disableProperty().bind(fabricationService.outputModeProperty().isEqualTo(OutputMode.FILE).not());
    labelOutputPathPrefix.disableProperty().bind(fabricationService.outputModeProperty().isEqualTo(OutputMode.FILE).not());

    fieldCraftAheadSeconds.textProperty().bindBidirectional(fabricationService.craftAheadSecondsProperty());
    fieldDubAheadSeconds.textProperty().bindBidirectional(fabricationService.dubAheadSecondsProperty());
    fieldShipAheadSeconds.textProperty().bindBidirectional(fabricationService.shipAheadSecondsProperty());
    fieldOutputFrameRate.textProperty().bindBidirectional(fabricationService.outputFrameRateProperty());
    fieldOutputChannels.textProperty().bindBidirectional(fabricationService.outputChannelsProperty());

    fieldTimelineSegmentViewLimit.textProperty().bindBidirectional(fabricationService.timelineSegmentViewLimitProperty());

    // Input mode is locked in PRODUCTION unless we are connected to a Lab
    if (labService.isAuthenticated()) {
      choiceInputMode.setItems(FXCollections.observableArrayList(InputMode.PREVIEW, InputMode.PRODUCTION));
    } else {
      choiceInputMode.setItems(FXCollections.observableArrayList(InputMode.PRODUCTION));
      choiceInputMode.setValue(InputMode.PRODUCTION);
    }
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
