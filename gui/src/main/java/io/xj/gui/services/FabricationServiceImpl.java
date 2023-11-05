// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.ProgramConfig;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.enums.UserRoleType;
import io.xj.hub.tables.pojos.*;
import io.xj.hub.util.ValueException;
import io.xj.lib.util.FormatUtils;
import io.xj.nexus.InputMode;
import io.xj.nexus.MacroMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.nexus.work.WorkConfiguration;
import io.xj.nexus.work.WorkManager;
import io.xj.nexus.work.WorkState;
import jakarta.annotation.Nullable;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;

@Service
public class FabricationServiceImpl implements FabricationService {
  private static final Logger LOG = LoggerFactory.getLogger(FabricationServiceImpl.class);
  private static final String defaultPathPrefix = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "XJ music" + File.separator;
  private final Preferences prefs = Preferences.userNodeForPackage(FabricationServiceImpl.class);
  private final static String BUTTON_TEXT_START = "Start";
  private final static String BUTTON_TEXT_STOP = "Stop";
  private final static String BUTTON_TEXT_RESET = "Reset";
  private final HostServices hostServices;
  private final String defaultContentStoragePathPrefix = computeDefaultPathPrefix("content");
  private final String defaultOutputPathPrefix = computeDefaultPathPrefix("output");
  private final int defaultTimelineSegmentViewLimit;
  private final Integer defaultCraftAheadSeconds;
  private final Integer defaultDubAheadSeconds;
  private final String defaultInputTemplateKey;
  private final int defaultOutputChannels;
  private final OutputFileMode defaultOutputFileMode;
  private final double defaultOutputFrameRate;
  private final MacroMode defaultMacroMode;
  private final InputMode defaultInputMode;
  private final OutputMode defaultOutputMode;
  private final Integer defaultOutputSeconds;
  final WorkManager workManager;
  final HubClient hubClient;
  final LabService labService;
  final Map<Integer, Integer> segmentBarBeats = new ConcurrentHashMap<>();
  final ObjectProperty<WorkState> status = new SimpleObjectProperty<>(WorkState.Standby);
  final StringProperty inputTemplateKey = new SimpleStringProperty();
  final StringProperty contentStoragePathPrefix = new SimpleStringProperty();
  final StringProperty outputPathPrefix = new SimpleStringProperty();
  final ObjectProperty<InputMode> inputMode = new SimpleObjectProperty<>();
  final ObjectProperty<MacroMode> macroMode = new SimpleObjectProperty<>();
  final ObjectProperty<OutputFileMode> outputFileMode = new SimpleObjectProperty<>();
  final ObjectProperty<OutputMode> outputMode = new SimpleObjectProperty<>();
  final StringProperty outputSeconds = new SimpleStringProperty();
  final StringProperty craftAheadSeconds = new SimpleStringProperty();
  final StringProperty dubAheadSeconds = new SimpleStringProperty();
  final StringProperty outputFrameRate = new SimpleStringProperty();
  final StringProperty outputChannels = new SimpleStringProperty();

  final StringProperty timelineSegmentViewLimit = new SimpleStringProperty();
  final BooleanProperty followPlayback = new SimpleBooleanProperty(true);
  final DoubleProperty progress = new SimpleDoubleProperty(0.0);
  final ObservableBooleanValue outputModeSync = Bindings.createBooleanBinding(() ->
    outputMode.get().isSync(), outputMode);
  private final ObservableBooleanValue outputModeFile = Bindings.createBooleanBinding(() ->
    outputMode.get() == OutputMode.FILE, outputMode);
  private final ObservableBooleanValue statusActive =
    Bindings.createBooleanBinding(() -> status.get() == WorkState.Active, status);
  private final ObservableBooleanValue statusStandby =
    Bindings.createBooleanBinding(() -> status.get() == WorkState.Standby, status);
  private final ObservableBooleanValue statusLoading =
    Bindings.createBooleanBinding(() -> status.get() == WorkState.LoadingContent || status.get() == WorkState.LoadedContent || status.get() == WorkState.PreparingAudio || status.get() == WorkState.PreparedAudio, status);

