// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

@Service
public class MainPaneTopController extends VBox implements ReadyAfterBootController {
  final FabricationService fabricationService;
  final PreloaderService preloaderService;
  final UIStateService uiStateService;
  final ModalFabricationSettingsController modalFabricationSettingsController;
  final ModalLabAuthenticationController modalLabAuthenticationController;
  final LabService labService;

  @FXML
  protected ProgressBar progressBarFabrication;

  @FXML
  protected Button buttonAction;

  @FXML
  protected Label labelFabricationStatus;

  @FXML
  protected Button buttonLab;

  @FXML
  protected ToggleButton buttonToggleFollowPlayback;

  @FXML
  protected Label labelLabStatus;

  @FXML
  protected Button buttonShowFabricationSettings;

  @FXML
  protected Button buttonPreload;

  public MainPaneTopController(
    FabricationService fabricationService,
    LabService labService,
    ModalFabricationSettingsController modalFabricationSettingsController,
    ModalLabAuthenticationController modalLabAuthenticationController,
    PreloaderService preloaderService,
    UIStateService uiStateService
  ) {
    this.fabricationService = fabricationService;
    this.labService = labService;
    this.modalFabricationSettingsController = modalFabricationSettingsController;
    this.modalLabAuthenticationController = modalLabAuthenticationController;
    this.preloaderService = preloaderService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    buttonAction.disableProperty().bind(uiStateService.fabricationActionDisabledProperty());
    buttonAction.textProperty().bind(fabricationService.mainActionButtonTextProperty());

    labService.statusProperty().addListener(this::handleLabStatusChange);
    fabricationService.statusProperty().addListener(this::handleFabricationStatusChange);
    buttonToggleFollowPlayback.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());
    buttonToggleFollowPlayback.disableProperty().bind(preloaderService.runningProperty());

    buttonShowFabricationSettings.disableProperty().bind(uiStateService.fabricationSettingsDisabledProperty());

    buttonPreload.disableProperty().bind(fabricationService.isStatusActive());
    buttonPreload.textProperty().bind(preloaderService.actionTextProperty());

    labelFabricationStatus.textProperty().bind(uiStateService.fabricationStatusTextProperty());

    labelLabStatus.textProperty().bind(labService.statusProperty().map(Enum::toString));

    progressBarFabrication.visibleProperty().bind(Bindings.createBooleanBinding(
      () -> isFileOutputActive.get() || preloaderService.runningProperty().get(),
      isFileOutputActive, preloaderService.runningProperty()));

    progressBarFabrication.progressProperty().bind(Bindings.createDoubleBinding(
      () -> {
        if (preloaderService.runningProperty().get()) {
          return preloaderService.progressProperty().get();
        } else if (uiStateService.isFileOutputActiveProperty().get()) {
          return fabricationService.progressProperty().get();
        } else {
          return 0.0;
        }
      },
      isFileOutputActive, fabricationService.progressProperty(), preloaderService.runningProperty(), preloaderService.progressProperty()));
  }

  @Override
  public void onStageClose() {
    fabricationService.cancel();
  }

  @FXML
  protected void handleButtonActionPress() {
    fabricationService.handleMainAction();
  }

  @FXML
  public void handlePreloadButtonPress(ActionEvent ignored) {
    if (preloaderService.isRunning()) {
      preloaderService.cancel();
    } else {
      preloaderService.resetAndStart();
    }
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

  private void handleFabricationStatusChange(ObservableValue<? extends FabricationStatus> observable, FabricationStatus prior, FabricationStatus newValue) {
    switch (newValue) {
      case Standby, Failed, Done, Cancelled -> buttonAction.getStyleClass().remove("button-active");
      case Starting, Active -> buttonAction.getStyleClass().add("button-active");
    }
  }

  private void handleLabStatusChange(ObservableValue<? extends LabStatus> observable, LabStatus prior, LabStatus newValue) {
    switch (newValue) {
      case Offline -> buttonLab.getStyleClass().removeAll("button-active", "button-pending", "button-failed");
      case Connecting, Configuring -> {
        buttonLab.getStyleClass().removeAll("button-active", "button-failed");
        buttonLab.getStyleClass().add("button-pending");
      }
      case Authenticated -> {
        buttonLab.getStyleClass().removeAll("button-pending", "button-failed");
        buttonLab.getStyleClass().add("button-active");
      }
      case Unauthorized, Failed -> {
        buttonLab.getStyleClass().removeAll("button-active", "button-pending");
        buttonLab.getStyleClass().add("button-failed");
      }
    }
  }
}
