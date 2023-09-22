// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.ProgramConfig;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.*;
import io.xj.hub.util.ValueException;
import io.xj.lib.util.FormatUtils;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.hub_client.HubClient;
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
import java.util.concurrent.ConcurrentHashMap;

@org.springframework.stereotype.Service
public class FabricationServiceImpl extends Service<Boolean> implements FabricationService {
  static final Logger LOG = LoggerFactory.getLogger(FabricationServiceImpl.class);
  final static String BUTTON_TEXT_START = "Start";
  final static String BUTTON_TEXT_STOP = "Stop";
  final static String BUTTON_TEXT_RESET = "Reset";
  final HostServices hostServices;
  private final HubClient hubClient;
  final WorkFactory workFactory;
  final LabService labService;
  final Map<Integer, Integer> segmentBarBeats = new ConcurrentHashMap<>();
  final ObjectProperty<FabricationStatus> status = new SimpleObjectProperty<>(FabricationStatus.Standby);
  final StringProperty inputTemplateKey = new SimpleStringProperty();
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
  final IntegerProperty timelineSegmentViewLimitInteger = new SimpleIntegerProperty();
  final BooleanProperty followPlayback = new SimpleBooleanProperty(true);
  final ObservableBooleanValue outputModeSync = Bindings.createBooleanBinding(() ->
    outputMode.get().isSync(), outputMode);
  final ObservableBooleanValue outputModeFile = Bindings.createBooleanBinding(() ->
    outputMode.get() == OutputMode.FILE, outputMode);
  final ObservableBooleanValue statusActive =
    Bindings.createBooleanBinding(() -> status.get() == FabricationStatus.Active, status);
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
    HubClient hubClient,
    WorkFactory workFactory,
    LabService labService
  ) {
    this.hostServices = hostServices;
    this.hubClient = hubClient;
    this.workFactory = workFactory;
    this.labService = labService;
    craftAheadSeconds.set(Integer.toString(defaultCraftAheadSeconds));
    dubAheadSeconds.set(Integer.toString(defaultDubAheadSeconds));
    shipAheadSeconds.set(Integer.toString(defaultShipAheadSeconds));
    inputMode.set(InputMode.PRODUCTION);
    inputTemplateKey.set(defaultInputTemplateKey);
    outputChannels.set(Integer.toString(defaultOutputChannels));
    outputFileMode.set(OutputFileMode.valueOf(defaultOutputFileMode.toUpperCase(Locale.ROOT)));
    outputFrameRate.set(Double.toString(defaultOutputFrameRate));
    outputMode.set(OutputMode.valueOf(defaultOutputMode.toUpperCase(Locale.ROOT)));
    outputPathPrefix.set(System.getProperty("user.home") + File.separator);
    outputSeconds.set(Integer.toString(defaultOutputSeconds));
    timelineSegmentViewLimit.set(Integer.toString(defaultTimelineSegmentViewLimit));
    timelineSegmentViewLimitInteger.bind(Bindings.createIntegerBinding(() -> Integer.parseInt(timelineSegmentViewLimit.get()), timelineSegmentViewLimit));
    setOnCancelled((WorkerStateEvent ignored) -> status.set(FabricationStatus.Cancelled));
    setOnFailed((WorkerStateEvent ignored) -> status.set(FabricationStatus.Failed));
    setOnReady((WorkerStateEvent ignored) -> status.set(FabricationStatus.Standby));
    setOnRunning((WorkerStateEvent ignored) -> status.set(FabricationStatus.Active));
    setOnScheduled((WorkerStateEvent ignored) -> status.set(FabricationStatus.Starting));
    setOnSucceeded((WorkerStateEvent ignored) -> status.set(FabricationStatus.Done));
  }

  protected Task<Boolean> createTask() {
    return new Task<>() {
      protected Boolean call() {
        var configuration = new WorkConfiguration()
          .setInputMode(inputMode.get())
          .setInputTemplateKey(inputTemplateKey.get())
          .setOutputFileMode(outputFileMode.get())
          .setOutputMode(outputMode.get())
          .setOutputPathPrefix(outputPathPrefix.get())
          .setOutputSeconds(Integer.parseInt(outputSeconds.get()))
          .setCraftAheadSeconds(Integer.parseInt(craftAheadSeconds.get()))
          .setDubAheadSeconds(Integer.parseInt(dubAheadSeconds.get()))
          .setShipAheadSeconds(Integer.parseInt(shipAheadSeconds.get()))
          .setOutputFrameRate(Double.parseDouble(outputFrameRate.get()))
          .setOutputChannels(Integer.parseInt(outputChannels.get()));
        hubClient.setBaseUrl(labService.baseUrlProperty().getValue());
        hubClient.setAccessToken(labService.accessTokenProperty().getValue());
        return workFactory.start(
          configuration,
          (Double ratio) -> updateProgress(ratio, 1.0),
          () -> {
            // no op; the WorkFactory start method blocks, then we rely on the JavaFX Service hooks
          });
      }
    };
  }

  public ObjectProperty<FabricationStatus> statusProperty() {
    return status;
  }

  public StringProperty inputTemplateKeyProperty() {
    return inputTemplateKey;
  }

  public StringProperty outputPathPrefixProperty() {
    return outputPathPrefix;
  }

  public ObjectProperty<InputMode> inputModeProperty() {
    return inputMode;
  }

  public ObjectProperty<OutputFileMode> outputFileModeProperty() {
    return outputFileMode;
  }

  public ObjectProperty<OutputMode> outputModeProperty() {
    return outputMode;
  }

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

  public WorkFactory getWorkFactory() {
    return workFactory;
  }

  public Collection<SegmentMeme> getSegmentMemes(Segment segment) {
    try {
      return workFactory.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentMeme.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment memes", e);
      return List.of();
    }
  }

  public Collection<SegmentChord> getSegmentChords(Segment segment) {
    try {
      return workFactory.getSegmentManager().readManySubEntitiesOfType(segment.getId(), SegmentChord.class);
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.error("Failed to get segment chords", e);
      return List.of();
    }
  }

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
      var from = Objects.nonNull(startIndex) ? startIndex : Math.max(0, workFactory.getSegmentManager().size() - timelineSegmentViewLimitInteger.get() - 1);
      var to = Math.min(workFactory.getSegmentManager().size() - 1, from + timelineSegmentViewLimitInteger.get());
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
}
