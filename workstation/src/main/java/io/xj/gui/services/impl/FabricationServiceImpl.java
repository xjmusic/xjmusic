// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services.impl;

import io.xj.engine.fabricator.ControlMode;
import io.xj.engine.util.FormatUtils;
import io.xj.engine.work.FabricationManager;
import io.xj.engine.work.FabricationSettings;
import io.xj.engine.work.FabricationState;
import io.xj.gui.project.ProjectState;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.model.ProgramConfig;
import io.xj.model.TemplateConfig;
import io.xj.model.enums.ProgramType;
import io.xj.model.meme.MemeTaxonomy;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.pojos.Program;
import io.xj.model.pojos.ProgramSequence;
import io.xj.model.pojos.ProgramSequenceBinding;
import io.xj.model.pojos.ProgramVoice;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangement;
import io.xj.model.pojos.SegmentChoiceArrangementPick;
import io.xj.model.pojos.SegmentChord;
import io.xj.model.pojos.SegmentMeme;
import io.xj.model.pojos.SegmentMessage;
import io.xj.model.pojos.SegmentMeta;
import io.xj.model.pojos.Template;
import io.xj.model.util.StringUtils;
import io.xj.model.util.ValueException;
import jakarta.annotation.Nullable;
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
  private final double defaultIntensityOverride;
  private final int defaultTimelineSegmentViewLimit;
  private final int defaultCraftAheadSeconds;
  private final int defaultDubAheadSeconds;
  private final int defaultMixerLengthSeconds;
  private final ControlMode defaultControlMode;
  private final ProjectService projectService;
  private final FabricationManager fabricationManager;
  private final Map<Integer, Integer> segmentBarBeats = new ConcurrentHashMap<>();
  private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
  private final StringProperty progressLabel = new SimpleStringProperty();
  private final ObjectProperty<FabricationState> state = new SimpleObjectProperty<>(FabricationState.Standby);
  private final ObservableStringValue stateText = Bindings.createStringBinding(
    () -> switch (state.get()) {
      case Standby -> "Ready";
      case Starting -> "Starting";
      case PreparingAudio -> progressLabel.get();
      case PreparedAudio -> "Prepared audio";
      case Initializing -> "Initializing";
      case Active -> "Active";
      case Done -> "Done";
      case Cancelled -> "Cancelled";
      case Failed -> "Failed";
    },
    state,
    progress,
    progressLabel);
  private final BooleanBinding stateActive =
    Bindings.createBooleanBinding(() -> state.get() == FabricationState.Active, state);
  private final BooleanBinding stateLoading =
    Bindings.createBooleanBinding(() -> state.get() == FabricationState.PreparingAudio || state.get() == FabricationState.PreparedAudio, state);
  private final ObjectProperty<Template> inputTemplate = new SimpleObjectProperty<>();
  private final ObjectProperty<TemplateConfig> inputTemplateConfig = new SimpleObjectProperty<>();
  private final ObjectProperty<ControlMode> controlMode = new SimpleObjectProperty<>();
  private final StringProperty craftAheadSeconds = new SimpleStringProperty();
  private final StringProperty dubAheadSeconds = new SimpleStringProperty();
  private final StringProperty mixerLengthSeconds = new SimpleStringProperty();
  private final StringProperty timelineSegmentViewLimit = new SimpleStringProperty();
  private final BooleanProperty followPlayback = new SimpleBooleanProperty(true);
  private final ObservableSet<String> overrideMemes = FXCollections.observableSet();
  private final ObjectProperty<UUID> overrideMacroProgramId = new SimpleObjectProperty<>();
  private final DoubleProperty intensityOverride = new SimpleDoubleProperty(1.0);
  private final BooleanProperty intensityOverrideActive = new SimpleBooleanProperty(false);
  private final ObservableValue<String> mainActionButtonText = Bindings.createStringBinding(() ->
    switch (state.get()) {
      case Standby -> BUTTON_TEXT_START;
      case Starting, PreparingAudio, PreparedAudio, Initializing, Active -> BUTTON_TEXT_STOP;
      case Cancelled, Failed, Done -> BUTTON_TEXT_RESET;
    }, state);

  public FabricationServiceImpl(
    @Value("${fabrication.defaultCraftAheadSeconds}") int defaultCraftAheadSeconds,
    @Value("${fabrication.defaultDubAheadSeconds}") int defaultDubAheadSeconds,
    @Value("${fabrication.defaultMixerLengthSeconds}") int defaultMixerLengthSeconds,
    @Value("${view.timelineMaxSegments}") int defaultTimelineSegmentViewLimit,
    @Value("${fabrication.defaultMacroMode}") String defaultMacroMode,
    @Value("${fabrication.baseIntensity}") double defaultIntensityOverride,
    ProjectService projectService,
    FabricationManager fabricationManager
  ) {
    this.defaultCraftAheadSeconds = defaultCraftAheadSeconds;
    this.defaultDubAheadSeconds = defaultDubAheadSeconds;
    this.defaultMixerLengthSeconds = defaultMixerLengthSeconds;
    this.defaultControlMode = ControlMode.valueOf(defaultMacroMode.toUpperCase(Locale.ROOT));
    this.defaultTimelineSegmentViewLimit = defaultTimelineSegmentViewLimit;
    this.defaultIntensityOverride = defaultIntensityOverride;
    this.projectService = projectService;
    this.fabricationManager = fabricationManager;

    projectService.stateProperty().addListener((o, ov, value) -> {
      if (value == ProjectState.Ready) {
        inputTemplate.set(projectService.getContent().getTemplates().stream().findFirst().orElse(null));
      }
    });

    intensityOverrideActive.addListener((o, ov, value) -> {
      if (value) {
        fabricationManager.setIntensityOverride(intensityOverride.get());
      } else {
        fabricationManager.setIntensityOverride(null);
      }
    });

    intensityOverride.addListener((o, ov, value) -> {
      if (intensityOverrideActive.get()) {
        fabricationManager.setIntensityOverride(intensityOverride.get());
      } else {
        fabricationManager.setIntensityOverride(null);
      }
    });

    inputTemplate.addListener((o, ov, value) -> {
      if (Objects.nonNull(value)) {
        try {
          inputTemplateConfig.set(new TemplateConfig(value.getConfig()));
        } catch (ValueException e) {
          LOG.warn("Failed to set TemplateConfig from Template[{}] because {}", value.getId(), e.getMessage());
          inputTemplateConfig.set(null);
        }
      }
    });

    attachPreferenceListeners();
    setAllFromPreferencesOrDefaults();
  }

  @Override
  public void start() {
    try {
      if (state.get() != FabricationState.Standby) {
        LOG.error("Cannot start fabrication unless in Standby status");
        return;
      }

      // default intensity override
      intensityOverrideActive.set(false);
      intensityOverride.set(defaultIntensityOverride);

      // reset progress
      progress.set(0.0);
      progressLabel.set("");
      LOG.debug("Did reset progress");

      // create work configuration
      var config = new FabricationSettings()
        .setCraftAheadSeconds(parseIntegerValue(craftAheadSeconds.get(), "fabrication setting for Craft Ahead Seconds"))
        .setDubAheadSeconds(parseIntegerValue(dubAheadSeconds.get(), "fabrication setting for Dub Ahead Seconds"))
        .setMixerLengthSeconds(parseIntegerValue(mixerLengthSeconds.get(), "fabrication setting for Mixer Length Seconds"))
        .setMacroMode(controlMode.get())
        .setInputTemplate(inputTemplate.get())
        .setOutputChannels(parseIntegerValue(projectService.outputChannelsProperty().get(), "fabrication setting for Output Channels"))
        .setOutputFrameRate(parseIntegerValue(projectService.outputFrameRateProperty().get(), "fabrication setting for Output Frame Rate"));
      LOG.debug("Did instantiate work configuration");

      // Get the content for this template
      var content = projectService.getContent(inputTemplate.get());

      // If memes/macro already engaged at fabrication start (which is always true in a manual control mode),
      // the first segment should be governed by that selection https://github.com/xjmusic/xjmusic/issues/201
      switch (controlMode.get()) {
        case MACRO -> content.getProgramsOfType(ProgramType.Macro).stream()
          .min(Comparator.comparing(Program::getName))
          .ifPresent(macro -> overrideMacroProgramId.set(macro.getId()));
        case TAXONOMY -> {
          var memeTaxonomy = new TemplateConfig(inputTemplate.get()).getMemeTaxonomy();
          var memes = memeTaxonomy.getCategories().stream()
            .map(category -> category.getMemes().stream().findFirst().orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
          if (!memes.isEmpty()) {
            overrideMemes.clear();
            overrideMemes.addAll(memes);
          }
        }
      }

      // start the work with the given configuration
      fabricationManager.setOnProgress((Float progress) -> Platform.runLater(() -> this.progress.set(progress)));
      fabricationManager.setOnProgressLabel((String label) -> Platform.runLater(() -> this.progressLabel.set(label)));
      fabricationManager.setOnStateChange((FabricationState state) -> Platform.runLater(() -> this.state.set(state)));
      LOG.debug("Did bind progress listeners");

      Platform.runLater(() -> fabricationManager.start(content, config));
      LOG.debug("Did send start signal to work manager");

    } catch (Exception e) {
      LOG.error("Failed to start fabrication! {}\n{}", e.getMessage(), StringUtils.formatStackTrace(e));
    }
  }

  @Override
  public void cancel() {
    if (state.get() == FabricationState.Standby) {
      LOG.debug("Will not cancel fabrication unless in Active status");
      return;
    }
    try {
      state.set(FabricationState.Cancelled);
      fabricationManager.finish(true);

    } catch (Exception e) {
      LOG.error("Failed to cancel fabrication", e);
    }
  }

  @Override
  public void doOverrideMacro(Program macroProgram) {
    fabricationManager.doOverrideMacro(macroProgram);
    overrideMacroProgramId.set(macroProgram.getId());
  }

  @Override
  public Optional<MemeTaxonomy> getMemeTaxonomy() {
    return fabricationManager.getMemeTaxonomy();
  }

  @Override
  public void doOverrideMemes(Collection<String> memes) {
    fabricationManager.doOverrideMemes(memes);
    overrideMemes.clear();
    overrideMemes.addAll(memes.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new)));
  }

  @Override
  public boolean getAndResetDidOverride() {
    return fabricationManager.getAndResetDidOverride();
  }

  @Override
  public void resetSettingsToDefaults() {
    craftAheadSeconds.set(String.valueOf(defaultCraftAheadSeconds));
    dubAheadSeconds.set(String.valueOf(defaultDubAheadSeconds));
    mixerLengthSeconds.set(String.valueOf(defaultMixerLengthSeconds));
    controlMode.set(defaultControlMode);
  }

  @Override
  public void reset() {
    try {
      state.set(FabricationState.Standby);
      fabricationManager.reset();
      overrideMacroProgramId.set(null);
      overrideMemes.clear();

    } catch (Exception e) {
      LOG.error("Failed to reset fabrication", e);
    }
  }

  @Override
  public List<Program> getAllMacroPrograms() {
    return fabricationManager.getAllMacroPrograms();
  }

  @Override
  public ObjectProperty<FabricationState> stateProperty() {
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
  public ObjectProperty<TemplateConfig> getTemplateConfig() {
    return inputTemplateConfig;
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
  public StringProperty timelineSegmentViewLimitProperty() {
    return timelineSegmentViewLimit;
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes(Segment segment) {
    return fabricationManager.getEntityStore().readAll(segment.getId(), SegmentMeme.class);
  }

  @Override
  public Collection<SegmentChord> getSegmentChords(Segment segment) {
    return fabricationManager.getEntityStore().readAll(segment.getId(), SegmentChord.class);
  }

  @Override
  public Collection<SegmentChoice> getSegmentChoices(Segment segment) {
    return fabricationManager.getEntityStore().readAll(segment.getId(), SegmentChoice.class);
  }

  @Override
  public Optional<Program> getProgram(UUID programId) {
    return fabricationManager.getSourceMaterial().getProgram(programId);
  }

  @Override
  public Optional<ProgramVoice> getProgramVoice(UUID programVoiceId) {
    return fabricationManager.getSourceMaterial().getProgramVoice(programVoiceId);
  }

  @Override
  public Optional<ProgramSequence> getProgramSequence(UUID programSequenceId) {
    return fabricationManager.getSourceMaterial().getProgramSequence(programSequenceId);
  }

  @Override
  public Optional<ProgramSequenceBinding> getProgramSequenceBinding(UUID programSequenceBindingId) {
    return fabricationManager.getSourceMaterial().getProgramSequenceBinding(programSequenceBindingId);
  }

  @Override
  public Optional<Instrument> getInstrument(UUID instrumentId) {
    return fabricationManager.getSourceMaterial().getInstrument(instrumentId);
  }

  @Override
  public Optional<InstrumentAudio> getInstrumentAudio(UUID instrumentAudioId) {
    return fabricationManager.getSourceMaterial().getInstrumentAudio(instrumentAudioId);
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice choice) {
    return fabricationManager.getEntityStore().readAll(choice.getSegmentId(), SegmentChoiceArrangement.class)
      .stream().filter(arrangement -> arrangement.getSegmentChoiceId().equals(choice.getId())).toList();
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks(SegmentChoiceArrangement arrangement) {
    return fabricationManager.getEntityStore().readAll(arrangement.getSegmentId(), SegmentChoiceArrangementPick.class)
      .stream().filter(pick -> pick.getSegmentChoiceArrangementId().equals(arrangement.getId())).toList();
  }

  @Override
  public Collection<SegmentMessage> getSegmentMessages(Segment segment) {
    return fabricationManager.getEntityStore().readAll(segment.getId(), SegmentMessage.class);
  }

  @Override
  public Collection<SegmentMeta> getSegmentMetas(Segment segment) {
    return fabricationManager.getEntityStore().readAll(segment.getId(), SegmentMeta.class);
  }

  @Override
  public List<Segment> getSegments(@Nullable Integer startIndex) {
    var viewLimit = parseIntegerValue(timelineSegmentViewLimit.getValue(), "Timeline Segment View Limit");
    var from = Objects.nonNull(startIndex) ? startIndex : Math.max(0, fabricationManager.getEntityStore().readLastSegmentId() - viewLimit - 1);
    var to = Math.min(fabricationManager.getEntityStore().readLastSegmentId(), from + viewLimit);
    return fabricationManager.getEntityStore().readSegmentsFromToOffset(from, to);
  }

  @Override
  public Boolean isEmpty() {
    return fabricationManager.getEntityStore().isEmpty();
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
    return fabricationManager.getShippedToChainMicros();
  }

  @Override
  public Optional<Long> getDubbedToChainMicros() {
    return fabricationManager.getDubbedToChainMicros();
  }

  @Override
  public Optional<Long> getCraftedToChainMicros() {
    return fabricationManager.getCraftedToChainMicros();
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
    return fabricationManager.getEntityStore().readChoiceHash(segment);
  }

  @Override
  public Optional<Segment> getSegmentAtShipOutput() {
    return
      fabricationManager.getShippedToChainMicros().flatMap(chainMicros ->
        fabricationManager.getEntityStore().readSegmentAtChainMicros(chainMicros));
  }

  @Override
  public DoubleProperty intensityOverrideProperty() {
    return intensityOverride;
  }

  @Override
  public BooleanProperty intensityOverrideActiveProperty() {
    return intensityOverrideActive;
  }

  /**
   Attach preference listeners.
   */
  private void attachPreferenceListeners() {
    craftAheadSeconds.addListener((o, ov, value) -> prefs.put("craftAheadSeconds", value));
    dubAheadSeconds.addListener((o, ov, value) -> prefs.put("dubAheadSeconds", value));
    mixerLengthSeconds.addListener((o, ov, value) -> prefs.put("mixerLengthSeconds", value));
    controlMode.addListener((o, ov, value) -> prefs.put("macroMode", Objects.nonNull(value) ? value.name() : ""));
    timelineSegmentViewLimit.addListener((o, ov, value) -> prefs.put("timelineSegmentViewLimit", value));
  }

  /**
   Set all properties from preferences, else defaults.
   */
  private void setAllFromPreferencesOrDefaults() {
    craftAheadSeconds.set(prefs.get("craftAheadSeconds", Integer.toString(defaultCraftAheadSeconds)));
    dubAheadSeconds.set(prefs.get("dubAheadSeconds", Integer.toString(defaultDubAheadSeconds)));
    mixerLengthSeconds.set(prefs.get("mixerLengthSeconds", Integer.toString(defaultMixerLengthSeconds)));
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
        var choice = fabricationManager.getEntityStore().readChoice(segment.getId(), ProgramType.Main);
        if (choice.isEmpty()) {
          LOG.warn("Failed to retrieve main program choice to determine beats for Segment[{}]", segment.getId());
          return Optional.empty();
        }

        var program = fabricationManager.getSourceMaterial().getProgram(choice.get().getProgramId());
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
   Parse integer value from string.

   @param value             string
   @param sourceDescription of value
   @return integer value
   */
  private int parseIntegerValue(String value, String sourceDescription) {
    try {
      return (int) Double.parseDouble(value);
    } catch (Exception e) {
      throw new RuntimeException(String.format("Failed to parse integer value of '%s' from %s", value, sourceDescription));
    }
  }

}
