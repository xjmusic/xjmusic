// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services.impl;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.ProjectService;
import io.xj.hub.ProgramConfig;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.meme.MemeTaxonomy;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.util.ValueException;
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
import io.xj.nexus.project.ProjectState;
import io.xj.nexus.util.FormatUtils;
import io.xj.nexus.work.WorkConfiguration;
import io.xj.nexus.work.WorkManager;
import io.xj.nexus.work.WorkState;
import jakarta.annotation.Nullable;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

@Service
public class FabricationServiceImpl implements FabricationService {
  private static final Logger LOG = LoggerFactory.getLogger(FabricationServiceImpl.class);
  private final Preferences prefs = Preferences.userNodeForPackage(FabricationServiceImpl.class);
  private final static String BUTTON_TEXT_START = "Start";
  private final static String BUTTON_TEXT_STOP = "Stop";
  private final static String BUTTON_TEXT_RESET = "Reset";
  private final HostServices hostServices;
  private final int defaultTimelineSegmentViewLimit;
  private final int defaultCraftAheadSeconds;
  private final int defaultDubAheadSeconds;
  private final int defaultMixerLengthSeconds;
  private final int defaultOutputChannels;
  private final int defaultOutputFrameRate;
  private final ControlMode defaultControlMode;
  private final WorkManager workManager;
  private final LabService labService;
  private final Map<Integer, Integer> segmentBarBeats = new ConcurrentHashMap<>();
  private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
  private final ObjectProperty<WorkState> state = new SimpleObjectProperty<>(WorkState.Standby);
  private final ObservableStringValue stateText = Bindings.createStringBinding(
    () -> switch (state.get()) {
      case Standby -> "Ready";
      case Starting -> "Starting";
      case PreparingAudio -> String.format("Preparing audio (%.02f%%)", progress.get() * 100);
      case PreparedAudio -> "Prepared audio";
      case Initializing -> "Initializing";
      case Active -> "Active";
      case Done -> "Done";
      case Cancelled -> "Cancelled";
      case Failed -> "Failed";
    },
    state,
    progress);
  private final BooleanBinding stateActive =
    Bindings.createBooleanBinding(() -> state.get() == WorkState.Active, state);
  private final BooleanBinding stateLoading =
    Bindings.createBooleanBinding(() -> state.get() == WorkState.PreparingAudio || state.get() == WorkState.PreparedAudio, state);
  private final ObjectProperty<Template> inputTemplate = new SimpleObjectProperty<>();
  private final ObjectProperty<ControlMode> controlMode = new SimpleObjectProperty<>();
  private final StringProperty craftAheadSeconds = new SimpleStringProperty();
  private final StringProperty dubAheadSeconds = new SimpleStringProperty();
  private final StringProperty mixerLengthSeconds = new SimpleStringProperty();
  private final StringProperty outputFrameRate = new SimpleStringProperty();
  private final StringProperty outputChannels = new SimpleStringProperty();
  private final StringProperty timelineSegmentViewLimit = new SimpleStringProperty();
  private final BooleanProperty followPlayback = new SimpleBooleanProperty(true);
  private final ObservableSet<String> overrideMemes = FXCollections.observableSet();
  private final ObjectProperty<UUID> overrideMacroProgramId = new SimpleObjectProperty<>();
  private final ObservableValue<String> mainActionButtonText = Bindings.createStringBinding(() ->
    switch (state.get()) {
      case Standby -> BUTTON_TEXT_START;
      case Starting, PreparingAudio, PreparedAudio, Initializing, Active -> BUTTON_TEXT_STOP;
      case Cancelled, Failed, Done -> BUTTON_TEXT_RESET;
    }, state);

  public FabricationServiceImpl(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${craft.ahead.seconds}") int defaultCraftAheadSeconds,
    @Value("${dub.ahead.seconds}") int defaultDubAheadSeconds,
    @Value("${mixer.length.seconds}") int defaultMixerLengthSeconds,
    @Value("${gui.timeline.max.segments}") int defaultTimelineSegmentViewLimit,
    @Value("${output.channels}") int defaultOutputChannels,
    @Value("${output.frame.rate}") int defaultOutputFrameRate,
    @Value("${macro.mode}") String defaultMacroMode,
    LabService labService,
    ProjectService projectService,
    WorkManager workManager
  ) {
    this.defaultCraftAheadSeconds = defaultCraftAheadSeconds;
    this.defaultDubAheadSeconds = defaultDubAheadSeconds;
    this.defaultMixerLengthSeconds = defaultMixerLengthSeconds;
    this.defaultControlMode = ControlMode.valueOf(defaultMacroMode.toUpperCase(Locale.ROOT));
    this.defaultOutputChannels = defaultOutputChannels;
    this.defaultOutputFrameRate = defaultOutputFrameRate;
    this.defaultTimelineSegmentViewLimit = defaultTimelineSegmentViewLimit;
    this.hostServices = hostServices;
    this.labService = labService;
    this.workManager = workManager;

    projectService.stateProperty().addListener((o, ov, value) -> {
      if (value == ProjectState.Ready) {
        inputTemplate.set(projectService.getContent().getTemplates().stream().findFirst().orElse(null));
      }
    });

    attachPreferenceListeners();
    setAllFromPreferencesOrDefaults();
  }

