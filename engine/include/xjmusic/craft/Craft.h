// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJ_MUSIC_CRAFT_H
#define XJ_MUSIC_CRAFT_H

#include <cmath>
#include <functional>
#include <map>
#include <optional>
#include <set>
#include <string>
#include <utility>
#include <vector>

#include "xjmusic/fabricator/FabricationWrapper.h"
#include "xjmusic/fabricator/Fabricator.h"
#include "xjmusic/fabricator/MarbleBag.h"
#include "xjmusic/fabricator/NotePicker.h"

namespace XJ {

  /**
 Arrangement of Segment Events is a common foundation for all craft
 */
  class Craft : protected FabricationWrapper {
  protected:
    std::map<std::string, int> deltaIns;
    std::map<std::string, int> deltaOuts;
    std::set<Instrument::Type> finalizeAudioLengthsForInstrumentTypes;

  public:
    /**
     Instrument provider to make some code more portable
     */
    class InstrumentProvider {
    public:
      virtual ~InstrumentProvider() = default;

      virtual std::optional<Instrument> get(const ProgramVoice *voice);
    };

    /**
     * Lambda instrument provider to create an instrument provider from a lambda
     */
    class LambdaInstrumentProvider final : public InstrumentProvider {
    public:
      explicit LambdaInstrumentProvider(std::function<std::optional<Instrument>(ProgramVoice)> func) : func_(
          std::move(func)) {}

      std::optional<Instrument> get(const ProgramVoice *voice) override {
        return func_(*voice);
      }

    private:
      std::function<std::optional<Instrument>(ProgramVoice)> func_;
    };

    /**
     Class to get a comparable string index based on any given choice, e.g. it's voice name or instrument type
     */
    class ChoiceIndexProvider {
    public:
      virtual ~ChoiceIndexProvider() = default;

      virtual std::string get(const SegmentChoice *choice);
    };

    /**
     * Lambda choiceIndex provider to create an choiceIndex provider from a lambda
     */
    class LambdaChoiceIndexProvider : public ChoiceIndexProvider {
    public:
      explicit LambdaChoiceIndexProvider(std::function<std::string(SegmentChoice)> func) : func_(std::move(func)) {}

      std::string get(const SegmentChoice *choice) override {
        return func_(*choice);
      }

    private:
      std::function<std::string(SegmentChoice)> func_;
    };

    /**
     Representation of a section of an arrangement, having a chord, beginning position and end position
     */
    class Section {
    public:
      const SegmentChord *chord;
      float fromPos{};
      float toPos{};
    };

    ChoiceIndexProvider choiceIndexProvider = {};

    /**
     Must extend this class and inject
  
     @param fabricator internal
     */
    explicit Craft(Fabricator *fabricator);

    /**
     Whether the current segment contains the delta in for the given choice
  
     @param choice to test whether the current segment contains this choice delta in
     @return true if the current segment contains the given choice's delta in
     */
    [[nodiscard]] bool isIntroSegment(const SegmentChoice *choice) const;

    /**
     Whether the current segment contains the delta out for the given choice
  
     @param choice to test whether the current segment contains this choice delta out
     @return true if the current segment contains the given choice's delta out
     */
    [[nodiscard]] bool isOutroSegment(const SegmentChoice *choice) const;

    /**
     Whether the given choice is silent during the entire segment
  
     @param choice to test for silence
     @return true if choice is silent the entire segment
     */
    [[nodiscard]] bool isSilentEntireSegment(const SegmentChoice *choice) const;

    /**
     Whether the given choice is fully active during the current segment
  
     @param choice to test for activation
     @return true if this choice is active the entire time
     */
    [[nodiscard]] bool isActiveEntireSegment(const SegmentChoice *choice) const;

