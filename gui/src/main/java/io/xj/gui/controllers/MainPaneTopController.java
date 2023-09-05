// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.FabricationStatus;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MainPaneTopController extends VBox implements ReadyAfterBootController {
  static final int FABRICATION_CONFIG_VIEW_HEIGHT = 220;
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
  final BooleanProperty configVisible = new SimpleBooleanProperty(false);

  @FXML
  protected Button buttonAction;

  @FXML
  public Label labelFabricationStatus;

  @FXML
  public ToggleButton toggleShowConfig;

  @FXML
  protected VBox fabricationConfigView;

  @FXML
  TextField fieldInputTemplateKey;

  @FXML
  ChoiceBox<InputMode> choiceInputMode;

  @FXML
  ChoiceBox<OutputMode> choiceOutputMode;

  @FXML
  ChoiceBox<OutputFileMode> choiceOutputFileMode;

  @FXML
  TextField fieldOutputSeconds;

  @FXML
  TextField fieldOutputPathPrefix;

  @FXML
  TextField fieldBufferAheadSeconds;

  @FXML
  TextField fieldBufferBeforeSeconds;

  @FXML
  TextField fieldOutputChannels;

  @FXML
  TextField fieldOutputFrameRate;

  public MainPaneTopController(
    FabricationService fabricationService
  ) {
    this.fabricationService = fabricationService;
  }

  @Override
  public void onStageReady() {
    buttonAction.disableProperty().bind(Bindings.createBooleanBinding(() ->
        BUTTON_ACTION_ACTIVE_IN_FABRICATION_STATES.contains(fabricationService.statusProperty().get()),
      fabricationService.statusProperty()).not());

    buttonAction.textProperty().bind(Bindings.createStringBinding(() ->
        switch (fabricationService.statusProperty().get()) {
          case Starting, Standby -> BUTTON_TEXT_START;
          case Active -> BUTTON_TEXT_STOP;
          case Cancelled, Failed, Done -> BUTTON_TEXT_RESET;
        },
      fabricationService.statusProperty()));

    choiceInputMode.getItems().setAll(InputMode.values());
    choiceOutputMode.getItems().setAll(OutputMode.values());
    choiceOutputFileMode.getItems().setAll(OutputFileMode.values());

    fieldInputTemplateKey.textProperty().bindBidirectional(fabricationService.inputTemplateKeyProperty());
    choiceInputMode.valueProperty().bindBidirectional(fabricationService.inputModeProperty());
    choiceOutputMode.valueProperty().bindBidirectional(fabricationService.outputModeProperty());
    choiceOutputFileMode.valueProperty().bindBidirectional(fabricationService.outputFileModeProperty());
    fieldOutputSeconds.textProperty().bindBidirectional(fabricationService.outputSecondsProperty());
    fieldOutputPathPrefix.textProperty().bindBidirectional(fabricationService.outputPathPrefixProperty());
    fieldBufferAheadSeconds.textProperty().bindBidirectional(fabricationService.bufferAheadSecondsProperty());
    fieldBufferBeforeSeconds.textProperty().bindBidirectional(fabricationService.bufferBeforeSecondsProperty());
    fieldOutputChannels.textProperty().bindBidirectional(fabricationService.outputChannelsProperty());
    fieldOutputFrameRate.textProperty().bindBidirectional(fabricationService.outputFrameRateProperty());

    labelFabricationStatus.textProperty().bind(fabricationService.statusProperty().map(Enum::toString).map((status) -> String.format("Fabrication %s", status)));
    toggleShowConfig.setSelected(configVisible.get());
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
}
