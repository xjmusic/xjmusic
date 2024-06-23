// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_CONTENT_STORE_H
#define XJMUSIC_CONTENT_STORE_H

#include <fstream>
#include <map>
#include <optional>
#include <vector>
#include <set>

#include "Instrument.h"
#include "InstrumentAudio.h"
#include "InstrumentMeme.h"
#include "Library.h"
#include "Program.h"
#include "ProgramMeme.h"
#include "ProgramSequence.h"
#include "ProgramSequenceBinding.h"
#include "ProgramSequenceBindingMeme.h"
#include "ProgramSequenceChord.h"
#include "ProgramSequenceChordVoicing.h"
#include "ProgramSequencePattern.h"
#include "ProgramSequencePatternEvent.h"
#include "ProgramVoice.h"
#include "ProgramVoiceTrack.h"
#include "Project.h"
#include "Template.h"
#include "TemplateBinding.h"

using namespace XJ;

#define CONTENT_STORE_CORE_HEADERS(ENTITY, ENTITIES)                     \
  std::optional<const ENTITY *> get##ENTITY(const UUID &id);             \
  std::set<const ENTITY *> get##ENTITIES();                              \
  ContentEntityStore set##ENTITIES(const std::set<ENTITY> &entities);    \
  ENTITY put(const ENTITY &entity);                                      \

namespace XJ {

  class ContentEntityStore {
    std::map<UUID, Instrument> instruments{};
    std::map<UUID, InstrumentAudio> instrumentAudios{};
    std::map<UUID, InstrumentMeme> instrumentMemes{};
    std::map<UUID, Library> libraries{};
    std::map<UUID, Program> programs{};
    std::map<UUID, ProgramMeme> programMemes{};
    std::map<UUID, ProgramSequence> programSequences{};
    std::map<UUID, ProgramSequenceBinding> programSequenceBindings{};
    std::map<UUID, ProgramSequenceBindingMeme> programSequenceBindingMemes{};
    std::map<UUID, ProgramSequenceChord> programSequenceChords{};
    std::map<UUID, ProgramSequenceChordVoicing> programSequenceChordVoicings{};
    std::map<UUID, ProgramSequencePattern> programSequencePatterns{};
    std::map<UUID, ProgramSequencePatternEvent> programSequencePatternEvents{};
    std::map<UUID, ProgramVoice> programVoices{};
    std::map<UUID, ProgramVoiceTrack> programVoiceTracks{};
    std::map<UUID, Project> projects{};
    std::map<UUID, Template> templates{};
    std::map<UUID, TemplateBinding> templateBindings{};

  public:
    CONTENT_STORE_CORE_HEADERS(Instrument, Instruments)

    CONTENT_STORE_CORE_HEADERS(InstrumentAudio, InstrumentAudios)

    CONTENT_STORE_CORE_HEADERS(InstrumentMeme, InstrumentMemes)

    CONTENT_STORE_CORE_HEADERS(Library, Libraries)

    CONTENT_STORE_CORE_HEADERS(Program, Programs)

    CONTENT_STORE_CORE_HEADERS(ProgramMeme, ProgramMemes)

    CONTENT_STORE_CORE_HEADERS(ProgramSequence, ProgramSequences)

    CONTENT_STORE_CORE_HEADERS(ProgramSequenceBinding, ProgramSequenceBindings)

    CONTENT_STORE_CORE_HEADERS(ProgramSequenceBindingMeme, ProgramSequenceBindingMemes)

    CONTENT_STORE_CORE_HEADERS(ProgramSequenceChord, ProgramSequenceChords)

    CONTENT_STORE_CORE_HEADERS(ProgramSequenceChordVoicing, ProgramSequenceChordVoicings)

    CONTENT_STORE_CORE_HEADERS(ProgramSequencePattern, ProgramSequencePatterns)

    CONTENT_STORE_CORE_HEADERS(ProgramSequencePatternEvent, ProgramSequencePatternEvents)

    CONTENT_STORE_CORE_HEADERS(ProgramVoice, ProgramVoices)

    CONTENT_STORE_CORE_HEADERS(ProgramVoiceTrack, ProgramVoiceTracks)

    CONTENT_STORE_CORE_HEADERS(Project, Projects)

    CONTENT_STORE_CORE_HEADERS(Template, Templates)

    CONTENT_STORE_CORE_HEADERS(TemplateBinding, TemplateBindings)

    /** Default constructor */
    explicit ContentEntityStore();

