// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_ENGINE_LIBRARY_H
#define XJMUSIC_ENGINE_LIBRARY_H

#include "work/WorkManager.h"


#include <string>

namespace XJ {

  class Engine {
    WorkManager *workManager;

  public:
    /**
    * Construct a new WorkManager
    * @param store  for segments
    * @param content for source material
    * @param config work settings
    */
    explicit Engine(
        SegmentEntityStore *store,
        ContentEntityStore *content,
        const WorkSettings &config);

    /**
     Start work.
     */
   void start() const;

    /**
     Stop work
     */
   void finish(bool cancelled) const;

    /**
    * Run the tick cycle
    * (1-3 times per second)
    * This returns the list of audio that should be queued up for playback in a structured way
    */
   std::set<ActiveAudio> runCycle(unsigned long long atChainMicros) const;

    /**
     Get the entity store
     */
    SegmentEntityStore *getEntityStore() const;

    /**
     Get the Hub content source material

     @return source material
     */
    ContentEntityStore *getSourceMaterial() const;

    /**
     @return the current work state
     */
    WorkState getWorkState() const;

    /**
     @return the meme taxonomy from the current template configuration
     */
    std::optional<MemeTaxonomy> getMemeTaxonomy() const;

    /**
     * @return all macro programs in alphabetical order
     */
    std::vector<const Program *> getAllMacroPrograms() const;

    /**
     Go to the given macro program right away
     https://github.com/xjmusic/xjmusic/issues/197

     @param macroProgram to go to
     */
    void doOverrideMacro(const Program *macroProgram);

    /**
     Manually go to a specific taxonomy category meme, and force until reset
     https://github.com/xjmusic/xjmusic/issues/199

     @param memes specific (assumed allowably) set of taxonomy category memes
     */
    void doOverrideMemes(const std::set<std::string> &memes);

    /**
     Set the intensity override to a value between 0 and 1, or null if no override

     @param intensity the intensity to set
     */
    void setIntensityOverride(std::optional<float> intensity);

    /**
     * Virtual destructor
     */
    ~Engine();
  };

}// namespace XJ

#endif//XJMUSIC_ENGINE_LIBRARY_H
