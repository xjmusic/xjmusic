// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.xj.hub.HubContent;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.ProgramConfig;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.meme.MemeTaxonomy;
import io.xj.hub.music.Chord;
import io.xj.hub.music.Note;
import io.xj.hub.music.NoteRange;
import io.xj.hub.music.StickyBun;
import io.xj.hub.tables.pojos.*;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.*;

import javax.sound.sampled.AudioFormat;
import java.util.*;

public interface Fabricator {

  /**
   Add a message of the given type to the segment, with the given body

   @param body to include in message
   */
  void addMessage(SegmentMessageType messageType, String body);

  /**
   Add an error message to the segment, with the given body

   @param body to include in message
   */
  void addErrorMessage(String body);

  /**
   Add a warning message to the segment, with the given body

   @param body to include in message
   */
  void addWarningMessage(String body);

  /**
   Add an info message to the segment, with the given body

   @param body to include in message
   */
  void addInfoMessage(String body);

  /**
   Remove an Entity by type and id

   @param entity to delete
   */
  <N> void delete(N entity);

  /**
   Update the original Segment submitted for craft,
   cache it in the internal in-memory object, and persisted in the database
   ALWAYS persist Segment content as JSON when work is performed https://www.pivotaltracker.com/story/show/162361525
   musical evolution depends on segments that continue the use of a main sequence https://www.pivotaltracker.com/story/show/162361534
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
   Instrument has overall volume parameter https://www.pivotaltracker.com/story/show/179215413

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
   Get choices for segment

   @return choices for segment
   */
  Collection<SegmentChoice> getChoices();

  /**
   Determine if a choice has been previously crafted
   in one of the previous segments of the current main sequence
   <p>
   Beat and Detail choices are kept for an entire Main Program https://www.pivotaltracker.com/story/show/176468964

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
   Determine if a choice has been previously crafted
   in one of the previous segments of the current main sequence

   @return choice if previously made, or null if none is found
   */
  Optional<SegmentChoice> getChoiceIfContinued(InstrumentType instrumentType, InstrumentMode instrumentMode);

  /**
   Get current ChordEntity for any position in Segment.
   Defaults to returning a chord based on the segment key, if nothing else is found

   @param position in segment
   @return ChordEntity
   */
  Optional<SegmentChord> getChordAt(Double position);

  /**
   fetch the main-type choice for the current segment in the chain

   @return main-type segment choice
   */
  Optional<SegmentChoice> getCurrentMainChoice();

  /**
   Get the current main program

   @return main program if present
   */
  Program getCurrentMainProgram() throws NexusException;

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

   @return set of voicing (instrument) types
   */
  Set<InstrumentType> getDistinctChordVoicingTypes() throws NexusException;

  /**
   @return Seconds elapsed since fabricator was instantiated
   */
  Long getElapsedMicros();

  /**
   Get the InstrumentConfig from a given instrument, with fallback to instrument section of injected config values

   @param instrument to get config of
   @return InstrumentConfig from a given instrument, with fallback values
   */
  InstrumentConfig getInstrumentConfig(Instrument instrument);

  /**
   Get the InstrumentConfig for a given pick, with fallback to instrument section of injected config values

   @param pick to get config of
   @return InstrumentConfig from a given instrument, with fallback values
   */
  InstrumentConfig getInstrumentConfig(SegmentChoiceArrangementPick pick) throws NexusException;

  /**
   Key for any pick designed to collide at same voice id + name

   @param pick to get key of
   @return unique key for pattern event
   */
  String computeCacheKeyForVoiceTrack(SegmentChoiceArrangementPick pick) throws NexusException;

  /**
   Get the Key for any given Choice, preferring its Sequence Key (bound), defaulting to the Program Key.
   <p>
   If Sequence has no key/tempo/density inherit from Program https://www.pivotaltracker.com/story/show/176474164

   @param choice to get key for
   @return key of specified sequence/program via choice
   @throws NexusException if unable to determine key of choice
   */
  Chord getKeyForChoice(SegmentChoice choice) throws NexusException;

  /**
   fetch the macro-type choice for the previous segment in the chain

   @return macro-type segment choice, null if none found
   */
  Optional<SegmentChoice> getMacroChoiceOfPreviousSegment();

  /**
   fetch the main-type choice for the previous segment in the chain

   @return main-type segment choice, null if none found
   */
  Optional<SegmentChoice> getPreviousMainChoice();

  /**
   Get the configuration of the current main program

   @return main-program configuration
   */
  ProgramConfig getCurrentMainProgramConfig() throws NexusException;

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
  Optional<InstrumentAudio> getPreferredAudio(String parentIdent, String ident);

  /**
   Get Program for any given choice

   @param choice to get program for
   @return Program for the specified choice
   */
  Optional<Program> getProgram(SegmentChoice choice);

  /**
   Get the ProgramConfig from a given program, with fallback to program section of injected config values

   @param program to get config of
   @return ProgramConfig from a given program, with fallback values
   */
  ProgramConfig getProgramConfig(Program program) throws NexusException;

  /**
   Get the complete set of program sequence chords,
   ignoring ghost chords* REF by choosing the voicings with largest # of notes at that position https://www.pivotaltracker.com/story/show/178420030
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
   Detail craft shifts source program events into the target range https://www.pivotaltracker.com/story/show/176696738
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

   @param instrumentType to switch behavior
   @param fromChord      to compute shift from
   @param toChord        to compute shift toward
   @return computed target shift
   */
  int getProgramTargetShift(InstrumentType instrumentType, Chord fromChord, Chord toChord);

