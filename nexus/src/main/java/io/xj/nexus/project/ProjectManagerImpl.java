package io.xj.nexus.project;

import io.xj.hub.HubContent;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.ProgramConfig;
import io.xj.hub.TemplateConfig;
import io.xj.hub.entity.EntityFactory;
import io.xj.hub.entity.EntityUtils;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.HubClientException;
import io.xj.nexus.hub_client.HubClientFactory;
import io.xj.nexus.util.FormatUtils;
import jakarta.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static io.xj.hub.util.FileUtils.computeWaveformKey;
import static io.xj.nexus.hub_client.HubClientFactory.FILE_SIZE_NOT_FOUND;

public class ProjectManagerImpl implements ProjectManager {
  static final Logger LOG = LoggerFactory.getLogger(ProjectManagerImpl.class);
  private static final InstrumentMode DEFAULT_INSTRUMENT_MODE = InstrumentMode.Event;
  private static final InstrumentState DEFAULT_INSTRUMENT_STATE = InstrumentState.Published;
  private static final InstrumentType DEFAULT_INSTRUMENT_TYPE = InstrumentType.Drum;
  private static final ProgramState DEFAULT_PROGRAM_STATE = ProgramState.Published;
  private static final ProgramType DEFAULT_PROGRAM_TYPE = ProgramType.Main;
  private static final String DEFAULT_INSTRUMENT_AUDIO_EVENT = "X";
  private static final String DEFAULT_INSTRUMENT_AUDIO_TONES = "X";
  private static final String DEFAULT_KEY = "C";
  private static final float DEFAULT_INTENSITY = 1.0f;
  private static final float DEFAULT_TEMPO = 120f;
  private static final float DEFAULT_LOOP_BEATS = 4.0f;
  private static final float DEFAULT_VOLUME = 1.0f;
  private static final String DEFAULT_PROGRAM_SEQUENCE_NAME = "New Sequence";
  private static final String DEFAULT_PROGRAM_SEQUENCE_PATTERN_NAME = "New Pattern";
  private static final String DEFAULT_PROGRAM_VOICE_NAME = "New Voice";
  private static final String DEFAULT_PROGRAM_VOICE_TRACK_NAME = "New Track";
  private static final Integer DEFAULT_PROGRAM_SEQUENCE_TOTAL = 4;
  private static final String DEFAULT_MEME_NAME = "XXX";
  private static final String DEFAULT_PROGRAM_SEQUENCE_PATTERN_EVENT_TONES = "X";
  private final AtomicReference<ProjectState> state = new AtomicReference<>(ProjectState.Standby);
  private final AtomicReference<Project> project = new AtomicReference<>();
  private final AtomicReference<String> projectPathPrefix = new AtomicReference<>(File.separator);
  private final AtomicReference<String> projectName = new AtomicReference<>("Project");
  private final AtomicReference<String> audioBaseUrl = new AtomicReference<>("https://audio.xj.io/");
  private final AtomicReference<HubContent> content = new AtomicReference<>();
  private final JsonProvider jsonProvider;
  private final EntityFactory entityFactory;
  private final HubClientFactory hubClientFactory;
  private final HttpClientProvider httpClientProvider;

  @Nullable
  private Consumer<Double> onProgress;

  @Nullable
  private Consumer<String> onProgressLabel;

  @Nullable
  private Consumer<ProjectState> onStateChange;

  /**
   Private constructor
   */
  public ProjectManagerImpl(
    JsonProvider jsonProvider,
    EntityFactory entityFactory,
    HttpClientProvider httpClientProvider,
    HubClientFactory hubClientFactory
  ) {
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.entityFactory = entityFactory;
    this.hubClientFactory = hubClientFactory;
  }

  @Override
  public String getProjectPathPrefix() {
    return projectPathPrefix.get();
  }

  @Override
  public Optional<Project> getProject() {
    return Optional.ofNullable(project.get());
  }

  @Override
  public String getAudioBaseUrl() {
    return this.audioBaseUrl.get();
  }

  @Override
  public boolean cloneProjectFromDemoTemplate(String audioBaseUrl, String parentPathPrefix, String templateShipKey, String projectName) {
    this.audioBaseUrl.set(audioBaseUrl);
    LOG.info("Cloning from demo template \"{}\" in parent folder {}", templateShipKey, parentPathPrefix);
    CloseableHttpClient httpClient = httpClientProvider.getClient();
    return cloneProject(parentPathPrefix, () -> hubClientFactory.loadApiV1(httpClient, templateShipKey, this.audioBaseUrl.get()), projectName);
  }

  @Override
  public boolean cloneFromLabProject(HubClientAccess hubAccess, String hubBaseUrl, String audioBaseUrl, String parentPathPrefix, UUID projectId, String projectName) {
    this.audioBaseUrl.set(audioBaseUrl);
    LOG.info("Cloning from lab Project[{}] in parent folder {}", projectId, parentPathPrefix);
    CloseableHttpClient httpClient = httpClientProvider.getClient();
    return cloneProject(parentPathPrefix, () -> hubClientFactory.getProjectApiV2(httpClient, hubBaseUrl, hubAccess, projectId), projectName);
  }

  @Override
  public boolean openProjectFromLocalFile(String projectFilePath) {
    LOG.info("Opening project at {}", projectFilePath);

    Matcher matcher = ProjectPathUtils.matchPrefixNameExtension(projectFilePath);
    if (!matcher.find()) {
      LOG.error("Failed to parse project path prefix and name from file path: {}", projectFilePath);
      return false;
    }
    projectPathPrefix.set(matcher.group(1));
    projectName.set(matcher.group(2));

    try {
      LOG.info("Will load project \"{}\" from {}", projectName.get(), projectFilePath);
      updateProgress(0.0);
      updateState(ProjectState.LoadingContent);
      var json = Files.readString(Path.of(projectFilePath));
      content.set(jsonProvider.getMapper().readValue(json, HubContent.class));
      project.set(content.get().getProject());
      updateState(ProjectState.LoadedContent);
      LOG.info("Did load content for project \"{}\" from {}", projectName.get(), projectFilePath);
      updateState(ProjectState.Ready);
      return true;

    } catch (Exception e) {
      LOG.error("Failed to open project from local file! {}\n{}", e, StringUtils.formatStackTrace(e));
      project.set(null);
      content.set(null);
      updateState(ProjectState.Failed);
      return false;
    }
  }

  @Override
  public boolean createProject(String parentPathPrefix, String projectName) {
    LOG.info("Create new project \"{}\" in parent folder {}", projectName, parentPathPrefix);

    try {
      createProjectFolder(parentPathPrefix, projectName);

      // Create the new project
      project.set(new Project());
      project.get().setId(UUID.randomUUID());
      project.get().setName(projectName);

      // Create the new content
      content.set(new HubContent());
      content.get().setProjects(List.of(project.get()));

      saveProjectContent();
      return true;

    } catch (Exception e) {
      LOG.error("Failed to open project from local file! {}\n{}", e, StringUtils.formatStackTrace(e));
      project.set(null);
      content.set(null);
      updateState(ProjectState.Failed);
      return false;
    }
  }

  @Override
  public void saveProject() {
    try {
      saveProjectContent();

    } catch (IOException e) {
      LOG.error("Failed to save project! {}\n{}", e, StringUtils.formatStackTrace(e));
      updateState(ProjectState.Ready);
    }
  }

