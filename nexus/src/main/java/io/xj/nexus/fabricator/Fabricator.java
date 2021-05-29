// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.protobuf.MessageLite;
import io.xj.Chain;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.InstrumentAudioEvent;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceBinding;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramSequencePatternEvent;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.SegmentMessage;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Key;
import io.xj.lib.music.NoteRange;
import io.xj.hub.client.HubClientAccess;
import io.xj.hub.client.HubContent;
import io.xj.hub.dao.InstrumentConfig;
import io.xj.hub.dao.ProgramConfig;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.ChainConfig;

import javax.sound.sampled.AudioFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Fabricator {

  /**
   Add a new Entity

   @param entity to add
   @return entity added
   */
  <N extends MessageLite> N add(N entity) throws NexusException;

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
  Double computeSecondsAtPosition(double p) throws NexusException;

  /**
   FUTURE: [#165815496] Chain fabrication access control

   @return HubClientAccess control
   @throws NexusException on failure to establish access
   */
  HubClientAccess getAccess() throws NexusException;

  /**
   id of all audio picked for current segment

   @return list of audio ids
   */
  Collection<InstrumentAudio> getPickedAudios();

  /**
   Get the Chain

   @return Chain
   */
  Chain getChain();

  /**
   Chain configuration, by type
   If no chain config is found for this type, a default config is returned.

   @return chain configuration
   */
  ChainConfig getChainConfig();

  /**
   Chain id, of segment

   @return chain id
   */
  String getChainId();

  /**
   Get current ChordEntity for any position in Segment.
   Defaults to returning a chord based on the segment key, if nothing else is found

   @param position in segment
   @return ChordEntity
   */
  Optional<SegmentChord> getChordAt(double position);

  /**
   Get the Messages for the current segment in the chain

   @return Segment Messages
   */
  Collection<SegmentMessage> getSegmentMessages();

  /**
   fetch the macro-type choice for the current segment in the chain

   @return macro-type segment choice
   */
  Optional<SegmentChoice> getCurrentMacroChoice();

  /**
   fetch the main-type choice for the current segment in the chain

   @return main-type segment choice
   */
  Optional<SegmentChoice> getCurrentMainChoice();

  /**
   fetch the rhythm-type choice for the current segment in the chain

   @return rhythm-type segment choice
   */
  Optional<SegmentChoice> getCurrentRhythmChoice();

  /**
   fetch the detail-type choice for the current segment in the chain

   @return detail-type segment choice
   */
  Collection<SegmentChoice> getCurrentDetailChoices();

  /**
   @return Seconds elapsed since fabricator was instantiated
   */
  Double getElapsedSeconds();

  /**
   Get the Key for any given Choice, preferring its Sequence Key (bound), defaulting to the Program Key.
   <p>
   [#176474164] If Sequence has no key/tempo/density inherit from Program

   @param choice to get key for
   @return key of specified sequence/program via choice
   @throws NexusException if unable to determine key of choice
   */
  Key getKeyForChoice(SegmentChoice choice) throws NexusException;

  /**
   Get max available sequence pattern offset for a given choice

   @param choice for which to check
   @return max available sequence pattern offset
   @throws NexusException on attempt to get max available SequenceBinding offset of choice with no SequenceBinding
   */
  Long getMaxAvailableSequenceBindingOffset(SegmentChoice choice) throws NexusException;

  /**
   Compute the pattern-meme constellations of any previous segments which selected the same main sequence
   <p>
   [#161736024] to compute unique constellations for prior segments with the same main sequence

   @return map of all previous segment meme constellations (as keys) to a collection of arrangements made
   */
  Map<String, Collection<SegmentChoiceArrangement>> getMemeConstellationArrangementsOfPreviousSegments() throws NexusException;

  /**
   Get the arrangements of any previous segments which selected the same main sequence
   <p>
   [#176468964] Rhythm and Detail choices are kept for an entire Main Program

   @return map of all previous segment meme constellations (as keys) to a collection of arrangements made
   */
  Collection<SegmentChoiceArrangement> getChoiceArrangementsOfPreviousSegments() throws NexusException;

  /**
   Compute the pattern-meme constellations of any previous segments which selected the same main program
   <p>
   [#161736024] to compute unique constellations for prior segments with the same main program

   @return map of all previous segment meme constellations (as keys) to a collection of choices made
   */
  Map<String, Collection<SegmentChoice>> getMemeConstellationChoicesOfPreviousSegments() throws NexusException;

  /**
   Get the choices of any previous segments which selected the same main sequence
   <p>
   [#176468964] Rhythm and Detail choices are kept for an entire Main Program

   @return map of all previous segment meme constellations (as keys) to a collection of choices made
   */
  Collection<SegmentChoice> getChoicesOfPreviousSegmentsWithSameMainProgram() throws NexusException;

  /**
   Get the picks of any previous segments which selected the same main sequence
   <p>
   [#175947230] Artist writing detail program expects 'X' note value to result in random part creation from available Voicings

   @return map of all previous segment meme constellations (as keys) to a collection of choices made
   */
  Collection<SegmentChoiceArrangementPick> getPicksOfPreviousSegmentsWithSameMainProgram();

  /**
   Get any sequence by id
   <p>
   /**
   [#162361534] Artist wants segments that continue the use of a main sequence to make the exact same instrument audio assignments, in order to further reign in the randomness, and use very slow evolution of percussive possibilities.

   @return map of all previous segment meme constellations (as keys) to a collection of picks extracted of their content JSON
   */
  Map<String, Collection<SegmentChoiceArrangementPick>> getMemeConstellationPicksOfPreviousSegments() throws NexusException;

  /**
   Get the choiceArrangementPicks of any previous segments which selected the same main sequence
   <p>
   [#176468964] Rhythm and Detail choiceArrangementPicks are kept for an entire Main Program

   @return map of all previous segment meme constellations (as keys) to a collection of choiceArrangementPicks made
   */
  Collection<SegmentChoiceArrangementPick> getChoiceArrangementPicksOfPreviousSegments() throws NexusException;

  /**
   Get previously chosen (for previous segments with same main program) instrument audio

   @return map of previous chosen instrument audio
   @throws NexusException on failure to build map
   */
  Map<String, InstrumentAudio> getPreviousInstrumentAudio() throws NexusException;

  /**
   Key for any pick designed to collide at same voice id + name

   @param pick to get key of
   @return unique key for pattern event
   */
  String keyByVoiceTrack(SegmentChoiceArrangementPick pick) throws NexusException;

  /**
   Key for any pattern event designed to collide at same voice id + track name

   @param event to get key of
   @return unique key for pattern event
   */
  String keyByVoiceTrack(ProgramSequencePatternEvent event) throws NexusException;

  /**
   Key for any pattern event designed to collide at same voice id + note
   <p>
   [#176649593] Sampler obeys isMultiphonic from Instrument config

   @param track to get key of
   @param note  to get key of
   @return unique key for pattern event
   */
  String keyByTrackNote(String track, String note) throws NexusException;

  /**
   Get the Voice ID of a given event

   @param event to get voice String of
   @return Track name
   */
  String getTrackName(ProgramSequencePatternEvent event) throws NexusException;

  /**
   Determine if an arrangement has been previously crafted
   in one of the previous segments of the current main sequence
   wherein the current pattern of the selected main sequence
   <p>
   [#176468964] Rhythm and Detail choices are kept for an entire Main Program

   @return rhythm sequence if previously selected, or null if none is found
   */
  Optional<String> getPreviousVoiceInstrumentId(String voiceId);

  /**
   Get meme isometry for the current offset in this macro-choice

   @return MemeIsometry for macro-choice
   */
  MemeIsometry getMemeIsometryOfCurrentMacro();

  /**
   Get meme isometry for the next offset in the previous segment's macro-choice

   @return MemeIsometry for previous macro-choice
   */
  MemeIsometry getMemeIsometryOfNextSequenceInPreviousMacro();

  /**
   Get meme isometry for the current segment

   @return MemeIsometry for current segment
   */
  MemeIsometry getMemeIsometryOfSegment();

  /**
   Get all memes for a given Choice id
   [#165954619] Memes include by sequence-pattern (macro- or main-type sequences) and by sequence (all sequences)

   @param choice to get memes for
   @return memes for choice
   @throws NexusException on failure
   */
  Collection<SegmentMeme> getMemesOfChoice(SegmentChoice choice) throws NexusException;

  /**
   Given a Choice having a SequenceBinding,
   determine the next available SequenceBinding offset of the chosen sequence,
   or loop back to zero (if past the end of the available SequenceBinding offsets)

   @param choice having a SequenceBinding
   @return next available SequenceBinding offset of the chosen sequence, or zero (if past the end of the available SequenceBinding offsets)
   */
  Long getNextSequenceBindingOffset(SegmentChoice choice);

  /**
   Output Audio Format

   @return output audio format
   */
  AudioFormat getOutputAudioFormat() throws NexusException;

  /**
   Output file path for a High-quality Audio output file

   @return High-quality Audio output file path
   */
  String getFullQualityAudioOutputFilePath() throws NexusException;

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
  Optional<Program> getProgram(SegmentChoice choice);

  /**
   Get Program for any given arrangement

   @param arrangement to get program for
   @return program for arrangement
   */
  Optional<Program> getProgram(SegmentChoiceArrangement arrangement);

  /**
   fetch the macro-type choice for the previous segment in the chain

   @return macro-type segment choice, null if none found
   */
  Optional<SegmentChoice> getPreviousMacroChoice();

  /**
   fetch the main-type choice for the previous segment in the chain

   @return main-type segment choice, null if none found
   */
  Optional<SegmentChoice> getPreviousMainChoice();

  /**
   fetch the previous segment in the chain

   @return previousSegment
   */
  Segment getPreviousSegment() throws NexusException;

  /**
   The segment being fabricated

   @return Segment
   */
  Segment getSegment();

  /**
   Set the Segment.
   Any modifications to the Segment must be re-written to here
   because protobuf instances are immutable

   @param segment to set
   */
  void updateSegment(Segment segment) throws NexusException;

  /**
   Returns the storage key concatenated with the output encoder as its file extension

   @return Output Waveform Key
   */
  String getSegmentOutputWaveformKey();

  /**
   Returns the storage key concatenated with JSON as its file extension

   @return Output Metadata Key
   */
  String getSegmentOutputMetadataKey();

  /**
   Returns the storage key concatenated with JSON as its file extension

   @return Output Metadata Key
   */
  String getChainOutputMetadataKey();

  /**
   Returns the segment storage key concatenated with a specified extension

   @return Output Metadata Key
   */
  String getSegmentStorageKey(String extension);

  /**
   Returns the chain storage key concatenated with a specified extension

   @return Output Metadata Key
   */
  String getChainStorageKey(String extension);

  /**
   [#165954619] Get the sequence for a Choice either directly (rhythm- and detail-type sequences), or by sequence-pattern (macro- or main-type sequences)
   <p>
   [#166690830] Program model handles all of its own entities
   Rhythm and Detail programs are allowed to have only one (default) sequence.

   @param choice to get sequence for
   @return Sequence for choice
   @throws NexusException on failure
   */
  Optional<ProgramSequence> getSequence(SegmentChoice choice) throws NexusException;

  /**
   Get the sequence pattern offset of a given Choice

   @param choice having a SequenceBinding
   @return sequence pattern offset
   */
  Long getSequenceBindingOffsetForChoice(SegmentChoice choice);

  /**
   Get the ingested source material for fabrication

   @return source material
   */
  HubContent getSourceMaterial();

  /**
   Determine type of content, e.g. initial segment in chain, or next macro-sequence

   @return macro-craft type
   */
  Segment.Type getType();

  /**
   Whether the current Segment Choice has one or more sequence pattern offsets
   with a higher pattern offset than the current one

   @param choice for which to check
   @return true if it has at least one more sequence pattern offset
   */
  boolean hasOneMoreSequenceBindingOffset(SegmentChoice choice);

  /**
   Whether the current Segment Choice has two or more sequence pattern offsets
   with a higher pattern offset than the current two

   @param choice for which to check
   @return true if it has at least two more sequence pattern offsets
   */
  boolean hasTwoMoreSequenceBindingOffsets(SegmentChoice choice);

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
  void done() throws NexusException;

  /**
   Randomly select any sequence binding at the given offset

   @param offset to get sequence binding at
   @return randomly selected sequence binding
   */
  Optional<ProgramSequenceBinding> randomlySelectSequenceBindingAtOffset(Program program, Long offset);

  /**
   Randomly select any sequence

   @return randomly selected sequence
   */
  Optional<ProgramSequence> randomlySelectSequence(Program program);

  /**
   Get choices for segment

   @return choices for segment
   */
  Collection<SegmentChoice> getChoices();

  /**
   Get Choice for arrangement

   @param arrangement for which to get choice
   @return choice for arrangement
   */
  Optional<SegmentChoice> getChoice(SegmentChoiceArrangement arrangement);

  /**
   Get arrangements for segment

   @return arrangements for segment
   */
  Collection<SegmentChoiceArrangement> getArrangements();

  /**
   Get arrangement picks for segment

   @return arrangement picks for segment
   */
  Collection<SegmentChoiceArrangementPick> getPicks();

  /**
   Get segment arrangements for a given choice

   @param choices to get segment arrangements for
   @return segments arrangements for the given segment choice
   */
  Collection<SegmentChoiceArrangement> getArrangements(Collection<SegmentChoice> choices);

  /**
   Get all segment chords, guaranteed to be in order of position ascending

   @return segment chords
   */
  Collection<SegmentChord> getSegmentChords();

  /**
   Get segment chord voicing for a given chord

   @param chord to get voicing for
   @return chord voicing for chord
   */
  Optional<SegmentChordVoicing> getVoicing(SegmentChord chord, Instrument.Type type);

  /**
   Get voicing notes for a given segment chord
   <p>
   Cache this lookup and transformation for optimal performance fabricating many notes from repeated voicings

   @param chord to get voicing notes for
   @param type  to get voicing notes for
   @return voicing notes
   */
  Collection<String> getVoicingNotes(SegmentChord chord, Instrument.Type type);

  /**
   Get instrument for a given segment pick

   @param pick to get instrument for
   @return instrument for pick
   @throws NexusException on failure
   */
  Optional<Instrument> getInstrument(SegmentChoiceArrangementPick pick) throws NexusException;

  /**
   Get instrument for a given arrangement

   @param arrangement to get instrument for
   @return instrument for pick
   @throws NexusException on failure
   */
  Optional<Instrument> getInstrument(SegmentChoiceArrangement arrangement) throws NexusException;

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

   @param patternType to select
   @return Pattern model, or null if no pattern of this type is found
   @throws NexusException on failure
   */
  Optional<ProgramSequencePattern> randomlySelectPatternOfSequenceByVoiceAndType(SegmentChoice choice, ProgramSequencePattern.Type patternType) throws NexusException;

  /**
   Get a JSON:API payload of the entire result of Segment Fabrication

   @return JSON:API payload of the entire result of Segment Fabrication
   */
  String getSegmentMetadataJson() throws NexusException;

  /**
   Get a JSON:API payload of the entire result of Chain Fabrication

   @return JSON:API payload of the entire result of Chain Fabrication
   */
  String getChainMetadataJson() throws NexusException;

  /**
   Whether a given Program is directly bound to the Chain,
   where "directly" means a level more specific than Library, e.g. Program or Instrument

   @param program to test for direct binding
   @return true if Program is directly bound to chain
   */
  boolean isDirectlyBound(Program program);

  /**
   Whether a given Instrument is directly bound to the Chain,
   where "directly" means a level more specific than Library, e.g. Program or Instrument

   @param instrument to test for direct binding
   @return true if Instrument is directly bound to chain
   */
  boolean isDirectlyBound(Instrument instrument);

  /**
   Get the ProgramConfig from a given program, with fallback to program section of guice-injected config values

   @param program to get config of
   @return ProgramConfig from a given program, with fallback values
   */
  ProgramConfig getProgramConfig(Program program) throws NexusException;

  /**
   Get the InstrumentConfig from a given instrument, with fallback to instrument section of guice-injected config values

   @param instrument to get config of
   @return InstrumentConfig from a given instrument, with fallback values
   */
  InstrumentConfig getInstrumentConfig(Instrument instrument) throws NexusException;

  /**
   Get a list of unique voicing (instrument) types present in the voicings of the current main program's chords.

   @return list of voicing (instrument) types
   */
  List<Instrument.Type> getDistinctChordVoicingTypes() throws NexusException;

  /**
   Determine if program has been previously selected
   in one of the previous segments of the current main program
   wherein the current pattern of the selected main program
   <p>
   [#176468964] Rhythm and Detail choices are kept for an entire Main Program

   @param programType    to get previously chosen program of
   @param instrumentType to get previously chosen program of
   @return detail program if previously selected, or null if none is found
   */
  List<String> getPreviouslyChosenProgramIds(Program.Type programType, Instrument.Type instrumentType) throws NexusException;

  /**
   Get the notes previously picked for this event, for the same main program

   @param programSequencePatternEventId to get previous notes picked for
   @param segmentChordName              to test for previously picked notes of
   @return notes picked previously for event
   */
  Optional<Set<String>> getPreviouslyPickedNotes(String programSequencePatternEventId, String segmentChordName);

  /**
   Remember which notes were picked for a given event

   @param programSequencePatternEventId to remember notes picked for
   @param chordName                     to remember notes picked for
   @param notes                         to remember were picked
   @return notes to pass  through for chaining method calls
   */
  Set<String> rememberPickedNotes(String programSequencePatternEventId, String chordName, Set<String> notes);

  /**
   Get the mix amplitude (ratio) for the instrument type of a given pick, based on chain config
   <p>
   [#176651700] Chain config includes proprietary mix % modifiers for different instrument types

   @param pick to get amplitude of
   @return amplitude of instrument type
   */
  double getAmplitudeForInstrumentType(SegmentChoiceArrangementPick pick) throws NexusException;

  /**
   Get the lowest note present in any voicing of all the segment chord voicings for this segment and instrument type

   @param type to get voicing threshold low of
   @return low voicing threshold
   */
  NoteRange computeVoicingNoteRange(Instrument.Type type) throws NexusException;

  /**
   Get the Notes from a Voicing

   @param voicing to get notes of
   @return notes from voicing
   @throws NexusException on failure
   */
  Collection<String> getNotes(SegmentChordVoicing voicing) throws NexusException;

  /**
   Get the note range for an arrangement based on all the events in its program

   @param programId        to get range of
   @param instrumentType to get range of
   @return Note range of arrangement
   */
  NoteRange computeProgramRange(String programId, Instrument.Type instrumentType) throws NexusException;

  /**
   Get the first event of each audio in the instrument

   @param instrument to get first audio events of
   @return first event of each audio from the instrument
   */
  Collection<InstrumentAudioEvent> getFirstEventsOfAudiosOfInstrument(Instrument instrument) throws NexusException;

  /**
   Whether this type of segment continues the same macro-program from the previous segment

   @return true if this segment continues the same macro-program
   */
  boolean continuesMacroProgram();

  /**
   Does the program of the specified Choice have at least N more sequence binding offsets available?

   @param choice of which to check the program for next available sequence binding offsets
   @param N      more sequence offsets to check for
   @return true if N more sequence binding offsets are available
   */
  boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N);

  /**
   Determine the type of fabricator

   @return type of fabricator
   */
  Segment.Type determineType();

  /**
   [#176696738] Detail craft shifts source program events into the target range
   <p>
   via average of delta from source low to target low, and from source high to target high, rounded to octave

   @param type        of instrument
   @param sourceRange to compute from
   @param targetRange to compute required # of octaves to shift into
   @return +/- octaves required to shift from source to target range
   */
  int computeRangeShiftOctaves(Instrument.Type type, NoteRange sourceRange, NoteRange targetRange);

  /**
   Compute the target shift from a key toward a chord

   @param fromKey to compute shift from
   @param toChord to compute shift toward
   @return computed target shift
   */
  int computeTargetShift(Key fromKey, Chord toChord);
}
