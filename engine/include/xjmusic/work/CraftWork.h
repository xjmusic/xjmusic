// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO implement this

#ifndef XJMUSIC_WORK_CRAFT_WORK_H
#define XJMUSIC_WORK_CRAFT_WORK_H

#include <optional>
#include <set>
#include <string>

#include "xjmusic/craft/CraftFactory.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/segment/SegmentEntityStore.h"
#include "Work.h"

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
  class CraftWork : Work {
    CraftFactory *craftFactory;
    FabricatorFactory* fabricatorFactory;
    SegmentEntityStore* store;
    ContentEntityStore* content;
    bool running = new bool(true);
    double outputFrameRate;
    int outputChannels;
    long craftAheadMicros;
    TemplateConfig templateConfig;
    const Chain * chain;
    long persistenceWindowMicros;
    bool nextCycleRewrite = false;
    std::optional<Program> nextCycleOverrideMacroProgram = std::nullopt;
    std::set<std::string> nextCycleOverrideMemes = {};
    bool didOverride = new bool(false);

  public:
    /**
     Run the work cycle
     */
    void runCycle(long shippedToChainMicros, long dubbedToChainMicros);

    /**
     Get the current chain, if loaded

     @return the current chain
     */
    std::optional <Chain> getChain();

    /**
     Get the template config, if loaded

     @return the template config
     */
    TemplateConfig getTemplateConfig();

    /**
     Get the segments spanning the given time range, if they are ready- if not, return an empty list

     @param planFromChainMicros the start time
     @param planToChainMicros   the end time
     @return the segments spanning the given time range, or an empty list if the segment span is not ready
     */
    std::vector <Segment> getSegmentsIfReady(unsigned long long planFromChainMicros, unsigned long long planToChainMicros);

    /**
     Get the segment at the given chain microseconds, if it is ready
     segment beginning <= chain microseconds < end

     @param chainMicros the microseconds since beginning of chain for which to get the segment
     @return the segment at the given chain microseconds, or an empty optional if the segment is not ready
     */
    std::optional <Segment> getSegmentAtChainMicros(long chainMicros);

    /**
     Get the segment at the given offset, if it is ready

     @param offset of segment
     @return the segment at the given offset
     */
    std::optional <Segment> getSegmentAtOffset(int offset);

    /**
     Get the segments spanning the given time range, if they are ready- if not, return an empty list

     @param segments the segments for which to get picks
     @return the picks for the given segments
     */
    std::set <SegmentChoiceArrangementPick> getPicks(std::set<Segment> segments);

    /**
     Get the instrument for the given pick

     @param audio the audio for which to get instrument
     @return the instrument for the given pick
     */
    Instrument getInstrument(InstrumentAudio audio);

    /**
     Get the audio for the given pick

     @param pick the pick for which to get audio
     @return the audio for the given pick
     */
    InstrumentAudio getInstrumentAudio(SegmentChoiceArrangementPick pick);

    /**
     Check whether the given pick is muted (by its choice)

     @param pick the pick for which to get audio
     @return true if the given pick is muted
     */
    bool isMuted(SegmentChoiceArrangementPick pick);

    /**
     Get the main program for the given segment

     @param segment for which to get main program
     @return the main program for the given segment, or empty if not chosen
     */
    std::optional <Program> getMainProgram(Segment segment);

    /**
     Get the macro program for the given segment

     @param segment for which to get macro program
     @return the macro program for the given segment, or empty if not chosen
     */
    std::optional <Program> getMacroProgram(Segment segment);

    /**
     Get the source material
     */
    ContentEntityStore getSourceMaterial();

    /**
     Get the crafted-to chain micros

     @return the crafted-to chain micros
     */
    std::optional <unsigned long long> getCraftedToChainMicros();

    /**
     Whether the current craft state is ready

     @return true if ready
     */
    bool isReady();

    /**
     Go to the given macro program right now
     https://github.com/xjmusic/xjmusic/issues/197

     @param macroProgram the macro program to go to
     */
    void doOverrideMacro(Program macroProgram);

    /**
     Manually go to a specific taxonomy category meme, and force until reset
     https://github.com/xjmusic/xjmusic/issues/199

     @param memes specific (assumed allowably) set of taxonomy category memes
     */
    void doOverrideMemes(std::set<std::string> memes);

    /**
     Get whether an override happened, and reset its state after getting

     @return true if an override happened
     */
    bool getAndResetDidOverride();

  private:
    const Chain * createChainForTemplate(const Template* tmpl);
  };

}// namespace XJ

#endif// XJMUSIC_WORK_CRAFT_WORK_H