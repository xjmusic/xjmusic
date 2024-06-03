// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.fabricator;

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
import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.InstrumentAudio;
import io.xj.hub.pojos.Program;
import io.xj.hub.pojos.ProgramSequence;
import io.xj.hub.pojos.ProgramSequenceBinding;
import io.xj.hub.pojos.ProgramSequenceChord;
import io.xj.hub.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.pojos.ProgramSequencePattern;
import io.xj.hub.pojos.ProgramSequencePatternEvent;
import io.xj.hub.pojos.ProgramVoice;
import io.xj.engine.FabricationException;
import io.xj.engine.model.Chain;
import io.xj.engine.model.Segment;
import io.xj.engine.model.SegmentChoice;
import io.xj.engine.model.SegmentChoiceArrangement;
import io.xj.engine.model.SegmentChoiceArrangementPick;
import io.xj.engine.model.SegmentChord;
import io.xj.engine.model.SegmentChordVoicing;
import io.xj.engine.model.SegmentMeme;
import io.xj.engine.model.SegmentMessageType;
import io.xj.engine.model.SegmentType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
   Delete an entity specified by Segment id, class and id

   @param <N>       type of entity
   @param segmentId partition (segment id) of entity
   @param type      of class to delete
   @param id        to delete
   */
  <N> void delete(int segmentId, Class<N> type, UUID id) throws FabricationException;

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
   Beat and Detail choices are kept for an entire Main Program https://github.com/xjmusic/workstation/issues/265

   @return choice if previously made, or null if none is found
   */
  Optional<SegmentChoice> getChoiceIfContinued(ProgramVoice voice);

  /**
   Determine if a choice has been previously crafted
   in one of the previous segments of the current main sequence

   @return choice if previously made, or null if none is found
   */
  Collection<SegmentChoice> getChoicesIfContinued(ProgramType programType);

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
  Set<InstrumentType> getDistinctChordVoicingTypes() throws FabricationException;

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
   Key for any pick designed to collide at same voice id + name

   @param pick to get key of
   @return unique key for pattern event
   */
  String computeCacheKeyForVoiceTrack(SegmentChoiceArrangementPick pick) throws FabricationException;

  /**
   Get the Key for any given Choice, preferring its Sequence Key (bound), defaulting to the Program Key.
   <p>
   If Sequence has no key/tempo/intensity inherit from Program https://github.com/xjmusic/workstation/issues/246

   @param choice to get key for
   @return key of specified sequence/program via choice
   @throws FabricationException if unable to determine key of choice
   */
  Chord getKeyForChoice(SegmentChoice choice) throws FabricationException;

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
  ProgramConfig getCurrentMainProgramConfig() throws FabricationException;

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
   @throws FabricationException on failure
   */
  Collection<String> getNotes(SegmentChordVoicing voicing) throws FabricationException;

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
  ProgramConfig getProgramConfig(Program program) throws FabricationException;

  /**
   Get the complete set of program sequence chords,
   ignoring ghost chords* REF by choosing the voicings with largest # of notes at that position https://github.com/xjmusic/workstation/issues/248
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
  NoteRange getProgramRange(UUID programId, InstrumentType instrumentType) throws FabricationException;

  /**
   Detail craft shifts source program events into the target range https://github.com/xjmusic/workstation/issues/221
   <p>
   via average of delta from source low to target low, and from source high to target high, rounded to octave

   @param type        of instrument
   @param sourceRange to compute from
   @param targetRange to compute required # of octaves to shift into
   @return +/- octaves required to shift from source to target range
   */
  int getProgramRangeShiftOctaves(InstrumentType type, NoteRange sourceRange, NoteRange targetRange) throws FabricationException;

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
  ProgramType getProgramType(ProgramVoice voice) throws FabricationException;


  /**
   Get the voice type for the given voicing
   <p>
   Programs persist main chord/voicing structure sensibly
   https://github.com/xjmusic/workstation/issues/266

   @param voicing for which to get voice type
   @return type of voice for voicing
   */
  InstrumentType getProgramVoiceType(ProgramSequenceChordVoicing voicing) throws FabricationException;

  /**
   Get the lowest note present in any voicing of all the segment chord voicings for this segment and instrument type

   @param type to get voicing threshold low of
   @return low voicing threshold
   */
  NoteRange getProgramVoicingNoteRange(InstrumentType type) throws FabricationException;

  /**
   Randomly select any sequence

   @return randomly selected sequence
   */
  Optional<ProgramSequence> getRandomlySelectedSequence(Program program);

  /**
   Selects one (at random) of all available patterns of a given type within a sequence. https://github.com/xjmusic/workstation/issues/204
   <p>
   Caches the selection, so it will always return the same output for any given input.
   <p>
   Beat fabrication composited of layered Patterns https://github.com/xjmusic/workstation/issues/267

   @return Pattern model, or null if no pattern of this type is found
   @throws FabricationException on failure
   */
  Optional<ProgramSequencePattern> getRandomlySelectedPatternOfSequenceByVoiceAndType(SegmentChoice choice) throws FabricationException;

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
   Segment should *never* be fabricated longer than its loop beats. https://github.com/xjmusic/workstation/issues/268
   Segment wherein tempo changes expect perfectly smooth sound of previous segment through to following segment https://github.com/xjmusic/workstation/issues/269

   @param tempo    in beats per minute
   @param position in beats
   @return seconds of start
   */
  long getSegmentMicrosAtPosition(double tempo, double position);

  /**
   @return the total number of seconds in the segment
   */
  long getTotalSegmentMicros();

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
   Get all segment chord voicings

   @return segment chord voicings
   */
  Collection<SegmentChordVoicing> getChordVoicings();

  /**
   Get all segment memes

   @return segment memes
   */
  Collection<SegmentMeme> getSegmentMemes();

  /**
   Get the sequence for a Choice either directly (beat- and detail-type sequences), or by sequence-pattern (macro- or main-type sequences) https://github.com/xjmusic/workstation/issues/204
   <p>
   Beat and Detail programs are allowed to have only one (default) sequence.

   @param choice to get sequence for
   @return Sequence for choice
   @throws FabricationException on failure
   */
  Optional<ProgramSequence> getSequence(SegmentChoice choice) throws FabricationException;

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
   @throws FabricationException          on failure
   */
  void putStickyBun(StickyBun bun) throws JsonProcessingException, FabricationException;

  /**
   Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://github.com/xjmusic/workstation/issues/222
   - Sticky bun is a simple coded key-value in segment meta
   --- key by pattern ID
   --- value is a comma-separated list of integers, one integer for each note in the pattern, where
   ----- tonal note is coded as a `0` value
   ----- atonal note has a random integer value generated ranging from -50 to 50
   - Rendering a pattern X voicing considers the sticky bun values
   --- the random seed for rendering the pattern will always come from the associated sticky bun
   <p>
   Sticky buns v2 persisted for each randomly selected note in the series for any given pattern https://github.com/xjmusic/workstation/issues/231
   - key on program-sequence-pattern-event id, persisting only the first value seen for any given event
   - super-key on program-sequence-pattern id, measuring delta from the first event seen in that pattern
   <p>
   TemplateConfig parameter stickyBunEnabled
   https://github.com/xjmusic/workstation/issues/251

   @param eventId for super-key
   @return sticky bun if present
   */
  Optional<StickyBun> getStickyBun(UUID eventId);

  /**
   Get the track name for a give program sequence pattern event

   @param event for which to get track name
   @return Track name
   */
  String getTrackName(ProgramSequencePatternEvent event) throws FabricationException;

  /**
   Determine type of content, e.g. initial segment in chain, or next macro-sequence

   @return macro-craft type
   */
  SegmentType getType() throws FabricationException;

  /**
   Get segment chord voicing for a given chord

   @param chord to get voicing for
   @return chord voicing for chord
   */
  Optional<SegmentChordVoicing> chooseVoicing(SegmentChord chord, InstrumentType type);

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
  boolean isContinuationOfMacroProgram() throws FabricationException;

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
   @throws FabricationException on failure
   */
  boolean isOneShot(Instrument instrument, String trackName) throws FabricationException;

  /**
   Test if a given instrument is one-shot

   @param instrument to test
   @return true if this is a one-shot instrument
   @throws FabricationException on failure
   */
  boolean isOneShot(Instrument instrument) throws FabricationException;

  /**
   Test if a given one-shot instrument has its cutoffs enable

   @param instrument to test
   @return true if a given one-shot instrument has its cutoffs enable
   @throws FabricationException on failure
   */
  boolean isOneShotCutoffEnabled(Instrument instrument) throws FabricationException;

  /**
   is initial segment?

   @return whether this is the initial segment in a chain
   */
  Boolean isInitialSegment();

  /**
   Put a new Entity by type and id
   <p>
   If it's a SegmentChoice...
   Should add meme from ALL program and instrument types! https://github.com/xjmusic/workstation/issues/210
   - Add memes of choices to segment in order to affect further choices.
   - Add all memes of this choice, from target program, program sequence binding, or instrument if present
   - Enhances: Straightforward meme logic https://github.com/xjmusic/workstation/issues/270
   - Enhances: XJ should not add memes to Segment for program/instrument that was not successfully chosen https://github.com/xjmusic/workstation/issues/216
   <p>

   @param entity to put
   @param force overriding safeguards (e.g. choices must not violate meme stack, memes must be unique)
   @return entity successfully put
   */
  <N> N put(N entity, boolean force) throws FabricationException;

  /**
   Set the preferred audio for a key

   @param parentIdent     for which to set
   @param ident           for which to set
   @param instrumentAudio value to set
   */
  void putPreferredAudio(String parentIdent, String ident, InstrumentAudio instrumentAudio);

  /**
   Put a key-value pair into the report
   only exports data as a sub-field of the standard content JSON

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
  void updateSegment(Segment segment) throws FabricationException;

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
   Get the number of micros per beat for the current segment

   @return micros per beat
   */
  Double getMicrosPerBeat(double tempo) throws FabricationException;

  /**
   Get the second macro sequence binding offset of a given macro program

   @param macroProgram for which to get second macro sequence binding offset
   @return second macro sequence binding offset
   */
  int getSecondMacroSequenceBindingOffset(Program macroProgram);

  /**
   @return the tempo of the current main program
   */
  double getTempo() throws FabricationException;

  /**
   @return the meme taxonomy for the source material
   */
  MemeTaxonomy getMemeTaxonomy();
}
