// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.tables.pojos.*;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.model.*;
import jakarta.annotation.Nullable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FabricationService {

  ObjectProperty<FabricationStatus> statusProperty();

  StringProperty inputTemplateKeyProperty();

  StringProperty contentStoragePathPrefixProperty();

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

  StringProperty timelineSegmentViewLimitProperty();

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

  List<Segment> getSegments(@Nullable Integer startIndex);

  Boolean isEmpty();

  String formatTotalBars(Segment segment, @Nullable Integer beats);

  String formatPositionBarBeats(Segment segment, @Nullable Double position);

  BooleanProperty followPlaybackProperty();

  ObservableDoubleValue progressProperty();

  ObservableBooleanValue isOutputModeSync();

  ObservableBooleanValue isStatusActive();

  ObservableBooleanValue isStatusLoading();

  ObservableBooleanValue isStatusStandby();

  ObservableBooleanValue isOutputModeFile();

  /**
   Return the current shipped-to chain micros

   @return chain micros, else empty
   */
  Optional<Long> getShippedToChainMicros();

  /**
   Return the current dubbed-to sync chain micros

   @return chain micros, else empty
   */
  Optional<Long> getDubbedToChainMicros();

  /**
   Return the current crafted-to chain micros

   @return chain micros, else empty
   */
  Optional<Long> getCraftedToChainMicros();

  /**
   If the current work is realtime, e.g. playback or HLS, return the target chain micros

   @return chain micros if realtime, else empty
   */
  Optional<Long> getShipTargetChainMicros();


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

  /**
   Play the demo for the given template key.

   @param templateKey       the template key to play
   @param craftAheadSeconds the number of seconds to craft ahead of the current position
   */
  void handleDemoPlay(String templateKey, Integer craftAheadSeconds);

  /**
   Get a hash of all the choices for the given segment

   @param segment for which to get the choice hash
   @return hash of all the ids of the choices for the given segment
   */
  String getChoiceHash(Segment segment);

  /**
   Get the segment spanning the current ship output chain micros

   @return the segment spanning the current ship output chain micros
   */
  Optional<Segment> getSegmentAtShipOutput();

  /**
   Cancel the current fabrication process
   */
  void cancel();

  /**
   Run the work cycle as a manual override
   */
  void runCycle();
}
