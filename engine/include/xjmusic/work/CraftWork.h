// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_WORK_CRAFT_WORK_H
#define XJMUSIC_WORK_CRAFT_WORK_H

#include <optional>
#include <set>
#include <string>

#include "xjmusic/content/ContentEntityStore.h"
#include "xjmusic/fabricator/Fabricator.h"
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
  class CraftWork final {
    SegmentEntityStore *store;
    ContentEntityStore *content;
    bool running = true;
    long craftAheadMicros = 0;
    long persistenceWindowMicros = 0;
    bool nextCycleRewrite = false;
    std::optional<const Program *> nextCycleOverrideMacroProgram = std::nullopt;
    std::set<std::string> nextCycleOverrideMemes = {};
    bool didOverride = false;

  public:
    explicit CraftWork(
        SegmentEntityStore *store,
        ContentEntityStore *content,
        long persistenceWindowSeconds,
        long craftAheadSeconds);

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
    This is the internal cycle that's Run indefinitely
    */
    void runCycle(const unsigned long long int atChainMicros);

    /**
     Get the template config, if loaded

     @return the template config
     */
    TemplateConfig getTemplateConfig() const;

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
    std::optional<const Segment *> getSegmentAtChainMicros(unsigned long long chainMicros) const;

    /**
     Get the segment at the given offset, if it is ready

     @param offset of segment
     @return the segment at the given offset
     */
    std::optional<const Segment *> getSegmentAtOffset(int offset) const;

    /**
     Get the segments spanning the given time range, if they are ready- if not, return an empty list

     @param segments the segments for which to get picks
     @return the picks for the given segments
     */
    std::set<const SegmentChoiceArrangementPick *> getPicks(const std::vector<const Segment *> &segments) const;

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
    bool isMuted(const SegmentChoiceArrangementPick *pick) const;

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
     Whether the current craft state is ready

     @return true if ready
     */
    bool isReady() const;

    /**
     Go to the given macro program right now
     https://github.com/xjmusic/xjmusic/issues/197

     @param macroProgram the macro program to go to
     */
    void doOverrideMacro(const Program *macroProgram);

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

    /**
     * Get all choices for the given segment
     * @param segment for which to get choices
     * @return  choices
     */
    std::set<const SegmentChoice *> getChoices(const Segment *segment) const;

    /**
     * Get all arrangements for the given choice
     * @param choice for which to get arrangements
     * @return  arrangements
     */
    std::set<const SegmentChoiceArrangement *> getArrangements(const SegmentChoice *choice) const;

    /**
     * Get all picks for the given arrangement
     * @param arrangement for which to get picks
     * @return  picks
     */
    std::set<const SegmentChoiceArrangementPick *> getPicks(const SegmentChoiceArrangement *arrangement) const;

  private:
    /**
     Log and send notification of error that job failed while (message)
    
     @param msgWhile phrased like "Doing work"
     @param e        exception (optional)
     */
    void didFailWhile(const std::string& msgWhile, const std::exception &e);

    /**
     Update Segment to Working state
    
     @param fabricator to update
     @param inputSegment    to update
     @param fromState  of existing segment
     @param toState    of new segment
     @ if record is invalid
     */
    static const Segment *
    updateSegmentState(Fabricator *fabricator, const Segment *inputSegment, const Segment::State fromState, const Segment::State toState);

    /**
    Fabricate the chain based on craft state
    <p>
    Only ready to dub after at least one craft cycle is completed since the last time we weren't ready to dub live performance modulation https://github.com/xjmusic/xjmusic/issues/197

    @param craftToChainMicros  target to craft until
    @throws FabricationFatalException if the chain cannot be fabricated
    */
    void doFabrication(const unsigned long long int craftToChainMicros);

    /**
    Default behavior is to fabricate the next segment if we are not crafted enough ahead, otherwise skip

    @param toChainMicros to target chain micros
    @param overrideMacroProgram to override fabrication
    @param overrideMemes to override fabrication
    @throws FabricationFatalException if the chain cannot be fabricated
    */
    void doFabricationDefault(unsigned long long toChainMicros, const std::optional<const Program *> overrideMacroProgram, const std::set<std::string> &overrideMemes);

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
    void doFabricationRewrite(unsigned long long dubbedToChainMicros, std::optional<const Program *> overrideMacroProgram, std::set<std::string> overrideMemes);

    /**
    Cut the current segment short after the given number of beats

    @param inputSegment          segment to cut short
    @param cutoffAfterBeats number of beats to cut short after
    */
    void doCutoffLastSegment(const Segment *inputSegment, float cutoffAfterBeats) const;

    /**
    Craft a Segment, or fail

    @param inputSegment              to craft
    @param overrideSegmentType  to use for crafting
    @param overrideMacroProgram to override fabrication
    @param overrideMemes        to override fabrication
    @ on configuration failure
    @ on craft failure
    */
    void doFabricationWork(const Segment *inputSegment, std::optional<Segment::Type> overrideSegmentType, const std::optional<const Program *> overrideMacroProgram, const std::set<std::string> &overrideMemes) const;

    /**
     Delete segments before the given shipped-to chain micros
    
     @param shippedToChainMicros the shipped-to chain micros
     */
    void doSegmentCleanup(const unsigned long long int shippedToChainMicros) const;

    /**
     Create the initial template segment
    
     @return initial template segment
     */
    Segment buildSegmentInitial() const;

    /**
     Create the next segment in the chain, following the last segment
    
     @param last segment
     @return next segment
     */
    Segment buildSegmentFollowing(const Segment *last) const;

    /**
    If memes/macro already engaged at fabrication start (which is always true in a manual control mode),
    the first segment should be governed by that selection
    https://github.com/xjmusic/xjmusic/issues/201
    */
    void doNextCycleRewriteUnlessInitialSegment();
  };

}// namespace XJ

#endif// XJMUSIC_WORK_CRAFT_WORK_H