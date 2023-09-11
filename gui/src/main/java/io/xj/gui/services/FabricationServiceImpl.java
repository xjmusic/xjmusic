// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.tables.pojos.*;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.nexus.work.WorkConfiguration;
import io.xj.nexus.work.WorkFactory;
import jakarta.annotation.Nullable;
import javafx.application.HostServices;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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

@org.springframework.stereotype.Service
public class FabricationServiceImpl extends Service<Boolean> implements FabricationService {
  static final Logger LOG = LoggerFactory.getLogger(FabricationServiceImpl.class);
  final HostServices hostServices;
  final Integer defaultBufferAheadSeconds;
  final WorkFactory workFactory;
  final LabService labService;

  final ObjectProperty<FabricationStatus> status = new SimpleObjectProperty<>(FabricationStatus.Standby);
  final StringProperty inputTemplateKey = new SimpleStringProperty();
  final StringProperty outputPathPrefix = new SimpleStringProperty();
  final ObjectProperty<InputMode> inputMode = new SimpleObjectProperty<>();
  final ObjectProperty<OutputFileMode> outputFileMode = new SimpleObjectProperty<>();
  final ObjectProperty<OutputMode> outputMode = new SimpleObjectProperty<>();
  final StringProperty outputSeconds = new SimpleStringProperty();
  final StringProperty bufferAheadSeconds = new SimpleStringProperty();
  final StringProperty bufferBeforeSeconds = new SimpleStringProperty();

  final StringProperty outputFrameRate = new SimpleStringProperty();
  final StringProperty outputChannels = new SimpleStringProperty();

  public FabricationServiceImpl(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${buffer.ahead.seconds}") Integer defaultBufferAheadSeconds,
    @Value("${buffer.before.seconds}") Integer defaultBufferBeforeSeconds,
    @Value("${input.mode}") String defaultInputMode,
    @Value("${input.template.key}") String defaultInputTemplateKey,
    @Value("${output.channels}") int defaultOutputChannels,
    @Value("${output.file.mode}") String defaultOutputFileMode,
    @Value("${output.frame.rate}") double defaultOutputFrameRate,
    @Value("${output.mode}") String defaultOutputMode,
    @Value("${output.seconds}") Integer defaultOutputSeconds,
    WorkFactory workFactory,
    LabService labService
  ) {
    this.hostServices = hostServices;
    this.defaultBufferAheadSeconds = defaultBufferAheadSeconds;
    this.workFactory = workFactory;
    this.labService = labService;
    bufferAheadSeconds.set(Integer.toString(defaultBufferAheadSeconds));
    bufferBeforeSeconds.set(Integer.toString(defaultBufferBeforeSeconds));
    inputMode.set(InputMode.valueOf(defaultInputMode.toUpperCase(Locale.ROOT)));
    inputTemplateKey.set(defaultInputTemplateKey);
    outputChannels.set(Integer.toString(defaultOutputChannels));
    outputFileMode.set(OutputFileMode.valueOf(defaultOutputFileMode.toUpperCase(Locale.ROOT)));
    outputFrameRate.set(Double.toString(defaultOutputFrameRate));
    outputMode.set(OutputMode.valueOf(defaultOutputMode.toUpperCase(Locale.ROOT)));
    outputPathPrefix.set(System.getProperty("user.home") + File.separator);
    outputSeconds.set(Integer.toString(defaultOutputSeconds));
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
          .setBufferAheadSeconds(Integer.parseInt(bufferAheadSeconds.get()))
          .setBufferBeforeSeconds(Integer.parseInt(bufferBeforeSeconds.get()))
          .setOutputFrameRate(Double.parseDouble(outputFrameRate.get()))
          .setOutputChannels(Integer.parseInt(outputChannels.get()));
        return workFactory.start(configuration, () -> {
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
  public StringProperty bufferAheadSecondsProperty() {
    return bufferAheadSeconds;
  }

  @Override
  public StringProperty bufferBeforeSecondsProperty() {
    return bufferBeforeSeconds;
  }

  @Override
  public StringProperty outputChannelsProperty() {
    return outputChannels;
  }

  @Override
  public StringProperty outputFrameRateProperty() {
    return outputFrameRate;
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
  public List<Segment> getSegments(int length, @Nullable Integer startIndex, @Nullable Long activeAtChainMicros, Long thresholdMicros) {
    try {
      var from = Objects.nonNull(startIndex) ? startIndex : Math.max(0, workFactory.getSegmentManager().size() - length);
      var to = Math.min(workFactory.getSegmentManager().size() - 1, from + length);
      return workFactory
        .getSegmentManager()
        .readManyFromToOffset(from, to);
    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException e) {
      LOG.error("Failed to get segments", e);
      return List.of();
    }
  }


  String computeProgramName(@Nullable Program program, @Nullable ProgramSequence programSequence, @Nullable ProgramSequenceBinding programSequenceBinding) {
    if (Objects.nonNull(program) && Objects.nonNull(programSequence) && Objects.nonNull(programSequenceBinding))
      return String.format("%s @ %d (%s)", program.getName(), programSequenceBinding.getOffset(), programSequence.getName());
    else if (Objects.nonNull(program))
      return program.getName();
    else return "Not Loaded";
  }
}