    /**
     Craft the arrangement for a given voice
     <p>
     Choice inertia
     https://github.com/xjmusic/xjmusic/issues/242
     Perform the inertia analysis, and determine whether they actually use the new choice or not
     IMPORTANT** If the previously chosen instruments are for the previous main program as the current segment,
     the inertia scores are not actually added to the regular scores or used to make choices--
     this would prevent new choices from being made. **Inertia must be its own layer of calculation,
     a question of whether the choices will be followed or whether the inertia will be followed**
     thus the new choices have been made, we know *where* we're going next,
     but we aren't actually using them yet until we hit the next main program in full, N segments later.
     <p>
     Ends with a pass to set the actual length of one-shot audio picks
     One-shot instruments cut off when other notes played with same instrument, or at end of segment https://github.com/xjmusic/xjmusic/issues/243

     @param tempo         of main program
     @param choice        to craft arrangements for
     @param defaultAtonal whether to default to a single atonal note, if no voicings are available
     @on failure
     */
    void craftNoteEventArrangements(float tempo, const SegmentChoice *choice, bool defaultAtonal);

    /**
     Precompute all deltas for a given program. This is where deltaIns and deltaOuts values come from.
     <p>
     Precompute deltas dynamically based on whatever is extending the arranger--
     Don't have anything in this class that's proprietary to beat or detail-- abstract that out into provider interfaces
     <p>
     Segments have intensity arcs; automate mixer layers in and out of each main program
     https://github.com/xjmusic/xjmusic/issues/233
     <p>
     Shift deltas so 2x more time is spent on construction than deconstruction
     <p>
     Vary the high plateau between delta in and out across layers

     @on failure
     */
    void precomputeDeltas(
        const std::function<bool(const SegmentChoice *)> &choiceFilter,
        const ChoiceIndexProvider &setChoiceIndexProvider,
        const std::vector<std::string> &layersInCraftOrder,
        const std::set<std::string> &layerPrioritizationSearches,
        const int numLayersIncoming);

    /**
     Whether a position is in the given bounds

     @param floor   of boundary
     @param ceiling of boundary
     @param value   to test for within bounds
     @return true if value is within bounds (inclusive)
     */
    static bool inBounds(int floor, int ceiling, float value);

    /**
     Whether a given choice has deltaIn unlimited

     @param choice to test
     @return true if deltaIn is unlimited
     */
    static bool isUnlimitedIn(const SegmentChoice &choice);

    /**
     Whether a given choice has deltaOut unlimited

     @param choice to test
     @return true if deltaOut is unlimited
     */
    static bool isUnlimitedOut(const SegmentChoice &choice);

    /**
     Choose a fresh program based on a set of memes

     @param programType to choose
     @param voicingType (optional) for which to choose a program for-- and the program is required to have this type of voice
     @return Program
     */
    [[nodiscard]] std::optional<const Program *>
    chooseFreshProgram(Program::Type programType, std::optional<Instrument::Type> voicingType) const;

    /**
     Choose instrument
     <p>
     Choose drum instrument to fulfill beat program event names https://github.com/xjmusic/xjmusic/issues/253

     @param type              of instrument to choose from
     @param requireEventNames instrument candidates are required to have event names https://github.com/xjmusic/xjmusic/issues/253
     @return Instrument
     */
    [[nodiscard]] std::optional<const Instrument *>
    chooseFreshInstrument(Instrument::Type type, const std::set<std::string> &requireEventNames) const;

    /**
     Percussion-type Loop-mode instrument audios are chosen in order of priority
     https://github.com/xjmusic/xjmusic/issues/255
     <p>
     Choose drum instrument to fulfill beat program event names https://github.com/xjmusic/xjmusic/issues/253

     @param types           of instrument to choose from
     @param modes           of instrument to choose from
     @param avoidIds        to avoid, or empty list
     @param preferredEvents instrument candidates are required to have event names https://github.com/xjmusic/xjmusic/issues/253
     @return Instrument
     */
    [[nodiscard]] std::optional<const InstrumentAudio *>
    chooseFreshInstrumentAudio(
        const std::set<Instrument::Type> &types,
        const std::set<Instrument::Mode> &modes,
        const std::set<UUID> &avoidIds,
        const std::set<std::string> &preferredEvents) const;

    /**
     Select a new random instrument audio based on a pattern event

     @param instrument of which to score available audios, and make a selection
     @param chord      to match
     @return matched new audio
     */
    [[nodiscard]] std::optional<const InstrumentAudio *>
    selectNewChordPartInstrumentAudio(const Instrument *instrument, const Chord &chord) const;

