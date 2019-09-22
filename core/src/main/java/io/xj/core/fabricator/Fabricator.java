// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.isometry.MemeIsometry;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainConfigType;
import io.xj.core.model.chain.sub.ChainConfig;
import io.xj.core.model.entity.MemeEntity;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.sub.Audio;
import io.xj.core.model.instrument.sub.AudioEvent;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.sub.Arrangement;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.segment.sub.Pick;
import io.xj.core.model.segment.sub.SegmentChord;
import io.xj.core.model.segment.sub.SegmentMeme;
import io.xj.core.model.segment.sub.SegmentMessage;
import io.xj.music.Chord;
import io.xj.music.Note;

import javax.sound.sampled.AudioFormat;
import java.math.BigInteger;
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
  Arrangement add(Arrangement arrangement);

  /**
   Add a new Choice

   @param choice to create
   @return choice with assigned next id (unique for this segment)
   */
  Choice add(Choice choice);

  /**
   Add a new Pick

   @param pick to add
   @return pick with assigned next id (unique for this segment)
   */
  Pick add(Pick pick);

  /**
   Add a new SegmentChord

   @param segmentChord to create
   @return segmentChord with assigned next id (unique for this segment)
   */
  SegmentChord add(SegmentChord segmentChord);

  /**
   Add a new SegmentMeme

   @param segmentMeme to create
   @return segmentMeme with assigned next id (unique for this segment)
   */
  SegmentMeme add(SegmentMeme segmentMeme);

  /**
   Add a new SegmentMessage

   @param segmentMessage to create
   @return segmentMessage with assigned next id (unique for this segment)
   */
  SegmentMessage add(SegmentMessage segmentMessage);

  /**
   Compute using an integral
   the seconds from start for any given position in beats
   Velocity of Segment meter (beats per minute) increases linearly from the beginning of the Segment (at the previous Segment's tempo) to the end of the Segment (arriving at the current Segment's tempo, only at its end)
   <p>
   [#166370833] Segment should *never* be fabricated longer than its total beats.
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
   Chain configurations
   (Caches its DAO read)

   @return map of chain config type to value
   */
  Map<ChainConfigType, ChainConfig> getAllChainConfigs() throws CoreException;

  /**
   id of all audio picked for current segment

   @return list of audio ids
   @throws CoreException on failure
   */
  Collection<Audio> getPickedAudios() throws CoreException;

  /**
   Get Audio by UUID

   @param id of audio
   @return audio
   @throws CoreException on failure to get
   */
  Audio getAudio(UUID id) throws CoreException;

  /**
   Get Audio for a given Pick

   @param pick to get audio for
   @return audio for the given pick
   @throws CoreException on failure to locate the audio for the specified pick
   */
  Audio getAudio(Pick pick) throws CoreException;

  /**
   Get the Chain

   @return Chain
   */
  Chain getChain() throws CoreException;

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
  Choice getCurrentMacroChoice() throws CoreException;

  /**
   macro-type sequence binding in current segment

   @return pattern
   @throws CoreException on failure
   */
  Sequence getCurrentMacroSequence() throws CoreException;

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
   */
  Double getElapsedSeconds();

  /**
   Read all AudioEvent that are first in an audio, for all audio in an Instrument

   @param instrument to get audio for
   @return audio events
   @throws CoreException on failure
   */
  Collection<AudioEvent> getFirstEventsOfAudiosOfInstrument(Instrument instrument) throws CoreException;

  /**
   Get the Key for any given Choice, preferring its Sequence Key (bound), defaulting to the Program Key.

   @param choice to get key for
   @return key of specified sequence/program via choice
   @throws CoreException if unable to determine key of choice
   */
  String getKeyForChoice(Choice choice) throws CoreException;

  /**
   Get max available sequence pattern offset for a given choice

   @param choice for which to check
   @return max available sequence pattern offset
   @throws CoreException on attempt to get max available SequenceBinding offset of choice with no SequenceBinding
   */
  Long getMaxAvailableSequenceBindingOffset(Choice choice) throws CoreException;

  /**
   Compute the pattern-meme constellations of any previous segments which selected the same main sequence
   <p>
   [#161736024] to compute unique constellations for prior segments with the same main sequence

   @return map of all previous segment meme constellations (as keys) to a collection of arrangements made
   */
  Map<String, Collection<Arrangement>> getMemeConstellationArrangementsOfPreviousSegments() throws CoreException;

  /**
   Compute the pattern-meme constellations of any previous segments which selected the same main sequence
   <p>
   [#161736024] to compute unique constellations for prior segments with the same main sequence

   @return map of all previous segment meme constellations (as keys) to a collection of choices made
   */
  Map<String, Collection<Choice>> getMemeConstellationChoicesOfPreviousSegments() throws CoreException;

  /**
   Get any sequence by id
   <p>
   /**
   [#162361534] Artist wants segments that continue the use of a main sequence to make the exact same instrument audio assignments, in order to further reign in the randomness, and use very slow evolution of percussive possibilities.

   @return map of all previous segment meme constellations (as keys) to a collection of picks extracted from their content JSON
   */
  Map<String, Collection<Pick>> getMemeConstellationPicksOfPreviousSegments() throws CoreException;

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
  Collection<MemeEntity> getMemesOfChoice(Choice choice) throws CoreException;

  /**
   Given a Choice having a SequenceBinding,
   determine the next available SequenceBinding offset of the chosen sequence,
   or loop back to zero (if past the end of the available SequenceBinding offsets)

   @param choice having a SequenceBinding
   @return next available SequenceBinding offset of the chosen sequence, or zero (if past the end of the available SequenceBinding offsets)
   @throws CoreException on attempt to get next SequenceBinding offset of choice with no SequenceBinding
   */
  Long getNextSequenceBindingOffset(Choice choice) throws CoreException;

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
  Program getProgram(Choice choice) throws CoreException;

  /**
   Get the program containing any given sequence

   @param sequence to get program for
   @return Program
   */
  Program getProgram(Sequence sequence) throws CoreException;

  /**
   fetch the macro-type choice for the previous segment in the chain

   @return macro-type segment choice, null if none found
   @throws CoreException on failure
   */
  Choice getPreviousMacroChoice() throws CoreException;

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
   The segment being fabricated

   @return Segment
   */
  Segment getSegment();

  /**
   Fetch a segment in a chain, by offset

   @param chainId to fetch segment in
   @param offset  of segment to fetch
   @return Segment
   @throws CoreException on failure
   */
  Segment getSegmentByOffset(BigInteger chainId, Long offset) throws CoreException;

  /**
   Total length of segment from beginning to end

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
  Sequence getSequence(Choice choice) throws CoreException;

  /**
   Get the sequence pattern offset of a given Choice

   @param choice having a SequenceBinding
   @return sequence pattern offset
   @throws CoreException on attempt to get next SequenceBinding offset of choice with no SequenceBinding
   */
  Long getSequenceBindingOffsetForChoice(Choice choice) throws CoreException;

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
  boolean hasOneMoreSequenceBindingOffset(Choice choice) throws CoreException;

  /**
   Whether the current Segment Choice has two or more sequence pattern offsets
   with a higher pattern offset than the current two

   @param choice for which to check
   @return true if it has at least two more sequence pattern offsets
   @throws CoreException on attempt to get next SequenceBinding offset of choice with no SequenceBinding
   */
  boolean hasTwoMoreSequenceBindingOffsets(Choice choice) throws CoreException;

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
  void updateSegment() throws CoreException;

}
