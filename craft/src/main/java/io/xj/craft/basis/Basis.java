// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.basis;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceType;
import io.xj.craft.ingest.Ingest;
import io.xj.craft.isometry.MemeIsometry;
import io.xj.music.Chord;
import io.xj.music.Note;
import org.json.JSONObject;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;

public interface Basis {

  /**
   Output file path

   @return output file path
   */
  String outputFilePath() throws BusinessException;

  /**
   Output Audio Format

   @return output audio format
   */
  AudioFormat outputAudioFormat() throws Exception;

  /**
   Determine type of basis, e.g. initial segment in chain, or next macro-sequence

   @return macro-craft type
   */
  BasisType type();

  /**
   An Ingest collection of entities that this chain segment fabrication basis will ingest.
   Based on primary chain-bindings, e.g. ChainLibrary, ChainInstrument, and ChainSequence.

   @return Ingest
   */
  Ingest ingest() throws Exception;

  /**
   An Ingest collection of entities that this chain segment fabrication basis will ingest.
   Based on tertiary chain-bindings, e.g. ChainLibrary, and Library from any of the original ChainInstrument or ChainSequence.

   @return Ingest
   */
  Ingest libraryIngest() throws Exception;

  /**
   The original segment submitted for craft

   @return Segment
   */
  Segment segment();

  /**
   is initial segment?

   @return whether this is the initial segment in a chain
   */
  Boolean isInitialSegment();

  /**
   Chain id, from segment

   @return chain id
   */
  BigInteger chainId();

  /**
   Chain configuration, by type
   If no chain config is found for this type, a default config is returned.
   (caches results)

   @param chainConfigType of config to fetch
   @return chain config value
   */
  ChainConfig chainConfig(ChainConfigType chainConfigType) throws Exception;

  /**
   Segment begin-at timestamp

   @return begin at
   */
  Timestamp segmentBeginAt();

  /**
   fetch the previous segment in the chain
   (caches results)

   @return previousSegment
   */
  Segment previousSegment() throws Exception;

  /**
   fetch the macro-type choice for the previous segment in the chain

   @return macro-type segment choice, null if none found
   @throws Exception on failure
   */
  @Nullable
  Choice previousMacroChoice() throws Exception;

  /**
   fetch the main-type choice for the previous segment in the chain

   @return main-type segment choice, null if none found
   @throws Exception on failure
   */
  @Nullable
  Choice previousMainChoice() throws Exception;

  /**
   fetch the rhythm-type choice for the previous segment in the chain

   @return rhythm-type segment choice, null if none found
   @throws Exception on failure
   */
  @Nullable
  Choice previousRhythmSelection() throws Exception;

  /**
   fetch all arrangements for the previous percussive choice

   @return arrangements
   @throws Exception on failure
   */
  Collection<Arrangement> previousPercussiveArrangements() throws Exception;

  /**
   fetch the macro-type choice for the current segment in the chain

   @return macro-type segment choice
   @throws Exception on failure
   */
  Choice currentMacroChoice() throws Exception;

  /**
   fetch the main-type choice for the current segment in the chain

   @return main-type segment choice
   @throws Exception on failure
   */
  Choice currentMainChoice() throws Exception;

  /**
   fetch the rhythm-type choice for the current segment in the chain

   @return rhythm-type segment choice
   @throws Exception on failure
   */
  Choice currentRhythmChoice() throws Exception;

  /**
   macro-type sequence pattern in current segment
   (caches results)

   @return pattern
   @throws Exception on failure
   */
  Pattern currentMacroPattern() throws Exception;

  /**
   macro-type sequence pattern in previous segment
   (caches results)

   @return pattern, null if none exists
   @throws Exception on failure
   */
  @Nullable
  Pattern previousMacroNextPattern() throws Exception;

  /**
   Chain configurations
   (caches results)

   @return map of chain config type to value
   */
  Map<ChainConfigType, ChainConfig> chainConfigs() throws Exception;

  /**
   fetch all arrangements of a choice
   (caches results)

   @param choiceId to fetch arrangements for
   @return arrangements
   @throws Exception on failure
   */
  Collection<Arrangement> choiceArrangements(BigInteger choiceId) throws Exception;