    /**
     Select audios for the given instrument

     @param instrument for which to pick audio
     @return drum-type Instrument
     */
    [[nodiscard]] std::set<const InstrumentAudio *> selectGeneralAudioIntensityLayers(const Instrument *instrument) const;

  protected:
    /**
     Filter only the directly bound programs

     @param programs to filter
     @return filtered programs
     */
    [[nodiscard]] std::set<const Program *> programsDirectlyBound(const std::set<const Program *> &programs) const;

    /**
     Filter only the published programs

     @param programs to filter
     @return filtered programs
     */
    static std::set<const Program *> programsPublished(const std::set<const Program *> &programs);

    /**
     Filter only the directly bound instruments

     @param instruments to filter
     @return filtered instruments
     */
    [[nodiscard]] std::set<const Instrument *>
    instrumentsDirectlyBound(const std::set<const Instrument *> &instruments) const;

    /**
     Filter only the published instruments

     @param instruments to filter
     @return filtered instruments
     */
    [[nodiscard]] static std::set<const Instrument *>
    instrumentsPublished(const std::set<const Instrument *> &instruments);

    /**
     Filter only the directly bound instrumentAudios

     @param instrumentAudios to filter
     @return filtered instrumentAudios
     */
    [[nodiscard]] std::set<const InstrumentAudio *>
    audiosDirectlyBound(const std::set<const InstrumentAudio *> &instrumentAudios) const;

    /**
     Filter only the published instrumentAudios

     @param instrumentAudios to filter
     @return filtered instrumentAudios
     */
    [[nodiscard]] std::set<const InstrumentAudio *>
    audiosPublished(const std::set<const InstrumentAudio *> &instrumentAudios) const;

    /**
     Compute a mute value, based on the template config

     @param instrumentType of instrument for which to compute mute
     @return true if muted
     */
    [[nodiscard]] bool computeMute(Instrument::Type instrumentType) const;

    /**
     Pick the transition

     @param arrangement          to pick
     @param audio                to pick
     @param startAtSegmentMicros to pick
     @param lengthMicros         to pick
     @param event                to pick
     @on failure
     */
    void pickInstrumentAudio(
        const SegmentChoiceArrangement *arrangement,
        const InstrumentAudio *audio,
        long startAtSegmentMicros,
        long lengthMicros,
        const std::string& event) const;

    /**
     Pick one audio for each desired intensity level, by layering the audios by intensity and picking one from each layer.
     Divide the audios into layers (ergo grouping them by intensity ascending) and pick one audio per layer.
  
     @param audios from which to pick layers
     @param layers number of layers to pick
     @return picked audios
     */
    [[nodiscard]] std::set<const InstrumentAudio *>
    selectAudioIntensityLayers(std::set<const InstrumentAudio *> audios, const int layers) const;

    /**
     Segments have intensity arcs; automate mixer layers in and out of each main program
     https://github.com/xjmusic/xjmusic/issues/233
  
     @param tempo              of main program
     @param sequence           for which to craft choices
     @param voices             for which to craft choices
     @param instrumentProvider from which to get instruments
     @on failure
     */
    void craftNoteEvents(
        float tempo,
        const ProgramSequence *sequence,
        const std::set<const ProgramVoice *> &voices,
        InstrumentProvider *instrumentProvider);

    /**
     Chord instrument mode
     https://github.com/xjmusic/xjmusic/issues/235
  
     @param tempo      of main program
     @param instrument for which to craft choices
     @on failure
     */
    void craftChordParts(float tempo, const Instrument *instrument);

    /**
     Chord instrument mode
     https://github.com/xjmusic/xjmusic/issues/235
  
     @param tempo      of main program
     @param instrument chosen
     @param choice     for which to craft chord parts
     @on failure
     */
    void craftChordParts(float tempo, const Instrument *instrument, const SegmentChoice *choice);

    /**
     Event instrument mode
  
     @param tempo      of main program
     @param instrument for which to craft choices
     @param program    for which to craft choices
     @on failure
     */
    void craftEventParts(float tempo, const Instrument *instrument, const Program *program);

