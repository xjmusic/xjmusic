package io.xj.gui.services.impl;

import io.xj.gui.services.LabService;
import io.xj.gui.services.ProjectDescriptor;
import io.xj.gui.services.ProjectService;
import io.xj.hub.HubContent;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.entity.EntityFactory;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.entity.EntityUtils;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

@Service
public class ProjectServiceImpl implements ProjectService {
  private static final Logger LOG = LoggerFactory.getLogger(ProjectServiceImpl.class);
  private static final String defaultPathPrefix = System.getProperty("user.home") + File.separator + "Documents";
  private static final Collection<ProjectState> PROJECT_LOADING_STATES = Set.of(
    ProjectState.LoadingContent,
    ProjectState.LoadedContent,
    ProjectState.LoadingAudio,
    ProjectState.LoadedAudio
  );
  private final Preferences prefs = Preferences.userNodeForPackage(ProjectServiceImpl.class);
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
  private final BooleanBinding isStateLoading = Bindings.createBooleanBinding(
    () -> PROJECT_LOADING_STATES.contains(state.get()),
    state);
  private final BooleanBinding isStateReady = state.isEqualTo(ProjectState.Ready);
  private final BooleanBinding isStateStandby = state.isEqualTo(ProjectState.Standby);
  private final int maxRecentProjects;
  private final LabService labService;
  private final ProjectManager projectManager;
  private final EntityFactory entityFactory;
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
    this.entityFactory = new EntityFactoryImpl(jsonProvider);
    attachPreferenceListeners();
    setAllFromPreferencesOrDefaults();

    projectManager.setOnProgress((progress) -> Platform.runLater(() -> this.progress.set(progress)));
    projectManager.setOnStateChange((state) -> Platform.runLater(() -> this.state.set(state)));