  @Override
  public void start() {
    try {
      if (state.get() != WorkState.Standby) {
        LOG.error("Cannot start fabrication unless in Standby status");
        return;
      }

      // reset progress
      progress.set(0.0);
      LOG.debug("Did reset progress");

      // create work configuration
      var config = new WorkConfiguration()
        .setCraftAheadSeconds(parseIntegerValue(craftAheadSeconds.get(), "fabrication setting for Craft Ahead Seconds"))
        .setDubAheadSeconds(parseIntegerValue(dubAheadSeconds.get(), "fabrication setting for Dub Ahead Seconds"))
        .setMixerLengthSeconds(parseIntegerValue(mixerLengthSeconds.get(), "fabrication setting for Mixer Length Seconds"))
        .setMacroMode(controlMode.get())
        .setInputTemplate(inputTemplate.get())
        .setOutputChannels(parseIntegerValue(outputChannels.get(), "fabrication setting for Output Channels"))
        .setOutputFrameRate(parseIntegerValue(outputFrameRate.get(), "fabrication setting for Output Frame Rate"));
      LOG.debug("Did instantiate work configuration");

      var hubAccess = new HubClientAccess()
        .setToken(labService.accessTokenProperty().get());
      LOG.debug("Did instantiate hub client access");

      // start the work with the given configuration
      workManager.setOnProgress((Float progress) -> Platform.runLater(() -> this.progress.set(progress)));
      workManager.setOnStateChange((WorkState state) -> Platform.runLater(() -> this.state.set(state)));
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
      state.set(WorkState.Cancelled);
      workManager.finish(true);

    } catch (Exception e) {
      LOG.error("Failed to cancel fabrication", e);
    }
  }

  @Override
  public void doOverrideMacro(Program macroProgram) {
    workManager.doOverrideMacro(macroProgram);
    overrideMacroProgramId.set(macroProgram.getId());
  }

  @Override
  public void resetOverrideMacro() {
    workManager.resetOverrideMacro();
    overrideMacroProgramId.set(null);
  }

  @Override
  public Optional<MemeTaxonomy> getMemeTaxonomy() {
    return workManager.getMemeTaxonomy();
  }

