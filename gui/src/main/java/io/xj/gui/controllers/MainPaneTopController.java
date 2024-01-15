// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.LabState;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ProjectViewMode;
import io.xj.gui.services.UIStateService;
import io.xj.nexus.work.FabricationState;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Service
public class MainPaneTopController extends VBox implements ReadyAfterBootController {
  private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");
  private static final PseudoClass PENDING_PSEUDO_CLASS = PseudoClass.getPseudoClass("pending");
  private static final PseudoClass FAILED_PSEUDO_CLASS = PseudoClass.getPseudoClass("failed");
  private static final Set<FabricationState> WORK_PENDING_STATES = Set.of(
    FabricationState.Initializing,
    FabricationState.PreparedAudio,
    FabricationState.PreparingAudio,
    FabricationState.Starting
  );
  private static final Set<LabState> LAB_PENDING_STATES = Set.of(
    LabState.Connecting,
    LabState.Configuring
  );
  private static final Set<LabState> LAB_FAILED_STATES = Set.of(
    LabState.Unauthorized,
    LabState.Failed
  );
  private final ProjectService projectService;
  private final FabricationService fabricationService;
  private final UIStateService uiStateService;
  private final FabricationSettingsModalController fabricationSettingsModalController;
  private final MainLabAuthenticationModalController mainLabAuthenticationModalController;
  private final LabService labService;

  @FXML
  protected HBox fabricationControlContainer;

  @FXML
  protected ProgressBar progressBar;

  @FXML
  protected ToggleGroup viewMode;

  @FXML
  protected ToggleButton buttonContent;

  @FXML
  protected Button buttonCancelLoading;

  @FXML
  protected ToggleButton buttonFabrication;

  @FXML
  protected Button buttonAction;

  @FXML
  protected Label labelStatus;

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
    FabricationSettingsModalController fabricationSettingsModalController,
    LabService labService,
    MainLabAuthenticationModalController mainLabAuthenticationModalController,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.fabricationService = fabricationService;
    this.fabricationSettingsModalController = fabricationSettingsModalController;
    this.labService = labService;
    this.mainLabAuthenticationModalController = mainLabAuthenticationModalController;
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    buttonAction.textProperty().bind(fabricationService.mainActionButtonTextProperty());
    buttonAction.disableProperty().bind(uiStateService.isMainActionButtonDisabledProperty());

    buttonShowFabricationSettings.disableProperty().bind(uiStateService.isFabricationSettingsDisabledProperty());

    buttonToggleFollowPlayback.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());

    fabricationService.stateProperty().addListener(this::handleFabricationStateChange);
    labService.stateProperty().addListener(this::handleLabStateChange);

    labelLabStatus.textProperty().bind(labService.stateProperty().map(Enum::toString));

    labelStatus.textProperty().bind(uiStateService.statusTextProperty());
    progressBar.progressProperty().bind(uiStateService.progressProperty());
    progressBar.visibleProperty().bind(uiStateService.isProgressBarVisibleProperty());
    progressBar.managedProperty().bind(uiStateService.isProgressBarVisibleProperty());

    buttonCancelLoading.visibleProperty().bind(projectService.isStateLoadingProperty());
    buttonCancelLoading.managedProperty().bind(projectService.isStateLoadingProperty());

    buttonContent.setSelected(true);
    buttonContent.visibleProperty().bind(projectService.isStateReadyProperty());
    buttonContent.managedProperty().bind(projectService.isStateReadyProperty());

    buttonFabrication.visibleProperty().bind(projectService.isStateReadyProperty());
    buttonFabrication.managedProperty().bind(projectService.isStateReadyProperty());

    fabricationControlContainer.visibleProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.Fabrication));
    fabricationControlContainer.managedProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.Fabrication));

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
  protected void handlePressedButtonContent() {
    projectService.viewModeProperty().set(ProjectViewMode.Content);
  }

  @FXML
  protected void handlePressedButtonFabrication() {
    projectService.viewModeProperty().set(ProjectViewMode.Fabrication);
  }

  @FXML
  protected void handlePressedCancelLoading() {
    Platform.runLater(projectService::cancelProjectLoading);
  }

  @FXML
  public void handleShowFabricationSettings(ActionEvent ignored) {
    fabricationSettingsModalController.launchModal();
  }

  @FXML
  public void handleButtonLabPressed(ActionEvent ignored) {
    if (labService.isAuthenticated()) {
      labService.launchInBrowser();
    } else {
      mainLabAuthenticationModalController.launchModal();
    }
  }

  private void handleFabricationStateChange(ObservableValue<? extends FabricationState> o, FabricationState ov, FabricationState value) {
    buttonAction.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, Objects.equals(value, FabricationState.Active));
    buttonAction.pseudoClassStateChanged(FAILED_PSEUDO_CLASS, Objects.equals(value, FabricationState.Failed));
    buttonAction.pseudoClassStateChanged(PENDING_PSEUDO_CLASS, WORK_PENDING_STATES.contains(value));
  }

  private void handleLabStateChange(ObservableValue<? extends LabState> o, LabState ov, LabState value) {
    buttonLab.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, Objects.equals(value, LabState.Authenticated));
    buttonLab.pseudoClassStateChanged(FAILED_PSEUDO_CLASS, LAB_FAILED_STATES.contains(value));
    buttonLab.pseudoClassStateChanged(PENDING_PSEUDO_CLASS, LAB_PENDING_STATES.contains(value));
  }
}
