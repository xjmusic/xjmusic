// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.ProjectController;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.types.Route;
import io.xj.hub.pojos.Template;
import io.xj.nexus.work.FabricationState;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

import static io.xj.gui.services.UIStateService.ACTIVE_PSEUDO_CLASS;
import static io.xj.gui.services.UIStateService.FAILED_PSEUDO_CLASS;
import static io.xj.gui.services.UIStateService.PENDING_PSEUDO_CLASS;

@Service
public class MainPaneTopController extends ProjectController {
  private static final Set<FabricationState> WORK_PENDING_STATES = Set.of(
    FabricationState.Initializing,
    FabricationState.PreparedAudio,
    FabricationState.PreparingAudio,
    FabricationState.Starting
  );
  private final CmdModalController cmdModalController;
  private final FabricationService fabricationService;
  private final SettingsModalController settingsModalController;
  private final StringBinding createEntityButtonText;

  @FXML
  AnchorPane mainTopPaneContainer;

  @FXML
  Button browserButtonUpContentLevel;

  @FXML
  Button browserCreateEntityButton;

  @FXML
  Button fabricationActionButton;

  @FXML
  Button fabricationButtonShowSettings;

  @FXML
  Button progressCancelButton;

  @FXML
  HBox browserLibraryContentSelectionContainer;

  @FXML
  Label browserLabelViewingEntity;

  @FXML
  Label browserLabelViewingParent;

  @FXML
  Label browserLabelViewingSeparator;

  @FXML
  Label progressLabel;

  @FXML
  ProgressBar progressBar;

  @FXML
  StackPane browserControlContainer;

  @FXML
  StackPane browserStatusContainer;

  @FXML
  StackPane fabricationControlContainer;

  @FXML
  StackPane progressContainer;

  @FXML
  ToggleButton browserLibraryContentInstrumentsButton;

  @FXML
  ToggleButton browserLibraryContentProgramsButton;

  @FXML
  ToggleButton fabricationToggleFollowButton;

  @FXML
  ToggleGroup browserLibraryContentSelectionToggle;

  @FXML
  ChoiceBox<TemplateChoice> choiceTemplate;

