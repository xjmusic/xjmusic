// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.HubContent;
import io.xj.hub.ProgramConfig;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.enums.UserRoleType;
import io.xj.hub.tables.pojos.*;
import io.xj.hub.util.ValueException;
import io.xj.lib.util.FormatUtils;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.HubContentProvider;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.nexus.work.WorkConfiguration;
import io.xj.nexus.work.WorkFactory;
import jakarta.annotation.Nullable;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

@org.springframework.stereotype.Service
public class FabricationServiceImpl extends Service<Boolean> implements FabricationService {
  static final Logger LOG = LoggerFactory.getLogger(FabricationServiceImpl.class);
  private static final String defaultPathPrefix = System.getProperty("user.home") + File.separator + "XJ music" + File.separator + "Documents" + File.separator;
  private final Preferences prefs = Preferences.userNodeForPackage(FabricationServiceImpl.class);
  final static String BUTTON_TEXT_START = "Start";
  final static String BUTTON_TEXT_STOP = "Stop";
  final static String BUTTON_TEXT_RESET = "Reset";
  final HostServices hostServices;
  private final String defaultContentStoragePathPrefix = computeDefaultPathPrefix("content");
  private final String defaultOutputPathPrefix = computeDefaultPathPrefix("output");
  private final int defaultTimelineSegmentViewLimit;
  private final Integer defaultCraftAheadSeconds;
  private final Integer defaultDubAheadSeconds;
  private final Integer defaultShipAheadSeconds;
  private final String defaultInputTemplateKey;
  private final int defaultOutputChannels;
  private final String defaultOutputFileMode;
  private final double defaultOutputFrameRate;
  private final String defaultOutputMode;
  private final Integer defaultOutputSeconds;
  final WorkFactory workFactory;
  final HubClient hubClient;
  final LabService labService;
  final Map<Integer, Integer> segmentBarBeats = new ConcurrentHashMap<>();
  final ObjectProperty<FabricationStatus> status = new SimpleObjectProperty<>(FabricationStatus.Standby);
  final StringProperty inputTemplateKey = new SimpleStringProperty();
  final StringProperty contentStoragePathPrefix = new SimpleStringProperty();
  final StringProperty outputPathPrefix = new SimpleStringProperty();
  final ObjectProperty<InputMode> inputMode = new SimpleObjectProperty<>();
  final ObjectProperty<OutputFileMode> outputFileMode = new SimpleObjectProperty<>();
  final ObjectProperty<OutputMode> outputMode = new SimpleObjectProperty<>();
  final StringProperty outputSeconds = new SimpleStringProperty();
  final StringProperty craftAheadSeconds = new SimpleStringProperty();
  final StringProperty dubAheadSeconds = new SimpleStringProperty();
  final StringProperty shipAheadSeconds = new SimpleStringProperty();
  final StringProperty outputFrameRate = new SimpleStringProperty();
  final StringProperty outputChannels = new SimpleStringProperty();

  final StringProperty timelineSegmentViewLimit = new SimpleStringProperty();
  final BooleanProperty followPlayback = new SimpleBooleanProperty(true);
  final ObservableBooleanValue outputModeSync = Bindings.createBooleanBinding(() ->
    outputMode.get().isSync(), outputMode);
  final ObservableBooleanValue outputModeFile = Bindings.createBooleanBinding(() ->
    outputMode.get() == OutputMode.FILE, outputMode);
  final ObservableBooleanValue statusActive =
    Bindings.createBooleanBinding(() -> status.get() == FabricationStatus.Active, status);
  final ObservableBooleanValue statusStandby =
    Bindings.createBooleanBinding(() -> status.get() == FabricationStatus.Standby, status);
  final ObservableValue<String> mainActionButtonText = Bindings.createStringBinding(() ->
    switch (status.get()) {
      case Starting, Standby -> BUTTON_TEXT_START;
      case Active -> BUTTON_TEXT_STOP;
      case Cancelled, Failed, Done -> BUTTON_TEXT_RESET;
    }, status);