    /**
     Get the delta in for the given voice
  
     @param choice for which to get delta in
     @return delta in for given voice
     */
    int computeDeltaIn(const SegmentChoice *choice);

    /**
     Get the delta out for the given voice
  
     @param choice for which to get delta out
     @return delta out for given voice
     */
    int computeDeltaOut(const SegmentChoice *choice);

  private:
    std::random_device rd;
    std::mt19937 gen{rd()};

    /**
     Iterate through all the chords of a sequence and arrange events per each chord
     <p>
     Detail programs can be made to repeat every chord change https://github.com/xjmusic/xjmusic/issues/244

     @param tempo         of main program
     @param choice        from which to craft events
     @param range         used to keep voicing in the tightest range possible
     @param defaultAtonal whether to default to a single atonal note, if no voicings are available
     @on failure
     */
    void
    craftNoteEventSectionRestartingEachChord(float tempo, const SegmentChoice *choice, NoteRange *range,
                                             bool defaultAtonal) const;

    /**
     Compute the segment chord sections

     @return sections in order of position ascending
     */
    [[nodiscard]] std::vector<Section> computeSections() const;

    /**
     Craft events for a section of one detail voice

     @param tempo         of main program
     @param choice        from which to craft events
     @param fromPos       position (in beats)
     @param maxPos        position (in beats)
     @param range         used to keep voicing in the tightest range possible
     @param defaultAtonal whether to default to a single atonal note, if no voicings are available
     @on failure
     */
    void craftNoteEventSection(
        float tempo,
        const SegmentChoice *choice,
        float fromPos,
        float maxPos,
        NoteRange *range,
        bool defaultAtonal) const;

    /**
     Craft the voice events of a single pattern.
     Artist during craft audio selection wants randomness of outro audio selection to gently ramp of zero to N over the course of the outro.

     @param tempo         of main program
     @param choice        to craft events for
     @param pattern       to source events
     @param fromPosition  to write events to segment
     @param toPosition    to write events to segment
     @param range         used to keep voicing in the tightest range possible
     @param defaultAtonal whether to default to a single atonal note, if no voicings are available
     @return deltaPos of start, after crafting this batch of pattern events
     */
    float craftPatternEvents(
        float tempo,
        const SegmentChoice *choice,
        const ProgramSequencePattern *pattern,
        float fromPosition,
        float toPosition,
        NoteRange *range,
        bool defaultAtonal) const;

    /**
     of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
     pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement

     @param tempo         of main program
     @param instrument    to pick audio for
     @param choice        to pick notes for
     @param arrangement   to pick notes for
     @param fromPosition  to pick notes for
     @param toPosition    to pick notes for
     @param event         to pick audio for
     @param range         used to keep voicing in the tightest range possible
     @param defaultAtonal whether to default to a single atonal note, if no voicings are available
     */
    void pickNotesAndInstrumentAudioForEvent(
        const float tempo,
        const Instrument *instrument,
        const SegmentChoice *choice,
        const SegmentChoiceArrangement *arrangement,
        const float fromPosition,
        const float toPosition,
        const ProgramSequencePatternEvent *event,
        NoteRange *range,
        const bool defaultAtonal) const;


    /**
     Ends with a pass to set the actual length of one-shot audio picks
     One-shot instruments cut off when other notes played with same instrument, or at end of segment https://github.com/xjmusic/xjmusic/issues/243
  
     @param choice for which to finalize length of one-shot audio picks
     */
    void finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks(const SegmentChoice *choice);

    /**
     Compute the volume ratio of a picked note
  
     @param choice          for which to compute volume ratio
     @param segmentPosition at which to compute
     @return volume ratio
     */
    [[nodiscard]] float computeVolumeRatioForPickedNote(const SegmentChoice &choice, float segmentPosition) const;

