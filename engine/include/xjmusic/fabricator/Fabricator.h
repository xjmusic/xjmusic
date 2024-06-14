// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATOR_H
#define XJMUSIC_FABRICATOR_H

#include "xjmusic/entities/Entity.h"
#include "xjmusic/entities/content/ContentEntityStore.h"
#include "xjmusic/entities/content/Instrument.h"
#include "xjmusic/entities/content/InstrumentAudio.h"
#include "xjmusic/entities/content/InstrumentConfig.h"
#include "xjmusic/entities/content/InstrumentMeme.h"
#include "xjmusic/entities/content/Library.h"
#include "xjmusic/entities/content/Program.h"
#include "xjmusic/entities/content/ProgramConfig.h"
#include "xjmusic/entities/content/ProgramMeme.h"
#include "xjmusic/entities/content/ProgramSequence.h"
#include "xjmusic/entities/content/ProgramSequenceBinding.h"
#include "xjmusic/entities/content/ProgramSequenceBindingMeme.h"
#include "xjmusic/entities/content/ProgramSequenceChord.h"
#include "xjmusic/entities/content/ProgramSequenceChordVoicing.h"
#include "xjmusic/entities/content/ProgramSequencePattern.h"
#include "xjmusic/entities/content/ProgramSequencePatternEvent.h"
#include "xjmusic/entities/content/ProgramVoice.h"
#include "xjmusic/entities/content/ProgramVoiceTrack.h"
#include "xjmusic/entities/content/Project.h"
#include "xjmusic/entities/content/Template.h"
#include "xjmusic/entities/content/TemplateBinding.h"
#include "xjmusic/entities/content/TemplateConfig.h"
#include "xjmusic/entities/meme/MemeTaxonomy.h"
#include "xjmusic/entities/music/Accidental.h"
#include "xjmusic/entities/music/BPM.h"
#include "xjmusic/entities/music/Bar.h"
#include "xjmusic/entities/music/Chord.h"
#include "xjmusic/entities/music/Note.h"
#include "xjmusic/entities/music/NoteRange.h"
#include "xjmusic/entities/music/Octave.h"
#include "xjmusic/entities/music/PitchClass.h"
#include "xjmusic/entities/music/Root.h"
#include "xjmusic/entities/music/SlashRoot.h"
#include "xjmusic/entities/music/Step.h"
#include "xjmusic/entities/music/StickyBun.h"
#include "xjmusic/entities/music/Tuning.h"
#include "xjmusic/entities/segment/Chain.h"
#include "xjmusic/entities/segment/Segment.h"
#include "xjmusic/entities/segment/SegmentChoice.h"
#include "xjmusic/entities/segment/SegmentChoiceArrangement.h"
#include "xjmusic/entities/segment/SegmentChoiceArrangementPick.h"
#include "xjmusic/entities/segment/SegmentChord.h"
#include "xjmusic/entities/segment/SegmentChordVoicing.h"
#include "xjmusic/entities/segment/SegmentEntityStore.h"
#include "xjmusic/entities/segment/SegmentMeme.h"
#include "xjmusic/entities/segment/SegmentMessage.h"
#include "xjmusic/entities/segment/SegmentMeta.h"
#include "xjmusic/fabricator/SegmentRetrospective.h"

namespace XJ {

