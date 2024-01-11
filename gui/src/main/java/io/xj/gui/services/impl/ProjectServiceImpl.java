package io.xj.gui.services.impl;

import io.xj.gui.services.LabService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ProjectViewMode;
import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Project;
import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.project.ProjectState;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Objects;
import java.util.UUID;
import java.util.prefs.Preferences;

@Service
public class ProjectServiceImpl implements ProjectService {
  static final Logger LOG = LoggerFactory.getLogger(ProjectServiceImpl.class);
  private static final String defaultPathPrefix = System.getProperty("user.home") + File.separator + "Documents";
  private final Preferences prefs = Preferences.userNodeForPackage(ProjectServiceImpl.class);
  private final ObjectProperty<ProjectViewMode> viewMode = new SimpleObjectProperty<>(ProjectViewMode.CONTENT);
  private final ObservableObjectValue<Project> currentProject;
  private final StringProperty basePathPrefix = new SimpleStringProperty();
  private final DoubleProperty progress = new SimpleDoubleProperty();
  private final ObjectProperty<ProjectState> state = new SimpleObjectProperty<>(ProjectState.Standby);
  private final ObservableStringValue stateText = Bindings.createStringBinding(
    () -> switch (state.get()) {
      case Standby -> "Standby";
      case CreatingFolder -> "Creating Folder";
      case CreatedFolder -> "Created Folder";
      case LoadingContent -> "Loading Content";
      case LoadedContent -> "Loaded Content";
      case LoadingAudio -> String.format("Loading Audio (%.02f%%)", progress.get() * 100);
      case LoadedAudio -> "Loaded Audio";
      case Ready -> "Ready";
      case Saving -> "Saving";
      case Failed -> "Failed";
    },
    state,
    progress);
  private final ObservableBooleanValue isStateLoading =
    Bindings.createBooleanBinding(() -> state.get() == ProjectState.LoadingContent || state.get() == ProjectState.LoadedContent || state.get() == ProjectState.LoadingAudio || state.get() == ProjectState.LoadedAudio, state);
  private final ObservableBooleanValue isStateReady =
    Bindings.createBooleanBinding(() -> state.get() == ProjectState.Ready, state);
  private final LabService labService;
  private final ProjectManager projectManager;
  private final ObservableStringValue windowTitle;

  public ProjectServiceImpl(
    LabService labService,
    ProjectManager projectManager
  ) {
    this.labService = labService;
    this.projectManager = projectManager;
    projectManager.setOnProgress((progress) -> Platform.runLater(() -> this.progress.set(progress)));
    projectManager.setOnStateChange((state) -> Platform.runLater(() -> this.state.set(state)));
    attachPreferenceListeners();
    setAllFromPreferencesOrDefaults();
    currentProject = Bindings.createObjectBinding(() -> {
      if (Objects.equals(state.get(), ProjectState.Ready)) {
        return projectManager.getContent().getProjects().stream().findFirst().orElse(null);
      } else {
        return null;
      }
    }, state);
    windowTitle = Bindings.createStringBinding(
      () -> Objects.nonNull(currentProject.get())
        ? String.format("%s - XJ music workstation", currentProject.get().getName())
        : "XJ music workstation",
      currentProject
    );
  }

  @Override
  public ObjectProperty<ProjectViewMode> viewModeProperty() {
    return viewMode;
  }

  @Override
  public void openProject(String path) {
    LOG.info("Opening project at {}", path);
    // TODO implement projectService.openProject()
  }

  @Override
  public void createProject(String pathPrefix, String name) {
    LOG.info("Creating project {} at {}", name, pathPrefix);
    // TODO implement projectService.newProject()
  }

  @Override
  public void cloneFromLabProject(String pathPrefix, UUID projectId, String name) {
    LOG.info("Cloning from lab project {} ({}) at {}", name, projectId, pathPrefix);
    // TODO implement projectService.cloneProjectFromLab()
  }

  @Override
  public void cloneFromDemoTemplate(String pathPrefix, String templateShipKey, String name) {
    projectManager.setPathPrefix(pathPrefix + name + File.separator);
    projectManager.setAudioBaseUrl(labService.hubConfigProperty().get().getAudioBaseUrl());
    Platform.runLater(() -> projectManager.cloneFromDemoTemplate(templateShipKey, name));
  }

  @Override
  public void saveProject() {
    LOG.info("Saving project");
    // TODO implement projectService.saveProject()
  }

  @Override
  public StringProperty basePathPrefixProperty() {
    return basePathPrefix;
  }

  @Override
  public DoubleProperty progressProperty() {
    return progress;
  }

  @Override
  public ObjectProperty<ProjectState> stateProperty() {
    return state;
  }

  @Override
  public ObservableStringValue stateTextProperty() {
    return stateText;
  }

  @Override
  public ObservableBooleanValue isStateLoadingProperty() {
    return isStateLoading;
  }

  @Override
  public ObservableBooleanValue isStateReadyProperty() {
    return isStateReady;
  }

  @Override
  public HubContent getContent() {
    return projectManager.getContent();
  }

  @Override
  public ObservableStringValue windowTitleProperty() {
    return windowTitle;
  }

  /**
   Attach preference listeners.
   */
  private void attachPreferenceListeners() {
    basePathPrefix.addListener((o, ov, value) -> prefs.put("pathPrefix", value));
  }

  /**
   Set all properties from preferences, else defaults.
   */
  private void setAllFromPreferencesOrDefaults() {
    basePathPrefix.set(prefs.get("pathPrefix", defaultPathPrefix));
  }
}
