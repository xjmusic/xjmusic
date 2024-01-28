package io.xj.gui.services.impl;

import io.xj.gui.services.LabService;
import io.xj.gui.services.ProjectDescriptor;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.hub.HubContent;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.project.ProjectState;
import io.xj.nexus.project.ProjectUpdate;
import jakarta.annotation.Nullable;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  private final Map<ProjectUpdate, Set<Runnable>> projectUpdateListeners = new HashMap<>();
  private final Preferences prefs = Preferences.userNodeForPackage(ProjectServiceImpl.class);
  private final ObservableObjectValue<Project> currentProject;
  private final ObservableListValue<ProjectDescriptor> recentProjects = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
  private final StringProperty basePathPrefix = new SimpleStringProperty();
  private final DoubleProperty progress = new SimpleDoubleProperty();
  private final BooleanProperty isModified = new SimpleBooleanProperty(false);
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
  private final ThemeService themeService;
  private final LabService labService;
  private final ProjectManager projectManager;
  private final JsonProvider jsonProvider;

  public ProjectServiceImpl(
    @Value("${gui.recent.projects.max}") int maxRecentProjects,
    ThemeService themeService,
    LabService labService,
    ProjectManager projectManager
  ) {
    this.maxRecentProjects = maxRecentProjects;
    this.themeService = themeService;
    this.labService = labService;
    this.projectManager = projectManager;
    this.jsonProvider = new JsonProviderImpl();
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

    state.addListener((o, ov, nv) -> {
      if (nv == ProjectState.Ready) {
        didUpdate(ProjectUpdate.Templates, false);
        didUpdate(ProjectUpdate.Libraries, false);
        didUpdate(ProjectUpdate.Programs, false);
        didUpdate(ProjectUpdate.Instruments, false);
      }
    });
  }

  @Override
  public void closeProject(@Nullable Runnable afterClose) {
    promptToSaveChanges(() -> {
      projectManager.closeProject();
      didUpdate(ProjectUpdate.Templates, false);
      didUpdate(ProjectUpdate.Libraries, false);
      didUpdate(ProjectUpdate.Programs, false);
      didUpdate(ProjectUpdate.Instruments, false);
      isModified.set(false);
      state.set(ProjectState.Standby);
      if (Objects.nonNull(afterClose)) Platform.runLater(afterClose);
    });
  }

  @Override
  public void openProject(String projectFilePath) {
    closeProject(() -> executeInBackground("Open Project", () -> {
      if (projectManager.openProjectFromLocalFile(projectFilePath)) {
        projectManager.getProject().ifPresent(project -> {
          isModified.set(false);
          addToRecentProjects(project, projectManager.getProjectFilename(), projectManager.getPathToProjectFile());
        });
      } else {
        removeFromRecentProjects(projectFilePath);
      }
    }));
  }

  @Override
  public void createProject(String parentPathPrefix, String projectName) {
    closeProject(() -> {
      if (promptToSkipOverwriteIfExists(parentPathPrefix, projectName)) return;
      executeInBackground("Create Project", () -> {
        if (projectManager.createProject(parentPathPrefix, projectName)) {
          projectManager.getProject().ifPresent(project -> {
            isModified.set(false);
            addToRecentProjects(project, projectManager.getProjectFilename(), projectManager.getPathToProjectFile());
          });
        }
      });
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
  public void saveProject(@Nullable Runnable afterSave) {
    LOG.info("Will save project");
    executeInBackground("Save Project", () -> {
      projectManager.saveProject();
      Platform.runLater(() -> {
        isModified.set(false);
        if (Objects.nonNull(afterSave)) Platform.runLater(afterSave);
      });
    });
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
    projectUpdateListeners.computeIfAbsent(type, k -> new HashSet<>());
    projectUpdateListeners.get(type).add(listener);
  }

  @Override
  public void didUpdate(ProjectUpdate type, boolean modified) {
    if (modified) isModified.set(true);

    if (projectUpdateListeners.containsKey(type))
      projectUpdateListeners.get(type).forEach(Runnable::run);
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
    projectManager.getContent().getTemplates().removeIf(n -> Objects.equals(n.getId(), template.getId()));
    didUpdate(ProjectUpdate.Templates, true);
    LOG.info("Deleted template \"{}\"", template.getName());
  }

  @Override
  public void deleteTemplateBinding(TemplateBinding binding) {
    projectManager.getContent().getTemplateBindings().removeIf(n -> Objects.equals(n.getId(), binding.getId()));
    didUpdate(ProjectUpdate.TemplateBindings, true);
    LOG.info("Deleted {} template binding", binding.getType());
  }

  @Override
  public void deleteLibrary(Library library) {
    projectManager.getContent().getLibraries().removeIf(n -> Objects.equals(n.getId(), library.getId()));
    didUpdate(ProjectUpdate.Libraries, true);
    LOG.info("Deleted library \"{}\"", library.getName());
  }

  @Override
  public void deleteProgram(Program program) {
    projectManager.getContent().getPrograms().removeIf(n -> Objects.equals(n.getId(), program.getId()));
    didUpdate(ProjectUpdate.Programs, true);
    LOG.info("Deleted program \"{}\"", program.getName());
  }

  @Override
  public void deleteInstrument(Instrument instrument) {
    projectManager.getContent().getInstruments().removeIf(n -> Objects.equals(n.getId(), instrument.getId()));
    didUpdate(ProjectUpdate.Instruments, true);
    LOG.info("Deleted instrument \"{}\"", instrument.getName());
  }

  @Override
  public void deleteInstrumentAudio(InstrumentAudio audio) {
    projectManager.getContent().getInstrumentAudios().removeIf(n -> Objects.equals(n.getId(), n.getId()));
    didUpdate(ProjectUpdate.InstrumentAudios, true);
    LOG.info("Deleted instrument audio \"{}\"", audio.getName());
  }

  @Override
  public Template createTemplate(String name) throws Exception {
    var template = projectManager.createTemplate(name);
    didUpdate(ProjectUpdate.Templates, true);
    LOG.info("Created template \"{}\"", name);
    return template;
  }

  @Override
  public Library createLibrary(String name) throws Exception {
    var library = projectManager.createLibrary(name);
    didUpdate(ProjectUpdate.Libraries, true);
    LOG.info("Created library \"{}\"", name);
    return library;
  }

  @Override
  public Program createProgram(Library library, String name) throws Exception {
    var program = projectManager.createProgram(library, name);
    didUpdate(ProjectUpdate.Programs, true);
    LOG.info("Created program \"{}\"", name);
    return program;
  }

  @Override
  public Instrument createInstrument(Library library, String name) throws Exception {
    var instrument = projectManager.createInstrument(library, name);
    didUpdate(ProjectUpdate.Instruments, true);
    LOG.info("Created instrument \"{}\"", name);
    return instrument;
  }

  @Override
  public Program moveProgram(UUID id, Library library) throws Exception {
    var program = projectManager.moveProgram(id, library.getId());
    didUpdate(ProjectUpdate.Programs, true);
    LOG.info("Moved program \"{}\" to library \"{}\"", program.getName(), library.getName());
    return program;
  }

  @Override
  public Instrument moveInstrument(UUID id, Library library) throws Exception {
    var instrument = projectManager.moveInstrument(id, library.getId());
    didUpdate(ProjectUpdate.Instruments, true);
    LOG.info("Moved instrument \"{}\" to library \"{}\"", instrument.getName(), library.getName());
    return instrument;
  }

  @Override
  public Template cloneTemplate(UUID fromId, String name) throws Exception {
    var template = projectManager.cloneTemplate(fromId, name);
    didUpdate(ProjectUpdate.Templates, true);
    LOG.info("Cloned template to \"{}\"", name);
    return template;
  }

  @Override
  public Library cloneLibrary(UUID fromId, String name) throws Exception {
    var library = projectManager.cloneLibrary(fromId, name);
    didUpdate(ProjectUpdate.Libraries, true);
    LOG.info("Cloned library to \"{}\"", name);
    return library;
  }

  @Override
  public Program cloneProgram(UUID fromId, UUID libraryId, String name) throws Exception {
    var program = projectManager.cloneProgram(fromId, libraryId, name);
    didUpdate(ProjectUpdate.Programs, true);
    LOG.info("Cloned program to \"{}\"", name);
    return program;
  }

  @Override
  public ProgramSequence cloneProgramSequence(UUID fromId, String name) throws Exception {
    var sequence = projectManager.cloneProgramSequence(fromId, name);
    didUpdate(ProjectUpdate.ProgramEntities, true);
    LOG.info("Cloned program sequence to \"{}\"", name);
    return sequence;
  }

  @Override
  public ProgramSequencePattern cloneProgramSequencePattern(UUID fromId, String name) throws Exception {
    var pattern = projectManager.cloneProgramSequencePattern(fromId, name);
    didUpdate(ProjectUpdate.ProgramEntities, true);
    LOG.info("Cloned program sequence pattern to \"{}\"", name);
    return pattern;
  }

  @Override
  public Instrument cloneInstrument(UUID fromId, UUID libraryId, String name) throws Exception {
    var instrument = projectManager.cloneInstrument(fromId, libraryId, name);
    didUpdate(ProjectUpdate.Instruments, true);
    LOG.info("Cloned instrument to \"{}\"", name);
    return instrument;
  }

  @Override
  public boolean updateLibrary(Library library) {
    try {
      projectManager.getContent().put(library);
      didUpdate(ProjectUpdate.Libraries, true);
      return true;

    } catch (Exception e) {
      LOG.error("Could not save Library", e);
      return false;
    }
  }

  @Override
  public boolean updateProgram(Program program) {
    try {
      projectManager.getContent().put(program);
      didUpdate(ProjectUpdate.Programs, true);
      return true;

    } catch (Exception e) {
      LOG.error("Could not save Program", e);
      return false;
    }
  }

  @Override
  public boolean updateInstrument(Instrument instrument) {
    try {
      projectManager.getContent().put(instrument);
      didUpdate(ProjectUpdate.Instruments, true);
      return true;

    } catch (Exception e) {
      LOG.error("Could not save Instrument", e);
      return false;
    }
  }

  @Override
  public boolean updateTemplate(Template template) {
    try {
      projectManager.getContent().put(template);
      didUpdate(ProjectUpdate.Templates, true);
      return true;

    } catch (Exception e) {
      LOG.error("Could not save Template", e);
      return false;
    }
  }

  @Override
  public void addTemplateBinding(UUID templateId, ContentBindingType contentBindingType, UUID targetId) {
    try {
      var binding = new TemplateBinding();
      binding.setId(UUID.randomUUID());
      binding.setTemplateId(templateId);
      binding.setType(contentBindingType);
      binding.setTargetId(targetId);
      projectManager.getContent().put(binding);
      didUpdate(ProjectUpdate.TemplateBindings, true);
      LOG.info("Added {} template binding", contentBindingType);

    } catch (Exception e) {
      LOG.error("Could not add Template Binding!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  @Override
  public BooleanProperty isModifiedProperty() {
    return isModified;
  }

  @Override
  public void promptToSaveChanges(Runnable afterConfirmation) {
    if (isModified.not().get()) {
      Platform.runLater(afterConfirmation);
      return;
    }

    Alert alert = new Alert(Alert.AlertType.WARNING);
    themeService.setup(alert.getDialogPane().getScene());
    alert.setTitle("Project Modified");
    alert.setHeaderText("Project has unsaved changes!");
    alert.setContentText(String.format("Save changes to the XJ music project \"%s\" before closing?",
      projectManager.getProject().orElseThrow(() -> new RuntimeException("Could not find project!")).getName()));

    // Set up buttons "Save", "Don't Save", and "Cancel"
    var saveButton = new ButtonType("Save");
    var dontSaveButton = new ButtonType("Don't Save");
    var cancelButton = new ButtonType("Cancel");
    alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

    // Show the dialog and capture the result
    var result = alert.showAndWait();
    if (result.isPresent())
      switch (result.get().getText()) {
        case "Save" -> saveProject(afterConfirmation);
        case "Don't Save" -> Platform.runLater(afterConfirmation);
      }
  }

  /**
   Clone a project from a remote source.

   @param parentPathPrefix parent folder
   @param projectName      project name
   @param clone            the clone callable
   */
  private void cloneProject(String parentPathPrefix, String projectName, Callable<Boolean> clone) {
    closeProject(() -> {
      if (promptToSkipOverwriteIfExists(parentPathPrefix, projectName)) return;
      executeInBackground("Clone Project", () -> {
        try {
          if (clone.call()) {
            projectManager.getProject().ifPresent(project -> {
              isModified.set(false);
              addToRecentProjects(project, projectManager.getProjectFilename(), projectManager.getPathToProjectFile());
            });
          } else {
            removeFromRecentProjects(parentPathPrefix + projectName + ".xj");
          }
        } catch (Exception e) {
          LOG.warn("Failed to clone project", e);
        }
      });
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
    themeService.setup(alert.getDialogPane().getScene());
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
