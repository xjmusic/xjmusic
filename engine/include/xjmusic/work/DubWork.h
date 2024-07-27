// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_WORK_DUB_WORK_H
#define XJMUSIC_WORK_DUB_WORK_H

#include <optional>

#include "xjmusic/audio/ActiveAudio.h"

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
  class DubWork final {
    int BITS_PER_BYTE = 8;
    bool running = true;
    CraftWork *craftWork;
    TemplateConfig templateConfig;
    unsigned long dubAheadMicros = 0;

    // Intensity override is null if no override, or a value between 0 and 1
    std::optional<float> intensityOverride = std::nullopt;

    // Next/Prev intensity updated each segment, either from segment or from override
    std::optional<float> nextIntensity = std::nullopt;
    std::optional<float> prevIntensity = std::nullopt;

  public:
    DubWork(CraftWork *craftWork, int dubAheadSeconds);

    /**
     * Start work
     */
    void start();

    /**
     Stop work
     */
    void finish();

    /**
    Check whether the craft work is finished

    @return true if finished (not running)
    */
    bool isFinished() const;

    /**
     Run the work cycle
     */
    std::set<ActiveAudio> runCycle(unsigned long long atChainMicros);

    /**
    Do dub frame
    <p>
    instead of mixing to file, mix to memory (produce to a BytePipeline) and let ship work consume the buffer
    use the same mixer from chunk to chunk, only changing the active audios
    <p>
    Ensure mixer has continuity of its processes/effects, e.g. the compressor levels at the last frame of the last chunk are carried over to the first frame of the next chunk
    */
    std::set<ActiveAudio> computeActiveAudios(unsigned long long atChainMicros);

    /**
     Get the segment at the given chain micros

     @param atChainMicros the chain micros
     @return the segment, or empty if not yet available
     */
    std::optional<const Segment *> getSegmentAtChainMicros(long atChainMicros) const;

    /**
     Get the segment at the given offset

     @param offset the offset
     @return the segment, or empty if not yet available
     */
    std::optional<const Segment *> getSegmentAtOffset(int offset) const;

    /**
     Get the main program for the given segment

     @param segment for which to get program
     @return main program or empty if not yet available
     */
    std::optional<const Program *> getMainProgram(const Segment *segment) const;

    /**
     Get the macro program for the given segment

     @param segment for which to get program
     @return macro program or empty if not yet available
     */
    std::optional<const Program *> getMacroProgram(const Segment &segment) const;

    /**
     Set the intensity override to a value between 0 and 1, or null if no override

     @param intensity the intensity override value, or null
     */
    void setIntensityOverride(std::optional<float> intensity);

  private:
    /**
    Log and of segment message of error that job failed while (message)

    @param msgWhile phrased like "Doing work"
    @param e        exception (optional)
    */
    void didFailWhile(std::string msgWhile, const std::exception &e);
  };

}// namespace XJ

#endif// XJMUSIC_WORK_DUB_WORK_H