  public FabricationServiceImpl(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${gui.timeline.max.segments}") int defaultTimelineSegmentViewLimit,
    @Value("${craft.ahead.seconds}") Integer defaultCraftAheadSeconds,
    @Value("${dub.ahead.seconds}") Integer defaultDubAheadSeconds,
    @Value("${ship.ahead.seconds}") Integer defaultShipAheadSeconds,
    @Value("${input.template.key}") String defaultInputTemplateKey,
    @Value("${output.channels}") int defaultOutputChannels,
    @Value("${output.file.mode}") String defaultOutputFileMode,
    @Value("${output.frame.rate}") double defaultOutputFrameRate,
    @Value("${output.mode}") String defaultOutputMode,
    @Value("${output.seconds}") Integer defaultOutputSeconds,
    WorkFactory workFactory,
    HubClient hubClient,
    LabService labService
  ) {
    this.hostServices = hostServices;
    this.defaultTimelineSegmentViewLimit = defaultTimelineSegmentViewLimit;
    this.defaultCraftAheadSeconds = defaultCraftAheadSeconds;
    this.defaultDubAheadSeconds = defaultDubAheadSeconds;
    this.defaultShipAheadSeconds = defaultShipAheadSeconds;
    this.defaultInputTemplateKey = defaultInputTemplateKey;
    this.defaultOutputChannels = defaultOutputChannels;
    this.defaultOutputFileMode = defaultOutputFileMode;
    this.defaultOutputFrameRate = defaultOutputFrameRate;
    this.defaultOutputMode = defaultOutputMode;
    this.defaultOutputSeconds = defaultOutputSeconds;
    this.workFactory = workFactory;
    this.hubClient = hubClient;
    this.labService = labService;

    attachPreferenceListeners();
    setAllFromPrefsOrDefaults();

    setOnCancelled((WorkerStateEvent ignored) -> status.set(FabricationStatus.Cancelled));
    setOnFailed((WorkerStateEvent ignored) -> status.set(FabricationStatus.Failed));
    setOnReady((WorkerStateEvent ignored) -> status.set(FabricationStatus.Standby));
    setOnRunning((WorkerStateEvent ignored) -> status.set(FabricationStatus.Active));
    setOnScheduled((WorkerStateEvent ignored) -> status.set(FabricationStatus.Starting));
    setOnSucceeded((WorkerStateEvent ignored) -> status.set(FabricationStatus.Done));
  }

  private void attachPreferenceListeners() {
    contentStoragePathPrefix.addListener((o, ov, value) -> prefs.put("contentStoragePathPrefix", value));
    craftAheadSeconds.addListener((o, ov, value) -> prefs.put("craftAheadSeconds", value));
    dubAheadSeconds.addListener((o, ov, value) -> prefs.put("dubAheadSeconds", value));
    inputMode.addListener((o, ov, value) -> prefs.put("inputMode", value.name()));
    inputTemplateKey.addListener((o, ov, value) -> prefs.put("inputTemplateKey", value));
    outputChannels.addListener((o, ov, value) -> prefs.put("outputChannels", value));
    outputFileMode.addListener((o, ov, value) -> prefs.put("outputFileMode", value.name()));
    outputFrameRate.addListener((o, ov, value) -> prefs.put("outputFrameRate", value));
    outputMode.addListener((o, ov, value) -> prefs.put("outputMode", value.name()));
    outputPathPrefix.addListener((o, ov, value) -> prefs.put("outputPathPrefix", value));
    outputSeconds.addListener((o, ov, value) -> prefs.put("outputSeconds", value));
    shipAheadSeconds.addListener((o, ov, value) -> prefs.put("shipAheadSeconds", value));
    timelineSegmentViewLimit.addListener((o, ov, value) -> prefs.put("timelineSegmentViewLimit", value));
  }

