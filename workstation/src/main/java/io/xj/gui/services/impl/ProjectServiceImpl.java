package io.xj.gui.services.impl;

import io.xj.engine.util.FormatUtils;
import io.xj.gui.project.ProjectBuildState;
import io.xj.gui.project.ProjectManager;
import io.xj.gui.project.ProjectState;
import io.xj.gui.services.ProjectDescriptor;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.VersionService;
import io.xj.gui.types.AudioFileContainer;
import io.xj.model.HubContent;
import io.xj.model.entity.EntityUtils;
import io.xj.model.enums.ContentBindingType;
import io.xj.model.enums.ProgramType;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.pojos.InstrumentMeme;
import io.xj.model.pojos.Library;
import io.xj.model.pojos.Program;
import io.xj.model.pojos.ProgramMeme;
import io.xj.model.pojos.ProgramSequence;
import io.xj.model.pojos.ProgramSequenceBinding;
import io.xj.model.pojos.ProgramSequenceBindingMeme;
import io.xj.model.pojos.ProgramSequenceChord;
import io.xj.model.pojos.ProgramSequenceChordVoicing;
import io.xj.model.pojos.ProgramSequencePattern;
import io.xj.model.pojos.ProgramSequencePatternEvent;
import io.xj.model.pojos.ProgramVoice;
import io.xj.model.pojos.ProgramVoiceTrack;
import io.xj.model.pojos.Project;
import io.xj.model.pojos.Template;
import io.xj.model.pojos.TemplateBinding;
import io.xj.model.util.StringUtils;
import io.xj.model.util.ValueUtils;
import jakarta.annotation.Nullable;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.prefs.Preferences;

@Service
public class ProjectServiceImpl implements ProjectService {
  private static final Logger LOG = LoggerFactory.getLogger(ProjectServiceImpl.class);
  private static final String defaultBasePathPrefix = System.getProperty("user.home") + File.separator + "Documents";
  private static final String defaultExportPathPrefix = System.getProperty("user.home") + File.separator + "Documents";
  private final int defaultOutputChannels;
  private final int defaultOutputFrameRate;
  private final AudioFileContainer defaultOutputContainer;
  private final String defaultOutputSampleBits;
  private static final double ERROR_DIALOG_WIDTH = 800.0;
  private static final double ERROR_DIALOG_HEIGHT = 600.0;
  private static final Collection<ProjectState> PROJECT_LOADING_STATES = Set.of(
    ProjectState.LoadingContent,
    ProjectState.LoadedContent,
    ProjectState.LoadingAudio,
    ProjectState.LoadedAudio,
    ProjectState.ExportingTemplate,
    ProjectState.BuildingProject
  );
  private final Map<Class<? extends Serializable>, Set<Runnable>> projectUpdateListeners = new HashMap<>();
  private final Preferences prefs = Preferences.userNodeForPackage(ProjectServiceImpl.class);
  private final ObservableObjectValue<Project> currentProject;
  private final ObservableListValue<ProjectDescriptor> recentProjects = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
  private final StringProperty projectsPathPrefix = new SimpleStringProperty();
  private final StringProperty exportPathPrefix = new SimpleStringProperty();
  private final DoubleProperty progress = new SimpleDoubleProperty();
  private final StringProperty progressLabel = new SimpleStringProperty();
  private final BooleanProperty isModified = new SimpleBooleanProperty(false);
  private final BooleanProperty isDemoProject = new SimpleBooleanProperty(false);
  private final StringProperty outputFrameRate = new SimpleStringProperty();
  private final StringProperty outputChannels = new SimpleStringProperty();
  private final ObjectProperty<AudioFileContainer> outputContainer = new SimpleObjectProperty<>();
  private final StringProperty outputSampleBits = new SimpleStringProperty();
  private final ObjectProperty<ProjectState> state = new SimpleObjectProperty<>(ProjectState.Standby);
  private final ObjectProperty<ProjectBuildState> buildState = new SimpleObjectProperty<>(ProjectBuildState.Standby);
  private final ObservableStringValue stateText = Bindings.createStringBinding(
    () -> switch (state.get()) {
      case Standby -> "Standby";
      case CreatingFolder -> "Creating Folder";
      case CreatedFolder -> "Created Folder";
      case LoadingContent -> "Loading Content";
      case LoadedContent -> "Loaded Content";
      case LoadingAudio, ExportingTemplate, BuildingProject -> progressLabel.get();
      case LoadedAudio -> "Loaded Audio";
      case Ready -> "Ready";
      case Saving -> "Saving";
      case Cancelled -> "Cancelled";
      case Failed -> "Failed";
    },
    state,
    progress,
    progressLabel);
  private final BooleanBinding isStateLoading = Bindings.createBooleanBinding(
    () -> PROJECT_LOADING_STATES.contains(state.get()),
    state);
  private final BooleanBinding isStateReady = state.isEqualTo(ProjectState.Ready);
  private final BooleanBinding isStateSaving = state.isEqualTo(ProjectState.Saving);
  private final BooleanBinding isStateStandby = state.isEqualTo(ProjectState.Standby);
  private final int maxRecentProjects;
  private final ThemeService themeService;
  private final ProjectManager projectManager;
  private final VersionService versionService;
  private final JsonProvider jsonProvider;
  private final String demoBaseUrl;

