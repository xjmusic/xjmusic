// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.controllers.fabrication.FabricationSettingsModalController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
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
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

import static io.xj.gui.services.UIStateService.ACTIVE_PSEUDO_CLASS;
import static io.xj.gui.services.UIStateService.FAILED_PSEUDO_CLASS;
import static io.xj.gui.services.UIStateService.PENDING_PSEUDO_CLASS;

@Service
public class MainPaneTopController extends VBox implements ReadyAfterBootController {
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
  private final CmdModalController cmdModalController;
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
  protected StackPane browserStatusContainer;

  @FXML
  protected StackPane browserControlContainer;

  @FXML
  protected HBox libraryContentSelectionContainer;

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

  @FXML
  protected Button buttonCreateEntity;

  @FXML
  protected Label labelViewingParent;

  @FXML
  protected Label labelViewingSeparator;

  @FXML
  protected Label labelViewingEntity;

  @FXML
  protected ToggleGroup libraryContentSelectionToggle;

  @FXML
  protected ToggleButton buttonLibraryContentPrograms;

  @FXML
  protected ToggleButton buttonLibraryContentInstruments;

  public MainPaneTopController(
    FabricationService fabricationService,
    FabricationSettingsModalController fabricationSettingsModalController,
    CmdModalController cmdModalController,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.fabricationService = fabricationService;
    this.fabricationSettingsModalController = fabricationSettingsModalController;
    this.cmdModalController = cmdModalController;
    this.projectService = projectService;
    this.uiStateService = uiStateService;

    isFabricationVisible = uiStateService.viewModeProperty().isEqualTo(ViewMode.Fabrication);

    isStatusVisible = uiStateService.isStateTextVisibleProperty()
      .or(uiStateService.isProgressBarVisibleProperty())
      .or(projectService.isStateLoadingProperty());

    isContentVisible = Bindings.createBooleanBinding(
      () -> isStatusVisible.not().get() && CONTENT_MODES.contains(uiStateService.viewModeProperty().get()),
      isStatusVisible, uiStateService.viewModeProperty());

    isStatusVisible.not().and(
      uiStateService.viewModeProperty().isEqualTo(ViewMode.Content)
        .or(uiStateService.viewModeProperty().isEqualTo(ViewMode.Templates)));

    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(v, ContentMode.ProgramBrowser)) {
        libraryContentSelectionToggle.selectToggle(buttonLibraryContentPrograms);
      } else if (Objects.equals(v, ContentMode.InstrumentBrowser)) {
        libraryContentSelectionToggle.selectToggle(buttonLibraryContentInstruments);
      }
    });
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
    labelStatus.textProperty().bind(uiStateService.stateTextProperty());
    labelStatus.visibleProperty().bind(uiStateService.isStateTextVisibleProperty());
    progressBar.progressProperty().bind(uiStateService.progressProperty());
    progressBar.visibleProperty().bind(uiStateService.isProgressBarVisibleProperty());
    progressBar.managedProperty().bind(uiStateService.isProgressBarVisibleProperty());
    buttonCancelLoading.visibleProperty().bind(projectService.isStateLoadingProperty());
    buttonCancelLoading.managedProperty().bind(projectService.isStateLoadingProperty());

    browserStatusContainer.visibleProperty().bind(isContentVisible);
    buttonGoUpContentLevel.visibleProperty().bind(uiStateService.isContentLevelUpPossibleProperty());
    buttonGoUpContentLevel.managedProperty().bind(uiStateService.isContentLevelUpPossibleProperty());
    labelViewingParent.visibleProperty().bind(projectService.isStateReadyProperty());
    labelViewingParent.textProperty().bind(uiStateService.currentParentNameProperty());
    var isSeparatorVisible = uiStateService.isViewingEntityProperty().or(uiStateService.isLibraryContentBrowserProperty());
    labelViewingSeparator.visibleProperty().bind(isSeparatorVisible);
    labelViewingSeparator.managedProperty().bind(isSeparatorVisible);
    labelViewingEntity.visibleProperty().bind(uiStateService.isViewingEntityProperty());
    labelViewingEntity.managedProperty().bind(uiStateService.isViewingEntityProperty());
    labelViewingEntity.textProperty().bind(uiStateService.currentEntityNameProperty());
    libraryContentSelectionContainer.visibleProperty().bind(uiStateService.isLibraryContentBrowserProperty());
    libraryContentSelectionToggle.selectedToggleProperty().addListener((o, ov, v) -> {
      if (Objects.equals(v, buttonLibraryContentPrograms)) {
        Platform.runLater(() -> uiStateService.contentModeProperty().set(ContentMode.ProgramBrowser));
      } else if (Objects.equals(v, buttonLibraryContentInstruments)) {
        Platform.runLater(() -> uiStateService.contentModeProperty().set(ContentMode.InstrumentBrowser));
      }
    });

    browserControlContainer.visibleProperty().bind(isContentVisible);
    buttonCreateEntity.visibleProperty().bind(uiStateService.isCreateEntityButtonVisibleProperty());
    buttonCreateEntity.textProperty().bind(uiStateService.createEntityButtonTextProperty());
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
    uiStateService.goUpContentLevel();
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
  private void handleCreateEntity(ActionEvent ignored) {
    switch (uiStateService.viewModeProperty().get()) {
      case Content -> {
        switch (uiStateService.contentModeProperty().get()) {
          case LibraryBrowser -> cmdModalController.createLibrary();
          case ProgramBrowser -> cmdModalController.createProgram(uiStateService.currentLibraryProperty().get());
          case InstrumentBrowser -> cmdModalController.createInstrument(uiStateService.currentLibraryProperty().get());
        }
      }
      case Templates -> cmdModalController.createTemplate();
    }
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
