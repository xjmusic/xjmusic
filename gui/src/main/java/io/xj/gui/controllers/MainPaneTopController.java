// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.FabricationStatus;
import io.xj.gui.services.LabService;
import io.xj.gui.services.PreloaderService;
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
  final FabricationService fabricationService;
  final PreloaderService preloaderService;
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
    PreloaderService preloaderService
  ) {
    this.fabricationService = fabricationService;
    this.labService = labService;
    this.modalFabricationSettingsController = modalFabricationSettingsController;
    this.modalLabAuthenticationController = modalLabAuthenticationController;
    this.preloaderService = preloaderService;
  }

  @Override
  public void onStageReady() {
    buttonAction.disableProperty().bind(Bindings.createBooleanBinding(
      () -> BUTTON_ACTION_ACTIVE_IN_FABRICATION_STATES.contains(fabricationService.statusProperty().get()),
      fabricationService.statusProperty()).not());

    buttonAction.textProperty().bind(fabricationService.mainActionButtonTextProperty());

    fabricationService.statusProperty().addListener(this::handleFabricationStatusChange);
    buttonToggleFollowPlayback.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());

    var fabricationIsActive = Bindings.createBooleanBinding(
      () -> fabricationService.isStatusActive().get() || preloaderService.runningProperty().get(),
      fabricationService.isStatusActive(), preloaderService.runningProperty());

    buttonShowFabricationSettings.disableProperty().bind(fabricationIsActive);
    buttonPreload.disableProperty().bind(fabricationIsActive);

    labelFabricationStatus.textProperty().bind(fabricationService.statusProperty().map(Enum::toString).map((status) -> String.format("Fabrication %s", status)));

    labelLabStatus.textProperty().bind(labService.statusProperty().map(Enum::toString));

    var isFileOutputActive = Bindings.createBooleanBinding(
      () -> fabricationService.isStatusActive().get() && fabricationService.isOutputModeFile().get(),
      fabricationService.statusProperty(), fabricationService.isOutputModeFile());

    progressBarFabrication.visibleProperty().bind(Bindings.createBooleanBinding(
      () -> isFileOutputActive.get() || preloaderService.runningProperty().get(),
      isFileOutputActive, preloaderService.runningProperty()));

    progressBarFabrication.progressProperty().bind(Bindings.createDoubleBinding(
      () -> {
        if (preloaderService.runningProperty().get()) {
          return preloaderService.progressProperty().get();
        } else if (isFileOutputActive.get()) {
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
  public void handlePreloadTemplate(ActionEvent ignored) {
    preloaderService.resetAndStart();
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
}
