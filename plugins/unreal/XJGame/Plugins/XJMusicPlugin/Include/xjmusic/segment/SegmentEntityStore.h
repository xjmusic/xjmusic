// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_SEGMENT_ENTITY_STORE_H
#define XJMUSIC_SEGMENT_ENTITY_STORE_H

#include <optional>
#include <set>
#include <string>
#include <vector>

#include "Chain.h"
#include "Segment.h"
#include "SegmentChoice.h"
#include "SegmentChoiceArrangement.h"
#include "SegmentChoiceArrangementPick.h"
#include "SegmentChord.h"
#include "SegmentChordVoicing.h"
#include "SegmentMeme.h"
#include "SegmentMessage.h"
#include "SegmentMeta.h"
#include "xjmusic/util/EntityUtils.h"

using namespace XJ;

#define SEGMENT_STORE_CORE_HEADERS(ENTITY, ENTITIES)                           \
  const ENTITY *put(const ENTITY &choice);                                     \
  std::optional<const ENTITY *> read##ENTITY(int segmentId, const UUID &id);   \
  std::set<const ENTITY *> readAll##ENTITIES(int segmentId);                   \
  std::set<const ENTITY *> readAll##ENTITIES(const std::set<int> &segmentIds); \
  void delete##ENTITY(int segmentId, const UUID &id);


namespace XJ {

  /**
   SegmentEntityStore segments and child entities partitioned by segment id for rapid addressing
   https://github.com/xjmusic/xjmusic/issues/276
   <p>
   XJ Lab Distributed Architecture
   https://github.com/xjmusic/xjmusic/issues/207
   Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
   */
  class SegmentEntityStore {
    std::optional<Chain> chain;
    std::map<int, Segment> segments;
    std::map<int, std::map<UUID, SegmentChoice>> segmentChoices;
    std::map<int, std::map<UUID, SegmentChoiceArrangement>> segmentChoiceArrangements;
    std::map<int, std::map<UUID, SegmentChoiceArrangementPick>> segmentChoiceArrangementPicks;
    std::map<int, std::map<UUID, SegmentChord>> segmentChords;
    std::map<int, std::map<UUID, SegmentChordVoicing>> segmentChordVoicings;
    std::map<int, std::map<UUID, SegmentMeme>> segmentMemes;
    std::map<int, std::map<UUID, SegmentMessage>> segmentMessages;
    std::map<int, std::map<UUID, SegmentMeta>> segmentMetas;

    static void validate(SegmentMeme entity);

    static void validate(Segment entity);

  public:
    SEGMENT_STORE_CORE_HEADERS(SegmentChoice, SegmentChoices)

    SEGMENT_STORE_CORE_HEADERS(SegmentChoiceArrangement, SegmentChoiceArrangements)

    SEGMENT_STORE_CORE_HEADERS(SegmentChoiceArrangementPick, SegmentChoiceArrangementPicks)

    SEGMENT_STORE_CORE_HEADERS(SegmentChord, SegmentChords)

    SEGMENT_STORE_CORE_HEADERS(SegmentChordVoicing, SegmentChordVoicings)

    SEGMENT_STORE_CORE_HEADERS(SegmentMeme, SegmentMemes)

    SEGMENT_STORE_CORE_HEADERS(SegmentMessage, SegmentMessages)

    SEGMENT_STORE_CORE_HEADERS(SegmentMeta, SegmentMetas);

    SegmentEntityStore() = default;

    /**
     * Put the Chain in the entity store
     * @returns stored Chain
     */
    Chain *put(const Chain &c);

    /**
     * Put a Segment in the entity store
     * @returns stored Segment
     */
    const Segment *put(const Segment &segment);

    /**
     * Read a Chain by #
     * @returns requested Chain
     */
    std::optional<Chain *> readChain();

    /**
     * Read a Segment by #
     * @returns requested Segment
     */
    std::optional<const Segment *> readSegment(int segmentId);

    /**
     Get the segment at the given chain microseconds, if it is ready
     segment beginning <= chain microseconds <= end
     <p>
     Note this algorithm intends to get the latter segment when the lookup point is on the line between two segments

     @param chainMicros the chain microseconds for which to get the segment
     @return the segment at the given chain microseconds, or an empty optional if the segment is not ready
     */
    std::optional<const Segment *> readSegmentAtChainMicros(const unsigned long long int chainMicros);