  public ProjectServiceImpl(
    @Value("${demo.baseUrl}") String demoBaseUrl,
    @Value("${view.recentProjectsMax}") int maxRecentProjects,
    @Value("${build.defaultOutputChannels}") int defaultOutputChannels,
    @Value("${build.defaultOutputFrameRate}") int defaultOutputFrameRate,
    @Value("${build.defaultOutputContainer}") String defaultOutputContainer,
    @Value("${build.defaultOutputSampleBits}") String defaultOutputSampleBits,
    ThemeService themeService,
    ProjectManager projectManager,
    VersionService versionService
  ) {
    this.demoBaseUrl = demoBaseUrl;
    this.maxRecentProjects = maxRecentProjects;
    this.defaultOutputChannels = defaultOutputChannels;
    this.defaultOutputFrameRate = defaultOutputFrameRate;
    this.defaultOutputContainer = AudioFileContainer.valueOf(defaultOutputContainer.toUpperCase(Locale.ROOT));
    this.defaultOutputSampleBits = defaultOutputSampleBits;
    this.themeService = themeService;
    this.projectManager = projectManager;
    this.versionService = versionService;
    this.jsonProvider = new JsonProviderImpl();
    attachPreferenceListeners();
    setAllFromPreferencesOrDefaults();

    projectManager.setOnProgressLabel((label) -> Platform.runLater(() -> this.progressLabel.set(label)));
    projectManager.setOnProgress((progress) -> Platform.runLater(() -> this.progress.set(progress)));
    projectManager.setOnStateChange((state) -> Platform.runLater(() -> this.state.set(state)));
    projectManager.setOnBuildStateChange((buildState) -> Platform.runLater(() -> this.buildState.set(buildState)));

    currentProject = Bindings.createObjectBinding(() -> {
      if (Objects.equals(state.get(), ProjectState.Ready)) {
        return projectManager.getProject().orElse(null);
      } else {
        return null;
      }
    }, state);

    state.addListener((o, ov, nv) -> {
      if (nv == ProjectState.Ready) {
        didUpdate(Template.class, false);
        didUpdate(Library.class, false);
        didUpdate(Program.class, false);
        didUpdate(Instrument.class, false);
        if (Objects.nonNull(projectManager.getContent()))
          isDemoProject.set(projectManager.getContent().getDemo());
      }
    });
  }

  @Override
  public void closeProject(@Nullable Runnable afterClose) {
    promptToSaveChanges(() -> {
      projectManager.closeProject();
      didUpdate(Template.class, false);
      didUpdate(Library.class, false);
      didUpdate(Program.class, false);
      didUpdate(Instrument.class, false);
      resetProjectModificationStates(true);
      state.set(ProjectState.Standby);
      if (Objects.nonNull(afterClose)) Platform.runLater(afterClose);
    });
  }

  @Override
  public void openProject(String projectFilePath) {
    closeProject(() -> executeInBackground("Open Project", () -> {
      if (projectManager.openProjectFromLocalFile(projectFilePath)) {
        projectManager.getProject().ifPresent(project -> {
          resetProjectModificationStates(true);
          addToRecentProjects(project, projectManager.getProjectFilename(), projectManager.getPathToProjectFile());
          Platform.runLater(() -> {
            if (Objects.isNull(project.getPlatformVersion()) || ValueUtils.compareMonotonicVersion(project.getPlatformVersion(), versionService.getVersion()) < 0) {
              var migrate = showConfirmationDialog("Legacy Project Version", "Project created with previous workstation version", "The project was created with a previous version of the software. It's necessary to migrate and save the project before using it. Do you want to continue?");
              if (migrate) {
                saveProject(() -> resetProjectModificationStates(true));
              } else {
                closeProject(null);
              }
            } else if (ValueUtils.compareMonotonicVersion(project.getPlatformVersion(), versionService.getVersion()) > 0) {
              showWarningAlert("Project Version", "Project created with newer workstation version", "The project was created with a newer version of the software. Please update the software to open the project.");
              closeProject(null);
            }
          });
        });
      } else {
        removeFromRecentProjects(projectFilePath);
      }
    }));
  }

  @Override
  public void createProject(String parentPathPrefix, String projectName) {
    closeProject(() -> {
      if (promptToSkipOverwriteIfExists(parentPathPrefix, projectName, "Project"))
        executeInBackground("Create Project", () -> {
          if (projectManager.createProject(parentPathPrefix, projectName, versionService.getVersion())) {
            projectManager.getProject().ifPresent(project -> {
              resetProjectModificationStates(true);
              addToRecentProjects(project, projectManager.getProjectFilename(), projectManager.getPathToProjectFile());
            });
          }
        });
    });
  }

  @Override
  public void fetchDemoTemplate(String parentPathPrefix, String templateShipKey, String projectName) {
    closeProject(() -> {
      if (promptToSkipOverwriteIfExists(parentPathPrefix, projectName, "Project"))
        executeInBackground("Duplicate Project", () -> {
          try {
            if (projectManager.createProjectFromDemoTemplate(
              demoBaseUrl,
              templateShipKey, parentPathPrefix,
              projectName,
              versionService.getVersion()
            )) {
              projectManager.getProject().ifPresent(project -> {
                resetProjectModificationStates(true);
                addToRecentProjects(project, projectManager.getProjectFilename(), projectManager.getPathToProjectFile());
              });
            } else {
              removeFromRecentProjects(parentPathPrefix + projectName + ".xj");
              Platform.runLater(this::cancelProjectLoading);
            }
          } catch (Exception e) {
            LOG.warn("Failed to duplicate project! {}\n{}", e, StringUtils.formatStackTrace(e));
            Platform.runLater(this::cancelProjectLoading);
          }
        });
    });
  }