  class Fabricator {
  private:
    static const std::string KEY_VOICE_NOTE_TEMPLATE;
    static const std::string KEY_VOICE_TRACK_TEMPLATE;
    static const std::string NAME_SEPARATOR;
    static const std::string UNKNOWN_KEY;
    Chain chain;
    TemplateConfig templateConfig;
    std::vector<TemplateBinding> templateBindings;
    ContentEntityStore sourceMaterial;
    double outputFrameRate;
    int outputChannels;
    std::map<double, std::optional<SegmentChord>> chordAtPosition;
    std::map<Instrument::Type, NoteRange> voicingNoteRange;
    std::map<SegmentChoice, ProgramSequence> sequenceForChoice;
    std::map<std::string, InstrumentAudio> preferredAudios;
    std::map<std::string, InstrumentConfig> instrumentConfigs;
    std::map<std::string, InstrumentConfig> pickInstrumentConfigs;
    std::map<std::string, int> rangeShiftOctave;
    std::map<std::string, int> targetShift;
    std::map<std::string, NoteRange> rangeForChoice;
    std::map<std::string, std::optional<Note>> rootNotesByVoicingAndChord;
    std::map<UUID, std::vector<ProgramSequenceChord>> completeChordsForProgramSequence;
    std::map<UUID, std::vector<SegmentChoiceArrangementPick>> picksForChoice;
    SegmentEntityStore store;
    SegmentRetrospective retrospective;
    std::set<UUID> boundInstrumentIds;
    std::set<UUID> boundProgramIds;
    unsigned long long startAtSystemNanoTime;
    int segmentId;
    Segment::Type type; // You need to define Segment::Type class

    std::optional<SegmentChoice> macroChoiceOfPreviousSegment;
    std::optional<SegmentChoice> mainChoiceOfPreviousSegment;

    double *microsPerBeat = nullptr;

    std::set<Instrument::Type> *distinctChordVoicingTypes = nullptr;

  public:

    /**
     * Fabrication control mode
     */
    enum class ControlMode {
      AUTO,
      MACRO,
      TAXONOMY
    };

    /**
     * Construct a Fabricator
     */
    explicit Fabricator(
        FabricatorFactory fabricatorFactory,
        FabricationEntityStore store,
        ContentStore sourceMaterial,
        int segmentId,
        double outputFrameRate,
        int outputChannels,
        std::optional<Segment::Type> overrideSegmentType
    );

    /**
     Add a message of the given type to the segment, with the given body

     @param body to include in message
     */
    void addMessage(SegmentMessage::Type messageType, std::string body);

    /**
     Add an error message to the segment, with the given body

     @param body to include in message
     */
    void addErrorMessage(std::string body);

    /**
     Add a warning message to the segment, with the given body

     @param body to include in message
     */
    void addWarningMessage(std::string body);

    /**
     Add an info message to the segment, with the given body

     @param body to include in message
     */
    void addInfoMessage(std::string body);

    /**
     Delete an entity specified by Segment id, class and id

     @param <N>       type of entity
     @param segmentId partition (segment id) of entity
     @param type      of class to delete
     @param id        to delete
     */
    void deleteEntity(int segmentId, std::string type, UUID id);

    /**
     Get arrangements for segment

     @return arrangements for segment
     */
    std::vector<SegmentChoiceArrangement> getArrangements();

    /**
     Get segment arrangements for a given choice

     @param choices to get segment arrangements for
     @return segments arrangements for the given segment choice
     */
    std::vector<SegmentChoiceArrangement> getArrangements(std::vector<SegmentChoice> choices);

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
    std::vector<SegmentChoice> getChoices();

    /**
     Determine if a choice has been previously crafted
     in one of the previous segments of the current main sequence
     <p>
     Beat and Detail choices are kept for an entire Main Program https://github.com/xjmusic/xjmusic/issues/265

     @return choice if previously made, or null if none is found
     */
    std::optional<SegmentChoice> getChoiceIfContinued(ProgramVoice voice);

    /**
     Determine if a choice has been previously crafted
     in one of the previous segments of the current main sequence

     @return choice if previously made, or null if none is found
     */
    std::vector<SegmentChoice> getChoicesIfContinued(Program::Type programType);

    /**
     Determine if a choice has been previously crafted
     in one of the previous segments of the current main sequence

     @return choice if previously made, or null if none is found
     */
    std::optional<SegmentChoice> getChoiceIfContinued(Instrument::Type instrumentType);

    /**
     Determine if a choice has been previously crafted
     in one of the previous segments of the current main sequence

     @return choice if previously made, or null if none is found
     */
    std::optional<SegmentChoice> getChoiceIfContinued(Instrument::Type instrumentType, Instrument::Mode instrumentMode);

