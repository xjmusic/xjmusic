// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_SEGMENT_ENTITY_STORE_H
#define XJMUSIC_SEGMENT_ENTITY_STORE_H

#include <optional>
#include <typeindex>
#include <vector>
#include <unordered_map>
#include <string>
#include <set>

#include "xjmusic/entities/Entity.h"
#include "xjmusic/entities/segment/Segment.h"
#include "xjmusic/entities/segment/Chain.h"
#include "xjmusic/entities/segment/SegmentChoiceArrangementPick.h"
#include "xjmusic/entities/segment/SegmentChoice.h"

using namespace XJ;

#define SEGMENT_STORE_CORE_HEADERS(ENTITY, ENTITIES)                    \
  ENTITY put(const ENTITY& choice);                                     \
  std::optional<ENTITY> read##ENTITY(int segmentId, const UUID& id);    \
  std::set<ENTITY> readAll##ENTITIES(int segmentId);                    \
  std::set<ENTITY> readAll##ENTITIES(const std::set<int>& segmentIds);  \


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

  public:
    SegmentEntityStore() = default;

    SEGMENT_STORE_CORE_HEADERS(SegmentChoice, SegmentChoices)

    SEGMENT_STORE_CORE_HEADERS(SegmentChoiceArrangement, SegmentChoiceArrangements)

    SEGMENT_STORE_CORE_HEADERS(SegmentChoiceArrangementPick, SegmentChoiceArrangementPicks)

    SEGMENT_STORE_CORE_HEADERS(SegmentChord, SegmentChords)

    SEGMENT_STORE_CORE_HEADERS(SegmentChordVoicing, SegmentChordVoicings)

    SEGMENT_STORE_CORE_HEADERS(SegmentMeme, SegmentMemes)

    SEGMENT_STORE_CORE_HEADERS(SegmentMessage, SegmentMessages)

    SEGMENT_STORE_CORE_HEADERS(SegmentMeta, SegmentMetas)

    /**
     * Put the Chain in the entity store
     * @returns stored Chain
     */
    Chain put(Chain c);

    /**
     * Put a Segment in the entity store
     * @returns stored Segment
     */
    Segment put(Segment segment);

    /**
     * Read a Chain by #
     * @returns requested Chain
     */
    std::optional<Chain> readChain();

    /**
     * Read a Segment by #
     * @returns requested Segment
     */
    std::optional<Segment> readSegment(int segmentId);

    /**
     Get the segment at the given chain microseconds, if it is ready
     segment beginning <= chain microseconds <= end
     <p>
     Note this algorithm intends to get the latter segment when the lookup point is on the line between two segments

     @param chainMicros the chain microseconds for which to get the segment
     @return the segment at the given chain microseconds, or an empty optional if the segment is not ready
     */
    std::optional<Segment> readSegmentAtChainMicros(long chainMicros);

    /**
     Get all segments for a chain id

     @return collection of segments
     @throws FabricationException on failure to retrieve the requested key
     */
    std::vector<Segment> readAllSegments();

    /**
     Read all Segments that are accessible, by Chain ID, starting and ending at particular offsets

     @param fromOffset to read segments form
     @param toOffset   to read segments to
     @return list of segments as JSON
     */
    std::vector<Segment> readSegmentsFromToOffset(int fromOffset, int toOffset);

    /**
     Fetch all sub-entities records for many parent segments by id

     @param segmentIds to fetch records for.
     @return collection of all sub entities of these parent segments, different classes that extend Entity
     */
    std::set<SegmentEntity> readAllSegmentEntities(const std::set<int>& segmentIds);

    /**
     Get the segments that span the given instant

     @param fromChainMicros for which to get segments
     @param toChainMicros   for which to get segments
     @return segments that span the given instant, empty if none found
     */
    std::vector<Segment> readAllSegmentsSpanning(long fromChainMicros, long toChainMicros);

    /**
     Get the last known segment id

     @return last segment id
     */
    int readLastSegmentId();

    /**
     Read the last segment in a Chain, Segments sorted by offset ascending

     @return Last Segment in Chain
     */
    std::optional<Segment> readSegmentLast();

    /**
     Read a choice for a given segment id and program type

     @param segmentId   for which to get choice
     @param programType to get
     @return main choice
     */
    std::optional<SegmentChoice> readChoice(int segmentId, Program::Type programType);

    /**
     Get a hash of all the choices for the given segment

     @param segment for which to get the choice hash
     @return hash of all the ids of the choices for the given segment
     */
    std::string readChoiceHash(const Segment& segment);

    /**
     Get the total number of segments in the store

     @return number of segments
     */
    int getSegmentCount();

    /**
     Whether the segment manager is completely empty

     @return true if there are zero segments
     */
    bool isEmpty();

    /**
     Update a specified Entity

     @param segment for the updated Entity.
     @throws FabricationException on failure
     */
    void updateSegment(Segment &segment);

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
     * Read a SegmentChoice by UUID in the given Segment #
     * @returns requested SegmentChoice
     */
    void deleteSegmentChoice(int segmentId, const UUID& id);

    /**
     * Read a SegmentChoiceArrangement by UUID in the given Segment #
     * @returns requested SegmentChoiceArrangement
     */
    void deleteSegmentChoiceArrangement(int segmentId, const UUID& id);

    /**
     * Read a SegmentChoiceArrangementPick by UUID in the given Segment #
     * @returns requested SegmentChoiceArrangementPick
     */
    void deleteSegmentChoiceArrangementPick(int segmentId, const UUID& id);

    /**
     * Read a SegmentChord by UUID in the given Segment #
     * @returns requested SegmentChord
     */
    void deleteSegmentChord(int segmentId, const UUID& id);

    /**
     * Read a SegmentChordVoicing by UUID in the given Segment #
     * @returns requested SegmentChordVoicing
     */
    void deleteSegmentChordVoicing(int segmentId, const UUID& id);

    /**
     * Read a SegmentMeme by UUID in the given Segment #
     * @returns requested SegmentMeme
     */
    void deleteSegmentMeme(int segmentId, const UUID& id);

    /**
     * Read a SegmentMessage by UUID in the given Segment #
     * @returns requested SegmentMessage
     */
    void deleteSegmentMessage(int segmentId, const UUID& id);

    /**
     * Read a SegmentMeta by UUID in the given Segment #
     * @returns requested SegmentMeta
     */
    void deleteSegmentMeta(int segmentId, const UUID& id);

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
     @throws FabricationException on failure
     */
    void clear();

    /**
     Segment state transitions are protected, dependent on the state this segment is being transitioned of, and the intended state it is being transitioned to.

     @param fromState to protect transition of
     @param toState   to test transition to
     @throws FabricationException on prohibited transition
     */
    static void protectSegmentStateTransition(Segment::State fromState, Segment::State toState);

    /**
     Require state is in an array of states

     @param toState       to check
     @param allowedStates required to be in
     @throws FabricationException if not in required states
     */
    static void onlyAllowSegmentStateTransitions(Segment::State toState, const std::set<Segment::State> &allowedStates);

  private:

    static void validate(SegmentMeme entity);

    static void validate(Segment entity);

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
  };

}

#endif //XJMUSIC_SEGMENT_ENTITY_STORE_H