  @Override
  public void exportTemplate(
    Template template,
    String parentPathPrefix,
    String exportName,
    Boolean conversion,
    int conversionFrameRate,
    int conversionSampleBits,
    int conversionChannels
  ) {
    if (promptToSkipOverwriteIfExists(parentPathPrefix, exportName, "Template Export"))
      executeInBackground("Export Template", () -> {
        try {
          if (!projectManager.exportTemplate(
            template,
            parentPathPrefix,
            exportName,
            conversion,
            conversionFrameRate,
            conversionSampleBits,
            conversionChannels
          )) {
            Platform.runLater(this::cancelProjectLoading);
          }
        } catch (Exception e) {
          LOG.warn("Failed to export template! {}\n{}", e, StringUtils.formatStackTrace(e));
          Platform.runLater(this::cancelProjectLoading);
        }
      });
  }

  @Override
  public void buildProject() {
    executeInBackground("Build Project", () -> {
      try {
        if (!projectManager.buildProject(
          outputContainer.get(),
          (int) Float.parseFloat(outputFrameRate.get()),
          Integer.parseInt(outputSampleBits.get()),
          Integer.parseInt(outputChannels.get())
        )) {
          Platform.runLater(this::cancelProjectLoading);
        }
      } catch (Exception e) {
        LOG.warn("Failed to build project! {}\n{}", e, StringUtils.formatStackTrace(e));
        Platform.runLater(this::cancelProjectLoading);
      }
    });
  }

  @Override
  public void saveProject(@Nullable Runnable onComplete) {
    LOG.info("Will save project");
    executeInBackground("Save Project", () -> {
      projectManager.saveProject(versionService.getVersion());
      Platform.runLater(() -> {
        resetProjectModificationStates(false);
        if (Objects.nonNull(onComplete)) Platform.runLater(onComplete);
      });
    });
  }

  @Override
  public void saveAsProject(String parentPathPrefix, String projectName) {
    LOG.info("Will save as new project");
    executeInBackground("Save As New Project", () -> {
      projectManager.saveAsProject(parentPathPrefix, projectName, versionService.getVersion());
      addToRecentProjects(projectManager.getProject().orElse(null), projectName + ".xj", projectManager.getPathToProjectFile());
      Platform.runLater(() -> resetProjectModificationStates(false));
    });
  }

  @Override
  public void cancelProjectLoading() {
    projectManager.cancelOperation();
  }

  @Override
  public StringProperty projectsPathPrefixProperty() {
    return projectsPathPrefix;
  }

  @Override
  public StringProperty exportPathPrefixProperty() {
    return exportPathPrefix;
  }

  @Override
  public StringProperty outputChannelsProperty() {
    return outputChannels;
  }

  @Override
  public StringProperty outputFrameRateProperty() {
    return outputFrameRate;
  }

  @Override
  public Property<AudioFileContainer> outputContainerProperty() {
    return outputContainer;
  }

  @Override
  public StringProperty outputSampleBitsProperty() {
    return outputSampleBits;
  }

