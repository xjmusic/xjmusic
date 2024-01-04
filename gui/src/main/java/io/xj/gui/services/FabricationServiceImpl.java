// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.ProgramConfig;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.meme.MemeTaxonomy;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.util.ValueException;
import io.xj.nexus.InputMode;
import io.xj.nexus.ControlMode;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.model.SegmentMessage;
import io.xj.nexus.model.SegmentMeta;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.nexus.util.FormatUtils;
import io.xj.nexus.work.WorkConfiguration;
import io.xj.nexus.work.WorkManager;
import io.xj.nexus.work.WorkState;
import jakarta.annotation.Nullable;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

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
  private final int defaultTimelineSegmentViewLimit;
  private final Integer defaultCraftAheadSeconds;
  private final Integer defaultDubAheadSeconds;
  private final Integer defaultMixerLengthSeconds;
  private final String defaultInputTemplateKey;
  private final int defaultOutputChannels;
  private final double defaultOutputFrameRate;
  private final ControlMode defaultControlMode;
  private final InputMode defaultInputMode;
  final WorkManager workManager;
  final LabService labService;
  final Map<Integer, Integer> segmentBarBeats = new ConcurrentHashMap<>();
  final ObjectProperty<WorkState> status = new SimpleObjectProperty<>(WorkState.Standby);
  final StringProperty inputTemplateKey = new SimpleStringProperty();
  final StringProperty contentStoragePathPrefix = new SimpleStringProperty();
  final ObjectProperty<InputMode> inputMode = new SimpleObjectProperty<>();
  final ObjectProperty<ControlMode> controlMode = new SimpleObjectProperty<>();
  final StringProperty craftAheadSeconds = new SimpleStringProperty();
  final StringProperty dubAheadSeconds = new SimpleStringProperty();
  final StringProperty mixerLengthSeconds = new SimpleStringProperty();
  final StringProperty outputFrameRate = new SimpleStringProperty();
  final StringProperty outputChannels = new SimpleStringProperty();

  final StringProperty timelineSegmentViewLimit = new SimpleStringProperty();
  final BooleanProperty followPlayback = new SimpleBooleanProperty(true);
  final DoubleProperty progress = new SimpleDoubleProperty(0.0);
  private final ObservableBooleanValue statusActive =
    Bindings.createBooleanBinding(() -> status.get() == WorkState.Active, status);
  private final ObservableBooleanValue statusStandby =
    Bindings.createBooleanBinding(() -> status.get() == WorkState.Standby, status);
  private final ObservableBooleanValue statusLoading =
    Bindings.createBooleanBinding(() -> status.get() == WorkState.LoadingContent || status.get() == WorkState.LoadedContent || status.get() == WorkState.PreparingAudio || status.get() == WorkState.PreparedAudio, status);

  private final ObservableValue<String> mainActionButtonText = Bindings.createStringBinding(() ->
    switch (status.get()) {
      case Standby -> BUTTON_TEXT_START;
      case Starting, LoadingContent, LoadedContent, PreparingAudio, PreparedAudio, Initializing, Active ->
        BUTTON_TEXT_STOP;
      case Cancelled, Failed, Done -> BUTTON_TEXT_RESET;
    }, status);

  public FabricationServiceImpl(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${craft.ahead.seconds}") int defaultCraftAheadSeconds,
    @Value("${dub.ahead.seconds}") int defaultDubAheadSeconds,
    @Value("${mixer.length.seconds}") int defaultMixerLengthSeconds,
    @Value("${gui.timeline.max.segments}") int defaultTimelineSegmentViewLimit,
    @Value("${input.template.key}") String defaultInputTemplateKey,
    @Value("${output.channels}") int defaultOutputChannels,
    @Value("${output.frame.rate}") double defaultOutputFrameRate,
    @Value("${macro.mode}") String defaultMacroMode,
    @Value("${input.mode}") String defaultInputMode,
    LabService labService,
    WorkManager workManager
  ) {
    this.defaultCraftAheadSeconds = defaultCraftAheadSeconds;
    this.defaultDubAheadSeconds = defaultDubAheadSeconds;
    this.defaultMixerLengthSeconds = defaultMixerLengthSeconds;
    this.defaultControlMode = ControlMode.valueOf(defaultMacroMode.toUpperCase(Locale.ROOT));
    this.defaultInputMode = InputMode.valueOf(defaultInputMode.toUpperCase(Locale.ROOT));
    this.defaultInputTemplateKey = defaultInputTemplateKey;
    this.defaultOutputChannels = defaultOutputChannels;
    this.defaultOutputFrameRate = defaultOutputFrameRate;
    this.defaultTimelineSegmentViewLimit = defaultTimelineSegmentViewLimit;
    this.hostServices = hostServices;
    this.labService = labService;
    this.workManager = workManager;

    attachPreferenceListeners();
    setAllFromPrefsOrDefaults();
  }

  @Override
  public void start() {
    try {
      if (status.get() != WorkState.Standby) {
        LOG.error("Cannot start fabrication unless in Standby status");
        return;
      }

      // reset progress
      progress.set(0.0);
      LOG.debug("Did reset progress");

      // create work configuration
      var config = new WorkConfiguration()
        .setContentStoragePathPrefix(contentStoragePathPrefix.get())
        .setCraftAheadSeconds(parseIntegerValue(craftAheadSeconds.get(), "fabrication setting for Craft Ahead Seconds"))
        .setDubAheadSeconds(parseIntegerValue(dubAheadSeconds.get(), "fabrication setting for Dub Ahead Seconds"))
        .setMixerLengthSeconds(parseIntegerValue(mixerLengthSeconds.get(), "fabrication setting for Mixer Length Seconds"))
        .setInputMode(inputMode.get())
        .setMacroMode(controlMode.get())
        .setInputTemplateKey(inputTemplateKey.get())
        .setOutputChannels(parseIntegerValue(outputChannels.get(), "fabrication setting for Output Channels"))
        .setOutputFrameRate(parseIntegerValue(outputFrameRate.get(), "fabrication setting for Output Frame Rate"));
      LOG.debug("Did instantiate work configuration");

      var hubAccess = new HubClientAccess()
        .setToken(labService.accessTokenProperty().get());
      LOG.debug("Did instantiate hub client access");

      // start the work with the given configuration
      workManager.setOnProgress((Float progress) -> Platform.runLater(() -> this.progress.set(progress)));
      workManager.setOnStateChange((WorkState state) -> Platform.runLater(() -> status.set(state)));
      LOG.debug("Did bind progress listeners");

      Platform.runLater(() -> workManager.start(config, labService.hubConfigProperty().get(), hubAccess));
      LOG.debug("Did send start signal to work manager");

    } catch (Exception e) {
      LOG.error("Failed to start fabrication", e);
    }
  }

  @Override
  public void cancel() {
    try {
      status.set(WorkState.Cancelled);
      workManager.finish(true);

    } catch (Exception e) {
      LOG.error("Failed to cancel fabrication", e);
    }
  }

  @Override
  public void gotoMacroProgram(Program macroProgram) {
    workManager.gotoMacroProgram(macroProgram);
  }

  @Override
  public Optional<MemeTaxonomy> getMemeTaxonomy() {
    return workManager.getMemeTaxonomy();
  }

  @Override
  public void gotoTaxonomyCategoryMemes(Collection<String> memes) {
    workManager.gotoTaxonomyCategoryMemes(memes);
  }

  @Override
  public void reset() {
    try {
      status.set(WorkState.Standby);
      workManager.reset();

    } catch (Exception e) {
      LOG.error("Failed to reset fabrication", e);
    }
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
  public ObjectProperty<InputMode> inputModeProperty() {
    return inputMode;
  }

  @Override
  public ObjectProperty<ControlMode> controlModeProperty() {
    return controlMode;
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
  public StringProperty mixerLengthSecondsProperty() {
    return mixerLengthSeconds;
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
      LOG.warn("Failed to get segment memes", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentChord> getSegmentChords(Segment segment) {
    try {
      return workManager.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentChord.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.warn("Failed to get segment chords", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentChoice> getSegmentChoices(Segment segment) {
    try {
      return workManager.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentChoice.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.warn("Failed to get segment choices", e);
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
      LOG.warn("Failed to get segment choice arrangements", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks(SegmentChoiceArrangement arrangement) {
    try {
      return workManager.getSegmentManager().readManySubEntitiesOfType(arrangement.getSegmentId(), SegmentChoiceArrangementPick.class)
        .stream().filter(pick -> pick.getSegmentChoiceArrangementId().equals(arrangement.getId())).toList();
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.warn("Failed to get segment choice arrangement picks", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentMessage> getSegmentMessages(Segment segment) {
    try {
      return workManager.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentMessage.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.warn("Failed to get segment messages", e);
      return List.of();
    }
  }

  @Override
  public Collection<SegmentMeta> getSegmentMetas(Segment segment) {
    try {
      return workManager.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentMeta.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.warn("Failed to get segment metas", e);
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
      var from = Objects.nonNull(startIndex) ? startIndex : Math.max(0, workManager.getSegmentManager().lastSegmentId() - viewLimit - 1);
      var to = Math.min(workManager.getSegmentManager().lastSegmentId() - 1, from + viewLimit);
      return workManager.getSegmentManager().readManyFromToOffset(from, to);

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException e) {
      LOG.warn("Failed to get segments", e);
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
        FormatUtils.formatFractionalSuffix((double) (beats % barBeats) / barBeats)))
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
  public void handleDemoPlay(String templateKey) {
    if (status.get() != WorkState.Standby) {
      LOG.error("Cannot play demo unless fabrication is in Standby status");
      return;
    }

    inputTemplateKey.set(templateKey);
    inputMode.set(defaultInputMode);

    start();
  }

  @Override
  public String computeChoiceHash(Segment segment) {
    return workManager.getSegmentManager().getChoiceHash(segment);
  }

  @Override
  public Optional<Segment> getSegmentAtShipOutput() {
    return
      workManager.getShippedToChainMicros().flatMap(chainMicros ->
        workManager.getSegmentManager().readOneAtChainMicros(chainMicros));
  }

  /**
   Attach preference listeners.
   */
  private void attachPreferenceListeners() {
    contentStoragePathPrefix.addListener((o, ov, value) -> prefs.put("contentStoragePathPrefix", value));
    craftAheadSeconds.addListener((o, ov, value) -> prefs.put("craftAheadSeconds", value));
    dubAheadSeconds.addListener((o, ov, value) -> prefs.put("dubAheadSeconds", value));
    mixerLengthSeconds.addListener((o, ov, value) -> prefs.put("mixerLengthSeconds", value));
    inputMode.addListener((o, ov, value) -> prefs.put("inputMode", Objects.nonNull(value) ? value.name() : ""));
    inputTemplateKey.addListener((o, ov, value) -> prefs.put("inputTemplateKey", value));
    controlMode.addListener((o, ov, value) -> prefs.put("macroMode", Objects.nonNull(value) ? value.name() : ""));
    outputChannels.addListener((o, ov, value) -> prefs.put("outputChannels", value));
    outputFrameRate.addListener((o, ov, value) -> prefs.put("outputFrameRate", value));
    timelineSegmentViewLimit.addListener((o, ov, value) -> prefs.put("timelineSegmentViewLimit", value));
  }

  /**
   Set all properties from preferences, else defaults.
   */
  private void setAllFromPrefsOrDefaults() {
    contentStoragePathPrefix.set(prefs.get("contentStoragePathPrefix", defaultContentStoragePathPrefix));
    craftAheadSeconds.set(prefs.get("craftAheadSeconds", Integer.toString(defaultCraftAheadSeconds)));
    dubAheadSeconds.set(prefs.get("dubAheadSeconds", Integer.toString(defaultDubAheadSeconds)));
    mixerLengthSeconds.set(prefs.get("mixerLengthSeconds", Integer.toString(defaultMixerLengthSeconds)));
    inputTemplateKey.set(prefs.get("inputTemplateKey", defaultInputTemplateKey));
    outputChannels.set(prefs.get("outputChannels", Integer.toString(defaultOutputChannels)));
    outputFrameRate.set(prefs.get("outputFrameRate", Double.toString(defaultOutputFrameRate)));
    timelineSegmentViewLimit.set(prefs.get("timelineSegmentViewLimit", Integer.toString(defaultTimelineSegmentViewLimit)));

    try {
      controlMode.set(ControlMode.valueOf(prefs.get("macroMode", defaultControlMode.toString()).toUpperCase(Locale.ROOT)));
    } catch (Exception e) {
      LOG.error("Failed to set macro mode from preferences", e);
      controlMode.set(defaultControlMode);
    }

    try {
      inputMode.set(InputMode.valueOf(prefs.get("inputMode", defaultInputMode.toString()).toUpperCase(Locale.ROOT)));
    } catch (Exception e) {
      LOG.error("Failed to set input mode from preferences", e);
      inputMode.set(defaultInputMode);
    }
  }

  /**
   Format total bars for the given beats.

   @param bars     to format
   @param fraction to format
   @return formatted total bars
   */
  private String formatTotalBars(int bars, String fraction) {
    return String.format("%d%s bar%s", bars, fraction, bars == 1 ? "" : "s");
  }

  /**
   Get the bar beats for the given segment.

   @param segment for which to get the bar beats
   @return bar beats, else empty
   */
  private Optional<Integer> getBarBeats(Segment segment) {
    if (!segmentBarBeats.containsKey(segment.getId())) {
      try {
        var choice = workManager.getSegmentManager().readChoice(segment.getId(), ProgramType.Main);
        if (choice.isEmpty()) {
          LOG.warn("Failed to retrieve main program choice to determine beats for Segment[{}]", segment.getId());
          return Optional.empty();
        }

        var program = workManager.getSourceMaterial().getProgram(choice.get().getProgramId());
        if (program.isEmpty()) {
          LOG.warn("Failed to retrieve main program to determine beats for Segment[{}]", segment.getId());
          return Optional.empty();
        }

        var config = new ProgramConfig(program.get());
        segmentBarBeats.put(segment.getId(), config.getBarBeats());

      } catch (ManagerFatalException | ValueException e) {
        LOG.warn("Failed to format beats duration for Segment[{}]", segment.getId(), e);
        return Optional.empty();
      }
    }
    return Optional.of(segmentBarBeats.get(segment.getId()));
  }

  /**
   Compute program name from program, program sequence, and program sequence binding.

   @param program                to compute name from
   @param programSequence        to compute name from
   @param programSequenceBinding to compute name from
   @return program name
   */
  private String computeProgramName(@Nullable Program program, @Nullable ProgramSequence
    programSequence, @Nullable ProgramSequenceBinding programSequenceBinding) {
    if (Objects.nonNull(program) && Objects.nonNull(programSequence) && Objects.nonNull(programSequenceBinding))
      return String.format("%s (%s)", program.getName(), programSequence.getName());
    else if (Objects.nonNull(program))
      return program.getName();
    else return "Not Loaded";
  }

  /**
   Parse integer value from string.

   @param value             string
   @param sourceDescription of value
   @return integer value
   */
  private int parseIntegerValue(String value, String sourceDescription) {
    try {
      return Integer.parseInt(value);
    } catch (Exception e) {
      throw new RuntimeException(String.format("Failed to parse integer value of '%s' from %s", value, sourceDescription));
    }
  }

  /**
   Compute default path prefix for a particular category.

   @param category of path
   @return path prefix
   */
  @SuppressWarnings("SameParameterValue")
  private static String computeDefaultPathPrefix(String category) {
    return defaultPathPrefix + category + File.separator;
  }
}
