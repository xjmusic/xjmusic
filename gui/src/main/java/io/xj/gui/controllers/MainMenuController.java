// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.WorkstationGuiFxApplication;
import io.xj.gui.controllers.fabrication.FabricationSettingsModalController;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.GuideService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.LabState;
import io.xj.gui.services.ProjectDescriptor;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.ProjectUtils;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static io.xj.gui.services.UIStateService.ACTIVE_PSEUDO_CLASS;
import static io.xj.gui.services.UIStateService.FAILED_PSEUDO_CLASS;
import static io.xj.gui.services.UIStateService.LAB_FAILED_STATES;
import static io.xj.gui.services.UIStateService.LAB_PENDING_STATES;
import static io.xj.gui.services.UIStateService.PENDING_PSEUDO_CLASS;

@Service
public class MainMenuController extends MenuBar implements ReadyAfterBootController {
  final static String DEBUG = "DEBUG";
  final static String INFO = "INFO";
  final static String WARN = "WARN";
  final static String ERROR = "ERROR";
  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final ThemeService themeService;
  final GuideService guideService;
  final UIStateService uiStateService;
  final LabService labService;
  private final ProjectCreationModalController projectCreationModalController;
  private final ProjectService projectService;
  final UIStateService guiService;
  final FabricationSettingsModalController fabricationSettingsModalController;
  final MainAboutModalController mainAboutModalController;
  final MainLabAuthenticationModalController mainLabAuthenticationModalController;

  @FXML
  protected AnchorPane container;

  @FXML
  protected MenuItem itemProjectClose;

  @FXML
  protected MenuItem itemProjectSave;

  @FXML
  protected MenuItem itemFabricationMainAction;

  @FXML
  protected Menu menuOpenRecent;

  @FXML
  protected CheckMenuItem checkboxFabricationFollow;

  @FXML
  protected MenuItem itemOpenFabricationSettings;

  @FXML
  protected CheckMenuItem checkboxDarkTheme;

  @FXML
  protected CheckMenuItem checkboxShowLogs;

  @FXML
  protected CheckMenuItem checkboxTailLogs;

  @FXML
  protected Button mainMenuButtonLab;

  @FXML
  protected Label labelLabStatus;

  @FXML
  protected RadioMenuItem logLevelDebug;

  @FXML
  protected RadioMenuItem logLevelInfo;

  @FXML
  protected RadioMenuItem logLevelWarn;

  @FXML
  protected RadioMenuItem logLevelError;

  @FXML
  ToggleGroup logLevelToggleGroup;

  public MainMenuController(
    ConfigurableApplicationContext ac,
    FabricationService fabricationService,
    FabricationSettingsModalController fabricationSettingsModalController,
    GuideService guideService,
    LabService labService,
    MainAboutModalController mainAboutModalController,
    MainLabAuthenticationModalController mainLabAuthenticationModalController,
    ProjectCreationModalController projectCreationModalController,
    ProjectService projectService,
    ThemeService themeService,
    UIStateService guiService,
    UIStateService uiStateService
  ) {
    this.ac = ac;
    this.fabricationService = fabricationService;
    this.fabricationSettingsModalController = fabricationSettingsModalController;
    this.projectCreationModalController = projectCreationModalController;
    this.projectService = projectService;
    this.guiService = guiService;
    this.guideService = guideService;
    this.labService = labService;
    this.mainAboutModalController = mainAboutModalController;
    this.mainLabAuthenticationModalController = mainLabAuthenticationModalController;
    this.themeService = themeService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    checkboxFabricationFollow.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());
    checkboxFabricationFollow.setAccelerator(computeFabricationFollowButtonAccelerator());

    checkboxTailLogs.disableProperty().bind(uiStateService.logsVisibleProperty().not());

    itemFabricationMainAction.setAccelerator(computeMainActionButtonAccelerator());
    itemFabricationMainAction.textProperty().bind(fabricationService.mainActionButtonTextProperty().map(this::addLeadingUnderscore));

