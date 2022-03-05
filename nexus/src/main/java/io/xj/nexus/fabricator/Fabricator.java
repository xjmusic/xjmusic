// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import io.xj.api.*;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.ProgramConfig;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.*;
import io.xj.lib.music.*;
import io.xj.nexus.NexusException;
import io.xj.nexus.hub_client.client.HubContent;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.util.*;

public interface Fabricator {

  /**
   Add a message of the given type to the segment, with the given body

   @param body to include in message
   */
  void addMessage(SegmentMessageType messageType, String body) throws NexusException;

  /**
   Add an error message to the segment, with the given body

   @param body to include in message
   */
  void addErrorMessage(String body) throws NexusException;

  /**
   Add an warning message to the segment, with the given body

   @param body to include in message
   */
  void addWarningMessage(String body) throws NexusException;

  /**
   Add an info message to the segment, with the given body

   @param body to include in message
   */
  void addInfoMessage(String body) throws NexusException;

  /**
   Remove an Entity by type and id

   @param entity to delete
   */
  <N> void delete(N entity) throws NexusException;

  /**
   Update the original Segment submitted for craft,
   cache it in the internal in-memory object, and persisted in the database
   [#162361525] ALWAYS persist Segment content as JSON when work is performed
   [#162361534] musical evolution depends on segments that continue the use of a main sequence
   */
  void done() throws NexusException;

  /**
   Get arrangements for segment

   @return arrangements for segment
   */
  Collection<SegmentChoiceArrangement> getArrangements();

  /**
   Get segment arrangements for a given choice

   @param choices to get segment arrangements for
   @return segments arrangements for the given segment choice
   */
  Collection<SegmentChoiceArrangement> getArrangements(Collection<SegmentChoice> choices);

  /**
   Compute the audio volume for a given pick
   <p>
   Instrument has overall volume parameter #179215413

   @param pick for which to get audio volume
   @return audio volume of pick
   */
  double getAudioVolume(SegmentChoiceArrangementPick pick);

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
  TemplateConfig getTemplateConfig();

  /**
   Get a JSON:API payload of the entire result of Chain Fabrication
   Including ALL segments-- this allows the chain to rehydrate from this output

   @return JSON:API payload of the entire result of Chain Fabrication
   */
  String getChainFullJson() throws NexusException;

  /**
   Returns the ship key concatenated with JSON as its file extension
   Including ALL segments-- this allows the chain to rehydrate from this output

   @return Output Metadata Key
   */
  String getChainFullJsonOutputKey();

  /**
   Get a JSON:API payload of the entire result of Chain Fabrication
   Including only the next 90 seconds worth of segments- to the keep the player load focused on the near-future

   @return JSON:API payload of the entire result of Chain Fabrication
   */
  String getChainJson() throws NexusException;

  /**
   Returns the ship key concatenated with JSON as its file extension
   Including only the next 90 seconds worth of segments- to the keep the player load focused on the near-future

   @return Output Metadata Key
   */
  String getChainJsonOutputKey();

  /**
   Get choices for segment

   @return choices for segment
   */
  Collection<SegmentChoice> getChoices();

  /**
   Determine if a choice has been previously crafted
   in one of the previous segments of the current main sequence
   <p>
   [#176468964] Beat and Detail choices are kept for an entire Main Program

   @return choice if previously made, or null if none is found
   */
  Optional<SegmentChoice> getChoiceIfContinued(ProgramVoice voice);

  /**
   Determine if a choice has been previously crafted
   in one of the previous segments of the current main sequence

   @return choice if previously made, or null if none is found
   */
  Optional<SegmentChoice> getChoiceIfContinued(ProgramType programType);

  /**
   Determine if a choice has been previously crafted
   in one of the previous segments of the current main sequence

   @return choice if previously made, or null if none is found
   */
  Optional<SegmentChoice> getChoiceIfContinued(InstrumentType instrumentType);

  /**
   Get current ChordEntity for any position in Segment.
   Defaults to returning a chord based on the segment key, if nothing else is found

   @param position in segment
   @return ChordEntity
   */
  Optional<SegmentChord> getChordAt(double position);

  /**
   fetch the main-type choice for the current segment in the chain

   @return main-type segment choice
   */
  Optional<SegmentChoice> getCurrentMainChoice();

  /**
   Get the sequence targeted by the current main choice

   @return current main sequence
   */
  Optional<ProgramSequence> getCurrentMainSequence();

  /**
   fetch the detail-type choice for the current segment in the chain

   @return detail-type segment choice
   */
  Collection<SegmentChoice> getCurrentDetailChoices();

