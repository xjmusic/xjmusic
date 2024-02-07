package io.xj.nexus.project;

import io.xj.hub.HubContent;
import io.xj.hub.entity.EntityFactory;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
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
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.HubClientException;
import io.xj.nexus.hub_client.HubClientImpl;
import jakarta.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import java.util.regex.Matcher;

import static io.xj.hub.util.FileUtils.computeWaveformKey;

public class ProjectManagerImpl implements ProjectManager {
  static final Logger LOG = LoggerFactory.getLogger(ProjectManagerImpl.class);
  private final AtomicReference<ProjectState> state = new AtomicReference<>(ProjectState.Standby);
  private final AtomicReference<Project> project = new AtomicReference<>();
  private final AtomicReference<String> projectPathPrefix = new AtomicReference<>(File.separator);
  private final AtomicReference<String> projectName = new AtomicReference<>("Project");
  private final AtomicReference<String> audioBaseUrl = new AtomicReference<>("https://audio.xj.io/");
  private final AtomicReference<HubContent> content = new AtomicReference<>();
  private final JsonProvider jsonProvider;
  private final EntityFactory entityFactory;
  private final int downloadAudioRetries;
  private final HttpClientProvider httpClientProvider;

  @Nullable
  private Consumer<Double> onProgress;

  @Nullable
  private Consumer<ProjectState> onStateChange;

