// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
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

  @FXML
  protected ProgressBar progressBarFabrication;

  @FXML
  protected Button buttonAction;

  @FXML
  protected Label labelFabricationStatus;

  @FXML
  protected ToggleButton buttonToggleFollowPlayback;

  @FXML
  protected Button buttonShowFabricationSettings;

  public MainPaneTopController(
    FabricationService fabricationService,
    ModalFabricationSettingsController modalFabricationSettingsController,
    UIStateService uiStateService
  ) {
    this.fabricationService = fabricationService;
    this.modalFabricationSettingsController = modalFabricationSettingsController;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    buttonAction.textProperty().bind(fabricationService.mainActionButtonTextProperty());

    buttonShowFabricationSettings.disableProperty().bind(uiStateService.isFabricationSettingsDisabledProperty());

    buttonToggleFollowPlayback.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());

    fabricationService.stateProperty().addListener(this::handleFabricationStatusChange);

    labelFabricationStatus.textProperty().bind(uiStateService.fabricationStatusTextProperty());

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

  private void handleFabricationStatusChange(ObservableValue<? extends FabricationState> observable, FabricationState prior, FabricationState newValue) {
    switch (newValue) {
      case Standby, Failed, Done, Cancelled -> buttonAction.getStyleClass().remove("button-active");
      case LoadingContent, Initializing, PreparedAudio, PreparingAudio, LoadedContent, Starting, Active ->
        buttonAction.getStyleClass().add("button-active");
    }
  }
}
