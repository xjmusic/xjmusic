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
import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.InstrumentAudio;
import io.xj.hub.pojos.InstrumentMeme;
import io.xj.hub.pojos.Library;
import io.xj.hub.pojos.Program;
import io.xj.hub.pojos.ProgramMeme;
import io.xj.hub.pojos.ProgramSequence;
import io.xj.hub.pojos.ProgramSequenceBinding;
import io.xj.hub.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.pojos.ProgramSequenceChord;
import io.xj.hub.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.pojos.ProgramSequencePattern;
import io.xj.hub.pojos.ProgramSequencePatternEvent;
import io.xj.hub.pojos.ProgramVoice;
import io.xj.hub.pojos.ProgramVoiceTrack;
import io.xj.hub.pojos.Project;
import io.xj.hub.pojos.Template;
import io.xj.hub.pojos.TemplateBinding;
import io.xj.hub.util.LocalFileUtils;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.hub_client.HubClientException;
import io.xj.nexus.hub_client.HubClientFactory;
import io.xj.nexus.mixer.FFmpegUtils;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

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
  private final AtomicReference<String> demoBaseUrl = new AtomicReference<>("https://audio.xj.io/");
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
    this.content.set(new HubContent());
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
  public String getDemoBaseUrl() {
    return this.demoBaseUrl.get();
  }

  @Override
  public boolean createProjectFromDemoTemplate(String baseUrl, String templateKey, String parentPathPrefix, String projectName, String platformVersion) {
    this.demoBaseUrl.set(baseUrl);
    LOG.info("Cloning from demo template \"{}\" in parent folder {}", templateKey, parentPathPrefix);
    CloseableHttpClient httpClient = httpClientProvider.getClient();
    try {
      createProjectFolder(parentPathPrefix, projectName);

      LOG.info("Will load content");
      updateProgress(0.0);
      updateState(ProjectState.LoadingContent);
      content.set(hubClientFactory.loadApiV1(httpClient, this.demoBaseUrl.get(), templateKey));
      content.get().getProject().setPlatformVersion(platformVersion);
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
      Collection<UUID> audioFileNotFoundIds = new HashSet<>();

      for (Instrument instrument : instruments) {
        for (InstrumentAudio audio : audios.stream()
          .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
          .sorted(Comparator.comparing(InstrumentAudio::getName))
          .toList()) {
          if (!Objects.equals(state.get(), ProjectState.LoadingAudio)) {
            // Workstation canceling preloading should cease resampling audio files https://github.com/xjmusic/workstation/issues/278
            project.set(null);
            return false;
          }
          if (!StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
            LOG.debug("Will download audio for instrument \"{}\" with waveform key \"{}\"", instrument.getName(), audio.getWaveformKey());
            // Fetch via HTTP if original does not exist
            var originalCachePath = getPathToInstrumentAudio(instrument, audio.getWaveformKey(), null);

            var remoteUrl = String.format("%s%s", this.demoBaseUrl, audio.getWaveformKey());
            var remoteFileSize = hubClientFactory.getRemoteFileSize(httpClient, remoteUrl);
            if (remoteFileSize == FILE_SIZE_NOT_FOUND) {
              LOG.warn("File not found for audio \"{}\" of instrument \"{}\" in library \"{}\" at {}", instrument.getName(), audio.getName(), content.get().getLibrary(instrument.getLibraryId()).map(Library::getName).orElse("Unknown"), remoteUrl);
              loadedAudios++;
              audioFileNotFoundIds.add(audio.getId());
              continue;
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
      if (!audioFileNotFoundIds.isEmpty()) {
        LOG.warn("File not found for {} audios; will remove these audio records from local project", audioFileNotFoundIds.size());
        for (UUID audioId : audioFileNotFoundIds) {
          content.get().delete(InstrumentAudio.class, audioId);
        }
      }
      LOG.info("Downloaded {} audios from {} instruments", loadedAudios, instruments.size());
      updateProgressLabel(String.format("Downloaded %d audios for %d instruments", loadedAudios, loadedInstruments));
      updateState(ProjectState.LoadedAudio);

      saveProjectContent(platformVersion, null);
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
      LOG.error("Failed to duplicate project! {}\n{}", e, StringUtils.formatStackTrace(e));
      updateState(ProjectState.Failed);
      project.set(null);
      return false;
    }
  }

  @Override
  public boolean exportTemplate(
    Template template,
    String parentPathPrefix,
    String exportName,
    Boolean conversion,
    @Nullable Integer conversionFrameRate,
    @Nullable Integer conversionSampleBits,
    @Nullable Integer conversionChannels
  ) {
    LOG.info("Exporting demo template \"{}\" to \"{}\" in parent folder {}", template.getName(), exportName, parentPathPrefix);
    try {
      var exportFolderPrefix = parentPathPrefix + exportName + File.separator;
      LOG.info("Will create export folder at {}", exportFolderPrefix);
      Files.createDirectories(Path.of(exportFolderPrefix));
      LOG.info("Did create export folder at {}", exportFolderPrefix);

      LOG.info("Will export template");
      updateProgress(0.0);
      int exportedAudios = 0;
      int exportedInstruments = 0;
      Collection<UUID> audioFileNotFoundIds = new HashSet<>();
      updateState(ProjectState.ExportingTemplate);

      // Get subset of content just for this template
      var templateContent = entityFactory.forTemplate(getContent(), template);
      cleanupOrphans(templateContent);

      // Export all audio files
      var instruments = templateContent.getInstruments();
      var audios = templateContent.getInstrumentAudios();
      for (Instrument instrument : instruments) {
        var library = templateContent.getLibrary(instrument.getLibraryId()).orElseThrow(() -> new NexusException("Library not found"));
        for (InstrumentAudio audio : audios.stream()
          .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
          .sorted(Comparator.comparing(InstrumentAudio::getName))
          .toList()) {
          if (!Objects.equals(state.get(), ProjectState.ExportingTemplate)) {
            // Workstation canceling preloading should cease resampling audio files https://github.com/xjmusic/workstation/issues/278
            project.set(null);
            return false;
          }

          // Export audio file
          // If source audio file not found, add to audioFileNotFoundIds
          var sourcePath = getPathToInstrumentAudio(instrument, audio.getWaveformKey(), null);
          var sourceSize = getFileSizeIfExistsOnDisk(sourcePath);
          if (sourceSize.isPresent()) {
            var extension = ProjectPathUtils.getExtension(File.separator + audio.getWaveformKey());
            var waveformKey = LocalFileUtils.computeWaveformKey(project.get().getName(), library.getName(), instrument.getName(), audio, extension);
            audio.setWaveformKey(waveformKey);
            templateContent.put(audio);
            var destinationPath = exportFolderPrefix + waveformKey;

            try {
              Path destination = Paths.get(destinationPath);
              Files.deleteIfExists(destination);
              if (conversion) {
                FFmpegUtils.resampleAudio(
                  sourcePath,
                  destinationPath,
                  Objects.requireNonNull(conversionFrameRate),
                  Objects.requireNonNull(conversionSampleBits),
                  Objects.requireNonNull(conversionChannels)
                );
              } else {
                var existingDestinationSize = getFileSizeIfExistsOnDisk(destinationPath);
                Path source = Paths.get(sourcePath);
                if (existingDestinationSize.isPresent()) {
                  if (!existingDestinationSize.get().equals(sourceSize.get())) {
                    Files.deleteIfExists(destination);
                    Files.copy(source, destination);
                  }
                } else {
                  Files.copy(source, destination);
                }
              }
            } catch (IOException e) {
              System.err.println("Failed to copy file: " + e.getMessage());
            }

          } else {
            audioFileNotFoundIds.add(audio.getId());
          }

          updateProgress((float) exportedAudios / audios.size());
          updateProgressLabel(String.format("Exported %d/%d audios for %d/%d instruments", exportedAudios, audios.size(), exportedInstruments, instruments.size()));
          exportedAudios++;
        }
      }

      if (!audioFileNotFoundIds.isEmpty()) {
        LOG.warn("File not found for {} audios; will remove these audio records from exported template", audioFileNotFoundIds.size());
        for (UUID audioId : audioFileNotFoundIds) {
          templateContent.delete(InstrumentAudio.class, audioId);
        }
      }

      var jsonPath = exportFolderPrefix + exportName + ".json";
      LOG.info("Will save template content \"{}\" to {}", exportName, jsonPath);
      var json = jsonProvider.getMapper().writeValueAsString(templateContent);
      Files.writeString(Path.of(jsonPath), json);
      LOG.info("Did write {} bytes of content to {}", json.length(), jsonPath);

      updateProgress(1.0);
      updateProgressLabel(String.format("Exported %d audios for %d instruments", exportedAudios, exportedInstruments));
      updateState(ProjectState.Ready);
      return true;

    } catch (Exception e) {
      LOG.error("Failed to export project! {}\n{}", e, StringUtils.formatStackTrace(e));
      updateState(ProjectState.Failed);
      project.set(null);
      return false;
    }
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

      // Path to project file and all .json files in project folder
      Collection<Path> contentPaths = new HashSet<>();
      contentPaths.add(Path.of(projectFilePath));
      contentPaths.addAll(LocalFileUtils.findJsonFiles(projectPathPrefix.get()));

      // HubContent deserialized from all content json files
      Collection<HubContent> contents = new HashSet<>();
      for (Path contentPath : contentPaths) {
        contents.add(jsonProvider.getMapper().readValue(Files.readString(contentPath), HubContent.class));
      }

      // Combine all found content
      content.set(HubContent.combine(contents));
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
  public boolean createProject(String parentPathPrefix, String projectName, String platformVersion) {
    LOG.info("Create new project \"{}\" in parent folder {}", projectName, parentPathPrefix);

    try {
      createProjectFolder(parentPathPrefix, projectName);

      // Create the new project
      project.set(new Project());
      project.get().setId(UUID.randomUUID());
      project.get().setName(projectName);
      project.get().setPlatformVersion(platformVersion);

      // Create the new content
      content.set(new HubContent());
      content.get().setProjects(List.of(project.get()));

      saveProjectContent(platformVersion, null);
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
  public void saveProject(String platformVersion) {
    try {
      saveProjectContent(platformVersion, null);

    } catch (IOException e) {
      LOG.error("Failed to save project! {}\n{}", e, StringUtils.formatStackTrace(e));
      updateState(ProjectState.Ready);
    }
  }

  @Override
  public void saveAsProject(String parentPathPrefix, String projectName, String platformVersion) {
    try {
      LOG.info("Will save project as \"{}\" in parent folder {}", projectName, parentPathPrefix);

      var project = this.project.get();
      project.setName(projectName);
      this.project.set(project);
      this.content.get().put(project);

      var legacyProjectPathPrefix = projectPathPrefix.get();
      createProjectFolder(LocalFileUtils.addTrailingSlash(parentPathPrefix), projectName);
      saveProjectContent(platformVersion, legacyProjectPathPrefix);

    } catch (IOException e) {
      LOG.error("Failed to save as new project! {}\n{}", e, StringUtils.formatStackTrace(e));
      updateState(ProjectState.Ready);
    }
  }

  @Override
  public void cancelOperation() {
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
  public HubContent getContent(Template template) {
    return entityFactory.forTemplate(content.get(), template);
  }

  @Override
  public String getPathPrefixToTemplate(Template template) {
    return getPathPrefixToTemplates() + StringUtils.toAlphanumericHyphenated(template.getName()) + File.separator;
  }

  @Override
  public String getPathPrefixToLibrary(Library library, @Nullable String overrideProjectPathPrefix) {
    return getPathPrefixToLibraries(overrideProjectPathPrefix) + StringUtils.toAlphanumericHyphenated(library.getName()) + File.separator;
  }

  @Override
  public String getPathPrefixToProgram(Program program) {
    var library = content.get().getLibrary(program.getLibraryId()).orElseThrow(() -> new RuntimeException("Library not found"));
    return getPathPrefixToLibrary(library, null) + StringUtils.toAlphanumericHyphenated(program.getName()) + File.separator;
  }

  @Override
  public String getPathPrefixToInstrument(Instrument instrument, @Nullable String overrideProjectPathPrefix) {
    var library = content.get().getLibrary(instrument.getLibraryId()).orElseThrow(() -> new RuntimeException("Library not found"));
    return getPathPrefixToLibrary(library, overrideProjectPathPrefix) + StringUtils.toAlphanumericHyphenated(instrument.getName()) + File.separator;
  }

  @Override
  public String getPathToInstrumentAudio(InstrumentAudio audio, @Nullable String overrideProjectPathPrefix) {
    var instrument = content.get().getInstrument(audio.getInstrumentId()).orElseThrow(() -> new RuntimeException("Instrument not found"));
    return getPathToInstrumentAudio(instrument, audio.getWaveformKey(), overrideProjectPathPrefix);
  }

  @Override
  public String getPathToInstrumentAudio(Instrument instrument, String waveformKey, @Nullable String overrideProjectPathPrefix) {
    return getPathPrefixToInstrument(instrument, overrideProjectPathPrefix) + waveformKey;
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
  public Library createLibrary(String name) {
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
  public ProgramMeme createProgramMeme(UUID programId) {
    var meme = new ProgramMeme();
    meme.setId(UUID.randomUUID());
    meme.setName(DEFAULT_MEME_NAME);
    meme.setProgramId(programId);
    content.get().put(meme);
    return meme;
  }

  @Override
  public ProgramSequenceBindingMeme createProgramSequenceBindingMeme(UUID programSequenceBindingId) {
    var meme = new ProgramSequenceBindingMeme();
    meme.setId(UUID.randomUUID());
    meme.setName(DEFAULT_MEME_NAME);
    meme.setProgramSequenceBindingId(programSequenceBindingId);
    content.get().put(meme);
    return meme;
  }

  @Override
  public InstrumentMeme createInstrumentMeme(UUID instrumentId) {
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
    var instrumentPath = getPathPrefixToInstrument(instrument, null);
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
    audio.setWaveformKey(LocalFileUtils.computeWaveformKey(project.getName(), library.getName(), instrument.getName(), audio, matcher.group(3)));

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
  public Template duplicateTemplate(UUID fromId, String name) throws Exception {
    var source = content.get().getTemplate(fromId).orElseThrow(() -> new NexusException("Template not found"));

    // New Create a template, increment a numerical suffix to make each sequence unique, e.g. "New Template 2" then "New Template 3"
    var existingTemplates = content.get().getTemplates();
    var existingNames = existingTemplates.stream().map(Template::getName).collect(Collectors.toSet());
    var actualName = FormatUtils.iterateNumericalSuffixFromExisting(existingNames, name);

    // Duplicate the Template
    var duplicate = new Template();
    duplicate.setId(UUID.randomUUID());
    duplicate.setName(actualName);
    entityFactory.setAllEmptyAttributes(source, duplicate);
    content.get().put(duplicate);

    // Duplicate the Template's Bindings
    var duplicateBindings = entityFactory.duplicateAll(content.get().getBindingsOfTemplate(fromId), Set.of(duplicate));
    content.get().putAll(duplicateBindings.values());

    return duplicate;
  }

  @Override
  public Library duplicateLibrary(UUID fromId, String name) throws Exception {
    var source = content.get().getLibrary(fromId).orElseThrow(() -> new NexusException("Library not found"));

    // New Create a library, increment a numerical suffix to make each sequence unique, e.g. "New Library 2" then "New Library 3"
    var existingLibraries = content.get().getLibraries();
    var existingNames = existingLibraries.stream().map(Library::getName).collect(Collectors.toSet());
    var actualName = FormatUtils.iterateNumericalSuffixFromExisting(existingNames, name);

    // Duplicate the Library and put it in the store
    var library = entityFactory.duplicate(source);
    library.setName(actualName);
    content.get().put(library);

    // Duplicate the Library's Programs
    for (Program program : content.get().getProgramsOfLibrary(fromId)) {
      duplicateProgram(program.getId(), library.getId(), String.format("Copy of %s", program.getName()));
    }

    // Duplicate the Library's Instruments
    for (Instrument instrument : content.get().getInstrumentsOfLibrary(fromId)) {
      duplicateInstrument(instrument.getId(), library.getId(), String.format("Copy of %s", instrument.getName()));
    }

    // Return the library
    return library;
  }

  @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
  @Override
  public Program duplicateProgram(UUID fromId, UUID libraryId, String name) throws Exception {
    var source = content.get().getProgram(fromId).orElseThrow(() -> new NexusException("Program not found"));

    // New Create a program, increment a numerical suffix to make each sequence unique, e.g. "New Program 2" then "New Program 3"
    var existingProgramsOfLibrary = content.get().getProgramsOfLibrary(source.getLibraryId());
    var existingNames = existingProgramsOfLibrary.stream().map(Program::getName).collect(Collectors.toSet());
    var actualName = FormatUtils.iterateNumericalSuffixFromExisting(existingNames, name);

    // Duplicate Program
    var duplicateProgram = entityFactory.duplicate(source);
    duplicateProgram.setLibraryId(libraryId);
    duplicateProgram.setName(actualName);

    // Prepare all maps of duplicated sub-entities to avoid putting more than once to store
    Map<UUID, ProgramMeme> duplicatedProgramMemes = new HashMap<>();
    Map<UUID, ProgramSequence> duplicatedProgramSequences = new HashMap<>();
    Map<UUID, ProgramSequenceBinding> duplicatedProgramSequenceBindings = new HashMap<>();
    Map<UUID, ProgramSequenceBindingMeme> duplicatedProgramSequenceBindingMemes = new HashMap<>();
    Map<UUID, ProgramSequenceChord> duplicatedProgramSequenceChords = new HashMap<>();
    Map<UUID, ProgramSequenceChordVoicing> duplicatedProgramSequenceChordVoicings = new HashMap<>();
    Map<UUID, ProgramSequencePattern> duplicatedProgramSequencePatterns = new HashMap<>();
    Map<UUID, ProgramSequencePatternEvent> duplicatedProgramSequencePatternEvents = new HashMap<>();
    Map<UUID, ProgramVoice> duplicatedProgramVoices = new HashMap<>();
    Map<UUID, ProgramVoiceTrack> duplicatedProgramVoiceTracks = new HashMap<>();

    // Duplicate the Program's Sequences
    var sequences = content.get().getSequencesOfProgram(fromId);
    duplicatedProgramSequences.putAll(entityFactory.duplicateAll(sequences, Set.of(duplicateProgram)));

    // Duplicate the Program's Memes
    duplicatedProgramMemes.putAll(entityFactory.duplicateAll(content.get().getMemesOfProgram(fromId), Set.of(duplicateProgram)));

    // Duplicate the Program's Voices
    var voices = content.get().getVoicesOfProgram(fromId);
    duplicatedProgramVoices.putAll(entityFactory.duplicateAll(voices, Set.of(duplicateProgram)));

    // Iterate through the duplicated voices and duplicate all the Program's Voice's Tracks
    Collection<ProgramVoiceTrack> tracks = content.get().getTracksOfProgram(fromId);
    for (ProgramVoice voice : voices) {
      var duplicatedVoice = duplicatedProgramVoices.get(voice.getId());
      duplicatedProgramVoiceTracks.putAll(entityFactory.duplicateAll(content.get().getTracksOfVoice(voice.getId()), Set.of(duplicateProgram, duplicatedVoice)));
    }

    // Iterate through the duplicated sequences
    for (ProgramSequence sequence : sequences) {
      var duplicatedSequence = duplicatedProgramSequences.get(sequence.getId());

      // Duplicate the Program's Sequence's Patterns
      var patterns = content.get().getPatternsOfSequence(sequence.getId());
      var duplicatedPatterns = entityFactory.duplicateAll(patterns, Set.of(duplicateProgram, duplicatedSequence));
      duplicatedProgramSequencePatterns.putAll(duplicatedPatterns);

      // Iterate through the duplicated patterns and tracks and duplicate all the Program's Sequences' Patterns' Events
      for (ProgramSequencePattern pattern : patterns) {
        var duplicatedPattern = duplicatedPatterns.get(pattern.getId());
        for (ProgramVoiceTrack track : tracks) {
          var duplicatedTrack = duplicatedProgramVoiceTracks.get(track.getId());
          duplicatedProgramSequencePatternEvents.putAll(entityFactory.duplicateAll(content.get().getEventsOfPatternAndTrack(pattern.getId(), track.getId()), Set.of(duplicateProgram, duplicatedPattern, duplicatedTrack)));
        }
      }

      // Duplicate the Program's Sequence's Bindings
      var bindings = content.get().getBindingsOfSequence(sequence.getId());
      duplicatedProgramSequenceBindings.putAll(entityFactory.duplicateAll(bindings, Set.of(duplicateProgram, duplicatedSequence)));

      // Iterate through the duplicated sequence's bindings
      for (ProgramSequenceBinding binding : bindings) {
        var duplicatedBinding = duplicatedProgramSequenceBindings.get(binding.getId());

        // Duplicate the Program's Sequence's Bindings' Memes
        duplicatedProgramSequenceBindingMemes.putAll(entityFactory.duplicateAll(content.get().getMemesOfSequenceBinding(binding.getId()), Set.of(duplicateProgram, duplicatedBinding)));
      }

      // Duplicate the Program's Sequence's Chords
      var chords = content.get().getChordsOfSequence(sequence.getId());
      Map<UUID, ProgramSequenceChord> duplicatedChords = entityFactory.duplicateAll(chords, Set.of(duplicateProgram, duplicatedSequence));
      duplicatedProgramSequenceChords.putAll(duplicatedChords);

      // Iterate through the duplicated chords and duplicate the Program's Sequences' Chords' Voicings
      for (ProgramSequenceChord chord : chords) {
        var duplicatedChord = duplicatedChords.get(chord.getId());
        for (ProgramVoice voice : voices) {
          var duplicatedVoice = duplicatedProgramVoices.get(voice.getId());
          duplicatedProgramSequenceChordVoicings.putAll(entityFactory.duplicateAll(content.get().getVoicingsOfChordAndVoice(chord.getId(), voice.getId()), Set.of(duplicateProgram, duplicatedChord, duplicatedVoice)));
        }
      }
    }

    // Put everything in the store and return the program
    content.get().put(duplicateProgram);
    content.get().putAll(duplicatedProgramMemes.values());
    content.get().putAll(duplicatedProgramSequences.values());
    content.get().putAll(duplicatedProgramSequenceBindings.values());
    content.get().putAll(duplicatedProgramSequenceBindingMemes.values());
    content.get().putAll(duplicatedProgramSequenceChords.values());
    content.get().putAll(duplicatedProgramSequenceChordVoicings.values());
    content.get().putAll(duplicatedProgramSequencePatterns.values());
    content.get().putAll(duplicatedProgramSequencePatternEvents.values());
    content.get().putAll(duplicatedProgramVoices.values());
    content.get().putAll(duplicatedProgramVoiceTracks.values());

    return duplicateProgram;
  }

  @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
  @Override
  public ProgramSequence duplicateProgramSequence(UUID fromId) throws Exception {
    var source = content.get().getProgramSequence(fromId).orElseThrow(() -> new NexusException("Program Sequence not found"));

    // Duplicate Program
    var duplicatedSequence = entityFactory.duplicate(source);
    duplicatedSequence.setName("Duplicate of " + source.getName());

    // Prepare all maps of duplicated sub-entities to avoid putting more than once to store
    Map<UUID, ProgramSequenceBinding> duplicatedProgramSequenceBindings = new HashMap<>();
    Map<UUID, ProgramSequenceBindingMeme> duplicatedProgramSequenceBindingMemes = new HashMap<>();
    Map<UUID, ProgramSequenceChord> duplicatedProgramSequenceChords = new HashMap<>();
    Map<UUID, ProgramSequenceChordVoicing> duplicatedProgramSequenceChordVoicings = new HashMap<>();
    Map<UUID, ProgramSequencePattern> duplicatedProgramSequencePatterns = new HashMap<>();
    Map<UUID, ProgramSequencePatternEvent> duplicatedProgramSequencePatternEvents = new HashMap<>();

    // Duplicate the Program's Sequence's Patterns
    var patterns = content.get().getPatternsOfSequence(fromId);
    duplicatedProgramSequencePatterns.putAll(entityFactory.duplicateAll(patterns, Set.of(duplicatedSequence)));

    // Iterate through the duplicated patterns and tracks and duplicate all the Program's Sequences' Patterns' Events
    for (ProgramSequencePattern pattern : patterns) {
      var duplicatedPattern = duplicatedProgramSequencePatterns.get(pattern.getId());
      for (ProgramVoiceTrack track : content.get().getTracksOfProgram(source.getProgramId())) {
        duplicatedProgramSequencePatternEvents.putAll(entityFactory.duplicateAll(content.get().getEventsOfPatternAndTrack(pattern.getId(), track.getId()), Set.of(duplicatedPattern)));
      }
    }

    // Duplicate the Program's Sequence's Chords
    var chords = content.get().getChordsOfSequence(fromId);
    Map<UUID, ProgramSequenceChord> duplicatedChords = entityFactory.duplicateAll(chords, Set.of(duplicatedSequence));
    duplicatedProgramSequenceChords.putAll(duplicatedChords);

    // Iterate through the duplicated chords and duplicate the Program's Sequences' Chords' Voicings
    var voices = content.get().getVoicesOfProgram(source.getProgramId());
    for (ProgramSequenceChord chord : chords) {
      var duplicatedChord = duplicatedChords.get(chord.getId());
      for (ProgramVoice voice : voices) {
        duplicatedProgramSequenceChordVoicings.putAll(entityFactory.duplicateAll(content.get().getVoicingsOfChordAndVoice(chord.getId(), voice.getId()), Set.of(duplicatedChord)));
      }
    }

    // Put everything in the store and return the program
    content.get().put(duplicatedSequence);
    content.get().putAll(duplicatedProgramSequenceBindings.values());
    content.get().putAll(duplicatedProgramSequenceBindingMemes.values());
    content.get().putAll(duplicatedProgramSequenceChords.values());
    content.get().putAll(duplicatedProgramSequenceChordVoicings.values());
    content.get().putAll(duplicatedProgramSequencePatterns.values());
    content.get().putAll(duplicatedProgramSequencePatternEvents.values());

    return duplicatedSequence;
  }

  @Override
  public ProgramSequencePattern duplicateProgramSequencePattern(UUID fromId) throws Exception {
    var source = content.get().getProgramSequencePattern(fromId).orElseThrow(() -> new NexusException("Program Sequence Pattern not found"));

    // Duplicate Program
    var duplicatedPattern = entityFactory.duplicate(source);
    duplicatedPattern.setName("Duplicate of " + source.getName());

    // Iterate through the tracks and duplicate all the Program's Sequences' Patterns' Events
    Map<UUID, ProgramSequencePatternEvent> duplicatedProgramSequencePatternEvents = new HashMap<>(entityFactory.duplicateAll(content.get().getEventsOfPattern(source.getId()), Set.of(duplicatedPattern)));

    // Put everything in the store and return the program
    content.get().put(duplicatedPattern);
    content.get().putAll(duplicatedProgramSequencePatternEvents.values());

    return duplicatedPattern;
  }

  @Override
  public Instrument duplicateInstrument(UUID fromId, UUID libraryId, String name) throws Exception {
    var source = content.get().getInstrument(fromId).orElseThrow(() -> new NexusException("Instrument not found"));

    // New Create a instrument, increment a numerical suffix to make each sequence unique, e.g. "New Instrument 2" then "New Instrument 3"
    var existingInstrumentsOfLibrary = content.get().getInstrumentsOfLibrary(source.getLibraryId());
    var existingNames = existingInstrumentsOfLibrary.stream().map(Instrument::getName).collect(Collectors.toSet());
    var actualName = FormatUtils.iterateNumericalSuffixFromExisting(existingNames, name);

    // Duplicate the Instrument
    var instrument = entityFactory.duplicate(source);
    instrument.setLibraryId(libraryId);
    instrument.setName(actualName);

    // Duplicate the Instrument's Audios
    var duplicatedAudios = entityFactory.duplicateAll(content.get().getAudiosOfInstrument(fromId), Set.of(instrument));

    // Duplicate the Instrument's Memes
    var duplicatedMemes = entityFactory.duplicateAll(content.get().getMemesOfInstrument(fromId), Set.of(instrument));

    // Put everything in the store
    content.get().put(instrument);
    content.get().putAll(duplicatedMemes.values());
    content.get().putAll(duplicatedAudios.values());

    // Copy all the instrument's audio to the new folder
    for (InstrumentAudio audio : duplicatedAudios.values()) {
      var fromPath = getPathToInstrumentAudio(source, audio.getWaveformKey(), null);
      var toPath = getPathToInstrumentAudio(instrument, audio.getWaveformKey(), null);
      if (new File(fromPath).exists())
        FileUtils.copyFile(new File(fromPath), new File(toPath));
    }

    // return the instrument
    return instrument;
  }

  /**
   Cleanup orphans in content
   */
  private void cleanupOrphans(HubContent content) {
    // Cleanup orphans
    int orphans = 0;
    orphans += deleteAllIf(Instrument.class, (Instrument instrument) -> content.getLibrary(instrument.getLibraryId()).isEmpty());
    orphans += deleteAllIf(InstrumentMeme.class, (InstrumentMeme meme) -> content.getInstrument(meme.getInstrumentId()).isEmpty());
    orphans += deleteAllIf(InstrumentAudio.class, (InstrumentAudio audio) -> content.getInstrument(audio.getInstrumentId()).isEmpty());
    orphans += deleteAllIf(Program.class, (Program program) -> content.getLibrary(program.getLibraryId()).isEmpty());
    orphans += deleteAllIf(ProgramMeme.class, (ProgramMeme meme) -> content.getProgram(meme.getProgramId()).isEmpty());
    orphans += deleteAllIf(ProgramSequence.class, (ProgramSequence sequence) -> content.getProgram(sequence.getProgramId()).isEmpty());
    orphans += deleteAllIf(ProgramSequenceBinding.class, (ProgramSequenceBinding binding) -> content.getProgramSequence(binding.getProgramSequenceId()).isEmpty());
    orphans += deleteAllIf(ProgramSequenceBinding.class, (ProgramSequenceBinding binding) -> content.getProgram(binding.getProgramId()).isEmpty());
    orphans += deleteAllIf(ProgramSequenceBindingMeme.class, (ProgramSequenceBindingMeme meme) -> content.getProgramSequenceBinding(meme.getProgramSequenceBindingId()).isEmpty());
    orphans += deleteAllIf(ProgramSequenceBindingMeme.class, (ProgramSequenceBindingMeme meme) -> content.getProgram(meme.getProgramId()).isEmpty());
    orphans += deleteAllIf(ProgramVoice.class, (ProgramVoice voice) -> content.getProgram(voice.getProgramId()).isEmpty());
    orphans += deleteAllIf(ProgramVoiceTrack.class, (ProgramVoiceTrack track) -> content.getProgramVoice(track.getProgramVoiceId()).isEmpty());
    orphans += deleteAllIf(ProgramVoiceTrack.class, (ProgramVoiceTrack track) -> content.getProgram(track.getProgramId()).isEmpty());
    orphans += deleteAllIf(ProgramSequencePattern.class, (ProgramSequencePattern pattern) -> content.getProgramSequence(pattern.getProgramSequenceId()).isEmpty());
    orphans += deleteAllIf(ProgramSequencePattern.class, (ProgramSequencePattern pattern) -> content.getProgramVoice(pattern.getProgramVoiceId()).isEmpty());
    orphans += deleteAllIf(ProgramSequencePattern.class, (ProgramSequencePattern pattern) -> content.getProgram(pattern.getProgramId()).isEmpty());
    orphans += deleteAllIf(ProgramSequencePatternEvent.class, (ProgramSequencePatternEvent event) -> content.getProgramSequencePattern(event.getProgramSequencePatternId()).isEmpty());
    orphans += deleteAllIf(ProgramSequencePatternEvent.class, (ProgramSequencePatternEvent event) -> content.getProgramVoiceTrack(event.getProgramVoiceTrackId()).isEmpty());
    orphans += deleteAllIf(ProgramSequencePatternEvent.class, (ProgramSequencePatternEvent event) -> content.getProgram(event.getProgramId()).isEmpty());
    orphans += deleteAllIf(ProgramSequenceChord.class, (ProgramSequenceChord chord) -> content.getProgramSequence(chord.getProgramSequenceId()).isEmpty());
    orphans += deleteAllIf(ProgramSequenceChord.class, (ProgramSequenceChord chord) -> content.getProgram(chord.getProgramId()).isEmpty());
    orphans += deleteAllIf(ProgramSequenceChordVoicing.class, (ProgramSequenceChordVoicing voicing) -> content.getProgramSequenceChord(voicing.getProgramSequenceChordId()).isEmpty());
    orphans += deleteAllIf(ProgramSequenceChordVoicing.class, (ProgramSequenceChordVoicing voicing) -> content.getProgramVoice(voicing.getProgramVoiceId()).isEmpty());
    orphans += deleteAllIf(ProgramSequenceChordVoicing.class, (ProgramSequenceChordVoicing voicing) -> content.getProgram(voicing.getProgramId()).isEmpty());
    orphans += deleteAllIf(TemplateBinding.class, (TemplateBinding binding) -> content.getTemplate(binding.getTemplateId()).isEmpty());
    if (orphans > 0) {
      LOG.info("Did delete {} orphaned entities", orphans);
    }
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
   Import an audio waveform file into the project from somewhere else on disk

   @param audio         for which to import the waveform
   @param audioFilePath path to the audio file on disk
   @throws IOException if the audio file could not be imported
   */
  private void importAudio(InstrumentAudio audio, String audioFilePath) throws IOException {
    var targetPath = getPathToInstrumentAudio(audio, null);
    FileUtils.createParentDirectories(new File(targetPath));
    FileUtils.copyFile(new File(audioFilePath), new File(targetPath));
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

   @param platformVersion        the platform version
   @param priorProjectPathPrefix (optional) the prior project path prefix, e.g. during a Save As operation
   @throws IOException if the project content could not be saved
   */
  private void saveProjectContent(String platformVersion, @Nullable String priorProjectPathPrefix) throws IOException {
    updateState(ProjectState.Saving);
    cleanupOrphans(content.get());
    project.get().setPlatformVersion(platformVersion);
    content.get().setProject(project.get());
    LOG.info("Will save project \"{}\" with platform version {} to {}", projectName.get(), platformVersion, getPathToProjectFile());

    // Report progress based on templates, libraries, programs, instruments, and audios
    int totalItems = content.get().getTemplates().size() + content.get().getLibraries().size() + content.get().getPrograms().size() + content.get().getInstruments().size() + content.get().getInstrumentAudios().size();
    int savedItems = 0;
    updateProgress(0);
    updateProgressLabel("Saving project...");

    // Walk the entire project folder and identify existing files and folders, store these paths
    // After saving the project we will delete any unknown files and folders
    Set<String> filesOnDisk = new HashSet<>();
    Set<String> foldersOnDisk = new HashSet<>();
    Set<String> filesInProject = new HashSet<>();
    Set<String> foldersInProject = new HashSet<>();
    Path prefixPath = Paths.get(getProjectPathPrefix());
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
        LOG.error("Failed to walk project folder! {}\n{}", e, StringUtils.formatStackTrace(e));
        updateState(ProjectState.Ready);
        return;
      }
    foldersInProject.add(ensureDirectoryExists(getProjectPathPrefix()));

    // .xj file is only the Project and whether it's a demo
    HubContent projectContent = new HubContent();
    projectContent.setDemo(content.get().getDemo());
    projectContent.put(content.get().getProject());
    filesInProject.add(writeContentToJsonFile(projectContent, getPathToProjectFile()));

    // Iterate through all templates, make a folder, and write the template.json file
    foldersInProject.add(ensureDirectoryExists(getPathPrefixToTemplates()));
    for (Template template : content.get().getTemplates()) {
      var templatePathPrefix = getPathPrefixToTemplate(template);
      foldersInProject.add(ensureDirectoryExists(templatePathPrefix));
      filesInProject.add(writeContentToJsonFile(content.get().subsetForTemplateId(template.getId()), templatePathPrefix + StringUtils.toAlphanumericHyphenated(template.getName()) + ".json"));
      updateProgress((double) ++savedItems / totalItems);
    }

    // Iterate through all libraries, make a folder, and write the library.json file
    foldersInProject.add(ensureDirectoryExists(getPathPrefixToLibraries(null)));
    for (Library library : content.get().getLibraries()) {
      var libraryPathPrefix = getPathPrefixToLibrary(library, null);
      foldersInProject.add(ensureDirectoryExists(libraryPathPrefix));
      filesInProject.add(writeContentToJsonFile(new HubContent(Set.of(library)), libraryPathPrefix + StringUtils.toAlphanumericHyphenated(library.getName()) + ".json"));
      updateProgress((double) ++savedItems / totalItems);

      // Iterate through all programs, make a folder, and write the program.json file
      for (Program program : content.get().getProgramsOfLibrary(library)) {
        var programPathPrefix = getPathPrefixToProgram(program);
        foldersInProject.add(ensureDirectoryExists(programPathPrefix));
        filesInProject.add(writeContentToJsonFile(content.get().subsetForProgramId(program.getId()), programPathPrefix + StringUtils.toAlphanumericHyphenated(program.getName()) + ".json"));
        updateProgress((double) ++savedItems / totalItems);
      }

      // Iterate through all instruments, make a folder, and write the instrument.json file
      for (Instrument instrument : content.get().getInstrumentsOfLibrary(library)) {
        var instrumentPathPrefix = libraryPathPrefix + StringUtils.toAlphanumericHyphenated(instrument.getName()) + File.separator;
        foldersInProject.add(ensureDirectoryExists(instrumentPathPrefix));
        filesInProject.add(writeContentToJsonFile(content.get().subsetForInstrumentId(instrument.getId()), instrumentPathPrefix + StringUtils.toAlphanumericHyphenated(instrument.getName()) + ".json"));
        updateProgress((double) ++savedItems / totalItems);

        // Iterate through all audios, determine expected path, and if the audio is not in that path, copy it from where it would be expected in the legacy project format, or from the legacy project path prefix
        for (InstrumentAudio audio : content.get().getAudiosOfInstrument(instrument.getId())) {
          var project = content.get().getProject();
          var extension = ProjectPathUtils.getExtension(File.separator + audio.getWaveformKey());
          var toWaveformKey = LocalFileUtils.computeWaveformKey(project.getName(), library.getName(), instrument.getName(), audio, extension);
          var idealAudioPath = getPathToInstrumentAudio(instrument, toWaveformKey, null);
          // Copy all audio files to the new project folder from the prior folder (during a Save As operation) or the legacy folder (during a migration operation)
          if (!Files.exists(Path.of(idealAudioPath))) {
            var currentPath = getPathToInstrumentAudio(audio, null); // the current (not necessarily ideal) path
            var priorAudioPath = getPathToInstrumentAudio(audio, priorProjectPathPrefix); // the prior path (during save-as)
            var legacyAudioPath = getLegacyPathToInstrumentAudio(audio, null); // the legacy path (during migration)
            var priorLegacyAudioPath = getLegacyPathToInstrumentAudio(audio, priorProjectPathPrefix); // the prior legacy path (during save-as migration)
            if (Objects.nonNull(priorAudioPath) && Files.exists(Path.of(priorAudioPath))) {
              FileUtils.copyFile(new File(priorAudioPath), new File(idealAudioPath));
            } else if (Objects.nonNull(priorAudioPath) && Files.exists(Path.of(priorLegacyAudioPath))) {
              FileUtils.copyFile(new File(priorLegacyAudioPath), new File(idealAudioPath));
            } else if (Files.exists(Path.of(legacyAudioPath))) {
              FileUtils.moveFile(new File(legacyAudioPath), new File(idealAudioPath));
            } else if (!Objects.equals(currentPath, idealAudioPath) && Files.exists(Path.of(currentPath))) {
              try {
                content.get().update(InstrumentAudio.class, audio.getId(), "waveformKey", toWaveformKey);
                FileUtils.moveFile(new File(currentPath), new File(idealAudioPath));
              } catch (Exception e) {
                LOG.error("Failed to move audio file from {} to {}! {}\n{}", currentPath, idealAudioPath, e, StringUtils.formatStackTrace(e));
              }
            } else {
              LOG.error("Could not find audio file at any of the following paths: {}, {}, {}, {}", priorAudioPath, priorLegacyAudioPath, legacyAudioPath, currentPath);
            }
          }
          filesInProject.add(idealAudioPath);
          updateProgress((double) ++savedItems / totalItems);
        }
      }
    }

    // Any unused files and folders in the project path should be deleted
    LOG.info("Saved {} folders on disk containing a total of {} files.", foldersOnDisk.size(), filesOnDisk.size());
    LOG.info("The project has {} instruments containing a total of {} audios.", foldersInProject.size(), filesInProject.size());
    foldersOnDisk.removeAll(foldersInProject);
    filesOnDisk.removeAll(filesInProject);
    LOG.info("Will delete {} folders and {} files.", foldersOnDisk.size(), filesOnDisk.size());
    for (String s : filesOnDisk) {
      try {
        Files.deleteIfExists(Paths.get(s));
      } catch (IOException e) {
        LOG.error("Failed to delete audio file \"{}\"! {}\n{}", s, e, StringUtils.formatStackTrace(e));
      }
    }
    for (String path : foldersOnDisk) {
      try {
        FileUtils.deleteDirectory(new File(path));
      } catch (IOException e) {
        LOG.error("Failed to delete instrument folder {}! {}\n{}", path, e, StringUtils.formatStackTrace(e));
      }
    }

    updateProgress(1.0);
    updateState(ProjectState.Ready);
  }

  /**
   Ensure a directory exists, creating it if it does not, and return the path

   @param dir to ensure exists
   @return the path
   @throws IOException if the directory could not be created
   */
  private String ensureDirectoryExists(String dir) throws IOException {
    Path path = Path.of(dir);
    if (!Files.exists(path)) {
      Files.createDirectory(path);
    }
    return dir;
  }

  /**
   Get the legacy path to the audio folder for an instrument -- for migrating a project from the legacy format

   @param audio                     for which to get path
   @param overrideProjectPathPrefix (optional) override the project path prefix
   @return the path to the audio
   */
  public String getLegacyPathToInstrumentAudio(InstrumentAudio audio, @Nullable String overrideProjectPathPrefix) {
    return Optional.ofNullable(overrideProjectPathPrefix).orElse(projectPathPrefix.get()) + "instrument" + File.separator + audio.getInstrumentId().toString() + File.separator + audio.getWaveformKey();
  }

  /**
   @return path prefix to the project's libraries
   */
  private String getPathPrefixToLibraries(@Nullable String overrideProjectPathPrefix) {
    return Optional.ofNullable(overrideProjectPathPrefix).orElse(projectPathPrefix.get()) + "libraries" + File.separator;
  }

  /**
   @return path prefix to the project's templates
   */
  private String getPathPrefixToTemplates() {
    return projectPathPrefix.get() + "templates" + File.separator;
  }

  /**
   Write a HubContent object to a JSON file

   @param content   to write
   @param writePath to write the content to
   @return path for chaining use of method
   @throws IOException if the content could not be written to the file
   */
  private String writeContentToJsonFile(HubContent content, String writePath) throws IOException {
    var json = jsonProvider.getMapper().writeValueAsString(content);
    Path path = Path.of(writePath);
    Files.writeString(path, json);
    LOG.info("Did write {} bytes of content to {}", json.length(), path);
    return writePath;
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