    /**
     Get current ChordEntity for any position in Segment.
     Defaults to returning a chord based on the segment key, if nothing else is found

     @param position in segment
     @return ChordEntity
     */
    std::optional<SegmentChord> getChordAt(float position);

    /**
     fetch the main-type choice for the current segment in the chain

     @return main-type segment choice
     */
    std::optional<SegmentChoice> getCurrentMainChoice();

    /**
     Get the sequence targeted by the current main choice

     @return current main sequence
     */
    std::optional<ProgramSequence> getCurrentMainSequence();

    /**
     fetch the detail-type choice for the current segment in the chain

     @return detail-type segment choice
     */
    std::vector<SegmentChoice> getCurrentDetailChoices();

    /**
     fetch the beat-type choice for the current segment in the chain

     @return beat-type segment choice
     */
    std::optional<SegmentChoice> getCurrentBeatChoice();

    /**
     Get a list of unique voicing (instrument) types present in the voicings of the current main program's chords.

     @return set of voicing (instrument) types
     */
    std::set<Instrument::Type> getDistinctChordVoicingTypes();

    /**
     @return Seconds elapsed since fabricator was instantiated
     */
    long getElapsedMicros();

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
     @throws FabricationException if unable to compute cache key
     */
    std::string computeCacheKeyForVoiceTrack(SegmentChoiceArrangementPick pick);

    /**
     Get the Key for any given Choice, preferring its Sequence Key (bound), defaulting to the Program Key.
     <p>
     If Sequence has no key/tempo/intensity inherit from Program https://github.com/xjmusic/xjmusic/issues/246

     @param choice to get key for
     @return key of specified sequence/program via choice
     @throws FabricationException if unable to determine key of choice
     */
    Chord getKeyForChoice(SegmentChoice choice);

    /**
     fetch the macro-type choice for the previous segment in the chain

     @return macro-type segment choice, null if none found
     */
    std::optional<SegmentChoice> getMacroChoiceOfPreviousSegment();

    /**
     fetch the main-type choice for the previous segment in the chain

     @return main-type segment choice, null if none found
     */
    std::optional<SegmentChoice> getPreviousMainChoice();

    /**
     Get the configuration of the current main program

     @return main-program configuration
     @throws FabricationException on failure
     */
    ProgramConfig getCurrentMainProgramConfig();

    /**
     Get the sequence targeted by the previous main choice

     @return previous main sequence
     */
    std::optional<ProgramSequence> getPreviousMainSequence();

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
    int getNextSequenceBindingOffset(SegmentChoice choice);

    /**
     Get the Notes from a Voicing

     @param voicing to get notes of
     @return notes from voicing
     @throws FabricationException on failure
     */
    std::vector<std::string> getNotes(SegmentChordVoicing voicing);

    /**
     Get arrangement picks for segment

     @return arrangement picks for segment
     */
    std::vector<SegmentChoiceArrangementPick> getPicks();

    /**
     Get the picks for a given choice, in order of position ascending from beginning of segment

     @param choice for which to get picks
     @return picks
     */
    std::vector<SegmentChoiceArrangementPick> getPicks(SegmentChoice choice);

    /**
     Get preferred (previously chosen) instrument audios

     @return preferred audios
     */
    std::optional<InstrumentAudio> getPreferredAudio(std::string parentIdent, std::string ident);

    /**
     Get Program for any given choice

     @param choice to get program for
     @return Program for the specified choice
     */
    std::optional<Program> getProgram(SegmentChoice choice);

    /**
     Get the ProgramConfig from a given program, with fallback to program section of injected config values

     @param program to get config of
     @return ProgramConfig from a given program, with fallback values
     @throws FabricationException on failure
     */
    ProgramConfig getProgramConfig(Program program);

    /**
     Get the complete set of program sequence chords,
     ignoring ghost chords* REF by choosing the voicings with largest # of notes at that position https://github.com/xjmusic/xjmusic/issues/248
     (caches results)

     @param programSequence for which to get complete do-ghosted set of chords
     @return get complete do-ghosted set of chords for program sequence
     */
    std::vector<ProgramSequenceChord> getProgramSequenceChords(ProgramSequence programSequence);

