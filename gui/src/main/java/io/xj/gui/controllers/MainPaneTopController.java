// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.controllers.fabrication.FabricationSettingsModalController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.TemplateMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.WindowUtils;
import io.xj.nexus.work.FabricationState;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

import static io.xj.gui.services.UIStateService.ACTIVE_PSEUDO_CLASS;
import static io.xj.gui.services.UIStateService.FAILED_PSEUDO_CLASS;
import static io.xj.gui.services.UIStateService.PENDING_PSEUDO_CLASS;

@Service
public class MainPaneTopController extends VBox implements ReadyAfterBootController {
  Logger LOG = LoggerFactory.getLogger(MainPaneTopController.class);
  private static final Set<FabricationState> WORK_PENDING_STATES = Set.of(
    FabricationState.Initializing,
    FabricationState.PreparedAudio,
    FabricationState.PreparingAudio,
    FabricationState.Starting
  );
  private static final Set<ViewMode> CONTENT_MODES = Set.of(
    ViewMode.Content,
    ViewMode.Templates
  );
  private final ProjectService projectService;
  private final FabricationService fabricationService;
  private final UIStateService uiStateService;
  private final FabricationSettingsModalController fabricationSettingsModalController;
  private final BooleanBinding isFabricationVisible;
  private final BooleanBinding isStatusVisible;
  private final BooleanBinding isContentVisible;

  @FXML
  protected AnchorPane mainTopPaneContainer;

  @FXML
  protected StackPane fabricationControlContainer;

  @FXML
  protected StackPane statusContainer;

  @FXML
  protected StackPane contentContainer;

  @FXML
  protected ProgressBar progressBar;

  @FXML
  protected Button buttonCancelLoading;

  @FXML
  protected Button buttonAction;

  @FXML
  protected Label labelStatus;

  @FXML
  protected ToggleButton buttonToggleFollowPlayback;

  @FXML
  protected Button buttonShowFabricationSettings;

  @FXML
  protected Button buttonGoUpContentLevel;

  public MainPaneTopController(
    FabricationService fabricationService,
    FabricationSettingsModalController fabricationSettingsModalController,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.fabricationService = fabricationService;
    this.fabricationSettingsModalController = fabricationSettingsModalController;
    this.projectService = projectService;
    this.uiStateService = uiStateService;

    isFabricationVisible = projectService.viewModeProperty().isEqualTo(ViewMode.Fabrication);

    isStatusVisible = uiStateService.isStatusTextVisibleProperty()
      .or(uiStateService.isProgressBarVisibleProperty())
      .or(projectService.isStateLoadingProperty());

    isContentVisible = Bindings.createBooleanBinding(
      () -> isStatusVisible.not().get() && CONTENT_MODES.contains(projectService.viewModeProperty().get()),
      isStatusVisible, projectService.viewModeProperty());

    isStatusVisible.not().and(
      projectService.viewModeProperty().isEqualTo(ViewMode.Content)
        .or(projectService.viewModeProperty().isEqualTo(ViewMode.Templates)));
  }

  @Override
  public void onStageReady() {
    fabricationControlContainer.visibleProperty().bind(isFabricationVisible);
    fabricationControlContainer.managedProperty().bind(isFabricationVisible);
    buttonAction.textProperty().bind(fabricationService.mainActionButtonTextProperty());
    buttonAction.disableProperty().bind(uiStateService.isMainActionButtonDisabledProperty());
    buttonShowFabricationSettings.disableProperty().bind(uiStateService.isFabricationSettingsDisabledProperty());
    fabricationService.stateProperty().addListener((o, ov, value) -> activateFabricationState(value));
    buttonToggleFollowPlayback.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());

    statusContainer.visibleProperty().bind(isStatusVisible);
    statusContainer.managedProperty().bind(isStatusVisible);
    labelStatus.textProperty().bind(uiStateService.statusTextProperty());
    labelStatus.visibleProperty().bind(uiStateService.isStatusTextVisibleProperty());
    progressBar.progressProperty().bind(uiStateService.progressProperty());
    progressBar.visibleProperty().bind(uiStateService.isProgressBarVisibleProperty());
    progressBar.managedProperty().bind(uiStateService.isProgressBarVisibleProperty());
    buttonCancelLoading.visibleProperty().bind(projectService.isStateLoadingProperty());
    buttonCancelLoading.managedProperty().bind(projectService.isStateLoadingProperty());

    contentContainer.visibleProperty().bind(isContentVisible);
    contentContainer.managedProperty().bind(isContentVisible);
    buttonGoUpContentLevel.visibleProperty().bind(projectService.isContentLevelUpPossibleProperty());
    buttonGoUpContentLevel.managedProperty().bind(projectService.isContentLevelUpPossibleProperty());
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
  protected void handlePressedGoUpContentLevel() {
    projectService.goUpContentLevel();
  }

  @FXML
  protected void handlePressedCancelLoading() {
    Platform.runLater(projectService::cancelProjectLoading);
  }

  @FXML
  public void handleShowFabricationSettings(ActionEvent ignored) {
    fabricationSettingsModalController.launchModal();
  }

  /**
   Handle a change in the fabrication state.

   @param value the new value
   */
  private void activateFabricationState(FabricationState value) {
    buttonAction.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, Objects.equals(value, FabricationState.Active));
    buttonAction.pseudoClassStateChanged(FAILED_PSEUDO_CLASS, Objects.equals(value, FabricationState.Failed));
    buttonAction.pseudoClassStateChanged(PENDING_PSEUDO_CLASS, WORK_PENDING_STATES.contains(value));
  }
}