    /**
    Get all segments for a chain id

    @return collection of segments
    @ on failure to retrieve the requested key
    */
    std::vector<const Segment *> readAllSegments();

    /**
     Get all segments for a chain id in a given state

      @param segmentState to filter by
     @return collection of segments
     @throws exception on failure to retrieve the requested key
     */
    std::vector<const Segment *> readAllSegmentsInState(Segment::State segmentState);

    /**
     Read all Segments that are accessible, by Chain ID, starting and ending at particular offsets

     @param fromOffset to read segments form
     @param toOffset   to read segments to
     @return list of segments as JSON
     */
    std::vector<const Segment *> readSegmentsFromToOffset(int fromOffset, int toOffset);

    /**
     Read all sub-entities records for many parent segments by id

     @param segmentIds for which to read records.
     @return collection of all sub entities of these parent segments, different classes that extend EntityUtils
     */
    std::set<const SegmentEntity *> readAllSegmentEntities(const std::set<int> &segmentIds);

    /**
     Get the segments that span the given instant

     @param fromChainMicros for which to get segments
     @param toChainMicros   for which to get segments
     @return segments that span the given instant, empty if none found
     */
    std::vector<const Segment *> readAllSegmentsSpanning(const unsigned long long int fromChainMicros, const unsigned long long int toChainMicros);

    /**
     Get the last known segment id

     @return last segment id
     */
    int readLastSegmentId() const;

    /**
     Read the last segment in a Chain, Segments sorted by offset ascending

     @return Last Segment in Chain
     */
    std::optional<const Segment *> readSegmentLast();

    /**
     Read a choice for a given segment id and program type

     @param segmentId   for which to get choice
     @param programType to get
     @return main choice
     */
    std::optional<const SegmentChoice *> readChoice(int segmentId, Program::Type programType);

    /**
     Get a hash of all the choices for the given segment

     @param segment for which to get the choice hash
     @return hash of all the ids of the choices for the given segment
     */
    std::string readChoiceHash(const Segment &segment);

    /**
    * Read all choices for the set of segments
    * @param forSegments    of segments
    * @return        list of choices
    */
    std::set<const SegmentChoiceArrangementPick *>
    readAllSegmentChoiceArrangementPicks(const std::vector<const Segment *> &forSegments);

    /**
    * Read all arrangement picks for the given segment choice
    * @param segmentChoice
    * @return
    */
    std::set<const SegmentChoiceArrangementPick *> readAllSegmentChoiceArrangementPicks(const SegmentChoice *segmentChoice);

    /**
     * Read all segment chords in order of position for the given segmefnt
     * @param segmentId  for which to get chords
     * @return         chords
     */
    std::vector<const SegmentChord *> readOrderedSegmentChords(int segmentId);

    /**
     Get the total number of segments in the store

     @return number of segments
     */
    int getSegmentCount() const;

    /**
     Whether the segment manager is completely empty

     @return true if there are zero segments
     */
    bool empty() const;

    /**
     Update a specified EntityUtils

     @param segment for the updated EntityUtils.
     @ on failure
     */
    const Segment *updateSegment(Segment &segment);

    /**
     * Read a Chain by #
     * @returns requested Chain
     */
    void deleteChain();

    /**
     * Read a Segment by #
     * @returns requested Segment
     */
    void deleteSegment(int id);

    /**
     Delete all segments before the given segment id

     @param lastSegmentId segment id
     */
    void deleteSegmentsBefore(int lastSegmentId);

    /**
     Delete all segments after the given segment id

     @param lastSegmentId segment id
     */
    void deleteSegmentsAfter(int lastSegmentId);

    /**
     Delete all records in the store (e.g. during integration testing)
     @ on failure
     */
    void clear();

    /**
     Segment state transitions are protected, dependent on the state this segment is being transitioned of, and the intended state it is being transitioned to.

     @param fromState to protect transition of
     @param toState   to test transition to
     @ on prohibited transition
     */
    static void protectSegmentStateTransition(Segment::State fromState, Segment::State toState);

    /**
     Require state is in an array of states

     @param toState       to check
     @param allowedStates required to be in
     @ if not in required states
     */
    static void onlyAllowSegmentStateTransitions(Segment::State toState, const std::set<Segment::State> &allowedStates);
  };

}// namespace XJ

#endif//XJMUSIC_SEGMENT_ENTITY_STORE_H