  /**
   fetch the beat-type choice for the current segment in the chain

   @return beat-type segment choice
   */
  Optional<SegmentChoice> getCurrentBeatChoice();

  /**
   Get a list of unique voicing (instrument) types present in the voicings of the current main program's chords.

   @return list of voicing (instrument) types
   */
  Set<InstrumentType> getDistinctChordVoicingTypes() throws NexusException;

  /**
   @return Seconds elapsed since fabricator was instantiated
   */
  Double getElapsedSeconds();

  /**
   Output file path for a High-quality Audio output file

   @return High-quality Audio output file path
   */
  String getFullQualityAudioOutputFilePath() throws NexusException;

  /**
   Get the InstrumentConfig from a given instrument, with fallback to instrument section of guice-injected config values

   @param instrument to get config of
   @return InstrumentConfig from a given instrument, with fallback values
   */
  InstrumentConfig getInstrumentConfig(Instrument instrument) throws NexusException;

  /**
   Key for any pick designed to collide at same voice id + name

   @param pick to get key of
   @return unique key for pattern event
   */
  String getKeyByVoiceTrack(SegmentChoiceArrangementPick pick) throws NexusException;

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
   fetch the macro-type choice for the previous segment in the chain

   @return macro-type segment choice, null if none found
   */
  Optional<SegmentChoice> getMacroChoiceOfPreviousSegment();

  /**
   fetch the main-type choice for the previous segment in the chain

   @return main-type segment choice, null if none found
   */
  Optional<SegmentChoice> getMainChoiceOfPreviousSegment();

  /**
   Get the configuration of the current main program

   @return main-program configuration
   */
  ProgramConfig getMainProgramConfig() throws NexusException;

  /**
   Get the sequence targeted by the previous main choice

   @return previous main sequence
   */
  Optional<ProgramSequence> getPreviousMainSequence();

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
   Given a Choice having a SequenceBinding,
   determine the next available SequenceBinding offset of the chosen sequence,
   or loop back to zero (if past the end of the available SequenceBinding offsets)

   @param choice having a SequenceBinding
   @return next available SequenceBinding offset of the chosen sequence, or zero (if past the end of the available SequenceBinding offsets)
   */
  Integer getNextSequenceBindingOffset(SegmentChoice choice);

  /**
   Get the Notes from a Voicing

   @param voicing to get notes of
   @return notes from voicing
   @throws NexusException on failure
   */
  Collection<String> getNotes(SegmentChordVoicing voicing) throws NexusException;

  /**
   Output Audio Format

   @return output audio format
   */
  AudioFormat getOutputAudioFormat() throws NexusException;

  /**
   id of all audio picked for current segment

   @return list of audio ids
   */
  Collection<InstrumentAudio> getPickedAudios();

  /**
   Get arrangement picks for segment

   @return arrangement picks for segment
   */
  Collection<SegmentChoiceArrangementPick> getPicks();

  /**
   Get the picks for a given choice, in order of position ascending from beginning of segment

   @param choice for which to get picks
   @return picks
   */
  List<SegmentChoiceArrangementPick> getPicks(SegmentChoice choice);

  /**
   Get preferred (previously chosen) instrument audios

   @return preferred audios
   */
  Optional<InstrumentAudio> getPreferredAudio(ProgramSequencePatternEvent event, String note);

  /**
   Get preferred (previously chosen) notes

   @param eventId   of event
   @param chordName of chord
   @return notes
   */
  Optional<Set<String>> getPreferredNotes(UUID eventId, String chordName);

  /**
   Get Program for any given choice

   @param choice to get program for
   @return Program for the specified choice
   */
  Optional<Program> getProgram(SegmentChoice choice);

  /**
   Get the ProgramConfig from a given program, with fallback to program section of guice-injected config values

   @param program to get config of
   @return ProgramConfig from a given program, with fallback values
   */
  ProgramConfig getProgramConfig(Program program) throws NexusException;

  /**
   Get the complete set of program sequence chords,
   ignoring ghost chords* REF https://www.pivotaltracker.com/story/show/178420030 by choosing the voicings with largest # of notes at that position
   (caches results)

   @param programSequence for which to get complete do-ghosted set of chords
   @return get complete do-ghosted set of chords for program sequence
   */
  Collection<ProgramSequenceChord> getProgramSequenceChords(ProgramSequence programSequence);

  /**
   Get the note range for an arrangement based on all the events in its program

   @param programId      to get range of
   @param instrumentType to get range of
   @return Note range of arrangement
   */
  NoteRange getProgramRange(UUID programId, InstrumentType instrumentType) throws NexusException;