  /**
   Set the cached contents of the choice arrangements array

   @param choiceId     to set content for
   @param arrangements to return for basis choice arrangements
   */
  void setChoiceArrangements(BigInteger choiceId, Collection<Arrangement> arrangements);

  /**
   Get current Chord for any position in Segment.
   Defaults to returning a chord based on the segment key, if nothing else is found

   @param position in segment
   @return Chord
   */
  Chord chordAt(int position) throws Exception;

  /**
   Pitch for any Note, in Hz
   <p>
   [#255] Note pitch is calculated at 32-bit floating point precision, based on root note configured in environment parameters.

   @param note to get pitch for
   @return pitch of note, in Hz
   */
  Double pitch(Note note);

  /**
   Note, for any pitch in Hz

   @param pitch to get octave of
   */
  Note note(Double pitch);

  /**
   Calculate the position in seconds from the beginning of the segment, for any position given in beats.
   <p>
   [#256] Velocity of Segment meter (beats per minute) increases linearly from the beginning of the Segment (at the previous Segment's tempo) to the end of the Segment (arriving at the current Segment's tempo, only at its end)

   @param p in beats
   @return position in seconds
   */
  Double secondsAtPosition(double p) throws Exception;

  /**
   Fetch all memes for a given segment
   (caches results)

   @param segmentId to get memes for
   @return collection of segment memes
   @throws Exception on failure
   */
  Collection<SegmentMeme> segmentMemes(BigInteger segmentId) throws Exception;

  /**
   Fetch all choices for a given segment
   (caches results)

   @param segmentId to get choices for
   @return collection of segment choices
   @throws Exception on failure
   */
  Collection<Choice> choices(BigInteger segmentId) throws Exception;

  /**
   Fetch all arrangements for a given segment
   (caches results)

   @param segmentId to get arrangements for
   @return collection of segment arrangements
   @throws Exception on failure
   */
  Collection<Arrangement> arrangements(BigInteger segmentId) throws Exception;

  /**
   Read an Audio by id, assumed to be in the set of audio found for all picks in the segment

   @param audioId to fetch
   @return Audio
   */
  Audio segmentAudio(BigInteger audioId) throws Exception;

  /**
   All Audio picked for current Segment

   @return audios
   @throws Exception on failure
   */
  Map<BigInteger, Audio> segmentAudios() throws Exception;

  /**
   id of all audio picked for current segment

   @return list of audio ids
   @throws Exception on failure
   */
  Collection<BigInteger> segmentAudioIds() throws Exception;

  /**
   Fetch all entities for the current segment
   (caches results)

   @return segment entities
   @throws Exception on failure
   */
  Collection<SegmentChord> segmentChords() throws Exception;

  /**
   Fetch memes for the current segment
   (results can be overridden by caching setSegmentMemes, otherwise it's a db lookup)

   @return collection of segment memes
   @throws Exception on failure
   */
  Collection<SegmentMeme> segmentMemes() throws Exception;

  /**
   Cache segment memes (instead of relying on writing to db followed by reading)
   Avoid race condition causing [#153888310] During craft, instruments should be chosen based on combined memes of all chosen sequences for that segment.

   @param memes memes for the current segment
   */
  void setSegmentMemes(Collection<SegmentMeme> memes);

  /**
   Fetch all memes for the previous segment
   (caches results)

   @return previous segment memes
   @throws Exception on failure
   */
  Collection<SegmentMeme> previousSegmentMemes() throws Exception;

  /**
   Add a Pick to the in-memory store
   [#154014731] Ops wants platform to use SQL only for business state persistence, in order to improve performance.

   @param pick to add
   */
  void pick(Pick pick);

  /**
   Fetch all picks for the current segment
   (caches results)

   @return segment picks
   @throws Exception on failure
   */
  Collection<Pick> picks() throws Exception;

  /**
   Total length of segment from beginning to end

   @return total length
   @throws Exception if unable to compute
   */
  Duration segmentTotalLength() throws Exception;

  /**
   Fetch a segment in a chain, by offset

   @param chainId to fetch segment in
   @param offset  of segment to fetch
   @return Segment
   @throws Exception on failure
   */
  Segment segmentByOffset(BigInteger chainId, BigInteger offset) throws Exception;