    /**
     Get the note range for an arrangement based on all the events in its program

     @param programId      to get range of
     @param instrumentType to get range of
     @return Note range of arrangement
     @throws FabricationException on failure
     */
    NoteRange getProgramRange(UUID programId, Instrument::Type instrumentType);

    /**
     Detail craft shifts source program events into the target range https://github.com/xjmusic/xjmusic/issues/221
     <p>
     via average of delta from source low to target low, and from source high to target high, rounded to octave

     @param type        of instrument
     @param sourceRange to compute from
     @param targetRange to compute required # of octaves to shift into
     @return +/- octaves required to shift from source to target range
     @throws FabricationException on failure
     */
    int getProgramRangeShiftOctaves(Instrument::Type type, NoteRange sourceRange, NoteRange targetRange);

    /**
     Get the sequence for a given choice

     @param choice for which to get sequence
     @return sequence of choice
     */
    std::optional<ProgramSequence> getProgramSequence(SegmentChoice choice);

    /**
     Compute the target shift from a key toward a chord

     @param instrumentType to switch behavior
     @param fromChord      to compute shift from
     @param toChord        to compute shift toward
     @return computed target shift
     */
    int getProgramTargetShift(Instrument::Type instrumentType, Chord fromChord, Chord toChord);

    /**
     Get the program type of given voice

     @param voice for which to get program type
     @return program type
     @throws FabricationException on failure
     */
    Program::Type getProgramType(ProgramVoice voice);


    /**
     Get the voice type for the given voicing
     <p>
     Programs persist main chord/voicing structure sensibly
     https://github.com/xjmusic/xjmusic/issues/266

     @param voicing for which to get voice type
     @return type of voice for voicing
     @throws FabricationException on failure
     */
    Instrument::Type getProgramVoiceType(ProgramSequenceChordVoicing voicing);

    /**
     Get the lowest note present in any voicing of all the segment chord voicings for this segment and instrument type

     @param type to get voicing threshold low of
     @return low voicing threshold
     @throws FabricationException on failure
     */
    NoteRange getProgramVoicingNoteRange(Instrument::Type type);

    /**
     Randomly select any sequence

     @return randomly selected sequence
     */
    std::optional<ProgramSequence> getRandomlySelectedSequence(Program program);

    /**
     Selects one (at random) of all available patterns of a given type within a sequence. https://github.com/xjmusic/xjmusic/issues/204
     <p>
     Caches the selection, so it will always return the same output for any given input.
     <p>
     Beat fabrication composited of layered Patterns https://github.com/xjmusic/xjmusic/issues/267

     @return Pattern model, or null if no pattern of this type is found
     @throws FabricationException on failure
     */
    std::optional<ProgramSequencePattern> getRandomlySelectedPatternOfSequenceByVoiceAndType(SegmentChoice choice);

    /**
     Randomly select any sequence binding at the given offset

     @param offset to get sequence binding at
     @return randomly selected sequence binding
     */
    std::optional<ProgramSequenceBinding> getRandomlySelectedSequenceBindingAtOffset(Program program, int offset);

    /**
     Get the root note from an available set of voicings and a given chord

     @param voicingNotes available voicing notes
     @param chord        for which to seek root note among available voicings
     @return root note
     */
    std::optional<Note> getRootNoteMidRange(std::string voicingNotes, Chord chord);

    /**
     Compute using an integral
     the seconds of start for any given position in beats
     Velocity of Segment meter (beats per minute) increases linearly of the beginning of the Segment (at the previous Segment's tempo) to the end of the Segment (arriving at the current Segment's tempo, only at its end)
     <p>
     Segment should *never* be fabricated longer than its loop beats. https://github.com/xjmusic/xjmusic/issues/268
     Segment wherein tempo changes expect perfectly smooth sound of previous segment through to following segment https://github.com/xjmusic/xjmusic/issues/269

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
    std::vector<SegmentChord> getSegmentChords();

    /**
     Get all segment chord voicings

     @return segment chord voicings
     */
    std::vector<SegmentChordVoicing> getChordVoicings();

