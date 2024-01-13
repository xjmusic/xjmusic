package io.xj.gui.services.impl;

import io.xj.gui.services.LabService;
import io.xj.gui.services.ProjectDescriptor;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ProjectViewMode;
import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Project;
import io.xj.nexus.json.JsonProvider;
import io.xj.nexus.json.JsonProviderImpl;
import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.project.ProjectState;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
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
  private final ObservableListValue<ProjectDescriptor> recentProjects = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
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
      case Cancelled -> "Cancelled";
      case Failed -> "Failed";
    },
    state,
    progress);
  private final BooleanBinding isStateLoading =
    state.isEqualTo(ProjectState.LoadingContent)
      .or(state.isEqualTo(ProjectState.LoadingAudio))
      .or(state.isEqualTo(ProjectState.LoadingAudio))
      .or(state.isEqualTo(ProjectState.LoadedAudio));
  private final BooleanBinding isStateReady = state.isEqualTo(ProjectState.Ready);
  private final BooleanBinding isStateStandby = state.isEqualTo(ProjectState.Standby);
  private final int maxRecentProjects;
  private final ProjectManager projectManager;
  private final ObservableStringValue windowTitle;
  private final JsonProvider jsonProvider;

  public ProjectServiceImpl(
    @Value("${gui.recent.projects.max}") int maxRecentProjects,
    LabService labService,
    ProjectManager projectManager
  ) {
    this.maxRecentProjects = maxRecentProjects;
    this.projectManager = projectManager;
    this.jsonProvider = new JsonProviderImpl();
    attachPreferenceListeners();
    setAllFromPreferencesOrDefaults();

    projectManager.setOnProgress((progress) -> Platform.runLater(() -> this.progress.set(progress)));
    projectManager.setOnStateChange((state) -> Platform.runLater(() -> this.state.set(state)));
    projectManager.setAudioBaseUrl(labService.hubConfigProperty().get().getAudioBaseUrl());
    labService.hubConfigProperty().addListener((o, ov, value) -> projectManager.setAudioBaseUrl(value.getAudioBaseUrl()));

    currentProject = Bindings.createObjectBinding(() -> {
      if (Objects.equals(state.get(), ProjectState.Ready)) {
        return projectManager.getProject().orElse(null);
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
  public void openProject(String projectFilePath) {
    new Thread(() -> {
      if (projectManager.openProjectFromLocalFile(projectFilePath)) {
        projectManager.getProject().ifPresent(project ->
          addToRecentProjects(project, projectManager.getProjectFilename(), projectManager.getPathToProjectFile()));
      } else {
        removeFromRecentProjects(projectFilePath);
      }
    }).start();
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
  public void cloneFromDemoTemplate(String parentPathPrefix, String templateShipKey, String projectName) {
    new Thread(() -> {
      if (projectManager.cloneProjectFromDemoTemplate(templateShipKey, parentPathPrefix, projectName)) {
        projectManager.getProject().ifPresent(project ->
          addToRecentProjects(project, projectManager.getProjectFilename(), projectManager.getPathToProjectFile()));
      } else {
        removeFromRecentProjects(parentPathPrefix + projectName + ".xj");
      }
    }).start();
  }

  @Override
  public void saveProject() {
    LOG.info("Saving project");
  }

  @Override
  public void cancelProjectLoading() {
    projectManager.cancelProjectLoading();
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
  public BooleanBinding isStateLoadingProperty() {
    return isStateLoading;
  }

  @Override
  public BooleanBinding isStateReadyProperty() {
    return isStateReady;
  }

  @Override
  public BooleanBinding getIsStateStandby() {
    return isStateStandby;
  }

  @Override
  public HubContent getContent() {
    return projectManager.getContent();
  }

  @Override
  public ObservableStringValue windowTitleProperty() {
    return windowTitle;
  }

  @Override
  public ObservableListValue<ProjectDescriptor> recentProjectsProperty() {
    return recentProjects;
  }

  /**
   Attach preference listeners.
   */
  private void attachPreferenceListeners() {
    basePathPrefix.addListener((o, ov, value) -> prefs.put("pathPrefix", value));
    recentProjects.addListener((o, ov, value) -> {
      try {
        prefs.put("recentProjects", jsonProvider.getMapper().writeValueAsString(value));
      } catch (Exception e) {
        LOG.warn("Failed to serialize recent projects!", e);
      }
    });
  }

  /**
   Set all properties from preferences, else defaults.
   */
  private void setAllFromPreferencesOrDefaults() {
    basePathPrefix.set(prefs.get("pathPrefix", defaultPathPrefix));
    try {
      recentProjects.setAll(jsonProvider.getMapper().readValue(prefs.get("recentProjects", "[]"), ProjectDescriptor[].class));
    } catch (Exception e) {
      LOG.warn("Failed to deserialize recent projects!", e);
    }
  }

  /**
   Add the current project to the list of recent projects.
   */
  private void addToRecentProjects(Project project, String projectFilename, String projectFilePath) {
    var descriptor = new ProjectDescriptor(project, projectFilename, projectFilePath);
    this.recentProjects.get().removeIf(existing -> Objects.equals(existing.projectFilePath(), descriptor.projectFilePath()));
    this.recentProjects.get().add(0, descriptor);
    if (this.recentProjects.get().size() > maxRecentProjects) {
      this.recentProjects.get().remove(maxRecentProjects);
    }
  }

  /**
   Remove the current project from the list of recent projects.

   @param projectFilePath the path to the project file
   */
  private void removeFromRecentProjects(String projectFilePath) {
    this.recentProjects.get().removeIf(existing -> Objects.equals(existing.projectFilePath(), projectFilePath));
  }

  public BooleanBinding isStateStandbyProperty() {
    return isStateStandby;
  }
}