    currentProject = Bindings.createObjectBinding(() -> {
      if (Objects.equals(state.get(), ProjectState.Ready)) {
        return projectManager.getProject().orElse(null);
      } else {
        return null;
      }
    }, state);
  }

  @Override
  public void closeProject() {
    projectManager.closeProject();
    state.set(ProjectState.Standby);
  }

  @Override
  public void openProject(String projectFilePath) {
    closeProject();
    executeInBackground("Open Project", () -> {
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
    closeProject();
    if (promptToSkipOverwriteIfExists(parentPathPrefix, projectName)) return;
    executeInBackground("Create Project", () -> {
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
      labService.hubConfigProperty().get().getAudioBaseUrl(),
      parentPathPrefix,
      projectId,
      projectName
    ));
  }

  @Override
  public void cloneFromDemoTemplate(String parentPathPrefix, String templateShipKey, String projectName) {
    cloneProject(parentPathPrefix, projectName, () -> projectManager.cloneProjectFromDemoTemplate(
      labService.hubConfigProperty().get().getAudioBaseUrl(),
      parentPathPrefix,
      templateShipKey,
      projectName
    ));
  }

  @Override
  public void saveProject() {
    LOG.info("Will save project");
    executeInBackground("Save Project", projectManager::saveProject);
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
  public HubContent getContent() {
    return projectManager.getContent();
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
  public ObservableObjectValue<Project> currentProjectProperty() {
    return currentProject;
  }

  @Override
  public void deleteTemplate(Template template) {
    projectManager.getContent().getTemplates().removeIf(binding -> Objects.equals(binding.getId(), template.getId()));
    projectManager.notifyProjectUpdateListeners(ProjectUpdate.Templates);
    LOG.info("Deleted template \"{}\"", template.getName());
  }

  @Override
  public void deleteTemplateBinding(TemplateBinding binding) {
    projectManager.getContent().getTemplateBindings().removeIf(templateBinding -> Objects.equals(templateBinding.getId(), binding.getId()));
    projectManager.notifyProjectUpdateListeners(ProjectUpdate.TemplateBindings);
    LOG.info("Deleted {} template binding", binding.getType());
  }

  @Override
  public void deleteLibrary(Library library) {
    projectManager.getContent().getLibraries().removeIf(binding -> Objects.equals(binding.getId(), library.getId()));
    projectManager.notifyProjectUpdateListeners(ProjectUpdate.Libraries);
    LOG.info("Deleted library \"{}\"", library.getName());
  }

  @Override
  public void deleteProgram(Program program) {
    projectManager.getContent().getPrograms().removeIf(binding -> Objects.equals(binding.getId(), program.getId()));
    projectManager.notifyProjectUpdateListeners(ProjectUpdate.Programs);
    LOG.info("Deleted program \"{}\"", program.getName());
  }

  @Override
  public void deleteInstrument(Instrument instrument) {
    projectManager.getContent().getInstruments().removeIf(binding -> Objects.equals(binding.getId(), instrument.getId()));
    projectManager.notifyProjectUpdateListeners(ProjectUpdate.Instruments);
    LOG.info("Deleted instrument \"{}\"", instrument.getName());
  }

  @Override
  public Template createTemplate(String name) throws Exception {
    var template = projectManager.createTemplate(name);
    projectManager.notifyProjectUpdateListeners(ProjectUpdate.Templates);
    LOG.info("Created template \"{}\"", name);
    return template;
  }

  @Override
  public Library createLibrary(String name) throws Exception {
    var library = projectManager.createLibrary(name);
    projectManager.notifyProjectUpdateListeners(ProjectUpdate.Libraries);
    LOG.info("Created library \"{}\"", name);
    return library;
  }

  @Override
  public Program createProgram(Library library, String name) {
    var program = new Program();
    program.setId(UUID.randomUUID());
    program.setName(name);
    program.setLibraryId(library.getId());
    program.setIsDeleted(false);
    try {
      projectManager.getContent().put(program);
    } catch (Exception e) {
      LOG.error("Failed to create Program!", e);
      return program;
    }
    projectManager.notifyProjectUpdateListeners(ProjectUpdate.Programs);
    LOG.info("Created program \"{}\"", name);
    return program;
  }

  @Override
  public Instrument createInstrument(Library library, String name) {
    var instrument = new Instrument();
    instrument.setId(UUID.randomUUID());
    instrument.setName(name);
    instrument.setLibraryId(library.getId());
    instrument.setIsDeleted(false);
    try {
      projectManager.getContent().put(instrument);
    } catch (Exception e) {
      LOG.error("Failed to create Instrument!", e);
      return instrument;
    }
    projectManager.notifyProjectUpdateListeners(ProjectUpdate.Instruments);
    LOG.info("Created instrument \"{}\"", name);
    return instrument;
  }

  @Override
  public Program moveProgram(UUID id, Library library) {
    var program = projectManager.getContent().getPrograms().stream()
      .filter(p -> Objects.equals(p.getId(), id))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Program not found!"));
    program.setLibraryId(library.getId());
    try {
      projectManager.getContent().put(program);
    } catch (Exception e) {
      LOG.error("Failed to move Program!", e);
      return program;
    }
    return program;
  }

  @Override
  public Instrument moveInstrument(UUID id, Library library) {
    var instrument = projectManager.getContent().getInstruments().stream()
      .filter(p -> Objects.equals(p.getId(), id))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Instrument not found!"));
    instrument.setLibraryId(library.getId());
    try {
      projectManager.getContent().put(instrument);
    } catch (Exception e) {
      LOG.error("Failed to move Instrument!", e);
      return instrument;
    }
    return instrument;
  }

  @Override
  public Template cloneTemplate(UUID id, String name) {
    try {
      entityFactory.setAllEmptyAttributes(source, clone);
      projectManager.getContent().put(clone);
      projectManager.cloneAllTemplateBindings(source, clone);
      projectManager.notifyProjectUpdateListeners(ProjectUpdate.Templates);
      LOG.info("Cloned template \"{}\"", name);
      return clone;

    } catch (Exception e) {
      LOG.error("Failed to clone Template!", e);
    }
  }

  /**
   Clone a project from a remote source.

   @param parentPathPrefix parent folder
   @param projectName      project name
   @param clone            the clone callable
   */
  private void cloneProject(String parentPathPrefix, String projectName, Callable<Boolean> clone) {
    if (promptToSkipOverwriteIfExists(parentPathPrefix, projectName)) return;
    closeProject();
    executeInBackground("Clone Project", () -> {
      try {
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
