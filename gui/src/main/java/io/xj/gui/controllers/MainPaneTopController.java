// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.FabricationStatus;
import io.xj.gui.services.LabService;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MainPaneTopController extends VBox implements ReadyAfterBootController {
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
  final ModalFabricationSettingsController modalFabricationSettingsController;
  final ModalLabAuthenticationController modalLabAuthenticationController;
  final LabService labService;

  @FXML
  Button buttonAction;

  @FXML
  Label labelFabricationStatus;

  @FXML
  public Button buttonLab;

  @FXML
  public Label labelLabStatus;

  public MainPaneTopController(
    ModalFabricationSettingsController modalFabricationSettingsController,
    ModalLabAuthenticationController modalLabAuthenticationController,
    FabricationService fabricationService,
    LabService labService
  ) {
    this.modalFabricationSettingsController = modalFabricationSettingsController;
    this.modalLabAuthenticationController = modalLabAuthenticationController;
    this.fabricationService = fabricationService;
    this.labService = labService;
  }

  @Override
  public void onStageReady() {
    buttonAction.disableProperty().bind(Bindings.createBooleanBinding(this::isActionButtonActive, fabricationService.statusProperty()).not());
    buttonAction.textProperty().bind(Bindings.createStringBinding(this::computeActionButtonText, fabricationService.statusProperty()));
    fabricationService.statusProperty().addListener(this::handleFabricationStatusChange);

    labelFabricationStatus.textProperty().bind(fabricationService.statusProperty().map(Enum::toString).map((status) -> String.format("Fabrication %s", status)));

    labelLabStatus.textProperty().bind(labService.statusProperty().map(Enum::toString));
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
  public void handleShowFabricationSettings(ActionEvent ignored) {
    modalFabricationSettingsController.launchModal();
  }

  @FXML
  public void handleButtonLabPressed(ActionEvent ignored) {
    if (labService.isAuthenticated()) {
      labService.launchInBrowser();
    } else {
      modalLabAuthenticationController.launchModal();
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
}