  /**
   Get the program type of given voice

   @param voice for which to get program type
   @return program type
   */
  ProgramType getProgramType(ProgramVoice voice) throws NexusException;


  /**
   Get the voice type for the given voicing
   <p>
   Programs persist main chord/voicing structure sensibly
   https://www.pivotaltracker.com/story/show/182220689

   @param voicing for which to get voice type
   @return type of voice for voicing
   */
  InstrumentType getProgramVoiceType(ProgramSequenceChordVoicing voicing) throws NexusException;

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
   Selects one (at random) of all available patterns of a given type within a sequence. https://www.pivotaltracker.com/story/show/165954619
   <p>
   Caches the selection, so it will always return the same output for any given input.
   <p>
   Beat fabrication composited of layered Patterns https://www.pivotaltracker.com/story/show/166481918

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
   Segment should *never* be fabricated longer than its total beats. https://www.pivotaltracker.com/story/show/166370833
   Segment wherein tempo changes expect perfectly smooth sound of previous segment through to following segment https://www.pivotaltracker.com/story/show/153542275

   @param p position in beats
   @return seconds of start
   */
  long getSegmentMicrosAtPosition(double p) throws NexusException;

  /**
   @return the total number of seconds in the segment
   */
  long getTotalSegmentMicros() throws NexusException;

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
   Returns the segment ship key concatenated with a specified extension

   @return Output Metadata Key
   */
  String getSegmentShipKey(String extension);

  /**
   Get the sequence for a Choice either directly (beat- and detail-type sequences), or by sequence-pattern (macro- or main-type sequences) https://www.pivotaltracker.com/story/show/165954619
   <p>
   Program model handles all of its own entities https://www.pivotaltracker.com/story/show/166690830
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
   Store a sticky bun in the fabricator

   @param bun to store
   @throws JsonProcessingException on failure
   @throws NexusException          on failure
   */
  void putStickyBun(StickyBun bun) throws JsonProcessingException, NexusException;

  /**
   Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787
   - Sticky bun is a simple coded key-value in segment meta
   --- key by pattern ID
   --- value is a comma-separated list of integers, one integer for each note in the pattern, where
   ----- tonal note is coded as a `0` value
   ----- atonal note has a random integer value generated ranging from -50 to 50
   - Rendering a pattern X voicing considers the sticky bun values
   --- the random seed for rendering the pattern will always come from the associated sticky bun
   <p>
   Sticky buns v2 persisted for each randomly selected note in the series for any given pattern https://www.pivotaltracker.com/story/show/179153822
   - key on program-sequence-pattern-event id, persisting only the first value seen for any given event
   - super-key on program-sequence-pattern id, measuring delta from the first event seen in that pattern
   <p>
   TemplateConfig parameter stickyBunEnabled
   https://www.pivotaltracker.com/story/show/181839489

   @param eventId for super-key
   @return sticky bun if present
   */
  Optional<StickyBun> getStickyBun(UUID eventId);

  /**
   Get the track name for a give program sequence pattern event

   @param event for which to get track name
   @return Track name
   */
  String getTrackName(ProgramSequencePatternEvent event) throws NexusException;

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
   Whether a given InstrumentAudio is directly bound to the Chain,
   where "directly" means a level more specific than Library, e.g. Program or InstrumentAudio

   @param instrumentAudio to test for direct binding
   @return true if InstrumentAudio is directly bound to chain
   */
  boolean isDirectlyBound(InstrumentAudio instrumentAudio);

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
   Should add meme from ALL program and instrument types! https://www.pivotaltracker.com/story/show/181336704
   - Add memes of choices to segment in order to affect further choices.
   - Add all memes of this choice to the workbench, from target program, program sequence binding, or instrument if present
   - Enhances: Straightforward meme logic https://www.pivotaltracker.com/story/show/179078533
   - Enhances: XJ should not add memes to Segment for program/instrument that was not successfully chosen https://www.pivotaltracker.com/story/show/180468224
   <p>

   @param entity to put
   @return entity successfully put
   */
  <N> N put(N entity) throws NexusException;

  /**
   Set the preferred audio for a key

   @param parentIdent     for which to set
   @param ident           for which to set
   @param instrumentAudio value to set
   */
  void putPreferredAudio(String parentIdent, String ident, InstrumentAudio instrumentAudio);

  /**
   Put a key-value pair into the report
   only exports data as a sub-field of the standard content JSON https://www.pivotaltracker.com/story/show/162999779

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
   Get the Segment Retrospective

   @return retrospective
   */
  SegmentRetrospective retrospective();

  /**
   Get the ingested source material for fabrication

   @return source material
   */
  HubContent sourceMaterial();

  /**
   If in local mode, use PCM, else use the template config

   @return output encoding
   */
  AudioFormat.Encoding computeOutputEncoding();

  /**
   If in local mode, use 16, else use the template config

   @return output sample bits
   */
  int computeOutputSampleBits();

  /**
   Get the number of micros per beat for the current segment

   @return micros per beat
   */
  Double getMicrosPerBeat() throws NexusException;

  /**
   Get the second macro sequence binding offset of a given macro program

   @param macroProgram for which to get second macro sequence binding offset
   @return second macro sequence binding offset
   */
  int getSecondMacroSequenceBindingOffset(Program macroProgram);

  /**
   @return the meme taxonomy for the source material
   */
  MemeTaxonomy getMemeTaxonomy();
}
