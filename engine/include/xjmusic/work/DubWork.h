// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO implement this

#ifndef XJMUSIC_WORK_DUB_WORK_H
#define XJMUSIC_WORK_DUB_WORK_H

#include <optional>

#include "CraftWork.h"

namespace XJ {

/**
 THERE IS NO SPOON
 <p>
 Ground-up rewrite of the XJ work logic. First we instantiate the dub cycle, which depends on the craft cycle
 operating on a separate thread. All Segment craft is persisted in memory, and the dub cycle is responsible for
 requesting crafted segments and specifically the picked audio, and then dubbing each output audio chunk.
 <p>
 Output audio chunks are dynamically sized. There is a default size, but the size is also determined by the
 duration of the segment. E.g., during gapless album output, the chunk will cut short if necessary to begin the next
 chunk at exactly the top of the following segment.
 */
  class DubWork : Work {
    int BITS_PER_BYTE = 8;
    bool running = new bool(true);
    CraftWork *craftWork;
    TemplateConfig templateConfig;
    unsigned long dubAheadMicros = 0;
    unsigned long long atChainMicros = 0; // dubbing is done up to this point

    // Intensity override is null if no override, or a value between 0 and 1
    std::optional<float> intensityOverride = std::nullopt;

    // Next/Prev intensity updated each segment, either from segment or from override
    std::optional<float> nextIntensity = std::nullopt;
    std::optional<float> prevIntensity = std::nullopt;

  public:
    /**
     Run the work cycle
     */
    void runCycle(long shippedToChainMicros);

    /**
     Get the chain from craft work

     @return chain or empty if not yet available
     */
    std::optional<Chain> getChain();

    /**
     Get the segment at the given chain micros

     @param atChainMicros the chain micros
     @return the segment, or empty if not yet available
     */
    std::optional<Segment> getSegmentAtChainMicros(long atChainMicros);

    /**
     Get the segment at the given offset

     @param offset the offset
     @return the segment, or empty if not yet available
     */
    std::optional<Segment> getSegmentAtOffset(int offset);

    /**
     Get the main program for the given segment

     @param segment for which to get program
     @return main program or empty if not yet available
     */
    std::optional<Program> getMainProgram(Segment segment);

    /**
     Get the macro program for the given segment

     @param segment for which to get program
     @return macro program or empty if not yet available
     */
    std::optional<Program> getMacroProgram(Segment segment);


    /**
     Get the dubbed-to chain micros

     @return chain micros if present, else empty
     */
    std::optional<unsigned long long> getDubbedToChainMicros();

    /**
     Set the intensity override to a value between 0 and 1, or null if no override

     @param intensity the intensity override value, or null
     */
    void setIntensityOverride(std::optional<float> intensity);
  };

}// namespace XJ

#endif// XJMUSIC_WORK_DUB_WORK_H