  /**
   [#176696738] Detail craft shifts source program events into the target range
   <p>
   via average of delta from source low to target low, and from source high to target high, rounded to octave

   @param type        of instrument
   @param sourceRange to compute from
   @param targetRange to compute required # of octaves to shift into
   @return +/- octaves required to shift from source to target range
   */
  int getProgramRangeShiftOctaves(InstrumentType type, NoteRange sourceRange, NoteRange targetRange) throws NexusException;

  /**
   Get the sequence for a given choice

   @param choice for which to get sequence
   @return sequence of choice
   */
  Optional<ProgramSequence> getProgramSequence(SegmentChoice choice);

  /**
   Compute the target shift from a key toward a chord

   @param fromKey to compute shift from
   @param toChord to compute shift toward
   @return computed target shift
   */
  int getProgramTargetShift(Key fromKey, Chord toChord);

  /**
   Get the program type of a given voice

   @param voice for which to get program type
   @return program type
   */
  ProgramType getProgramType(ProgramVoice voice) throws NexusException;

  /**
   Get the lowest note present in any voicing of all the segment chord voicings for this segment and instrument type

   @param type to get voicing threshold low of
   @return low voicing threshold
   */
  NoteRange getProgramVoicingNoteRange(InstrumentType type) throws NexusException;

  /**
   Randomly select any sequence

   @return randomly selected sequence
   */
  Optional<ProgramSequence> getRandomlySelectedSequence(Program program);

  /**
   [#165954619] Selects one (at random) of all available patterns of a given type within a sequence.
   <p>
   Caches the selection, so it will always return the same output for any given input.
   <p>
   [#166481918] Beat fabrication composited of layered Patterns

   @return Pattern model, or null if no pattern of this type is found
   @throws NexusException on failure
   */
  Optional<ProgramSequencePattern> getRandomlySelectedPatternOfSequenceByVoiceAndType(SegmentChoice choice) throws NexusException;

  /**
   Randomly select any sequence binding at the given offset

   @param offset to get sequence binding at
   @return randomly selected sequence binding
   */
  Optional<ProgramSequenceBinding> getRandomlySelectedSequenceBindingAtOffset(Program program, Integer offset);

  /**
   Get a randomly selected voice of the given program id

   @param programId       for which to randomly select voice
   @param excludeVoiceIds to exclude from random selection
   @return randomly selected voice
   */
  Optional<ProgramVoice> getRandomlySelectedVoiceForProgramId(UUID programId, Collection<UUID> excludeVoiceIds);

  /**
   Get the root note from an available set of voicings and a given chord

   @param voicingNotes available voicing notes
   @param chord        for which to seek root note among available voicings
   @return root note
   */
  Optional<Note> getRootNoteMidRange(String voicingNotes, Chord chord);

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
  Double getSecondsAtPosition(double p) throws NexusException;

  /**
   @return the total number of seconds in the segment
   */
  double getTotalSeconds() throws NexusException;

  /**
   The segment being fabricated

   @return Segment
   */
  Segment getSegment();

  /**
   Get all segment chords, guaranteed to be in order of position ascending

   @return segment chords
   */
  List<SegmentChord> getSegmentChords();

  /**
   Get all segment memes

   @return segment memes
   */
  Collection<SegmentMeme> getSegmentMemes();

  /**
   Get a JSON:API payload of the entire result of Segment Fabrication

   @return JSON:API payload of the entire result of Segment Fabrication
   */
  String getSegmentJson() throws NexusException;

  /**
   Returns the ship key concatenated with JSON as its file extension

   @return Output Metadata Key
   */
  String getSegmentJsonOutputKey();

  /**
   Returns the ship key concatenated with the output encoder as its file extension

   @return Output Waveform Key
   */
  String getSegmentOutputWaveformKey();

  /**
   Returns the segment ship key concatenated with a specified extension

   @return Output Metadata Key
   */
  String getSegmentShipKey(String extension);

  /**
   [#165954619] Get the sequence for a Choice either directly (beat- and detail-type sequences), or by sequence-pattern (macro- or main-type sequences)
   <p>
   [#166690830] Program model handles all of its own entities
   Beat and Detail programs are allowed to have only one (default) sequence.

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
  Integer getSequenceBindingOffsetForChoice(SegmentChoice choice);

  /**
   Sticky buns v2 #179153822 persisted for each randomly selected note in the series for any given pattern
   - key on program-sequence-pattern-event id, persisting only the first value seen for any given event
   - super-key on program-sequence-pattern id, measuring delta from the first event seen in that pattern

   @param patternId for super-key
   @return sticky bun if present
   */
  Optional<StickyBun> getStickyBun(UUID patternId);

  /**
   Get the track name for a give program sequence pattern event

   @param event for which to get track name
   @return Track name
   */
  String getTrackName(ProgramSequencePatternEvent event) throws NexusException;