  public MainPaneTopController(
    @Value("classpath:/views/main-pane-top.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    FabricationService fabricationService,
    SettingsModalController settingsModalController,
    CmdModalController cmdModalController,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.fabricationService = fabricationService;
    this.settingsModalController = settingsModalController;
    this.cmdModalController = cmdModalController;

    uiStateService.navStateProperty().addListener((o, ov, v) -> {
      if (Objects.equals(v, Route.ContentProgramBrowser)) {
        browserLibraryContentSelectionToggle.selectToggle(browserLibraryContentProgramsButton);
      } else if (Objects.equals(v, Route.ContentInstrumentBrowser)) {
        browserLibraryContentSelectionToggle.selectToggle(browserLibraryContentInstrumentsButton);
      }
    });

    createEntityButtonText = Bindings.createStringBinding(
      () -> switch (uiStateService.navStateProperty().get()) {
        case ContentLibraryBrowser -> "New Library";
        case ContentProgramBrowser -> "New Program";
        case ContentInstrumentBrowser -> "New Instrument";
        case TemplateBrowser, TemplateEditor -> "New Template";
        default -> "";
      },
      uiStateService.navStateProperty()
    );
  }

  @Override
  public void onStageReady() {
    var isViewModeFabrication = Bindings.createBooleanBinding(
      () -> uiStateService.navStateProperty().get().isFabrication(),
      uiStateService.navStateProperty()
    );
    fabricationActionButton.disableProperty().bind(uiStateService.isMainActionButtonDisabledProperty());
    fabricationActionButton.textProperty().bind(fabricationService.mainActionButtonTextProperty());
    fabricationButtonShowSettings.disableProperty().bind(uiStateService.isFabricationSettingsDisabledProperty());
    fabricationControlContainer.managedProperty().bind(isViewModeFabrication);
    fabricationControlContainer.visibleProperty().bind(isViewModeFabrication);
    fabricationService.stateProperty().addListener((o, ov, value) -> activateFabricationState(value));
    fabricationToggleFollowButton.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());

    progressContainer.managedProperty().bind(uiStateService.isViewProgressStatusModeProperty());
    progressContainer.visibleProperty().bind(uiStateService.isViewProgressStatusModeProperty());
    progressBar.managedProperty().bind(uiStateService.isProgressBarVisibleProperty());
    progressBar.progressProperty().bind(uiStateService.progressProperty());
    progressBar.visibleProperty().bind(uiStateService.isProgressBarVisibleProperty());
    progressCancelButton.managedProperty().bind(projectService.isStateLoadingProperty());
    progressCancelButton.visibleProperty().bind(projectService.isStateLoadingProperty());
    progressLabel.textProperty().bind(uiStateService.stateTextProperty());
    progressLabel.visibleProperty().bind(uiStateService.isProgressBarVisibleProperty());

    var isProgramOrInstrumentBrowser = Bindings.createBooleanBinding(
      () -> uiStateService.navStateProperty().get() == Route.ContentProgramBrowser
        || uiStateService.navStateProperty().get() == Route.ContentInstrumentBrowser,
      uiStateService.navStateProperty());
    var browserSeparatorVisible = uiStateService.isViewingEntityProperty().or(isProgramOrInstrumentBrowser);
    browserButtonUpContentLevel.managedProperty().bind(uiStateService.isContentLevelUpPossibleProperty());
    browserButtonUpContentLevel.visibleProperty().bind(uiStateService.isContentLevelUpPossibleProperty());
    browserControlContainer.visibleProperty().bind(uiStateService.isViewContentNavigationStatusModeProperty());
    browserCreateEntityButton.textProperty().bind(createEntityButtonText);
    browserCreateEntityButton.visibleProperty().bind(uiStateService.isCreateEntityButtonVisibleProperty());
    browserLabelViewingEntity.managedProperty().bind(uiStateService.isViewingEntityProperty());
    browserLabelViewingEntity.textProperty().bind(uiStateService.currentEntityNameProperty());
    browserLabelViewingEntity.visibleProperty().bind(uiStateService.isViewingEntityProperty());
    browserLabelViewingParent.textProperty().bind(uiStateService.currentParentNameProperty());
    browserLabelViewingParent.visibleProperty().bind(projectService.isStateReadyProperty());
    browserLabelViewingSeparator.managedProperty().bind(browserSeparatorVisible);
    browserLabelViewingSeparator.visibleProperty().bind(browserSeparatorVisible);
    browserLibraryContentSelectionContainer.visibleProperty().bind(isProgramOrInstrumentBrowser);
    browserStatusContainer.visibleProperty().bind(uiStateService.isViewContentNavigationStatusModeProperty());
    browserLibraryContentSelectionToggle.selectedToggleProperty().addListener((o, ov, v) -> {
      if (Objects.equals(v, browserLibraryContentProgramsButton)) {
        Platform.runLater(() -> uiStateService.navigateTo(Route.ContentProgramBrowser));
      } else if (Objects.equals(v, browserLibraryContentInstrumentsButton)) {
        Platform.runLater(() -> uiStateService.navigateTo(Route.ContentInstrumentBrowser));
      }
    });

    Runnable updateTemplateChoices = () -> {
      var templates = projectService.getContent().getTemplates().stream().map(TemplateChoice::new).toList();
      choiceTemplate.setItems(FXCollections.observableArrayList(templates));
      // if no template is selected, select the first one in the list
      if (Objects.isNull(fabricationService.inputTemplateProperty().get()) && !templates.isEmpty()) {
        choiceTemplate.setValue(templates.get(0));
      }
    };
    projectService.isStateReadyProperty().addListener((o, ov, v) -> {
      if (v) updateTemplateChoices.run();
    });
    projectService.addProjectUpdateListener(Template.class, ()->{
      if (projectService.isStateReadyProperty().get()) updateTemplateChoices.run();
    });
    choiceTemplate.setOnAction(event -> {
      TemplateChoice choice = choiceTemplate.getValue();
      if (Objects.nonNull(choice)) {
        fabricationService.inputTemplateProperty().set(choice.template());
      }
    });
    choiceTemplate.setValue(new TemplateChoice(fabricationService.inputTemplateProperty().get()));
  }

  @Override
  public void onStageClose() {
    fabricationService.cancel();
  }

  @FXML
  void fabricationPressedAction() {
    fabricationService.handleMainAction();
  }

  @FXML
  void browserPressedUpContentLevel() {
    uiStateService.goUpContentLevel();
  }

  @FXML
  void progressPressedCancel() {
    Platform.runLater(() -> {
      fabricationService.cancel();
      projectService.cancelProjectLoading();
    });
  }

  @FXML
  void fabricationPressedShowSettings(ActionEvent ignored) {
    settingsModalController.launchModal();
  }

  @FXML
  private void browserPressedCreateEntity(ActionEvent ignored) {
    switch (uiStateService.navStateProperty().get()) {
      case ContentLibraryBrowser -> cmdModalController.createLibrary();
      case ContentProgramBrowser -> cmdModalController.createProgram(uiStateService.currentLibraryProperty().get());
      case ContentInstrumentBrowser ->
        cmdModalController.createInstrument(uiStateService.currentLibraryProperty().get());
      case TemplateBrowser -> cmdModalController.createTemplate();
      default -> {
        /* no op */
      }
    }
  }

  /**
   Handle a change in the fabrication state.

   @param value the new value
   */
  private void activateFabricationState(FabricationState value) {
    fabricationActionButton.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, Objects.equals(value, FabricationState.Active));
    fabricationActionButton.pseudoClassStateChanged(FAILED_PSEUDO_CLASS, Objects.equals(value, FabricationState.Failed));
    fabricationActionButton.pseudoClassStateChanged(PENDING_PSEUDO_CLASS, WORK_PENDING_STATES.contains(value));
  }

  /**
   This class is used to display the template name in the ChoiceBox while preserving the underlying ID
   */
  public record TemplateChoice(Template template) {
    @Override
    public String toString() {
      return Objects.nonNull(template) ? template.getName() : "Select...";
    }
  }
}