  @Override
  public void doOverrideMemes(Collection<String> memes) {
    workManager.doOverrideMemes(memes);
    overrideMemes.clear();
    overrideMemes.addAll(memes.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new)));
  }

  @Override
  public void resetOverrideMemes() {
    workManager.resetOverrideMemes();
    overrideMemes.clear();
  }

  @Override
  public boolean getAndResetDidOverride() {
    return workManager.getAndResetDidOverride();
  }

  @Override
  public void resetSettingsToDefaults() {
    craftAheadSeconds.set(String.valueOf(defaultCraftAheadSeconds));
    dubAheadSeconds.set(String.valueOf(defaultDubAheadSeconds));
    mixerLengthSeconds.set(String.valueOf(defaultMixerLengthSeconds));
    controlMode.set(defaultControlMode);
    outputChannels.set(String.valueOf(defaultOutputChannels));
    outputFrameRate.set(String.valueOf(defaultOutputFrameRate));
  }

  @Override
  public void reset() {
    try {
      state.set(WorkState.Standby);
      workManager.reset();
      overrideMacroProgramId.set(null);
      overrideMemes.clear();

    } catch (Exception e) {
      LOG.error("Failed to reset fabrication", e);
    }
  }

  @Override
  public List<Program> getAllMacroPrograms() {
    return workManager.getSourceMaterial().getProgramsByType(ProgramType.Macro).stream()
      .sorted(Comparator.comparing(Program::getName))
      .toList();
  }

  @Override
  public ObjectProperty<WorkState> stateProperty() {
    return state;
  }

  @Override
  public ObservableStringValue stateTextProperty() {
    return stateText;
  }

  @Override
  public ObjectProperty<Template> inputTemplateProperty() {
    return inputTemplate;
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
    return workManager.getEntityStore().readManySubEntitiesOfType(segment.getId(), SegmentMeme.class);
  }

  @Override
  public Collection<SegmentChord> getSegmentChords(Segment segment) {
    return workManager.getEntityStore().readManySubEntitiesOfType(segment.getId(), SegmentChord.class);
  }

  @Override
  public Collection<SegmentChoice> getSegmentChoices(Segment segment) {
    return workManager.getEntityStore().readManySubEntitiesOfType(segment.getId(), SegmentChoice.class);
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
    return workManager.getEntityStore().readManySubEntitiesOfType(choice.getSegmentId(), SegmentChoiceArrangement.class)
      .stream().filter(arrangement -> arrangement.getSegmentChoiceId().equals(choice.getId())).toList();
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks(SegmentChoiceArrangement arrangement) {
    return workManager.getEntityStore().readManySubEntitiesOfType(arrangement.getSegmentId(), SegmentChoiceArrangementPick.class)
      .stream().filter(pick -> pick.getSegmentChoiceArrangementId().equals(arrangement.getId())).toList();
  }

  @Override
  public Collection<SegmentMessage> getSegmentMessages(Segment segment) {
    return workManager.getEntityStore().readManySubEntitiesOfType(segment.getId(), SegmentMessage.class);
  }

  @Override
  public Collection<SegmentMeta> getSegmentMetas(Segment segment) {
    return workManager.getEntityStore().readManySubEntitiesOfType(segment.getId(), SegmentMeta.class);
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
    var viewLimit = Integer.parseInt(timelineSegmentViewLimit.getValue());
    var from = Objects.nonNull(startIndex) ? startIndex : Math.max(0, workManager.getEntityStore().readLastSegmentId() - viewLimit - 1);
    var to = Math.min(workManager.getEntityStore().readLastSegmentId() - 1, from + viewLimit);
    return workManager.getEntityStore().readSegmentsFromToOffset(from, to);
  }

  @Override
  public Boolean isEmpty() {
    return workManager.getEntityStore().isEmpty();
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
  public ObservableSet<String> overrideMemesProperty() {
    return overrideMemes;
  }

  @Override
  public ObjectProperty<UUID> overrideMacroProgramIdProperty() {
    return overrideMacroProgramId;
  }

  @Override
  public ObservableDoubleValue progressProperty() {
    return progress;
  }

  @Override
  public BooleanBinding isStateActiveProperty() {
    return stateActive;
  }

  @Override
  public BooleanBinding isStateLoadingProperty() {
    return stateLoading;
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
    switch (state.get()) {
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
  public String computeChoiceHash(Segment segment) {
    return workManager.getEntityStore().readChoiceHash(segment);
  }

  @Override
  public Optional<Segment> getSegmentAtShipOutput() {
    return
      workManager.getShippedToChainMicros().flatMap(chainMicros ->
        workManager.getEntityStore().readSegmentAtChainMicros(chainMicros));
  }

  /**
   Attach preference listeners.
   */
  private void attachPreferenceListeners() {
    craftAheadSeconds.addListener((o, ov, value) -> prefs.put("craftAheadSeconds", value));
    dubAheadSeconds.addListener((o, ov, value) -> prefs.put("dubAheadSeconds", value));
    mixerLengthSeconds.addListener((o, ov, value) -> prefs.put("mixerLengthSeconds", value));
    controlMode.addListener((o, ov, value) -> prefs.put("macroMode", Objects.nonNull(value) ? value.name() : ""));
    outputChannels.addListener((o, ov, value) -> prefs.put("outputChannels", value));
    outputFrameRate.addListener((o, ov, value) -> prefs.put("outputFrameRate", value));
    timelineSegmentViewLimit.addListener((o, ov, value) -> prefs.put("timelineSegmentViewLimit", value));
  }

  /**
   Set all properties from preferences, else defaults.
   */
  private void setAllFromPreferencesOrDefaults() {
    craftAheadSeconds.set(prefs.get("craftAheadSeconds", Integer.toString(defaultCraftAheadSeconds)));
    dubAheadSeconds.set(prefs.get("dubAheadSeconds", Integer.toString(defaultDubAheadSeconds)));
    mixerLengthSeconds.set(prefs.get("mixerLengthSeconds", Integer.toString(defaultMixerLengthSeconds)));
    outputChannels.set(prefs.get("outputChannels", Integer.toString(defaultOutputChannels)));
    outputFrameRate.set(prefs.get("outputFrameRate", Double.toString(defaultOutputFrameRate)));
    timelineSegmentViewLimit.set(prefs.get("timelineSegmentViewLimit", Integer.toString(defaultTimelineSegmentViewLimit)));

    try {
      controlMode.set(ControlMode.valueOf(prefs.get("macroMode", defaultControlMode.toString()).toUpperCase(Locale.ROOT)));
    } catch (Exception e) {
      LOG.error("Failed to set control mode from preferences", e);
      controlMode.set(defaultControlMode);
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
        var choice = workManager.getEntityStore().readChoice(segment.getId(), ProgramType.Main);
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

      } catch (ValueException e) {
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
}