    /**
     * Deserialize a ContentEntityStore object from a JSON file
     * @param file  The JSON file to deserialize
     * @return      The deserialized ContentEntityStore object
     */
    explicit ContentEntityStore(std::ifstream &file);

    /**
     * Deserialize a ContentEntityStore object from a JSON string
     * @param input  The JSON string to deserialize
     * @return       The deserialized ContentEntityStore object
     */
    explicit ContentEntityStore(std::string &input);

    /**
     * Get the Project (there should be only one)
     * @return  The Project
     */
    std::optional<Project *> getProject();

    /**
     * Get Program track for a given program event
     * @param event to get program track of
     * @return Program track for the given program event
     */
    std::optional<const ProgramVoiceTrack *> getTrackOfEvent(const ProgramSequencePatternEvent *event);

    /**
     * Get Program voice for a given program event
     * @param event to get program voice of
     * @return Program voice for the given program event
     */
    std::optional<const ProgramVoice *> getVoiceOfEvent(const ProgramSequencePatternEvent *event);

    /**
     * Get the instrument type for the given event
     * @param event for which to get instrument type
     * @return instrument type
     */
    Instrument::Type getInstrumentTypeOfEvent(const ProgramSequencePatternEvent *event);

    /**
     * Whether the content contains instruments of the given type
     * @param type of instrument for which to search
     * @return true if present
     */
    bool hasInstrumentsOfType(Instrument::Type type);

    /**
     * Whether the content contains instruments of the given mode
     * @param mode of instrument for which to search
     * @return true if present
     */
    bool hasInstrumentsOfMode(Instrument::Mode mode);

    /**
     * Whether the content contains instruments of the given type
     * @param type of instrument for which to search
     * @param mode of instrument for which to search
     * @return true if present
     */
    bool hasInstrumentsOfTypeAndMode(Instrument::Type type, Instrument::Mode mode);

    /**
     * Get all available sequence pattern offsets of a given sequence, sorted of offset
     * @param sequenceBinding for which to get available sequence pattern offsets
     * @return collection of available sequence pattern offsets
     */
    std::vector<int> getAvailableOffsets(const ProgramSequenceBinding *sequenceBinding) const;

    /**
   * Get all Audios for a given instrument id
   * @param id of instrument for which to get audios
   * @return audios of instrument id
   */
    std::set<const InstrumentAudio *> getAudiosOfInstrument(const UUID &id) const;

    /**
    * Get all InstrumentAudios for a given Instrument
    * @param instrument for which to get audios
    * @return audios for instrument
    */
    std::set<const InstrumentAudio *> getAudiosOfInstrument(const Instrument *instrument) const;

    /**
     * Get the sequence bindings for a given sequence
     * @param sequence for which to get bindings
     * @return bindings of sequence
     */
    std::vector<const ProgramSequenceBinding *> getBindingsOfSequence(const ProgramSequence *sequence) const;

    /**
      * Get the sequence bindings for a given sequence id
      * @param sequenceId for which to get bindings
      * @return bindings of sequence
      */
    std::vector<const ProgramSequenceBinding *> getBindingsOfSequence(const UUID &sequenceId) const;

    /**
     * Get the sequence binding memes for a given program
     * @param program for which to get sequence binding memes
     * @return sequence binding memes of program
     */
    std::set<const ProgramSequenceBindingMeme *> getSequenceBindingMemesOfProgram(const Program *program) const;

    /**
     * Get the sequence binding memes for a given program id
     * @param programId for which to get sequence binding memes
     * @return sequence binding memes of program
     */
    std::set<const ProgramSequenceBindingMeme *> getSequenceBindingMemesOfProgram(const UUID &programId) const;

    /**
     * Get sequence bindings at a specified offset.
     * If the target offset is not found in the chosen Main Program,
     * we'll find the nearest matching offset, and return all bindings at that offset.
     * <p>
     * Chain should always be able to determine main sequence binding offset https://www.pivotaltracker.com/story/show/177052278
     * @param program        for which to get sequence bindings
     * @param offset         to get sequence bindings at
     * @param includeNearest whether to include the nearest offset if the target offset is not found
     * @return sequence bindings at offset
     */
    std::vector<const ProgramSequenceBinding *>
    getBindingsAtOffsetOfProgram(const Program *program, int offset, bool includeNearest) const;

