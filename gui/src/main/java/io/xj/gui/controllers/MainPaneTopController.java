// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.LabStatus;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ProjectViewMode;
import io.xj.gui.services.UIStateService;
import io.xj.nexus.work.WorkState;
import javafx.beans.binding.Bindings;
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
  private static final Set<WorkState> WORK_PENDING_STATES = Set.of(
    WorkState.LoadingContent,
    WorkState.Initializing,
    WorkState.PreparedAudio,
    WorkState.PreparingAudio,
    WorkState.LoadedContent,
    WorkState.Starting
  );
  private static final Set<LabStatus> LAB_PENDING_STATES = Set.of(
    LabStatus.Connecting,
    LabStatus.Configuring
  );
  private static final Set<LabStatus> LAB_FAILED_STATES = Set.of(
    LabStatus.Unauthorized,
    LabStatus.Failed
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

    buttonShowFabricationSettings.disableProperty().bind(uiStateService.isFabricationSettingsDisabledProperty());

    buttonToggleFollowPlayback.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());

    fabricationService.stateProperty().addListener(this::handleFabricationStatusChange);

    labService.statusProperty().addListener(this::handleLabStatusChange);


    labelLabStatus.textProperty().bind(labService.statusProperty().map(Enum::toString));

    labelStatus.textProperty().bind(uiStateService.statusTextProperty());
    progressBar.progressProperty().bind(uiStateService.progressProperty());
    progressBar.visibleProperty().bind(uiStateService.isProgressBarVisibleProperty());
    progressBar.managedProperty().bind(uiStateService.isProgressBarVisibleProperty());

    buttonContent.setSelected(true);
    buttonFabrication.disableProperty().bind(Bindings.createBooleanBinding(() -> !projectService.isStateReadyProperty().get(), projectService.isStateReadyProperty()));
    fabricationControlContainer.visibleProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.FABRICATION));
    fabricationControlContainer.managedProperty().bind(projectService.viewModeProperty().isEqualTo(ProjectViewMode.FABRICATION));
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
  protected void handleButtonContentPress() {
    projectService.viewModeProperty().set(ProjectViewMode.CONTENT);
  }

  @FXML
  protected void handleButtonFabricationPress() {
    projectService.viewModeProperty().set(ProjectViewMode.FABRICATION);
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

  private void handleFabricationStatusChange(ObservableValue<? extends WorkState> ignored1, WorkState ignored2, WorkState value) {
    buttonAction.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, Objects.equals(value, WorkState.Active));
    buttonAction.pseudoClassStateChanged(FAILED_PSEUDO_CLASS, Objects.equals(value, WorkState.Failed));
    buttonAction.pseudoClassStateChanged(PENDING_PSEUDO_CLASS, WORK_PENDING_STATES.contains(value));
  }

  private void handleLabStatusChange(ObservableValue<? extends LabStatus> ignored1, LabStatus ignored2, LabStatus value) {
    buttonLab.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, Objects.equals(value, LabStatus.Authenticated));
    buttonLab.pseudoClassStateChanged(FAILED_PSEUDO_CLASS, LAB_FAILED_STATES.contains(value));
    buttonLab.pseudoClassStateChanged(PENDING_PSEUDO_CLASS, LAB_PENDING_STATES.contains(value));
  }
}