    /**
     Get all segment memes

     @return segment memes
     */
    std::vector<SegmentMeme> getSegmentMemes();

    /**
     Get the sequence for a Choice either directly (beat- and detail-type sequences), or by sequence-pattern (macro- or main-type sequences) https://github.com/xjmusic/xjmusic/issues/204
     <p>
     Beat and Detail programs are allowed to have only one (default) sequence.

     @param choice to get sequence for
     @return Sequence for choice
     @throws FabricationException on failure
     */
    std::optional<ProgramSequence> getSequence(SegmentChoice choice);

    /**
     Get the sequence pattern offset of a given Choice

     @param choice having a SequenceBinding
     @return sequence pattern offset
     */
    int getSequenceBindingOffsetForChoice(SegmentChoice choice);

    /**
     Store a sticky bun in the fabricator

     @param bun to store
     @throws JsonProcessingException on failure
     @throws FabricationException          on failure
     */
    void putStickyBun(StickyBun bun);

    /**
     Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://github.com/xjmusic/xjmusic/issues/222
     - Sticky bun is a simple coded key-value in segment meta
     --- key by pattern ID
     --- value is a comma-separated list of integers, one integer for each note in the pattern, where
     ----- tonal note is coded as a `0` value
     ----- atonal note has a random integer value generated ranging from -50 to 50
     - Rendering a pattern X voicing considers the sticky bun values
     --- the random seed for rendering the pattern will always come from the associated sticky bun
     <p>
     Sticky buns v2 persisted for each randomly selected note in the series for any given pattern https://github.com/xjmusic/xjmusic/issues/231
     - key on program-sequence-pattern-event id, persisting only the first value seen for any given event
     - super-key on program-sequence-pattern id, measuring delta from the first event seen in that pattern
     <p>
     TemplateConfig parameter stickyBunEnabled
     https://github.com/xjmusic/xjmusic/issues/251

     @param eventId for super-key
     @return sticky bun if present
     */
    std::optional<StickyBun> getStickyBun(UUID eventId);

    /**
     Get the track name for a give program sequence pattern event

     @param event for which to get track name
     @return Track name
     @throws FabricationException on failure
     */
    std::string getTrackName(ProgramSequencePatternEvent event);

    /**
     Determine type of content, e.g. initial segment in chain, or next macro-sequence

     @return macro-craft type
     @throws FabricationException on failure
     */
    Segment::Type getType();

    /**
     Get segment chord voicing for a given chord

     @param chord to get voicing for
     @return chord voicing for chord
     */
    std::optional<SegmentChordVoicing> chooseVoicing(SegmentChord chord, Instrument::Type type);

    /**
     Does the program of the specified Choice have at least N more sequence binding offsets available?

     @param choice of which to check the program for next available sequence binding offsets
     @param N      more sequence offsets to check for
     @return true if N more sequence binding offsets are available
     */
    bool hasMoreSequenceBindingOffsets(SegmentChoice choice, int N);

    /**
     Whether the current Segment Choice has one or more sequence pattern offsets
     with a higher pattern offset than the current one

     @param choice for which to check
     @return true if it has at least one more sequence pattern offset
     */
    bool hasOneMoreSequenceBindingOffset(SegmentChoice choice);

    /**
     Whether the current Segment Choice has two or more sequence pattern offsets
     with a higher pattern offset than the current two

     @param choice for which to check
     @return true if it has at least two more sequence pattern offsets
     */
    bool hasTwoMoreSequenceBindingOffsets(SegmentChoice choice);

    /**
     Whether this type of segment continues the same macro-program from the previous segment

     @return true if this segment continues the same macro-program
     @throws FabricationException on failure
     */
    bool isContinuationOfMacroProgram();