  /**
   Get the track name for a give segment choice arrangement pick

   @param pick for which to get track name
   @return Track name
   */
  String getTrackName(SegmentChoiceArrangementPick pick) throws NexusException;

  /**
   Determine type of content, e.g. initial segment in chain, or next macro-sequence

   @return macro-craft type
   */
  SegmentType getType() throws NexusException;

  /**
   Get segment chord voicing for a given chord

   @param chord to get voicing for
   @return chord voicing for chord
   */
  Optional<SegmentChordVoicing> getVoicing(SegmentChord chord, InstrumentType type);

  /**
   Does the program of the specified Choice have at least N more sequence binding offsets available?

   @param choice of which to check the program for next available sequence binding offsets
   @param N      more sequence offsets to check for
   @return true if N more sequence binding offsets are available
   */
  boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N);

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
   Whether this type of segment continues the same macro-program from the previous segment

   @return true if this segment continues the same macro-program
   */
  boolean isContinuationOfMacroProgram() throws NexusException;

  /**
   Whether a given Instrument is directly bound to the Chain,
   where "directly" means a level more specific than Library, e.g. Program or Instrument

   @param instrument to test for direct binding
   @return true if Instrument is directly bound to chain
   */
  boolean isDirectlyBound(Instrument instrument);

  /**
   Whether a given Program is directly bound to the Chain,
   where "directly" means a level more specific than Library, e.g. Program or Instrument

   @param program to test for direct binding
   @return true if Program is directly bound to chain
   */
  boolean isDirectlyBound(Program program);

  /**
   Test if a given instrument and track name is a one-shot sample hit

   @param instrument to test
   @param trackName  to test
   @return true if this is a one-shot instrument and track name
   @throws NexusException on failure
   */
  boolean isOneShot(Instrument instrument, String trackName) throws NexusException;

  /**
   Test if a given instrument is one-shot

   @param instrument to test
   @return true if this is a one-shot instrument
   @throws NexusException on failure
   */
  boolean isOneShot(Instrument instrument) throws NexusException;

  /**
   Test if a given one-shot instrument has its cutoffs enable

   @param instrument to test
   @return true if a given one-shot instrument has its cutoffs enable
   @throws NexusException on failure
   */
  boolean isOneShotCutoffEnabled(Instrument instrument) throws NexusException;

  /**
   is initial segment?

   @return whether this is the initial segment in a chain
   */
  Boolean isInitialSegment();

  /**
   Put a new Entity by type and id
   <p>
   If it's a SegmentChoice...
   Should add meme from ALL program and instrument types! #181336704
   - Add memes of choices to segment in order to affect further choices.
   - Add all memes of this choice to the workbench, from target program, program sequence binding, or instrument if present
   - Enhances: Straightforward meme logic #179078533
   - Enhances: XJ should not add memes to Segment for program/instrument that was not successfully chosen #180468224
   <p>

   @param entity to put
   @return entity successfully put
   */
  <N> N put(N entity) throws NexusException;

  /**
   Remember which notes were picked for a given event

   @param event     for which to remember picked notes
   @param chordName to remember notes picked for
   @param notes     to remember were picked
   */
  void putNotesPickedForChord(ProgramSequencePatternEvent event, String chordName, Set<String> notes);

  /**
   Set the preferred audio for a key

   @param event           for which to set
   @param note            for which to set
   @param instrumentAudio value to set
   */
  void putPreferredAudio(ProgramSequencePatternEvent event, String note, InstrumentAudio instrumentAudio);

  /**
   Put a key-value pair into the report
   [#162999779] only exports data as a sub-field of the standard content JSON

   @param key   to put
   @param value to put
   */
  void putReport(String key, Object value);

  /**
   Set the Segment.
   Any modifications to the Segment must be re-written to here
   because protobuf instances are immutable

   @param segment to set
   */
  void putSegment(Segment segment) throws NexusException;

  /**
   Sticky buns v2 #179153822 persisted for each randomly selected note in the series for any given pattern
   - key on program-sequence-pattern-event id, persisting only the first value seen for any given event
   - super-key on program-sequence-pattern id, measuring delta from the first event seen in that pattern@param eventId  member id-- if this is null, the sticky bun will be ignored

   @param rootNote     note of current chord
   @param position of note
   @param note     to put
   */
  void putStickyBun(@Nullable UUID eventId, Note rootNote, Double position, Note note);

  /**
   Get the Segment Retrospective

   @return retrospective
   */
  SegmentRetrospective retrospective();

  /**
   Get the ingested source material for fabrication

   @return source material
   */
  HubContent sourceMaterial();
}
