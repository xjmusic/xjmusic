// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.FabricationStatus;
import io.xj.gui.services.LabService;
import io.xj.gui.services.LabStatus;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MainPaneTopController extends VBox implements ReadyAfterBootController {
  static final int FABRICATION_CONFIG_VIEW_HEIGHT = 240;
  static final List<FabricationStatus> BUTTON_ACTION_ACTIVE_IN_FABRICATION_STATES = Arrays.asList(
    FabricationStatus.Standby,
    FabricationStatus.Active,
    FabricationStatus.Cancelled,
    FabricationStatus.Done,
    FabricationStatus.Failed
  );
  final static String BUTTON_TEXT_START = "Start";
  final static String BUTTON_TEXT_STOP = "Stop";
  final static String BUTTON_TEXT_RESET = "Reset";
  final FabricationService fabricationService;
  final ModalLabAuthenticationController modalLabAuthenticationController;
  final LabService labService;
  final BooleanProperty configVisible = new SimpleBooleanProperty(false);

  @FXML
  Button buttonAction;

  @FXML
  Label labelFabricationStatus;

  @FXML
  ToggleButton toggleShowConfig;

  @FXML
  VBox fabricationConfigView;

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
  public Button buttonLab;

  @FXML
  public Label labelLabStatus;

  public MainPaneTopController(
    FabricationService fabricationService,
    ModalLabAuthenticationController modalLabAuthenticationController,
    LabService labService
  ) {
    this.fabricationService = fabricationService;
    this.modalLabAuthenticationController = modalLabAuthenticationController;
    this.labService = labService;
  }

  @Override
  public void onStageReady() {
    buttonAction.disableProperty().bind(Bindings.createBooleanBinding(this::isActionButtonActive, fabricationService.statusProperty()).not());
    buttonAction.textProperty().bind(Bindings.createStringBinding(this::computeActionButtonText, fabricationService.statusProperty()));
    fabricationService.statusProperty().addListener(this::handleFabricationStatusChange);

    // Input mode is locked in PRODUCTION unless we are connected to a Lab
    choiceInputMode.valueProperty().bindBidirectional(fabricationService.inputModeProperty());
    choiceInputMode.disableProperty().bind(labService.statusProperty().isEqualTo(LabStatus.Authenticated).not());
    labelInputMode.disableProperty().bind(labService.statusProperty().isEqualTo(LabStatus.Authenticated).not());
    choiceInputMode.setItems(FXCollections.observableArrayList(InputMode.PRODUCTION));
    labService.statusProperty().addListener(this::handleLabStatusChange);
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

    labelFabricationStatus.textProperty().bind(fabricationService.statusProperty().map(Enum::toString).map((status) -> String.format("Fabrication %s", status)));

    toggleShowConfig.setSelected(configVisible.get());

    labelLabStatus.textProperty().bind(labService.statusProperty().map(Enum::toString));

    updateConfigVisibility();
  }

  @Override
  public void onStageClose() {
    fabricationService.cancel();
  }

  @FXML
  protected void onButtonActionPress() {
    switch (fabricationService.statusProperty().get()) {
      case Standby -> start();
      case Active -> stop();
      case Cancelled, Done, Failed -> reset();
    }
  }

  public void start() {
    fabricationService.start();
  }

  public void stop() {
    fabricationService.cancel();
  }

  public void reset() {
    fabricationService.reset();
  }

  @FXML
  public void toggleShowConfig(ActionEvent ignored) {
    configVisible.set(toggleShowConfig.isSelected());
    updateConfigVisibility();
  }

  @FXML
  public void handleButtonLabPressed(ActionEvent ignored) {
    if (labService.isAuthenticated()) {
      labService.launchInBrowser();
    } else {
      modalLabAuthenticationController.launchModal();
    }
  }

  void updateConfigVisibility() {
    if (configVisible.get()) {
      fabricationConfigView.setVisible(true);
      fabricationConfigView.setMinHeight(FABRICATION_CONFIG_VIEW_HEIGHT);
      fabricationConfigView.setPrefHeight(FABRICATION_CONFIG_VIEW_HEIGHT);
      fabricationConfigView.setMaxHeight(FABRICATION_CONFIG_VIEW_HEIGHT);
    } else {
      fabricationConfigView.setVisible(false);
      fabricationConfigView.setMinHeight(0);
      fabricationConfigView.setPrefHeight(0);
      fabricationConfigView.setMaxHeight(0);
    }
  }

  private void handleFabricationStatusChange(ObservableValue<? extends FabricationStatus> observable, FabricationStatus oldValue, FabricationStatus newValue) {
    switch (newValue) {
      case Standby, Failed, Done, Cancelled -> buttonAction.getStyleClass().remove("button-active");
      case Starting, Active -> buttonAction.getStyleClass().add("button-active");
    }
  }

  private String computeActionButtonText() {
    return switch (fabricationService.statusProperty().get()) {
      case Starting, Standby -> BUTTON_TEXT_START;
      case Active -> BUTTON_TEXT_STOP;
      case Cancelled, Failed, Done -> BUTTON_TEXT_RESET;
    };
  }

  private Boolean isActionButtonActive() {
    return BUTTON_ACTION_ACTIVE_IN_FABRICATION_STATES.contains(fabricationService.statusProperty().get());
  }

  private void handleLabStatusChange(ObservableValue<? extends LabStatus> observable, LabStatus oldValue, LabStatus newValue) {
    if (newValue == LabStatus.Authenticated) {
      if (choiceInputMode.getItems().size() == 1) {
        choiceInputMode.getItems().add(InputMode.PREVIEW);
      }
    } else {
      if (choiceInputMode.getItems().size() == 2) {
        choiceInputMode.getItems().remove(1);
      }
      choiceInputMode.setValue(InputMode.PRODUCTION);
    }
  }
}
