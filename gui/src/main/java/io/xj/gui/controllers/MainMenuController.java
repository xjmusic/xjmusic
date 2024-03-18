// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.ProjectController;
import io.xj.gui.WorkstationGuiFxApplication;
import io.xj.gui.controllers.fabrication.FabricationSettingsModalController;
import io.xj.gui.types.ViewMode;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.LabState;
import io.xj.gui.services.ProjectDescriptor;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.SupportService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.ProjectUtils;
import io.xj.gui.utils.UiUtils;
import io.xj.nexus.work.FabricationState;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static io.xj.gui.services.UIStateService.ACTIVE_PSEUDO_CLASS;
import static io.xj.gui.services.UIStateService.FABRICATION_FAILED_STATES;
import static io.xj.gui.services.UIStateService.FABRICATION_PENDING_STATES;
import static io.xj.gui.services.UIStateService.FAILED_PSEUDO_CLASS;
import static io.xj.gui.services.UIStateService.LAB_FAILED_STATES;
import static io.xj.gui.services.UIStateService.LAB_PENDING_STATES;
import static io.xj.gui.services.UIStateService.PENDING_PSEUDO_CLASS;

@Service
public class MainMenuController extends ProjectController {
  private final static String DEBUG = "DEBUG";
  private final static String INFO = "INFO";
  private final static String WARN = "WARN";
  private final static String ERROR = "ERROR";
  private final FabricationService fabricationService;
  private final SupportService supportService;
  private final LabService labService;
  private final ProjectCreationModalController projectCreationModalController;
  private final UIStateService guiService;
  private final FabricationSettingsModalController fabricationSettingsModalController;
  private final MainAboutModalController mainAboutModalController;
  private final MainLabAuthenticationModalController mainLabAuthenticationModalController;

  @FXML
  AnchorPane container;

  @FXML
  MenuItem itemProjectClose;

  @FXML
  MenuItem itemProjectSave;

  @FXML
  MenuItem itemProjectPush;

  @FXML
  MenuItem itemProjectCleanup;

  @FXML
  MenuItem itemFabricationMainAction;

  @FXML
  Menu menuOpenRecent;

  @FXML
  CheckMenuItem checkboxFabricationFollow;

  @FXML
  Menu menuFabrication;

  @FXML
  MenuItem itemOpenFabricationSettings;

  @FXML
  CheckMenuItem checkboxShowLogs;

  @FXML
  CheckMenuItem checkboxTailLogs;

  @FXML
  Button mainMenuButtonLab;

  @FXML
  Label labelLabStatus;

  @FXML
  RadioMenuItem logLevelDebug;

  @FXML
  RadioMenuItem logLevelInfo;

  @FXML
  RadioMenuItem logLevelWarn;

  @FXML
  RadioMenuItem logLevelError;

  @FXML
  ToggleGroup menuViewModeToggleGroup;
  @FXML
  RadioMenuItem menuViewModeContent;
  @FXML
  RadioMenuItem menuViewModeTemplates;
  @FXML
  RadioMenuItem menuViewModeFabrication;

  @FXML
  StackPane labFeatureContainer;
  @FXML
  ToggleGroup buttonViewModeToggleGroup;
  @FXML
  ToggleButton buttonViewModeContent;
  @FXML
  ToggleButton buttonViewModeTemplates;
  @FXML
  ToggleButton buttonViewModeFabrication;

  @FXML
  ToggleGroup logLevelToggleGroup;


