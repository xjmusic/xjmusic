// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.LabState;
import io.xj.gui.services.UIStateService;
import io.xj.nexus.work.FabricationState;
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

  public MainPaneTopController(
    FabricationService fabricationService,
    LabService labService,
    ModalFabricationSettingsController modalFabricationSettingsController,
    ModalLabAuthenticationController modalLabAuthenticationController,
    UIStateService uiStateService
  ) {
    this.fabricationService = fabricationService;
    this.labService = labService;
    this.modalFabricationSettingsController = modalFabricationSettingsController;
    this.modalLabAuthenticationController = modalLabAuthenticationController;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    buttonAction.textProperty().bind(fabricationService.mainActionButtonTextProperty());

    buttonShowFabricationSettings.disableProperty().bind(uiStateService.isFabricationSettingsDisabledProperty());

    buttonToggleFollowPlayback.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());

    fabricationService.stateProperty().addListener(this::handleFabricationStatusChange);

    labService.stateProperty().addListener(this::handleLabStatusChange);

    labelFabricationStatus.textProperty().bind(uiStateService.fabricationStatusTextProperty());

    labelLabStatus.textProperty().bind(labService.stateProperty().map(Enum::toString));

    progressBarFabrication.progressProperty().bind(fabricationService.progressProperty());
    progressBarFabrication.visibleProperty().bind(uiStateService.isProgressBarVisibleProperty());
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

  private void handleFabricationStatusChange(ObservableValue<? extends FabricationState> observable, FabricationState prior, FabricationState newValue) {
    switch (newValue) {
      case Standby, Failed, Done, Cancelled ->
        buttonAction.getStyleClass().remove("button-active");
      case LoadingContent, Initializing, PreparedAudio, PreparingAudio, LoadedContent, Starting, Active ->
        buttonAction.getStyleClass().add("button-active");
    }
  }

  private void handleLabStatusChange(ObservableValue<? extends LabState> observable, LabState prior, LabState newValue) {
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
