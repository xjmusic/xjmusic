// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO implement this

#ifndef XJMUSIC_WORK_CRAFT_WORK_H
#define XJMUSIC_WORK_CRAFT_WORK_H

#include <optional>
#include <set>
#include <string>

#include "Work.h"
#include "xjmusic/craft/CraftFactory.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/segment/SegmentEntityStore.h"

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
  class CraftWork final : Work {
    CraftFactory *craftFactory = nullptr;
    FabricatorFactory *fabricatorFactory = nullptr;
    SegmentEntityStore *store = nullptr;
    ContentEntityStore *content = nullptr;
    bool running = new bool(true);
    double outputFrameRate = 0;
    int outputChannels = 0;
    long craftAheadMicros = 0;
    TemplateConfig templateConfig;
    const Chain *chain = nullptr;
    long persistenceWindowMicros = 0;
    bool nextCycleRewrite = false;
    std::optional<Program*> nextCycleOverrideMacroProgram = std::nullopt;
    std::set<std::string> nextCycleOverrideMemes = {};
    bool didOverride = new bool(false);

  public:
    CraftWork(
        CraftFactory *craftFactory,
        FabricatorFactory *fabricatorFactory,
        SegmentEntityStore *store,
        ContentEntityStore *content,
        long persistenceWindowSeconds,
        long craftAheadSeconds);

    void finish() override;
    bool isFinished() override;


   /**
    This is the internal cycle that's run indefinitely
    */
   void runCycle(long shippedToChainMicros, long dubbedToChainMicros);

    /**
     Get the current chain, if loaded

     @return the current chain
     */
    const Chain *getChain() const;

    /**
     Get the template config, if loaded

     @return the template config
     */
    TemplateConfig getTemplateConfig();

    /**
     Get the segments spanning the given time range, if they are ready- if not, return an empty list

     @param fromChainMicros the start time
     @param toChainMicros   the end time
     @return the segments spanning the given time range, or an empty list if the segment span is not ready
     */
    std::vector<const Segment *> getSegmentsIfReady(unsigned long long fromChainMicros, unsigned long long toChainMicros) const;

    /**
     Get the segment at the given chain microseconds, if it is ready
     segment beginning <= chain microseconds < end

     @param chainMicros the microseconds since beginning of chain for which to get the segment
     @return the segment at the given chain microseconds, or an empty optional if the segment is not ready
     */
    std::optional<Segment *> getSegmentAtChainMicros(unsigned long long chainMicros) const;

    /**
     Get the segment at the given offset, if it is ready

     @param offset of segment
     @return the segment at the given offset
     */
    std::optional<Segment *> getSegmentAtOffset(int offset) const;

    /**
     Get the segments spanning the given time range, if they are ready- if not, return an empty list

     @param segments the segments for which to get picks
     @return the picks for the given segments
     */
    std::set<SegmentChoiceArrangementPick *> getPicks(const std::vector<const Segment *> & segments);

    /**
     Get the instrument for the given pick

     @param audio the audio for which to get instrument
     @return the instrument for the given pick
     */
    const Instrument *getInstrument(const InstrumentAudio *audio) const;

    /**
     Get the audio for the given pick

     @param pick the pick for which to get audio
     @return the audio for the given pick
     */
    const InstrumentAudio *getInstrumentAudio(const SegmentChoiceArrangementPick *pick) const;

    /**
     Check whether the given pick is muted (by its choice)

     @param pick the pick for which to get audio
     @return true if the given pick is muted
     */
    bool isMuted(SegmentChoiceArrangementPick *pick) const;

    /**
     Get the main program for the given segment

     @param segment for which to get main program
     @return the main program for the given segment, or empty if not chosen
     */
    std::optional<const Program *> getMainProgram(const Segment *segment) const;

    /**
     Get the macro program for the given segment

     @param segment for which to get macro program
     @return the macro program for the given segment, or empty if not chosen
     */
    std::optional<const Program *> getMacroProgram(const Segment &segment) const;

    /**
     Get the source material
     */
    ContentEntityStore *getSourceMaterial() const;

    /**
     Get the crafted-to chain micros

     @return the crafted-to chain micros
     */
    std::optional<unsigned long long> getCraftedToChainMicros() const;

    /**
     Whether the current craft state is ready

     @return true if ready
     */
    bool isReady() const;

    /**
     Go to the given macro program right now
     https://github.com/xjmusic/xjmusic/issues/197

     @param macroProgram the macro program to go to
     */
    void doOverrideMacro(Program *macroProgram);

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
    const Chain *createChainForTemplate(const Template *tmpl) const;
    void didFailWhile(std::string msgWhile, const std::exception &e);
    static void updateSegmentState(Fabricator *fabricator, Segment *segment, Segment::State fromState, Segment::State toState);

   /**
    Fabricate the chain based on craft state
    <p>
    Only ready to dub after at least one craft cycle is completed since the last time we weren't ready to dub live performance modulation https://github.com/xjmusic/xjmusic/issues/197

    @param dubbedToChainMicros already dubbed to here
    @param craftToChainMicros  target to craft until
    @throws FabricationFatalException if the chain cannot be fabricated
    */
   void doFabrication(long dubbedToChainMicros, long craftToChainMicros);

   /**
    Default behavior is to fabricate the next segment if we are not crafted enough ahead, otherwise skip

    @param toChainMicros to target chain micros
    @param overrideMacroProgram to override fabrication
    @param overrideMemes to override fabrication
    @throws FabricationFatalException if the chain cannot be fabricated
    */
   void doFabricationDefault(unsigned long long toChainMicros, std::optional<Program *> overrideMacroProgram, const std::set<std::string> &overrideMemes);

    /**
    Override behavior deletes all future segments and re-fabricates starting with the given parameters
    <p>
    Macro program override
    https://github.com/xjmusic/xjmusic/issues/197
    <p>
    Memes override
    https://github.com/xjmusic/xjmusic/issues/199

    @param dubbedToChainMicros  already dubbed to here
    @param overrideMacroProgram to override fabrication
    @param overrideMemes        to override fabrication
    @throws FabricationFatalException if the chain cannot be fabricated
    */
   void doFabricationRewrite(unsigned long long dubbedToChainMicros, std::optional<Program *> overrideMacroProgram, std::set<std::string> overrideMemes);

   /**
    Cut the current segment short after the given number of beats

    @param segment          to cut short
    @param cutoffAfterBeats number of beats to cut short after
    */
   void doCutoffLastSegment(Segment *segment, double cutoffAfterBeats);

   /**
    Craft a Segment, or fail

    @param segment              to craft
    @param overrideSegmentType  to use for crafting
    @param overrideMacroProgram to override fabrication
    @param overrideMemes        to override fabrication
    @ on configuration failure
    @ on craft failure
    */
   void doFabricationWork(Segment *segment, std::optional<Segment::Type> overrideSegmentType, std::optional<Program *> overrideMacroProgram, const std::set<std::string> &overrideMemes) const;
   void doSegmentCleanup(long shippedToChainMicros) const;
   Segment *buildSegmentInitial() const;
   Segment *buildSegmentFollowing(const Segment *last) const;

   /**
    If memes/macro already engaged at fabrication start (which is always true in a manual control mode),
    the first segment should be governed by that selection
    https://github.com/xjmusic/xjmusic/issues/201
    */
   void doNextCycleRewriteUnlessInitialSegment();
  };

}// namespace XJ

#endif// XJMUSIC_WORK_CRAFT_WORK_H