    /**
     * Get sequence bindings at a specified offset.
     * If the target offset is not found in the chosen Main Program,
     * we'll find the nearest matching offset, and return all bindings at that offset.
     * <p>
     * Chain should always be able to determine main sequence binding offset https://www.pivotaltracker.com/story/show/177052278
     * @param programId      for which to get sequence bindings
     * @param offset         to get sequence bindings at
     * @param includeNearest whether to include the nearest offset if the target offset is not found
     * @return sequence bindings at offset
     */
    std::vector<const ProgramSequenceBinding *>
    getBindingsAtOffsetOfProgram(const UUID &programId, int offset, bool includeNearest) const;

    /**
     * Get all ProgramSequenceChords for a given Sequence
     * @param sequence for which to get chords
     * @return chords of sequence
     */
    std::vector<const ProgramSequenceChord *> getChordsOfSequence(const ProgramSequence *sequence) const;

    /**
     * Get all ProgramSequenceChords for a given Sequence
     * @param programSequenceId for which to get chords
     * @return chords of sequence
     */
    std::vector<const ProgramSequenceChord *> getChordsOfSequence(const UUID &programSequenceId) const;

    /**
     * Get events for a given program pattern, sorted of position
     * @param pattern for which to get events
     * @return events for given program pattern
     */
    std::vector<const ProgramSequencePatternEvent *> getEventsOfPattern(const ProgramSequencePattern *pattern) const;

    /**
     * Get events for a given program sequence pattern id, sorted of position
     * @param patternId for which to get events
     * @return events for given pattern id
     */
    std::vector<const ProgramSequencePatternEvent *> getEventsOfPattern(const UUID &patternId) const;

    /**
     * Get all ProgramSequencePatterns for a given sequence and voice
     * @return ProgramSequencePatterns for sequence and voice
     */
    std::set<const ProgramSequencePattern *>
    getPatternsOfSequenceAndVoice(const UUID &programSequenceId, const UUID &programVoiceId) const;

    /**
     * Get all bindings for the given template id
     * @return template bindings
     */
    std::set<const TemplateBinding *> getBindingsOfTemplate(const UUID &templateId) const;

    /**
     * Get patterns for a given program
     * @param programId for which to get patterns
     * @return patterns for given program
     */
    std::set<const ProgramSequencePattern *> getSequencePatternsOfProgram(const UUID &programId) const;

    /**
     * Get patterns for a given program pattern, sorted of position
     * @param program for which to get patterns
     * @return patterns for given program pattern
     */
    std::set<const ProgramSequencePattern *> getSequencePatternsOfProgram(const Program *program) const;

    /**
     * Get events for a given program
     * @param programId for which to get events
     * @return events for given program
     */
    std::vector<const ProgramSequencePatternEvent *> getSequencePatternEventsOfProgram(const UUID &programId) const;

    /**
     * Get events for a given program track, sorted of position
     * @param track for which to get events
     * @return events for given program track
     */
    std::vector<const ProgramSequencePatternEvent *> getEventsOfTrack(const ProgramVoiceTrack *track) const;

    /**
     * Get events for a given program voice track id, sorted of position
     * @param trackId for which to get events
     * @return events for given track id
     */
    std::vector<const ProgramSequencePatternEvent *> getEventsOfTrack(const UUID &trackId) const;

    /**
     * Get events for a given program pattern and track, sorted of position
     * @param pattern for which to get events
     * @param track   for which to get events
     * @return events for given program pattern
     */
    std::vector<const ProgramSequencePatternEvent *>
    getEventsOfPatternAndTrack(const ProgramSequencePattern *pattern, const ProgramVoiceTrack *track) const;

    /**
     * Get events for a given program sequence pattern id and track id, sorted of position
     * @param patternId for which to get events
     * @param trackId   for which to get events
     * @return events for given pattern id
     */
    std::vector<const ProgramSequencePatternEvent *>
    getEventsOfPatternAndTrack(const UUID &patternId, const UUID &trackId) const;

    /**
     * Get all instrument audios for the given instrument types and modes
     * @param types of instrument
     * @param modes of instrument
     * @return all audios for instrument type
     */
    std::set<const InstrumentAudio *> getAudiosOfInstrumentTypesAndModes(const std::set<Instrument::Type> &types,
                                                                         const std::set<Instrument::Mode> &modes) const;

    /**
     * Get all instrument audios for the given instrument types
     * @param types of instrument
     * @return all audios for instrument type
     */
    std::set<const InstrumentAudio *> getAudiosOfInstrumentTypes(const std::set<Instrument::Type> &types) const;

