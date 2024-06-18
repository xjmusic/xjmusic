// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATOR_SEGMENT_UTILS_H
#define XJMUSIC_FABRICATOR_SEGMENT_UTILS_H

#include <string>
#include <set>
#include <vector>
#include <optional>
#include <algorithm>

#include "xjmusic/util/EntityUtils.h"
#include "xjmusic/content/Template.h"
#include "xjmusic/content/TemplateBinding.h"
#include "xjmusic/music/Note.h"
#include "xjmusic/segment/Chain.h"
#include "xjmusic/segment/Segment.h"
#include "xjmusic/segment/SegmentChoice.h"
#include "xjmusic/segment/SegmentChoiceArrangement.h"
#include "xjmusic/segment/SegmentChoiceArrangementPick.h"
#include "xjmusic/segment/SegmentChord.h"
#include "xjmusic/segment/SegmentChordVoicing.h"
#include "xjmusic/segment/SegmentMeme.h"
#include "xjmusic/segment/SegmentMessage.h"
#include "xjmusic/segment/SegmentMeta.h"
#include "xjmusic/util/StringUtils.h"

/**
 Utilities for working with segments
 */
namespace XJ {

  class SegmentUtils {

  public:

    /**
     Find first segment choice of a given type in a collection of segment choices

     @param segmentChoices to filter from
     @param type           to find one of
     @return segment choice of given type
     */
    static std::optional<const SegmentChoice *>
    findFirstOfType(const std::vector<SegmentChoice> &segmentChoices, Program::Type type);

    /**
     Find first segment choice of a given type in a collection of segment choices

     @param segmentChoices to filter from
     @param type           to find one of
     @return segment choice of given type
     */
    static std::optional<const SegmentChoice *>
    findFirstOfType(const std::vector<SegmentChoice> &segmentChoices, Instrument::Type type);

    /**
     Get the identifier or a Segment: ship key if available, else ID

     @param segment to get identifier of
     @return ship key if available, else ID
     */
    static std::string getIdentifier(Segment *segment);

    /**
     Get the last dubbed from any collection of Segments

     @param segments to get last dubbed from
     @return last dubbed segment from collection
     */
    static std::optional<Segment> getLastCrafted(const std::vector<Segment> &segments);

    /**
     Get the last from any collection of Segments

     @param segments to get last from
     @return last segment from collection
     */
    static std::optional<Segment> getLast(std::vector<Segment> &segments);

    /**
     Get only the dubbed from any collection of Segments

     @param segments to get dubbed from
     @return dubbed segments from collection
     */
    static std::vector<Segment> getCrafted(const std::vector<Segment> &segments);

    /**
     Whether a segment chord voicing contains any valid notes

     @param voicing to test
     @return true if contains any valid notes
     */
    static bool containsAnyValidNotes(const SegmentChordVoicing &voicing);

    /**
     Whether the segment is spanning a given time frame.
     Inclusive of segment start time; exclusive of segment end time.

     @param segment         to test
     @param fromChainMicros to test frame from
     @param toChainMicros   to test frame to
     @return true if segment is spanning time frame
     */
    static bool isSpanning(Segment &segment, long long fromChainMicros, long long toChainMicros);

    /**
     Whether the segment is intersecting a given time.
     Inclusive of segment start time; exclusive of segment end time.
     Designed so to return true when within the threshold of being active (may true for multiple segments)
     + return false when threshold is 0 and at the exact end of the segment (to avoid double-activation)

     @param segment         to test
     @param atChainMicros   to test at
     @param thresholdMicros to test threshold
     @return true if segment is spanning time
     */
    static bool isIntersecting(Segment &segment, long long atChainMicros, long long thresholdMicros);

    /**
     Get the storage filename for a Segment

     @param segment   for which to get storage filename
     @param extension of key
     @return segment ship key
     */
    static std::string getStorageFilename(Segment &segment, const std::string &extension);

    /**
     Get the full storage key for a segment audio

     @param segment for which to get storage key
     @return storage key for segment
     */
    static std::string getStorageFilename(Segment &segment);

    /**
     @param choice to describe
     @return description of choice
     */
    static std::string describe(SegmentChoice &choice);

    /**
     * Get the end-at time of a segment in chain micros
     * @param segment  for which to get time
     * @return       end-at time in chain micros
     */
    static long getEndAtChainMicros(Segment &segment);

    /**
     * Whether two segments are the same but updated
     * @param s1   segment one
     * @param s2   segment two
     * @return   true if same but updated
     */
    static bool isSameButUpdated(Segment &s1, Segment &s2);

    /**
     * Get the duration of a collection of segments in microseconds
     * @param segments  collection of segments
     * @return        duration in microseconds
     */
    static long getDurationMinMicros(std::vector<Segment> &segments);

  };

} // namespace XJ

#endif //XJMUSIC_FABRICATOR_SEGMENT_UTILS_H