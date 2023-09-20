// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.tables.pojos.*;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.model.*;
import io.xj.nexus.work.WorkFactory;
import jakarta.annotation.Nullable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventTarget;
import javafx.scene.Node;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FabricationService extends Worker<Boolean>, EventTarget {

  ObjectProperty<FabricationStatus> statusProperty();

  StringProperty inputTemplateKeyProperty();

  StringProperty outputPathPrefixProperty();

  ObjectProperty<InputMode> inputModeProperty();

  ObjectProperty<OutputFileMode> outputFileModeProperty();

  ObjectProperty<OutputMode> outputModeProperty();

  StringProperty outputSecondsProperty();

  StringProperty craftAheadSecondsProperty();

  StringProperty dubAheadSecondsProperty();

  StringProperty shipAheadSecondsProperty();

  StringProperty outputChannelsProperty();

  StringProperty outputFrameRateProperty();

  WorkFactory getWorkFactory();

  Collection<SegmentMeme> getSegmentMemes(Segment segment);

  Collection<SegmentChord> getSegmentChords(Segment segment);

  Collection<SegmentChoice> getSegmentChoices(Segment segment);

  void start();

  void reset();

  Optional<Program> getProgram(UUID programId);

  Optional<ProgramSequence> getProgramSequence(UUID programSequenceId);

  Optional<ProgramVoice> getProgramVoice(UUID programVoiceId);

  Optional<ProgramSequenceBinding> getProgramSequenceBinding(UUID programSequenceBindingId);

  Optional<Instrument> getInstrument(UUID instrumentId);

  Optional<InstrumentAudio> getInstrumentAudio(UUID instrumentAudioId);

  Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice choice);

  Collection<SegmentChoiceArrangementPick> getPicks(SegmentChoiceArrangement arrangement);

  Collection<SegmentMessage> getSegmentMessages(Segment segment);

  Collection<SegmentMeta> getSegmentMetas(Segment segment);

  Node computeProgramReferenceNode(UUID programId, @Nullable UUID programSequenceBindingId);

  Node computeProgramVoiceReferenceNode(UUID programVoiceId);

  Node computeInstrumentReferenceNode(UUID instrumentId);

  Node computeInstrumentAudioReferenceNode(UUID instrumentAudioId);

  List<Segment> getSegments(int length, @Nullable Integer startIndex);

  Boolean isEmpty();

  String formatTotalBars(Segment segment, @Nullable Integer beats);

  String formatPositionBarBeats(Segment segment, @Nullable Double position);

  BooleanProperty followPlaybackProperty();

  ObservableBooleanValue isOutputModeSync();

  ObservableBooleanValue isStatusActive();

  ObservableBooleanValue isOutputModeFile();

  /**
   The fabrication service has a main action that can be triggered by the user. This action is
   dependent on the current status of the service. For example, if the service is currently
   {@link FabricationStatus#Standby}, then the main action will be to start the fabrication process.
   */
  void handleMainAction();

  /**
   The main action button text is dependent on the current status of the service. For example, if
    the service is currently {@link FabricationStatus#Standby}, then the main action button text
    will be "Start".
   */
  ObservableValue<String> mainActionButtonTextProperty();
}