    /**
     * Get a collection of all instruments of a particular type for ingest
     * @return collection of instruments
     */
    std::set<const Instrument *> getInstrumentsOfType(const Instrument::Type &type) const;

    /**
     * Get a collection of all instruments of particular types and modes
     * @param types of instrument; empty list is a wildcard
     * @param modes of instrument; empty list is a wildcard
     * @return collection of instruments
     */
    std::set<const Instrument *> getInstrumentsOfTypesAndModes(const std::set<Instrument::Type> &types,
                                                               const std::set<Instrument::Mode> &modes) const;

    /**
     * Get a collection of all instruments of particular types
     * @param types of instrument; empty list is a wildcard
     * @return collection of instruments
     */
    std::set<const Instrument *> getInstrumentsOfTypes(const std::set<Instrument::Type> &types) const;

    /**
     * Get memes of instrument
     * @param instrumentId for which to get memes
     * @return memes of instrument
     */
    std::set<const InstrumentMeme *> getMemesOfInstrument(const UUID &instrumentId) const;

    /**
     * Get a collection of all instruments of the given library
     * @param library for which to get instruments
     * @return collection of instruments
     */
    std::set<const Instrument *> getInstrumentsOfLibrary(const Library *library) const;

    /**
     * Get a collection of all instruments of the given library id
     * @param libraryId for which to get instruments
     * @return collection of instruments
     */
    std::set<const Instrument *> getInstrumentsOfLibrary(const UUID &libraryId) const;

    /**
     * Get the instrument type for the given audio id
     * @param instrumentAudioId for which to get instrument type
     * @return instrument type
     * @throws RuntimeException on failure
     */
    Instrument::Type getInstrumentTypeOfAudio(const UUID &instrumentAudioId) const;

    /**
     * Get memes of program
     * @param programId for which to get memes
     * @return memes of program
     */
    std::set<const ProgramMeme *> getMemesOfProgram(const UUID &programId) const;


    /**
     * Fetch all memes for a given program at sequence binding offset 0
     * @return collection of sequence memes
     */
    std::set<std::string> getMemesAtBeginning(const Program *program) const;

    /**
     * Get all program sequence binding memes for program sequence binding
     * @param programSequenceBinding for which to get memes
     * @return memes
     */
    std::set<const ProgramSequenceBindingMeme *>
    getMemesOfSequenceBinding(const ProgramSequenceBinding *programSequenceBinding) const;

    /**
     * Get all program sequence binding memes for program sequence binding
     * @param programSequenceBindingId for which to get memes
     * @return memes
     */
    std::set<const ProgramSequenceBindingMeme *> getMemesOfSequenceBinding(const UUID &programSequenceBindingId) const;

    /**
     * Get the pattern id for an event id
     * @param eventId for which to get pattern
     * @return pattern id
     */
    UUID getPatternIdOfEvent(const UUID &eventId);

    /**
     * Get all patterns for a sequence
     * @param sequence for which to get patterns
     * @return patterns of sequence
     */
    std::set<const ProgramSequencePattern *> getPatternsOfSequence(const ProgramSequence *sequence);

    /**
     * Get all patterns for a sequence ID
     * @param sequence for which to get patterns
     * @return patterns of sequence
     */
    std::set<const ProgramSequencePattern *> getPatternsOfSequence(const UUID &sequence);

    /**
     * Get all patterns for a voice
     * @param voice for which to get patterns
     * @return patterns of voice
     */
    std::set<const ProgramSequencePattern *> getPatternsOfVoice(const ProgramVoice *voice) const;

    /**
     * Get all patterns for a voice ID
     * @param voiceId for which to get patterns
     * @return patterns of voice
     */
    std::set<const ProgramSequencePattern *> getPatternsOfVoice(const UUID &voiceId) const;

    /**
     * Get a collection of all programs of the given library
     * @param library for which to get programs
     * @return collection of programs
     */
    std::set<const Program *> getProgramsOfLibrary(const Library *library) const;

    /**
     * Get a collection of all programs of the given library id
     * @param libraryId for which to get programs
     * @return collection of programs
     */
    std::set<const Program *> getProgramsOfLibrary(const UUID &libraryId) const;

    /**
     * Get a collection of all sequences of a particular type for ingest
     * @return collection of sequences
     */
    std::set<const Program *> getProgramsOfType(Program::Type type) const;

