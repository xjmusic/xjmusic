// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_ENGINE_H
#define XJMUSIC_ENGINE_H

#include "work/WorkManager.h"

#include <string>

namespace XJ {

  class Engine {
    WorkSettings settings;
    std::unique_ptr<SegmentEntityStore> store;
    std::unique_ptr<ContentEntityStore> projectContent;
    std::unique_ptr<ContentEntityStore> templateContent;
    std::unique_ptr<WorkManager> work;
    std::filesystem::path pathToBuildDirectory;

  public:
    /**
    * Construct a new Engine
    * @param pathToProjectFile     path to the .xj project file from which to load content
    * @param controlMode      the fabrication control mode
    * @param craftAheadSeconds (optional) how many seconds ahead to craft
    * @param dubAheadSeconds  (optional) how many seconds ahead to dub
    * @param deadlineSeconds  (optional) audio scheduling deadline in seconds
    * @param persistenceWindowSeconds (optional) how long to keep segments in memory
    */
    explicit Engine(
        const std::optional<std::string>& pathToProjectFile,
        std::optional<Fabricator::ControlMode> controlMode,
        std::optional<int> craftAheadSeconds,
        std::optional<int> dubAheadSeconds,
        std::optional<int> deadlineSeconds,
        std::optional<int> persistenceWindowSeconds);

    /**
     Start work with the given template identifier.
     1. Clear the segment entity store
     2. Reload the project content
     3. Load the template content
     4. Begin fabrication work
     @param templateIdentifier identify the template to fabricate
     */
    void start(const std::optional<std::string> &templateIdentifier);

    /**
     Stop work
     */
    void finish(bool cancelled) const;

    /**
    * Run the tick cycle
    * (1-3 times per second)
    * This returns the list of audio that should be queued up for playback in a structured way
    */
    [[nodiscard]] std::set<AudioScheduleEvent> RunCycle(unsigned long long atChainMicros) const;

    /**
     Get the entity store
     */
    [[nodiscard]] SegmentEntityStore *getSegmentStore() const;

    /**
     Get all the content loaded for the project

     @return source material
     */
    [[nodiscard]] ContentEntityStore *getProjectContent() const;

    /**
     Get all the content for the working template
     * @return
     */
    [[nodiscard]] ContentEntityStore *getTemplateContent() const;

    /**
     @return the current work state
     */
    [[nodiscard]] WorkState getWorkState() const;

    /**
     @return the meme taxonomy from the current template configuration
     */
    [[nodiscard]] std::optional<MemeTaxonomy> getMemeTaxonomy() const;

    /**
     * @return all macro programs in alphabetical order
     */
    [[nodiscard]] std::vector<const Program *> getAllMacroPrograms() const;

    /**
     Go to the given macro program right away
     https://github.com/xjmusic/xjmusic/issues/197

     @param macroProgram to go to
     */
    void doOverrideMacro(const Program *macroProgram) const;

    /**
     Manually go to a specific taxonomy category meme, and force until reset
     https://github.com/xjmusic/xjmusic/issues/199

     @param memes specific (assumed allowably) set of taxonomy category memes
     */
    void doOverrideMemes(const std::set<std::string> &memes) const;

    /**
     Set the intensity override to a value between 0 and 1, or null if no override

     @param intensity the intensity to set
     */
    void setIntensityOverride(std::optional<float> intensity) const;

    /**
     Get the path to the build directory
     */
    std::filesystem::path getPathToBuildDirectory();

    /**
     * Get the work settings
     * @return  the settings
     */
    [[nodiscard]] WorkSettings getSettings() const;

    /**
     * Virtual destructor
     */
    ~Engine();

  private:
    /**
     * Load the project content
     */
    void loadProjectContent(const std::basic_string<char>& pathToProjectFile);
  };

}// namespace XJ

#endif//XJMUSIC_ENGINE_H