  private void setAllFromPrefsOrDefaults() {
    contentStoragePathPrefix.set(prefs.get("contentStoragePathPrefix", defaultContentStoragePathPrefix));
    craftAheadSeconds.set(prefs.get("craftAheadSeconds", Integer.toString(defaultCraftAheadSeconds)));
    dubAheadSeconds.set(prefs.get("dubAheadSeconds", Integer.toString(defaultDubAheadSeconds)));
    inputMode.set(InputMode.valueOf(prefs.get("inputMode", InputMode.PRODUCTION.name())));
    inputTemplateKey.set(prefs.get("inputTemplateKey", defaultInputTemplateKey));
    outputChannels.set(prefs.get("outputChannels", Integer.toString(defaultOutputChannels)));
    outputFileMode.set(OutputFileMode.valueOf(prefs.get("outputFileMode", defaultOutputFileMode.toUpperCase(Locale.ROOT))));
    outputFrameRate.set(prefs.get("outputFrameRate", Double.toString(defaultOutputFrameRate)));
    outputMode.set(OutputMode.valueOf(prefs.get("outputMode", defaultOutputMode.toUpperCase(Locale.ROOT))));
    outputPathPrefix.set(prefs.get("outputPathPrefix", defaultOutputPathPrefix));
    outputSeconds.set(prefs.get("outputSeconds", Integer.toString(defaultOutputSeconds)));
    shipAheadSeconds.set(prefs.get("shipAheadSeconds", Integer.toString(defaultShipAheadSeconds)));
    timelineSegmentViewLimit.set(prefs.get("timelineSegmentViewLimit", Integer.toString(defaultTimelineSegmentViewLimit)));
  }

  @Override
  protected Task<Boolean> createTask() {
    return new Task<>() {
      protected Boolean call() {
        return workFactory.start(
          getWorkConfig(),
          labService.hubConfigProperty().get(),
          getHubContentProvider(),
          (Double ratio) -> updateProgress(ratio, 1.0),
          () -> updateProgress(1.0, 1.0));
      }
    };
  }

  @Override
  public Callable<HubContent> getHubContentProvider() {
    var hubAccess = new HubClientAccess()
      .setRoleTypes(List.of(UserRoleType.Internal))
      .setToken(labService.accessTokenProperty().get());

    return new HubContentProvider(
      hubClient,
      labService.hubConfigProperty().get(),
      hubAccess,
      inputMode.get(),
      inputTemplateKey.get());
  }

  @Override
  public WorkConfiguration getWorkConfig() {
    return new WorkConfiguration()
      .setContentStoragePathPrefix(contentStoragePathPrefix.get())
      .setCraftAheadSeconds(Integer.parseInt(craftAheadSeconds.get()))
      .setDubAheadSeconds(Integer.parseInt(dubAheadSeconds.get()))
      .setInputMode(inputMode.get())
      .setInputTemplateKey(inputTemplateKey.get())
      .setOutputChannels(Integer.parseInt(outputChannels.get()))
      .setOutputFileMode(outputFileMode.get())
      .setOutputFrameRate(Double.parseDouble(outputFrameRate.get()))
      .setOutputMode(outputMode.get())
      .setOutputPathPrefix(outputPathPrefix.get())
      .setOutputSeconds(Integer.parseInt(outputSeconds.get()))
      .setShipAheadSeconds(Integer.parseInt(shipAheadSeconds.get()));
  }