    /**
     Whether a given Instrument is directly bound to the Chain,
     where "directly" means a level more specific than Library, e.g. Program or Instrument

     @param instrument to test for direct binding
     @return true if Instrument is directly bound to chain
     */
    bool isDirectlyBound(Instrument instrument);

    /**
     Whether a given InstrumentAudio is directly bound to the Chain,
     where "directly" means a level more specific than Library, e.g. Program or InstrumentAudio

     @param instrumentAudio to test for direct binding
     @return true if InstrumentAudio is directly bound to chain
     */
    bool isDirectlyBound(InstrumentAudio instrumentAudio);

    /**
     Whether a given Program is directly bound to the Chain,
     where "directly" means a level more specific than Library, e.g. Program or Instrument

     @param program to test for direct binding
     @return true if Program is directly bound to chain
     */
    bool isDirectlyBound(Program program);

    /**
     Test if a given instrument and track name is a one-shot sample hit

     @param instrument to test
     @param trackName  to test
     @return true if this is a one-shot instrument and track name
     @throws FabricationException on failure
     */
    bool isOneShot(Instrument instrument, std::string trackName);

    /**
     Test if a given instrument is one-shot

     @param instrument to test
     @return true if this is a one-shot instrument
     @throws FabricationException on failure
     */
    bool isOneShot(Instrument instrument);

    /**
     Test if a given one-shot instrument has its cutoffs enable

     @param instrument to test
     @return true if a given one-shot instrument has its cutoffs enable
     @throws FabricationException on failure
     */
    bool isOneShotCutoffEnabled(Instrument instrument);

    /**
     is initial segment?

     @return whether this is the initial segment in a chain
     */
    bool isInitialSegment();

    /**
     Put a new Entity by type and id
     <p>
     If it's a SegmentChoice...
     Should add meme from ALL program and instrument types! https://github.com/xjmusic/xjmusic/issues/210
     - Add memes of choices to segment in order to affect further choices.
     - Add all memes of this choice, from target program, program sequence binding, or instrument if present
     - Enhances: Straightforward meme logic https://github.com/xjmusic/xjmusic/issues/270
     - Enhances: XJ should not add memes to Segment for program/instrument that was not successfully chosen https://github.com/xjmusic/xjmusic/issues/216
     <p>

     @param entity to put
     @param force overriding safeguards (e.g. choices must not violate meme stack, memes must be unique)
     @return entity successfully put
     @throws FabricationException on failure
     */
    template <typename N>
    N put(N entity, bool force);

    /**
     Set the preferred audio for a key

     @param parentIdent     for which to set
     @param ident           for which to set
     @param instrumentAudio value to set
     */
    void putPreferredAudio(std::string parentIdent, std::string ident, InstrumentAudio instrumentAudio);

    /**
     Put a key-value pair into the report
     only exports data as a sub-field of the standard content JSON

     @param key   to put
     @param value to put
     */
    void putReport(std::string key, std::map<std::string, std::string> value);

    /**
     Set the Segment.
     Any modifications to the Segment must be re-written to here
     because protobuf instances are immutable

     @param segment to set
     @throws FabricationException on failure
     */
    void updateSegment(Segment segment);

    /**
     Get the Segment Retrospective

     @return retrospective
     */
    SegmentRetrospective retrospective();

    /**
     Get the ingested source material for fabrication

     @return source material
     */
    ContentStore sourceMaterial();

    /**
     Get the number of micros per beat for the current segment

     @return micros per beat
     @throws FabricationException on failure
     */
    float getMicrosPerBeat(double tempo);

    /**
     Get the second macro sequence binding offset of a given macro program

     @param macroProgram for which to get second macro sequence binding offset
     @return second macro sequence binding offset
     */
    int getSecondMacroSequenceBindingOffset(Program macroProgram);

    /**
     @return the tempo of the current main program
     @throws FabricationException on failure
     */
    double getTempo();

    /**
     @return the meme taxonomy for the source material
     */
    MemeTaxonomy getMemeTaxonomy();
  };

} // namespace XJ

#endif //XJMUSIC_FABRICATOR_H