  private final ObservableValue<String> mainActionButtonText = Bindings.createStringBinding(() ->
    switch (status.get()) {
      case Starting, Standby -> BUTTON_TEXT_START;
      case LoadingContent, LoadedContent, PreparingAudio, PreparedAudio, Initializing, Active -> BUTTON_TEXT_STOP;
      case Cancelled, Failed, Done -> BUTTON_TEXT_RESET;
    }, status);

  public FabricationServiceImpl(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${craft.ahead.seconds}") int defaultCraftAheadSeconds,
    @Value("${dub.ahead.seconds}") int defaultDubAheadSeconds,
    @Value("${gui.timeline.max.segments}") int defaultTimelineSegmentViewLimit,
    @Value("${input.template.key}") String defaultInputTemplateKey,
    @Value("${output.channels}") int defaultOutputChannels,
    @Value("${output.file.mode}") String defaultOutputFileMode,
    @Value("${output.frame.rate}") double defaultOutputFrameRate,
    @Value("${macro.mode}") String defaultMacroMode,
    @Value("${input.mode}") String defaultInputMode,
    @Value("${output.mode}") String defaultOutputMode,
    @Value("${output.seconds}") int defaultOutputSeconds,
    HubClient hubClient,
    LabService labService,
    WorkManager workManager
  ) {
    this.defaultCraftAheadSeconds = defaultCraftAheadSeconds;
    this.defaultDubAheadSeconds = defaultDubAheadSeconds;
    this.defaultMacroMode = MacroMode.valueOf(defaultMacroMode.toUpperCase(Locale.ROOT));
    this.defaultInputMode = InputMode.valueOf(defaultInputMode.toUpperCase(Locale.ROOT));
    this.defaultInputTemplateKey = defaultInputTemplateKey;
    this.defaultOutputChannels = defaultOutputChannels;
    this.defaultOutputFileMode = OutputFileMode.valueOf(defaultOutputFileMode.toUpperCase(Locale.ROOT));
    this.defaultOutputFrameRate = defaultOutputFrameRate;
    this.defaultOutputMode = OutputMode.valueOf(defaultOutputMode.toUpperCase(Locale.ROOT));
    this.defaultOutputSeconds = defaultOutputSeconds;
    this.defaultTimelineSegmentViewLimit = defaultTimelineSegmentViewLimit;
    this.hostServices = hostServices;
    this.workManager = workManager;
    this.hubClient = hubClient;
    this.labService = labService;

    attachPreferenceListeners();
    setAllFromPrefsOrDefaults();
  }

  @Override
  public void start() {
    if (status.get() != WorkState.Standby) {
      LOG.error("Cannot start fabrication unless in Standby status");
      return;
    }
    status.set(WorkState.Starting);

    // create work configuration
    var config = new WorkConfiguration()
      .setContentStoragePathPrefix(contentStoragePathPrefix.get())
      .setCraftAheadMicros(Long.parseLong(craftAheadSeconds.get()) * MICROS_PER_SECOND)
      .setDubAheadMicros(Long.parseLong(dubAheadSeconds.get()) * MICROS_PER_SECOND)
      .setInputMode(inputMode.get())
      .setMacroMode(macroMode.get())
      .setInputTemplateKey(inputTemplateKey.get())
      .setOutputChannels(Integer.parseInt(outputChannels.get()))
      .setOutputFileMode(outputFileMode.get())
      .setOutputFrameRate(Double.parseDouble(outputFrameRate.get()))
      .setOutputMode(outputMode.get())
      .setOutputPathPrefix(outputPathPrefix.get())
      .setOutputSeconds(Integer.parseInt(outputSeconds.get()));

    var hubAccess = new HubClientAccess()
      .setRoleTypes(List.of(UserRoleType.Internal))
      .setToken(labService.accessTokenProperty().get());

    // start the work with the given configuration
    workManager.setOnProgress((Float progress) -> Platform.runLater(() -> this.progress.set(progress)));
    workManager.setOnStateChange((WorkState state) -> Platform.runLater(() -> status.set(state)));
    Platform.runLater(() -> workManager.start(config, labService.hubConfigProperty().get(), hubAccess));
  }