  public MainMenuController(
    @Value("classpath:/views/main-menu.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    FabricationService fabricationService,
    FabricationSettingsModalController fabricationSettingsModalController,
    SupportService supportService,
    LabService labService,
    MainAboutModalController mainAboutModalController,
    MainLabAuthenticationModalController mainLabAuthenticationModalController,
    ProjectCreationModalController projectCreationModalController,
    ProjectService projectService,
    UIStateService guiService,
    UIStateService uiStateService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.fabricationService = fabricationService;
    this.fabricationSettingsModalController = fabricationSettingsModalController;
    this.projectCreationModalController = projectCreationModalController;
    this.guiService = guiService;
    this.supportService = supportService;
    this.labService = labService;
    this.mainAboutModalController = mainAboutModalController;
    this.mainLabAuthenticationModalController = mainLabAuthenticationModalController;
  }

  @Override
  public void onStageReady() {
    checkboxFabricationFollow.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());
    checkboxFabricationFollow.setAccelerator(computeFabricationFollowButtonAccelerator());

    checkboxTailLogs.disableProperty().bind(uiStateService.logsVisibleProperty().not());

    itemFabricationMainAction.setAccelerator(computeMainActionButtonAccelerator());
    itemFabricationMainAction.textProperty().bind(fabricationService.mainActionButtonTextProperty().map(this::addLeadingUnderscore));

    itemOpenFabricationSettings.disableProperty().bind(guiService.isFabricationSettingsDisabledProperty());
    itemOpenFabricationSettings.setAccelerator(computeFabricationSettingsAccelerator());

    checkboxTailLogs.selectedProperty().bindBidirectional(uiStateService.logsTailingProperty());
    checkboxShowLogs.selectedProperty().bindBidirectional(uiStateService.logsVisibleProperty());

    switch (uiStateService.logLevelProperty().getValue()) {
      case DEBUG -> logLevelDebug.setSelected(true);
      case INFO -> logLevelInfo.setSelected(true);
      case WARN -> logLevelWarn.setSelected(true);
      case ERROR -> logLevelError.setSelected(true);
    }

    var hasNoProject = uiStateService.hasCurrentProjectProperty().not();
    itemProjectClose.disableProperty().bind(hasNoProject);
    itemProjectSave.disableProperty().bind(hasNoProject.or(projectService.isModifiedProperty().not()));
    itemProjectPush.disableProperty().bind(hasNoProject.or(labService.isAuthenticated().not()).or(projectService.isDemoProjectProperty()));
    itemProjectCleanup.disableProperty().bind(hasNoProject);

    projectService.recentProjectsProperty().addListener((ChangeListener<? super ObservableList<ProjectDescriptor>>) (o, ov, value) -> updateRecentProjectsMenu());
    updateRecentProjectsMenu();

    labService.stateProperty().addListener((o, ov, value) -> updateLabButtonState(value));
    updateLabButtonState(labService.stateProperty().get());

    labelLabStatus.textProperty().bind(labService.stateProperty().map(Enum::toString));

    setupViewModeToggle(menuViewModeToggleGroup, menuViewModeContent, menuViewModeTemplates, menuViewModeFabrication);
    menuViewModeContent.disableProperty().bind(projectService.isStateReadyProperty().not());
    menuViewModeTemplates.disableProperty().bind(projectService.isStateReadyProperty().not());
    menuViewModeFabrication.disableProperty().bind(projectService.isStateReadyProperty().not());

    setupViewModeToggle(buttonViewModeToggleGroup, buttonViewModeContent, buttonViewModeTemplates, buttonViewModeFabrication);
    buttonViewModeContent.disableProperty().bind(projectService.isStateReadyProperty().not());
    buttonViewModeTemplates.disableProperty().bind(projectService.isStateReadyProperty().not());
    buttonViewModeFabrication.disableProperty().bind(projectService.isStateReadyProperty().not());
    menuFabrication.disableProperty().bind(projectService.isStateReadyProperty().not());

    UiUtils.toggleGroupPreventDeselect(buttonViewModeToggleGroup);

    fabricationService.stateProperty().addListener((o, ov, value) -> updateFabricationButtonState(value));
    updateFabricationButtonState(fabricationService.stateProperty().get());

    labFeatureContainer.translateXProperty().bind(container.widthProperty().subtract(labFeatureContainer.widthProperty()).divide(2));
    labFeatureContainer.visibleProperty().bind(uiStateService.isLabFeatureEnabledProperty());
    labFeatureContainer.managedProperty().bind(uiStateService.isLabFeatureEnabledProperty());

    itemProjectPush.visibleProperty().bind(uiStateService.isLabFeatureEnabledProperty());
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @FXML
  void onQuit() {
    projectService.promptToSaveChanges(() -> WorkstationGuiFxApplication.exit(ac));
  }

  @FXML
  void onLaunchUserGuide() {
    supportService.launchGuideInBrowser();
  }

  @FXML
  void onLaunchDiscord() {
    supportService.launchDiscordInBrowser();
  }

  @FXML
  void handleAbout() {
    mainAboutModalController.launchModal();
  }

  @FXML
  void handleProjectNew() {
    projectCreationModalController.setMode(ProjectCreationMode.NEW_PROJECT);
    projectCreationModalController.launchModal();
  }

  @FXML
  void handleProjectOpen() {
    projectService.promptToSaveChanges(() -> {
      var projectFilePath = ProjectUtils.chooseXJProjectFile(
        container.getScene().getWindow(), "Choose project", projectService.basePathPrefixProperty().getValue()
      );
      if (Objects.nonNull(projectFilePath)) {
        fabricationService.cancel();
        Platform.runLater(() -> projectService.openProject(projectFilePath));
      }
    });
  }

  @FXML
  void handleProjectClose() {
    fabricationService.cancel();
    Platform.runLater(() -> projectService.closeProject(null));
  }

  @FXML
  void handleProjectClone() {
    projectCreationModalController.setMode(ProjectCreationMode.CLONE_PROJECT);
    projectCreationModalController.launchModal();
  }

  @FXML
  void handleProjectSave() {
    projectService.saveProject(null);
  }

  @FXML
  void handleProjectPush() {
    projectService.pushProject();
  }

  @FXML
  void handleProjectCleanup() {
    projectService.cleanupProject();
  }

  @FXML
  void handleOpenFabricationSettings() {
    fabricationSettingsModalController.launchModal();
  }

  @FXML
  void handleFabricationMainAction(ActionEvent ignored) {
    fabricationService.handleMainAction();
  }

  @FXML
  void handleSetLogLevel(ActionEvent ignored) {
    uiStateService.logLevelProperty().set(((RadioMenuItem) logLevelToggleGroup.getSelectedToggle()).getText());
  }

  @FXML
  void handleButtonLabPressed(ActionEvent ignored) {
    mainLabAuthenticationModalController.launchModal();
  }

  /**
   Set up the view mode toggle.

   @param toggleGroup       the toggle group
   @param toggleContent     the content toggle
   @param toggleTemplates   the templates toggle
   @param toggleFabrication the fabrication toggle
   */
  private void setupViewModeToggle(
    ToggleGroup toggleGroup,
    Toggle toggleContent,
    Toggle toggleTemplates,
    Toggle toggleFabrication
  ) {
    activateViewModeToggle(toggleContent, toggleTemplates, toggleFabrication, uiStateService.viewModeProperty().getValue());
    uiStateService.viewModeProperty().addListener((o, ov, value) -> activateViewModeToggle(toggleContent, toggleTemplates, toggleFabrication, value));
    toggleGroup.selectedToggleProperty().addListener((o, ov, value) -> {
      if (Objects.equals(value, toggleContent)) {
        uiStateService.navigateTo(Route.getContentDefault());
      } else if (Objects.equals(value, toggleTemplates)) {
        uiStateService.viewModeProperty().set(ViewMode.Templates);
      } else if (Objects.equals(value, toggleFabrication)) {
        uiStateService.viewModeProperty().set(ViewMode.Fabrication);
      }
    });
  }

  /**
   Activate the toggle in a group corresponding to the given view mode

   @param toggleContent     the content toggle
   @param toggleTemplates   the templates toggle
   @param toggleFabrication the fabrication toggle
   @param value             the view mode
   */
  private void activateViewModeToggle(Toggle toggleContent, Toggle toggleTemplates, Toggle toggleFabrication, ViewMode value) {
    switch (value) {
      case Content -> toggleContent.setSelected(true);
      case Templates -> toggleTemplates.setSelected(true);
      case Fabrication -> toggleFabrication.setSelected(true);
    }
  }

  /**
   Update the state of the lab button.

   @param value the new state
   */
  private void updateLabButtonState(LabState value) {
    mainMenuButtonLab.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, Objects.equals(value, LabState.Authenticated));
    mainMenuButtonLab.pseudoClassStateChanged(FAILED_PSEUDO_CLASS, LAB_FAILED_STATES.contains(value));
    mainMenuButtonLab.pseudoClassStateChanged(PENDING_PSEUDO_CLASS, LAB_PENDING_STATES.contains(value));
  }

