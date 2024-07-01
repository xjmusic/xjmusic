// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO implement this

#ifndef XJMUSIC_WORK_FABRICATION_MANAGER_H
#define XJMUSIC_WORK_FABRICATION_MANAGER_H

#include "xjmusic/craft/CraftFactory.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/segment/SegmentEntityStore.h"

#include "FabricationState.h"
#include "FabricationSettings.h"

namespace XJ {

  class FabricationManager {
    CraftFactory craftFactory;
    FabricatorFactory fabricatorFactory;
    SegmentEntityStore entityStore;
    FabricationState state = FabricationState::Standby;
    bool isAudioLoaded = false;
    long long startedAtMillis = 0;
    bool running = false;
    CraftWork craftWork;
    DubWork dubWork;
    FabricationSettings config;
    ContentEntityStore content;


    /**
     Start work.
     * @param content for fabrication
     * @param config for fabrication
     */
    void start(
        ContentEntityStore content,
        FabricationSettings config
    );

    /**
     Stop work
     */
    void finish(boolean cancelled);

    /**
     Get the entity store
     */
    SegmentEntityStore getEntityStore();

    /**
     Reset the factory including the segment manager and its store
     */
    void reset();

    /**
     Get the Hub content source material

     @return source material
     */
    ContentEntityStore getSourceMaterial();

    /**
     Return the current shipped-to chain micros

     @return chain micros, else empty
     */
    std::optional <Long> getShippedToChainMicros();

    /**
     Return the current dubbed-to sync chain micros

     @return chain micros, else empty
     */
    std::optional <Long> getDubbedToChainMicros();

    /**
     Return the current crafted-to chain micros

     @return chain micros, else empty
     */
    std::optional <Long> getCraftedToChainMicros();

    /**
     @return the current work state
     */
    FabricationState getWorkState();

    /**
     Set the on progress callback

     @param onProgress callback
     */
    void setOnProgress(

    @
    Nullable Consumer<Float>
    onProgress);

    /**
     Set the on progress label callback

     @param onProgressLabel callback
     */
    void setOnProgressLabel(

    @
    Nullable Consumer<String>
    onProgressLabel);

    /**
     Set the on status callback

     @param onStateChange callback
     */
    void setOnStateChange(Consumer <FabricationState> onStateChange);

    /**
     Go to the given macro program right away
     https://github.com/xjmusic/xjmusic/issues/197

     @param macroProgram to go to
     */
    void doOverrideMacro(Program macroProgram);

    /**
     @return the meme taxonomy from the current template configuration
     */
    std::optional <MemeTaxonomy> getMemeTaxonomy();

    /**
     * @return all macro programs in alphabetical order
     */
    List <Program> getAllMacroPrograms();

    /**
     Manually go to a specific taxonomy category meme, and force until reset
     https://github.com/xjmusic/xjmusic/issues/199

     @param memes specific (assumed allowably) set of taxonomy category memes
     */
    void doOverrideMemes(Collection <String> memes);

    /**
     Get whether an override happened, and reset its state after getting

     @return true if an override happened
     */
    boolean getAndResetDidOverride();

    /**
     Set the intensity override to a value between 0 and 1, or null if no override

     @param intensity the intensity to set
     */
    void setIntensityOverride(

    @
    Nullable Double
    intensity);
  };

}// namespace XJ

#endif// XJMUSIC_WORK_FABRICATION_MANAGER_H