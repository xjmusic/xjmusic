package io.xj.gui.services.impl;

import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.TemplateMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.LabService;
import io.xj.gui.services.ProjectDescriptor;
import io.xj.gui.services.ProjectService;
import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.json.JsonProvider;
import io.xj.nexus.json.JsonProviderImpl;
import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.project.ProjectState;
import io.xj.nexus.project.ProjectUpdate;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

@Service
public class ProjectServiceImpl implements ProjectService {
  static final Logger LOG = LoggerFactory.getLogger(ProjectServiceImpl.class);
  private static final String defaultPathPrefix = System.getProperty("user.home") + File.separator + "Documents";
  private final Preferences prefs = Preferences.userNodeForPackage(ProjectServiceImpl.class);
  private final ObjectProperty<ViewMode> viewMode = new SimpleObjectProperty<>(ViewMode.Content);
  private final ObjectProperty<ContentMode> contentMode = new SimpleObjectProperty<>(ContentMode.LibraryBrowser);
  private final ObjectProperty<TemplateMode> templateMode = new SimpleObjectProperty<>(TemplateMode.TemplateBrowser);
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
  private final LabService labService;
  private final ProjectManager projectManager;
  private final ObservableStringValue windowTitle;
  private final JsonProvider jsonProvider;

  public ProjectServiceImpl(
    @Value("${gui.recent.projects.max}") int maxRecentProjects,
    LabService labService,
    ProjectManager projectManager
  ) {
    this.maxRecentProjects = maxRecentProjects;
    this.labService = labService;
    this.projectManager = projectManager;
    this.jsonProvider = new JsonProviderImpl();
    attachPreferenceListeners();
    setAllFromPreferencesOrDefaults();

    projectManager.setOnProgress((progress) -> Platform.runLater(() -> this.progress.set(progress)));
    projectManager.setOnStateChange((state) -> Platform.runLater(() -> this.state.set(state)));
    projectManager.setAudioBaseUrl(labService.hubConfigProperty().get().getAudioBaseUrl());
    labService.hubConfigProperty().addListener((o, ov, value) -> projectManager.setAudioBaseUrl(value.getAudioBaseUrl()));

    state.addListener((o, ov, value) -> {
      if (Objects.equals(value, ProjectState.Standby)) {
        viewMode.set(ViewMode.Content);
        contentMode.set(ContentMode.LibraryBrowser);
        templateMode.set(TemplateMode.TemplateBrowser);
      }
    });

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
  public ObjectProperty<ViewMode> viewModeProperty() {
    return viewMode;
  }

  @Override
  public void closeProject() {
    projectManager.closeProject();
    viewMode.set(ViewMode.Content);
    contentMode.set(ContentMode.LibraryBrowser);
    templateMode.set(TemplateMode.TemplateBrowser);
    state.set(ProjectState.Standby);
  }

  @Override
  public void openProject(String projectFilePath) {
    executeInBackground("Open Project", () -> {
      closeProject();
      if (projectManager.openProjectFromLocalFile(projectFilePath)) {
        projectManager.getProject().ifPresent(project ->
          addToRecentProjects(project, projectManager.getProjectFilename(), projectManager.getPathToProjectFile()));
      } else {
        removeFromRecentProjects(projectFilePath);
      }
    });
  }

  @Override
  public void createProject(String parentPathPrefix, String projectName) {
    if (promptToSkipOverwriteIfExists(parentPathPrefix, projectName)) return;
    executeInBackground("Create Project", () -> {
      closeProject();
      if (projectManager.createProject(parentPathPrefix, projectName)) {
        projectManager.getProject().ifPresent(project ->
          addToRecentProjects(project, projectManager.getProjectFilename(), projectManager.getPathToProjectFile()));
      }
    });
  }

  @Override
  public void cloneFromLabProject(String parentPathPrefix, UUID projectId, String projectName) {
    cloneProject(parentPathPrefix, projectName, () -> projectManager.cloneFromLabProject(
      labService.getHubClientAccess(),
      labService.hubConfigProperty().get().getApiBaseUrl(),
      parentPathPrefix,
      projectId,
      projectName
    ));
  }

  @Override
  public void cloneFromDemoTemplate(String parentPathPrefix, String templateShipKey, String projectName) {
    cloneProject(parentPathPrefix, projectName, () -> projectManager.cloneProjectFromDemoTemplate(
      parentPathPrefix,
      templateShipKey,
      projectName
    ));
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
  public BooleanBinding isStateStandbyProperty() {
    return isStateStandby;
  }

  @Override
  public BooleanBinding isViewModeContentProperty() {
    return viewMode.isEqualTo(ViewMode.Content);
  }

  @Override
  public BooleanBinding isViewModeFabricationProperty() {
    return viewMode.isEqualTo(ViewMode.Fabrication);
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

  @Override
  public void addProjectUpdateListener(ProjectUpdate type, Runnable listener) {
    projectManager.addProjectUpdateListener(type, listener);
  }

  @Override
  public List<Library> getLibraries() {
    return Objects.nonNull(projectManager.getContent()) ?
      projectManager.getContent().getLibraries().stream()
        .filter(library -> !library.getIsDeleted())
        .sorted(Comparator.comparing(Library::getName))
        .toList()
      : new ArrayList<>();
  }

  @Override
  public List<Program> getPrograms() {
    return Objects.nonNull(projectManager.getContent()) ?
      projectManager.getContent().getPrograms().stream()
        .filter(program -> !program.getIsDeleted())
        .sorted(Comparator.comparing(Program::getName))
        .toList()
      : new ArrayList<>();
  }

  @Override
  public List<Instrument> getInstruments() {
    return Objects.nonNull(projectManager.getContent()) ?
      projectManager.getContent().getInstruments().stream()
        .filter(instrument -> !instrument.getIsDeleted())
        .sorted(Comparator.comparing(Instrument::getName))
        .toList()
      : new ArrayList<>();
  }

  @Override
  public List<Template> getTemplates() {
    return Objects.nonNull(projectManager.getContent()) ?
      projectManager.getContent().getTemplates().stream()
        .filter(template -> !template.getIsDeleted())
        .sorted(Comparator.comparing(Template::getName))
        .toList()
      : new ArrayList<>();
  }

  @Override
  public ObjectProperty<ContentMode> contentModeProperty() {
    return contentMode;
  }

  @Override
  public ObjectProperty<TemplateMode> templateModeProperty() {
    return templateMode;
  }

  /**
   Clone a project from a remote source.

   @param parentPathPrefix parent folder
   @param projectName      project name
   @param clone            the clone callable
   */
  private void cloneProject(String parentPathPrefix, String projectName, Callable<Boolean> clone) {
    if (promptToSkipOverwriteIfExists(parentPathPrefix, projectName)) return;
    executeInBackground("Clone Project", () -> {
      try {
        closeProject();
        if (clone.call()) {
          projectManager.getProject().ifPresent(project ->
            addToRecentProjects(project, projectManager.getProjectFilename(), projectManager.getPathToProjectFile()));
        } else {
          removeFromRecentProjects(parentPathPrefix + projectName + ".xj");
        }
      } catch (Exception e) {
        LOG.warn("Failed to clone project", e);
      }
    });
  }

  /**
   Execute a runnable in a background thread. Use JavaFX Platform.runLater(...) as well as spawning an additional thread.

   @param threadName           the name of the thread
   @param failedToCloneProject the runnable
   */
  private void executeInBackground(String threadName, Runnable failedToCloneProject) {
    var thread = new Thread(failedToCloneProject);
    thread.setName(threadName);
    Platform.runLater(thread::start);
  }

  /**
   If the directory already exists then pop up a confirmation dialog

   @param parentPathPrefix parent folder
   @param projectName      project name
   @return true if overwrite confirmed
   */
  private boolean promptToSkipOverwriteIfExists(String parentPathPrefix, String projectName) {
    if (!Files.exists(Path.of(parentPathPrefix + projectName))) {
      return false;
    }
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Project Already Exists");
    alert.setHeaderText("Will update (and may overwrite) existing project");
    alert.setContentText("Are you sure you want to proceed?");

    // Optional: Customize the buttons (optional)
    alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);

    // Show the dialog and capture the result
    var result = alert.showAndWait();
    return result.isEmpty() || result.get() != ButtonType.YES;
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
}
