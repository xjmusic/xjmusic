// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.isometry.MemeIsometry;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence_pattern.SequencePattern;
import io.xj.music.Chord;
import io.xj.music.Note;

import javax.sound.sampled.AudioFormat;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface Fabricator {

  /**
   Add a new Arrangement

   @param arrangement to create
   @return arrangement with assigned next id (unique for this segment)
   */
  Arrangement add(Arrangement arrangement) throws CoreException;

  /**
   Add a new Choice

   @param choice to create
   @return choice with assigned next id (unique for this segment)
   */
  Choice add(Choice choice) throws CoreException;

  /**
   Add a new Pick

   @param pick to add
   @return pick with assigned next id (unique for this segment)
   */
  Pick add(Pick pick) throws CoreException;

  /**
   Add a new SegmentChord

   @param segmentChord to create
   @return segmentChord with assigned next id (unique for this segment)
   */
  SegmentChord add(SegmentChord segmentChord) throws CoreException;

  /**
   Add a new SegmentMeme

   @param segmentMeme to create
   @return segmentMeme with assigned next id (unique for this segment)
   */
  SegmentMeme add(SegmentMeme segmentMeme) throws CoreException;

  /**
   Add a new SegmentMessage

   @param segmentMessage to create
   @return segmentMessage with assigned next id (unique for this segment)
   */
  SegmentMessage add(SegmentMessage segmentMessage) throws CoreException;

  /**
   Compute using an integral
   the seconds from start for any given position in beats
   Velocity of Segment meter (beats per minute) increases linearly from the beginning of the Segment (at the previous Segment's tempo) to the end of the Segment (arriving at the current Segment's tempo, only at its end)
   <p>
   [#153542275] Segment wherein tempo changes expect perfectly smooth sound from previous segment through to following segment

   @param p position in beats
   @return seconds from start
   */
    Double computeSecondsAtPosition(double p) throws CoreException;

  /**
   FUTURE: [#165815496] Chain fabrication access control

   @return Access control
   @throws CoreException on failure to establish access
   */
    Access getAccess() throws CoreException;

  /**
   Get all entities bound to chain

   @return collection of entities
   @throws CoreException on failure
   */
    Collection<Entity> getAllAvailableEntities() throws CoreException;

  /**
   Chain configurations
   (Caches its DAO read)

   @return map of chain config type to value
   */
    Map<ChainConfigType, ChainConfig> getAllChainConfigs();

  /**
   id of all audio picked for current segment

   @return list of audio ids
   @throws CoreException on failure
   */
  Collection<BigInteger> getAllSegmentAudioIds() throws CoreException;

  /**
   All Audio picked for current Segment

   @return audios
   @throws CoreException on failure
   */
  Map<BigInteger, Audio> getAllSegmentAudios() throws CoreException;

  /**
   Chain configuration, by type
   If no chain config is found for this type, a default config is returned.

   @param chainConfigType of config to fetch
   @return chain config value
   */
    ChainConfig getChainConfig(ChainConfigType chainConfigType) throws CoreException;

  /**
   Chain id, from segment

   @return chain id
   */
    BigInteger getChainId();

  /**
   Get current Chord for any position in Segment.
   Defaults to returning a chord based on the segment key, if nothing else is found

   @param position in segment
   @return Chord
   */
  Chord getChordAt(int position) throws CoreException;

  /**
   fetch the macro-type choice for the current segment in the chain

   @return macro-type segment choice
   @throws CoreException on failure
   */
    Choice getCurrentMacroChoice() throws CoreException;

  /**
   macro-type sequence pattern in current segment

   @return pattern
   @throws CoreException on failure
   */
    Pattern getCurrentMacroOffset() throws CoreException;

  /**
   fetch the main-type choice for the current segment in the chain

   @return main-type segment choice
   @throws CoreException on failure
   */
    Choice getCurrentMainChoice() throws CoreException;

  /**
   fetch the rhythm-type choice for the current segment in the chain

   @return rhythm-type segment choice
   @throws CoreException on failure
   */
    Choice getCurrentRhythmChoice() throws CoreException;

  /**
   @return Seconds elapsed since content was instantiated
   <p>
   */
  Double getElapsedSeconds();

  /**
   An Ingest collection of entities that this chain segment fabrication content will ingest.
   Based on primary chain-bindings, e.g. ChainLibrary, ChainInstrument, and ChainSequence.
   (CACHED)

   @return Ingest
   */
    Ingest getSourceMaterial() throws CoreException;

  /**
   Get max available sequence pattern offset for a given choice

   @param choice for which to check
   @return max available sequence pattern offset
   @throws CoreException on attempt to get max available SequencePattern offset of choice with no SequencePattern
   */
    BigInteger getMaxAvailableSequencePatternOffset(Choice choice) throws CoreException;

  /**
   Compute the pattern-meme constellations of any previous segments which selected the same main sequence
   <p>
   [#161736024] to compute unique constellations for prior segments with the same main sequence

   @return map of all previous segment meme constellations (as keys) to a collection of arrangements made
   */
    Map<String, Collection<Arrangement>> getMemeConstellationArrangementsOfPreviousSegment() throws CoreException;

  /**
   Compute the pattern-meme constellations of any previous segments which selected the same main sequence
   <p>
   [#161736024] to compute unique constellations for prior segments with the same main sequence

   @return map of all previous segment meme constellations (as keys) to a collection of choices made
   */
    Map<String, Collection<Choice>> getMemeConstellationChoicesOfPreviousSegment() throws CoreException;

  /**
   Get any sequence by id
   <p>
   /**
   [#162361534] Artist wants segments that continue the use of a main sequence to make the exact same instrument audio assignments, in order to further reign in the randomness, and use very slow evolution of percussive possibilities.

   @return map of all previous segment meme constellations (as keys) to a collection of picks extracted from their content JSON
   */
    Map<String, Collection<Pick>> getMemeConstellationPicksOfPreviousSegment() throws CoreException;

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
    MemeIsometry getMemeIsometryOfNextPatternInPreviousMacro() throws CoreException;

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
    Collection<Meme> getMemesOfChoice(Choice choice) throws CoreException;

  /**
   Given a Choice having a SequencePattern,
   determine the next available SequencePattern offset of the chosen sequence,
   or loop back to zero (if past the end of the available SequencePattern offsets)

   @param choice having a SequencePattern
   @return next available SequencePattern offset of the chosen sequence, or zero (if past the end of the available SequencePattern offsets)
   @throws CoreException on attempt to get next SequencePattern offset of choice with no SequencePattern
   */
    BigInteger getNextSequencePatternOffset(Choice choice) throws CoreException;

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
   Get the preset choice arrangements, stored after scanning previous segments choosing same sequences
   TODO ensure this is implemented with tests, in order to achieve craft that uses same arrangements as previous segment(s)

   @return preset choice arrangements
   */
  Map<UUID, Collection<Arrangement>> getPresetChoiceArrangements();

  /**
   fetch the macro-type choice for the previous segment in the chain

   @return macro-type segment choice, null if none found
   @throws CoreException on failure
   */
    Choice getPreviousMacroChoice() throws CoreException;

  /**
   macro-type sequence pattern in previous segment

   @return pattern, null if none exists
   @throws CoreException on failure
   */
    Pattern getPreviousMacroNextOffset() throws CoreException;

  /**
   fetch the main-type choice for the previous segment in the chain

   @return main-type segment choice, null if none found
   @throws CoreException on failure
   */
    Choice getPreviousMainChoice() throws CoreException;

  /**
   fetch the previous segment in the chain

   @return previousSegment
   */
    Segment getPreviousSegment() throws CoreException;

  /**
   Read all previous segments with the same main sequence as this one
   (Caches its DAO read)

   @return collection of segments
   */
    Collection<Segment> getPreviousSegmentsWithSameMainSequence();

  /**
   [#165954619]
   Selects one (at random) from all available patterns of a given type within a sequence.
   Caches the selection, so it will always return the same output for any given input.

   @param sequenceId  of pattern
   @param patternType to fetch
   @return Pattern model
   @throws CoreException on failure
   */
    Pattern getRandomPatternByType(BigInteger sequenceId, PatternType patternType) throws CoreException;

  /**
   [#165954619]
   Selects one (at random) from all available sequence patterns an at offset of a sequence.
   Caches the selection, so it will always return the same output for any given input.
   NOTE:
   It's necessary for accessors to use this cached sequence pattern (NOT a cached pattern) because sequence_pattern_memes cannot be determined by pattern alone!

   @param sequenceId            of pattern
   @param sequencePatternOffset to fetch
   @return SequencePattern model
   @throws CoreException on failure
   */
    SequencePattern getRandomSequencePatternAtOffset(BigInteger sequenceId, BigInteger sequencePatternOffset) throws CoreException;

  /**
   The segment being fabricated

   @return Segment
   */
  Segment getSegment();

  /**
   Read an Audio by id, assumed to be in the set of audio found for all picks in the segment

   @param audioId to fetch
   @return Audio
   */
  Audio getSegmentAudio(BigInteger audioId) throws CoreException;

  /**
   Segment begin-at timestamp

   @return begin at
   */
    Timestamp getSegmentBeginAt();

  /**
   Fetch a segment in a chain, by offset

   @param chainId to fetch segment in
   @param offset  of segment to fetch
   @return Segment
   @throws CoreException on failure
   */
    Segment getSegmentByOffset(BigInteger chainId, BigInteger offset) throws CoreException;

  /**
   Total length of segment from beginning to end

   @return total length
   @throws CoreException if unable to compute
   */
    Duration getSegmentTotalLength() throws CoreException;

  /**
   [#165954619] Get the sequence for a Choice either directly (rhythm- and detail-type sequences), or by sequence-pattern (macro- or main-type sequences)

   @param choice to get sequence for
   @return Sequence for choice
   @throws CoreException on failure
   */
    Sequence getSequenceOfChoice(Choice choice) throws CoreException;

  /**
   Get the sequence pattern offset of a given Choice

   @param choice having a SequencePattern
   @return sequence pattern offset
   @throws CoreException on attempt to get next SequencePattern offset of choice with no SequencePattern
   */
    BigInteger getSequencePatternOffsetForChoice(Choice choice) throws CoreException;

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
   @throws CoreException on attempt to get next SequencePattern offset of choice with no SequencePattern
   */
    boolean hasOneMoreSequencePatternOffset(Choice choice) throws CoreException;

  /**
   Whether the current Segment Choice has two or more sequence pattern offsets
   with a higher pattern offset than the current two

   @param choice for which to check
   @return true if it has at least two more sequence pattern offsets
   @throws CoreException on attempt to get next SequencePattern offset of choice with no SequencePattern
   */
    boolean hasTwoMoreSequencePatternOffsets(Choice choice) throws CoreException;

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
  void putReport(String key, String value);

  /**

   Set the cached contents of the choice arrangements array@param choice     to set content for

   @param arrangements to return for content choice arrangements
   */
  void setPreArrangementsForChoice(Choice choice, Collection<Arrangement> arrangements);

  /**
   Update the original Segment submitted for craft,
   cache it in the internal in-memory object, and persisted in the database
   [#162361525] ALWAYS persist Segment content as JSON when work is performed
   [#162361534] musical evolution depends on segments that continue the use of a main sequence
   */
  void updateSegment() throws CoreException;

}
