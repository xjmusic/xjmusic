// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.engine.work.FabricationState;
import io.xj.gui.ProjectController;
import io.xj.gui.WorkstationFxApplication;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectDescriptor;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.SupportService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.types.Route;
import io.xj.gui.utils.ProjectUtils;
import io.xj.gui.utils.UiUtils;
import jakarta.annotation.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static io.xj.gui.services.UIStateService.ACTIVE_PSEUDO_CLASS;
import static io.xj.gui.services.UIStateService.FABRICATION_FAILED_STATES;
import static io.xj.gui.services.UIStateService.FABRICATION_PENDING_STATES;
import static io.xj.gui.services.UIStateService.FAILED_PSEUDO_CLASS;
import static io.xj.gui.services.UIStateService.PENDING_PSEUDO_CLASS;

@Service
public class MainMenuController extends ProjectController {
  static final Logger LOG = LoggerFactory.getLogger(MainMenuController.class);
  private final static String DEBUG = "DEBUG";
  private final static String INFO = "INFO";
  private final static String WARN = "WARN";
  private final static String ERROR = "ERROR";
  private final FabricationService fabricationService;
  private final SupportService supportService;
  private final ProjectCreationModalController projectCreationModalController;
  private final SettingsModalController settingsModalController;
  private final MainAboutModalController mainAboutModalController;
  private final static int BUTTON_BUILD_IMAGE_ANIMATION_DELAY_MILLIS = 333;

  @FXML
  AnchorPane container;

  @FXML
  MenuItem itemProjectClose;

  @FXML
  MenuItem itemProjectSave;

  @FXML
  MenuItem itemProjectSaveAs;

  @FXML
  MenuItem itemFabricationMainAction;

  @FXML
  Menu menuOpenRecent;

  @FXML
  CheckMenuItem checkboxFabricationFollow;

  @FXML
  Menu menuFabrication;

  @FXML
  MenuItem itemOpenSettings;

  @FXML
  CheckMenuItem checkboxShowLogs;

