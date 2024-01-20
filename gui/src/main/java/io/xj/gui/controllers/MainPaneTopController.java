// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.controllers.fabrication.FabricationSettingsModalController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.TemplateMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.nexus.work.FabricationState;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
  private final ProjectService projectService;
  private final FabricationService fabricationService;
  private final UIStateService uiStateService;
  private final FabricationSettingsModalController fabricationSettingsModalController;

  @FXML
  protected AnchorPane mainTopPaneContainer;

  @FXML
  protected TabPane mainTabPane;

  @FXML
  protected Tab tabContent;

  @FXML
  protected Tab tabTemplates;

  @FXML
  protected Tab tabFabrication;

  @FXML
  protected HBox fabricationControlContainer;

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
  }

  @Override
  public void onStageReady() {
    mainTopPaneContainer.visibleProperty().bind(projectService.isStateReadyProperty());
    mainTopPaneContainer.managedProperty().bind(projectService.isStateReadyProperty());

    mainTabPane.getSelectionModel().selectedItemProperty().addListener((o, ov, value) -> {
      if (Objects.equals(value, tabContent)) {
        handlePressedButtonContent();
      } else if (Objects.equals(value, tabTemplates)) {
        handlePressedButtonTemplate();
      } else if (Objects.equals(value, tabFabrication)) {
        handlePressedButtonFabrication();
      }
    });

    fabricationControlContainer.visibleProperty().bind(projectService.viewModeProperty().isEqualTo(ViewMode.Fabrication));
    fabricationControlContainer.managedProperty().bind(projectService.viewModeProperty().isEqualTo(ViewMode.Fabrication));

    buttonAction.textProperty().bind(fabricationService.mainActionButtonTextProperty());
    buttonAction.disableProperty().bind(uiStateService.isMainActionButtonDisabledProperty());

    buttonShowFabricationSettings.disableProperty().bind(uiStateService.isFabricationSettingsDisabledProperty());

    fabricationService.stateProperty().addListener((o, ov, value) -> activateFabricationState(value));

    buttonToggleFollowPlayback.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());

    labelStatus.textProperty().bind(uiStateService.statusTextProperty());
    progressBar.progressProperty().bind(uiStateService.progressProperty());
    progressBar.visibleProperty().bind(uiStateService.isProgressBarVisibleProperty());
    progressBar.managedProperty().bind(uiStateService.isProgressBarVisibleProperty());

    buttonCancelLoading.visibleProperty().bind(projectService.isStateLoadingProperty());
    buttonCancelLoading.managedProperty().bind(projectService.isStateLoadingProperty());

    projectService.viewModeProperty().addListener((o, ov, value) -> activateTab(value));
    activateTab(projectService.viewModeProperty().get());
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
    if (projectService.viewModeProperty().get() == ViewMode.Content) {
      switch (projectService.contentModeProperty().get()) {
        case LibraryEditor, InstrumentBrowser, LibraryBrowser, ProgramBrowser ->
          projectService.contentModeProperty().set(ContentMode.LibraryBrowser);
        case ProgramEditor -> projectService.contentModeProperty().set(ContentMode.ProgramBrowser);
        case InstrumentEditor -> projectService.contentModeProperty().set(ContentMode.InstrumentBrowser);
      }
    } else {
      projectService.viewModeProperty().set(ViewMode.Content);
    }
  }

  @FXML
  protected void handlePressedButtonTemplate() {
    projectService.templateModeProperty().set(TemplateMode.TemplateBrowser);
    projectService.viewModeProperty().set(ViewMode.Template);
  }

  @FXML
  protected void handlePressedButtonFabrication() {
    projectService.viewModeProperty().set(ViewMode.Fabrication);
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
   Activate the tab corresponding to the given view mode.

   @param value the view mode
   */
  private void activateTab(ViewMode value) {
    switch (value) {
      case Content -> mainTabPane.getSelectionModel().select(tabContent);
      case Template -> mainTabPane.getSelectionModel().select(tabTemplates);
      case Fabrication -> mainTabPane.getSelectionModel().select(tabFabrication);
    }
  }

  /**
   Handle a change in the fabrication state.

   @param value      the new value
   */
  private void activateFabricationState(FabricationState value) {
    buttonAction.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, Objects.equals(value, FabricationState.Active));
    buttonAction.pseudoClassStateChanged(FAILED_PSEUDO_CLASS, Objects.equals(value, FabricationState.Failed));
    buttonAction.pseudoClassStateChanged(PENDING_PSEUDO_CLASS, WORK_PENDING_STATES.contains(value));
  }
}