  @Override
  public void resetBuildSettingsToDefaults() {
    outputChannels.set(String.valueOf(defaultOutputChannels));
    outputFrameRate.set(String.valueOf(defaultOutputFrameRate));
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
  public ObjectProperty<ProjectBuildState> buildStateProperty() {
    return buildState;
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
  public BooleanBinding isStateSavingProperty() {
    return isStateSaving;
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
  public HubContent getContent(Template template) {
    return projectManager.getContent(template);
  }

  @Override
  public void deleteContent(Object entity) {
    try {
      var id = EntityUtils.getId(entity);
      try {
        deleteContent(entity.getClass(), id);
      } catch (Exception e2) {
        LOG.error("Could not delete content {}[{}]! {}\n{}", entity.getClass().getSimpleName(), id, e2, StringUtils.formatStackTrace(e2));
      }
    } catch (Exception e) {
      LOG.error("Could not delete {}! {}\n{}", entity.getClass().getSimpleName(), e, StringUtils.formatStackTrace(e));
    }
  }

  @Override
  public void deleteContent(Class<?> type, UUID id) {
    try {
      projectManager.getContent().delete(type, id);
      didUpdate(type, true);
      LOG.info("Deleted {}[{}]", type.getSimpleName(), id);
    } catch (Exception e) {
      LOG.error("Could not delete content having type and id {}[{}]! {}\n{}", type.getSimpleName(), id, e, StringUtils.formatStackTrace(e));
    }
  }

  @Override
  public boolean deleteProgramVoiceTrack(UUID programVoiceTrackId) {
    var trackEvents = projectManager.getContent().getEventsOfTrack(programVoiceTrackId);
    if (!trackEvents.isEmpty() && !showConfirmationDialog("Delete Track", "Track contains events", "This operation will delete the track and all of its events. Do you want to proceed?")) {
      return false;
    }
    try {
      for (ProgramSequencePatternEvent event : trackEvents) deleteContent(event);
      deleteContent(ProgramVoiceTrack.class, programVoiceTrackId);
      LOG.info("Deleted ProgramVoiceTrack[{}]{}", programVoiceTrackId, trackEvents.isEmpty() ? "" : String.format(" and %d events", trackEvents.size()));
    } catch (Exception e) {
      LOG.error("Could not delete ProgramVoiceTrack[{}]! {}\n{}", programVoiceTrackId, e, StringUtils.formatStackTrace(e));
    }
    return true;
  }

  @Override
  public boolean deleteProgramVoice(UUID programVoiceId) {
    var voiceTracks = projectManager.getContent().getTracksOfVoice(programVoiceId);
    var voicePatterns = projectManager.getContent().getPatternsOfVoice(programVoiceId);
    var voiceTrackEvents = projectManager.getContent().getSequencePatternEventsOfProgram(programVoiceId).stream()
      .filter(event -> voiceTracks.stream().anyMatch(track -> track.getId().equals(event.getProgramVoiceTrackId())))
      .toList();
    if (!voiceTracks.isEmpty() && !showConfirmationDialog("Delete Voice", "Voice contains tracks, patterns, or events", "This operation will delete the voice and all of its tracks, patterns, and events. Do you want to proceed?")) {
      return false;
    }
    try {
      for (ProgramVoiceTrack track : voiceTracks) deleteContent(track);
      for (ProgramSequencePattern pattern : voicePatterns) deleteContent(pattern);
      for (ProgramSequencePatternEvent event : voiceTrackEvents) deleteContent(event);
      deleteContent(ProgramVoice.class, programVoiceId);
      LOG.info("Deleted ProgramVoice[{}]{}", programVoiceId,
        voiceTracks.isEmpty() ? "" : " and " + FormatUtils.describeCounts(Map.of(
          "track", voiceTracks.size(),
          "pattern", voicePatterns.size(),
          "event", voiceTrackEvents.size()
        )));
    } catch (Exception e) {
      LOG.error("Could not delete ProgramVoiceTrack[{}]! {}\n{}", programVoiceId, e, StringUtils.formatStackTrace(e));
    }
    return true;
  }

  @Override
  public boolean deleteProgramSequencePattern(UUID programSequencePatternId) {
    var patternEvents = projectManager.getContent().getEventsOfPattern(programSequencePatternId);
    if (!patternEvents.isEmpty() && !showConfirmationDialog("Delete Pattern", "Pattern contains events", "This operation will delete the pattern and all of its events. Do you want to proceed?")) {
      return false;
    }
    try {
      for (ProgramSequencePatternEvent event : patternEvents) deleteContent(event);
      deleteContent(ProgramSequencePattern.class, programSequencePatternId);
      LOG.info("Deleted ProgramSequencePattern[{}]{}", programSequencePatternId, patternEvents.isEmpty() ? "" : String.format(" and %d events", patternEvents.size()));
    } catch (Exception e) {
      LOG.error("Could not delete ProgramSequencePattern[{}]! {}\n{}", programSequencePatternId, e, StringUtils.formatStackTrace(e));
    }
    return true;
  }


  @Override
  public ObservableListValue<ProjectDescriptor> recentProjectsProperty() {
    return recentProjects;
  }

  @Override
  public <N extends Serializable> Runnable addProjectUpdateListener(Class<N> type, Runnable listener) {
    projectUpdateListeners.computeIfAbsent(type, k -> new HashSet<>());
    projectUpdateListeners.get(type).add(listener);
    return () -> projectUpdateListeners.get(type).remove(listener);
  }

  @Override
  public <N> void didUpdate(Class<N> type, boolean modified) {
    if (modified) isModified.set(true);
    buildState.set(ProjectBuildState.Dirty);

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
  public Template createTemplate(String name) throws Exception {
    var template = projectManager.createTemplate(name);
    didUpdate(Template.class, true);
    LOG.info("Created Template \"{}\"", name);
    return template;
  }

  @Override
  public Library createLibrary(String name) throws Exception {
    var library = projectManager.createLibrary(name);
    didUpdate(Library.class, true);
    LOG.info("Created Library \"{}\"", name);
    return library;
  }

  @Override
  public Program createProgram(Library library, String name) throws Exception {
    var program = projectManager.createProgram(library, name);
    didUpdate(Program.class, true);
    LOG.info("Created Program \"{}\"", name);
    return program;
  }

  @Override
  public ProgramSequence createProgramSequence(UUID programId) throws Exception {
    var programSequence = projectManager.createProgramSequence(programId);
    didUpdate(ProgramSequence.class, true);
    LOG.info("Created Program Sequence \"{}\"", programSequence.getName());
    return programSequence;
  }

  @Override
  public ProgramSequencePattern createProgramSequencePattern(UUID programId, UUID programSequenceId, UUID programVoiceId) throws Exception {
    var programSequencePattern = projectManager.createProgramSequencePattern(programId, programSequenceId, programVoiceId);
    didUpdate(ProgramSequencePattern.class, true);
    LOG.info("Created Program Sequence Pattern \"{}\"", programSequencePattern.getName());
    return programSequencePattern;
  }

  @Override
  public ProgramSequencePatternEvent createProgramSequencePatternEvent(UUID trackId, UUID patternId, double position, double duration) throws Exception {
    var programSequencePatternEvent = projectManager.createProgramSequencePatternEvent(trackId, patternId, position, duration);
    didUpdate(ProgramSequencePatternEvent.class, true);
    LOG.info("Created Program Sequence Pattern Event at {}", programSequencePatternEvent.getPosition());
    return programSequencePatternEvent;
  }

  @Override
  public ProgramVoice createProgramVoice(UUID programId) throws Exception {
    var programVoice = projectManager.createProgramVoice(programId);
    didUpdate(ProgramVoice.class, true);
    LOG.info("Created Program Voice \"{}\"", programVoice.getName());
    return programVoice;
  }

  @Override
  public ProgramVoiceTrack createProgramVoiceTrack(UUID voiceId) throws Exception {
    var programVoiceTrack = projectManager.createProgramVoiceTrack(voiceId);
    didUpdate(ProgramVoiceTrack.class, true);
    LOG.info("Created Program VoiceTrack \"{}\"", programVoiceTrack.getName());
    return programVoiceTrack;
  }

  @Override
  public ProgramMeme createProgramMeme(UUID programId) throws Exception {
    var meme = projectManager.createProgramMeme(programId);
    didUpdate(ProgramMeme.class, true);
    LOG.info("Created Program Meme \"{}\"", meme.getName());
    return meme;
  }

  @Override
  public ProgramSequenceBindingMeme createProgramSequenceBindingMeme(UUID programSequenceBindingId) throws Exception {
    var meme = projectManager.createProgramSequenceBindingMeme(programSequenceBindingId);
    didUpdate(ProgramSequenceBindingMeme.class, true);
    LOG.info("Created ProgramSequenceBindingMeme \"{}\"", meme.getName());
    return meme;
  }

  @Override
  public Instrument createInstrument(Library library, String name) throws Exception {
    var instrument = projectManager.createInstrument(library, name);
    didUpdate(Instrument.class, true);
    LOG.info("Created instrument \"{}\"", name);
    return instrument;
  }

  @Override
  public InstrumentMeme createInstrumentMeme(UUID instrumentId) throws Exception {
    var meme = projectManager.createInstrumentMeme(instrumentId);
    didUpdate(InstrumentMeme.class, true);
    LOG.info("Created Instrument Meme \"{}\"", meme.getName());
    return meme;
  }

  @Override
  public InstrumentAudio createInstrumentAudio(Instrument instrument, String audioFilePath) throws Exception {
    var audio = projectManager.createInstrumentAudio(instrument, audioFilePath);
    didUpdate(InstrumentAudio.class, true);
    LOG.info("Created Instrument Audio \"{}\" by importing waveform from {}", audio.getName(), audioFilePath);
    return audio;
  }

  @Override
  public Program moveProgram(UUID id, Library library) throws Exception {
    var program = projectManager.moveProgram(id, library.getId());
    didUpdate(Program.class, true);
    LOG.info("Moved program \"{}\" to library \"{}\"", program.getName(), library.getName());
    return program;
  }

  @Override
  public Instrument moveInstrument(UUID id, Library library) throws Exception {
    var instrument = projectManager.moveInstrument(id, library.getId());
    didUpdate(Instrument.class, true);
    LOG.info("Moved instrument \"{}\" to library \"{}\"", instrument.getName(), library.getName());
    return instrument;
  }

  @Override
  public Template duplicateTemplate(UUID fromId, String name) throws Exception {
    var template = projectManager.duplicateTemplate(fromId, name);
    didUpdate(Template.class, true);
    LOG.info("Duplicated template to \"{}\"", name);
    return template;
  }

  @Override
  public Library duplicateLibrary(UUID fromId, String name) throws Exception {
    var library = projectManager.duplicateLibrary(fromId, name);
    didUpdate(Library.class, true);
    LOG.info("Duplicated library to \"{}\"", name);
    return library;
  }

  @Override
  public Program duplicateProgram(UUID fromId, UUID libraryId, String name) throws Exception {
    var program = projectManager.duplicateProgram(fromId, libraryId, name);
    didUpdate(Program.class, true);
    LOG.info("Duplicated program to \"{}\"", name);
    return program;
  }

  @Override
  public ProgramSequence duplicateProgramSequence(UUID fromId) throws Exception {
    var sequence = projectManager.duplicateProgramSequence(fromId);
    didUpdate(ProgramSequence.class, true);
    LOG.info("Duplicated program sequence to \"{}\"", sequence.getName());
    return sequence;
  }

  @Override
  public ProgramSequencePattern duplicateProgramSequencePattern(UUID fromId) throws Exception {
    var pattern = projectManager.duplicateProgramSequencePattern(fromId);
    didUpdate(ProgramSequence.class, true);
    LOG.info("Duplicated program sequence pattern to \"{}\"", pattern.getName());
    return pattern;
  }

  @Override
  public Instrument duplicateInstrument(UUID fromId, UUID libraryId, String name) throws Exception {
    var instrument = projectManager.duplicateInstrument(fromId, libraryId, name);
    didUpdate(Instrument.class, true);
    LOG.info("Duplicated instrument to \"{}\"", name);
    return instrument;
  }

  @Override
  public <N> void update(N entity) {
    try {
      projectManager.getContent().put(entity);
      didUpdate(entity.getClass(), true);
    } catch (Exception e) {
      LOG.error("Could not update entity! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

  @Override
  public <N> void update(Class<N> type, UUID id, String attribute, Object value) {
    try {
      if (Objects.isNull(projectManager.getContent()))
        return; // the project was closed and this method was called from a listener
      if (projectManager.getContent().update(type, id, attribute, value)) {
        LOG.info("Updated {}[{}] attribute \"{}\" to \"{}\"", type.getSimpleName(), id, attribute, value);
        didUpdate(type, true);
      }
    } catch (Exception e) {
      LOG.error("Could not update {}[{}] attribute \"{}\" to \"{}\"! {}\n{}", type.getSimpleName(), id, attribute, value, e, StringUtils.formatStackTrace(e));
    }
  }

  @Override
  public boolean updateLibrary(Library library) {
    try {
      projectManager.getContent().put(library);
      didUpdate(Library.class, true);
      return true;

    } catch (Exception e) {
      LOG.error("Could not save Library! {}\n{}", e, StringUtils.formatStackTrace(e));
      return false;
    }
  }

  @Override
  public boolean updateProgramSequencePatternTotal(UUID programSequencePatternId, String totalText) {
    int total;
    try {
      total = Integer.parseInt(totalText);
    } catch (Exception e) {
      showWarningAlert("Invalid Total", "Invalid value for pattern total", "Total must be a whole number");
      return false;
    }

    if (total < 1) {
      showWarningAlert("Invalid Total", "Invalid value for pattern total", "Total must be at least 1");
      return false;
    }

    Optional<ProgramSequencePattern> pattern = projectManager.getContent().get(ProgramSequencePattern.class, programSequencePatternId);
    if (pattern.isEmpty()) return false; // the pattern has been deleted
    Optional<ProgramSequence> sequence = projectManager.getContent().get(ProgramSequence.class, pattern.get().getProgramSequenceId());
    if (sequence.isEmpty()) return false; // the sequence has been deleted

    if (total > sequence.get().getTotal()) {
      showWarningAlert("Invalid Total", "Invalid value for pattern total", "Total must be less than or equal to the sequence total");
      return false;
    }

    // Changing pattern length, if there are events past the end of the new length, ask for confirmation and delete those events
    List<ProgramSequencePatternEvent> events = projectManager.getContent().getEventsOfPattern(programSequencePatternId);
    if (events.stream().anyMatch(e -> e.getPosition() >= total)) {
      if (!showConfirmationDialog("Change Pattern Length", "Pattern contains events past the new length", "This operation will delete the events past the new length. Do you want to proceed?")) {
        return false;
      }
      for (ProgramSequencePatternEvent event : events) {
        if (event.getPosition() >= total) {
          deleteContent(event);
        }
      }
    }

    update(ProgramSequencePattern.class, programSequencePatternId, "total", total);
    return true;
  }

  @Override
  public boolean updateProgramSequenceTotal(UUID programSequenceId, String totalString) {
    int total;
    try {
      total = Integer.parseInt(totalString);
    } catch (Exception e) {
      showWarningAlert("Invalid Total", "Invalid value for sequence total", "Total must be a whole number");
      return false;
    }

    if (total < 1) {
      showWarningAlert("Invalid Total", "Invalid value for sequence total", "Total must be at least 1");
      return false;
    }

    Optional<ProgramSequence> sequence = projectManager.getContent().get(ProgramSequence.class, programSequenceId);
    if (sequence.isEmpty()) return false; // the sequence has been deleted

    // Changing sequence length, if there are patterns with total greater than the new length, ask for confirmation and delete those patterns
    Collection<ProgramSequencePattern> patterns = projectManager.getContent().getPatternsOfSequence(programSequenceId);
    if (patterns.stream().anyMatch(p -> p.getTotal() > total)) {
      showWarningAlert("Change Sequence Length", "Sequence contains patterns with total greater than the new length", "Please shorten or delete those patterns first");
      return false;
    }

    update(ProgramSequence.class, programSequenceId, "total", total);
    return true;
  }

  @Override
  public String getPathPrefixToInstrument(Instrument instrument) {
    return projectManager.getPathPrefixToInstrument(instrument, null);
  }

  @Override
  public String getPathToInstrumentAudioWaveform(InstrumentAudio audio) {
    return projectManager.getPathToInstrumentAudio(audio, null);
  }

  @Override
  public void createTemplateBinding(UUID templateId, ContentBindingType contentBindingType, UUID targetId) {
    try {
      var binding = new TemplateBinding();
      binding.setId(UUID.randomUUID());
      binding.setTemplateId(templateId);
      binding.setType(contentBindingType);
      binding.setTargetId(targetId);
      projectManager.getContent().put(binding);
      didUpdate(TemplateBinding.class, true);
      LOG.info("Added {} template binding", contentBindingType);

    } catch (Exception e) {
      LOG.error("Could not add Template Binding! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

  @Override
  public BooleanProperty isModifiedProperty() {
    return isModified;
  }

  @Override
  public BooleanProperty isDemoProjectProperty() {
    return isDemoProject;
  }

  @Override
  public void promptToSaveChanges(Runnable afterConfirmation) {
    if (isModified.not().get()) {
      Platform.runLater(afterConfirmation);
      return;
    }

    Alert alert = new Alert(Alert.AlertType.WARNING);
    themeService.setup(alert);
    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
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

  @Override
  public void showWarningAlert(String title, String header, String body) {
    showAlert(Alert.AlertType.WARNING, title, header, body);
  }

  @Override
  public void showAlert(Alert.AlertType type, String title, String header, @Nullable String body) {
    Alert alert = new Alert(type);
    themeService.setup(alert);
    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    alert.setTitle(title);
    alert.setHeaderText(header);
    if (Objects.nonNull(body)) alert.setContentText(body);
    alert.showAndWait();
  }

  @Override
  public void showErrorDialog(String title, String header, String body) {
    ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
    Dialog<String> dialog = new Dialog<>();
    themeService.setup(dialog);
    dialog.getDialogPane().getButtonTypes().add(loginButtonType);

    dialog.setTitle(title);
    dialog.setHeaderText(header);

    // Create a TextArea for the message
    TextArea textArea = new TextArea(body);
    textArea.setEditable(false); // Make it non-editable
    textArea.setWrapText(true); // Enable text wrapping
    textArea.setMaxWidth(Double.MAX_VALUE); // Use max width for better responsiveness
    textArea.setMaxHeight(Double.MAX_VALUE); // Use max height for better responsiveness
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    GridPane content = new GridPane();
    content.setMaxWidth(Double.MAX_VALUE);
    content.add(textArea, 0, 0);

    // Set the dialog content
    dialog.getDialogPane().setContent(content);
    dialog.setResizable(true);
    dialog.getDialogPane().setPrefWidth(ERROR_DIALOG_WIDTH);
    dialog.getDialogPane().setPrefHeight(ERROR_DIALOG_HEIGHT);
    themeService.setup(dialog.getDialogPane().getScene());

    dialog.showAndWait();
  }

  @Override
  public boolean showConfirmationDialog(String title, String header, String content) {
    // Create a custom dialog
    Dialog<ButtonType> dialog = new Dialog<>();
    themeService.setup(dialog);
    dialog.setTitle(title);

    // Set the header and content
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setHeaderText(header);
    dialogPane.setContentText(content);

    // Add Yes and No buttons
    ButtonType yesButton = new ButtonType("Yes", ButtonType.OK.getButtonData());
    ButtonType noButton = new ButtonType("No", ButtonType.CANCEL.getButtonData());
    dialogPane.getButtonTypes().addAll(yesButton, noButton);

    // Ensure it's resizable and has a preferred width
    dialogPane.setMinHeight(Region.USE_PREF_SIZE);
    dialogPane.setPrefWidth(400); // You can adjust this value

    // Show the dialog and wait for the user to close it
    java.util.Optional<ButtonType> result = dialog.showAndWait();

    // Return true if 'Yes' was clicked, false otherwise
    return result.isPresent() && result.get() == yesButton;
  }

  @Override
  public boolean updateProgramType(UUID programId, ProgramType type) {
    switch (type) {
      case Main -> {
        // Changing type to a Main program, confirm then delete any voice tracks, sequence patterns, and sequence pattern events
        Collection<ProgramVoiceTrack> tracks = projectManager.getContent().getTracksOfProgram(programId);
        Collection<ProgramSequencePattern> patterns = projectManager.getContent().getSequencePatternsOfProgram(programId);
        Collection<ProgramSequencePatternEvent> events = projectManager.getContent().getSequencePatternEventsOfProgram(programId);
        if (!tracks.isEmpty() || !patterns.isEmpty() || !events.isEmpty()) {
          if (!showConfirmationDialog(
            "Change to Main-type Program",
            "Program contains " + FormatUtils.describeCounts(Map.of(
              "Voice Track", tracks.size(),
              "Sequence Pattern", patterns.size(),
              "Sequence Pattern Event", events.size()
            )),
            "This operation will delete all these entities which are not relevant to Main-type programs. Do you want to proceed?"
          )) return false;
          for (ProgramSequencePatternEvent event : events) deleteContent(event);
          for (ProgramSequencePattern pattern : patterns) deleteContent(pattern);
          for (ProgramVoiceTrack track : tracks) deleteContent(track);
        }
      }
      case Macro -> {
        // Changing type to a Macro program, confirm then delete any sequence chords, sequence chord voicings, voices, voice tracks, sequence patterns, and sequence pattern events
        Collection<ProgramSequenceChord> chords = projectManager.getContent().getSequenceChordsOfProgram(programId);
        Collection<ProgramSequenceChordVoicing> voicings = projectManager.getContent().getSequenceChordVoicingsOfProgram(programId);
        Collection<ProgramVoice> voices = projectManager.getContent().getVoicesOfProgram(programId);
        Collection<ProgramVoiceTrack> tracks = projectManager.getContent().getTracksOfProgram(programId);
        Collection<ProgramSequencePattern> patterns = projectManager.getContent().getSequencePatternsOfProgram(programId);
        Collection<ProgramSequencePatternEvent> events = projectManager.getContent().getSequencePatternEventsOfProgram(programId);
        if (!chords.isEmpty() || !voicings.isEmpty() || !voices.isEmpty() || !tracks.isEmpty() || !patterns.isEmpty() || !events.isEmpty()) {
          if (!showConfirmationDialog(
            "Change to Macro-type Program",
            "Program contains " + FormatUtils.describeCounts(Map.of(
              "Sequence Chord", chords.size(),
              "Sequence Chord Voicing", voicings.size(),
              "Voice", voices.size(),
              "Voice Track", tracks.size(),
              "Sequence Pattern", patterns.size(),
              "Sequence Pattern Event", events.size()
            )),
            "This operation will delete all these entities which are not relevant to Macro-type programs. Do you want to proceed?"))
            return false;
          for (ProgramSequencePatternEvent event : events) deleteContent(event);
          for (ProgramSequencePattern pattern : patterns) deleteContent(pattern);
          for (ProgramVoiceTrack track : tracks) deleteContent(track);
          for (ProgramSequenceChordVoicing voicing : voicings) deleteContent(voicing);
          for (ProgramSequenceChord chord : chords) deleteContent(chord);
          for (ProgramVoice voice : voices) deleteContent(voice);
        }
      }
      case Beat, Detail -> {
        // Changing type to a Detail or Beat program, confirm then delete any sequence bindings, sequence chords, sequence chord voicings, and sequence binding memes
        Collection<ProgramSequenceBinding> sequenceBindings = projectManager.getContent().getSequenceBindingsOfProgram(programId);
        Collection<ProgramSequenceBindingMeme> sequenceBindingMemes = projectManager.getContent().getSequenceBindingMemesOfProgram(programId);
        Collection<ProgramSequenceChord> chords = projectManager.getContent().getSequenceChordsOfProgram(programId);
        Collection<ProgramSequenceChordVoicing> voicings = projectManager.getContent().getSequenceChordVoicingsOfProgram(programId);
        if (!sequenceBindings.isEmpty() || !sequenceBindingMemes.isEmpty() || !chords.isEmpty() || !voicings.isEmpty()) {
          if (!showConfirmationDialog(
            "Change to " + type + "-type Program",
            "Program contains " + FormatUtils.describeCounts(Map.of(
              "Sequence Binding", sequenceBindings.size(),
              "Sequence Binding Meme", sequenceBindingMemes.size(),
              "Sequence Chord", chords.size(),
              "Sequence Chord Voicing", voicings.size()
            )),
            "This operation will delete all these entities which are not relevant to " + type + "-type programs. Do you want to proceed?"))
            return false;
          for (ProgramSequenceBindingMeme meme : sequenceBindingMemes) deleteContent(meme);
          for (ProgramSequenceBinding binding : sequenceBindings) deleteContent(binding);
          for (ProgramSequenceChordVoicing voicing : voicings) deleteContent(voicing);
          for (ProgramSequenceChord chord : chords) deleteContent(chord);
        }
      }
    }
    update(Program.class, programId, "type", type);
    return true;
  }

  /**
   If the directory already exists then pop up a confirmation dialog

   @param parentPathPrefix parent folder
   @param name             thing name
   @param type             of thing to overwrite
   @return true if overwrite confirmed
   */
  private boolean promptToSkipOverwriteIfExists(String parentPathPrefix, String name, String type) {
    if (!Files.exists(Path.of(parentPathPrefix + name))) {
      return true;
    }
    return promptForConfirmation("Overwrite Existing " + type,
      "The " + type + " already exists",
      String.format("The %s \"%s\" already exists in the folder \"%s\". This operation will overwrite the target location. Do you want to proceed?",
        type, name, parentPathPrefix));
  }

  /**
   Show a YES/NO confirmation dialog

   @param title       of confirmation
   @param headerText  of confirmation
   @param contentText of confirmation
   @return true if user chooses yes
   */
  private boolean promptForConfirmation(String title, String headerText, String contentText) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    themeService.setup(alert);
    alert.setTitle(title);
    alert.setHeaderText(headerText);
    alert.setContentText(contentText);
    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

    // Optional: Customize the buttons (optional)
    alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);

    // Show the dialog and capture the result
    var result = alert.showAndWait();
    return result.isPresent() && result.get() == ButtonType.YES;
  }

  /**
   Execute a runnable in a background thread. Use JavaFX Platform.runLater(...) as well as spawning an additional thread.

   @param threadName the name of the thread
   @param run        the runnable
   */
  private void executeInBackground(String threadName, Runnable run) {
    var thread = new Thread(run);
    thread.setName(threadName);
    Platform.runLater(thread::start);
  }

  /**
   Attach preference listeners.
   */
  private void attachPreferenceListeners() {
    projectsPathPrefix.addListener((o, ov, value) -> prefs.put("basePathPrefix", value));
    exportPathPrefix.addListener((o, ov, value) -> prefs.put("exportPathPrefix", value));
    outputChannels.addListener((o, ov, value) -> prefs.put("outputChannels", value));
    outputFrameRate.addListener((o, ov, value) -> prefs.put("outputFrameRate", value));
    outputContainer.addListener((o, ov, value) -> prefs.put("outputContainer", value.toString()));
    outputSampleBits.addListener((o, ov, value) -> prefs.put("outputSampleBits", value));
    recentProjects.addListener((o, ov, value) -> {
      try {
        prefs.put("recentProjects", jsonProvider.getMapper().writeValueAsString(value));
      } catch (Exception e) {
        LOG.warn("Failed to serialize recent projects! {}\n{}", e, StringUtils.formatStackTrace(e));
      }
    });
  }

  /**
   Set all properties from preferences, else defaults.
   */
  private void setAllFromPreferencesOrDefaults() {
    projectsPathPrefix.set(prefs.get("basePathPrefix", defaultBasePathPrefix));
    exportPathPrefix.set(prefs.get("exportPathPrefix", defaultExportPathPrefix));
    try {
      recentProjects.setAll(jsonProvider.getMapper().readValue(prefs.get("recentProjects", "[]"), ProjectDescriptor[].class));
    } catch (Exception e) {
      LOG.warn("Failed to deserialize recent projects! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
    outputChannels.set(prefs.get("outputChannels", Integer.toString(defaultOutputChannels)));
    outputFrameRate.set(prefs.get("outputFrameRate", Double.toString(defaultOutputFrameRate)));
    outputSampleBits.set(prefs.get("outputSampleBits", defaultOutputSampleBits));

    try {
      outputContainer.set(AudioFileContainer.valueOf(prefs.get("outputContainer", defaultOutputContainer.toString()).toUpperCase(Locale.ROOT)));
    } catch (Exception e) {
      LOG.error("Failed to set control mode from preferences", e);
      outputContainer.set(defaultOutputContainer);
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

  /**
   Reset the project modification states.
   */
  private void resetProjectModificationStates(boolean resetBuild) {
    isModified.set(false);
    if (resetBuild)
      buildState.set(ProjectBuildState.Standby);
  }

}
