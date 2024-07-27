// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_WORK_FABRICATION_MANAGER_H
#define XJMUSIC_WORK_FABRICATION_MANAGER_H

#include "xjmusic/audio/ActiveAudio.h"
#include "xjmusic/content/ContentEntityStore.h"
#include "xjmusic/segment/SegmentEntityStore.h"

#include "CraftWork.h"
#include "DubWork.h"
#include "WorkSettings.h"
#include "WorkState.h"
#include "xjmusic/audio/AudioScheduleEvent.h"

namespace XJ {
  class WorkManager {
    SegmentEntityStore *store;
    ContentEntityStore *content;
    WorkSettings config = WorkSettings();
    CraftWork craftWork;
    DubWork dubWork;
    std::optional<MemeTaxonomy> memeTaxonomy{};
    WorkState state = Standby;
    std::map<std::string, ActiveAudio> activeAudioMap;
    bool isAudioLoaded = false;
    long long startedAtMillis = 0;

  public:
    /**
    * Construct a new WorkManager
    * @param store  for segments
    * @param content for source material
    * @param config work settings
    */
    explicit WorkManager(
        SegmentEntityStore *store,
        ContentEntityStore *content,
        const WorkSettings &config);

    /**
     Start work.
     */
    void start();

    /**
     Stop work
     */
    void finish(bool cancelled);

    /**
    * Run the tick cycle
    * (1-3 times per second)
    * This returns the list of audio that should be queued up for playback in a structured way
    */
    std::set<AudioScheduleEvent> runCycle(unsigned long long atChainMicros);

    /**
     Get the entity store
     */
    [[nodiscard]] SegmentEntityStore *getEntityStore() const;

    /**
     Get the Hub content source material

     @return source material
     */
    [[nodiscard]] ContentEntityStore *getSourceMaterial() const;

    /**
     @return the current work state
     */
    [[nodiscard]] WorkState getState() const;

    /**
     Go to the given macro program right away
     https://github.com/xjmusic/xjmusic/issues/197

     @param macroProgram to go to
     */
    void doOverrideMacro(const Program *macroProgram);

    /**
     @return the meme taxonomy from the current template configuration
     */
    [[nodiscard]] std::optional<MemeTaxonomy> getMemeTaxonomy() const;

    /**
     * @return all macro programs in alphabetical order
     */
    [[nodiscard]] std::vector<const Program *> getAllMacroPrograms() const;

    /**
     Manually go to a specific taxonomy category meme, and force until reset
     https://github.com/xjmusic/xjmusic/issues/199

     @param memes specific (assumed allowably) set of taxonomy category memes
     */
    void doOverrideMemes(const std::set<std::string> &memes);

    /**
     Get whether an override happened, and reset its state after getting

     @return true if an override happened
     */
    bool getAndResetDidOverride();

    /**
     Set the intensity override to a value between 0 and 1, or null if no override

     @param intensity the intensity to set
     */
    void setIntensityOverride(std::optional<float> intensity);

  private:
    /**
     * Run the craft cycle
     */
    void runCraftCycle(unsigned long long atChainMicros);

    /**
     * Run the dub cycle
     */
    std::set<ActiveAudio> runDubCycle(unsigned long long atChainMicros);

    /**
    Log and of segment message of error that job failed while (message)

    @param msgWhile phrased like "Doing work"
    @param e        exception (optional)
    */
    void didFailWhile(std::string msgWhile, const std::exception &e);

    /**
    * Get string representation of the work state
    * @param state  work state
    * @return  string representation
    */
    static std::string toString(WorkState state);

    /**
     Update the current work state

     @param fabricationState work state
     */
    void updateState(WorkState fabricationState);
  };

}// namespace XJ

#endif// XJMUSIC_WORK_FABRICATION_MANAGER_H