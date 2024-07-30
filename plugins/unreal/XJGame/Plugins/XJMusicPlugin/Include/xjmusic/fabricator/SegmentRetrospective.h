// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATOR_SEGMENT_RETROSPECTIVE_H
#define XJMUSIC_FABRICATOR_SEGMENT_RETROSPECTIVE_H

#include <optional>
#include <set>
#include <vector>

#include "xjmusic/content/Instrument.h"
#include "xjmusic/segment/Segment.h"
#include "xjmusic/segment/SegmentChoice.h"
#include "xjmusic/segment/SegmentChoiceArrangement.h"
#include "xjmusic/segment/SegmentChoiceArrangementPick.h"
#include "xjmusic/segment/SegmentChord.h"
#include "xjmusic/segment/SegmentEntityStore.h"
#include "xjmusic/segment/SegmentMeta.h"

namespace XJ {

  /**
   Digest segments of the previous main program
   <p>
   NextMain/NextMacro-type: Retrospective of the previous main choice, primary choices only
   REF https://github.com/xjmusic/xjmusic/issues/242
   <p>
   Continue-type: Retrospective of all segments in this main program
   REF https://github.com/xjmusic/xjmusic/issues/242
   */
  class SegmentRetrospective {
    int segmentId;
    SegmentEntityStore *entityStore{};
    std::vector<const Segment *> retroSegments{};
    std::set<int> previousSegmentIds{};
    std::optional<const Segment *> previousSegment;
    std::map<int, std::vector<const SegmentChord *>> segmentChords{};// indexed by id, vector of chords ordered by position

  public:
    virtual ~SegmentRetrospective() = default;

    /**
     * Constructor from entity store and segment id
    * Compute the retrospective
    * <p>
    * NOTE: the segment retrospective is empty for segments of type Initial, NextMain, and NextMacro--
    * Only Continue-type segments have a retrospective
    * <p>
    * Begin by getting the previous segment
    * Only can build retrospective if there is at least one previous segment
    * The previous segment is the first one cached here. We may cache even further back segments below if found
    * @param entityStore  for retrospective
    * @param segmentId    from which to create retrospective
    * @param autoload     whether to autoload the retrospective
    * @throws FabricationFatalException on failure to retrieve
    * @ on failure to compute
    */
    explicit SegmentRetrospective(SegmentEntityStore *entityStore, int segmentId, bool autoload = true);

    /**
     Get the arrangement for the given pick

     @param pick for which to get arrangement
     @return arrangement
     @ on failure to retrieve
     */
    virtual const SegmentChoiceArrangement *getArrangement(const SegmentChoiceArrangementPick *pick) const;

    /**
     @return all choices
     */
    virtual std::set<const SegmentChoice *> getChoices() const;

    /**
     Get the choice for the given arrangement

     @param arrangement for which to get choice
     @return choice
     @ on failure to retrieve
     */
    virtual const SegmentChoice *getChoice(const SegmentChoiceArrangement *arrangement) const;

    /**
     Get the instrument type for a given pick

     @param pick for which to get instrument type
     @return instrument type of pick
     */
    virtual Instrument::Type getInstrumentType(const SegmentChoiceArrangementPick *pick) const;

    /**
     Get the meta from the previous segment with the given key
     <p>
     Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://github.com/xjmusic/xjmusic/issues/222

     @param key to search for meta
     @return meta if found
     */
    virtual std::optional<const SegmentMeta *> getPreviousMeta(const std::string &key) const;

    /**
     Get the previous segment choices for the given instrument
     (although there should only be one previous segment choice for each instrument)

     @param instrumentId for which to get choice
     @return previous segment choice
     */
    virtual std::set<const SegmentChoice *> getPreviousChoicesForInstrument(const UUID &instrumentId) const;

    /**
     Get the previous arrangements for the given instrument id

     @param instrumentId for which to get arrangements
     @return segment choice arrangements
     */
    virtual std::set<const SegmentChoiceArrangement *> getPreviousArrangementsForInstrument(const UUID &instrumentId) const;

    /**
     Get the picks of any previous segments which selected the same main sequence
     <p>
     Artist writing detail program expects 'X' note value to result in random part creation from available Voicings https://github.com/xjmusic/xjmusic/issues/251

     @return map of all previous segment meme constellations (as keys) to a collection of choices made
     */
    virtual std::set<const SegmentChoiceArrangementPick *> getPicks() const;

    /**
     Get the choice of a given type

     @param segment for which to get choice
     @param programType of choice to get
     @return choice of given type
     */
    virtual std::optional<const SegmentChoice *> getPreviousChoiceOfType(const Segment *segment, Program::Type programType) const;

    /**
     Get the previous-segment choice of a given type

     @param programType of choice to get
     @return choice of given type
     */
    virtual std::optional<const SegmentChoice *> getPreviousChoiceOfType(Program::Type programType) const;

    /**
     Get the previous-segment choices of a given instrument mode

     @param instrumentMode for which to get previous-segment choices
     @return choices
     */
    virtual std::set<const SegmentChoice *> getPreviousChoicesOfMode(Instrument::Mode instrumentMode) const;

    /**
     Get the previous-segment choices of a given instrument type and mode

     @param instrumentType  for which to get previous-segment choices
     @param instrumentMode for which to get previous-segment choices
     @return choices
     */
    virtual std::set<const SegmentChoice *>
    getPreviousChoicesOfTypeMode(Instrument::Type instrumentType, Instrument::Mode instrumentMode) const;

    /**
     Get the previous-segment choices of a given instrument type

     @param instrumentType for which to get previous-segment choices
     @return choices
     */
    virtual std::optional<const SegmentChoice *> getPreviousChoiceOfType(Instrument::Type instrumentType) const;

    /**
     Get the previous picks for the given instrument id

     @param instrumentId for which to get picks
     @return segment choice picks
     */
    virtual std::set<const SegmentChoiceArrangementPick *> getPreviousPicksForInstrument(const UUID &instrumentId) const;

    /**
     Get the segment immediately previous to the current segment

     @return previous segment
     */
    virtual std::optional<const Segment *> getPreviousSegment() const;

    /**
     @return all cached segments, ordered by id
     <p>
     Always starts with the segment at current offset minus one,
     includes that segment and all others with the same main program, which covers both
     whether this retrospective is looking back on the current main program (Continue segment),
     or the previous one (NextMain/NextMacro segments)
     */
    virtual std::vector<const Segment *> getSegments() const;

    /**
     Get all segment chords for the given segment id, ordered by position

     @param segmentId for which to get chords
     @return chords
     */
    virtual std::vector<const SegmentChord *> getSegmentChords(int segmentId) const;
  };

}// namespace XJ

#endif//XJMUSIC_FABRICATOR_SEGMENT_RETROSPECTIVE_H