  @Override
  public void cancel() {
    status.set(WorkState.Cancelled);
    workManager.finish(true);
  }

  @Override
  public void reset() {
    status.set(WorkState.Standby);
    workManager.reset();
  }

  @Override
  public List<Program> getAllMacroPrograms() {
    return workManager.getSourceMaterial().getPrograms(ProgramType.Macro).stream()
      .sorted(Comparator.comparing(Program::getName))
      .toList();
  }

  @Override
  public ObjectProperty<WorkState> statusProperty() {
    return status;
  }

  @Override
  public StringProperty inputTemplateKeyProperty() {
    return inputTemplateKey;
  }

  @Override
  public StringProperty contentStoragePathPrefixProperty() {
    return contentStoragePathPrefix;
  }

  @Override
  public StringProperty outputPathPrefixProperty() {
    return outputPathPrefix;
  }

  @Override
  public ObjectProperty<InputMode> inputModeProperty() {
    return inputMode;
  }

  @Override
  public ObjectProperty<MacroMode> macroModeProperty() {
    return macroMode;
  }

  @Override
  public ObjectProperty<OutputFileMode> outputFileModeProperty() {
    return outputFileMode;
  }

  @Override
  public ObjectProperty<OutputMode> outputModeProperty() {
    return outputMode;
  }

  @Override
  public StringProperty outputSecondsProperty() {
    return outputSeconds;
  }

  @Override
  public StringProperty craftAheadSecondsProperty() {
    return craftAheadSeconds;
  }