  @FXML
  CheckMenuItem checkboxTailLogs;

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
  Button buttonBuild;
  @FXML
  ImageView buttonBuildImageView;
  @Nullable
  Timeline buttonBuildImageTimeline;
  @FXML
  Button buttonBuildSettings;

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
    SettingsModalController settingsModalController,
    SupportService supportService,
    MainAboutModalController mainAboutModalController,
    ProjectCreationModalController projectCreationModalController,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.fabricationService = fabricationService;
    this.settingsModalController = settingsModalController;
    this.projectCreationModalController = projectCreationModalController;
    this.supportService = supportService;
    this.mainAboutModalController = mainAboutModalController;
  }

  @Override
  public void onStageReady() {
    checkboxFabricationFollow.selectedProperty().bindBidirectional(fabricationService.followPlaybackProperty());
    checkboxFabricationFollow.setAccelerator(computeFabricationFollowButtonAccelerator());

    checkboxTailLogs.disableProperty().bind(uiStateService.logsVisibleProperty().not());

    itemFabricationMainAction.setAccelerator(computeMainActionButtonAccelerator());
    itemFabricationMainAction.textProperty().bind(fabricationService.mainActionButtonTextProperty());

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
    itemProjectSaveAs.disableProperty().bind(hasNoProject);

    projectService.recentProjectsProperty().addListener((ChangeListener<? super ObservableList<ProjectDescriptor>>) (o, ov, value) -> updateRecentProjectsMenu());
    updateRecentProjectsMenu();

    buttonBuild.disableProperty().bind(projectService.isStateReadyProperty().not());
    buttonBuildSettings.disableProperty().bind(projectService.isStateReadyProperty().not());
    projectService.buildStateProperty().addListener((o, ov, value) -> {
      switch (value) {
        case Standby -> setupButtonBuild("Build the project", List.of("icons/build.png"));
        case Dirty -> setupButtonBuild("Project modified. Build to apply changes", List.of("icons/build-warn.png"));
        case Clean -> setupButtonBuild("Build is up-to-date.", List.of("icons/build-ok.png"));
        case Building -> {
          setupButtonBuild("Building the project", List.of("icons/build-spin1.png","icons/build-spin2.png","icons/build-spin3.png"));
        }
        case Failed -> setupButtonBuild("Build failed. Click to retry.", List.of("icons/build-fail.png"));
      }
    });

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
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @FXML
  void onQuit() {
    projectService.promptToSaveChanges(() -> WorkstationFxApplication.exit(ac));
  }

  @FXML
  void onLaunchUserGuide() {
    supportService.launchGuideInBrowser();
  }

  @FXML
  void onLaunchTutorialVideo() {
    supportService.launchTutorialInBrowser();
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
  void handleNavigateBack() {
    uiStateService.navigateBack();
  }

  @FXML
  void handleNavigateForward() {
    uiStateService.navigateForward();
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
        container.getScene().getWindow(), "Choose project", projectService.projectsPathPrefixProperty().getValue()
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
  void handleProjectDemos() {
    projectCreationModalController.setMode(ProjectCreationMode.DEMO_PROJECT);
    projectCreationModalController.launchModal();
  }

  @FXML
  void handleProjectSave() {
    projectService.saveProject(null);
  }

  @FXML
  void handleProjectSaveAs() {
    projectCreationModalController.setMode(ProjectCreationMode.SAVE_AS_PROJECT);
    projectCreationModalController.launchModal();
  }

  @FXML
  void handleOpenSettings() {
    settingsModalController.launchModal();
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
  void handleBuild() {
    projectService.buildProject();
  }

  @FXML
  void handleBuildSettings() {
    settingsModalController.launchModalWithBuildSettings();
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
    activateViewModeToggle(toggleContent, toggleTemplates, toggleFabrication, uiStateService.navStateProperty().getValue());
    uiStateService.navStateProperty().addListener((o, ov, value) -> activateViewModeToggle(toggleContent, toggleTemplates, toggleFabrication, value));
    toggleGroup.selectedToggleProperty().addListener((o, ov, value) -> {
      if (Objects.equals(value, toggleContent)) {
        if (!uiStateService.navStateProperty().get().isContent())
          uiStateService.navigateTo(Route.ContentLibraryBrowser);
      } else if (Objects.equals(value, toggleTemplates)) {
        if (!uiStateService.navStateProperty().get().isTemplate())
          uiStateService.navigateTo(Route.TemplateBrowser);
      } else if (Objects.equals(value, toggleFabrication)) {
        if (!uiStateService.navStateProperty().get().isFabrication())
          uiStateService.navigateTo(Route.FabricationTimeline);
      }
    });
  }

  /**
   Activate the toggle in a group corresponding to the given view mode

   @param toggleContent     the content toggle
   @param toggleTemplates   the templates toggle
   @param toggleFabrication the fabrication toggle
   @param route             the view mode
   */
  private void activateViewModeToggle(Toggle toggleContent, Toggle toggleTemplates, Toggle toggleFabrication, Route route) {
    if (route.isContent()) {
      toggleContent.setSelected(true);
    } else if (route.isTemplate()) {
      toggleTemplates.setSelected(true);
    } else if (route.isFabrication()) {
      toggleFabrication.setSelected(true);
    }
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
   Set up the build button.

   @param tooltipText  the tooltip text
   @param imageSources a list of image URLs -- if there is one, just set it. if there are more, animate them
   */
  private void setupButtonBuild(String tooltipText, List<String> imageSources) {
    buttonBuild.setTooltip(new Tooltip(tooltipText));
    if (imageSources.size() > 1) {
      AtomicInteger imageIndex = new AtomicInteger(0);
      buttonBuildImageTimeline = new Timeline(new KeyFrame(Duration.millis(BUTTON_BUILD_IMAGE_ANIMATION_DELAY_MILLIS), event -> {
        buttonBuildImageView.setImage(new Image(imageSources.get(imageIndex.getAndIncrement() % imageSources.size())));
      }));
      buttonBuildImageTimeline.setCycleCount(Timeline.INDEFINITE);
      buttonBuildImageTimeline.play();

    } else {
      if (Objects.nonNull(buttonBuildImageTimeline))
        buttonBuildImageTimeline.stop();
    }

    // Set the first image
    setupButtonBuildImage(imageSources.get(0));
  }

  private void setupButtonBuildImage(String imageSource) {
    try {
      buttonBuildImageView.setImage(new Image(imageSource));
      buttonBuild.setGraphic(buttonBuildImageView);
    } catch (Exception e) {
      LOG.error("Failed to setup build button with image source \"{}\"", imageSource, e);
    }
  }
}
