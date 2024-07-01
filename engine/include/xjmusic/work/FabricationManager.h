// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_WORK_FABRICATION_MANAGER_H
#define XJMUSIC_WORK_FABRICATION_MANAGER_H

#include "CraftWork.h"
#include "DubWork.h"
#include "xjmusic/craft/CraftFactory.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/segment/SegmentEntityStore.h"

#include "FabricationSettings.h"
#include "FabricationState.h"

namespace XJ {

  class FabricationManager {
    CraftFactory *craftFactory = nullptr;
    FabricatorFactory *fabricatorFactory = nullptr;
    SegmentEntityStore *entityStore = nullptr;
    CraftWork *craftWork = nullptr;
    DubWork *dubWork = nullptr;
    ContentEntityStore *content = nullptr;
    FabricationSettings config = FabricationSettings();
    FabricationState state = Standby;
    bool isAudioLoaded = false;
    long long startedAtMillis = 0;
    bool running = false;

    /**
    * Construct a new FabricationManager
    * @param craftFactory  for craft
    * @param fabricatorFactory  for fabrication
    * @param store  for segments
    */
    FabricationManager(
        CraftFactory *craftFactory,
        FabricatorFactory *fabricatorFactory,
        SegmentEntityStore *store);

    /**
     Start work.
     * @param content for fabrication
     * @param config for fabrication
     */
    void start(
        ContentEntityStore *content,
        FabricationSettings config);

    /**
     Stop work
     */
    void finish(bool cancelled);

    /**
    * Run the tick cycle
    * (between 1 and 10 times per second)
    * TODO implement this in the app-- maybe returns the list of audio that should be queued up for playback in a structured way
    */
    void tick();

    /**
     Get the entity store
     */
    SegmentEntityStore *getEntityStore() const;

    /**
     Reset the factory including the segment manager and its store
     */
    void reset();

    /**
     Get the Hub content source material

     @return source material
     */
    ContentEntityStore *getSourceMaterial() const;

    /**
     Return the current dubbed-to sync chain micros

     @return chain micros, else empty
     */
    std::optional<unsigned long long> getDubbedToChainMicros() const;

    /**
     Return the current crafted-to chain micros

     @return chain micros, else empty
     */
    std::optional<unsigned long long> getCraftedToChainMicros() const;

    /**
     @return the current work state
     */
    FabricationState getWorkState() const;

    /**
     Go to the given macro program right away
     https://github.com/xjmusic/xjmusic/issues/197

     @param macroProgram to go to
     */
    void doOverrideMacro(const Program *macroProgram) const;

    /**
     @return the meme taxonomy from the current template configuration
     */
    std::optional<MemeTaxonomy> getMemeTaxonomy() const;

    /**
     * @return all macro programs in alphabetical order
     */
    std::vector<const Program *> getAllMacroPrograms() const;

    /**
     Manually go to a specific taxonomy category meme, and force until reset
     https://github.com/xjmusic/xjmusic/issues/199

     @param memes specific (assumed allowably) set of taxonomy category memes
     */
    void doOverrideMemes(const std::set<std::string> &memes) const;

    /**
     Get whether an override happened, and reset its state after getting

     @return true if an override happened
     */
    bool getAndResetDidOverride() const;

    /**
     Set the intensity override to a value between 0 and 1, or null if no override

     @param intensity the intensity to set
     */
    void setIntensityOverride(std::optional<float> intensity) const;

  private:
    /**
     * Run the control cycle
     */
    void runControlCycle();

    /**
     * Run the craft cycle
     */
    void runCraftCycle();

    /**
     * Run the dub cycle
     */
    void runDubCycle();

    /**
     Initialize the work
     */
    void initialize();

   /**
    @return true if initialized
    */
   bool isInitialized() const;

   /**
    Log and of segment message of error that job failed while (message)

    @param msgWhile phrased like "Doing work"
    @param e        exception (optional)
    */
   void didFailWhile(std::string msgWhile, std::exception e);

    /**
     Update the current work state

     @param fabricationState work state
     */
    void updateState(FabricationState fabricationState);
  };

}// namespace XJ

#endif// XJMUSIC_WORK_FABRICATION_MANAGER_H