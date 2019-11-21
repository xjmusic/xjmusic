// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator;

import io.xj.core.access.Access;
import io.xj.core.entity.MemeEntity;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.isometry.MemeIsometry;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainConfig;
import io.xj.core.model.ChainConfigType;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramSequencePattern;
import io.xj.core.model.ProgramPatternType;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramVoice;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;
import io.xj.core.model.SegmentMessage;
import io.xj.music.Chord;
import io.xj.music.Note;

import javax.sound.sampled.AudioFormat;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface Fabricator {

  /**
   Add a new Arrangement

   @param arrangement to of
   @return arrangement with assigned next id (unique for this segment)
   */
  SegmentChoiceArrangement add(SegmentChoiceArrangement arrangement);

  /**
   Add a new Choice

   @param choice to of
   @return choice with assigned next id (unique for this segment)
   */
  SegmentChoice add(SegmentChoice choice);

  /**
   Add a new Pick

   @param pick to add
   @return pick with assigned next id (unique for this segment)
   */
  SegmentChoiceArrangementPick add(SegmentChoiceArrangementPick pick);

  /**
   Add a new SegmentChord

   @param segmentChord to of
   @return segmentChord with assigned next id (unique for this segment)
   */
  SegmentChord add(SegmentChord segmentChord);

  /**
   Add a new SegmentMeme

   @param segmentMeme to of
   @return segmentMeme with assigned next id (unique for this segment)
   */
  SegmentMeme add(SegmentMeme segmentMeme);

  /**
   Add a new SegmentMessage

   @param segmentMessage to of
   @return segmentMessage with assigned next id (unique for this segment)
   */
  SegmentMessage add(SegmentMessage segmentMessage);

  /**
   Compute using an integral
   the seconds of start for any given position in beats
   Velocity of Segment meter (beats per minute) increases linearly of the beginning of the Segment (at the previous Segment's tempo) to the end of the Segment (arriving at the current Segment's tempo, only at its end)
   <p>
   [#166370833] Segment should *never* be fabricated longer than its total beats.
   [#153542275] Segment wherein tempo changes expect perfectly smooth sound of previous segment through to following segment

   @param p position in beats
   @return seconds of start
   */
  Double computeSecondsAtPosition(double p) throws CoreException;

  /**
   FUTURE: [#165815496] Chain fabrication access control

   @return Access control
   @throws CoreException on failure to establish access
   */
  Access getAccess() throws CoreException;

  /**
   id of all audio picked for current segment

   @return list of audio ids
   @throws CoreException on failure
   */
  Collection<InstrumentAudio> getPickedAudios() throws CoreException;

  /**
   Get the Chain

   @return Chain
   */
  Chain getChain() throws CoreException;

  /**
   Get configs of Chain

   @return Chain Configs
   */
  Collection<ChainConfig> getChainConfigs();

  /**
   Get bindings of Chain

   @return Chain Bindings
   */
  Collection<ChainBinding> getChainBindings();

  /**
   Chain configuration, by type
   If no chain config is found for this type, a default config is returned.

   @param chainConfigType of config to fetch
   @return chain config value
   */
  ChainConfig getChainConfig(ChainConfigType chainConfigType) throws CoreException;

  /**
   Chain id, of segment

   @return chain id
   */
  UUID getChainId();

  /**
   Get current ChordEntity for any position in Segment.
   Defaults to returning a chord based on the segment key, if nothing else is found

   @param position in segment
   @return ChordEntity
   */
  Chord getChordAt(int position) throws CoreException;

  /**
   fetch the macro-type choice for the current segment in the chain

   @return macro-type segment choice
   @throws CoreException on failure
   */
  SegmentChoice getCurrentMacroChoice() throws CoreException;

  /**
   macro-type sequence binding in current segment

   @return pattern
   @throws CoreException on failure
   */
  ProgramSequence getCurrentMacroSequence() throws CoreException;

  /**
   fetch the main-type choice for the current segment in the chain

   @return main-type segment choice
   @throws CoreException on failure
   */
  SegmentChoice getCurrentMainChoice() throws CoreException;

  /**
   fetch the rhythm-type choice for the current segment in the chain

   @return rhythm-type segment choice
   @throws CoreException on failure
   */
  SegmentChoice getCurrentRhythmChoice() throws CoreException;

  /**
   @return Seconds elapsed since fabricator was instantiated
   */
  Double getElapsedSeconds();

  /**
   Get the Key for any given Choice, preferring its Sequence Key (bound), defaulting to the Program Key.

   @param choice to get key for
   @return key of specified sequence/program via choice
   @throws CoreException if unable to determine key of choice
   */
  String getKeyForChoice(SegmentChoice choice) throws CoreException;

  /**
   Get max available sequence pattern offset for a given choice

   @param choice for which to check
   @return max available sequence pattern offset
   @throws CoreException on attempt to get max available SequenceBinding offset of choice with no SequenceBinding
   */
  Long getMaxAvailableSequenceBindingOffset(SegmentChoice choice) throws CoreException;

  /**
   Compute the pattern-meme constellations of any previous segments which selected the same main sequence
   <p>
   [#161736024] to compute unique constellations for prior segments with the same main sequence

   @return map of all previous segment meme constellations (as keys) to a collection of arrangements made
   */
  Map<String, Collection<SegmentChoiceArrangement>> getMemeConstellationArrangementsOfPreviousSegments() throws CoreException;

  /**
   Compute the pattern-meme constellations of any previous segments which selected the same main sequence
   <p>
   [#161736024] to compute unique constellations for prior segments with the same main sequence

   @return map of all previous segment meme constellations (as keys) to a collection of choices made
   */
  Map<String, Collection<SegmentChoice>> getMemeConstellationChoicesOfPreviousSegments() throws CoreException;

  /**
   Get any sequence by id
   <p>
   /**
   [#162361534] Artist wants segments that continue the use of a main sequence to make the exact same instrument audio assignments, in order to further reign in the randomness, and use very slow evolution of percussive possibilities.

   @return map of all previous segment meme constellations (as keys) to a collection of picks extracted of their content JSON
   */
  Map<String, Collection<SegmentChoiceArrangementPick>> getMemeConstellationPicksOfPreviousSegments() throws CoreException;

  /**
   Get meme isometry for the current offset in this macro-choice

   @return MemeIsometry for macro-choice
   @throws CoreException on failure
   */
  MemeIsometry getMemeIsometryOfCurrentMacro() throws CoreException;

  /**
   Get meme isometry for the next offset in the previous segment's macro-choice

   @return MemeIsometry for previous macro-choice
   @throws CoreException on failure
   */
  MemeIsometry getMemeIsometryOfNextSequenceInPreviousMacro() throws CoreException;

  /**
   Get meme isometry for the current segment

   @return MemeIsometry for current segment
   @throws CoreException on failure
   */
  MemeIsometry getMemeIsometryOfSegment() throws CoreException;

  /**
   Get all memes for a given Choice id
   [#165954619] Memes include by sequence-pattern (macro- or main-type sequences) and by sequence (all sequences)

   @param choice to get memes for
   @return memes for choice
   @throws CoreException on failure
   */
  Collection<MemeEntity> getMemesOfChoice(SegmentChoice choice) throws CoreException;

  /**
   Given a Choice having a SequenceBinding,
   determine the next available SequenceBinding offset of the chosen sequence,
   or loop back to zero (if past the end of the available SequenceBinding offsets)

   @param choice having a SequenceBinding
   @return next available SequenceBinding offset of the chosen sequence, or zero (if past the end of the available SequenceBinding offsets)
   @throws CoreException on attempt to get next SequenceBinding offset of choice with no SequenceBinding
   */
  Long getNextSequenceBindingOffset(SegmentChoice choice) throws CoreException;

  /**
   Note, for any pitch in Hz

   @param pitch to get octave of
   */
  Note getNoteAtPitch(Double pitch);

  /**
   Output Audio Format

   @return output audio format
   */
  AudioFormat getOutputAudioFormat() throws CoreException;

  /**
   Output file path

   @return output file path
   */
  String getOutputFilePath() throws CoreException;

  /**
   Pitch for any Note, in Hz
   <p>
   [#255] Note pitch is calculated at 32-bit floating point precision, based on root note configured in environment parameters.

   @param note to get pitch for
   @return pitch of note, in Hz
   */
  Double getPitch(Note note);

  /**
   Get all previous segments with same main program

   @return previous segments with ame main program
   */
  Collection<Segment> getPreviousSegmentsWithSameMainProgram();

  /**
   Get Program for any given choice

   @param choice to get program for
   @return Program for the specified choice
   */
  Program getProgram(SegmentChoice choice) throws CoreException;

  /**
   fetch the macro-type choice for the previous segment in the chain

   @return macro-type segment choice, null if none found
   @throws CoreException on failure
   */
  SegmentChoice getPreviousMacroChoice() throws CoreException;

  /**
   fetch the main-type choice for the previous segment in the chain

   @return main-type segment choice, null if none found
   @throws CoreException on failure
   */
  SegmentChoice getPreviousMainChoice() throws CoreException;

  /**
   fetch the previous segment in the chain

   @return previousSegment
   */
  Segment getPreviousSegment() throws CoreException;

  /**
   The segment being fabricated

   @return Segment
   */
  Segment getSegment();

  /**
   Total length of segment of beginning to end

   @return total length
   @throws CoreException if unable to compute
   */
  Duration getSegmentTotalLength() throws CoreException;

  /**
   [#165954619] Get the sequence for a Choice either directly (rhythm- and detail-type sequences), or by sequence-pattern (macro- or main-type sequences)
   <p>
   [#166690830] Program model handles all of its own entities
   Rhythm and Detail programs are allowed to have only one (default) sequence.

   @param choice to get sequence for
   @return Sequence for choice
   @throws CoreException on failure
   */
  ProgramSequence getSequence(SegmentChoice choice) throws CoreException;

  /**
   Get the sequence pattern offset of a given Choice

   @param choice having a SequenceBinding
   @return sequence pattern offset
   @throws CoreException on attempt to get next SequenceBinding offset of choice with no SequenceBinding
   */
  Long getSequenceBindingOffsetForChoice(SegmentChoice choice) throws CoreException;

  /**
   Get the ingested source material for fabrication

   @return source material
   */
  Ingest getSourceMaterial();

  /**
   Determine type of content, e.g. initial segment in chain, or next macro-sequence

   @return macro-craft type
   */
  FabricatorType getType() throws CoreException;

  /**
   Whether the current Segment Choice has one or more sequence pattern offsets
   with a higher pattern offset than the current one

   @param choice for which to check
   @return true if it has at least one more sequence pattern offset
   @throws CoreException on attempt to get next SequenceBinding offset of choice with no SequenceBinding
   */
  boolean hasOneMoreSequenceBindingOffset(SegmentChoice choice) throws CoreException;

  /**
   Whether the current Segment Choice has two or more sequence pattern offsets
   with a higher pattern offset than the current two

   @param choice for which to check
   @return true if it has at least two more sequence pattern offsets
   @throws CoreException on attempt to get next SequenceBinding offset of choice with no SequenceBinding
   */
  boolean hasTwoMoreSequenceBindingOffsets(SegmentChoice choice) throws CoreException;

  /**
   is initial segment?

   @return whether this is the initial segment in a chain
   */
  Boolean isInitialSegment();

  /**
   Put a key-value pair into the report
   [#162999779] only exports data as a sub-field of the standard content JSON

   @param key   to put
   @param value to put
   */
  void putReport(String key, Object value);

  /**
   Update the original Segment submitted for craft,
   cache it in the internal in-memory object, and persisted in the database
   [#162361525] ALWAYS persist Segment content as JSON when work is performed
   [#162361534] musical evolution depends on segments that continue the use of a main sequence
   */
  void done() throws CoreException;

  /**
   Randomly select any sequence binding at the given offset

   @param offset to get sequence binding at
   @return randomly selected sequence binding
   @throws CoreException on failure to select a sequence binding
   */
  ProgramSequenceBinding randomlySelectSequenceBindingAtOffset(Program program, Long offset) throws CoreException;

  /**
   Randomly select any sequence

   @return ranomly selected sequence
   @throws CoreException if failure to select a sequence
   */
  ProgramSequence randomlySelectSequence(Program program) throws CoreException;

  /**
   Get picks for segment

   @return picks for segment
   */
  Collection<SegmentChoiceArrangementPick> getSegmentPicks();

  /**
   Get choices for segment

   @return choices for segment
   */
  Collection<SegmentChoice> getSegmentChoices();

  /**
   Get arrangements for segment

   @return arrangements for segment
   */
  Collection<SegmentChoiceArrangement> getSegmentArrangements();

  /**
   Get segment arrangements for a given choice

   @param choice to get segment arrangements for
   @return segments arrangements for the given segment choice
   */
  Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice choice);

  /**
   Get memes for segment

   @return memes for segment
   */
  Collection<SegmentMeme> getSegmentMemes();

  /**
   [#165954619] Selects one (at random) of all available patterns of a given type within a sequence.
   <p>
   Caches the selection, so it will always return the same output for any given input.
   <p>
   [#166481918] Rhythm fabrication composited of layered Patterns

   @param sequence    of which to select
   @param voice       of which to select
   @param patternType to select
   @return Pattern model, or null if no pattern of this type is found
   @throws CoreException on failure
   */
  Optional<ProgramSequencePattern> randomlySelectPatternOfSequenceByVoiceAndType(ProgramSequence sequence, ProgramVoice voice, ProgramPatternType patternType) throws CoreException;
}