  @Override
  public ProjectCleanupResults cleanupProject() {
    updateState(ProjectState.Saving);
    var results = new ProjectCleanupResults();
    var prefix = getInstrumentPathPrefix();
    LOG.info("Cleaning up project audio folder {}", prefix);
    Set<String> filesOnDisk = new HashSet<>();
    Set<String> foldersOnDisk = new HashSet<>();
    Set<String> filesInProject = new HashSet<>();
    Set<String> foldersInProject = new HashSet<>();
    Path prefixPath = Paths.get(prefix);
    if (Files.exists(prefixPath))
      try (var paths = Files.walk(prefixPath)) {
        paths.forEach(path -> {
          if (Files.isRegularFile(path)) {
            filesOnDisk.add(path.toString());
          } else if (Files.isDirectory(path)) {
            foldersOnDisk.add(path + File.separator);
          }
        });
      } catch (IOException e) {
        LOG.error("Failed to walk project audio folder! {}\n{}", e, StringUtils.formatStackTrace(e));
        updateState(ProjectState.Ready);
        return results;
      }
    foldersInProject.add(prefix + File.separator);
    content.get().getInstruments().forEach(instrument -> {
      var instrumentPath = getPathPrefixToInstrumentAudio(instrument.getId());
      foldersInProject.add(instrumentPath);
      content.get().getAudiosOfInstrument(instrument.getId()).forEach(audio ->
        filesInProject.add(String.format("%s%s", instrumentPath, audio.getWaveformKey())));
    });
    LOG.info("Found {} instrument folders on disk containing a total of {} audio files.", foldersOnDisk.size(), filesOnDisk.size());
    LOG.info("The project has {} instruments containing a total of {} audios.", foldersInProject.size(), filesInProject.size());
    foldersOnDisk.removeAll(foldersInProject);
    filesOnDisk.removeAll(filesInProject);
    LOG.info("Will delete {} instrument folders and {} audio files.", foldersOnDisk.size(), filesOnDisk.size());
    for (String s : filesOnDisk) {
      try {
        Files.deleteIfExists(Paths.get(s));
        results.incrementFiles();
      } catch (IOException e) {
        LOG.error("Failed to delete audio file \"{}\"! {}\n{}", s, e, StringUtils.formatStackTrace(e));
        updateState(ProjectState.Ready);
        return results;
      }
    }
    for (String path : foldersOnDisk) {
      try {
        FileUtils.deleteDirectory(new File(path));
        results.incrementFolders();
      } catch (IOException e) {
        LOG.error("Failed to delete instrument folder {}! {}\n{}", path, e, StringUtils.formatStackTrace(e));
        updateState(ProjectState.Ready);
        return results;
      }
    }

    // Cleanup orphans
    results.addEntities(deleteAllIf(Instrument.class, (Instrument instrument) -> content.get().getLibrary(instrument.getLibraryId()).isEmpty()));
    results.addEntities(deleteAllIf(InstrumentMeme.class, (InstrumentMeme meme) -> content.get().getInstrument(meme.getInstrumentId()).isEmpty()));
    results.addEntities(deleteAllIf(InstrumentAudio.class, (InstrumentAudio audio) -> content.get().getInstrument(audio.getInstrumentId()).isEmpty()));
    results.addEntities(deleteAllIf(Program.class, (Program program) -> content.get().getLibrary(program.getLibraryId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramMeme.class, (ProgramMeme meme) -> content.get().getProgram(meme.getProgramId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequence.class, (ProgramSequence sequence) -> content.get().getProgram(sequence.getProgramId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequenceBinding.class, (ProgramSequenceBinding binding) -> content.get().getProgramSequence(binding.getProgramSequenceId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequenceBinding.class, (ProgramSequenceBinding binding) -> content.get().getProgram(binding.getProgramId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequenceBindingMeme.class, (ProgramSequenceBindingMeme meme) -> content.get().getProgramSequenceBinding(meme.getProgramSequenceBindingId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequenceBindingMeme.class, (ProgramSequenceBindingMeme meme) -> content.get().getProgram(meme.getProgramId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramVoice.class, (ProgramVoice voice) -> content.get().getProgram(voice.getProgramId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramVoiceTrack.class, (ProgramVoiceTrack track) -> content.get().getProgramVoice(track.getProgramVoiceId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramVoiceTrack.class, (ProgramVoiceTrack track) -> content.get().getProgram(track.getProgramId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequencePattern.class, (ProgramSequencePattern pattern) -> content.get().getProgramSequence(pattern.getProgramSequenceId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequencePattern.class, (ProgramSequencePattern pattern) -> content.get().getProgramVoice(pattern.getProgramVoiceId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequencePattern.class, (ProgramSequencePattern pattern) -> content.get().getProgram(pattern.getProgramId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequencePatternEvent.class, (ProgramSequencePatternEvent event) -> content.get().getProgramSequencePattern(event.getProgramSequencePatternId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequencePatternEvent.class, (ProgramSequencePatternEvent event) -> content.get().getProgramVoiceTrack(event.getProgramVoiceTrackId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequencePatternEvent.class, (ProgramSequencePatternEvent event) -> content.get().getProgram(event.getProgramId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequenceChord.class, (ProgramSequenceChord chord) -> content.get().getProgramSequence(chord.getProgramSequenceId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequenceChord.class, (ProgramSequenceChord chord) -> content.get().getProgram(chord.getProgramId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequenceChordVoicing.class, (ProgramSequenceChordVoicing voicing) -> content.get().getProgramSequenceChord(voicing.getProgramSequenceChordId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequenceChordVoicing.class, (ProgramSequenceChordVoicing voicing) -> content.get().getProgramVoice(voicing.getProgramVoiceId()).isEmpty()));
    results.addEntities(deleteAllIf(ProgramSequenceChordVoicing.class, (ProgramSequenceChordVoicing voicing) -> content.get().getProgram(voicing.getProgramId()).isEmpty()));
    results.addEntities(deleteAllIf(TemplateBinding.class, (TemplateBinding binding) -> content.get().getTemplate(binding.getTemplateId()).isEmpty()));

    try {
      saveProjectContent();
    } catch (IOException e) {
      LOG.error("Failed to save project after cleanup! {}\n{}", e, StringUtils.formatStackTrace(e));
      updateState(ProjectState.Ready);
      return results;
    }

    updateState(ProjectState.Ready);
    return results;
  }

  @Override
  public ProjectPushResults pushProject(HubClientAccess hubAccess, String hubBaseUrl, String audioBaseUrl) {
    this.audioBaseUrl.set(audioBaseUrl);
    var pushResults = new ProjectPushResults();
    try {
      // Don't close the client, only close the responses from it
      CloseableHttpClient httpClient = httpClientProvider.getClient();

      // First, publish the entire project content as a payload to Hub.
      updateState(ProjectState.PushingContent);
      LOG.info("Will push project content to Hub");
      hubClientFactory.postProjectSyncApiV2(httpClient, hubBaseUrl, hubAccess, content.get());
      pushResults.addAudios(content.get().getInstrumentAudios().size());
      pushResults.addPrograms(content.get().getPrograms().size());
      pushResults.addLibraries(content.get().getLibraries().size());
      pushResults.addTemplates(content.get().getTemplates().size());
      LOG.info("Pushed project content to Hub");
      updateState(ProjectState.PushedContent);

      // Then, push all individual audios. If an audio is not found remotely, request an upload authorization token from Hub.
      var instruments = new ArrayList<>(content.get().getInstruments());
      var audios = new ArrayList<>(content.get().getInstrumentAudios());
      LOG.info("Will push {} audio for {} instruments", audios.size(), instruments.size());
      updateProgress(0.0);
      updateProgressLabel(String.format("Pushed 0/%d audios for 0/%d instruments", audios.size(), instruments.size()));
      updateState(ProjectState.PushingAudio);

      for (Instrument instrument : instruments) {
        for (InstrumentAudio audio : audios.stream()
          .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
          .sorted(Comparator.comparing(InstrumentAudio::getName))
          .toList()) {
          if (!Objects.equals(state.get(), ProjectState.PushingAudio)) {
            // Workstation canceling pushing should cease uploading audio files https://www.pivotaltracker.com/story/show/186209135
            return pushResults;
          }
          if (!StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
            LOG.debug("Will upload audio for instrument \"{}\" with waveform key \"{}\"", instrument.getName(), audio.getWaveformKey());
            // Fetch via HTTP if original does not exist
            var pathOnDisk = getPathToInstrumentAudio(
              instrument.getId(),
              audio.getWaveformKey()
            );

            var remoteUrl = String.format("%s%s", this.audioBaseUrl, audio.getWaveformKey());
            var remoteFileSize = hubClientFactory.getRemoteFileSize(httpClient, remoteUrl);
            var upload = new ProjectAudioUpload(audio.getId(), pathOnDisk);
            boolean shouldUpload = false;

            if (remoteFileSize == FILE_SIZE_NOT_FOUND) {
              LOG.info("File {} not found remotely - Will upload {} bytes", remoteUrl, upload.getContentLength());
              shouldUpload = true;
            } else if (upload.getContentLength() != remoteFileSize) {
              LOG.info("File size of {} does not match remote {} - Will upload {} bytes from {}", pathOnDisk, remoteFileSize, remoteFileSize, remoteUrl);
              shouldUpload = true;
            }

            // When requesting upload authorization, it's necessary to specify an existing instrument. Hub will compute the waveform key.
            if (shouldUpload) {
              hubClientFactory.uploadInstrumentAudioFile(hubAccess, hubBaseUrl, httpClient, upload);
              if (upload.hasErrors()) {
                pushResults.addErrors(upload.getErrors());
                updateState(ProjectState.Ready);
                return pushResults;
              }
              LOG.debug("Did upload audio OK");
              // After upload, we must update the local content with the new waveform key and rename the audio file on disk.
              content.get().update(InstrumentAudio.class, audio.getId(), "waveformKey", upload.getAuth().getWaveformKey());
              var updatedPathOnDisk = getPathToInstrumentAudio(
                instrument.getId(),
                upload.getAuth().getWaveformKey()
              );
              if (!Objects.equals(pathOnDisk, updatedPathOnDisk)) {
                LOG.info("After upload, will rename audio file from {} to {}", pathOnDisk, updatedPathOnDisk);
                Files.move(Paths.get(pathOnDisk), Paths.get(updatedPathOnDisk));
              }
              pushResults.incrementAudiosUploaded();
            }
          }
          updateProgress((float) pushResults.getAudios() / audios.size());
          updateProgressLabel(String.format("Pushed %d/%d audios for %d/%d instruments", pushResults.getAudios(), audios.size(), pushResults.getInstruments(), instruments.size()));
        }
        pushResults.incrementInstruments();
      }
      updateProgress(1.0);
      updateProgressLabel(String.format("Pushed %d audios for %d instruments", pushResults.getAudios(), pushResults.getInstruments()));
      LOG.info("Pushed {} audios for {} instruments", pushResults.getAudios(), pushResults.getInstruments());
      updateState(ProjectState.PushedAudio);

      // Save project after push, because instrument audio waveform keys may have been updated
      saveProject();

      updateState(ProjectState.Ready);
      return pushResults;

    } catch (HubClientException e) {
      pushResults.addError(String.format("Failed to push project because %s", e.getCause().getMessage()));
      updateState(ProjectState.Ready);
      return pushResults;

    } catch (IOException e) {
      pushResults.addError(String.format("Failed to push project because of I/O failure: %s", e.getMessage()));
      updateState(ProjectState.Ready);
      return pushResults;

    } catch (Exception e) {
      pushResults.addError(String.format("Failed to push project because of unknown error: %s", e.getMessage()));
      updateState(ProjectState.Ready);
      return pushResults;
    }
  }

  @Override
  public void cancelProjectLoading() {
    updateState(ProjectState.Cancelled);
  }

  @Override
  public String getProjectFilename() {
    return projectName.get() + ".xj";
  }

  @Override
  public String getPathToProjectFile() {
    return projectPathPrefix.get() + getProjectFilename();
  }

  @Override
  public void setProjectPathPrefix(String projectPathPrefix) {
    this.projectPathPrefix.set(projectPathPrefix);
  }

  @Override
  public HubContent getContent() {
    return content.get();
  }

  @Override
  public String getPathToInstrumentAudio(UUID instrumentId, String waveformKey) {
    return getPathPrefixToInstrumentAudio(instrumentId) + waveformKey;
  }

  @Override
  public String getPathPrefixToInstrumentAudio(UUID instrumentId) {
    return getInstrumentPathPrefix() + instrumentId.toString() + File.separator;
  }

  @Override
  public void setOnProgress(@Nullable Consumer<Double> onProgress) {
    this.onProgress = onProgress;
  }

  @Override
  public void setOnProgressLabel(@Nullable Consumer<String> onProgressLabel) {
    this.onProgressLabel = onProgressLabel;
  }

  @Override
  public void setOnStateChange(@Nullable Consumer<ProjectState> onStateChange) {
    this.onStateChange = onStateChange;
  }

  @Override
  public void closeProject() {
    project.set(null);
    content.set(null);
  }

  @Override
  public Template createTemplate(String name) throws Exception {
    var existingTemplates = content.get().getTemplates();

    // New Create a template, increment a numerical suffix to make each sequence unique, e.g. "New Sequence 2" then "New Sequence 3"
    var existingNames = existingTemplates.stream().map(Template::getName).collect(Collectors.toSet());
    var actualName = FormatUtils.iterateNumericalSuffixFromExisting(existingNames, name);

    var template = new Template();
    template.setId(UUID.randomUUID());
    template.setProjectId(project.get().getId());
    template.setName(actualName);
    template.setConfig(new TemplateConfig().toString());
    template.setIsDeleted(false);
    content.get().put(template);
    return template;
  }

  @Override
  public Library createLibrary(String name) throws Exception {
    var existingLibraries = content.get().getLibraries();

    // New Create a library, increment a numerical suffix to make each sequence unique, e.g. "New Sequence 2" then "New Sequence 3"
    var existingNames = existingLibraries.stream().map(Library::getName).collect(Collectors.toSet());
    var actualName = FormatUtils.iterateNumericalSuffixFromExisting(existingNames, name);

    var library = new Library();
    library.setId(UUID.randomUUID());
    library.setProjectId(project.get().getId());
    library.setName(actualName);
    library.setIsDeleted(false);
    content.get().put(library);
    return library;
  }

  @Override
  public Program createProgram(Library library, String name) throws Exception {
    var existingProgramsOfLibrary = content.get().getProgramsOfLibrary(library.getId());
    var existingProgramOfLibrary = existingProgramsOfLibrary.stream().findFirst();
    var existingProgram = existingProgramOfLibrary.isPresent() ? existingProgramOfLibrary : content.get().getPrograms().stream().findFirst();

    // New Create a program, increment a numerical suffix to make each sequence unique, e.g. "New Program 2" then "New Program 3"
    var existingNames = existingProgramsOfLibrary.stream().map(Program::getName).collect(Collectors.toSet());
    var actualName = FormatUtils.iterateNumericalSuffixFromExisting(existingNames, name);

    var program = new Program();
    program.setId(UUID.randomUUID());
    program.setName(actualName);
    program.setLibraryId(library.getId());
    program.setConfig(new ProgramConfig().toString());
    program.setType(existingProgram.map(Program::getType).orElse(DEFAULT_PROGRAM_TYPE));
    program.setState(existingProgram.map(Program::getState).orElse(DEFAULT_PROGRAM_STATE));
    program.setTempo(existingProgram.map(Program::getTempo).orElse(DEFAULT_TEMPO));
    program.setKey(existingProgram.map(Program::getKey).orElse(DEFAULT_KEY));
    program.setIsDeleted(false);
    content.get().put(program);
    return program;
  }

  @Override
  public ProgramSequence createProgramSequence(UUID programId) throws Exception {
    var program = content.get().getProgram(programId).orElseThrow(() -> new NexusException("Program not found"));
    var library = content.get().getLibrary(program.getLibraryId()).orElseThrow(() -> new NexusException("Library not found"));
    var project = content.get().getProject();
    if (Objects.isNull(project)) throw new NexusException("Project not found");
    var existingSequencesOfProgram = content.get().getSequencesOfProgram(program.getId());
    var existingSequenceOfProgram = existingSequencesOfProgram.stream().findFirst();
    var existingSequenceOfLibrary = existingSequenceOfProgram.isPresent() ? existingSequenceOfProgram : content.get().getProgramsOfLibrary(library).stream().flatMap(i -> content.get().getSequencesOfProgram(i.getId()).stream()).findFirst();
    var existingSequence = existingSequenceOfLibrary.isPresent() ? existingSequenceOfLibrary : content.get().getProgramSequences().stream().findFirst();

    // New Create a sequence, increment a numerical suffix to make each sequence unique, e.g. "New Sequence 2" then "New Sequence 3"
    var existingSequenceNames = existingSequencesOfProgram.stream().map(ProgramSequence::getName).collect(Collectors.toSet());
    var newSequenceName = FormatUtils.iterateNumericalSuffixFromExisting(existingSequenceNames, DEFAULT_PROGRAM_SEQUENCE_NAME);

    // Prepare the sequence record
    var sequence = new ProgramSequence();
    sequence.setId(UUID.randomUUID());
    sequence.setName(newSequenceName);
    sequence.setTotal(existingSequence.map(ProgramSequence::getTotal).orElse(DEFAULT_PROGRAM_SEQUENCE_TOTAL.shortValue()));
    sequence.setKey(existingSequence.map(ProgramSequence::getKey).orElse(DEFAULT_KEY));
    sequence.setIntensity(existingSequence.map(ProgramSequence::getIntensity).orElse(DEFAULT_INTENSITY));
    sequence.setIntensity(
      existingSequenceOfLibrary.map(ProgramSequence::getIntensity).orElse(
        existingSequence.map(ProgramSequence::getIntensity).orElse(
          DEFAULT_INTENSITY
        )));
    sequence.setProgramId(program.getId());

    content.get().put(sequence);
    return sequence;
  }

  @Override
  public ProgramSequencePattern createProgramSequencePattern(UUID programId, UUID programSequenceId, UUID programVoiceId) throws Exception {
    var program = content.get().getProgram(programId).orElseThrow(() -> new NexusException("Program not found"));
    var sequence = content.get().getProgramSequence(programSequenceId).orElseThrow(() -> new NexusException("Sequence not found"));
    var voice = content.get().getProgramVoice(programVoiceId).orElseThrow(() -> new NexusException("Voice not found"));
    var existingPatternsOfSequence = content.get().getPatternsOfSequence(sequence.getId());
    var existingPatternOfSequence = existingPatternsOfSequence.stream().findFirst();
    var existingPattern = existingPatternOfSequence.isPresent() ? existingPatternOfSequence : content.get().getProgramSequencePatterns().stream().findFirst();

    // New Create a pattern, increment a numerical suffix to make each sequence unique, e.g. "New Pattern 2" then "New Pattern 3"
    var existingPatternNames = existingPatternsOfSequence.stream().map(ProgramSequencePattern::getName).collect(Collectors.toSet());
    var newPatternName = FormatUtils.iterateNumericalSuffixFromExisting(existingPatternNames, DEFAULT_PROGRAM_SEQUENCE_PATTERN_NAME);

    // Prepare the pattern record
    var pattern = new ProgramSequencePattern();
    pattern.setId(UUID.randomUUID());
    pattern.setName(newPatternName);
    pattern.setTotal(existingPattern.map(ProgramSequencePattern::getTotal).orElse(sequence.getTotal()));
    pattern.setProgramId(program.getId());
    pattern.setProgramSequenceId(sequence.getId());
    pattern.setProgramVoiceId(voice.getId());

    content.get().put(pattern);
    return pattern;
  }

  @Override
  public ProgramSequencePatternEvent createProgramSequencePatternEvent(UUID trackId, UUID patternId, double position, double duration) throws Exception {
    var track = content.get().getProgramVoiceTrack(trackId).orElseThrow(() -> new NexusException("Track not found"));
    var pattern = content.get().getProgramSequencePattern(patternId).orElseThrow(() -> new NexusException("Pattern not found"));

    // Prepare the event record
    var event = new ProgramSequencePatternEvent();
    event.setId(UUID.randomUUID());
    event.setProgramId(track.getProgramId());
    event.setProgramVoiceTrackId(track.getId());
    event.setProgramSequencePatternId(pattern.getId());
    event.setPosition((float) position);
    event.setVelocity(1.0f);
    event.setTones(DEFAULT_PROGRAM_SEQUENCE_PATTERN_EVENT_TONES);
    event.setDuration((float) duration);


    content.get().put(event);
    return event;
  }

  @Override
  public ProgramVoice createProgramVoice(UUID programId) throws Exception {
    var program = content.get().getProgram(programId).orElseThrow(() -> new NexusException("Program not found"));
    var project = content.get().getProject();
    if (Objects.isNull(project)) throw new NexusException("Project not found");
    var existingVoicesOfProgram = content.get().getVoicesOfProgram(program.getId());

    // New Create a voice, increment a numerical suffix to make each voice unique, e.g. "New Voice 2" then "New Voice 3"
    var existingVoiceNames = existingVoicesOfProgram.stream().map(ProgramVoice::getName).collect(Collectors.toSet());
    var newVoiceName = FormatUtils.iterateNumericalSuffixFromExisting(existingVoiceNames, DEFAULT_PROGRAM_VOICE_NAME);

    // Use a type not seen in another voice, if possible
    var existingTypes = existingVoicesOfProgram.stream().map(ProgramVoice::getType).collect(Collectors.toSet());
    var types = Arrays.stream(InstrumentType.values()).collect(Collectors.toSet());
    types.removeAll(existingTypes);

    // Prepare the voice record
    var voice = new ProgramVoice();
    voice.setId(UUID.randomUUID());
    voice.setName(newVoiceName);
    voice.setType(types.stream().sorted().findFirst().orElse(InstrumentType.Drum));
    voice.setOrder(existingVoicesOfProgram.stream().map(ProgramVoice::getOrder).max(Float::compareTo).orElse(0f) + 1);
    voice.setProgramId(program.getId());

    content.get().put(voice);
    return voice;
  }

  @Override
  public ProgramVoiceTrack createProgramVoiceTrack(UUID voiceId) throws Exception {
    var voice = content.get().getProgramVoice(voiceId).orElseThrow(() -> new NexusException("Voice not found"));
    var existingTracksOfVoice = content.get().getTracksOfVoice(voice.getId());

    // New Create a track, increment a numerical suffix to make each track unique, e.g. "New Track 2" then "New Track 3"
    var existingTracksOfProgram = content.get().getTracksOfProgram(voice.getProgramId());
    var existingTrackNames = existingTracksOfProgram.stream().map(ProgramVoiceTrack::getName).collect(Collectors.toSet());
    var newTrackName = FormatUtils.iterateNumericalSuffixFromExisting(existingTrackNames, DEFAULT_PROGRAM_VOICE_TRACK_NAME);

    // Prepare the track record
    var track = new ProgramVoiceTrack();
    track.setId(UUID.randomUUID());
    track.setName(newTrackName);
    track.setOrder(existingTracksOfVoice.stream().map(ProgramVoiceTrack::getOrder).max(Float::compareTo).orElse(0f) + 1);
    track.setProgramId(voice.getProgramId());
    track.setProgramVoiceId(voice.getId());

    content.get().put(track);
    return track;
  }

  @Override
  public ProgramMeme createProgramMeme(UUID programId) throws Exception {
    var meme = new ProgramMeme();
    meme.setId(UUID.randomUUID());
    meme.setName(DEFAULT_MEME_NAME);
    meme.setProgramId(programId);
    content.get().put(meme);
    return meme;
  }

  @Override
  public ProgramSequenceBindingMeme createProgramSequenceBindingMeme(UUID programSequenceBindingId) throws Exception {
    var meme = new ProgramSequenceBindingMeme();
    meme.setId(UUID.randomUUID());
    meme.setName(DEFAULT_MEME_NAME);
    meme.setProgramSequenceBindingId(programSequenceBindingId);
    content.get().put(meme);
    return meme;
  }

  @Override
  public InstrumentMeme createInstrumentMeme(UUID instrumentId) throws Exception {
    var meme = new InstrumentMeme();
    meme.setId(UUID.randomUUID());
    meme.setName(DEFAULT_MEME_NAME);
    meme.setInstrumentId(instrumentId);
    content.get().put(meme);
    return meme;
  }

  @Override
  public Instrument createInstrument(Library library, String name) throws Exception {
    var existingInstrumentsOfLibrary = content.get().getInstrumentsOfLibrary(library.getId());
    var existingInstrumentOfLibrary = existingInstrumentsOfLibrary.stream().findFirst();
    var existingInstrument = existingInstrumentOfLibrary.isPresent() ? existingInstrumentOfLibrary : content.get().getInstruments().stream().findFirst();

    // New Create a instrument, increment a numerical suffix to make each sequence unique, e.g. "New Instrument 2" then "New Instrument 3"
    var existingNames = existingInstrumentsOfLibrary.stream().map(Instrument::getName).collect(Collectors.toSet());
    var actualName = FormatUtils.iterateNumericalSuffixFromExisting(existingNames, name);

    var instrument = new Instrument();
    instrument.setId(UUID.randomUUID());
    instrument.setName(actualName);
    instrument.setLibraryId(library.getId());
    instrument.setConfig(new InstrumentConfig().toString());
    instrument.setType(existingInstrument.map(Instrument::getType).orElse(DEFAULT_INSTRUMENT_TYPE));
    instrument.setMode(existingInstrument.map(Instrument::getMode).orElse(DEFAULT_INSTRUMENT_MODE));
    instrument.setState(existingInstrument.map(Instrument::getState).orElse(DEFAULT_INSTRUMENT_STATE));
    instrument.setVolume(existingInstrument.map(Instrument::getVolume).orElse(DEFAULT_VOLUME));
    instrument.setIsDeleted(false);
    var instrumentPath = getPathPrefixToInstrumentAudio(instrument.getId());
    FileUtils.createParentDirectories(new File(instrumentPath));
    content.get().put(instrument);
    return instrument;
  }

  @Override
  public InstrumentAudio createInstrumentAudio(Instrument instrument, String audioFilePath) throws Exception {
    var library = content.get().getLibrary(instrument.getLibraryId()).orElseThrow(() -> new NexusException("Library not found"));
    var project = content.get().getProject();
    if (Objects.isNull(project)) throw new NexusException("Project not found");
    var existingAudioOfInstrument = content.get().getAudiosOfInstrument(instrument.getId()).stream().findFirst();
    var existingAudioOfLibrary = existingAudioOfInstrument.isPresent() ? existingAudioOfInstrument : content.get().getInstrumentsOfLibrary(library).stream().flatMap(i -> content.get().getAudiosOfInstrument(i.getId()).stream()).findFirst();
    var existingAudio = existingAudioOfLibrary.isPresent() ? existingAudioOfLibrary : content.get().getInstrumentAudios().stream().findFirst();
    var existingProgramOfLibrary = content.get().getProgramsOfLibrary(library.getId()).stream().findFirst();
    var existingProgram = existingProgramOfLibrary.isPresent() ? existingProgramOfLibrary : content.get().getPrograms().stream().findFirst();

    // extract the file name and extension
    Matcher matcher = ProjectPathUtils.matchPrefixNameExtension(audioFilePath);
    if (!matcher.find()) {
      throw new RuntimeException(String.format("Failed to parse project path prefix and name from file path: %s", audioFilePath));
    }

    // Prepare the audio record
    var audio = new InstrumentAudio();
    audio.setId(UUID.randomUUID());
    audio.setName(matcher.group(2));
    audio.setTones(existingAudio.map(InstrumentAudio::getTones).orElse(DEFAULT_INSTRUMENT_AUDIO_TONES));
    audio.setEvent(existingAudio.map(InstrumentAudio::getEvent).orElse(DEFAULT_INSTRUMENT_AUDIO_EVENT));
    audio.setIntensity(existingAudio.map(InstrumentAudio::getIntensity).orElse(DEFAULT_INTENSITY));
    audio.setTempo(
      existingAudioOfLibrary.map(InstrumentAudio::getTempo).orElse(
        existingProgramOfLibrary.map(Program::getTempo).orElse(
          existingAudio.map(InstrumentAudio::getTempo).orElse(
            existingProgram.map(Program::getTempo).orElse(DEFAULT_TEMPO)
          ))
      ));
    audio.setLoopBeats(existingAudio.map(InstrumentAudio::getLoopBeats).orElse(DEFAULT_LOOP_BEATS));
    audio.setTransientSeconds(0.0f);
    audio.setVolume(existingAudio.map(InstrumentAudio::getVolume).orElse(DEFAULT_VOLUME));
    audio.setInstrumentId(instrument.getId());
    audio.setWaveformKey(computeWaveformKey(project.getName(), library.getName(), instrument.getName(), audio, matcher.group(3)));

    // Import the audio waveform
    importAudio(audio, audioFilePath);

    content.get().put(audio);
    return audio;
  }

  @Override
  public Program moveProgram(UUID id, UUID libraryId) throws Exception {
    var program = content.get().getProgram(id).orElseThrow(() -> new NexusException("Program not found"));
    program.setLibraryId(libraryId);
    content.get().put(program);
    return program;
  }

  @Override
  public Instrument moveInstrument(UUID id, UUID libraryId) throws Exception {
    var instrument = content.get().getInstrument(id).orElseThrow(() -> new NexusException("Instrument not found"));
    instrument.setLibraryId(libraryId);
    content.get().put(instrument);
    return instrument;
  }

  @Override
  public void renameWaveformIfNecessary(UUID instrumentAudioId) throws Exception {
    var audio = content.get().getInstrumentAudio(instrumentAudioId)
      .orElseThrow(() -> new RuntimeException("Could not find Instrument Audio"));
    var library = content.get().getInstrument(audio.getInstrumentId()).orElseThrow(() -> new NexusException("Instrument not found"));
    var project = content.get().getProject();
    var extension = ProjectPathUtils.getExtension(File.separator + audio.getWaveformKey());
    var toWaveformKey = computeWaveformKey(project.getName(), library.getName(), library.getName(), audio, extension);
    var toPath = getPathToInstrumentAudio(library.getId(), toWaveformKey);

    if (!Objects.equals(audio.getWaveformKey(), toWaveformKey)) {
      var fromPath = getPathToInstrumentAudio(library.getId(), audio.getWaveformKey());
      FileUtils.copyFile(new File(fromPath), new File(toPath));
      content.get().update(InstrumentAudio.class, instrumentAudioId, "waveformKey", toWaveformKey);
    }
  }

  @Override
  public Template cloneTemplate(UUID fromId, String name) throws Exception {
    var source = content.get().getTemplate(fromId).orElseThrow(() -> new NexusException("Template not found"));

    // Clone the Template
    var clone = new Template();
    clone.setId(UUID.randomUUID());
    entityFactory.setAllEmptyAttributes(source, clone);
    content.get().put(clone);

    // Clone the Template's Bindings
    var clonedBindings = entityFactory.cloneAll(content.get().getBindingsOfTemplate(fromId), Set.of(clone));
    content.get().putAll(clonedBindings.values());

    return clone;
  }

  @Override
  public Library cloneLibrary(UUID fromId, String name) throws Exception {
    var source = content.get().getLibrary(fromId).orElseThrow(() -> new NexusException("Library not found"));

    // Clone the Library and put it in the store
    var library = entityFactory.clone(source);
    library.setName(name);
    content.get().put(library);

    // Clone the Library's Programs
    for (Program program : content.get().getProgramsOfLibrary(fromId)) {
      cloneProgram(program.getId(), library.getId(), String.format("Copy of %s", program.getName()));
    }

    // Clone the Library's Instruments
    for (Instrument instrument : content.get().getInstrumentsOfLibrary(fromId)) {
      cloneInstrument(instrument.getId(), library.getId(), String.format("Copy of %s", instrument.getName()));
    }

    // Return the library
    return library;
  }

  @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
  @Override
  public Program cloneProgram(UUID fromId, UUID libraryId, String name) throws Exception {
    var program = content.get().getProgram(fromId).orElseThrow(() -> new NexusException("Program not found"));

    // Clone Program
    var clonedProgram = entityFactory.clone(program);
    clonedProgram.setLibraryId(libraryId);
    clonedProgram.setName(name);

    // Prepare all maps of cloned sub-entities to avoid putting more than once to store
    Map<UUID, ProgramMeme> clonedProgramMemes = new HashMap<>();
    Map<UUID, ProgramSequence> clonedProgramSequences = new HashMap<>();
    Map<UUID, ProgramSequenceBinding> clonedProgramSequenceBindings = new HashMap<>();
    Map<UUID, ProgramSequenceBindingMeme> clonedProgramSequenceBindingMemes = new HashMap<>();
    Map<UUID, ProgramSequenceChord> clonedProgramSequenceChords = new HashMap<>();
    Map<UUID, ProgramSequenceChordVoicing> clonedProgramSequenceChordVoicings = new HashMap<>();
    Map<UUID, ProgramSequencePattern> clonedProgramSequencePatterns = new HashMap<>();
    Map<UUID, ProgramSequencePatternEvent> clonedProgramSequencePatternEvents = new HashMap<>();
    Map<UUID, ProgramVoice> clonedProgramVoices = new HashMap<>();
    Map<UUID, ProgramVoiceTrack> clonedProgramVoiceTracks = new HashMap<>();

    // Clone the Program's Sequences
    var sequences = content.get().getSequencesOfProgram(fromId);
    clonedProgramSequences.putAll(entityFactory.cloneAll(sequences, Set.of(clonedProgram)));

    // Clone the Program's Memes
    clonedProgramMemes.putAll(entityFactory.cloneAll(content.get().getMemesOfProgram(fromId), Set.of(clonedProgram)));

    // Clone the Program's Voices
    var voices = content.get().getVoicesOfProgram(fromId);
    clonedProgramVoices.putAll(entityFactory.cloneAll(voices, Set.of(clonedProgram)));

    // Iterate through the cloned voices and clone all the Program's Voice's Tracks
    Collection<ProgramVoiceTrack> tracks = content.get().getTracksOfProgram(fromId);
    for (ProgramVoice voice : voices) {
      var clonedVoice = clonedProgramVoices.get(voice.getId());
      clonedProgramVoiceTracks.putAll(entityFactory.cloneAll(content.get().getTracksOfVoice(voice.getId()), Set.of(clonedProgram, clonedVoice)));
    }

    // Iterate through the cloned sequences
    for (ProgramSequence sequence : sequences) {
      var clonedSequence = clonedProgramSequences.get(sequence.getId());

      // Clone the Program's Sequence's Patterns
      var patterns = content.get().getPatternsOfSequence(sequence.getId());
      var clonedPatterns = entityFactory.cloneAll(patterns, Set.of(clonedProgram, clonedSequence));
      clonedProgramSequencePatterns.putAll(clonedPatterns);

      // Iterate through the cloned patterns and tracks and clone all the Program's Sequences' Patterns' Events
      for (ProgramSequencePattern pattern : patterns) {
        var clonedPattern = clonedPatterns.get(pattern.getId());
        for (ProgramVoiceTrack track : tracks) {
          var clonedTrack = clonedProgramVoiceTracks.get(track.getId());
          clonedProgramSequencePatternEvents.putAll(entityFactory.cloneAll(content.get().getEventsOfPatternAndTrack(pattern.getId(), track.getId()), Set.of(clonedProgram, clonedPattern, clonedTrack)));
        }
      }

      // Clone the Program's Sequence's Bindings
      var bindings = content.get().getBindingsOfSequence(sequence.getId());
      clonedProgramSequenceBindings.putAll(entityFactory.cloneAll(bindings, Set.of(clonedProgram, clonedSequence)));

      // Iterate through the cloned sequence's bindings
      for (ProgramSequenceBinding binding : bindings) {
        var clonedBinding = clonedProgramSequenceBindings.get(binding.getId());

        // Clone the Program's Sequence's Bindings' Memes
        clonedProgramSequenceBindingMemes.putAll(entityFactory.cloneAll(content.get().getMemesOfSequenceBinding(binding.getId()), Set.of(clonedProgram, clonedBinding)));
      }

      // Clone the Program's Sequence's Chords
      var chords = content.get().getChordsOfSequence(sequence.getId());
      Map<UUID, ProgramSequenceChord> clonedChords = entityFactory.cloneAll(chords, Set.of(clonedProgram, clonedSequence));
      clonedProgramSequenceChords.putAll(clonedChords);

      // Iterate through the cloned chords and clone the Program's Sequences' Chords' Voicings
      for (ProgramSequenceChord chord : chords) {
        var clonedChord = clonedChords.get(chord.getId());
        for (ProgramVoice voice : voices) {
          var clonedVoice = clonedProgramVoices.get(voice.getId());
          clonedProgramSequenceChordVoicings.putAll(entityFactory.cloneAll(content.get().getVoicingsOfChordAndVoice(chord.getId(), voice.getId()), Set.of(clonedProgram, clonedChord, clonedVoice)));
        }
      }
    }

    // Put everything in the store and return the program
    content.get().put(clonedProgram);
    content.get().putAll(clonedProgramMemes.values());
    content.get().putAll(clonedProgramSequences.values());
    content.get().putAll(clonedProgramSequenceBindings.values());
    content.get().putAll(clonedProgramSequenceBindingMemes.values());
    content.get().putAll(clonedProgramSequenceChords.values());
    content.get().putAll(clonedProgramSequenceChordVoicings.values());
    content.get().putAll(clonedProgramSequencePatterns.values());
    content.get().putAll(clonedProgramSequencePatternEvents.values());
    content.get().putAll(clonedProgramVoices.values());
    content.get().putAll(clonedProgramVoiceTracks.values());

    return clonedProgram;
  }

  @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
  @Override
  public ProgramSequence cloneProgramSequence(UUID fromId) throws Exception {
    var source = content.get().getProgramSequence(fromId).orElseThrow(() -> new NexusException("Program Sequence not found"));

    // Clone Program
    var clonedSequence = entityFactory.clone(source);
    clonedSequence.setName("Clone of " + source.getName());

    // Prepare all maps of cloned sub-entities to avoid putting more than once to store
    Map<UUID, ProgramSequenceBinding> clonedProgramSequenceBindings = new HashMap<>();
    Map<UUID, ProgramSequenceBindingMeme> clonedProgramSequenceBindingMemes = new HashMap<>();
    Map<UUID, ProgramSequenceChord> clonedProgramSequenceChords = new HashMap<>();
    Map<UUID, ProgramSequenceChordVoicing> clonedProgramSequenceChordVoicings = new HashMap<>();
    Map<UUID, ProgramSequencePattern> clonedProgramSequencePatterns = new HashMap<>();
    Map<UUID, ProgramSequencePatternEvent> clonedProgramSequencePatternEvents = new HashMap<>();

    // Clone the Program's Sequence's Patterns
    var patterns = content.get().getPatternsOfSequence(fromId);
    clonedProgramSequencePatterns.putAll(entityFactory.cloneAll(patterns, Set.of(clonedSequence)));

    // Iterate through the cloned patterns and tracks and clone all the Program's Sequences' Patterns' Events
    for (ProgramSequencePattern pattern : patterns) {
      var clonedPattern = clonedProgramSequencePatterns.get(pattern.getId());
      for (ProgramVoiceTrack track : content.get().getTracksOfProgram(source.getProgramId())) {
        clonedProgramSequencePatternEvents.putAll(entityFactory.cloneAll(content.get().getEventsOfPatternAndTrack(pattern.getId(), track.getId()), Set.of(clonedPattern)));
      }
    }

    // Clone the Program's Sequence's Chords
    var chords = content.get().getChordsOfSequence(fromId);
    Map<UUID, ProgramSequenceChord> clonedChords = entityFactory.cloneAll(chords, Set.of(clonedSequence));
    clonedProgramSequenceChords.putAll(clonedChords);

    // Iterate through the cloned chords and clone the Program's Sequences' Chords' Voicings
    var voices = content.get().getVoicesOfProgram(source.getProgramId());
    for (ProgramSequenceChord chord : chords) {
      var clonedChord = clonedChords.get(chord.getId());
      for (ProgramVoice voice : voices) {
        clonedProgramSequenceChordVoicings.putAll(entityFactory.cloneAll(content.get().getVoicingsOfChordAndVoice(chord.getId(), voice.getId()), Set.of(clonedChord)));
      }
    }

    // Put everything in the store and return the program
    content.get().put(clonedSequence);
    content.get().putAll(clonedProgramSequenceBindings.values());
    content.get().putAll(clonedProgramSequenceBindingMemes.values());
    content.get().putAll(clonedProgramSequenceChords.values());
    content.get().putAll(clonedProgramSequenceChordVoicings.values());
    content.get().putAll(clonedProgramSequencePatterns.values());
    content.get().putAll(clonedProgramSequencePatternEvents.values());

    return clonedSequence;
  }

  @Override
  public ProgramSequencePattern cloneProgramSequencePattern(UUID fromId) throws Exception {
    var source = content.get().getProgramSequencePattern(fromId).orElseThrow(() -> new NexusException("Program Sequence Pattern not found"));

    // Clone Program
    var clonedPattern = entityFactory.clone(source);
    clonedPattern.setName("Clone of " + source.getName());

    // Iterate through the tracks and clone all the Program's Sequences' Patterns' Events
    Map<UUID, ProgramSequencePatternEvent> clonedProgramSequencePatternEvents = new HashMap<>(entityFactory.cloneAll(content.get().getEventsOfPattern(source.getId()), Set.of(clonedPattern)));

    // Put everything in the store and return the program
    content.get().put(clonedPattern);
    content.get().putAll(clonedProgramSequencePatternEvents.values());

    return clonedPattern;
  }

  @Override
  public Instrument cloneInstrument(UUID fromId, UUID libraryId, String name) throws Exception {
    var source = content.get().getInstrument(fromId).orElseThrow(() -> new NexusException("Instrument not found"));

    // Clone the Instrument
    var instrument = entityFactory.clone(source);
    instrument.setLibraryId(libraryId);
    instrument.setName(name);

    // Clone the Instrument's Audios
    var clonedAudios = entityFactory.cloneAll(content.get().getAudiosOfInstrument(fromId), Set.of(instrument));

    // Clone the Instrument's Memes
    var clonedMemes = entityFactory.cloneAll(content.get().getMemesOfInstrument(fromId), Set.of(instrument));

    // Put everything in the store
    content.get().put(instrument);
    content.get().putAll(clonedMemes.values());
    content.get().putAll(clonedAudios.values());

    // Copy all the instrument's audio to the new folder
    for (InstrumentAudio audio : clonedAudios.values()) {
      var fromPath = getPathToInstrumentAudio(fromId, audio.getWaveformKey());
      var toPath = getPathToInstrumentAudio(instrument.getId(), audio.getWaveformKey());
      if (new File(fromPath).exists())
        FileUtils.copyFile(new File(fromPath), new File(toPath));
    }

    // return the instrument
    return instrument;
  }

  /**
   Delete all entities of the given type if they meet a condition

   @param type to delete if its test returns true
   @param test to run on each entity
   @param <N>  type of entity
   @return the number of entities deleted
   */
  private <N> int deleteAllIf(Class<N> type, Function<N, Boolean> test) {
    int count = 0;
    for (N entity : content.get().getAll(type)) {
      try {
        if (test.apply(entity)) {
          content.get().delete(type, EntityUtils.getId(entity));
          count++;
        }
      } catch (Exception e) {
        LOG.error("Failed to test if {} is orphaned! {}\n{}", type.getSimpleName(), e, StringUtils.formatStackTrace(e));
      }
    }
    return count;
  }

  /**
   Get the instrument path prefix

   @return the instrument path prefix
   */
  private String getInstrumentPathPrefix() {
    return projectPathPrefix.get() + "instrument" + File.separator;
  }

  /**
   Import an audio waveform file into the project from somewhere else on disk

   @param audio         for which to import the waveform
   @param audioFilePath path to the audio file on disk
   @throws IOException if the audio file could not be imported
   */
  private void importAudio(InstrumentAudio audio, String audioFilePath) throws IOException {
    var targetPath = getPathToInstrumentAudio(audio.getInstrumentId(), audio.getWaveformKey());
    FileUtils.createParentDirectories(new File(targetPath));
    FileUtils.copyFile(new File(audioFilePath), new File(targetPath));
  }

  /**
   Clone a project from a template

   @param parentPathPrefix parent folder of the project folder
   @param fetchContent     fetch content from the hub
   @param projectName      name of the project folder
   @return true if the project was cloned successfully
   */
  private boolean cloneProject(String parentPathPrefix, Callable<HubContent> fetchContent, String projectName) {
    try {
      createProjectFolder(parentPathPrefix, projectName);

      LOG.info("Will load content");
      updateProgress(0.0);
      updateState(ProjectState.LoadingContent);
      content.set(fetchContent.call());
      project.set(content.get().getProject());
      updateState(ProjectState.LoadedContent);
      LOG.info("Did load content");

      var instruments = new ArrayList<>(content.get().getInstruments());
      var audios = new ArrayList<>(content.get().getInstrumentAudios());
      LOG.info("Will download {} audio for {} instruments", audios.size(), instruments.size());
      updateProgressLabel(String.format("Downloaded 0/%d audios for 0/%d instruments", audios.size(), instruments.size()));
      updateState(ProjectState.LoadingAudio);
      int loadedAudios = 0;
      int loadedInstruments = 0;

      // Don't close the client, only close the responses from it
      CloseableHttpClient httpClient = httpClientProvider.getClient();

      for (Instrument instrument : instruments) {
        for (InstrumentAudio audio : audios.stream()
          .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
          .sorted(Comparator.comparing(InstrumentAudio::getName))
          .toList()) {
          if (!Objects.equals(state.get(), ProjectState.LoadingAudio)) {
            // Workstation canceling preloading should cease resampling audio files https://www.pivotaltracker.com/story/show/186209135
            project.set(null);
            return false;
          }
          if (!StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
            LOG.debug("Will download audio for instrument \"{}\" with waveform key \"{}\"", instrument.getName(), audio.getWaveformKey());
            // Fetch via HTTP if original does not exist
            var originalCachePath = getPathToInstrumentAudio(
              instrument.getId(),
              audio.getWaveformKey()
            );

            var remoteUrl = String.format("%s%s", this.audioBaseUrl, audio.getWaveformKey());
            var remoteFileSize = hubClientFactory.getRemoteFileSize(httpClient, remoteUrl);
            if (remoteFileSize == FILE_SIZE_NOT_FOUND) {
              LOG.error("File not found for audio \"{}\" of instrument \"{}\" in library \"{}\" at {}", instrument.getName(), audio.getName(), content.get().getLibrary(instrument.getLibraryId()).map(Library::getName).orElse("Unknown"), remoteUrl);
              updateState(ProjectState.Failed);
              project.set(null);
              return false;
            }
            var localFileSize = getFileSizeIfExistsOnDisk(originalCachePath);

            boolean shouldDownload = false;

            if (localFileSize.isEmpty()) {
              shouldDownload = true;
            } else if (localFileSize.get() != remoteFileSize) {
              LOG.info("File size of {} does not match remote {} - Will download {} bytes from {}", originalCachePath, remoteFileSize, remoteFileSize, remoteUrl);
              shouldDownload = true;
            }

            if (shouldDownload) {
              if (!hubClientFactory.downloadRemoteFileWithRetry(httpClient, remoteUrl, originalCachePath, remoteFileSize)) {
                updateState(ProjectState.Failed);
                project.set(null);
                return false;
              }
            }

            LOG.debug("Did preload audio OK");
            updateProgress((float) loadedAudios / audios.size());
            updateProgressLabel(String.format("Downloaded %d/%d audios for %d/%d instruments", loadedAudios, audios.size(), loadedInstruments, instruments.size()));
            loadedAudios++;
          }
        }
        loadedInstruments++;
      }
      updateProgress(1.0);
      LOG.info("Downloaded {} audios from {} instruments", loadedAudios, instruments.size());
      updateProgressLabel(String.format("Downloaded %d audios for %d instruments", loadedAudios, loadedInstruments));
      updateState(ProjectState.LoadedAudio);

      saveProjectContent();
      return true;

    } catch (HubClientException e) {
      LOG.error("Failed to load content for project! {}\n{}", e, StringUtils.formatStackTrace(e));
      updateState(ProjectState.Failed);
      project.set(null);
      return false;

    } catch (IOException e) {
      LOG.error("Failed to preload audio for project! {}\n{}", e, StringUtils.formatStackTrace(e));
      updateState(ProjectState.Failed);
      project.set(null);
      return false;

    } catch (Exception e) {
      LOG.error("Failed to clone project! {}\n{}", e, StringUtils.formatStackTrace(e));
      updateState(ProjectState.Failed);
      project.set(null);
      return false;
    }
  }

  /**
   Create the project folder on disk

   @param parentPathPrefix parent folder of the project folder
   @param projectName      name of the project folder
   @throws IOException if the project folder could not be created
   */
  private void createProjectFolder(String parentPathPrefix, String projectName) throws IOException {
    projectPathPrefix.set(parentPathPrefix + projectName + File.separator);
    this.projectName.set(projectName);

    LOG.info("Will create project folder at {}", projectPathPrefix.get());
    updateState(ProjectState.CreatingFolder);
    Files.createDirectories(Path.of(projectPathPrefix.get()));
    updateState(ProjectState.CreatedFolder);
    LOG.info("Did create project folder at {}", projectPathPrefix.get());
  }

  /**
   Save the project content to disk

   @throws IOException if the project content could not be saved
   */
  private void saveProjectContent() throws IOException {
    updateState(ProjectState.Saving);
    LOG.info("Will save project \"{}\" to {}", projectName.get(), getPathToProjectFile());
    var json = jsonProvider.getMapper().writeValueAsString(content);
    var jsonPath = getPathToProjectFile();
    Files.writeString(Path.of(jsonPath), json);
    LOG.info("Did write {} bytes of content to {}", json.length(), jsonPath);
    updateState(ProjectState.Ready);
  }

  /**
   Get the file size on disk if it exists

   @return optional true if this dub audio cache item exists (as audio waveform data) on disk
   */
  private Optional<Integer> getFileSizeIfExistsOnDisk(String absolutePath) {
    try {
      return Optional.of((int) Files.size(Path.of(absolutePath)));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  /**
   Update the state and send the updated state to the state callback

   @param state new value
   */
  private void updateState(ProjectState state) {
    this.state.set(state);
    if (Objects.nonNull(onStateChange))
      onStateChange.accept(state);
  }

  /**
   Update the progress and send the updated progress to the progress callback

   @param progress new value
   */
  private void updateProgress(double progress) {
    if (Objects.nonNull(onProgress))
      this.onProgress.accept(progress);
  }

  /**
   Update the progress label and send the updated label to the progress label callback

   @param label new value
   */
  private void updateProgressLabel(String label) {
    if (Objects.nonNull(onProgressLabel))
      this.onProgressLabel.accept(label);
  }
}