  /**
   Private constructor
   */
  public ProjectManagerImpl(
    HttpClientProvider httpClientProvider,
    JsonProvider jsonProvider,
    EntityFactory entityFactory,
    int downloadAudioRetries
  ) {
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.entityFactory = entityFactory;
    this.downloadAudioRetries = downloadAudioRetries;
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
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubClient hubClient = new HubClientImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory);
    return cloneProject(parentPathPrefix, () -> hubClient.loadApiV1(templateShipKey, this.audioBaseUrl.get()), projectName);
  }

  @Override
  public boolean cloneFromLabProject(HubClientAccess access, String labBaseUrl, String audioBaseUrl, String parentPathPrefix, UUID projectId, String projectName) {
    this.audioBaseUrl.set(audioBaseUrl);
    LOG.info("Cloning from lab Project[{}] in parent folder {}", projectId, parentPathPrefix);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubClient hubClient = new HubClientImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory);
    return cloneProject(parentPathPrefix, () -> hubClient.ingestApiV2(labBaseUrl, access, projectId), projectName);
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
      updateState(ProjectState.LoadingContent);
      var json = Files.readString(Path.of(projectFilePath));
      content.set(jsonProvider.getMapper().readValue(json, HubContent.class));
      project.set(content.get().getProjects().stream().findFirst().orElse(null));
      updateState(ProjectState.LoadedContent);
      LOG.info("Did load content for project \"{}\" from {}", projectName.get(), projectFilePath);
      updateState(ProjectState.Ready);
      return true;

    } catch (Exception e) {
      LOG.error("Failed to open project from local file!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
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
      LOG.error("Failed to open project from local file!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
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
      LOG.error("Failed to save project!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
      updateState(ProjectState.Ready);
    }
  }

  @Override
  public int cleanupProject() {
    updateState(ProjectState.Saving);
    var prefix = getInstrumentPathPrefix();
    LOG.info("Cleaning up project audio folder {}", prefix);
    Set<String> audioFilesOnDisk = new HashSet<>();
    Set<String> instrumentFoldersOnDisk = new HashSet<>();
    Set<String> audioFilesInProject = new HashSet<>();
    Set<String> instrumentFoldersInProject = new HashSet<>();
    try (var instrumentFolders = Files.list(Paths.get(prefix))) {
      for (var instrumentFolder : instrumentFolders.toList()) {
        var instrumentPath = prefix + instrumentFolder.getFileName().toString() + File.separator;
        instrumentFoldersOnDisk.add(instrumentPath);
        try (var audioFiles = Files.list(instrumentFolder)) {
          audioFiles.forEach(audioFile -> audioFilesOnDisk.add(instrumentPath + audioFile.getFileName()));
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to list all instrument folders and audio files on disk!\n{}", StringUtils.formatStackTrace(e), e);
      updateState(ProjectState.Ready);
      return 0;
    }
    content.get().getInstruments().forEach(instrument -> {
      var instrumentPath = getPathPrefixToInstrumentAudio(instrument.getId());
      instrumentFoldersInProject.add(instrumentPath);
      content.get().getAudiosOfInstrument(instrument.getId()).forEach(audio ->
        audioFilesInProject.add(String.format("%s%s", instrumentPath, audio.getWaveformKey())));
    });
    LOG.info("Found {} instrument folders on disk containing a total of {} audio files.", instrumentFoldersOnDisk.size(), audioFilesOnDisk.size());
    LOG.info("The project has {} instruments containing a total of {} audios.", instrumentFoldersInProject.size(), audioFilesInProject.size());
    instrumentFoldersOnDisk.removeAll(instrumentFoldersInProject);
    audioFilesOnDisk.removeAll(audioFilesInProject);
    LOG.info("Will delete {} instrument folders and {} audio files.", instrumentFoldersOnDisk.size(), audioFilesOnDisk.size());
    for (String s : audioFilesOnDisk) {
      try {
        Files.deleteIfExists(Paths.get(s));
      } catch (IOException e) {
        LOG.error("Failed to delete audio file {}\n{}", s, StringUtils.formatStackTrace(e));
        updateState(ProjectState.Ready);
        return 0;
      }
    }
    for (String path : instrumentFoldersOnDisk) {
      try {
        FileUtils.deleteDirectory(new File(path));
      } catch (IOException e) {
        LOG.error("Failed to delete instrument folder {}\n{}", path, StringUtils.formatStackTrace(e));
        updateState(ProjectState.Ready);
        return 0;
      }
    }
    updateState(ProjectState.Ready);
    return audioFilesOnDisk.size();
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
    var template = new Template();
    template.setId(UUID.randomUUID());
    template.setName(name);
    template.setIsDeleted(false);
    content.get().put(template);
    return template;
  }

  @Override
  public Library createLibrary(String name) throws Exception {
    var library = new Library();
    library.setId(UUID.randomUUID());
    library.setName(name);
    library.setIsDeleted(false);
    content.get().put(library);
    return library;
  }

  @Override
  public Program createProgram(Library library, String name) throws Exception {
    var program = new Program();
    program.setId(UUID.randomUUID());
    program.setName(name);
    program.setLibraryId(library.getId());
    program.setIsDeleted(false);
    content.get().put(program);
    return program;
  }

  @Override
  public Instrument createInstrument(Library library, String name) throws Exception {
    var instrument = new Instrument();
    instrument.setId(UUID.randomUUID());
    instrument.setName(name);
    instrument.setLibraryId(library.getId());
    instrument.setIsDeleted(false);
    var instrumentPath = getPathPrefixToInstrumentAudio(instrument.getId());
    FileUtils.createParentDirectories(new File(instrumentPath));
    content.get().put(instrument);
    return instrument;
  }

  @Override
  public InstrumentAudio createInstrumentAudio(Instrument instrument, String audioFilePath) throws Exception {
    var library = content.get().getLibrary(instrument.getLibraryId()).orElseThrow(() -> new NexusException("Library not found"));
    var project = content.get().getProject(library.getProjectId()).orElseThrow(() -> new NexusException("Project not found"));
    var existingAudioOfInstrument = content.get().getAudiosOfInstrument(instrument.getId()).stream().findFirst();

    // extract the file name and extension
    Matcher matcher = ProjectPathUtils.matchPrefixNameExtension(audioFilePath);
    if (!matcher.find()) {
      throw new RuntimeException(String.format("Failed to parse project path prefix and name from file path: %s", audioFilePath));
    }

    // Prepare the audio record
    var audio = new InstrumentAudio();
    audio.setId(UUID.randomUUID());
    audio.setName(matcher.group(2));
    audio.setTones("");
    audio.setIntensity(1.0f);
    audio.setTempo(existingAudioOfInstrument.map(InstrumentAudio::getTempo).orElse(0.0f));
    audio.setLoopBeats(1.0f);
    audio.setTransientSeconds(0.0f);
    audio.setVolume(1.0f);
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
  public void updateInstrumentAudioAndCopyWaveformFile(InstrumentAudio audio) throws Exception {
    var fromAudio = content.get().getInstrumentAudio(audio.getId())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument Audio"));
    var fromInstrument = content.get().getInstrument(fromAudio.getInstrumentId()).orElseThrow(() -> new NexusException("Instrument not found"));
    var fromPath = getPathToInstrumentAudio(fromInstrument.getId(), fromAudio.getWaveformKey());
    var matcher = ProjectPathUtils.matchPrefixNameExtension(fromPath);
    if (!matcher.find()) return;

    var toInstrument = content.get().getInstrument(audio.getInstrumentId()).orElseThrow(() -> new NexusException("Instrument not found"));
    var toLibrary = content.get().getLibrary(toInstrument.getLibraryId()).orElseThrow(() -> new NexusException("Library not found"));
    var toProject = content.get().getProject(toLibrary.getProjectId()).orElseThrow(() -> new NexusException("Project not found"));
    var toWaveformKey = computeWaveformKey(toProject.getName(), toLibrary.getName(), toInstrument.getName(), audio, matcher.group(3));
    var toPath = getPathToInstrumentAudio(toInstrument.getId(), toWaveformKey);

    if (!Objects.equals(fromPath, toPath)) {
      FileUtils.copyFile(new File(fromPath), new File(toPath));
      audio.setWaveformKey(toWaveformKey);
    }

    content.get().put(audio);
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
  public ProgramSequence cloneProgramSequence(UUID fromId, String name) throws Exception {
    var source = content.get().getProgramSequence(fromId).orElseThrow(() -> new NexusException("Program Sequence not found"));

    // Clone Program
    var clonedSequence = entityFactory.clone(source);
    clonedSequence.setName(name);

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
  public ProgramSequencePattern cloneProgramSequencePattern(UUID fromId, String name) throws Exception {
    var source = content.get().getProgramSequencePattern(fromId).orElseThrow(() -> new NexusException("Program Sequence Pattern not found"));

    // Clone Program
    var clonedPattern = entityFactory.clone(source);
    clonedPattern.setName(name);

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
      updateState(ProjectState.LoadingContent);
      content.set(fetchContent.call());
      project.set(content.get().getProjects().stream().findFirst().orElse(null));
      updateState(ProjectState.LoadedContent);
      LOG.info("Did load content");

      LOG.info("Will load {} audio for {} instruments", content.get().getInstrumentAudios().size(), content.get().getInstruments().size());
      updateProgress(0.0);
      updateState(ProjectState.LoadingAudio);
      int loaded = 0;
      var instruments = new ArrayList<>(content.get().getInstruments());
      var audios = new ArrayList<>(content.get().getInstrumentAudios());

      // Don't close the client, only close the responses from it
      CloseableHttpClient httpClient = httpClientProvider.getClient();

      for (Instrument instrument : instruments) {
        for (InstrumentAudio audio : audios.stream()
          .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
          .sorted(Comparator.comparing(InstrumentAudio::getName))
          .toList()) {
          if (!Objects.equals(state.get(), ProjectState.LoadingAudio)) {
            // Workstation canceling preloading should cease resampling audio files https://www.pivotaltracker.com/story/show/186209135
            return false;
          }
          if (!StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
            LOG.debug("Will preload audio for instrument {} with waveform key {}", instrument.getName(), audio.getWaveformKey());
            // Fetch via HTTP if original does not exist
            var originalCachePath = getPathToInstrumentAudio(
              instrument.getId(),
              audio.getWaveformKey()
            );

            var remoteUrl = String.format("%s%s", this.audioBaseUrl, audio.getWaveformKey());
            var remoteFileSize = getRemoteFileSize(httpClient, remoteUrl);
            var localFileSize = getFileSizeIfExistsOnDisk(originalCachePath);

            boolean shouldDownload = false;

            if (localFileSize.isEmpty()) {
              shouldDownload = true;
            } else if (localFileSize.get()!=remoteFileSize) {
              LOG.info("File size of {} does not match remote {} - Will download {} bytes from {}", originalCachePath, remoteFileSize, remoteFileSize, remoteUrl);
              shouldDownload = true;
            }

            if (shouldDownload) {
              if (!downloadRemoteFileWithRetry(httpClient, remoteUrl, originalCachePath, remoteFileSize)) {
                return false;
              }
            }

            LOG.debug("Did preload audio OK");
            updateProgress((float) loaded / audios.size());
            loaded++;
          }
        }
      }
      updateProgress(1.0);
      LOG.info("Preloaded {} audios from {} instruments", loaded, instruments.size());
      updateState(ProjectState.LoadedAudio);

      saveProjectContent();
      return true;

    } catch (HubClientException e) {
      LOG.error("Failed to load content for project!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
      updateState(ProjectState.Failed);
      return false;

    } catch (IOException e) {
      LOG.error("Failed to preload audio for project!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
      updateState(ProjectState.Failed);
      return false;

    } catch (Exception e) {
      LOG.error("Failed to clone project!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
      updateState(ProjectState.Failed);
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
   Download a file from the given URL to the given output path, retrying some number of times

   @param httpClient http client (don't close the client; only close the responses from it)
   @param url        URL to download from
   @param outputPath path to write to
   @return true if the file was downloaded successfully
   */
  private boolean downloadRemoteFileWithRetry(CloseableHttpClient httpClient, String url, String outputPath, long expectedSize) {
    for (int attempt = 1; attempt <= downloadAudioRetries; attempt++) {
      try {
        Path path = Paths.get(outputPath);
        Files.deleteIfExists(path);
        downloadRemoteFile(httpClient, url, outputPath);
        long downloadedSize = Files.size(path);
        if (downloadedSize==expectedSize) {
          return true;
        }
        LOG.info("File size does not match! Attempt " + attempt + " of " + downloadAudioRetries + " to download " + url + " to " + outputPath + " failed. Expected " + expectedSize + " bytes, but got " + downloadedSize + " bytes.");

      } catch (Exception e) {
        LOG.info("Attempt " + attempt + " of " + downloadAudioRetries + " to download " + url + " to " + outputPath + " failed because " + e.getMessage());
      }
    }
    return false;
  }

  /**
   Get the size of the file at the given URL

   @param httpClient http client (don't close the client; only close the responses from it)
   @param url        url
   @return size of the file
   @throws Exception if the file size could not be determined
   */
  private long getRemoteFileSize(CloseableHttpClient httpClient, String url) throws Exception {
    try (
      CloseableHttpResponse response = httpClient.execute(new HttpHead(url))
    ) {
      return Long.parseLong(response.getFirstHeader("Content-Length").getValue());
    } catch (Exception e) {
      throw new NexusException(String.format("Unable to get %s", url), e);
    }
  }

  /**
   Download a file from the given URL to the given output path

   @param httpClient http client (don't close the client; only close the responses from it)
   @param url        url
   @param outputPath output path
   */
  private void downloadRemoteFile(CloseableHttpClient httpClient, String url, String outputPath) throws IOException, NexusException {
    try (
      CloseableHttpResponse response = httpClient.execute(new HttpGet(url))
    ) {
      if (Objects.isNull(response.getEntity().getContent()))
        throw new NexusException(String.format("Unable to write bytes to disk: %s", outputPath));

      try (OutputStream toFile = FileUtils.openOutputStream(new File(outputPath))) {
        var size = IOUtils.copy(response.getEntity().getContent(), toFile); // stores number of bytes copied
        LOG.debug("Did write media item to disk: {} ({} bytes)", outputPath, size);
      }
    }
  }
}
