// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.ProgramConfig;
import io.xj.hub.TemplateConfig;
import io.xj.hub.meme.MemeTaxonomy;
import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.InstrumentAudio;
import io.xj.hub.pojos.Program;
import io.xj.hub.pojos.ProgramSequence;
import io.xj.hub.pojos.ProgramSequenceBinding;
import io.xj.hub.pojos.ProgramVoice;
import io.xj.hub.pojos.Template;
import io.xj.nexus.ControlMode;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.model.SegmentMessage;
import io.xj.nexus.model.SegmentMeta;
import io.xj.nexus.work.FabricationState;
import jakarta.annotation.Nullable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FabricationService {

  ObjectProperty<FabricationState> stateProperty();

  ObservableStringValue stateTextProperty();

  ObjectProperty<Template> inputTemplateProperty();

  ObjectProperty<TemplateConfig> getTemplateConfig();

  ObjectProperty<ControlMode> controlModeProperty();

  StringProperty craftAheadSecondsProperty();

  StringProperty dubAheadSecondsProperty();

  StringProperty mixerLengthSecondsProperty();

  StringProperty outputChannelsProperty();

  StringProperty outputFrameRateProperty();

  StringProperty timelineSegmentViewLimitProperty();

  Collection<SegmentMeme> getSegmentMemes(Segment segment);

  Collection<SegmentChord> getSegmentChords(Segment segment);

  Collection<SegmentChoice> getSegmentChoices(Segment segment);

  void start();

  void reset();

  /**
   @return all macro-type programs
   */
  List<Program> getAllMacroPrograms();

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

  List<Segment> getSegments(@Nullable Integer startIndex);

  Boolean isEmpty();

  String formatTotalBars(Segment segment, @Nullable Integer beats);

  String formatPositionBarBeats(Segment segment, @Nullable Double position);

  BooleanProperty followPlaybackProperty();

  ObservableSet<String> overrideMemesProperty();

  ObjectProperty<UUID> overrideMacroProgramIdProperty();

  ObservableDoubleValue progressProperty();

  BooleanBinding isStateActiveProperty();

  BooleanBinding isStateLoadingProperty();

  BooleanBinding isStateStandbyProperty();

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
   The fabrication service has a main action that can be triggered by the user. This action is
   dependent on the current status of the service. For example, if the service is currently
   {@link FabricationState#Standby}, then the main action will be to start the fabrication process.
   */
  void handleMainAction();

  /**
   The main action button text is dependent on the current status of the service. For example, if
   the service is currently {@link FabricationState#Standby}, then the main action button text
   will be "Start".
   */
  ObservableValue<String> mainActionButtonTextProperty();

  /**
   Get a hash of all the choices for the given segment

   @param segment for which to get the choice hash
   @return hash of all the ids of the choices for the given segment
   */
  String computeChoiceHash(Segment segment);

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
   Manually go to a specific macro program, and force until reset
   https://www.pivotaltracker.com/story/show/186003440

   @param macroProgram the macro program to go to
   */
  void doOverrideMacro(Program macroProgram);

  /**
   Reset the macro program override
   https://www.pivotaltracker.com/story/show/186003440
   */
  void resetOverrideMacro();

  /**
   Get all meme taxonomies in the source template
   */
  Optional<MemeTaxonomy> getMemeTaxonomy();

  /**
   Manually go to a specific taxonomy category meme, and force until reset
   https://www.pivotaltracker.com/story/show/186714075

   @param memes specific (assumed allowably) set of taxonomy category memes
   */
  void doOverrideMemes(Collection<String> memes);

  /**
   Reset the taxonomy category memes
   https://www.pivotaltracker.com/story/show/186714075
   */
  void resetOverrideMemes();

  /**
   Get whether an override happened, and reset its state after getting

   @return true if an override happened
   */
  boolean getAndResetDidOverride();

  /**
   Reset all fabrication settings to their defaults
   */
  void resetSettingsToDefaults();

  /**
   @return The intensity override value property is between 0 and 1
   */
  DoubleProperty intensityOverrideProperty();

  /**
   @return The intensity override active property
   */
  BooleanProperty intensityOverrideActiveProperty();

}