  @Override
  public StringProperty dubAheadSecondsProperty() {
    return dubAheadSeconds;
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
  public StringProperty timelineSegmentViewLimitProperty() {
    return timelineSegmentViewLimit;
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes(Segment segment) {
    try {
      return workManager.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentMeme.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment memes", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentChord> getSegmentChords(Segment segment) {
    try {
      return workManager.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentChord.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment chords", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentChoice> getSegmentChoices(Segment segment) {
    try {
      return workManager.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentChoice.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment choices", e);
      return List.of();
    }
  }

  @Override
  public Optional<Program> getProgram(UUID programId) {
    return workManager.getSourceMaterial().getProgram(programId);
  }

  @Override
  public Optional<ProgramVoice> getProgramVoice(UUID programVoiceId) {
    return workManager.getSourceMaterial().getProgramVoice(programVoiceId);
  }

  @Override
  public Optional<ProgramSequence> getProgramSequence(UUID programSequenceId) {
    return workManager.getSourceMaterial().getProgramSequence(programSequenceId);
  }

  @Override
  public Optional<ProgramSequenceBinding> getProgramSequenceBinding(UUID programSequenceBindingId) {
    return workManager.getSourceMaterial().getProgramSequenceBinding(programSequenceBindingId);
  }

  @Override
  public Optional<Instrument> getInstrument(UUID instrumentId) {
    return workManager.getSourceMaterial().getInstrument(instrumentId);
  }

  @Override
  public Optional<InstrumentAudio> getInstrumentAudio(UUID instrumentAudioId) {
    return workManager.getSourceMaterial().getInstrumentAudio(instrumentAudioId);
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice choice) {
    try {
      return workManager.getSegmentManager().readManySubEntitiesOfType(choice.getSegmentId(), SegmentChoiceArrangement.class)
        .stream().filter(arrangement -> arrangement.getSegmentChoiceId().equals(choice.getId())).toList();
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment choice arrangements", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks(SegmentChoiceArrangement arrangement) {
    try {
      return workManager.getSegmentManager().readManySubEntitiesOfType(arrangement.getSegmentId(), SegmentChoiceArrangementPick.class)
        .stream().filter(pick -> pick.getSegmentChoiceArrangementId().equals(arrangement.getId())).toList();
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment choice arrangement picks", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentMessage> getSegmentMessages(Segment segment) {
    try {
      return workManager.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentMessage.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment messages", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentMeta> getSegmentMetas(Segment segment) {
    try {
      return workManager.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentMeta.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment metas", e);
      return List.of();
    }
  }

  @Override
  public Node computeProgramReferenceNode(UUID programId, @Nullable UUID programSequenceBindingId) {
    var program = getProgram(programId);
    Optional<ProgramSequenceBinding> programSequenceBinding = Objects.nonNull(programSequenceBindingId) ? getProgramSequenceBinding(programSequenceBindingId) : Optional.empty();
    var programSequence = programSequenceBinding.map(ProgramSequenceBinding::getProgramSequenceId).flatMap(this::getProgramSequence);

    var programUrl = labService.computeUrl(String.format("programs/%s", programId));

    var hyperlink = new Hyperlink(computeProgramName(program.orElse(null), programSequence.orElse(null), programSequenceBinding.orElse(null)));
    hyperlink.setOnAction(event -> hostServices.showDocument(programUrl));
    return hyperlink;
  }

  @Override
  public Node computeProgramVoiceReferenceNode(UUID programVoiceId) {
    var programVoice = getProgramVoice(programVoiceId);

    var programUrl = labService.computeUrl(String.format("programs/%s", programVoice.orElseThrow().getProgramId()));

    var hyperlink = new Hyperlink(programVoice.orElseThrow().getName());
    hyperlink.setOnAction(event -> hostServices.showDocument(programUrl));
    return hyperlink;
  }

  @Override
  public Node computeInstrumentReferenceNode(UUID instrumentId) {
    var instrument = getInstrument(instrumentId);

    var instrumentUrl = labService.computeUrl(String.format("instruments/%s", instrumentId));

    var hyperlink = new Hyperlink(instrument.orElseThrow().getName());
    hyperlink.setOnAction(event -> hostServices.showDocument(instrumentUrl));
    return hyperlink;
  }

  @Override
  public Node computeInstrumentAudioReferenceNode(UUID instrumentAudioId) {
    var instrumentAudio = getInstrumentAudio(instrumentAudioId);

    var instrumentUrl = labService.computeUrl(String.format("instruments/%s", instrumentAudio.orElseThrow().getInstrumentId()));

    var hyperlink = new Hyperlink(instrumentAudio.orElseThrow().getName());
    hyperlink.setOnAction(event -> hostServices.showDocument(instrumentUrl));
    return hyperlink;
  }

  @Override
  public List<Segment> getSegments(@Nullable Integer startIndex) {
    try {
      var viewLimit = Integer.parseInt(timelineSegmentViewLimit.getValue());
      var from = Objects.nonNull(startIndex) ? startIndex : Math.max(0, workManager.getSegmentManager().size() - viewLimit - 1);
      var to = Math.min(workManager.getSegmentManager().size() - 1, from + viewLimit);
      return workManager.getSegmentManager().readManyFromToOffset(from, to);

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException e) {
      LOG.error("Failed to get segments", e);
      return List.of();
    }
  }

  @Override
  public Boolean isEmpty() {
    return workManager.getSegmentManager().isEmpty();
  }

  @Override
  public String formatTotalBars(Segment segment, @Nullable Integer beats) {
    return Objects.nonNull(beats)
      ? getBarBeats(segment)
      .map(barBeats -> formatTotalBars((int) Math.floor((float) beats / barBeats),
        FormatUtils.formatFractionalSuffix((float) (beats % barBeats) / barBeats)))
      .orElse(String.format("%d beat%s", beats, beats == 1 ? "" : "s"))
      : "N/A";
  }

  @Override
  public String formatPositionBarBeats(Segment segment, @Nullable Double position) {
    return
      Objects.nonNull(position) ?
        getBarBeats(segment)
          .map(barBeats -> {
            var bars = (int) Math.floor(position / barBeats);
            var beats = (int) Math.floor(position % barBeats);
            var remaining = beats > 0 ? position % barBeats % beats : 0;
            return String.format("%d.%d%s", bars + 1, beats + 1, FormatUtils.formatDecimalSuffix(remaining));
          })
          .orElse(FormatUtils.formatMinDecimal(position))
        : "N/A";
  }

  @Override
  public BooleanProperty followPlaybackProperty() {
    return followPlayback;
  }

  @Override
  public ObservableDoubleValue progressProperty() {
    return progress;
  }

  @Override
  public ObservableBooleanValue isOutputModeSync() {
    return outputModeSync;
  }

  @Override
  public ObservableBooleanValue isStatusActive() {
    return statusActive;
  }

  @Override
  public ObservableBooleanValue isStatusLoading() {
    return statusLoading;
  }

  @Override
  public ObservableBooleanValue isStatusStandby() {
    return statusStandby;
  }

  @Override
  public ObservableBooleanValue isOutputModeFile() {
    return outputModeFile;
  }

  @Override
  public Optional<Long> getShippedToChainMicros() {
    return workManager.getShippedToChainMicros();
  }

  @Override
  public Optional<Long> getDubbedToChainMicros() {
    return workManager.getDubbedToChainMicros();
  }

  @Override
  public Optional<Long> getCraftedToChainMicros() {
    return workManager.getCraftedToChainMicros();
  }

  @Override
  public Optional<Long> getShipTargetChainMicros() {
    return workManager.getShipTargetChainMicros();
  }


  @Override
  public void handleMainAction() {
    switch (status.get()) {
      case Standby -> start();
      case Cancelled, Done, Failed -> reset();
      default -> cancel();
    }
  }

  @Override
  public ObservableValue<String> mainActionButtonTextProperty() {
    return mainActionButtonText;
  }

  @Override
  public void handleDemoPlay(String templateKey, Integer craftAheadSeconds) {
    if (status.get() != WorkState.Standby) {
      LOG.error("Cannot play demo unless fabrication is in Standby status");
      return;
    }

    this.craftAheadSeconds.set(craftAheadSeconds.toString());
    dubAheadSeconds.set(Integer.toString(defaultDubAheadSeconds));
    inputTemplateKey.set(templateKey);
    outputFileMode.set(defaultOutputFileMode);
    outputMode.set(defaultOutputMode);
    inputMode.set(defaultInputMode);

    start();
  }

  @Override
  public String getChoiceHash(Segment segment) {
    return workManager.getSegmentManager().getChoiceHash(segment);
  }

  @Override
  public Optional<Segment> getSegmentAtShipOutput() {
    return
      workManager.getShippedToChainMicros().flatMap(chainMicros ->
        workManager.getSegmentManager().readOneAtChainMicros(chainMicros));
  }

  private void attachPreferenceListeners() {
    contentStoragePathPrefix.addListener((o, ov, value) -> prefs.put("contentStoragePathPrefix", value));
    craftAheadSeconds.addListener((o, ov, value) -> prefs.put("craftAheadSeconds", value));
    dubAheadSeconds.addListener((o, ov, value) -> prefs.put("dubAheadSeconds", value));
    inputMode.addListener((o, ov, value) -> prefs.put("inputMode", Objects.nonNull(value) ? value.name() : ""));
    inputTemplateKey.addListener((o, ov, value) -> prefs.put("inputTemplateKey", value));
    macroMode.addListener((o, ov, value) -> prefs.put("macroMode", Objects.nonNull(value) ? value.name() : ""));
    outputChannels.addListener((o, ov, value) -> prefs.put("outputChannels", value));
    outputFileMode.addListener((o, ov, value) -> prefs.put("outputFileMode", Objects.nonNull(value) ? value.name() : ""));
    outputFrameRate.addListener((o, ov, value) -> prefs.put("outputFrameRate", value));
    outputMode.addListener((o, ov, value) -> prefs.put("outputMode", Objects.nonNull(value) ? value.name() : ""));
    outputPathPrefix.addListener((o, ov, value) -> prefs.put("outputPathPrefix", value));
    outputSeconds.addListener((o, ov, value) -> prefs.put("outputSeconds", value));
    timelineSegmentViewLimit.addListener((o, ov, value) -> prefs.put("timelineSegmentViewLimit", value));
  }

  private void setAllFromPrefsOrDefaults() {
    contentStoragePathPrefix.set(prefs.get("contentStoragePathPrefix", defaultContentStoragePathPrefix));
    craftAheadSeconds.set(prefs.get("craftAheadSeconds", Integer.toString(defaultCraftAheadSeconds)));
    dubAheadSeconds.set(prefs.get("dubAheadSeconds", Integer.toString(defaultDubAheadSeconds)));
    inputTemplateKey.set(prefs.get("inputTemplateKey", defaultInputTemplateKey));
    outputChannels.set(prefs.get("outputChannels", Integer.toString(defaultOutputChannels)));
    outputFrameRate.set(prefs.get("outputFrameRate", Double.toString(defaultOutputFrameRate)));
    outputPathPrefix.set(prefs.get("outputPathPrefix", defaultOutputPathPrefix));
    outputSeconds.set(prefs.get("outputSeconds", Integer.toString(defaultOutputSeconds)));
    timelineSegmentViewLimit.set(prefs.get("timelineSegmentViewLimit", Integer.toString(defaultTimelineSegmentViewLimit)));

    try {
      macroMode.set(MacroMode.valueOf(prefs.get("macroMode", defaultMacroMode.toString()).toUpperCase(Locale.ROOT)));
    } catch (Exception e) {
      LOG.error("Failed to set macro mode from preferences", e);
      macroMode.set(defaultMacroMode);
    }

    try {
      inputMode.set(InputMode.valueOf(prefs.get("inputMode", defaultInputMode.toString()).toUpperCase(Locale.ROOT)));
    } catch (Exception e) {
      LOG.error("Failed to set input mode from preferences", e);
      inputMode.set(defaultInputMode);
    }

    try {
      outputMode.set(OutputMode.valueOf(prefs.get("outputMode", defaultOutputMode.toString()).toUpperCase(Locale.ROOT)));
    } catch (Exception e) {
      LOG.error("Failed to set output mode from preferences", e);
      outputMode.set(defaultOutputMode);
    }

    try {
      outputFileMode.set(OutputFileMode.valueOf(prefs.get("outputFileMode", defaultOutputFileMode.toString()).toUpperCase(Locale.ROOT)));
    } catch (Exception e) {
      LOG.error("Failed to set output file mode from preferences", e);
      outputFileMode.set(defaultOutputFileMode);
    }
  }

  private String formatTotalBars(int bars, String fraction) {
    return String.format("%d%s bar%s", bars, fraction, bars == 1 ? "" : "s");
  }

  private Optional<Integer> getBarBeats(Segment segment) {
    if (!segmentBarBeats.containsKey(segment.getId())) {
      try {
        var choice = workManager.getSegmentManager().readChoice(segment.getId(), ProgramType.Main);
        if (choice.isEmpty()) {
          LOG.error("Failed to retrieve main program choice to determine beats for Segment[{}]", segment.getId());
          return Optional.empty();
        }

        var program = workManager.getSourceMaterial().getProgram(choice.get().getProgramId());
        if (program.isEmpty()) {
          LOG.error("Failed to retrieve main program to determine beats for Segment[{}]", segment.getId());
          return Optional.empty();
        }

        var config = new ProgramConfig(program.get());
        segmentBarBeats.put(segment.getId(), config.getBarBeats());

      } catch (ManagerFatalException | ValueException e) {
        LOG.error("Failed to format beats duration for Segment[{}]", segment.getId(), e);
        return Optional.empty();
      }
    }
    return Optional.of(segmentBarBeats.get(segment.getId()));
  }

  String computeProgramName(@Nullable Program program, @Nullable ProgramSequence
    programSequence, @Nullable ProgramSequenceBinding programSequenceBinding) {
    if (Objects.nonNull(program) && Objects.nonNull(programSequence) && Objects.nonNull(programSequenceBinding))
      return String.format("%s (%s)", program.getName(), programSequence.getName());
    else if (Objects.nonNull(program))
      return program.getName();
    else return "Not Loaded";
  }

  private static String computeDefaultPathPrefix(String category) {
    return defaultPathPrefix + category + File.separator;
  }
}