  @Override
  public ObjectProperty<FabricationStatus> statusProperty() {
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
  public StringProperty shipAheadSecondsProperty() {
    return shipAheadSeconds;
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
  public WorkFactory getWorkFactory() {
    return workFactory;
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes(Segment segment) {
    try {
      return workFactory.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentMeme.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment memes", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentChord> getSegmentChords(Segment segment) {
    try {
      return workFactory.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentChord.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment chords", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentChoice> getSegmentChoices(Segment segment) {
    try {
      return workFactory.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentChoice.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment choices", e);
      return List.of();
    }
  }

  @Override
  public void reset() {
    super.reset();
    workFactory.reset();
  }

  @Override
  public Optional<Program> getProgram(UUID programId) {
    return workFactory.getSourceMaterial().getProgram(programId);
  }

  @Override
  public Optional<ProgramVoice> getProgramVoice(UUID programVoiceId) {
    return workFactory.getSourceMaterial().getProgramVoice(programVoiceId);
  }

  @Override
  public Optional<ProgramSequence> getProgramSequence(UUID programSequenceId) {
    return workFactory.getSourceMaterial().getProgramSequence(programSequenceId);
  }

  @Override
  public Optional<ProgramSequenceBinding> getProgramSequenceBinding(UUID programSequenceBindingId) {
    return workFactory.getSourceMaterial().getProgramSequenceBinding(programSequenceBindingId);
  }

  @Override
  public Optional<Instrument> getInstrument(UUID instrumentId) {
    return workFactory.getSourceMaterial().getInstrument(instrumentId);
  }

  @Override
  public Optional<InstrumentAudio> getInstrumentAudio(UUID instrumentAudioId) {
    return workFactory.getSourceMaterial().getInstrumentAudio(instrumentAudioId);
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice choice) {
    try {
      return workFactory.getSegmentManager().readManySubEntitiesOfType(choice.getSegmentId(), SegmentChoiceArrangement.class)
        .stream().filter(arrangement -> arrangement.getSegmentChoiceId().equals(choice.getId())).toList();
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment choice arrangements", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks(SegmentChoiceArrangement arrangement) {
    try {
      return workFactory.getSegmentManager().readManySubEntitiesOfType(arrangement.getSegmentId(), SegmentChoiceArrangementPick.class)
        .stream().filter(pick -> pick.getSegmentChoiceArrangementId().equals(arrangement.getId())).toList();
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment choice arrangement picks", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentMessage> getSegmentMessages(Segment segment) {
    try {
      return workFactory.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentMessage.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment messages", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentMeta> getSegmentMetas(Segment segment) {
    try {
      return workFactory.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentMeta.class);
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
      var from = Objects.nonNull(startIndex) ? startIndex : Math.max(0, workFactory.getSegmentManager().size() - viewLimit - 1);
      var to = Math.min(workFactory.getSegmentManager().size() - 1, from + viewLimit);
      return workFactory
        .getSegmentManager()
        .readManyFromToOffset(from, to);

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException e) {
      LOG.error("Failed to get segments", e);
      return List.of();
    }
  }

  @Override
  public Boolean isEmpty() {
    return workFactory.getSegmentManager().isEmpty();
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
  public ObservableBooleanValue isOutputModeSync() {
    return outputModeSync;
  }

  @Override
  public ObservableBooleanValue isStatusActive() {
    return statusActive;
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
  public void handleMainAction() {
    switch (status.get()) {
      case Standby -> start();
      case Active -> cancel();
      case Cancelled, Done, Failed -> reset();
    }
  }

  @Override
  public ObservableValue<String> mainActionButtonTextProperty() {
    return mainActionButtonText;
  }

  @Override
  public void handleDemoPlay(String templateKey, Integer craftAheadSeconds) {
    if (status.get() != FabricationStatus.Standby) {
      LOG.error("Cannot play demo unless fabrication is in Standby status");
      return;
    }

    this.craftAheadSeconds.set(craftAheadSeconds.toString());
    dubAheadSeconds.set(Integer.toString(defaultDubAheadSeconds));
    shipAheadSeconds.set(Integer.toString(defaultShipAheadSeconds));
    inputMode.set(InputMode.PRODUCTION);
    inputTemplateKey.set(templateKey);
    outputFileMode.set(OutputFileMode.valueOf(defaultOutputFileMode.toUpperCase(Locale.ROOT)));
    outputMode.set(OutputMode.valueOf(defaultOutputMode.toUpperCase(Locale.ROOT)));

    start();
  }

  @Override
  public String getChoiceHash(Segment segment) {
    return workFactory.getSegmentManager().getChoiceHash(segment);
  }

  @Override
  public Optional<Segment> getSegmentAtShipOutput() {
    return
      workFactory.getShippedToChainMicros().flatMap(chainMicros ->
        workFactory.getSegmentManager().readOneAtChainMicros(chainMicros));
  }

  private String formatTotalBars(int bars, String fraction) {
    return String.format("%d%s bar%s", bars, fraction, bars == 1 ? "" : "s");
  }

  private Optional<Integer> getBarBeats(Segment segment) {
    if (!segmentBarBeats.containsKey(segment.getId())) {
      try {
        var choice = workFactory.getSegmentManager().readChoice(segment.getId(), ProgramType.Main);
        if (choice.isEmpty()) {
          LOG.error("Failed to retrieve main program choice to determine beats for Segment[{}]", segment.getId());
          return Optional.empty();
        }

        var program = workFactory.getSourceMaterial().getProgram(choice.get().getProgramId());
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


  String computeProgramName(@Nullable Program program, @Nullable ProgramSequence programSequence, @Nullable ProgramSequenceBinding programSequenceBinding) {
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
