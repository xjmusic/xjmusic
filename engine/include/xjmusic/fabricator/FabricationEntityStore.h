// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

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

namespace XJ {

  /**
   FabricationEntityStore segments and child entities partitioned by segment id for rapid addressing
   https://github.com/xjmusic/workstation/issues/276
   <p>
   XJ Lab Distributed Architecture
   https://github.com/xjmusic/workstation/issues/207
   Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
   */
  class FabricationEntityStore {

  public:

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
     * Put a SegmentChoice in the entity store
     * @returns stored SegmentChoice
     */
    SegmentChoice put(const SegmentChoice& choice);

    /**
     * Put a SegmentChoiceArrangement in the entity store
     * @returns stored SegmentChoiceArrangement
     */
    SegmentChoiceArrangement put(const SegmentChoiceArrangement& arrangement);

    /**
     * Put a SegmentChoiceArrangementPick in the entity store
     * @returns stored SegmentChoiceArrangementPick
     */
    SegmentChoiceArrangementPick put(const SegmentChoiceArrangementPick& pick);

    /**
     * Put a SegmentChord in the entity store
     * @returns stored SegmentChord
     */
    SegmentChord put(const SegmentChord& chord);

    /**
     * Put a SegmentChordVoicing in the entity store
     * @returns stored SegmentChordVoicing
     */
    SegmentChordVoicing put(const SegmentChordVoicing& voicing);

    /**
     * Put a SegmentMeme in the entity store
     * @returns stored SegmentMeme
     */
    SegmentMeme put(const SegmentMeme& meme);

    /**
     * Put a SegmentMessage in the entity store
     * @returns stored SegmentMessage
     */
    SegmentMessage put(const SegmentMessage& message);

    /**
     * Put a SegmentMeta in the entity store
     * @returns stored SegmentMeta
     */
    SegmentMeta put(const SegmentMeta& meta);

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
     * Read a SegmentChoice by UUID in the given Segment #
     * @returns requested SegmentChoice
     */
    std::optional<SegmentChoice> readSegmentChoice(int segmentId, const UUID& id);

    /**
     * Read a SegmentChoiceArrangement by UUID in the given Segment #
     * @returns requested SegmentChoiceArrangement
     */
    std::optional<SegmentChoiceArrangement> readSegmentChoiceArrangement(int segmentId, const UUID& id);

    /**
     * Read a SegmentChoiceArrangementPick by UUID in the given Segment #
     * @returns requested SegmentChoiceArrangementPick
     */
    std::optional<SegmentChoiceArrangementPick> readSegmentChoiceArrangementPick(int segmentId, const UUID& id);

    /**
     * Read a SegmentChord by UUID in the given Segment #
     * @returns requested SegmentChord
     */
    std::optional<SegmentChord> readSegmentChord(int segmentId, const UUID& id);

    /**
     * Read a SegmentChordVoicing by UUID in the given Segment #
     * @returns requested SegmentChordVoicing
     */
    std::optional<SegmentChordVoicing> readSegmentChordVoicing(int segmentId, const UUID& id);

    /**
     * Read a SegmentMeme by UUID in the given Segment #
     * @returns requested SegmentMeme
     */
    std::optional<SegmentMeme> readSegmentMeme(int segmentId, const UUID& id);

    /**
     * Read a SegmentMessage by UUID in the given Segment #
     * @returns requested SegmentMessage
     */
    std::optional<SegmentMessage> readSegmentMessage(int segmentId, const UUID& id);

    /**
     * Read a SegmentMeta by UUID in the given Segment #
     * @returns requested SegmentMeta
     */
    std::optional<SegmentMeta> readSegmentMeta(int segmentId, const UUID& id);

    /**
     * Read all SegmentChoices in the given Segment #
     */
    std::set<SegmentChoice> readAllSegmentChoices(int segmentId);

    /**
     * Read all SegmentChoiceArrangements in the given Segment #
     */
    std::set<SegmentChoiceArrangement> readAllSegmentChoiceArrangements(int segmentId);

    /**
     * Read all SegmentChoiceArrangementPicks in the given Segment #
     */
    std::set<SegmentChoiceArrangementPick> readAllSegmentChoiceArrangementPicks(int segmentId);

    /**
     * Read all SegmentChords in the given Segment #
     */
    std::set<SegmentChord> readAllSegmentChords(int segmentId);

    /**
     * Read all SegmentChordVoicings in the given Segment #
     */
    std::set<SegmentChordVoicing> readAllSegmentChordVoicings(int segmentId);

    /**
     * Read all SegmentMemes in the given Segment #
     */
    std::set<SegmentMeme> readAllSegmentMemes(int segmentId);

    /**
     * Read all SegmentMessages in the given Segment #
     */
    std::set<SegmentMessage> readAllSegmentMessages(int segmentId);

    /**
     * Read all SegmentMetas in the given Segment #
     */
    std::set<SegmentMeta> readAllSegmentMetas(int segmentId);

    /**
     * Read all SegmentChoices in the given Segments by #s
     */
    std::set<SegmentChoice> readAllSegmentChoices(const std::set<int>& segmentIds);

    /**
     * Read all SegmentChoiceArrangements in the given Segments by #s
     */
    std::set<SegmentChoiceArrangement> readAllSegmentChoiceArrangements(const std::set<int>& segmentIds);

    /**
     * Read all SegmentChoiceArrangementPicks in the given Segments by #s
     */
    std::set<SegmentChoiceArrangementPick> readAllSegmentChoiceArrangementPicks(const std::set<int>& segmentIds);

    /**
     * Read all SegmentChords in the given Segments by #s
     */
    std::set<SegmentChord> readAllSegmentChords(const std::set<int>& segmentIds);

    /**
     * Read all SegmentChordVoicings in the given Segments by #s
     */
    std::set<SegmentChordVoicing> readAllSegmentChordVoicings(const std::set<int>& segmentIds);

    /**
     * Read all SegmentMemes in the given Segments by #s
     */
    std::set<SegmentMeme> readAllSegmentMemes(const std::set<int>& segmentIds);

    /**
     * Read all SegmentMessages in the given Segments by #s
     */
    std::set<SegmentMessage> readAllSegmentMessages(const std::set<int>& segmentIds);

    /**
     * Read all SegmentMetas in the given Segments by #s
     */
    std::set<SegmentMeta> readAllSegmentMetas(const std::set<int>& segmentIds);

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
    std::string readChoiceHash(Segment segment);

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
    void updateSegment(const Segment& segment);

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