    itemOpenFabricationSettings.disableProperty().bind(guiService.isFabricationSettingsDisabledProperty());

    checkboxTailLogs.selectedProperty().bindBidirectional(uiStateService.logsTailingProperty());
    checkboxShowLogs.selectedProperty().bindBidirectional(uiStateService.logsVisibleProperty());

    themeService.isDarkThemeProperty().bindBidirectional(checkboxDarkTheme.selectedProperty());

    logLevelToggleGroup = new ToggleGroup();
    logLevelDebug.setToggleGroup(logLevelToggleGroup);
    logLevelInfo.setToggleGroup(logLevelToggleGroup);
    logLevelWarn.setToggleGroup(logLevelToggleGroup);
    logLevelError.setToggleGroup(logLevelToggleGroup);
    switch (uiStateService.logLevelProperty().getValue()) {
      case DEBUG -> logLevelDebug.setSelected(true);
      case INFO -> logLevelInfo.setSelected(true);
      case WARN -> logLevelWarn.setSelected(true);
      case ERROR -> logLevelError.setSelected(true);
    }

    var hasNoProject = uiStateService.hasCurrentProjectProperty().not();
    itemProjectClose.disableProperty().bind(hasNoProject);
    itemProjectSave.disableProperty().bind(hasNoProject);

    projectService.recentProjectsProperty().addListener((ChangeListener<? super ObservableList<ProjectDescriptor>>) (o, ov, value) -> updateRecentProjectsMenu());
    updateRecentProjectsMenu();

    labService.stateProperty().addListener((o, ov, value) -> updateLabButtonState(value));
    updateLabButtonState(labService.stateProperty().get());

    labelLabStatus.textProperty().bind(labService.stateProperty().map(Enum::toString));
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @FXML
  protected void onQuit() {
    WorkstationGuiFxApplication.exit(ac);
  }

  @FXML
  protected void onLaunchUserGuide() {
    guideService.launchGuideInBrowser();
  }

  @FXML
  protected void handleAbout() {
    mainAboutModalController.launchModal();
  }

  @FXML
  protected void handleProjectNew() {
    projectCreationModalController.setMode(ProjectCreationMode.NEW_PROJECT);
    projectCreationModalController.launchModal();
  }

  @FXML
  protected void handleProjectOpen() {
    var projectFilePath = ProjectUtils.chooseXJProjectFile(
      container.getScene().getWindow(), "Choose project", projectService.basePathPrefixProperty().getValue()
    );
    if (Objects.nonNull(projectFilePath)) {
      projectService.openProject(projectFilePath);
    }
  }

  @FXML
  protected void handleProjectClose() {
    projectService.closeProject();
  }

  @FXML
  protected void handleProjectClone() {
    projectCreationModalController.setMode(ProjectCreationMode.CLONE_PROJECT);
    projectCreationModalController.launchModal();
  }

  @FXML
  protected void handleProjectSave() {
    projectService.saveProject();
  }

  @FXML
  protected void handleOpenFabricationSettings() {
    fabricationSettingsModalController.launchModal();
  }

  @FXML
  public void handleFabricationMainAction(ActionEvent ignored) {
    fabricationService.handleMainAction();
  }

  @FXML
  public void handleSetLogLevel(ActionEvent ignored) {
    uiStateService.logLevelProperty().set(((RadioMenuItem) logLevelToggleGroup.getSelectedToggle()).getText());
  }

  @FXML
  public void handleButtonLabPressed(ActionEvent ignored) {
    mainLabAuthenticationModalController.launchModal();
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
   Update the recent projects menu.
   */
  private void updateRecentProjectsMenu() {
    menuOpenRecent.getItems().clear();
    for (ProjectDescriptor project : projectService.recentProjectsProperty().getValue()) {
      MenuItem menuItem = new MenuItem(project.projectFilename());
      menuItem.setOnAction(event -> projectService.openProject(project.projectFilePath()));
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
}