    /**
     Pick note based on instrument type, voice event, transposition and current chord
     <p>
     XJ should choose correct instrument note based on detail program note
  
     @param instrumentType  comprising audios
     @param choice          for reference
     @param event           of program to pick instrument note for
     @param rawSegmentChord to use for interpreting the voicing
     @param voicing         to choose a note from
     @param optimalRange    used to keep voicing in the tightest range possible
     @return note picked from the available voicing
     */
    [[nodiscard]] std::set<std::string> pickNotesForEvent(
        Instrument::Type instrumentType,
        const SegmentChoice *choice,
        const ProgramSequencePatternEvent *event,
        const SegmentChord *rawSegmentChord,
        const SegmentChordVoicing *voicing,
        NoteRange *optimalRange) const;

    /**
     XJ has a serviceable voicing algorithm https://github.com/xjmusic/xjmusic/issues/221
     <p>
     Artist can edit comma-separated notes into detail program events https://github.com/xjmusic/xjmusic/issues/246
     <p>
     of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
     pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement
  
     @param note                     to pick audio for
     @param instrument               from which to pick audio
     @param event                    to pick audio for
     @param segmentChoiceArrangement arranging this instrument for a program
     @param startAtSegmentMicros     of audio
     @param lengthMicros             of audio
     @param volRatio                 ratio of volume
     @on failure
     */
    void pickInstrumentAudio(
        const std::string &note,
        const Instrument *instrument,
        const ProgramSequencePatternEvent *event,
        const SegmentChoiceArrangement *segmentChoiceArrangement,
        const long &startAtSegmentMicros,
        const std::optional<long> &lengthMicros,
        const std::optional<UUID> &segmentChordVoicingId,
        const float &volRatio) const;

    /**
     Select audio from a multiphonic instrument
     <p>
     Sampler obeys isMultiphonic from Instrument config https://github.com/xjmusic/xjmusic/issues/252
  
     @param instrument of which to score available audios, and make a selection
     @param event      for caching reference
     @param note       to match selection
     @return matched new audio
     */
    std::optional<const InstrumentAudio *>
    selectMultiphonicInstrumentAudio(const Instrument *instrument, const ProgramSequencePatternEvent *event,
                                     const std::string &note) const;

    /**
     Select audio from a multiphonic instrument
     <p>
     If never encountered, default to new selection and cache that.
  
     @param instrument of which to score available audios, and make a selection
     @param event      to match selection
     @return matched new audio
     @on failure
     */
    [[nodiscard]] std::optional<const InstrumentAudio *>
    selectMonophonicInstrumentAudio(const Instrument *instrument, const ProgramSequencePatternEvent *event) const;

    /**
     Chord instrument mode
     https://github.com/xjmusic/xjmusic/issues/235
     <p>
     If never encountered, default to new selection and cache that.
  
     @param instrument of which to score available audios, and make a selection
     @param chord      to match selection
     @return matched new audio
     */
    [[nodiscard]] std::optional<const InstrumentAudio *>
    selectChordPartInstrumentAudio(const Instrument *instrument, const Chord &chord) const;

    /**
     Select a new random instrument audio based on a pattern event
  
     @param instrument of which to score available audios, and make a selection
     @param event      to match
     @return matched new audio
     */
    [[nodiscard]] std::optional<const InstrumentAudio *>
    selectNewNoteEventInstrumentAudio(const Instrument *instrument, const ProgramSequencePatternEvent *event) const;

    /**
     Select a new random instrument audio based on a pattern event
     <p>
     Sampler obeys isMultiphonic from Instrument config https://github.com/xjmusic/xjmusic/issues/252
  
     @param instrument of which to score available audios, and make a selection
     @param note       to match
     @return matched new audio
     */
    std::optional<const InstrumentAudio *>
    selectNewMultiphonicInstrumentAudio(const Instrument *instrument, std::string note) const;

    /**
     Test if an instrument contains audios named like N
     <p>
     Choose drum instrument to fulfill beat program event names https://github.com/xjmusic/xjmusic/issues/253
  
     @param instrument    to test
     @param requireEvents N
     @return true if instrument contains audios named like N or required event names list is empty
     */
    bool
    instrumentContainsAudioEventsLike(const Instrument &instrument, const std::set<std::string> &requireEvents) const;
  };

}// namespace XJ

#endif//XJ_MUSIC_CRAFT_H