    /**
     * Get the program sequence for a given program sequence binding
     * @param sequenceBinding for which to get program sequence
     * @return program sequence for the given program sequence binding
     */
    std::optional<const ProgramSequence *> getSequenceOfBinding(const ProgramSequenceBinding *sequenceBinding);

    /**
     * Get all ProgramSequences
     * @param programId to search for sequences
     * @return ProgramSequences
     */
    std::set<const ProgramSequence *> getSequencesOfProgram(const UUID &programId) const;

    /**
     * Get all sequence bindings for the given program
     * @param programId for which to get bindings
     * @return sequence bindings
     */
    std::vector<const ProgramSequenceBinding *> getSequenceBindingsOfProgram(const UUID &programId) const;

    /**
     * Get all ProgramSequenceChords
     * @return ProgramSequenceChords
     */
    std::vector<const ProgramSequenceChord *> getSequenceChordsOfProgram(const UUID &programId) const;

    /**
     * Get program sequence chord voicings
     * @param programId to get sequence chord voicings of
     * @return sequence chord voicings for program
     */
    std::set<const ProgramSequenceChordVoicing *> getSequenceChordVoicingsOfProgram(const UUID &programId) const;

    /**
     * Get all program voice tracks for the given program id
     * @param programId for which to get tracks
     * @return tracks for program
     */
    std::set<const ProgramVoiceTrack *> getTracksOfProgram(const UUID &programId) const;

    /**
     * Get all program voice tracks for the given program type
     * @param type of program
     * @return all voice tracks for program type
     */
    std::set<const ProgramVoiceTrack *> getTracksOfProgramType(Program::Type type) const;

    /**
     * Get all Program Voice Tracks for the given Voice
     * @param voice for which to get tracks
     * @return tracks for voice
     */
    std::set<const ProgramVoiceTrack *> getTracksOfVoice(const ProgramVoice *voice) const;

    /**
     * Get all Program Voice Tracks for the given Voice ID
     * @param voiceId for which to get tracks
     * @return tracks for voice
     */
    std::set<const ProgramVoiceTrack *> getTracksOfVoice(const UUID &voiceId) const;

    /**
     * Get all track names for a given program voice
     * @param voice for which to get track names
     * @return names of tracks for the given voice
     */
    std::set<std::string> getTrackNamesOfVoice(const ProgramVoice *voice) const;

    /**
     * Get all ProgramSequenceChordVoicings for a given Sequence Chord
     * @param chord for which to get voicings
     * @return chords of sequence
     */
    std::set<const ProgramSequenceChordVoicing *> getVoicingsOfChord(const ProgramSequenceChord *chord) const;

    /**
     * Get all ProgramSequenceChordVoicings for a given Sequence Chord ID
     * @param chordId for which to get voicings
     * @return chords of sequence
     */
    std::set<const ProgramSequenceChordVoicing *> getVoicingsOfChord(const UUID &chordId) const;

    /**
     * Get all ProgramSequenceChordVoicings for a given Sequence Chord ID and Voice ID
     * @param chord for which to get voicings
     * @param voice for which to get voicings
     * @return chords of sequence
     */
    std::set<const ProgramSequenceChordVoicing *>
    getVoicingsOfChordAndVoice(const ProgramSequenceChord *chord, const ProgramVoice *voice) const;

    /**
     * Get all ProgramSequenceChordVoicings for a given Sequence Chord ID and Voice ID
     * @param chordId for which to get voicings
     * @param voiceId for which to get voicings
     * @return chords of sequence
     */
    std::set<const ProgramSequenceChordVoicing *>
    getVoicingsOfChordAndVoice(const UUID &chordId, const UUID &voiceId) const;

    /**
     * Get all program voices for a given program
     * @param program for which to get program voices
     * @return program voices for the given program
     */
    std::set<const ProgramVoice *> getVoicesOfProgram(const Program *program) const;

    /**
     * Get all program voices for a given program
     * @param programId for which to get program voices
     * @return program voices for the given program
     */
    std::set<const ProgramVoice *> getVoicesOfProgram(const UUID &programId) const;

    /**
     * Get a new ContentEntityStore object for a specific template
     * - Only include entities bound to the template
     * - Only include entities in a published state (for entities with a state)
     */
    ContentEntityStore forTemplate(const Template *tmpl);

    /**
     * Clear the content store
     */
    void clear();
  };

}// namespace XJ

#endif//XJMUSIC_CONTENT_STORE_H