  /**
   Fetch current choice of macro-type segment
   (caches results)

   @return choice record
   @throws Exception on failure
   */
  Choice segmentChoiceByType(BigInteger segmentId, SequenceType sequenceType) throws Exception;

  /**
   Update the original Segment submitted for craft,
   cache it in the internal in-memory object, and persisted in the database
   [#162361525] ALWAYS persist Segment basis as JSON when work is performed

   @param segment Segment to replace current segment, and update database with
   */
  void updateSegment(Segment segment) throws Exception;

  /**
   Update the original Segment submitted for craft,
   cache it in the internal in-memory object, and persisted in the database
   [#162361525] ALWAYS persist Segment basis as JSON when work is performed
   [#162361534] musical evolution depends on segments that continue the use of a main sequence
   */
  void updateSegment() throws Exception;

  /**
   [#162361525] Segment basis is persisted as JSON
   [#162999779] report is contained in a sub-field of the standard basis JSON
   */
  JSONObject toJSONObject();

  /**
   Put a key-value pair into the report
   [#162999779] only exports data as a sub-field of the standard basis JSON

   @param key   to put
   @param value to put
   */
  void report(String key, String value);

  /**
   Microseconds from seconds

   @param seconds to get microseconds of
   @return microseconds
   */
  Long atMicros(Double seconds);

  /**
   Get MemeIsometry instance for previous macro-choice and pattern memes

   @return MemeIsometry for previous macro-choice
   @throws Exception on failure
   */
  MemeIsometry previousMacroNextPatternMemeIsometry() throws Exception;

  /**
   Get MemeIsometry instance for macro-choice and pattern memes

   @return MemeIsometry for macro-choice
   @throws Exception on failure
   */
  MemeIsometry currentMacroMemeIsometry() throws Exception;

  /**
   Get MemeIsometry instance for current segment

   @return MemeIsometry for current segment
   @throws Exception on failure
   */
  MemeIsometry currentSegmentMemeIsometry() throws Exception;

  /**
   Create a new Choice

   @param choice to create
   */
  Choice create(Choice choice) throws Exception;

  /**
   Create a new Arrangement

   @param arrangement to create
   */
  Arrangement create(Arrangement arrangement) throws Exception;

  /**
   Create a new SegmentMeme

   @param segmentMeme to create
   */
  SegmentMeme create(SegmentMeme segmentMeme) throws Exception;

  /**
   Create a new SegmentChord

   @param segmentChord to create
   */
  SegmentChord create(SegmentChord segmentChord) throws Exception;

  /**
   Read all previous segments that selected the same main sequence as the current segment
   <p>
   [#161736024] Artist wants the rhythm selections to be consistent throughout a main sequence

   @return collection of segments
   */
  Collection<Segment> previousSegmentsWithSameMainSequence() throws Exception;

  /**
   Get any sequence by id

   @param id of sequence
   @return sequence
   */
  Sequence sequence(BigInteger id) throws Exception;

  /**
   [#162361534] Artist wants segments that continue the use of a main sequence to make the exact same instrument audio assignments, in order to further reign in the randomness, and use very slow evolution of percussive possibilities.

   @return map of all previous segment meme constellations (as keys) to a collection of picks extracted from their basis JSON
   */
  Map<String, Collection<Pick>> previousSegmentMemeConstellationPicks() throws Exception;

  /**
   Compute the pattern-meme constellations of any previous segments which selected the same main sequence
   <p>
   [#161736024] to compute unique constellations for prior segments with the same main sequence

   @return map of all previous segment meme constellations (as keys) to a collection of choices made
   */
  Map<String, Collection<Choice>> previousSegmentMemeConstellationChoices() throws Exception;

  /**
   Compute the pattern-meme constellations of any previous segments which selected the same main sequence
   <p>
   [#161736024] to compute unique constellations for prior segments with the same main sequence

   @return map of all previous segment meme constellations (as keys) to a collection of arrangements made
   */
  Map<String, Collection<Arrangement>> previousSegmentMemeConstellationArrangements() throws Exception;

  /**
   @return Seconds elapsed since basis was instantiated
   */
  Double elapsedSeconds();

}
