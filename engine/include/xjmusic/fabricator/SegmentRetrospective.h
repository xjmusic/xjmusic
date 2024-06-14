// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATOR_SEGMENT_RETROSPECTIVE_H
#define XJMUSIC_FABRICATOR_SEGMENT_RETROSPECTIVE_H

#include <vector>
#include <optional>
#include <set>

#include "FabricationEntityStore.h"
#include "xjmusic/entities/content/Instrument.h"
#include "xjmusic/entities/segment/Segment.h"
#include "xjmusic/entities/segment/SegmentChoice.h"
#include "xjmusic/entities/segment/SegmentChoiceArrangement.h"
#include "xjmusic/entities/segment/SegmentChoiceArrangementPick.h"
#include "xjmusic/entities/segment/SegmentChord.h"
#include "xjmusic/entities/segment/SegmentMeta.h"

namespace XJ {

  /**
   Digest segments of the previous main program
   <p>
   NextMain/NextMacro-type: Retrospective of the previous main choice, primary choices only
   REF https://github.com/xjmusic/workstation/issues/242
   <p>
   Continue-type: Retrospective of all segments in this main program
   REF https://github.com/xjmusic/workstation/issues/242
   */
  class SegmentRetrospective {
  public:

    /**
     * Constructor from entity store and segment id
     * @param entityStore  for retrospective
     * @param segmentId    from which to create retrospective
     */
    explicit SegmentRetrospective(FabricationEntityStore entityStore, int segmentId);

    /**
     Get the arrangement for the given pick

     @param pick for which to get arrangement
     @return arrangement
     @throws FabricationException on failure to retrieve
     */
    SegmentChoiceArrangement getArrangement(const SegmentChoiceArrangementPick& pick);

    /**
     @return all choices
     */
    std::set<SegmentChoice> getChoices();

    /**
     Get the choice for the given arrangement

     @param arrangement for which to get choice
     @return choice
     @throws FabricationException on failure to retrieve
     */
    SegmentChoice getChoice(const SegmentChoiceArrangement& arrangement);

    /**
     Get the instrument type for a given pick

     @param pick for which to get instrument type
     @return instrument type of pick
     */
    Instrument::Type getInstrumentType(SegmentChoiceArrangementPick pick);

    /**
     Get the meta from the previous segment with the given key
     <p>
     Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://github.com/xjmusic/workstation/issues/222

     @param key to search for meta
     @return meta if found
     */
    std::optional<SegmentMeta> getPreviousMeta(const std::string& key);

    /**
     Get the previous segment choices for the given instrument
     (although there should only be one previous segment choice for each instrument)

     @param instrumentId for which to get choice
     @return previous segment choice
     */
    std::set<SegmentChoice> getPreviousChoicesForInstrument(const UUID& instrumentId);

    /**
     Get the previous arrangements for the given instrument id

     @param instrumentId for which to get arrangements
     @return segment choice arrangements
     */
    std::set<SegmentChoiceArrangement> getPreviousArrangementsForInstrument(UUID instrumentId);

    /**
     Get the picks of any previous segments which selected the same main sequence
     <p>
     Artist writing detail program expects 'X' note value to result in random part creation from available Voicings https://github.com/xjmusic/workstation/issues/251

     @return map of all previous segment meme constellations (as keys) to a collection of choices made
     */
    std::set<SegmentChoiceArrangementPick> getPicks();

    /**
     Get the choice of a given type

     @param type of choice to get
     @return choice of given type
     */
    std::optional<SegmentChoice> getPreviousChoiceOfType(const Segment &segment, Program::Type type);

    /**
     Get the previous-segment choice of a given type

     @param type of choice to get
     @return choice of given type
     */
    std::optional<SegmentChoice> getPreviousChoiceOfType(Program::Type type);

    /**
     Get the previous-segment choices of a given instrument mode

     @param instrumentMode for which to get previous-segment choices
     @return choices
     */
    std::set<SegmentChoice> getPreviousChoicesOfMode(Instrument::Mode instrumentMode);

    /**
     Get the previous-segment choices of a given instrument type and mode

     @param instrumentType  for which to get previous-segment choices
     @param instrumentModes for which to get previous-segment choices
     @return choices
     */
    std::set<SegmentChoice>
    getPreviousChoicesOfTypeMode(Instrument::Type instrumentType, Instrument::Mode instrumentModes);

    /**
     Get the previous-segment choices of a given instrument type

     @param instrumentType for which to get previous-segment choices
     @return choices
     */
    std::optional<SegmentChoice> getPreviousChoiceOfType(Instrument::Type instrumentType);

    /**
     Get the previous picks for the given instrument id

     @param instrumentId for which to get picks
     @return segment choice picks
     */
    std::set<SegmentChoiceArrangementPick> getPreviousPicksForInstrument(UUID instrumentId);

    /**
     Get the segment immediately previous to the current segment

     @return previous segment
     */
    std::optional<Segment> getPreviousSegment();

    /**
     @return all cached segments, ordered by id
     <p>
     Always starts with the segment at current offset minus one,
     includes that segment and all others with the same main program, which covers both
     whether this retrospective is looking back on the current main program (Continue segment),
     or the previous one (NextMain/NextMacro segments)
     */
    std::vector<Segment> getSegments();

    /**
     Get all segment chords for the given segment id, ordered by position

     @param segmentId for which to get chords
     @return chords
     */
    std::vector<SegmentChord> getSegmentChords(int segmentId);

  private:
    std::map<int, std::vector<SegmentChord>> segmentChords{}; // indexed by id, vector of chords ordered by position
    FabricationEntityStore entityStore{};
    std::vector<Segment> retroSegments{};
    std::set<int> previousSegmentIds{};
    std::optional<Segment> previousSegment;

  };

}// namespace XJ

#endif //XJMUSIC_FABRICATOR_SEGMENT_RETROSPECTIVE_H