  /**
   Update the state of the fabrication button.

   @param value the new state
   */
  private void updateFabricationButtonState(FabricationState value) {
    buttonViewModeFabrication.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, Objects.equals(value, FabricationState.Active));
    buttonViewModeFabrication.pseudoClassStateChanged(FAILED_PSEUDO_CLASS, FABRICATION_FAILED_STATES.contains(value));
    buttonViewModeFabrication.pseudoClassStateChanged(PENDING_PSEUDO_CLASS, FABRICATION_PENDING_STATES.contains(value));
  }

  /**
   Update the recent project menu.
   */
  private void updateRecentProjectsMenu() {
    menuOpenRecent.getItems().clear();
    for (ProjectDescriptor project : projectService.recentProjectsProperty().getValue()) {
      MenuItem menuItem = new MenuItem(project.projectFilename());
      menuItem.setOnAction(event -> {
        fabricationService.cancel();
        Platform.runLater(() -> projectService.openProject(project.projectFilePath()));
      });
      menuOpenRecent.getItems().add(menuItem);
    }
  }

  /**
   Add a leading underscore to a string.

   @param s the string
   @return the string with a leading underscore
   */
  private String addLeadingUnderscore(String s) {
    return String.format("_%s", s);
  }

  /**
   Compute the accelerator for the main action button.
   Depending on the platform, it will be either SHORTCUT+SPACE or SHORTCUT+B (on Mac because of conflict).

   @return the accelerator
   */
  private KeyCombination computeMainActionButtonAccelerator() {
    return KeyCombination.valueOf("SHORTCUT+" + (System.getProperty("os.name").toLowerCase().contains("mac") ? "B" : "SPACE"));
  }

  /**
   Compute the accelerator for the fabricator follow toggle button.
   Depending on the platform, it will be either SHORTCUT+ALT+SPACE or SHORTCUT+ALT+B (on Mac because of conflict).

   @return the accelerator
   */
  private KeyCombination computeFabricationFollowButtonAccelerator() {
    return KeyCombination.valueOf("SHORTCUT+ALT+" + (System.getProperty("os.name").toLowerCase().contains("mac") ? "B" : "SPACE"));
  }

  /**
   Compute the accelerator for the fabricator settings button.
   Depending on the platform, it will be either SHORTCUT+SHIFT+ALT+SPACE or SHORTCUT+SHIFT+ALT+B (on Mac because of conflict).

   @return the accelerator
   */
  private KeyCombination computeFabricationSettingsAccelerator() {
    return KeyCombination.valueOf("SHORTCUT+SHIFT+ALT+" + (System.getProperty("os.name").toLowerCase().contains("mac") ? "B" : "SPACE"));
  }
}
